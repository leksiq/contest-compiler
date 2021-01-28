
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.security.AccessControlException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class _p000009A {

    static public void main(final String[] args)
            throws IOException {
        p000009A._main(args);
    }

    static private class p000009A extends Solver {

        public p000009A() {
            singleTest = true;
        }

        @Override
        public void solve() throws IOException {
            int Y = sc.nextInt();
            int W = sc.nextInt();
            int A = 7 - Math.max(Y, W);
            int B = 6;
            int nod = Utility.GreatestCommonFactor(A, B);
            A /= nod;
            B
                    /= nod;
            pw.println(A + "/" + B);
        }

        static public void _main(String[] args) throws IOException {
            new p000009A().run();
        }
    }

    static private class MyScanner {

        private StringBuilder cache
                = new StringBuilder();
        int cache_pos = 0;
        private int first_linebreak = -1;
        private int second_linebreak = -1;
        private StringBuilder sb = new StringBuilder();
        private InputStream is = null;

        public MyScanner(final InputStream is) {
            this.is = is;
        }

        private String charToString(final int c) {
            return String.format("'%s'", c == '\n' ? "\\n" : (c == '\r' ? "\\r" : String.valueOf((char) c)));
        }

        public int get() {
            int res = -1;
            if (cache_pos < cache.length()) {
                res = cache.charAt(cache_pos);
                cache_pos++;
                if (cache_pos == cache.length()) {
                    cache.delete(0, cache_pos);
                    cache_pos = 0;
                }
            } else {
                try {
                    res = is.read();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            return res;
        }

        private void unget(final int c) {
            if (cache_pos == 0) {
                cache.insert(0, (char) c);
            } else {
                cache_pos--;
            }
        }

        public String nextLine() {
            sb.delete(0, sb.length());
            int c;
            boolean done = false;
            boolean end = false;
            while ((c = get()) != -1) {
                if (check_linebreak(c)) {
                    done = true;
                    if (c == first_linebreak) {
                        if (!end) {
                            end = true;
                        } else {
                            cache.append((char) c);
                            break;
                        }
                    } else {
                        if (second_linebreak != -1 && c == second_linebreak) {
                            break;
                        }
                    }
                }
                if (end && c != first_linebreak
                        && c != second_linebreak) {
                    cache.append((char) c);
                    break;
                }
                if (!done) {
                    sb.append((char) c);
                }
            }
            return sb.toString();
        }

        private boolean check_linebreak(int c) {
            if (c == '\n' || c == '\r') {
                if (first_linebreak == -1) {
                    first_linebreak = c;
                } else if (c != first_linebreak && second_linebreak
                        == -1) {
                    second_linebreak = c;
                }
                return true;
            }
            return false;
        }

        public int nextInt() {
            return Integer.parseInt(next());
        }

        public long nextLong() {
            return Long.parseLong(next());
        }

        public boolean hasNext() {
            boolean res = false;
            int c;
            while ((c = get()) != -1) {
                if (!check_linebreak(c)
                        && c != ' ' && c != '\t') {
                    res = true;
                    unget(c);
                    break;
                }
            }
            return res;
        }

        public String next() {
            sb.delete(0, sb.length());
            boolean started = false;
            int c;
            while ((c = get()) != -1) {
                if (check_linebreak(c)
                        || c == ' ' || c == '\t') {
                    if (started) {
                        unget(c);
                        break;
                    }
                } else {
                    started = true;
                    sb.append((char) c);
                }
            }
            return sb.toString();
        }

        public int nextChar() {
            return get();
        }

        public boolean eof() {
            int c = get();
            boolean res = false;
            if (c != -1) {
                unget(c);
            } else {
                res = true;
            }
            return res;
        }

        public double nextDouble() {
            return Double.parseDouble(next());
        }
    }

    static private class Pair<K, V> {

        private K k;
        private V v;

        public Pair(final K t, final V u) {
            this.k = t;
            this.v = u;
        }

        public K getKey() {
            return k;
        }

        public V getValue() {
            return v;
        }
    }

    static private abstract class Solver {

        protected String nameIn = null;
        protected String nameOut = null;
        protected boolean singleTest = false;
        protected boolean preprocessDebug = false;
        protected boolean doNotPreprocess
                = false;
        protected PrintStream debugPrintStream = null;
        protected MyScanner sc = null;
        protected PrintWriter pw = null;
        final static String SPACE = " ";
        final static String SPACES = "\\s+";
        private int current_test = 0;
        private int count_tests = 0;

        protected int currentTest() {
            return current_test;
        }

        protected int countTests() {
            return count_tests;
        }

        private void
                process() throws IOException {
            if (!singleTest) {
                count_tests = lineToIntArray()[0];
                for (current_test
                        = 1; current_test <= count_tests; current_test++) {
                    solve();
                    pw.flush();
                }
            } else {
                count_tests
                        = 1;
                current_test = 1;
                solve();
                pw.flush();
            }
        }

        abstract protected void solve() throws IOException;

        protected String[] lineToArray() throws IOException {
            return sc.nextLine().trim().split(SPACES);
        }

        protected int[] lineToCharArray() throws IOException {
            return sc.nextLine().chars().toArray();
        }

        protected int[] lineToIntArray() throws IOException {
            return Arrays.stream(lineToArray()).mapToInt(Integer::parseInt).toArray();
        }

        protected long[] lineToLongArray() throws IOException {
            return Arrays.stream(lineToArray()).mapToLong(Long::parseLong).toArray();
        }

        protected double[] lineToDoubleArray() throws IOException {
            return Arrays.stream(lineToArray()).mapToDouble(Double::parseDouble).toArray();
        }

        protected void run() throws IOException {
            boolean done = false;
            try {
                if (nameIn != null
                        && new File(nameIn).exists()) {
                    try (FileInputStream fis = new FileInputStream(nameIn); PrintWriter pw0 = select_output();) {
                        done = true;
                        sc = new MyScanner(fis);
                        pw = pw0;
                        process();
                    }
                }
            } catch (IOException ex) {
            } catch (AccessControlException ex) {
            }
            if (!done) {
                try (PrintWriter pw0 = select_output();) {
                    sc = new MyScanner(System.in);
                    pw = pw0;
                    process();
                }
            }
        }

        private PrintWriter select_output()
                throws FileNotFoundException {
            if (nameOut != null) {
                return new PrintWriter(nameOut);
            }
            return new PrintWriter(System.out);
        }

        public static Map<Integer, Integer> mapc(final int[] a) {
            return IntStream.range(0, a.length).collect(() -> new TreeMap<Integer, Integer>(),
                    (res, i) -> {
                        res.put(a[i], res.getOrDefault(a[i], 0) + 1);
                    }, Map::putAll);
        }

        public static Map<Long, Integer> mapc(final long[] a) {
            return IntStream.range(0, a.length).collect(
                    () -> new TreeMap<Long, Integer>(), (res, i) -> {
                        res.put(a[i], res.getOrDefault(a[i], 0)
                                + 1);
                    }, Map::putAll);
        }

        public static <T> Map<T, Integer> mapc(final T[] a, Comparator<T> cmp) {
            return IntStream.range(0, a.length).collect(cmp != null ? () -> new TreeMap<T, Integer>(cmp)
                    : () -> new TreeMap<T, Integer>(), (res, i) -> {
                        res.put(a[i], res.getOrDefault(a[i], 0) + 1);
                    }, Map::putAll);
        }

        public static <T> Map<T, Integer> mapc(final T[] a) {
            return mapc(a, null);
        }

        public static Map<Integer, Integer> mapc(final IntStream a) {
            return a.collect(()
                    -> new TreeMap<Integer, Integer>(), (res, v) -> {
                res.put(v, res.getOrDefault(v, 0) + 1);
            }, Map::putAll
            );
        }

        public static Map<Long, Integer> mapc(final LongStream a) {
            return a.collect(()
                    -> new TreeMap<Long, Integer>(), (res, v) -> {
                res.put(v, res.getOrDefault(v, 0) + 1);
            }, Map::putAll
            );
        }

        public static <T> Map<T, Integer> mapc(final Stream<T> a, Comparator<T> cmp) {
            return a.collect(cmp != null ? () -> new TreeMap<T, Integer>(cmp) : () -> new TreeMap<T, Integer>(),
                    (res, v) -> {
                        res.put(v, res.getOrDefault(v, 0) + 1);
                    }, Map::putAll);
        }

        public static <T> Map<T, Integer> mapc(final Stream<T> a) {
            return mapc(a, null);
        }

        public static <T> Map<T, Integer>
                mapc(final Collection<T> a, Comparator<T> cmp) {
            return mapc(a.stream(), cmp);
        }

        public static <T> Map<T, Integer> mapc(final Collection<T> a) {
            return mapc(a.stream());
        }

        public static Map<Integer, List<Integer>> mapi(final int[] a) {
            return IntStream.range(0, a.length).collect(
                    () -> new TreeMap<Integer, List<Integer>>(), (res, i) -> {
                        if (!res.containsKey(a[i])) {
                            res.put(a[i],
                                    Stream.of(i).collect(Collectors.toList()));
                        } else {
                            res.get(a[i]).add(i);
                        }
                    }, Map::putAll
            );
        }

        public static Map<Long, List<Integer>> mapi(final long[] a) {
            return IntStream.range(0,
                    a.length).collect(() -> new TreeMap<Long, List<Integer>>(), (res, i) -> {
                        if (!res.containsKey(a[i])) {
                            res.put(a[i], Stream.of(i).collect(Collectors.toList()));
                        } else {
                            res.get(a[i]).add(i);
                        }
                    }, Map::putAll);
        }

        public static <T> Map<T, List<Integer>> mapi(final T[] a, Comparator<T> cmp) {
            return IntStream.range(0, a.length).collect(cmp != null ? () -> new TreeMap<T, List<Integer>>(cmp)
                    : () -> new TreeMap<T, List<Integer>>(), (res, i) -> {
                        if (!res.containsKey(a[i])) {
                            res.put(a[i],
                                    Stream.of(i).collect(Collectors.toList()));
                        } else {
                            res.get(a[i]).add(i);
                        }
                    }, Map::putAll
            );
        }

        public static <T> Map<T, List<Integer>> mapi(final T[] a) {
            return mapi(a, null);
        }

        public static Map<Integer, List<Integer>> mapi(final IntStream a) {
            int[] i = new int[]{0};
            return a.collect(() -> new TreeMap<Integer, List<Integer>>(), (res, v) -> {
                if (!res.containsKey(v)) {
                    res.put(v, Stream.of(i[0]).collect(Collectors.toList()));
                } else {
                    res.get(v).add(i[0]);
                }
                i[0]++;
            }, Map::putAll);
        }

        public static Map<Long, List<Integer>> mapi(final LongStream a) {
            int[] i = new int[]{0};
            return a.collect(() -> new TreeMap<Long, List<Integer>>(), (res,
                    v) -> {
                if (!res.containsKey(v)) {
                    res.put(v, Stream.of(i[0]).collect(Collectors.toList()));
                } else {
                    res.get(v).add(i[0]);
                }
                i[0]++;
            }, Map::putAll);
        }

        public static <T> Map<T, List<Integer>>
                mapi(final Stream<T> a, Comparator<T> cmp) {
            int[] i = new int[]{0};
            return a.collect(cmp
                    != null ? () -> new TreeMap<T, List<Integer>>(cmp) : () -> new TreeMap<T, List<Integer>>(),
                    (res, v) -> {
                        if (!res.containsKey(v)) {
                            res.put(v, Stream.of(i[0]).collect(Collectors.toList()));
                        } else {
                            res.get(v).add(i[0]);
                        }
                        i[0]++;
                    }, Map::putAll);
        }

        public static <T> Map<T, List<Integer>>
                mapi(final Stream<T> a) {
            return mapi(a, null);
        }

        public static <T> Map<T, List<Integer>>
                mapi(final Collection<T> a, Comparator<T> cmp) {
            return mapi(a.stream(), cmp);
        }

        public static <T> Map<T, List<Integer>> mapi(final Collection<T> a) {
            return mapi(a.stream());
        }

        public static List<int[]> listi(final int[] a) {
            return IntStream.range(0, a.length).mapToObj(i
                    -> new int[]{a[i], i}).collect(Collectors.toList());
        }

        public static List<long[]> listi(final long[] a) {
            return IntStream.range(0, a.length).mapToObj(i -> new long[]{a[i], i}).collect(Collectors.toList());
        }

        public static <T> List<Pair<T, Integer>> listi(final T[] a) {
            return IntStream.range(0,
                    a.length).mapToObj(i -> new Pair<T, Integer>(a[i], i)).collect(Collectors.toList());
        }

        public static List<int[]> listi(final IntStream a) {
            int[] i = new int[]{0};
            return a.mapToObj(v
                    -> new int[]{v, i[0]++}).collect(Collectors.toList());
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

        public static <T> String join(final T[] a, final Function<T, String> toString) {
            return Arrays.stream(a).map(v -> toString.apply(v)).collect(Collectors.joining(SPACE));
        }

        public static <T> String join(final Collection<T> a) {
            return a.stream().map(v -> Objects.toString(v)).collect(Collectors.joining(SPACE));
        }

        public static <T> String join(final Collection<T> a, final Function<T, String> toString) {
            return a.stream().map(v -> toString.apply(v)).collect(Collectors.joining(SPACE));
        }

        public static <T> String join(final Stream<T> a) {
            return a.map(v -> Objects.toString(v)).collect(Collectors.joining(SPACE));
        }

        public static <T> String join(final Stream<T> a, final Function<T, String> toString) {
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

    static private class Utility {

        public static int GreatestCommonFactor(int a, int b) {
            int max = Math.max(a, b);
            int min = Math.min(a, b);
            while (min > 1) {
                int r = max % min;
                if (r == 0) {
                    return min;
                }
                max = min;
                min
                        = r;
            }
            return 1;
        }
    }
}
