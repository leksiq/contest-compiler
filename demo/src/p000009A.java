/***********************************************/
/*!Please, Don't change or delete this comment!*/
/*                $script$:iY,W                */
/***********************************************/
import java.io.IOException;
import net.leksi.contest.Solver;
import static net.leksi.contest.demo.Utility.GreatestCommonFactor;
public class p000009A extends Solver {
    public p000009A() {
        singleTest = true;
        /*+Preprocess-DONOTCOPY*/
        localNameIn = "demo/p000009A.in";
        /*-Preprocess-DONOTCOPY*/
    }
    @Override
    public void solve() throws IOException {
        int Y = sc.nextInt();
        int W = sc.nextInt();
        sc.nextLine();
        /**************************/
        /* Write your code below. */
        /*vvvvvvvvvvvvvvvvvvvvvvvv*/
        int A = 7 - Math.max(Y, W);
        int B = 6;
        int nod = GreatestCommonFactor(A, B);
        A /= nod;
        B /= nod;
        pw.println(A + "/" + B);
        /*^^^^^^^^^^^^^^^^^^^^^^^^*/
    }
    static public void main(String[] args) throws IOException {
        new p000009A().run();
    }
}
