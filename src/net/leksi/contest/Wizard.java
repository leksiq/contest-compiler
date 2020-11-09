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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 *
 * @author alexei
 */
public class Wizard {
    
    static public void main(final String[] args) throws IOException {
        new Wizard().run(args);
//        new Wizard().run(new String[]{"A", "*in,m/ia[n]/ss/(m;lb[]/)ic[m]"});
//        new Wizard().run(new String[]{"-stdout", "A", "(5;im[5])"});
    }

    private static void usage() {
        System.out.println("Usage: java java_options " + Wizard.class.getCanonicalName() + " wizard_options class-name script");
        System.out.println("    java_options:           java options like -classpath;");
        System.out.println("    wizard_options:");
        System.out.println("        -stdout             - write to stdout (default creates file <class-name>.java);");
        System.out.println("        -src directory      - the directory to generate sources into (default .);");
        System.out.println("        -package package    - the package of class to generate (default empty);");
        System.out.println("        -force              - overwrite existing file (default throws exception);");
        System.out.println("    class-name:             name of class to generate;");
        System.out.println("    script:                 input script;");
    }
    
    interface IVariable {}
    static class Variable implements IVariable {
        String type;
        String name;
        String length;
        @Override
        public String toString() {
            return String.format("{V:%s;%s;%s}", type == null ? "-" : type, 
                name == null ? "-" : name, length == null ? "-" : length
            );
        }
    }
    static class Cycle implements IVariable {
        String count;
        ArrayList<IVariable> variables = new ArrayList<>();
        StringBuilder sb_class;
        String base;
        Cycle parent = null;
        String class_name;
        int field_gen = 0;
        Variable simple = null;
        @Override
        public String toString() {
            return String.format("{C:%s;[%s]}", 
                count == null ? "-" : count, 
                variables.stream().map(Object::toString).collect(Collectors.joining(","))
            );
        }
    }
    static final String DELIMS = "/,;()[]";
    static final String TYPES = "ilbfds";
    
    Stack<Cycle> cycles = new Stack<>();
    ArrayList<Cycle> all_cycles = new ArrayList<>();
    TreeSet<String> var_names = new TreeSet<>();

