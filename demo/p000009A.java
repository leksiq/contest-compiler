import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import net.leksi.cf.Utility;
import net.leksi.contest.Solver;

public class p000009A {
    static public void main(String[] args) throws IOException {
        new Solver() {{{ nameIn = "demo/p000009A.in"; singleTest = true;}}
            @Override
            public void process(BufferedReader br, PrintWriter pw) throws IOException {
                int[] YW = readIntArray(br);
                
                int A = 7 - Math.max(YW[0], YW[1]);
                int B = 6;
                int nod = Utility.GreatestCommonFactor(A, B);
                A /= nod;
                B /= nod;
                pw.println(A + "/" + B);
            }

        }.run();
    }
}
