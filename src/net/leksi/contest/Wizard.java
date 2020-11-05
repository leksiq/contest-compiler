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

import java.util.ArrayList;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 *
 * @author alexei
 */
public class Wizard {
    
    static public void main(final String[] args) {
        new Wizard().run(args);
//        new Wizard().run(new String[]{"A", "*in,m/ia[n]/ss/(m;lb[]/)ic[m]"});
    }

    private static void usage() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        StringBuilder sb_class = new StringBuilder();
        String base;
        Cycle parent = null;
        String class_name;
        int field_gen = 0;
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

    private void run(String[] args) {
        if(args.length < 2) {
            usage();
            return;
        }
        Variable var = null;
        Cycle last_cycle = null;
        
        
        last_cycle = new Cycle();
        cycles.push(last_cycle);
        last_cycle.count = "";
        all_cycles.add(last_cycle);
        
        String script = args[1].trim();
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
                    for(; DELIMS.indexOf(script.charAt(j)) < 0 && !Character.isSpaceChar(script.charAt(j)); j++){}
                    String name = script.substring(i, j);
//                    System.out.println(name);
                    if(wait_for_var) {
                        var = new Variable();
                        cycles.peek().variables.add(var);
                        last_cycle = cycles.peek();
                        var.name = name;
                        var.type = type;
                        wait_for_var = false;
                    } else if(";".equals(wf)) {
                        cycles.peek().count = name;
                    } else if("]".equals(wf)) {
                        var.length = name;
                        var = null;
                    }
                    for(; Character.isSpaceChar(script.charAt(j)); j++){}
                    if(wf.indexOf(script.charAt(j)) < 0) {
                        throw new RuntimeException("Unexpected symbol: '" + script.charAt(j) + "', expecting: \"" + wf + "\"");
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
        System.out.println(cycles);
        
        class Reenter {
            Consumer<Cycle> process;
        }
        
        Reenter reenter = new Reenter();

        StringBuilder sb = new StringBuilder();
        
        int[] indention = new int[]{1};
        
        Supplier<String> indent = () -> String.format("%" + (indention[0] * 4) + "s", "");
        
        reenter.process = (cycle) -> {
            cycle.class_name = "Cy" + class_gen[0]++;
            cycle.sb_class.append("    static class ").append(cycle.class_name).append(" {\n");
            indention[0]++;
            if(cycle.parent == null) {
                cycle.base = "input";
                sb.append("import java.io.IOException;\n");
                sb.append("import java.util.Arrays;\n");
                sb.append("import net.leksi.contest.Solver;\n");
                sb.append("public class ").append(args[0]).append(" extends Solver {\n");
                sb.append("    private solve(final ").append(cycle.class_name).append(" input) {\n");
                sb.append("    /*\n");
                sb.append("     * Generated from \"").append(script).append("\".\n");
                sb.append("     * Write your code below. *\n");
                sb.append("     */\n");
                sb.append("    }\n");
                sb.append("    public ").append(args[0]).append("() {\n");
                sb.append("        nameIn = \"").append(args[0]).append(".in\"; singleTest = ").append(singleTest[0]).append(";\n");
                sb.append("    }\n");
                sb.append("    static public void main(String[] args) throws IOException {\n");
                sb.append("        new ").append(args[0]).append("().run();\n");
                sb.append("    }\n");
                sb.append("    @Override\n");
                sb.append("    public void readInputAndSolve() throws IOException {\n");
                sb.append("        ").append(cycle.class_name).append(" input = new ").append(cycle.class_name).append("();\n");
            }
            boolean[] line_read = new boolean[]{false};
            cycle.variables.forEach(v -> {
                if(v instanceof Variable) {
                    Variable vv = (Variable)v;
                    if(!"/".equals(vv.type)) {
                        String type1;
                        String nextType;
                        switch(vv.type.charAt(0)) {
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
                        String next = vv.type.charAt(0) != 's' ? type1.substring(0, 1).toUpperCase() + type1.substring(1) : "";
                        if(vv.type.endsWith("[")) {
                            type1 += "[]";
                        }
                        cycle.sb_class.append("        ").append(type1).append(" ").append(vv.name).append(";\n");
                        
                        sb.append(indent.get()).append(cycle.base).append(".").append(vv.name);
                        if(vv.length != null) {
                            if(!"".equals(vv.length)) {
                                sb.append(" = new ").append(type1).append("[");
                                String count;
                                try {
                                    int len = Integer.valueOf(vv.length);
                                    count = Integer.toString(len);
                                } catch(NumberFormatException e) {
                                    count =  cycle.base + "." + vv.length;
                                }
                                sb.append(count).append("]").append(";\n");
                                sb.append(indent.get()).append("for(int ").append("_i_").append(vv.name).append(" = 0; _i_").
                                        append(vv.name).append(" < ").append(count).append("; _i_").append(vv.name).append("++) {\n");
                                indention[0]++;
                                sb.append(indent.get()).append(cycle.base).append(".").append(vv.name).
                                        append("[").append("_i_").append(vv.name).append("] = next").
                                        append(next).append("();\n");
                                indention[0]--;
                                sb.append(indent.get()).append("}\n");
                            } else {
                                line_read[0] = true;
                                if(vv.type.charAt(0) != 's') {
                                    sb.append(" = Arrays.stream(").append("cs.nextLine().trim().split(\"\\\\s+\")).mapTo").append(next).append("(").
                                            append(vv.type.charAt(0) == 'i' ? "Integer" : next).append("::valueOf).toArray()");
                                } else {
                                    sb.append(" = cs.nextLine().trim().split(\"\\\\s+\")");
                                }
                                sb.append(";\n");
                            }
                        } else {
                            if(vv.type.charAt(0) != 's') {
                                sb.append(" = sc.next").append(next).append(";\n");
                            } else {
                                sb.append(" = sc.nextLine().trim();\n");
                                line_read[0] = true;
                            }
                        }
                    } else {
                        if(!line_read[0]) {
                            sb.append(indent.get()).append("sc.nextLine();\n");
                        }
                        line_read[0] = false;
                    }
                } else {
                    Cycle cy = (Cycle)v;
                    String field_name = "_f" + (cycle.field_gen++);
                    cy.base = cycle.base + "." + field_name + "[" + "_i" + field_name + "]";
                    sb.append(indent.get()).append("for(int ").append("_i").append(field_name).append(" = 0; _i").
                            append(field_name).append(" < ").append(cy.count).append("; _i").append(field_name).append("++) {\n");
                    reenter.process.accept(cy);
                    sb.append(indent.get()).append("}\n");
                    cycle.sb_class.append("        ").append(cy.class_name).append("[] ").append(field_name).append(";\n");
                }
            });
            indention[0]--;
            cycle.sb_class.append("   ").append("}\n");
        };
        
        reenter.process.accept(all_cycles.get(0));
        

        sb.append("        solve(input);\n");
        sb.append("    }\n");
        sb.append(all_cycles.stream().map(cy -> cy.sb_class).collect(Collectors.joining()));
        sb.append("}\n");
        System.out.println(sb);
    }
}
