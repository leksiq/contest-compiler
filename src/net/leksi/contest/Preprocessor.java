/*
 * The MIT License
 *
 * Copyright 2020 Alexey Zakharov <leksi@leksi.net>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.leksi.contest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Preprocessor {

    enum WaitingFor {COMPILED, CLASS, METHOD_OR_FIELD, METHOD, FIELD, CODE, OPER, EXCEPTION_TABLE, TABLE_SWITCH, LOOKUP_SWITCH, DONE, NONE};
    
    static final String TOKENIZER = "\\s+|,|<|>|\\[|\\]|&|:";
    static final String KEYWORDS = "public|private|protected|final|abstract|static|extends|implements|throws|void|int|char|long|double|float|boolean|switch|case|break";
    static final String INDENTION = "    ";
    static final String ACCESS_KEYWORDS = "public|private|protected";
    
    Pattern pCompiled = Pattern.compile("^Compiled\\s+from\\s+\"([^.]+\\.java)\"$");
    Pattern pCode = Pattern.compile("^Code:$");
    Pattern pPrimitiveTypes = Pattern.compile("(?:I|J|D|F|Z|C|V)+");
    Pattern pMain = Pattern.compile("(?:public(?:\\s|$)+static|static(?:\\s|$)+public)(?:\\s|$)+void(?:\\s|$)+(main)(?:\\s|$)*\\(", Pattern.MULTILINE);
    Pattern pMain1 = Pattern.compile("(?:\\s|$)*(_+main)(?:\\s|$)*\\(", Pattern.MULTILINE);
    
    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            new Preprocessor().run(args[0]);
        }
    }

    private boolean debug = false;
    private PrintStream debugPrintStream = System.out;
    private String skipPrefix = null;
    
    public void debug(final boolean debug) {
        this.debug = debug;
        System.setOut(debugPrintStream);
    }

    public void debugPrintStream(final PrintStream debugPrintStream) {
        this.debugPrintStream = debugPrintStream;
        debug(this.debug);
    }

    public void skipPrefix(final String skipPrefix) {
        this.skipPrefix = skipPrefix;
    }

    static class JavaSource {
        File file;
        JarFile jarFile;
        JarEntry jarEntry;
    }
    
    private String url_decode(String url) {
        try {
            return URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            return url;
        }
    }

    public void run(final String arg) {
//        System.out.println(arg);

        

        String classPath = Arrays.stream(((URLClassLoader) (Thread.currentThread().
                getContextClassLoader())).getURLs()).map(v -> url_decode(v.getPath())).collect(Collectors.joining(File.pathSeparator));
        if(debug) { System.out.println("classpath: " + classPath); }
        
        Map<String, String> env = System.getenv();
        Properties props = System.getProperties();
        
        String dir = "";
        if(props.containsKey("net.leksi.solver.javap.dir")) {
            dir = props.getProperty("net.leksi.solver.javap.dir");
        } else if(env.containsKey("_") && env.get("_").endsWith("/java")) {
            dir = env.get("_").substring(0, env.get("_").length() - 4);
        }
        
        System.out.println(dir);
        
        String[] params = new String[6];
        params[0] = (!"".equals(dir) ? (dir + "/").replace("//", "/").
                replace("\\/", "/").replace("\\", "/") : "") + "javap";
        params[1] = "-c";
        params[2] = "-p";
        params[3] = "-classpath";
        params[4] = classPath;
        
        
        ArrayList<String> queue = new ArrayList<>();
        TreeSet<String> touched = new TreeSet<>();
        TreeSet<String> foundClasses = new TreeSet<>();
        TreeSet<String> decompiledClasses = new TreeSet<>();
        TreeSet<String> foundClassesAtMethod = new TreeSet<>();
        TreeSet<String> sources = new TreeSet<>();
        TreeSet<String> main_exceptions = new TreeSet<>();
        TreeSet<String> probed_paths = new TreeSet<>();
        String[] main_java = new String[]{null};
        boolean[] failed = new boolean[]{false};

        Predicate<String> used =  s -> {
            return foundClasses.contains(s) || touched.contains(s);
        };
        
        Tokenizer tokenizer = new Tokenizer();
        
        class Walker {
            BinaryOperator<File> find;
        }
        
        Walker walker = new Walker();
        
        BinaryOperator<File> findRecursive = (path1, filename1) -> {
            File res = null;
            if(!probed_paths.contains(path1.toString())) {
                probed_paths.add(path1.toString());
                File res1 = new File(path1.toString(), filename1.toString());
                if(res1.exists() && !res1.isDirectory()) {
                    if(debug) { System.out.println(INDENTION + "found " + res1); }
                    return res1;
                }
                res1 = new File(path1.toString(), filename1.toString().replace(".java", ".class"));
                if(res1.exists() && !res1.isDirectory()) {
                    if(debug) { System.out.println(INDENTION + "found " + res1); }
                    res = res1;
                }
                File[] files = path1.listFiles(f -> f.isDirectory());
                for(File file: files) {
                    if(debug) { System.out.println(INDENTION + "probe " + file); }
                    res1 = walker.find.apply(file, filename1);
                    if(res1 != null && !res1.toString().endsWith(".class")) {
                        return res1;
                    }
                }
            }
            return res;
        };
        
        walker.find = findRecursive;
        
        Function<File, JavaSource> getSourceFileEntry = (filename) -> {
            if(debug) { System.out.println(INDENTION + "searching " + filename); }
            JavaSource res = new JavaSource();
            Stream.concat(Stream.of("."), Arrays.stream(classPath.split(File.pathSeparator))).allMatch(path -> {
                File fPath = new File(path);
                if(debug) { System.out.println(INDENTION + "probe " + fPath); }
                if(fPath.isDirectory()) {
                    res.file = walker.find.apply(fPath, filename);
                    if(res.file != null) {
                        return false;
                    }
                } else {
                    try {
                        JarFile jar = new JarFile(fPath);
                        Enumeration<JarEntry> en = jar.entries();
                        while(en.hasMoreElements()) {
                            JarEntry je = en.nextElement();
                            if(!je.isDirectory()) {
                                if(debug) { System.out.println(INDENTION + "probe " + fPath + "!" + je); }
                                if(je.toString().equals(filename.toString().replace("\\", "/"))) {
                                    if(debug) { System.out.println(INDENTION + "found " + je); }
                                    res.jarEntry = je;
                                    res.jarFile = jar;
                                    return false;
                                }
                            }
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(Preprocessor.class.getName()).log(Level.SEVERE, null, ex);
                        return true;
                    }
                }
                return true;
            });
            return res;
        };
        
        queue.add(arg);
        
        try {
            boolean[] first = new boolean[]{true};
            while(!queue.isEmpty()) {
                foundClasses.clear();
                WaitingFor[] wf = new WaitingFor[]{WaitingFor.COMPILED};
                params[5] = queue.get(0);
                if(debug) { System.out.println("Decompile: " + params[5]); }
                queue.remove(0);
                final Process p = Runtime.getRuntime().exec(params);
                StringBuilder sb_error = new StringBuilder();

                Thread thread = new Thread() {
                    public void run() {
                        String line;

                        try(
                            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                            BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                                ) {
                            boolean skip = false;
                            String source = null;
                            while ((line = input.readLine()) != null) {
                                line = line.trim();
                                if(wf[0] == WaitingFor.EXCEPTION_TABLE) {
                                    wf[0] = WaitingFor.OPER;
                                    if(debug) { System.out.println(INDENTION + wf[0] + " OK" + (skip ? " (skipped)" : "")); }
                                }
                                if(debug) { System.out.println("javap: " + line); }
                                if(line.isEmpty()) {
                                    if(wf[0] != WaitingFor.TABLE_SWITCH && wf[0] != WaitingFor.LOOKUP_SWITCH && wf[0] != WaitingFor.NONE) {
                                        wf[0] = WaitingFor.METHOD_OR_FIELD;
                                        skip = false;
                                    }
                                } else {
                                    Matcher matcher;
                                    String[] tokens;
                                    int selector;
                                    switch(wf[0]) {
                                        case COMPILED:
                                            matcher = pCompiled.matcher(line);
                                            if(!matcher.matches()) {
                                                throw new RuntimeException("Inconsistent case: " + wf[0] + " and " + line);
                                            }
                                            source = matcher.group(1);
                                            if(debug) { System.out.println(INDENTION + wf[0] + " OK"); }
                                            wf[0] = WaitingFor.CLASS;
                                            break;
                                        case CLASS:
                                            tokens = Arrays.stream(line.split(TOKENIZER)).filter(s -> !KEYWORDS.contains(s) && !s.isEmpty()).toArray(i -> new String[i]);
                                            selector = 0;
                                            for(int i = 0; i < tokens.length; i++) {
                                                String s = tokens[i];
                                                if(selector == 0 && (s.equals("class") || s.equals("interface"))) {
                                                    selector++;
                                                    String packagePath = !tokens[i + 1].contains(".") ? null : tokens[i + 1].substring(0, tokens[i + 1].lastIndexOf(".")).replace(".", "/");
                                                    String java = (packagePath != null ? packagePath + "/" : "") + source;
                                                    if(main_java[0] == null) {
                                                        main_java[0] = java;
                                                    }
                                                    sources.add(java);
                                                    decompiledClasses.add(tokens[i + 1]);
                                                } else if(selector == 1 && s.equals("{")) {
                                                    selector++;
                                                } else if(selector == 2) {
                                                    throw new RuntimeException("Inconsistent case: " + wf[0] + " and " + line);
                                                } else {
                                                    if(!used.test(s)) {
                                                        if(debug) { System.out.println(INDENTION + "--found 1: " + s); }
                                                        foundClasses.add(s);
                                                    }
                                                }
                                            }
                                            if(debug) { System.out.println(INDENTION + wf[0] + " OK"); }
                                            wf[0] = WaitingFor.METHOD_OR_FIELD;
                                            break;
                                        case METHOD_OR_FIELD:
                                            selector = 0;
                                            foundClassesAtMethod.clear();
                                            skip = false;
                                            tokens = Arrays.stream(line.replace("(", " ( ").replace(")", " ) ").replace(";", " ; ").split(TOKENIZER)).filter(s -> !s.isEmpty()).toArray(i -> new String[i]);
                                            if(debug) { System.out.println(INDENTION + "tokens: " + Arrays.stream(tokens).collect(Collectors.joining(", ", "[", "]"))); }
                                            String name = "";
                                            boolean isPublic = false;
                                            boolean isStatic = false;
                                            boolean throwsMet = false;
                                            for(int i = 0; i < tokens.length; i++) {
                                                String s = tokens[i];
                                                if(selector == 0 && ACCESS_KEYWORDS.contains(s)) {
                                                    isPublic = "public".equals(s);
                                                } else if(selector == 0 && "static".equals(s)) {
                                                    isStatic = true;
                                                } else if(selector == 2 && "throws".equals(s)) {
                                                    throwsMet = true;
                                                } else if(KEYWORDS.contains(s)) {
                                                    //ignore keywords
                                                } else if(selector == 0 && (s.equals("("))) {
                                                    selector++;
                                                } else if(selector == 1 && s.equals(")")) {
                                                    selector++;
                                                } else if((selector == 2 || selector == 0) && s.equals(";")) {
                                                    if(selector == 0) {
                                                        name = tokens[i - 1];
                                                    }
                                                    selector++;
                                                } else {
                                                    if(selector == 0 && (tokens[i + 1].equals("(") || tokens[i + 1].equals(";"))) {
                                                        name = s;
                                                        if(skipPrefix != null && s.startsWith(skipPrefix)) {
                                                            skip = true;
                                                            if(debug) { System.out.println(INDENTION + "skip because of skipPrefix: " + s); }
                                                        }
                                                    } else {
                                                        foundClassesAtMethod.add(s);
                                                        if(first[0] && "main".equals(name) && isPublic && isStatic && throwsMet) {
                                                            main_exceptions.add(s);
                                                        }
                                                    }
                                                }
                                            }
                                            if(selector != 1 && selector != 3) {
                                                throw new RuntimeException("Inconsistent case: " + wf[0] + " and " + line);
                                            }
                                            
                                            if(debug) { System.out.println(INDENTION + (selector == 1 ? WaitingFor.FIELD : WaitingFor.METHOD) + " " + name + " OK" + (skip ? " (skipped)" : "")); }
                                            if(!skip) {
                                                foundClassesAtMethod.forEach(v -> {
                                                    if (!used.test(v)) {
                                                        if (debug) { System.out.println(INDENTION + "--found 2: " + v); }
                                                        foundClasses.add(v);
                                                    }
                                                });
                                            }
                                            wf[0] = WaitingFor.CODE;
                                            break;
                                        case CODE:
                                            matcher = pCode.matcher(line);
                                            if(!matcher.matches()) {
                                                if("}".equals(line)) {
                                                    if (debug) {
                                                        System.out.println(INDENTION + wf[0] + " OK" + (skip ? " (skipped)" : ""));
                                                    }
                                                    wf[0] = WaitingFor.METHOD_OR_FIELD;
                                                } else {
                                                    throw new RuntimeException("Inconsistent case: " + wf[0] + " and " + line);
                                                }
                                            } else {
                                                if(debug) { System.out.println(INDENTION + wf[0] + " OK" + (skip ? " (skipped)" : "")); }
                                                wf[0] = WaitingFor.OPER;
                                            }
                                            break;
                                        case TABLE_SWITCH:
                                        case LOOKUP_SWITCH:
                                            if("}".equals(line)) {
                                                wf[0] = WaitingFor.OPER;
                                                if(debug) { System.out.println(INDENTION + wf[0] + " OK" + (skip ? " (skipped)" : "")); }
                                                break;
                                            }
                                            break;
                                        case OPER:
                                            selector = 0;
                                            tokens = Arrays.stream(line.replace("(", " ( ").replace(")", " ) ").replace(";", " ; ").replace(".", " . ").split(TOKENIZER)).filter(s -> !KEYWORDS.contains(s) && !s.isEmpty()).toArray(i -> new String[i]);
                                            if(debug && !skip) { System.out.println(INDENTION + "tokens: " + Arrays.stream(tokens).collect(Collectors.joining(", ", "[", "]"))); }
                                            if(tokens.length == 2 && tokens[0].equals("Exception") && tokens[1].equals("table")) {
                                                wf[0] = WaitingFor.EXCEPTION_TABLE;
                                                break;
                                            }
                                            if(tokens.length == 1 && tokens[0].equals("}")) {
                                                wf[0] = WaitingFor.DONE;
                                                break;
                                            }
                                            for(int i = 0; i < tokens.length; i++) {
                                                String s = tokens[i];
                                                if(i == 1 && s.equals("tableswitch") && tokens[i + 1].equals("{")) {
                                                    wf[0] = WaitingFor.TABLE_SWITCH;
                                                    break;
                                                }
                                                if(i == 1 && s.equals("lookupswitch") && tokens[i + 1].equals("{")) {
                                                    wf[0] = WaitingFor.LOOKUP_SWITCH;
                                                    break;
                                                }
                                                if(selector == 0) {
                                                    if(s.equals("//")) {
                                                        selector++;
                                                    }
                                                } else if(selector == 1) {
                                                    if(s.equals("class") || s.equals("Field") || s.equals("Method")) {
                                                        selector = 2;
                                                        if(s.equals("Field")) {
                                                            i++; //skip field name
                                                        }
                                                    }
                                                    String last_added_token = null;
                                                    String token_to_add = null;
                                                    int loded_case = 0;
                                                    for(int j = i + 1; j < tokens.length; j++) {
                                                        token_to_add = null;
                                                        s = tokens[j];
                                                        if(selector == 2 && !s.equals(".") && !s.equals("(")) {
                                                            if(s.equals("\"")) {
                                                                s = "";
                                                                for(j++; j < tokens.length; j++) {
                                                                    if(!tokens[j].equals("\"")) {
                                                                        s += tokens[j];
                                                                    } else {
                                                                        break;
                                                                    }
                                                                }
                                                            }
                                                            s = s.replace("/", ".");
                                                            token_to_add = s;
                                                            loded_case = 3;
                                                        } else if(selector == 2 && s.equals(".")) {
                                                            selector = 3;
                                                        } else if(selector == 2 && s.equals("(")) {
                                                            if(last_added_token != null) {
                                                                if(debug && !skip) { System.out.println(INDENTION + "--removed " + last_added_token); }
                                                                foundClasses.remove(last_added_token);
                                                            }
                                                            selector = 4;
                                                        } else if(selector == 3 && s.equals("(")) {
                                                            selector = 4;
                                                        } else if(selector == 4 && !s.equals(")") && !s.equals(";")) {
                                                            s = s.replace("/", ".");
                                                            token_to_add = s;
                                                            loded_case = 4;
                                                        } else if(selector == 4 && s.equals(")")) {
                                                            selector = 5;
                                                        } else if(selector == 5) {
                                                            s = s.replace("/", ".");
                                                            token_to_add = s;
                                                            loded_case = 5;
                                                            selector = 6;
                                                        } else if(selector == 6 && s.equals(";")) {
                                                        } else if(selector == 6) {
                                                            throw new RuntimeException("Inconsistent case: " + wf[0] + " and " + line);
                                                        }
                                                        if(token_to_add != null) {
                                                            matcher = pPrimitiveTypes.matcher(s);
                                                            if (matcher.find() && matcher.start() == 0) {
                                                                token_to_add = token_to_add.substring(matcher.end());
                                                            }
                                                            if (token_to_add.startsWith("L") && (token_to_add.endsWith(";") || tokens[j + 1].equals(";"))) {
                                                                if (!token_to_add.endsWith(";")) {
                                                                    token_to_add = token_to_add.substring(1);
                                                                    j++;
                                                                } else {
                                                                    token_to_add = token_to_add.substring(1, token_to_add.length() - 1);
                                                                }
                                                            }
                                                            if(!skip && !token_to_add.isEmpty()) {
                                                                last_added_token = token_to_add;
                                                                if(!used.test(token_to_add)) {
                                                                    if(debug) { System.out.println(INDENTION + "--found " + loded_case + ": " + token_to_add); }
                                                                    foundClasses.add(token_to_add);
                                                                }
                                                            }
                                                        }
                                                    }
                                                    break;
                                                }
                                                
                                            }
                                            if(wf[0] != WaitingFor.OPER) {
                                                break;
                                            }
                                            if(debug) { System.out.println(INDENTION + wf[0] + " OK" + (skip ? " (skipped)" : "")); }
                                            break;
                                        case DONE:
                                            throw new RuntimeException("Inconsistent case: " + wf[0] + " and " + line);
                                    }
                                }
                            }
                            while ((line = err.readLine()) != null) {
                                line = line.trim();
                                sb_error.append(line).append("\n");
                            }
                            if(debug) { System.out.println(INDENTION + "found: " + foundClasses); }
                            foundClasses.forEach(v -> {
                                try {
                                    Class<?> cl = ClassLoader.getSystemClassLoader().loadClass(v);
                                    if(cl.getProtectionDomain().getCodeSource() != null) {
                                        if(debug) { System.out.println(INDENTION + "User's library: " + cl); }
                                        if(!decompiledClasses.contains(v)) {
                                            queue.add(cl.getName());
                                            decompiledClasses.add(v);
                                        }
                                    } else {
                                        if(debug) { System.out.println(INDENTION + "Runtime library: " + cl); }
                                    }
                                } catch(ClassNotFoundException ccex) {
                                    if(debug) { System.out.println(INDENTION + "Generic parameter: " + v); }
                                }
                            });
                            touched.addAll(foundClasses);
                            input.close();
                            if(debug) { System.out.println("Done: " + params[5] + "\n"); }
                        } catch (IOException ex) {
                            Logger.getLogger(Preprocessor.class.getName()).log(Level.SEVERE, null, ex);
                            failed[0] = true;
                        } catch (RuntimeException rex) {
                            Logger.getLogger(Preprocessor.class.getName()).log(Level.SEVERE, null, rex);
                            failed[0] = true;
                        }

                    }
                };
                thread.start();
                int result = p.waitFor();
                thread.join();
                if (result != 0 || failed[0]) {
                    System.err.println("Process failed with status: " + result);
                    System.err.println(sb_error);
                    break;
                }
                first[0] = false;
            }
            if(debug) { System.out.println("touched: " + touched); }
            if(debug) { System.out.println("decompiled: " + decompiledClasses); }
            if(debug) { System.out.println("sources: " + sources); }
            sources.remove(main_java[0]);
            String java = main_java[0];
            String new_java = "";
            String main_class = "";
            String main_package = null;
            first[0] = true;
            StringBuilder sb = new StringBuilder();
            TreeSet<String> imports = new TreeSet<>();
            StringBuilder sb1 = new StringBuilder();
            String underline = "";
            int[] line_length = new int[]{0};
            while(java != null) {
                probed_paths.clear();
                File fJava = new File(java);
                JavaSource src = getSourceFileEntry.apply(fJava);
                if(src.file != null && src.file.toString().endsWith(".class")) {
                    if(debug) { System.out.println("only class found: " + src.file); }
                    String base = src.file.toString().substring(0, src.file.toString().length() - fJava.toString().replace(".java", ".class").length()).replace("\\", "/");
                    File src1 = null;
                    if(src1 == null && base.endsWith("build/classes/")) { // NetBeans
                        src1 = new File(base.substring(0, base.indexOf("build/classes/")) + "src/" + java);
                        if(debug) { System.out.println("probe NetBeans structure: " + src1); }
                        if(!src1.exists() || src1.isDirectory()) {
                            src1 = null;
                        }
                        //todo other structures
                    }
                    if(src1 != null) {
                        src.file = src1;
                        if(debug) { System.out.println("found: " + src.file); }
                    }
                }
                if(src.file == null && src.jarFile == null) {
                    throw new IOException("source file not found: " + java);
                }
                sb1.delete(0, sb1.length());
//                sb.append("//begin ").append(java.replace("\\", "\\\\")).append("\n");
                try (
                    InputStream is = src.file != null ? new FileInputStream(src.file) : src.jarFile.getInputStream(src.jarEntry);
                    InputStreamReader isr = new InputStreamReader(is);
                ) {
                    String classname = java.substring(0, java.indexOf(".java")).replace("/", ".");
                    List<String> tokens = tokenizer.tokenize(isr);
                    boolean isAbstract = false;
                    boolean class_started = false;
                    boolean doNotCopy = false;
//                    line_length[0] = 0;
//                    System.out.println(tokens);
                    for(int i = 0; i < tokens.size(); i++) {
                        if("?import".equals(tokens.get(i))) {
                            int j = i;
                            for(; j < tokens.size(); j++) {
                                if("!;".equals(tokens.get(j))) {
                                    break;
                                }
                            }
                            String imp = tokens.subList(i, j + 1).stream().map(v -> v.startsWith("?") || v.startsWith("!") ? v.substring(1) : v).collect(Collectors.joining());
                            imports.add(imp);
                            i = j;
                        } else if(("?class".equals(tokens.get(i)) || "?interface".equals(tokens.get(i)) || "?enum".equals(tokens.get(i)))) {
                            if(!sb1.substring(sb1.length() - Math.min(10, sb1.length())).trim().endsWith("static")) {
                                sb1.append("static ");
                                line_length[0] += "static ".length();
                            }
                            sb1.append("private ");
                            line_length[0] += "private ".length();
                            if(isAbstract) {
                                sb1.append("abstract ");
                                line_length[0] += "abstract ".length();
                            }
                            sb1.append(tokens.get(i).substring(1));
                            line_length[0] += tokens.get(i).length() - 1;
                            class_started = true;
                        } else if("?abstract".equals(tokens.get(i))) {
                            if(!class_started) {
                                isAbstract = true;
                            } else if(!doNotCopy) {
                                sb1.append(tokens.get(i).substring(1));
                                line_length[0] += tokens.get(i).length() - 1;
                            }
                        } else  {
                            if(" ".equals(tokens.get(i)) || tokens.get(i).startsWith("!/*") || tokens.get(i).startsWith("!//")) {
                                boolean remove_token = false;
                                if(i > 0 && tokens.get(i - 1).startsWith("!") || i < tokens.size() - 1 && tokens.get(i + 1).startsWith("!")) {
                                    // skip space or comment
                                    remove_token = true;
                                } else if(class_started && !doNotCopy) {
                                    // insert space in place of space or comment
                                    sb1.append(" ");
                                    line_length[0]++;
                                }
                                if("!/*+Preprocess-DONOTCOPY*/".equals(tokens.get(i))) {
                                    doNotCopy = true;
                                } else if("!/*-Preprocess-DONOTCOPY*/".equals(tokens.get(i))) {
                                    doNotCopy = false;
                                }
                                if(remove_token) {
                                    tokens.remove(i);
                                    i--;
                                }
                                if(line_length[0] >= 80) {
                                    sb1.append("\n");
                                    line_length[0] = 0;
                                }
                            } else if(class_started && !doNotCopy) {
                                sb1.append(tokens.get(i).substring(1));
                                line_length[0] += tokens.get(i).length() - 1;
                            }
                        }