    private void run(String[] args) throws IOException {
        String pkg = null;
        String src = ".";
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
                } else if("-stdout".equals(args[i])) {
                    stdout = true;
                } else if("-force".equals(args[i])) {
                    force = true;
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
        
        Variable var = null;
        Cycle last_cycle = null;
        
        
        last_cycle = new Cycle();
        last_cycle.sb_class = new StringBuilder();
        cycles.push(last_cycle);
        last_cycle.count = "";
        all_cycles.add(last_cycle);
        
        
        boolean[] singleTest = new boolean[]{true};
        int i = 0;
        if(script.charAt(0) == '*') {
            singleTest[0] = false;
            i++;
        }
        
        String wf = TYPES + "(";
        
        String type = null;
        boolean wait_for_name = false;
        boolean wait_for_var = false;
        int[] class_gen = new int[]{0};
        
        for(; i < script.length(); i++) {
            char c = script.charAt(i);
            if(!Character.isSpaceChar(c)) {
//                System.out.println(c);
                if(!wait_for_name) {
                    if(wf.indexOf(c) < 0) {
                        throw new RuntimeException("Unexpected symbol: '" + c + "', expecting: \"" + wf + "\"");
                    }
                    if(TYPES.indexOf(c) >= 0) {
                        type = String.valueOf(c);
                        wf = "[/,;()";
                        wait_for_name = true;
                        wait_for_var = true;
                    } else {
                        switch(c) {
                            case '/':
                                var = new Variable();
                                var.type = "/";
                                cycles.peek().variables.add(var);
                                last_cycle = cycles.peek();
                                var = null;
                                wf = TYPES + "(" + (cycles.size() > 1 ? ")" : "");
                                wait_for_name = false;
                                break;
                            case ',':
                                var = null;
                                wf = "[/,;()";
                                wait_for_name = true;
                                wait_for_var = true;
                                break;
                            case ';':
                                var = null;
                                wf = TYPES + "(";
                                wait_for_name = false;
                                break;
                            case '[':
                                if(var != null) {
                                    var.type += "[";
                                }
                                wf = "]";
                                wait_for_name = true;
                                break;
                            case ']':
                                wf = "/,;()";
                                wait_for_name = false;
                                break;
                            case '(':
                                last_cycle = new Cycle();
                                last_cycle.parent = cycles.peek();
                                cycles.peek().variables.add(last_cycle);
                                cycles.push(last_cycle);
                                all_cycles.add(last_cycle);
                                var = null;
                                wf = ";";
                                wait_for_name = true;
                                break;
                            case ')':
                                cycles.pop();
                                var = null;
                                wf = TYPES + "()";
                                wait_for_name = false;
                                break;
                        }
                    }
                } else {
                    int j = i;
                    for(; j < script.length() && DELIMS.indexOf(script.charAt(j)) < 0 && !Character.isSpaceChar(script.charAt(j)); j++){}
                    String name = script.substring(i, j);
//                    System.out.println(name);
                    if(wait_for_var) {
                        var = new Variable();
                        cycles.peek().variables.add(var);
                        last_cycle = cycles.peek();
                        if("".equals(name)) {
                            throw new RuntimeException("Unexpected char: '" + c + "', variable expected\n    \"" + script + "\"\n      " + String.format("%" + (i == 0 ? "" : i) + "s", "^"));
                        }
                        var.name = name;
                        var.type = type;
                        var_names.add(name);
                        wait_for_var = false;
                    } else if(";".equals(wf)) {
                        if("".equals(name)) {
                            throw new RuntimeException("Unexpected char: '" + c + "', variable or number expected\n    \"" + script + "\"\n      " + String.format("%" + (i == 0 ? "" : i) + "s", "^"));
                        }
                        cycles.peek().count = name;
                    } else if("]".equals(wf)) {
                        var.length = name;
                        var = null;
                    }
                    if(j < script.length()) {
                        for(; Character.isSpaceChar(script.charAt(j)); j++){}
                        if(wf.indexOf(script.charAt(j)) < 0) {
                            throw new RuntimeException("Unexpected symbol: '" + script.charAt(j) + "', expecting: \"" + wf + "\"");
                        }
                    }
                    i = j - 1;
                    wait_for_name = false;
                }
            }
        }
        IVariable last_var = last_cycle.variables.get(last_cycle.variables.size() - 1);
        if(!(last_var instanceof Variable) || !"/".equals(((Variable)last_var).type)) {
            var = new Variable();
            var.type = "/";
            last_cycle.variables.add(var);
        }
//        System.out.println(cycles);
        
        class Reenter {
            Consumer<Cycle> process;
        }
        
        Reenter reenter = new Reenter();

        StringBuilder sb0 = new StringBuilder();
        if(pkg != null) {
            sb0.append("package ").append(pkg).append(";\n");
        }
        sb0.append("import java.io.IOException;\n");
        sb0.append("import net.leksi.contest.Solver;\n");
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        sb1.append("public class ").append(class_name).append(" extends Solver {\n");
        sb1.append("    public ").append(class_name).append("() {\n");
        sb1.append("        nameIn = \"").append(class_name).append(".in\"; singleTest = ").append(singleTest[0]).append(";\n");
        sb1.append("    }\n");
        sb2.append("    @Override\n");
        sb2.append("    public void readInput() throws IOException {\n");
        
        int[] indention = new int[]{1};
        
        Supplier<String> indent = () -> String.format("%" + (indention[0] * 4) + "s", "");
        UnaryOperator<String> type2 = s -> {
            String type1 = "int";
            switch (s.charAt(0)) {
                case 'i':
                    type1 = "int";
                    break;
                case 'l':
                    type1 = "long";
                    break;
                case 'd':
                    type1 = "double";
                    break;
                case 's':
                    type1 = "String";
                    break;
                default:
                    type1 = "int";
                    break;
            }
            return type1;
        };
        UnaryOperator<String> next2 = s -> {
            if(s.charAt(0) != 's') {
                String type1 = type2.apply(s);
                return type1.substring(0, 1).toUpperCase() + type1.substring(1);
            }
            return "";
        };
        
        BiFunction<Cycle, Variable, String> find_variable = (cy, vv) -> {
            try {
                int len = Integer.valueOf(vv.length);
                return Integer.toString(len);
            } catch(NumberFormatException e) {
                Variable res = null;
                Cycle cyc = cy;
                while(true) {
                    res = (Variable)cy.variables.stream().filter(v -> v instanceof Variable && vv.length.equals(((Variable)v).name)).findFirst().orElse(null);
                    if(res != null) {
                        break;
                    }
                    cy = cy.parent;
                    if(cy == null) {
                        break;
                    }
                }
                return res != null ? (cy.base != null ? cy.base + "." : "") + res.name : "";
            }
        };
        
        reenter.process = (cycle) -> {
            String[] indent1 = new String[]{"    "};
            if(cycle.parent != null && cycle.simple == null) {
                cycle.sb_class.append("    static class ").append(cycle.class_name).append(" {\n");
                indent1[0] += indent1[0];
            }
            indention[0]++;
            boolean[] line_read = new boolean[]{false};
            cycle.variables.forEach(v -> {
                if(v instanceof Variable) {
                    Variable vv = (Variable)v;
                    if(!"/".equals(vv.type)) {
                        String nextType;
                        String type1 = type2.apply(vv.type);
                        String next = next2.apply(vv.type);
                        if(vv.type.endsWith("[")) {
                            type1 += "[]";
                        }
                        if(cycle.simple == null) {
                            cycle.sb_class.append(indent1[0]).append(type1).append(" ").append(vv.name).append(";\n");
                        }
                        
                        if(cycle.simple == null || vv.length == null || "".equals(vv.length)) {
                            sb2.append(indent.get());
                            if(cycle.base != null) {
                                sb2.append(cycle.base);
                                if(cycle.simple == null) {
                                    sb2.append(".");
                                }
                            }
                            if(cycle.simple == null) {
                                sb2.append(vv.name);
                            }
                        }
                        if(vv.length != null) {
                            String count =  find_variable.apply(cycle, vv);
                            if(!"".equals(vv.length)) {
                                if(cycle.simple == null || vv.length == null && "".equals(vv.length)) {
                                    sb2.append(" = new ").append(type1).append("[");
                                    sb2.append(count).append("]").append(";\n");
                                }
                                sb2.append(indent.get()).append("for(int ").append("_i_").append(vv.name).append(" = 0; _i_").
                                        append(vv.name).append(" < ").append(count).append("; _i_").append(vv.name).append("++) {\n");
                                indention[0]++;
                                sb2.append(indent.get()).append(cycle.base);
                                if(cycle.simple == null) {
                                    sb2.append(".").append(vv.name);
                                }
                                sb2.append("[").append("_i_").append(vv.name).append("] = sc.next").
                                        append(next).append("();\n");
                                indention[0]--;
                                sb2.append(indent.get()).append("}\n");
                            } else {
                                line_read[0] = true;
                                if(vv.type.charAt(0) != 's') {
                                    if(sb0.indexOf("java.util.Arrays") < 0) {
                                        sb0.append("import java.util.Arrays;\n");
                                    }
                                    sb2.append(" = Arrays.stream(").append("sc.nextLine().trim().split(\"\\\\s+\")).mapTo").append(next).append("(").
                                            append(vv.type.charAt(0) == 'i' ? "Integer" : next).append("::valueOf).toArray()");
                                } else {
                                    sb2.append(" = sc.nextLine().trim().split(\"\\\\s+\")");
                                }
                                sb2.append(";\n");
                            }
                        } else {
                            if(vv.type.charAt(0) != 's') {
                                sb2.append(" = sc.next").append(next).append("();\n");
                            } else {
                                sb2.append(" = sc.nextLine().trim();\n");
                                line_read[0] = true;
                            }
                        }
                    } else {
                        if(!line_read[0]) {
                            sb2.append(indent.get()).append("sc.nextLine();\n");
                        }
                        line_read[0] = false;
                    }
                } else {
                    Cycle cy = (Cycle)v;
                    String field_name;
                    if(!cy.variables.stream().anyMatch(v1 -> (v1 instanceof Cycle)) && cy.variables.stream().filter(v1 -> !"/".equals(((Variable)v1).type)).count() <= 1) {
                        cy.simple = (Variable)cy.variables.stream().filter(v1 -> !"/".equals(((Variable)v1).type)).findFirst().get();
                        field_name = cy.simple.name;
                        cy.class_name = type2.apply(cy.simple.type);
//                        cy.base = cycle.base;
                    } else {
                        do {
                            field_name = "f" + (cycle.field_gen++);
                        } while (!var_names.contains(field_name));
                        cy.class_name = "Cy" + class_gen[0]++;
                        cy.sb_class = new StringBuilder();
                    }
                    cy.base = (cycle.base != null ? cycle.base + "." : "") + field_name + "[" + "_i" + field_name + "]";
                    sb2.append(indent.get());
                    if (cycle.base != null) {
                        sb2.append(cycle.base).append(".");
                    }
                    sb2.append(field_name).append(" = new ").append(cy.class_name).append("[").append(cy.count).append("]");
                    if(cy.simple != null && cy.simple.length != null) {
                        sb2.append("[").append(find_variable.apply(cy, cy.simple)).append("]");
                    }
                    sb2.append(";\n");
                    sb2.append(indent.get()).append("for(int ").append("_i").append(field_name).append(" = 0; _i").
                            append(field_name).append(" < ").append(cy.count).append("; _i").append(field_name).append("++) {\n");
                    if(cy.simple == null) {
                        sb2.append(indent.get()).append("    ").append(field_name).append("[_i").append(field_name).append("").append("] = new ").append(cy.class_name).append("();\n");
                    }
                    reenter.process.accept(cy);
                    sb2.append(indent.get()).append("}\n");
                    cycle.sb_class.append(indent1[0]).append(cy.class_name);
                    if(cy.simple != null && cy.simple.length != null) {
                        cycle.sb_class.append("[]");
                    }
                    cycle.sb_class.append("[] ").append(field_name).append(";\n");
                }
            });
            if(cycle.parent != null && cycle.simple == null) {
                cycle.sb_class.append("    ").append("}\n");
            }
            indention[0]--;
        };
        
        reenter.process.accept(all_cycles.get(0));
        sb1.append("    /*\n");
        sb1.append("     * Generated from \"");
        sb1.append(script);
        sb1.append("\".\n");
        sb1.append("     */\n");
        sb1.append(all_cycles.get(0).sb_class);
        sb1.append("    @Override\n");
        sb1.append("    protected void solve() {\n");
        sb1.append("        /*\n");
        sb1.append("         * Write your code below.\n");
        sb1.append("         */\n");
        sb1.append("\n");
        sb1.append("    }\n");

        sb1.append(all_cycles.stream().skip(1).filter(cy -> cy.simple == null).map(cy -> cy.sb_class).collect(Collectors.joining()));
        sb1.append(sb2);
        sb1.append("    }\n");
        sb1.append("    static public void main(String[] args) throws IOException {\n");
        sb1.append("        new ").append(class_name).append("().run();\n");
        sb1.append("    }\n");
        sb1.append("}\n");
        sb1.insert(0, sb0);
        if(!stdout) {
            File src_file = new File(src);
            if(pkg != null) {
                for(String part: pkg.split("\\.")) {
                    src_file = new File(src_file, part);
                }
            }
    //        System.out.println(src_file);
            if(!src_file.exists()) {
                src_file.mkdirs();
            }
            src_file = new File(src_file, class_name + ".java");
            if(!force && src_file.exists()) {
                throw new IOException("File exists: " + src_file);
            }

            try (
                    FileWriter fw = new FileWriter(src_file);
                ) {
                fw.write(sb1.toString());
            }
        } else {
            System.out.println(sb1);
        }
    }
}
