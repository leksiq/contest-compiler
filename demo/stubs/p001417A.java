import java.io.IOException;
import net.leksi.contest.Solver;
import java.util.Arrays;
public class p001417A extends Solver {
    public p001417A() {
        nameIn = "p001417A.in"; singleTest = false;
    }
    /*
     * Generated from "*in,k/ia[]". 
     */
    int n;
    int k;
    int[] a;
    @Override
    protected void solve() {
        /*
         * Write your code below.
         */

    }
    @Override
    public void readInput() throws IOException {
        n = sc.nextInt();
        k = sc.nextInt();
        sc.nextLine();
        a = Arrays.stream(sc.nextLine().trim().split("\\s+")).mapToInt(Integer::valueOf).toArray();
    }
    static public void main(String[] args) throws IOException {
        new p001417A().run();
    }
}
