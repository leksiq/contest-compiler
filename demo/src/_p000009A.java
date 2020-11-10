import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
public class _p000009A {
    static public void main(final String[] args) throws IOException {
        p000009A._main(args);
    }
//begin p000009A.java
static private class p000009A extends Solver{public p000009A(){nameIn="demo/p000009A.in"
;singleTest=true;}int Y;int W;@Override protected void solve(){int A=7-Math.max(Y
,W);int B=6;int nod=Utility.GreatestCommonFactor(A,B);A/=nod;B/=nod;pw.println(A
+"/"+B);}@Override public void readInput()throws IOException{Y=sc.nextInt();W=sc
.nextInt();sc.nextLine();}static public void _main(String[]args)throws IOException
{new p000009A().run();}}
//end p000009A.java
//begin net/leksi/contest/Solver.java
static private abstract class Solver{protected String nameIn=null;protected String
nameOut=null;protected boolean singleTest=false;protected boolean preprocessDebug
=false;protected boolean doNotPreprocess=false;protected Scanner sc=null;protected
PrintWriter pw=null;private void process()throws IOException{if(!singleTest){int
t=lineToIntArray()[0];while(t-->0){readInput();solve();}}else{readInput();solve(
);}}abstract protected void readInput()throws IOException;abstract protected void
solve()throws IOException;protected int[]lineToIntArray()throws IOException{return
Arrays.stream(sc.nextLine().trim().split("\\s+")).mapToInt(Integer::valueOf).toArray
();}protected long[]lineToLongArray()throws IOException{return Arrays.stream(sc.nextLine
().trim().split("\\s+")).mapToLong(Long::valueOf).toArray();}protected String intArrayToString
(final int[]a){return Arrays.stream(a).mapToObj(Integer::toString).collect(Collectors
.joining(" "));}protected String longArrayToString(final long[]a){return Arrays.stream
(a).mapToObj(Long::toString).collect(Collectors.joining(" "));}protected List<Long>
longArrayToList(final long[]a){return Arrays.stream(a).mapToObj(Long::valueOf).collect
(Collectors.toList());}protected List<Integer>intArrayToList(final int[]a){return
Arrays.stream(a).mapToObj(Integer::valueOf).collect(Collectors.toList());}protected
List<Long>intArrayToLongList(final int[]a){return Arrays.stream(a).mapToObj(Long
::valueOf).collect(Collectors.toList());}protected void run()throws IOException{
try{try(FileInputStream fis=new FileInputStream(nameIn);PrintWriter pw0=select_output
();){sc=new Scanner(fis);pw=pw0;process();}}catch(IOException ex){try(PrintWriter
pw0=select_output();){sc=new Scanner(System.in);pw=pw0;process();}}}private PrintWriter
select_output()throws FileNotFoundException{if(nameOut !=null){return new PrintWriter
(nameOut);}return new PrintWriter(System.out);}}
//end net/leksi/contest/Solver.java
//begin net/leksi/contest/demo/Utility.java
static private class Utility{public static int GreatestCommonFactor(int a,int b)
{int max=Math.max(a,b);int min=Math.min(a,b);while(min>1){int r=max % min;if(r==
0){return min;}max=min;min=r;}return 1;}}
//end net/leksi/contest/demo/Utility.java
}
