/*
 * MIT License
 * 
 * Copyright (c) 2020 Alexey Zakharov <leksi@leksi.net>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.leksi.contest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Stack;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

/**
 *
 * @author alexei
 */
public class Wizard {
    
    static public void main(final String[] args) throws IOException {
//        new Wizard().run(args);
//        new Wizard().run(new String[]{"A", "*in,m/ia[n]/ss/(m;lb[]/)ic[m]"});
        new Wizard().run(new String[]{"-stdout", "A", "?in,h,m/{m;il,r,x/)"});
//        new Wizard().run(new String[]{"-stdout", "A", "?{2;ss}"});
        new Wizard().run(new String[]{"-stdout", "A", "?in/ia[n]/ib[a[3]]/ic[]"});
    }

    private static void usage() {
        System.out.println("Usage: java java_options -jar net.leksi.contest.compiler.jar wizard_options class-name script");
        System.out.println("    java_options:           java options like -classpath;");
        System.out.println("    wizard_options:");
        System.out.println("        -stdout                 - write to stdout (default creates file <class-name>.java);");
        System.out.println("        -src <directory>        - the directory to generate source into (default .);");
        System.out.println("        -in <directory>         - the directory to generate input file into (default .);");
        System.out.println("        -infile <name>          - generate input file <name> (default <class-name>.in);");
        System.out.println("        -package package        - the package of class to generate (default empty);");
        System.out.println("        -force                  - overwrite existing files (default throws exception for source file) and leaves input file;");
        System.out.println("        -version                - shows current version and checks if it is latest, then returns;");
        System.out.println("        -usage, -help, ?        - shows this info, then returns;");
        System.out.println("    class-name:             name of class to generate;");
        System.out.println("    script:                 input script;");
    }

    private void version() {
        try {
            String[] version = new String[]{null, null};
            Enumeration<URL> resources = getClass().getClassLoader()
                    .getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {
                    Manifest manifest = new Manifest(resources.nextElement().openStream());
                    Map<String, Attributes> entries = manifest.getEntries();
                    entries.keySet().stream().allMatch(k -> {
                        return entries.get(k).entrySet().stream().allMatch(e -> {
//                            System.out.println(">" + e.getKey() + "<");
                            if("Implementation-Version".equals(e.getKey().toString())) {
                                version[0] = e.getValue().toString();
                                return false;
                            }
                            return true;
                        });
                    });
            }
            System.out.println("Version");
            System.out.println("current: " + version[0]);
            final String path = "/leksiq/java-contest-assistant/releases/tag/";
            try (
                Reader r = new InputStreamReader((InputStream)new URL("https://github.com/leksiq/java-contest-assistant").getContent());
                BufferedReader br = new BufferedReader(r);
            ) {
                String line;
                while((line = br.readLine()) != null) {
                    if(line.contains("href=\"" + path)) {
                        version[1] = line.substring(line.indexOf(path) + path.length(), line.indexOf("\">"));
                        break;
                    }
                }
            }
            System.out.println("latest release: " + version[1]);
            int res = version[0].compareTo(version[1]);
            if(res < 0) {
                System.out.println("A newer version is released. To download visit:");
                System.out.println("https://github.com/leksiq/java-contest-assistant/releases/download/" + version[1] + "/net.leksi.contest.assistant.jar");
            } else if(res == 0) {
                System.out.println("The latest release version is installed.");
            } else {
                System.out.println("Your version is not a release yet.");
            }
        } catch (IOException E) {
            System.out.println("error occured: " + E.toString());
        }
    }

    private String show_pos(String[][] string) {
        Arrays.sort(string, (x, y) -> Integer.parseInt(y[0]) - Integer.parseInt(y[0]));
        StringBuilder sb = new StringBuilder();
        int pos = 0;
        for(String[] s: string) {
            int indent = Integer.parseInt(s[0]) - pos;
            sb.append(String.format("%" + (indent == 0 ? "" : Integer.toString(indent)) + "s^", ""));
            pos += indent;
        }
        return sb.toString();
    }

    interface IVariable {}
    
    static class Variable implements IVariable {
        String type;
        String name;
        Stack<String> lengths = new Stack<>();
        @Override
        public String toString() {
            return String.format("{V:%s;%s;%s}", type == null ? "-" : type, 
                name == null ? "-" : name, lengths.toString()
            );
        }
    }

    static class Cycle implements IVariable {
        String count;
        Stack<IVariable> variables = new Stack<>();
        Cycle parent = null;
        String class_name;
        int field_gen = 0;
        boolean is_action = false;
        int pos = 0;
        @Override
        public String toString() {
            return String.format("{%s:%s;[%s]}", 
                is_action ? "A" : "D",
                count == null ? "-" : count, 
                variables.stream().map(Object::toString).collect(Collectors.joining(","))
            );
        }
    }

