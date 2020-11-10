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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public abstract class Solver {
    
    protected String nameIn = null;
    protected String nameOut = null;
    protected boolean singleTest = false;

    protected boolean preprocessDebug = false;
    protected boolean doNotPreprocess = false;
    
    protected Scanner sc = null;
    protected PrintWriter pw = null;
    
    /*+Preprocess-DONOTCOPY*/
    
    private void Preprocess_DONOTCOPY() {
        if(!doNotPreprocess) {
            String running = getClass().getName();
            if(running.contains("$")) {
                running = running.substring(0, running.indexOf("$"));
            }
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
    private void process() throws IOException {
        /*+Preprocess-DONOTCOPY*/
        Preprocess_DONOTCOPY();
        /*-Preprocess-DONOTCOPY*/
        if(!singleTest) {
            int t = lineToIntArray()[0];
            while(t-- > 0) {
                readInput();
                solve();
            }
        } else {
            readInput();
            solve();
        }
    }
    
    abstract protected void readInput() throws IOException;
    abstract protected void solve() throws IOException;

    protected int[] lineToIntArray() throws IOException {
        return Arrays.stream(sc.nextLine().trim().split("\\s+")).mapToInt(Integer::valueOf).toArray();
    }

    protected long[] lineToLongArray() throws IOException {
        return Arrays.stream(sc.nextLine().trim().split("\\s+")).mapToLong(Long::valueOf).toArray();
    }
    
    protected String intArrayToString(final int[] a) {
        return Arrays.stream(a).mapToObj(Integer::toString).collect(Collectors.joining(" "));
    }

    protected String longArrayToString(final long[] a) {
        return Arrays.stream(a).mapToObj(Long::toString).collect(Collectors.joining(" "));
    }

    protected List<Long> longArrayToList(final long[] a) {
        return Arrays.stream(a).mapToObj(Long::valueOf).collect(Collectors.toList());
    }

    protected List<Integer> intArrayToList(final int[] a) {
        return Arrays.stream(a).mapToObj(Integer::valueOf).collect(Collectors.toList());
    }

    protected List<Long> intArrayToLongList(final int[] a) {
        return Arrays.stream(a).mapToObj(Long::valueOf).collect(Collectors.toList());
    }

    protected void run() throws IOException {
        try {
            try (
                FileInputStream fis = new FileInputStream(nameIn);
                PrintWriter pw0 = select_output();
            ) {
                sc = new Scanner(fis);
                pw = pw0;
                process();
            }
        } catch(IOException ex) {
            try (
                PrintWriter pw0 = select_output();
            ) {
                sc = new Scanner(System.in);
                pw = pw0;
                process();
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
