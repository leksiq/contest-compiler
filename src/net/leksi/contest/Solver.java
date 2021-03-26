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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.security.AccessControlException;
import java.util.List;
import java.util.Stack;

public abstract class Solver {
    
    protected String nameIn = null;
    protected String nameOut = null;
    protected boolean singleTest = false;

    protected MyScanner sc = null;
    
    /*+Preprocess-DONOTCOPY*/
    
    protected boolean preprocessDebug = false;
    protected boolean doNotPreprocess = false;
    protected PrintStream debugPrintStream = null;

    protected PrintStream tps = null;
    
    protected boolean localMultiTest = false;
    protected String localNameIn = "";
    protected boolean localShowTestCases = false;
    protected int localRunTester = 0;
    
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
                    System.out.println("----- " + running + " output -----");
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
                /*+Preprocess-DONOTCOPY*/
                if(doNotPreprocess) {
                    doNotPreprocessWarning();
                }
                /*-Preprocess-DONOTCOPY*/
                System.out.flush();
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
            System.out.flush();
        }
    }
    
    private void doNotPreprocessWarning() {
        System.out.println("/*********************************/");
        System.out.println("/* Warning! doNotPreprocess=true */");
        System.out.println("/* Target file is not compiled!  */");
        System.out.println("/*********************************/");
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
                        ) {
                            select_output();
                            done = true;
                            sc = new MyScanner(fis);
                            process();
                        }
                    } else {
                        throw new RuntimeException("File " + new File(nameIn).getAbsolutePath() + " does not exist!");
                    }
                }
            } catch(IOException | AccessControlException ex) {}
            if(!done) {
                select_output();
                sc = new MyScanner(System.in);
                process();
            }
        /*+Preprocess-DONOTCOPY*/
        } else {
            singleTest = true;
            tester();
        }
        /*-Preprocess-DONOTCOPY*/
    }

    private void select_output() throws FileNotFoundException {
        /*+Preprocess-DONOTCOPY*/
        if (preprocessDebug && debugPrintStream != null) {
            System.setOut(debugPrintStream);
        } else 
        /*-Preprocess-DONOTCOPY*/
        if (nameOut != null) {
            System.setOut(new PrintStream(nameOut));
        }
    }

    /*+Preprocess-DONOTCOPY*/
    
    protected int getRandomInt(final int min, final int max) {
        return (min + (int)Math.floor(Math.random() * (max - min + 1)));
    }
    
    protected long getRandomLong(final long min, final long max) {
       return (min + (long)Math.floor(Math.random() * (max - min + 1)));
    }
    
    protected double getRandomDouble(final double min, final double maxExclusive) {
        return (min + Math.random() * (maxExclusive - min));
    }
    
    protected Object test_input(){
        return null;
    }
    protected void test(final Object input_data, final List<String> output_data){}
    
    private volatile boolean running = true;
    private volatile boolean waiting_test = false;
    private volatile boolean waiting_result = false;
    private volatile Object input_data = null;
    
    private Stack<String> solve_output = new Stack<>();
    

    private final String boundary = "----=_NextPart_001_005A_01D71CDC.D25E57A0\n";

    private void tester() throws IOException {
        try (
            final PipedOutputStream output1 = new PipedOutputStream();
            final InputStream testInputStream = new PipedInputStream(output1);
            final PipedOutputStream testOutputStream = new PipedOutputStream();
            final PrintStream ps = new PrintStream(testOutputStream);
            final PipedInputStream input2 = new PipedInputStream(testOutputStream);
            final PrintStream tps1 = new PrintStream(output1);
            final InputStreamReader r2 = new InputStreamReader(input2);
            final BufferedReader br2 = new BufferedReader(r2);
        ) {
            tps = tps1;
            System.setOut(ps);
            sc = new MyScanner(testInputStream);
            
            Object monitor = this;
            Object[] monitors = new Object[1];

            Thread tr1 = new Thread(new Runnable() {
                @Override
                public synchronized void run() {
                    monitors[0] = this;
                    while (running) {
                        try {
                            solve();
                            testOutputStream.write(boundary.getBytes());
                            waiting_test = true;
                            wait();
                            waiting_test = false;
                        } catch (IOException | InterruptedException ex) {
                        } catch (StackOverflowError | Exception ex1) {
                            running = false;
                            try {
                                testOutputStream.write(boundary.getBytes());
                            } catch (IOException ex) {}
                            if(input_data != null) {
                                throw new RuntimeException(input_data.toString(), ex1);
                            }
                            throw ex1;
                        }
                    }
                }
            });
            Thread tr2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    String line;
                    while (running) {
                        solve_output.clear();
                        try {
                            while ((line = br2.readLine()) != null && !boundary.trim().equals(line)) {
                                solve_output.push(line);
                            }
                            test(input_data, solve_output);
                            if (waiting_result) {
                                synchronized (monitor) {
                                    monitor.notifyAll();
                                }
                            }
                        } catch (Exception ex) {
                            if (ex.getMessage() == null || !ex.getMessage().contains("Write end dead") && !ex.getMessage().contains("Pipe broken")) {
                                running = false;
                                ex.printStackTrace(System.err);
                            }
                            if (waiting_result) {
                                synchronized (monitor) {
                                    monitor.notifyAll();
                                }
                            }
                        }
                    }
                }
            });

            tr1.start();
            tr2.start();
            
            int count = 0;

            while(running && localRunTester >= 0) {

                input_data = test_input();
                synchronized (this) {
                    waiting_result = true;
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                    }
                    waiting_result = false;
                }
                if (waiting_test) {
                    synchronized (monitors[0]) {
                        monitors[0].notifyAll();
                    }
                }
                if(running) {

                    localRunTester--;
                    if(localRunTester == 0) {
                        running = false;
                    }
                    count++;
                    System.err.println(count + " done");
                }
            }

            running = false;

            try {
                tr1.join();
                tr2.join();
            } catch (InterruptedException ex) {
            }
        }
    }
    /*-Preprocess-DONOTCOPY*/
}