    static final TreeSet<Integer> BRAKETS = new TreeSet<Integer>() {
        {
            addAll("(){}[]".chars().mapToObj(Integer::valueOf).collect(Collectors.toList()));
        }
    };
    static final TreeSet<Integer> TYPES = new TreeSet<Integer>() {
        {
            addAll("ildstc".chars().mapToObj(Integer::valueOf).collect(Collectors.toList()));
        }
    };

    Stack<Cycle> cycles = new Stack<>();
    TreeSet<Integer> wf = new TreeSet<>();
    Stack<Integer> brackets = new Stack<>();

    private void set_wait_for(Object ...symbols) {
        wf.clear();
        for(Object sym: symbols) {
            if(sym instanceof String) {
                wf.addAll(((String)sym).chars().mapToObj(Integer::valueOf).collect(Collectors.toList()));
            } else if(sym instanceof Collection) {
                wf.addAll((Collection)sym);
            } else {
                wf.add((int)((Character)sym).charValue());
            }
        }
    }
    
    private boolean check_brackets(int c, int pos, String script) {
        if(BRAKETS.contains(c)) {
            if(c == '(' || c == '{' || c == '[') {
                brackets.push(pos);
            } else {
                if (brackets.isEmpty()) {
                    throw new RuntimeException(String.format("Unexpected char '%c', no open bracket found:%n%s%n", c, script)
                            + show_pos(new String[][]{new String[]{Integer.toString(pos), "close"}}));
                } else {
                    int start = brackets.pop();
                    if (c == ')' && script.charAt(start) != '('
                            || c == '}' && script.charAt(start) != '{'
                            || c == ']' && script.charAt(start) != '[') {
                        throw new RuntimeException(String.format("Unexpected char '%c', the brackets are unbalanced:%n%s%n", c, script)
                                + show_pos(new String[][]{new String[]{Integer.toString(start), "open"}, new String[]{Integer.toString(pos), "close"}}));
                    }
                }
            }
            return true;
        }
        return false;
    }
    
    private void parse_script(String script, int from_pos) {

        set_wait_for(TYPES, '/', '(', '{');

        boolean[] wait_for_name = new boolean[1];
        boolean[] wait_for_wf = new boolean[1];
        int[] wait_for_wf_brakets_count = new int[1];
        StringBuilder sb = new StringBuilder();
        
        cycles.clear();
        
        cycles.push(new Cycle());
        cycles.peek().is_action = true;
        
        Supplier<Integer> begin_wait_for_name = () -> {
            wait_for_name[0] = true;
            sb.delete(0, sb.length());
            return 0;
        };
        
        Supplier<Integer> begin_wait_for_wf = () -> {
            wait_for_wf[0] = true;
            wait_for_wf_brakets_count[0] = brackets.size();
            sb.delete(0, sb.length());
            return 0;
        };
        
        for (int i = from_pos; i < script.length(); i++) {
            int c = script.charAt(i);
            System.out.println(i + "> " + (char)c + " wf: " + 
                    wf.stream().map(v -> "'" + String.valueOf((char)v.intValue()) + "'").collect(Collectors.joining(", ", "[", "]")) + 
                    ", wait_for_name: " + Boolean.toString(wait_for_name[0]) + ", wait_for_wf: " + Boolean.toString(wait_for_wf[0]));
            if (!Character.isSpaceChar(c)) {
                if(wait_for_name[0]) {
                    if(wf.contains((int)c)) {
                        ((Variable)cycles.peek().variables.peek()).name = sb.toString();
                        wait_for_name[0] = false;
                        i--;
                    } else {
                        sb.append((char) c);
                    }
                } else if(wait_for_wf[0]) {
                    check_brackets(c, i, script);
                    if(brackets.size() == wait_for_wf_brakets_count[0] && wf.contains((int)c)) {
                        if(c == ']') {
                            ((Variable)cycles.peek().variables.peek()).lengths.push(sb.toString());
                            set_wait_for('[', ',', ';', '/', '(', '{', ')', '}');
                        } else if(c == ';') {
                            cycles.peek().count = sb.toString();
                            set_wait_for(TYPES, '/', '(', '{');
                        } else {
                            throw new RuntimeException(String.format("Unexpected char '%c', not supported yet:%n%s%n", c, script)
                                    + show_pos(new String[][]{new String[]{Integer.toString(i)}}));
                        }
                        wait_for_wf[0] = false;
                    } else {
                        sb.append((char)c);
                    }
                } else {
                    if(!wf.contains((int)c)) {
                        throw new RuntimeException(String.format("Unexpected char '%c':%n%s%n", c, script)
                                + show_pos(new String[][]{new String[]{Integer.toString(i), 
                                    "expecting: " + wf.stream().map(v -> "'" + String.valueOf((int)v) + "'").
                                            collect(Collectors.joining(", "))}}));
                    }
                    if(c == '(' || c == '{') {
                        Cycle cy = new Cycle();
                        cy.pos = i;
                        cy.parent = cycles.peek();
                        cy.parent.variables.add(cy);
                        cycles.push(cy);
                        cycles.peek().is_action = c == '{';
                        for(Cycle p = cy.parent; p != null; p = p.parent) {
                            if(!p.is_action) {
                                throw new RuntimeException(String.
                                        format("Unexpected char '%c', a \"loop-cycle\" is not allowed inside \"data-cycle\":%n%s%n", c, script) + 
                                        show_pos(new String[][]{new String[]{
                                            Integer.toString(p.pos), "\"data-cycle\" start"}, 
                                            new String[]{Integer.toString(i), "\"loop-cycle\" start"}}));
                            }
                        }
                        set_wait_for(';');
                        begin_wait_for_wf.get();
                    } else if(c == ',') {
                        Variable var = new Variable();
                        var.type = ((Variable)cycles.peek().variables.peek()).type;
                        cycles.peek().variables.push(var);
                        begin_wait_for_name.get();
                        set_wait_for('[', ',', ';', '/', '(', '{', ')', '}');
                    } else if(c == ';') {
                        Variable var = new Variable();
                        cycles.peek().variables.push(var);
                        set_wait_for(TYPES, '/', '(', '{', ')', '}');
                    } else if(c == '/') {
                        Variable var = new Variable();
                        var.name = "/";
                        cycles.peek().variables.push(var);
                        set_wait_for(TYPES, '/', '(', '{', ')', '}');
                    } else if(c == '[') {
                        set_wait_for(']');
                        begin_wait_for_wf.get();
                        check_brackets(c, i, script);
                    } else {
                        if(!TYPES.contains(c)) {
                            throw new RuntimeException(String.format("Unexpected char '%c':%n%s%n", c, script)
                                    + show_pos(new String[][]{new String[]{Integer.toString(i),
                                "expecting: " + wf.stream().map(v -> "'" + String.valueOf((int) v) + "'").
                                collect(Collectors.joining(", "))}}));
                        }
                        Variable var = new Variable();
                        var.type = String.valueOf((char)c);
                        cycles.peek().variables.push(var);
                        begin_wait_for_name.get();
                        set_wait_for('[', ',', ';', '/', '(', '{', ')', '}');
                    }
                }
            } else {
                if(wait_for_name[0]) {
                    ((Variable)cycles.peek().variables.peek()).name = sb.toString();
                    wait_for_name[0] = false;
                }
            }
        }
    }
    
