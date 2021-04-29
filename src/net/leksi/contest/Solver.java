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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.AccessControlException;
import java.util.Date;
import java.util.Stack;

public abstract class Solver {
    
    protected String nameIn = null;
    protected String nameOut = null;
    protected boolean singleTest = false;

    protected MyScanner sc = null;
    protected PrintWriter pw = null;
    
    /*+Preprocess-DONOTCOPY*/
    
    protected boolean preprocessDebug = false;
    protected boolean doNotPreprocess = false;
    protected PrintStream debugPrintStream = null;

    protected boolean localMultiTest = false;
    protected String localNameIn = "";
    protected boolean localShowTestCases = false;
    protected int localRunTester = 0;
    protected long localRunTesterTimeLimit = 5000;
    
    private void Preprocess_DONOTCOPY() {
        if(localRunTester == 0) {
            if(!doNotPreprocess) {
                String running = getClass().getName();
                if(running.contains("$")) {
                    running = running.substring(0, running.indexOf("$"));
                }
                Preprocessor pp = new Preprocessor();
                pp.debug(preprocessDebug);
                if(preprocessDebug && debugPrintStream != null) {
                    pp.debugPrintStream(debugPrintStream);
                }
                pp.skipPrefix("Preprocess_DONOTCOPY");
                pp.run(running);
                if(preprocessDebug) {
                    pw.println("----- " + running + " output -----");
                }
            }
            if(localMultiTest) {
                singleTest = false;
            }
            if(!"".equals(localNameIn)) {
                nameIn = localNameIn;
                nameOut = null;
            }
        }
    }
    
    private void doNotPreprocessWarning() {
        pw.println("/*********************************/");
        pw.println("/* Warning! doNotPreprocess=true */");
        pw.println("/* Target file is not compiled!  */");
        pw.println("/*********************************/");
    }
    
    /*-Preprocess-DONOTCOPY*/
    
    private int current_test = 0;
    private int count_tests = 0;
    
    protected int currentTest() {
        return current_test;
    }
    
    protected int countTests() {
        return count_tests;
    }
    
    private void process() throws IOException {
        if(!singleTest) {
            count_tests = sc.nextInt();
            sc.nextLine();
            for(current_test = 1; current_test <= count_tests; current_test++) {
                /*+Preprocess-DONOTCOPY*/
                if(localMultiTest) {
                    pw.println("--- test " + current_test + " ---");
                } else if(localShowTestCases) {
                    pw.println("--- test case " + current_test + " ---");
                }
                /*-Preprocess-DONOTCOPY*/
                solve();
                /*+Preprocess-DONOTCOPY*/
                if(doNotPreprocess) {
                    doNotPreprocessWarning();
                }
                /*-Preprocess-DONOTCOPY*/
                pw.flush();
            }
        } else {
            count_tests = 1;
            current_test = 1;
            solve();
            /*+Preprocess-DONOTCOPY*/
            if(doNotPreprocess) {
                doNotPreprocessWarning();
            }
            /*-Preprocess-DONOTCOPY*/
        }
    }
    
    abstract protected void solve() throws IOException;

    public void run() throws IOException {
        /*+Preprocess-DONOTCOPY*/
        if (localRunTester == 0) {
            Preprocess_DONOTCOPY();
        /*-Preprocess-DONOTCOPY*/
            boolean done = false;
            try {
                if(nameIn != null) {
                    if(new File(nameIn).exists()) {
                        try (
                            FileInputStream fis = new FileInputStream(nameIn);
                            PrintWriter pw0 = select_output();
                        ) {
                            select_output();
                            done = true;
                            sc = new MyScanner(fis);
                            pw = pw0;
                            process();
                        }
                    } else {
                        throw new RuntimeException("File " + new File(nameIn).getAbsolutePath() + " does not exist!");
                    }
                }
            } catch(IOException | AccessControlException ex) {}
            if(!done) {
                try (
                    PrintWriter pw0 = select_output();
                ) {
                    sc = new MyScanner(System.in);
                    pw = pw0;
                    process();
                }
            }
        /*+Preprocess-DONOTCOPY*/
        } else {
            singleTest = true;
            tester();
        }
        /*-Preprocess-DONOTCOPY*/
    }

    private PrintWriter select_output() throws FileNotFoundException {
        /*+Preprocess-DONOTCOPY*/
        if (preprocessDebug && debugPrintStream != null) {
            return new PrintWriter(debugPrintStream);
        }
        /*-Preprocess-DONOTCOPY*/
        if (nameOut != null) {
            return new PrintWriter(nameOut);
        }
        return new PrintWriter(System.out);
    }

    /*+Preprocess-DONOTCOPY*/
    
    protected Tester createTester(){
        return null;
    }
    
    private Stack<String> solve_output = new Stack<>();
    
    volatile Exception exception_at_thread = null;
    
    private void tester() throws IOException {
            
        int count = 0;
        
        Tester tester = createTester();
        
        if(tester == null) {
            return;
            
        }
        tester.beforeTesting();

        while(!tester.broken() && --localRunTester >= 0) {
            StringWriter sw = new StringWriter();
            PrintWriter tpw = new PrintWriter(sw);
            tester.generateInput();
            tpw.println(tester.inputDataToString());
            tpw.flush();

            System.setIn(new ByteArrayInputStream(sw.toString().getBytes()));
            sc = new MyScanner(System.in);
            StringWriter testOutputStream = new StringWriter();
            pw = new PrintWriter(testOutputStream);
            Thread th = new Thread(() -> {
                try {
                    solve();
                } catch (Exception ex) {
                    exception_at_thread = ex;
                }
            });
            th.start();
            try {
                long start = new Date().getTime();
                th.join(localRunTesterTimeLimit);
                if(new Date().getTime() - start >= localRunTesterTimeLimit) {
                    System.err.println("Time limit "+ localRunTesterTimeLimit + " ms exceeded at solve()!\ninput_data: " + tester.inputDataToString() + "\n"
                            + "set localRunTesterTimeLimit field to change this limit.");
                    Runtime.getRuntime().exit(1);
                }
            } catch (InterruptedException ex) {
            }
            if(exception_at_thread != null) {
                System.err.println("Exception at solve()!\ninput_data: " + tester.inputDataToString() + "\nissue: ");
                exception_at_thread.printStackTrace(System.err);
                break;
            }
            pw.flush();
            BufferedReader br2 = new BufferedReader(new StringReader(testOutputStream.toString()));
            String line;
            solve_output.clear();
            while ((line = br2.readLine()) != null) {
                solve_output.push(line);
            }
            try {
                tester.testOutput(solve_output);
            } catch(Exception ex) {
                System.err.println("Exception at test()!\ninput_data: " + tester.inputDataToString() + "\nissue: ");
                ex.printStackTrace(System.err);
                break;
            }
            count++;
            System.err.println(count + " done");
        }
    }
    
    /*-Preprocess-DONOTCOPY*/
}
