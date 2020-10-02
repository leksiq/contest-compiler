import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import net.leksi.contest.Solver;

public class p001343B {
    static public void main(String[] args) throws IOException {
        new Solver() {{{ nameIn = "demo/p001343B.in"; singleTest = false;}}
            @Override
            public void process(BufferedReader br, PrintWriter pw) throws IOException {
                int n = readIntArray(br)[0];
                int[] res = new int[n];
                int n2 = n / 2;
                if((n2) % 2 == 0) {
                    for(int i = 0; i < n2; i++) {
                        res[i] = (i + 1) * 2;
                    }
                    for(int i = n2; i < n - 1; i++) {
                        res[i] = 1 + 2 * (i - n2);
                    }
                    res[n - 1] = n2 * (n2 + 1) - Arrays.stream(res, n2, n - 1).sum();
                }
                pw.println(res[0] == 0 ? "NO" : "YES\n" + intArrayToString(res));
            }

        }.run();
    }
}
