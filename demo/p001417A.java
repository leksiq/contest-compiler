import java.io.IOException;
import java.util.Arrays;
import net.leksi.contest.Solver;
import net.leksi.contest.demo.IntArraySorter;

public class p001417A extends Solver {
    
    p001417A() {
        nameIn = "demo/p001417A.in"; 
        singleTest = false;
    }
    
    int n;
    int k;
    int[] a;
    
    static public void main(String[] args) throws IOException {
        new p001417A().run();
    }

    @Override
    protected void readInputAndSolve() throws IOException {
        int[] nk = lineToIntArray();
        n = nk[0];
        k = nk[1];
        a = lineToIntArray();
        solve();
    }

    private void solve() throws IOException {
        IntArraySorter.sort(a);
        int res = Arrays.stream(a, 1, n).map(v -> (k - v) / a[0]).sum();
        pw.println(res);
    }
}
