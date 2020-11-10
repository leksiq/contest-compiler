import java.io.IOException;
import net.leksi.contest.Solver;
public class p001284A extends Solver {
    public p001284A() {
        nameIn = "p001284A.in"; singleTest = true;
    }
    /*
     * Generated from "in,m/ss[]/st[]/iq/(q;iy/)".
     */
    int n;
    int m;
    String[] s;
    String[] t;
    int q;
    int[] y;
    @Override
    protected void solve() {
        /*
         * Write your code below.
         */

    }
    @Override
    public void readInput() throws IOException {
        n = sc.nextInt();
        m = sc.nextInt();
        sc.nextLine();
        s = sc.nextLine().trim().split("\\s+");
        t = sc.nextLine().trim().split("\\s+");
        q = sc.nextInt();
        sc.nextLine();
        y = new int[q];
        for(int _iy = 0; _iy < q; _iy++) {
            y[_iy] = sc.nextInt();
            sc.nextLine();
        }
    }
    static public void main(String[] args) throws IOException {
        new p001284A().run();
    }
}
