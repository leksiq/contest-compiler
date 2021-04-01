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
import java.io.PrintWriter;
import java.security.AccessControlException;
import java.util.Date;
import java.util.List;
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
    protected long localRunTesterTimeout = 10000;
    
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
            final PrintWriter pw0 = new PrintWriter(testOutputStream);
            final PipedInputStream input2 = new PipedInputStream(testOutputStream);
            final PrintWriter tpw = new PrintWriter(output1);
            final InputStreamReader r2 = new InputStreamReader(input2);
            final BufferedReader br2 = new BufferedReader(r2);
        ) {
            pw = pw0;
            System.setIn(testInputStream);
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
                            pw.flush();
                            testOutputStream.write(boundary.getBytes());
                            waiting_test = true;
                            wait();
                            waiting_test = false;
                        } catch (IOException | InterruptedException ex) {
                        } catch (StackOverflowError | Exception ex1) {
                            running = false;
                            pw.print(boundary);
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
                                if(input_data != null) {
                                    System.err.println(input_data.toString());
                                }
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
                tpw.println(input_data);
                tpw.flush();
                synchronized (this) {
                    waiting_result = true;
                    try {
                        long start = new Date().getTime();
                        wait(localRunTesterTimeout);
                        if(new Date().getTime() - start >= localRunTesterTimeout) {
                            System.err.println("Seems to hang: " + input_data);
                        }
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
