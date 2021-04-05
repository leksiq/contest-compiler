/***********************************************/
/*!Please, Don't change or delete this comment!*/
/*     $script$:in,m/ts[]/tt[]/iq/(q;iy/)      */
/***********************************************/
import java.io.IOException;
import net.leksi.contest.Solver;
public class p001284A extends Solver {
    public p001284A() {
        singleTest = true;
        /*+Preprocess-DONOTCOPY*/
        localNameIn = "demo/p001284A.in";
        /*-Preprocess-DONOTCOPY*/
    }
    @Override
    public void solve() throws IOException {
        int n = sc.nextInt();
        int m = sc.nextInt();
        sc.nextLine();
        String[] s = sc.nextLine().trim().split("\\s+");
        String[] t = sc.nextLine().trim().split("\\s+");
        int q = sc.nextInt();
        sc.nextLine();
        int[] y = new int[q];
        for(int $i2 = 0; $i2 < q; $i2++) {
            y[$i2] = sc.nextInt();
            sc.nextLine();
        }
        /**************************/
        /* Write your code below. */
        /*vvvvvvvvvvvvvvvvvvvvvvvv*/
        for (int i = 0; i < q; i++) {
            String st = s[(y[i] - 1) % n] + t[(y[i] - 1) % m];
            pw.println(st);
        }
        /*^^^^^^^^^^^^^^^^^^^^^^^^*/

    }
    static public void main(String[] args) throws IOException {
        new p001284A().run();
    }
}
