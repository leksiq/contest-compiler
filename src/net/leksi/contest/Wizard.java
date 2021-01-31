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
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
//        new Wizard().run(new String[]{"-stdout", "A", "?in/(n;(m;ia[k][]/)/)"});
//        new Wizard().run(new String[]{"-stdout", "A", "?in/(n;ia[m])"});
//        new Wizard().run(new String[]{"-stdout", "A", "?in/ia[n][m]"});
//        new Wizard().run(new String[]{"-stdout", "A", "?in/ia[]"});
//        new Wizard().run(new String[]{"-stdout", "A", "?in/ia[n][]"});
//        new Wizard().run(new String[]{"-stdout", "A", "?in/ia[n][m][k][]"});
//        new Wizard().run(new String[]{"-stdout", "A", "?in,m/(n;ia,b/(m;ic,d))"});
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
    
    static final int TAB_LEN = 4;
    static final String TAB_SPACE = String.format("%" + TAB_LEN + "s", "");

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
    
    static void write_code_banner(String space, StringBuilder sb) {
        sb.append(space).append("/**************************/\n");
        sb.append(space).append("/* Write your code below. */\n");
        sb.append(space).append("/*vvvvvvvvvvvvvvvvvvvvvvvv*/\n");
        sb.append(space).append("\n");
        sb.append(space).append("/*^^^^^^^^^^^^^^^^^^^^^^^^*/\n");
    }
    
    static class Variable {
        static boolean line_read = false;
        String type = null;
        String name = null;
        Stack<String[]> lengths = new Stack<>();
        Stack<Variable> variables = new Stack<>();
        int pos = 0;
        boolean is_action = false;
        boolean is_field = false;
        Variable parent = null;
        
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
                IntStream.range(0, variables.size()).mapToObj(i -> variables.get(i).toString()).collect(Collectors.joining(","))
            );
        }
        
        String get_path() {
            StringBuilder sb = null;
            for(Variable p = parent; p != null && !p.is_action; p = p.parent) {
                if(sb == null) {
                    sb = new StringBuilder();
                }
                if(p != parent && sb.charAt(0) != '[') {
                    sb.insert(0, ".");
                }
                sb.insert(0, (!"0".equals(p.name) && !"&".equals(p.name) ? p.name : "") + p.lengths.stream().map(v -> ("+".equals(v[0]) ? "" : "[" + v[1] + "]")).collect(Collectors.joining()));
            }
            return sb == null ? "" : sb.toString();
        }
        
        String render_class(String space, StringBuilder sb) {
            if(is_action || type.length() == 1 && TYPES.contains((int)type.charAt(0))) {
                return null;
            }
            sb.append(space).append("static class ").append(type).append(" {\n");
            variables.forEach(v -> {
                if(v.type != null) {
                    sb.append(space).append(TAB_SPACE).append(v.get_render_type()).
                            append(" ").append(v.name).append(";\n");
                }
            });
            sb.append(space).append("}\n");
            return sb.toString();
        }
        
        void render_process(String space, StringBuilder sb) {
            if(!is_action) {
                if("/".equals(name)) {
                    if(!line_read) {
                        sb.append(space).append("sc.nextLine();\n");
                        line_read = true;
                    }
                } else {
                    sb.append(space);
                    if(!is_field) {
                        sb.append(get_render_type()).append(" ");
                    } else {
                        String path = get_path();
                        if(path != null) {
                            sb.append(path);
                            if(!"&".equals(name) && !"0".equals(name)) {
                                sb.append(".");
                            }
                        }
                    }
                    if(!"&".equals(name) && !"0".equals(name)) {
                        sb.append(name);
                    }
                    sb.append(" = ").append(get_render_init()).append(";\n");
                }
            }
            int len_pos = -1;
            for(String[] v: lengths) {
                if(!"+".equals(v[0])) {
                    len_pos++;
                    if(is_action) {
                        write_code_banner(space, sb);
                    }
                    sb.append(space).append("for(int ").append(v[1]).
                            append(" = 0; ").append(get_render_condition(v)).
                            append("; ").append(v[1]).append("++) {\n");
                    space += TAB_SPACE;
                    if(!lengths.isEmpty() && type.startsWith("$T")) {
                        Variable var = new Variable();
                        var.type = type;
                        var.parent = parent;
                        var.name = name;
                        var.is_field = is_field;
                        sb.append(space);
                        if (is_field) {
                            String path = var.get_path();
                            if (path != null) {
                                sb.append(path);
                                if (!"&".equals(var.name) && !"0".equals(var.name)) {
                                    sb.append(".");
                                }
                            }
                        }
                        if (!"&".equals(var.name) && !"0".equals(var.name)) {
                            sb.append(var.name);
                        }
                        sb.append("[").append(v[1]).append("] = ").append(var.get_render_init()).append(";\n");
                    }
                } else {
                    break;
                }
            }
            if(!variables.isEmpty()) {
                for(Variable v: variables) {
                    v.render_process(space, sb);
                }
            } else {
                if(len_pos >= 0) {
                    Variable var = new Variable();
                    var.type = type;
                    var.parent = this;
                    var.name = "&";
                    var.is_field = true;
                    var.lengths.addAll(lengths.subList(len_pos + 1, lengths.size()));
                    var.render_process(space, sb);
                }
            }
            for(String[] v: lengths) {
                if(!"+".equals(v[0])) {
                    if (is_action) {
                        write_code_banner(space, sb);
                    }
                    space = space.substring(0, space.length() - TAB_LEN);
                    sb.append(space).append("}\n");
                } else {
                    break;
                }
            }
        }

        String get_render_condition(String[] v) {
            return v[1] + " < " + v[0];
        }
        
        String get_render_init() {
            if(lengths.isEmpty()) {
                switch (type) {
                    case "c":
                        line_read = false;
                        return "sc.nextChar()";
                    case "i":
                        line_read = false;
                        return "sc.nextInt()";
                    case "l":
                        line_read = false;
                        return "sc.nextLong()";
                    case "d":
                        line_read = false;
                        return "sc.nextDouble()";
                    case "s":
                        line_read = true;
                        return "sc.nextLine()";
                    case "t":
                        line_read = false;
                        return "sc.next()";
                    default:
                        return "new " + type + "()";
                }
            }
            if("+".equals(lengths.get(0)[0])) {
                switch (type) {
                    case "c":
                        line_read = true;
                        return "lineToCharArray()";
                    case "i":
                        line_read = true;
                        return "lineToIntArray()";
                    case "l":
                        line_read = true;
                        return "lineToLongArray()";
                    case "d":
                        line_read = true;
                        return "lineToDoubleArray()";
                    case "t":
                        line_read = true;
                        return "lineToArray()";
                }
            }
            int rest_dimentions = get_dimensions() - lengths.size();
            return "new " + get_render_scalar_type() + 
                    lengths.stream().map(v -> "[" + (!"+".equals(v[0]) ? v[0] : "") + "]").collect(Collectors.joining()) +
                    (rest_dimentions == 0 ? "" : String.format("%" + rest_dimentions + "s", "").replace(" ", "[]"));
        }
        
        int get_dimensions() {
            int len = lengths.size();
            if(type.length() == 1 && TYPES.contains((int)type.charAt(0))) {
                Variable p = this;
                while(p != null) {
                    boolean found = false;
                    for(Variable v: p.variables) {
                        if("0".equals(v.name) || "&".equals(v.name)) {
                            len += v.lengths.size();
                            p = v;
                            found = true;
                            break;
                        }
                    }
                    if(!found) {
                        break;
                    }
                }
            }
            return len;
        }

        String get_render_type() {
            int len = get_dimensions();
            return String.format("%s%" + (len == 0 ? "" : len) + "s", get_render_scalar_type(), "").replace(" ", "[]");
        }

        String get_render_scalar_type() {
            switch(type) {
                case "c":
                case "i":
                    return "int";
                case "l":
                    return "long";
                case "d":
                    return "double";
                case "s":
                case "t":
                    return "String";
                default:
                    return type;
            }
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
            if(c == '(' || c == '{' || c == '[') {
                brackets.push(pos);
            } else {
                if (brackets.isEmpty()) {
                    throw new RuntimeException(String.format("%nUnexpected char '%c', no open bracket found:%n", (char)c)
                            + show_pos(script, new String[][]{new String[]{Integer.toString(pos), "close"}}));
                } else {
                    int eb = expected_braket(script);
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
        classes.clear();
        
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
                        var.is_field = !tree.peek().is_action;
                        var.parent = tree.peek();
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
                        var.is_field = !tree.peek().is_action;
                        var.parent = tree.peek();
                        tree.peek().variables.push(var);
                        begin_wait_for_name.get();
                        set_wait_for("[,;/(){}");
                    } else if(c == ';') {
                        Variable var = new Variable();
                        var.type = ";";
                        var.is_field = !tree.peek().is_action;
                        var.parent = tree.peek();
                        tree.peek().variables.push(var);
                        set_wait_for(TYPES, "/(){}");
                    } else if(c == '/') {
                        Variable var = new Variable();
                        var.name = "/";
                        tree.peek().variables.push(var);
                        set_wait_for(TYPES, "/(){}");
                    } else if(c == '[') {
                        if("s".equals(tree.peek().variables.peek().type)) {
                            throw new RuntimeException(String.format("%nUnexpected char '%c':%n", (char)c)
                                    + show_pos(script, new String[][]{new String[]{Integer.toString(i), 
                                        "Array of type 's' is ambiguous and unsupported."}}) + 
                                    "\nTo get an array of tokens till the end of line use type 't' instead" +
                                    "\nTo get an array of Strings till the end of input use type {+;ss/} instead and fill a list by your code\n" 
                                    );
                        }
                        if(!tree.peek().variables.peek().lengths.isEmpty() && "+".equals(tree.peek().variables.peek().lengths.peek()[0])) {
                            throw new RuntimeException(String.format("%nUnexpected char '%c':%n", (char) c)
                                    + show_pos(script, new String[][]{new String[]{Integer.toString(i),
                                "Array can have only last undefined dimension."}}));
                        }
                        set_wait_for(']');
                        begin_wait_for_wf.get();
                        check_brackets(c, i);
                    } else if(c == '}' || c == ')') {
                        check_brackets(c, i);
                        if(!tree.peek().is_action) {
                            int[] variable = new int[]{-1};
                            int num_vars = IntStream.range(0, tree.peek().variables.size()).map(v -> {
                                if(!"/".equals(tree.peek().variables.get(v).name)) {
                                    variable[0] = v;
                                    return 1;
                                }
                                return 0;
                            }).sum();
                            if(num_vars != 1) {
                                variable[0] = -1;
                            }
                            if(variable[0] < 0) {
                                tree.peek().type = "$T" + Integer.toString(name_gen++);
                                tree.peek().name = "$f" + Integer.toString(name_gen++);
                                classes.add(tree.peek());
                            } else {
                                tree.peek().type = tree.peek().variables.get(variable[0]).type;
                                tree.peek().name = tree.peek().variables.get(variable[0]).name;
                                if(tree.peek().variables.get(variable[0]).variables.isEmpty()) {
                                    tree.peek().variables.get(variable[0]).name = "&";
                                } else {
                                    tree.peek().variables.get(variable[0]).name = "0";
                                }
                            }
                        }
                        tree.pop();
                        set_wait_for(TYPES, "/(){}");
                    } else {
                        assert TYPES.contains(c);
                        if(!tree.peek().variables.isEmpty() && ";".equals(tree.peek().variables.peek().type)) {
                            tree.peek().variables.peek().type = String.valueOf((char)c);
                        } else {
                            Variable var = new Variable();
                            var.type = String.valueOf((char)c);
                            var.is_field = !tree.peek().is_action;
                            var.parent = tree.peek();
                            tree.peek().variables.push(var);
                        }
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
    
    int index_gen = 0;
    String pkg = null;
    String src = null;
    String in_dir = null;
    String in_name = null;
    String outfile = null;
    String class_name = null;
    boolean stdout = false;
    boolean force = false;

    private void run(String[] args) throws IOException {
        
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
        
        singleTest[0] = true;
        localMultiTest[0] = false;

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
        
        
        index_gen = 0;
        Variable.line_read = false;
        
        StringBuilder sb = new StringBuilder();

        
        generate_code(sb);
        
        render_code(args, sb);
        
    }

    boolean[] singleTest = new boolean[]{true};
    boolean[] localMultiTest = new boolean[]{false};
    int saved_code_pos = -1;
    ArrayList<Variable> classes = new ArrayList<>();
    
    private void generate_code(StringBuilder sb) {
        String banner = "!Please, Don't change or delete this comment!";
        String script1 = "$script$:" + script;
        int max_len = Math.max(script.length(), banner.length());
        if(banner.length() < max_len) {
            banner = String.format("%" + (max_len - banner.length()) / 2 + "s", "") + banner;
            banner += String.format("%" + (max_len - banner.length()) + "s", "");
        } else if(script1.length() < max_len) {
            script1 = String.format("%" + (max_len - script1.length()) / 2 + "s", "") + script1;
            script1 += String.format("%" + (max_len - script1.length()) + "s", "");
        }
        String stars = String.format("%" + max_len + "s", "").replace(" ", "*");
        sb.append("/*").append(stars).append("*/\n");
        sb.append("/*").append(banner).append("*/\n");
        sb.append("/*").append(script1).append("*/\n");
        sb.append("/*").append(stars).append("*/\n");
        if(pkg != null) {
            sb.append("package ").append(pkg).append(";\n");
        }
        sb.append("import java.io.IOException;\n");
        sb.append("import net.leksi.contest.Solver;\n");
        sb.append("public class ").append(class_name).append(" extends Solver {\n");
        sb.append("    public ").append(class_name).append("() {\n");
        if(singleTest[0]) {
            sb.append("        singleTest = ").append(singleTest[0]).append(";\n");
        }
        if(!stdout || localMultiTest[0]) {
            sb.append("        /*+Preprocess-DONOTCOPY*/\n");
            if(!stdout) {
                sb.append("        localNameIn = \"").append(new File(in_dir, in_name).getPath().replace("\\", "/")).append("\";\n");
            }
            if(localMultiTest[0]) {
                sb.append("        localMultiTest = true;\n");
            }
            sb.append("        /*-Preprocess-DONOTCOPY*/\n");
        }
        sb.append("    }\n");
        classes.forEach(v -> {
            v.render_class("    ", sb);
        });
        sb.append("    @Override\n");
        sb.append("    public void solve() throws IOException {\n");
        tree.peek().render_process("        ", sb);
        if(!Variable.line_read) {
            sb.append("        sc.nextLine();\n");
        }
        write_code_banner("        ", sb);
        saved_code_pos = sb.length();
        sb.append("    }\n");
        sb.append("    static public void main(String[] args) throws IOException {\n");
        sb.append("        new ").append(class_name).append("().run();\n");
        sb.append("    }\n");
        sb.append("}\n");
    }

    private void render_code(String[] args, StringBuilder sb) throws IOException {
        if (!stdout) {
            File src_file = null;
            if (outfile == null) {
                src_file = new File(src);
                if (pkg != null) {
                    for (String part : pkg.split("\\.")) {
                        src_file = new File(src_file, part);
                    }
                }
                //        System.out.println(src_file);
                if (!src_file.exists()) {
                    src_file.mkdirs();
                }
                src_file = new File(src_file, class_name + ".java");
                if (!force && src_file.exists()) {
                    throw new IOException("File exists: " + src_file + "! Use -force to overwrite.");
                }

                if (src_file.exists()) {
                    File save = null;
                    for (int j = 1;; j++) {
                        save = new File(src_file.getPath() + ";" + Integer.toString(j));
                        if (!save.exists()) {
                            break;
                        }
                    }
                    src_file.renameTo(save);
                    String script2 = null;
                    try (
                            FileReader fr = new FileReader(save);
                            BufferedReader br = new BufferedReader(fr);) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            if (line.contains("$script$:")) {
                                script2 = line.trim();
                                script2 = script2.substring(script2.indexOf("/*"));
                                script2 = script2.substring(0, script2.lastIndexOf("*/"));
                                script2 = script2.trim();
                                script2 = script2.substring(script2.indexOf("$script$:") + "$script$:".length());
                                script2 = script2.trim();
                                break;
                            }
                        }
                    }
                    if (script2 != null) {
                        String[] args1 = new String[args.length + 2];
                        for (int j = 0; j < args.length; j++) {
                            if (args[j].equals(script)) {
                                args1[j] = script2;
                            } else {
                                args1[j] = args[j];
                            }
                        }
                        args1[args.length] = "-outfile";
                        File tmp = File.createTempFile("temp", null);
                        tmp.deleteOnExit();
                        args1[args.length + 1] = tmp.getAbsolutePath();
                        try {
                            new Wizard().run(args1);
                            StringBuilder sb2 = new StringBuilder();

                            try (
                                    FileReader fr = new FileReader(save);
                                    BufferedReader br = new BufferedReader(fr);
                                    FileReader fr_tmp = new FileReader(tmp);
                                    BufferedReader br_tmp = new BufferedReader(fr_tmp);) {
                                String line;
                                String line_tmp = null;
                                boolean stopped = false;
                                boolean stopped_tmp = false;
                                ArrayList<String> saved_code = new ArrayList<>();
                                while (!stopped) {
                                    if (!stopped_tmp) {
                                        String line1 = br_tmp.readLine();
                                        if (line1 == null) {
                                            stopped_tmp = true;
                                        } else {
                                            if ("".equals(line1.trim())) {
                                                continue;
                                            }
                                            line_tmp = line1;
                                        }
                                    }
                                    while (!stopped) {
                                        String line1 = br.readLine();
                                        if (line1 == null) {
                                            stopped = true;
                                            break;
                                        }
                                        line = line1;
                                        if (line.trim().equals(line_tmp.trim())) {
                                            if (sb2.toString().trim().length() > 0) {
                                                saved_code.add(sb2.toString());
                                                sb2.delete(0, sb2.length());
                                            }
                                            break;
                                        }
                                        sb2.append(line).append("\n");
                                    }
                                }
                                if (sb2.toString().trim().length() > 0) {
                                    saved_code.add(sb2.toString());
                                    sb2.delete(0, sb2.length());
                                }
                                for (int j = 0; j < saved_code.size(); j++) {
                                    sb2.append("\n");
                                    sb2.append("        /**** begin of piece #").append(j + 1).append("/").append(saved_code.size()).append(" of saved code ****/\n");
                                    sb2.append(saved_code.get(j));
                                    sb2.append("        /**** end of piece #").append(j + 1).append("/").append(saved_code.size()).append(" of saved code ****/\n");
                                    sb2.append("\n");
                                }
                                if (sb2.length() > 0) {
                                    sb.insert(saved_code_pos, sb2.toString());
                                }
                            }
                        } catch (Exception ex) {
                            System.out.println("Can not find changes. See previous version at " + save);
                        }
                    }
                }

                File in_file = new File(in_dir);
                if (!in_file.exists()) {
                    in_file.mkdirs();
                }
                in_file = new File(in_dir, in_name);
                if (!in_file.exists()) {
                    try (
                            FileWriter fw = new FileWriter(in_file);) {
                        fw.write("");
                    }
                }
            } else {
                src_file = new File(outfile);
            }
            try (
                    FileWriter fw = new FileWriter(src_file);) {
                fw.write(sb.toString());
            }

        } else {
            System.out.println(sb);
        }
    }

}
