import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
public class _p001343B {
    static public void main(final String[] args) throws java.io.IOException {
        p001343B._main(args);
    }
//begin p001343B.java
static private class p001343B extends Solver{int n;p001343B(){nameIn="demo/p001343B.in"
;singleTest=false;}static public void _main(String[]args)throws IOException{new p001343B
().run();}@Override protected void readInput()throws IOException{n=lineToIntArray
()[0];}@Override protected void solve()throws IOException{int[]res=new int[n];int
n2=n/2;if((n2)% 2==0){for(int i=0;i<n2;i++){res[i]=(i+1)*2;}for(int i=n2;i<n-1;i++
){res[i]=1+2*(i-n2);}res[n-1]=n2*(n2+1)-Arrays.stream(res,n2,n-1).sum();}pw.println
(res[0]==0?"NO":"YES\n"+intArrayToString(res));}}
//end p001343B.java
//begin net/leksi/contest/Solver.java
static private abstract class Solver{protected String nameIn=null;protected String
nameOut=null;protected boolean singleTest=false;protected boolean preprocessDebug
=false;protected boolean doNotPreprocess=false;protected Scanner sc=null;protected
PrintWriter pw=null;private void process()throws IOException{if(!singleTest){int
t=sc.nextInt();while(t-->0){readInput();solve();}}else{readInput();solve();}}abstract
protected void readInput()throws IOException;abstract protected void solve()throws
IOException;protected int[]lineToIntArray()throws IOException{return Arrays.stream
(sc.nextLine().trim().split("\\s+")).mapToInt(Integer::valueOf).toArray();}protected
long[]lineToLongArray()throws IOException{return Arrays.stream(sc.nextLine().trim
().split("\\s+")).mapToLong(Long::valueOf).toArray();}protected String intArrayToString
(final int[]a){return Arrays.stream(a).mapToObj(Integer::toString).collect(Collectors
.joining(" "));}protected String longArrayToString(final long[]a){return Arrays.stream
(a).mapToObj(Long::toString).collect(Collectors.joining(" "));}protected List<Long>
longArrayToList(final long[]a){return Arrays.stream(a).mapToObj(Long::valueOf).collect
(Collectors.toList());}protected List<Integer>intArrayToList(final int[]a){return
Arrays.stream(a).mapToObj(Integer::valueOf).collect(Collectors.toList());}protected
List<Long>intArrayToLongList(final int[]a){return Arrays.stream(a).mapToObj(Long
::valueOf).collect(Collectors.toList());}protected void run()throws IOException{
try{try(FileInputStream fis=new FileInputStream(nameIn);PrintWriter pw=select_output
();){sc=new Scanner(fis);process();}}catch(Exception ex){try(PrintWriter pw=select_output
();){sc=new Scanner(System.in);process();}}}private PrintWriter select_output()throws
FileNotFoundException{if(nameOut !=null){return new PrintWriter(nameOut);}return
new PrintWriter(System.out);}}
//end net/leksi/contest/Solver.java
}
