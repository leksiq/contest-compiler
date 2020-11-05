import java.io.IOException;
import net.leksi.cf.Utility;
import net.leksi.contest.Solver;

public class p000009A extends Solver {
    p000009A() {
        nameIn = "demo/p000009A.in"; 
        singleTest = true;
    }
    
    int A;
    int B;

    @Override
    protected void readInput() throws IOException {
        int[] YW = lineToIntArray();

        A = 7 - Math.max(YW[0], YW[1]);
        B = 6;
    }

    @Override
    protected void solve() throws IOException {
        int nod = Utility.GreatestCommonFactor(A, B);
        A /= nod;
        B /= nod;
        pw.println(A + "/" + B);
    }
    
    static public void main(String[] args) throws IOException {

        new p000009A().run();
    }

}
