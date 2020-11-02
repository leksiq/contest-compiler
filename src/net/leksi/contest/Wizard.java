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

/**
 *
 * @author alexei
 */
public class Wizard {
    
    static public void main(final String[] args) {
        new Wizard().run(args);
    }

    private static void usage() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    static class Variable {
        String type;
        String name;
        String length;
    }
    interface CycleOrLine {}
    static class Cycle implements CycleOrLine {
        String count;
        ArrayList<CycleOrLine> items = new ArrayList<>();
    }
    static class Line implements CycleOrLine {
        ArrayList<Variable> variables = new ArrayList<>();
    }
    static enum WaitFor {TYPE, NAME, DELIM};
    static final String DELIMS = "/,;()[]";
    
    ArrayList<Cycle> stack = new ArrayList<>();

    private void run(String[] args) {
        if(args.length < 2) {
            usage();
            return;
        }
        Cycle cycle = new Cycle();
        Line line = new Line();
        Variable var = null;
        
        cycle.count = "";
        stack.add(cycle);
        
        String script = args[1].trim();
        boolean singleTest = true;
        int i = 0;
        if(script.charAt(0) == '*') {
            singleTest = false;
            i++;
        }
        
        WaitFor wf = WaitFor.TYPE;
        
        String type = null;
        
        for(; i < script.length(); i++) {
            if(!Character.isSpaceChar(script.charAt(i))) {
                switch(wf) {
                    case TYPE:
                        type = script.substring(i, i + 1);
                        wf = WaitFor.NAME;
                        break;
                    case NAME:
                        int j = i;
                        for(; DELIMS.indexOf(script.charAt(j)) < 0; j++);
                        if(cycle.count == null) {
                            cycle.count = script.substring(i, j);
                        } else if(var != null && var.type.endsWith("[")) {
                            var.length = script.substring(i, j);
                        } else {
                            var = new Variable();
                            line.variables.add(var);
                            var.name = script.substring(i, j);
                            var.type = type;
                        }
                        wf = WaitFor.DELIM;
                        i = j - 1;
                        break;
                    case DELIM:
                        
                        break;
                }
            }
        }

//        StringBuilder sb = new StringBuilder();
//        sb.append("import java.io.BufferedReader;\n");
//        sb.append("import java.io.IOException;\n");
//        sb.append("import java.io.PrintWriter;\n");
//        sb.append("import net.leksi.contest.Solver;\n");
//        sb.append("public class ").append(args[0]).append(" extends Solver {\n");
//        sb.append("    public ").append(args[0]).append("() {\n");
//        sb.append("        nameIn = \"").append(args[0]).append(".in\"; singleTest = ").append(singleTest).append(";\n");
//        sb.append("    }\n");
//        sb.append("    static public void main(String[] args) throws IOException {\n");
//        sb.append("        new ").append(args[0]).append("().run();\n");
//        sb.append("    }\n");
//        sb.append("    @Override\n");
//        sb.append("    public void process(BufferedReader br, PrintWriter pw) throws IOException {\n");
//        sb.append("    }\n");
//        sb.append("}\n");
//        System.out.println(sb);
    }
}
