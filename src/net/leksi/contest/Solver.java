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
import java.io.PrintStream;
import java.io.PrintWriter;
import java.security.AccessControlException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public abstract class Solver {
    
    protected String nameIn = null;
    protected String nameOut = null;
    protected boolean singleTest = false;

    protected boolean preprocessDebug = false;
    protected boolean doNotPreprocess = false;
    protected PrintStream debugPrintStream = null;
    
    protected Scanner sc = null;
    protected PrintWriter pw = null;
    
    final static String SPACE = " ";
    final static String SPACES = "\\s+";
    
    /*+Preprocess-DONOTCOPY*/
    
    protected boolean localMultiTest = false;
    protected String localNameIn = "";
    
    private void Preprocess_DONOTCOPY() {
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
        }
    }
    
    /*-Preprocess-DONOTCOPY*/
    private void process() throws IOException {
        if(!singleTest) {
            int t = lineToIntArray()[0];
            while(t-- > 0) {
                /*+Preprocess-DONOTCOPY*/
                if(localMultiTest) {
                    pw.println("--- test ---");
                }
                /*-Preprocess-DONOTCOPY*/
                solve();
                pw.flush();
            }
        } else {
            solve();
            pw.flush();
        }
    }
    
    abstract protected void solve() throws IOException;

    protected String[] lineToArray() throws IOException {
        return sc.nextLine().trim().split(SPACES);
    }

    protected int[] lineToIntArray() throws IOException {
        return Arrays.stream(lineToArray()).mapToInt(Integer::valueOf).toArray();
    }

    protected long[] lineToLongArray() throws IOException {
        return Arrays.stream(lineToArray()).mapToLong(Long::valueOf).toArray();
    }
    
    protected void run() throws IOException {
        /*+Preprocess-DONOTCOPY*/
        Preprocess_DONOTCOPY();
        /*-Preprocess-DONOTCOPY*/
        boolean done = false;
        try {
            if(nameIn != null && new File(nameIn).exists()) {
                    try (
                        FileInputStream fis = new FileInputStream(nameIn);
                        PrintWriter pw0 = select_output();
                    ) {
                        done = true;
                        sc = new Scanner(fis);
                        pw = pw0;
                        process();
                    }
            }
        } catch(IOException ex) {
        } catch(AccessControlException ex) {
        }
        if(!done) {
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
    public static Map<Integer, List<Integer>> mapi(final int[] a) {
        return IntStream.range(0, a.length).collect(
            () -> new TreeMap<Integer, List<Integer>>(), 
            (res, i) -> {
                if(!res.containsKey(a[i])) {
                    res.put(a[i], Stream.of(i).collect(Collectors.toList()));
                } else {
                    res.get(a[i]).add(i);
                }
            }, 
            Map::putAll
        );
    }
    public static Map<Long, List<Integer>> mapi(final long[] a) {
        return IntStream.range(0, a.length).collect(
            () -> new TreeMap<Long, List<Integer>>(), 
            (res, i) -> {
                if(!res.containsKey(a[i])) {
                    res.put(a[i], Stream.of(i).collect(Collectors.toList()));
                } else {
                    res.get(a[i]).add(i);
                }
            }, 
            Map::putAll
        );
    }
    public static <T> Map<T, List<Integer>> mapi(final T[] a) {
        return IntStream.range(0, a.length).collect(
            () -> new TreeMap<T, List<Integer>>(), 
            (res, i) -> {
                if(!res.containsKey(a[i])) {
                    res.put(a[i], Stream.of(i).collect(Collectors.toList()));
                } else {
                    res.get(a[i]).add(i);
                }
            }, 
            Map::putAll
        );
    }
    public static <T> Map<T, List<Integer>> mapi(final T[] a, Comparator<T> cmp) {
        return IntStream.range(0, a.length).collect(
            () -> new TreeMap<T, List<Integer>>(cmp), 
            (res, i) -> {
                if(!res.containsKey(a[i])) {
                    res.put(a[i], Stream.of(i).collect(Collectors.toList()));
                } else {
                    res.get(a[i]).add(i);
                }
            }, 
            Map::putAll
        );
    }
    public static Map<Integer, List<Integer>> mapi(final IntStream a) {
        int[] i = new int[]{0};
        return a.collect(
            () -> new TreeMap<Integer, List<Integer>>(), 
            (res, v) -> {
                if(!res.containsKey(v)) {
                    res.put(v, Stream.of(i[0]).collect(Collectors.toList()));
                } else {
                    res.get(v).add(i[0]);
                }
                i[0]++;
            }, 
            Map::putAll
        );
    }
    public static Map<Long, List<Integer>> mapi(final LongStream a) {
        int[] i = new int[]{0};
        return a.collect(
            () -> new TreeMap<Long, List<Integer>>(), 
            (res, v) -> {
                if(!res.containsKey(v)) {
                    res.put(v, Stream.of(i[0]).collect(Collectors.toList()));
                } else {
                    res.get(v).add(i[0]);
                }
                i[0]++;
            }, 
            Map::putAll
        );
    }
    public static <T> Map<T, List<Integer>> mapi(final Stream<T> a, Comparator<T> cmp) {
        int[] i = new int[]{0};
        return a.collect(
            () -> new TreeMap<T, List<Integer>>(cmp), 
            (res, v) -> {
                if(!res.containsKey(v)) {
                    res.put(v, Stream.of(i[0]).collect(Collectors.toList()));
                } else {
                    res.get(v).add(i[0]);
                }
            }, 
            Map::putAll
        );
    }
    public static <T> Map<T, List<Integer>> mapi(final Stream<T> a) {
        int[] i = new int[]{0};
        return a.collect(
            () -> new TreeMap<T, List<Integer>>(), 
            (res, v) -> {
                if(!res.containsKey(v)) {
                    res.put(v, Stream.of(i[0]).collect(Collectors.toList()));
                } else {
                    res.get(v).add(i[0]);
                }
            }, 
            Map::putAll
        );
    }
    public static List<int[]> listi(final int[] a) {
        return IntStream.range(0, a.length).mapToObj(i -> new int[]{a[i], i}).collect(Collectors.toList());
    }
    public static List<long[]> listi(final long[] a) {
        return IntStream.range(0, a.length).mapToObj(i -> new long[]{a[i], i}).collect(Collectors.toList());
    }
    public static <T> List<Pair<T, Integer>> listi(final T[] a) {
        return IntStream.range(0, a.length).mapToObj(i -> new Pair<T, Integer>(a[i], i)).collect(Collectors.toList());
    }
    public static List<int[]> listi(final IntStream a) {
        int[] i = new int[]{0};
        return a.mapToObj(v -> new int[]{v, i[0]++}).collect(Collectors.toList());
    }
    public static List<long[]> listi(final LongStream a) {
        int[] i = new int[]{0};
        return a.mapToObj(v -> new long[]{v, i[0]++}).collect(Collectors.toList());
    }
    public static <T> List<Pair<T, Integer>> listi(final Stream<T> a) {
        int[] i = new int[]{0};
        return a.map(v -> new Pair<T, Integer>(v, i[0]++)).collect(Collectors.toList());
    }
    public static <T> List<Pair<T, Integer>> listi(final Collection<T> a) {
        int[] i = new int[]{0};
        return a.stream().map(v -> new Pair<T, Integer>(v, i[0]++)).collect(Collectors.toList());
    }
    public static String join(final int[] a) {
        return Arrays.stream(a).mapToObj(Integer::toString).collect(Collectors.joining(SPACE));
    }
    
    public static String join(final long[] a) {
        return Arrays.stream(a).mapToObj(Long::toString).collect(Collectors.joining(SPACE));
    }
    
    public static <T> String join(final T[] a) {
        return Arrays.stream(a).map(v -> Objects.toString(v)).collect(Collectors.joining(SPACE));
    }

    public static <T> String join(final T[] a, final Function<T,String> toString) {
        return Arrays.stream(a).map(v -> toString.apply(v)).collect(Collectors.joining(SPACE));
    }

    public static <T> String join(final Collection<T> a) {
        return a.stream().map(v -> Objects.toString(v)).collect(Collectors.joining(SPACE));
    }

    public static <T> String join(final Collection<T> a, final Function<T,String> toString) {
        return a.stream().map(v -> toString.apply(v)).collect(Collectors.joining(SPACE));
    }

    public static <T> String join(final Stream<T> a) {
        return a.map(v -> Objects.toString(v)).collect(Collectors.joining(SPACE));
    }

    public static <T> String join(final Stream<T> a, final Function<T,String> toString) {
        return a.map(v -> toString.apply(v)).collect(Collectors.joining(SPACE));
    }

    public static <T> String join(final IntStream a) {
        return a.mapToObj(Integer::toString).collect(Collectors.joining(SPACE));
    }

    public static <T> String join(final LongStream a) {
        return a.mapToObj(Long::toString).collect(Collectors.joining(SPACE));
    }

    public static List<Integer> list(final int[] a) {
        return Arrays.stream(a).mapToObj(Integer::valueOf).collect(Collectors.toList());
    }

    public static List<Integer> list(final IntStream a) {
        return a.mapToObj(Integer::valueOf).collect(Collectors.toList());
    }

    public static List<Long> list(final long[] a) {
        return Arrays.stream(a).mapToObj(Long::valueOf).collect(Collectors.toList());
    }

    public static List<Long> list(final LongStream a) {
        return a.mapToObj(Long::valueOf).collect(Collectors.toList());
    }

    public static <T> List<T> list(final Stream<T> a) {
        return a.collect(Collectors.toList());
    }

    public static <T> List<T> list(final Collection<T> a) {
        return a.stream().collect(Collectors.toList());
    }

    public static <T> List<T> list(final T[] a) {
        return Arrays.stream(a).collect(Collectors.toList());
    }
    
    public static String yesNo(final boolean res) {
        return res ? "YES" : "NO";
    }

}
