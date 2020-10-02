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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class Solver {
    
    protected String nameIn = null;
    protected String nameOut = null;
    protected boolean singleTest = false;

    protected boolean preprocessDebug = false;
    protected boolean doNotPreprocess = false;
    
    /*+Preprocess-DONOTCOPY*/
    private void Preprocess_DONOTCOPY() {
        if(!doNotPreprocess) {
            String running = getClass().getName();
            running = running.substring(0, running.indexOf("$"));
            Preprocessor pp = new Preprocessor();
            pp.debug(preprocessDebug);
            pp.skipPrefix("Preprocess_DONOTCOPY");
            pp.run(running);
            if(preprocessDebug) {
                System.out.println("----- " + running + " output -----");
            }
        }
    }
    /*-Preprocess-DONOTCOPY*/
    private void preProcess(final BufferedReader br, final PrintWriter pw) throws IOException {
        /*+Preprocess-DONOTCOPY*/
        Preprocess_DONOTCOPY();
        /*-Preprocess-DONOTCOPY*/
        if(!singleTest) {
            int t = Integer.valueOf(br.readLine().trim());
            while(t-- > 0) {
                process(br, pw);
            }
        } else {
            process(br, pw);
        }
    }
    
    abstract public void process(final BufferedReader br, final PrintWriter pw) throws IOException;

    protected int[] readIntArray(final BufferedReader br) throws IOException {
        return Arrays.stream(br.readLine().trim().split("\\s+")).mapToInt(v -> Integer.valueOf(v)).toArray();
    }

    protected long[] readLongArray(final BufferedReader br) throws IOException {
        return Arrays.stream(br.readLine().trim().split("\\s+")).mapToLong(v -> Long.valueOf(v)).toArray();
    }
    
    protected String readString(final BufferedReader br) throws IOException {
        return br.readLine().trim();
    }

    protected String intArrayToString(final int[] a) {
        return Arrays.stream(a).mapToObj(v -> Integer.toString(v)).collect(Collectors.joining(" "));
    }

    protected String longArrayToString(final long[] a) {
        return Arrays.stream(a).mapToObj(v -> Long.toString(v)).collect(Collectors.joining(" "));
    }

    public void run() throws IOException {
        try {
            try (
                FileReader fr = new FileReader(nameIn);
                BufferedReader br = new BufferedReader(fr);
                PrintWriter pw = select_output();
            ) {
                preProcess(br, pw);
            }
        } catch(Exception ex) {
            try (
                InputStreamReader fr = new InputStreamReader(System.in);
                BufferedReader br = new BufferedReader(fr);
                PrintWriter pw = select_output();
            ) {
                preProcess(br, pw);
            }
        }
    }

    private PrintWriter select_output() throws FileNotFoundException {
        if (nameOut != null) {
            return new PrintWriter(nameOut);
        }
        return new PrintWriter(System.out);
    }
}