//                        if(line_length[0] >= 80) {
//                            sb1.append("\n");
//                            line_length[0] = 0;
//                        }
                    }
                    if (first[0]) {
                        Matcher matcher = pMain.matcher(sb1);
                        if (matcher.find()) {
                            int pos = matcher.start(1);
                            int underlines = 1;
                            matcher = pMain1.matcher(sb1);
                            while (matcher.find()) {
                                if (matcher.end(1) - matcher.start(1) > underlines + "main".length()) {
                                    underlines = matcher.end(1) - matcher.start(1) - "main".length() + 1;
                                }
                            }
                            underline = String.format("%" + underlines + "s", "").replace(" ", "_");
                            sb1.insert(pos, underline);
                        }
                        new_java = src.file.toString().replace("\\", "/");
                        if (new_java.contains("/")) {
                            new_java = new_java.substring(0, new_java.lastIndexOf("/") + 1) + "_" + new_java.substring(new_java.lastIndexOf("/") + 1);
                        } else {
                            new_java = "_" + new_java;
                        }
                        main_class = classname;
                        main_package = main_class.contains(".") ? main_class.substring(0, main_class.lastIndexOf(".")) : null;
                    }
                }
                sb.append(sb1);
//                sb.append("\n").append("//end ").append(java.replace("\\", "\\\\")).append("\n");
                java = sources.pollFirst();
                first[0] = false;
            } 
            sb1.delete(0, sb1.length());
            if(main_package != null) {
                sb1.append("package ").append(main_package).append(";\n");
            }
            line_length[0] = 0;
            imports.stream().filter(v -> {
                String cl = v.substring(v.indexOf("import") + "import".length()).trim();
                cl = cl.substring(0, cl.indexOf(";"));
                return !decompiledClasses.contains(cl) && sb.toString().contains(cl.substring(cl.lastIndexOf(".") + 1));
            }).forEach(v -> {
                v = v.trim();
                sb1.append(v);
                line_length[0] += v.length();
                if (line_length[0] >= 80) {
                    sb1.append("\n");
                    line_length[0] = 0;
                }
            });
            int l = sb1.length();
            sb1.append("public class _").append(main_class).append(" {");
            line_length[0] += sb1.length() - l;
            if (line_length[0] >= 80) {
                sb1.append("\n");
                line_length[0] = 0;
            }
            l = sb1.length();
            sb1.append("static public void main(final String[] args) ");
            line_length[0] += sb1.length() - l;
            if (line_length[0] >= 80) {
                sb1.append("\n");
                line_length[0] = 0;
            }
            if(!main_exceptions.isEmpty()) {
                l = sb1.length();
                sb1.append("throws ").append(main_exceptions.stream().map(v -> v.substring(v.lastIndexOf(".") + 1)).collect(Collectors.joining(",")));
                line_length[0] += sb1.length() - l;
                if (line_length[0] >= 80) {
                    sb1.append("\n");
                    line_length[0] = 0;
                }
            }
            l = sb1.length();
            sb1.append("{").append(main_class).append(".").append(underline).append("main(args);}\n");
            line_length[0] += sb1.length() - l;
            if (line_length[0] >= 80) {
                sb1.append("\n");
                line_length[0] = 0;
            }
            
            sb.insert(0, sb1);
            sb.append("}\n");
            
            try(FileWriter fw = new FileWriter(new_java)) {
                if(debug) {
                    System.out.println("Saving: " + new_java);
                }
                fw.write(sb.toString());
            }
        } catch (IOException e) {
            System.err.println("Failed: " + e);
        } catch (InterruptedException ex) {
            Logger.getLogger(Preprocessor.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Failed: " + ex);
        }
    }

}
