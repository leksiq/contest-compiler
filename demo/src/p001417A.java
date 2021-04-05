/***********************************************/
/*!Please, Don't change or delete this comment!*/
/*             $script$:+in,k/ia[]             */
/***********************************************/
import java.io.IOException;
import java.util.Arrays;
import net.leksi.contest.Solver;
import net.leksi.contest.demo.IntArraySorter;
public class p001417A extends Solver {
    public p001417A() {
        /*+Preprocess-DONOTCOPY*/
        localNameIn = "demo/p001417A.in";
        /*-Preprocess-DONOTCOPY*/
    }
    @Override
    public void solve() throws IOException {
        int n = sc.nextInt();
        int k = sc.nextInt();
        sc.nextLine();
        int[] a = Arrays.stream(sc.nextLine().trim().split("\\s+")).mapToInt(Integer::parseInt).toArray();
        /**************************/
        /* Write your code below. */
        /*vvvvvvvvvvvvvvvvvvvvvvvv*/
        IntArraySorter.sort(a);
        int res = Arrays.stream(a, 1, n).map(v -> (k - v) / a[0]).sum();
        pw.println(res);
        /*^^^^^^^^^^^^^^^^^^^^^^^^*/

    }
    static public void main(String[] args) throws IOException {
        new p001417A().run();
    }
}
