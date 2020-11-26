/*
 *      The task: 
 *
 *      https://codeforces.com/problemset/problem/9/A
 */
import java.io.IOException;
import net.leksi.contest.demo.Utility;
import net.leksi.contest.Solver;
public class p000009A extends Solver {
    public p000009A() {
        nameIn = "demo/p000009A.in"; singleTest = true;
    }
    /*
     * Generated from "iY,W".
     */
    int Y;
    int W;
    @Override
    public void readInput() throws IOException {
        Y = sc.nextInt();
        W = sc.nextInt();
        sc.nextLine();
        /*
         * Write your code below.
         */
        int A = 7 - Math.max(Y, W);
        int B = 6;
        int nod = Utility.GreatestCommonFactor(A, B);
        A /= nod;
        B /= nod;
        pw.println(A + "/" + B);
    }
    static public void main(String[] args) throws IOException {
        new p000009A().run();
    }
}
