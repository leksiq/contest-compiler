import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import net.leksi.contest.Solver;
import net.leksi.contest.demo.IntArraySorter;

public class p001417A {
    static public void main(String[] args) throws IOException {
        new Solver() {{{ nameIn = "demo/p001417A.in"; singleTest = false;}}
            @Override
            public void process(BufferedReader br, PrintWriter pw) throws IOException {
                int[] nk = readIntArray(br);
                int[] a = readIntArray(br);
                solve(nk[0], nk[1], a, pw);
            }

            private void solve(int n, int k, int[] a, PrintWriter pw) {
                IntArraySorter.sort(a);
                int res = Arrays.stream(a, 1, n).map(v -> (k - v) / a[0]).sum();
                pw.println(res);
            }

        }.run();
    }
}
