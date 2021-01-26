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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author alexei
 */
public class Wizard {
    
    static public void main(final String[] args) throws IOException {
        new Wizard().run(args);
//        new Wizard().run(new String[]{"-stdout", "A", "{+;ss/}"});
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

    private String show_pos(String script, String[][] msgs) {
        Arrays.sort(msgs, (x, y) -> Integer.parseInt(x[0]) - Integer.parseInt(y[0]));
        StringBuilder sb = new StringBuilder(script).append("\n");
        int pos = 0;
        int indent = 0;
        for(int i = 0; i < msgs.length; i++) {
            indent = Integer.parseInt(msgs[i][0]) - pos;
            sb.append(String.format("%" + (indent == 0 ? "" : Integer.toString(indent)) + "s%s", "", i < msgs.length - 1 ? "|" : "+"));
            pos += indent + 1;
        }
        indent = script.length() - pos + 1;
        sb.append(String.format("%" + (indent == 0 ? "" : Integer.toString(indent)) + "s", "").replace(" ", "-")).append(" ").append(msgs[msgs.length - 1][1]);
        sb.append("\n");
        for(int i = msgs.length - 2; i >= 0; i--) {
            pos = 0;
            for (int j = 0; j <= i; j++) {
                indent = Integer.parseInt(msgs[j][0]) - pos;
                sb.append(String.format("%" + (indent == 0 ? "" : Integer.toString(indent)) + "s%s", "", j < i - 1 ? "|" : "+"));
                pos += indent + 1;
            }
            indent = script.length() - pos + 1;
            sb.append(String.format("%" + (indent == 0 ? "" : Integer.toString(indent)) + "s", "").replace(" ", "-")).append(" ").append(msgs[i][1]);
            sb.append("\n");
        }
        return sb.toString();
    }
    
    static class Variable {
        String type = null;
        String name = null;
        Stack<String[]> lengths = new Stack<>();
        Stack<Variable> variables = new Stack<>();
        int pos = 0;
        int variable = -1;
        boolean is_action = false;
        
        @Override
        public String toString() {
            if("/".equals(name)) {
                return "/";
            }
            return String.format("{%s:%s;%s;%s;[%s]}", 
                is_action ? "A" : "D",
                type == null ? "-" : type, 
                name == null ? "-" : name, 
                lengths.stream().map(v -> "(" + v[0] + ", " + v[1] + ")").collect(Collectors.joining(", ", "[", "]")),
                IntStream.range(0, variables.size()).mapToObj(i -> (i == variable ? "*" : "") + variables.get(i).toString()).collect(Collectors.joining(","))
            );
        }
        String render_class(final int indention) {
            if(is_action || TYPES.contains(type)) {
                return null;
            }
            if(variable > 0) {
                return variables.get(variable).render_class(indention);
            }
            StringBuilder sb = new StringBuilder();
            
            return sb.toString();
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
    static final TreeSet<Integer> NONAME = new TreeSet<Integer>() {
        {
            addAll(BRAKETS);
            addAll(",;/".chars().mapToObj(Integer::valueOf).collect(Collectors.toList()));
        }
    };
    

    Stack<Variable> tree = new Stack<>();
    TreeSet<Integer> wf = new TreeSet<>();
    Stack<Integer> brackets = new Stack<>();
    String script = null;
    
    private void set_wait_for(Object ...symbols) {
        wf.clear();
        for(Object sym: symbols) {
            if(sym instanceof String) {
                wf.addAll(((String)sym).chars().mapToObj(Integer::valueOf).collect(Collectors.toList()));
            } else if(sym instanceof Collection) {
                @SuppressWarnings("unchecked")
                final Collection<Integer> sym1 = (Collection<Integer>)sym;
                wf.addAll(sym1);
            } else {
                wf.add((int)((Character)sym).charValue());
            }
        }
        if(wf.first() == 0) {
            wf.pollFirst();
        }
    }
    
    private boolean check_brackets(int c, int pos) {
        if(BRAKETS.contains(c)) {
//            System.out.println("brakets: " + brackets.stream().map(v -> "'" + script.charAt(v) + "'").collect(Collectors.joining(", ", "[", "]")));
            if(c == '(' || c == '{' || c == '[') {
                brackets.push(pos);
            } else {
                if (brackets.isEmpty()) {
                    throw new RuntimeException(String.format("%nUnexpected char '%c', no open bracket found:%n", (char)c)
                            + show_pos(script, new String[][]{new String[]{Integer.toString(pos), "close"}}));
                } else {
                    int eb = expected_braket(script);
//                    System.out.println("c: " + (char)c + ", eb: " + (char)eb);
                    int start = brackets.pop();
                    if (c != eb) {
                        throw new RuntimeException(String.format("%nUnexpected char '%c', the brackets are unbalanced:%n", (char)c)
                                + show_pos(script, new String[][]{new String[]{Integer.toString(start), "open"}, new String[]{Integer.toString(pos), "close"}}));
                    }
                }
            }
            return true;
        }
        return false;
    }
    
    private int expected_braket(String script) {
        if(!brackets.isEmpty()) {
            switch(script.charAt(brackets.peek())) {
                case '(':
                    return ')';
                case '{':
                    return '}';
                case '[':
                    return ']';
            }
        }
        return 0;
    } 
    
    private void parse_script_to_tree(String script, int from_pos) {

        set_wait_for(TYPES, "/({");

        boolean[] wait_for_name = new boolean[1];
        boolean[] wait_for_wf = new boolean[1];
        int[] wait_for_wf_brakets_count = new int[1];
        StringBuilder sb = new StringBuilder();
        int name_gen = 0;
        
        tree.clear();
        
        tree.push(new Variable());
        tree.peek().is_action = true;
        
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
//            System.out.println(i + "> " + (char)c + " wf: " + 
//                    wf.stream().map(v -> "'" + String.valueOf((char)v.intValue()) + "'").collect(Collectors.joining(", ", "[", "]")) + 
//                    ", wait_for_name: " + Boolean.toString(wait_for_name[0]) + ", wait_for_wf: " + Boolean.toString(wait_for_wf[0]));
            if (!Character.isSpaceChar(c)) {
                if(wait_for_name[0]) {
                    if(NONAME.contains(c)) {
                        if (sb.length() == 0) {
                            throw new RuntimeException(String.format("%nUnexpected char '%c':%n", (char) c)
                                    + show_pos(script, new String[][]{new String[]{Integer.toString(i),
                                "expecting: <name>"}}));
                        }
                        ((Variable) tree.peek().variables.peek()).name = sb.toString();
                        wait_for_name[0] = false;
                        i--;
                        continue;
                    } else {
                        sb.append((char) c);
                    }
                } else if(wait_for_wf[0]) {
                    check_brackets(c, i);
                    if(brackets.size() == wait_for_wf_brakets_count[0] && wf.contains(c)) {
                        if(c == ']') {
                            tree.peek().variables.peek().lengths.push(new String[]{sb.length() == 0 ? "+" : sb.toString(), "$i" + Integer.toString(name_gen++)});
                            set_wait_for("[,;/(){}");
                        } else if(c == ';') {
                            tree.peek().lengths.add(new String[]{sb.toString(), "$i" + Integer.toString(name_gen++)});
                            set_wait_for(TYPES, "/({");
                        }
                        wait_for_wf[0] = false;
                    } else {
                        sb.append((char)c);
                    }
                } else {
                    if(!wf.contains(c)) {
                        throw new RuntimeException(String.format("%nUnexpected char '%c':%n", (char)c)
                                + show_pos(script, new String[][]{new String[]{Integer.toString(i), 
                                    "expecting: " + wf.stream().map(v -> "'" + String.valueOf((char)v.intValue()) + "'").
                                            collect(Collectors.joining(" | "))}}));
                    }
                    if(c == '(' || c == '{') {
                        Variable var = new Variable();
                        var.pos = i;
                        tree.peek().variables.add(var);
                        var.is_action = c == '{';
                        if(var.is_action) {
                            for(int j = tree.size() - 1; j >= 0; j--) {
                                if(!tree.get(j).is_action) {
                                    throw new RuntimeException(String.
                                            format("%nUnexpected char '%c', a \"loop-cycle\" is not allowed inside \"data-cycle\":%n", (char)c) + 
                                            show_pos(script, new String[][]{new String[]{
                                                Integer.toString(tree.get(j).pos), "\"data-cycle\" start"}, 
                                                new String[]{Integer.toString(i), "\"loop-cycle\" start"}}));
                                }
                            }
                        }
                        tree.push(var);
                        check_brackets(c, i);
                        set_wait_for(';');
                        begin_wait_for_wf.get();
                    } else if(c == ',') {
                        Variable var = new Variable();
                        var.type = ((Variable)tree.peek().variables.peek()).type;
                        tree.peek().variables.push(var);
                        begin_wait_for_name.get();
                        set_wait_for("[,;/(){}");
                    } else if(c == ';') {
                        Variable var = new Variable();
                        tree.peek().variables.push(var);
                        set_wait_for(TYPES, "/(){}");
                    } else if(c == '/') {
                        Variable var = new Variable();
                        var.name = "/";
                        tree.peek().variables.push(var);
                        set_wait_for(TYPES, "/(){}");
                    } else if(c == '[') {
                        set_wait_for(']');
                        begin_wait_for_wf.get();
                        check_brackets(c, i);
                    } else if(c == '}' || c == ')') {
                        check_brackets(c, i);
                        if(!tree.peek().is_action) {
                            int num_vars = IntStream.range(0, tree.peek().variables.size()).map(v -> {
                                if(!"/".equals(tree.peek().variables.get(v).name)) {
                                    tree.peek().variable = v;
                                    return 1;
                                }
                                return 0;
                            }).sum();
                            if(num_vars != 1) {
                                tree.peek().variable = -1;
                            }
                            if(tree.peek().variable < 0) {
                                tree.peek().type = "$T" + Integer.toBinaryString(name_gen++);
                            }
                        }
                        tree.pop();
                        set_wait_for(TYPES, "/(){}");
                    } else {
                        assert TYPES.contains(c);
                        Variable var = new Variable();
                        var.type = String.valueOf((char)c);
                        tree.peek().variables.push(var);
                        begin_wait_for_name.get();
                        set_wait_for("[,;/(){}");
                    }
                }
            } else {
                if(wait_for_name[0] && sb.length() > 0) {
                    ((Variable)tree.peek().variables.peek()).name = sb.toString();
                    wait_for_name[0] = false;
                }
            }
        }
        if (wait_for_name[0]) {
            ((Variable) tree.peek().variables.peek()).name = sb.toString();
        }
        if(wait_for_wf[0]) {
            throw new RuntimeException(String.format("%nUnexpected end of script:%n")
                    + show_pos(script, new String[][]{new String[]{Integer.toString(script.length()),
                "expecting: " + (sb.length() == 0 ? "<expr> | " : "") + wf.stream().map(v -> "'" + String.valueOf((char)v.intValue()) + "'").
                collect(Collectors.joining(" | "))}}));
        }
        if(!brackets.isEmpty()) {
            throw new RuntimeException(String.format("%nend of script, no close bracket found:%n")
                    + show_pos(script, new String[][]{new String[]{Integer.toString(brackets.peek()), "close"}}));
        }
    }
    
    private void run(String[] args) throws IOException {
        String pkg = null;
        String src = null;
        String in_dir = null;
        String in_name = null;
        String outfile = null;
        String class_name = null;
        script = null;
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
        
        parse_script_to_tree(script, i);
        
        System.out.println(tree.peek());
        
        sb_solve.delete(0, sb_solve.length());
        sb_classes.delete(0, sb_classes.length());
        index_gen = 0;
        process_tree(0, tree.peek());
        
        
    }
    
    StringBuilder sb_solve = new StringBuilder();
    int index_gen = 0;
    StringBuilder sb_classes = new StringBuilder();
    
    private void process_tree(final int deep, final Variable var) {
//        ArrayList<String> fields = new ArrayList<>();
//        for(IVariable var: cy.variables) {
//            if(var instanceof Cycle) {
//                if(!((Cycle)var).is_action && ((Cycle)var).variable < 0) {
//                    ((Cycle)var).class_name = "Cy" + Integer.toString(index_gen++);
//                    fields.add(((Cycle)var).class_name + " " + );
//                }
//            }
//        }
    }

}
