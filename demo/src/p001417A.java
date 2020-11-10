/*
 *      The task: 
 *
 *      https://codeforces.com/problemset/problem/1417/A
 */
import java.io.IOException;
import net.leksi.contest.Solver;
import java.util.Arrays;
import net.leksi.contest.demo.IntArraySorter;
public class p001417A extends Solver {
    public p001417A() {
        nameIn = "demo/p001417A.in"; singleTest = false;
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
        IntArraySorter.sort(a);
        int res = Arrays.stream(a, 1, n).map(v -> (k - v) / a[0]).sum();
        pw.println(res);
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
