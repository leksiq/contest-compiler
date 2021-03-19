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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.AccessControlException;

public abstract class Solver {
    
    protected String nameIn = null;
    protected String nameOut = null;
    protected boolean singleTest = false;

    protected boolean preprocessDebug = false;
    protected boolean doNotPreprocess = false;
    protected PrintStream debugPrintStream = null;
    
    protected MyScanner sc = null;
    
    /*+Preprocess-DONOTCOPY*/
    
    
    private InputStream testInputStream = null;
    private OutputStream testOutputStream = null;
    
    public void setTestInputStream(final InputStream is) {
        testInputStream = is;
    }
    
    public void setTestOutputStream(final OutputStream os) {
        testOutputStream = os;
    }
    
    protected boolean localMultiTest = false;
    protected String localNameIn = "";
    protected boolean localShowTestCases = false;
    
    private void Preprocess_DONOTCOPY() {
        if(!doNotPreprocess && testInputStream == null) {
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
                System.out.println("----- " + running + " output -----");
            }
        }
        if(localMultiTest) {
            singleTest = false;
        }
        if(testInputStream == null && !"".equals(localNameIn)) {
            nameIn = localNameIn;
            nameOut = null;
        }
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
                    System.out.println("--- test " + current_test + " ---");
                } else if(localShowTestCases) {
                    System.out.println("--- test case " + current_test + " ---");
                }
                /*-Preprocess-DONOTCOPY*/
                solve();
                System.out.flush();
            }
        } else {
            count_tests = 1;
            current_test = 1;
            solve();
            System.out.flush();
        }
        /*+Preprocess-DONOTCOPY*/
        if(doNotPreprocess && testOutputStream == null) {
            System.out.println("/*********************************/");
            System.out.println("/* Warning! doNotPreprocess=true */");
            System.out.println("/* Target file is not compiled!  */");
            System.out.println("/*********************************/");
            System.out.flush();
        }
        /*-Preprocess-DONOTCOPY*/
    }
    
    abstract protected void solve() throws IOException;

    public void run() throws IOException {
        /*+Preprocess-DONOTCOPY*/
        Preprocess_DONOTCOPY();
        /*-Preprocess-DONOTCOPY*/
        boolean done = false;
        try {
        /*+Preprocess-DONOTCOPY*/
            if (testInputStream != null) {
                System.setIn(testInputStream);
            } else
        /*-Preprocess-DONOTCOPY*/
            
            if(nameIn != null && new File(nameIn).exists()) {
                    try (
                        FileInputStream fis = new FileInputStream(nameIn);
                    ) {
                        select_output();
                        done = true;
                        sc = new MyScanner(fis);
                        process();
                    }
            }
        } catch(IOException ex) {
        } catch(AccessControlException ex) {
        }
        if(!done) {
            select_output();
            sc = new MyScanner(System.in);
            process();
        }
    }

    private void select_output() throws FileNotFoundException {
        /*+Preprocess-DONOTCOPY*/
        if(testOutputStream != null) {
            System.setOut(new PrintStream(testOutputStream));
        } else if (preprocessDebug && debugPrintStream != null) {
            System.setOut(debugPrintStream);
        } else 
        /*-Preprocess-DONOTCOPY*/
        if (nameOut != null) {
            System.setOut(new PrintStream(nameOut));
        }
    }
}