    private void run(String[] args) throws IOException {
        String pkg = null;
        String src = null;
        String in_dir = null;
        String in_name = null;
        String outfile = null;
        String class_name = null;
        String script = null;
        boolean stdout = false;
        boolean force = false;
        
        for(int i = 0 ; i < args.length; i++) {
            if(args[i].startsWith("-")) {
                if("-src".equals(args[i])) {
                    i++;
                    src = args[i];
                } else if("-package".equals(args[i])) {
                    i++;
                    pkg = args[i];
                } else if("-in".equals(args[i])) {
                    i++;
                    in_dir = args[i];
                } else if("-infile".equals(args[i])) {
                    i++;
                    in_name = args[i];
                } else if("-outfile".equals(args[i])) {
                    i++;
                    outfile = args[i];
                } else if("-stdout".equals(args[i])) {
                    stdout = true;
                } else if("-force".equals(args[i])) {
                    force = true;
                } else if("-version".equals(args[i])) {
                    version();
                    return;
                } else if("-usage".equals(args[i]) || "-help".equals(args[i]) || "-?".equals(args[i])) {
                    usage();
                    return;
                }
            } else if(class_name == null) {
                class_name = args[i];
            } else if(script == null) {
                script = args[i];
            } else {
                usage();
                return;
            }
        }
        if(class_name == null || script == null) {
            usage();
            return;
        }
        
        if(stdout) {
            if(force) {
                System.out.println("Warning: -stdout applied, -force ignored.");
            }
            if(in_dir != null) {
                in_dir = null;
                System.out.println("Warning: -stdout applied, -in ignored.");
            }
        } else {
            if(src == null) {
                src = ".";
            }
            if(in_dir == null) {
                in_dir = ".";
            }
            if(in_name == null) {
                in_name = class_name + ".in";
            }
        }
        
        boolean[] singleTest = new boolean[]{true};
        boolean[] localMultiTest = new boolean[]{false};
        int i = 0;
        if(script.charAt(0) == '+') {
            singleTest[0] = false;
            i++;
        } else if(script.charAt(0) == '?') {
            singleTest[0] = true;
            localMultiTest[0] = true;
            i++;
        }
        
        parse_script(script, i);
        
        System.out.println(cycles.peek());
    }
}
