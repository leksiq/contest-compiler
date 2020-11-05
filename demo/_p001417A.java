import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
public class _p001417A {
    static public void main(final String[] args) throws java.io.IOException {
        p001417A._main(args);
    }
//begin p001417A.java
static private class p001417A extends Solver{p001417A(){nameIn="demo/p001417A.in"
;singleTest=false;}int n;int k;int[]a;static public void _main(String[]args)throws
IOException{new p001417A().run();}@Override protected void readInput()throws IOException
{int[]nk=lineToIntArray();n=nk[0];k=nk[1];a=lineToIntArray();}@Override protected
void solve()throws IOException{IntArraySorter.sort(a);int res=Arrays.stream(a,1,
n).map(v->(k-v)/a[0]).sum();pw.println(res);}}
//end p001417A.java
//begin net/leksi/contest/Solver.java
static private abstract class Solver{protected String nameIn=null;protected String
nameOut=null;protected boolean singleTest=false;protected boolean preprocessDebug
=false;protected boolean doNotPreprocess=false;protected Scanner sc=null;protected
PrintWriter pw=null;private void process()throws IOException{if(!singleTest){int
t=lineToIntArray()[0];while(t-->0){readInput();solve();}}else{readInput();solve(
);}}abstract protected void readInput()throws IOException;abstract protected void
solve()throws IOException;protected int[]lineToIntArray()throws IOException{String
s=sc.nextLine().trim();System.out.println(s);return Arrays.stream(s.split("\\s+"
)).mapToInt(Integer::valueOf).toArray();}protected long[]lineToLongArray()throws
IOException{return Arrays.stream(sc.nextLine().trim().split("\\s+")).mapToLong(Long
::valueOf).toArray();}protected String intArrayToString(final int[]a){return Arrays
.stream(a).mapToObj(Integer::toString).collect(Collectors.joining(" "));}protected
String longArrayToString(final long[]a){return Arrays.stream(a).mapToObj(Long::toString
).collect(Collectors.joining(" "));}protected List<Long>longArrayToList(final long
[]a){return Arrays.stream(a).mapToObj(Long::valueOf).collect(Collectors.toList()
);}protected List<Integer>intArrayToList(final int[]a){return Arrays.stream(a).mapToObj
(Integer::valueOf).collect(Collectors.toList());}protected List<Long>intArrayToLongList
(final int[]a){return Arrays.stream(a).mapToObj(Long::valueOf).collect(Collectors
.toList());}protected void run()throws IOException{try{try(FileInputStream fis=new
FileInputStream(nameIn);PrintWriter pw0=select_output();){sc=new Scanner(fis);pw
=pw0;process();}}catch(IOException ex){try(PrintWriter pw0=select_output();){sc=
new Scanner(System.in);pw=pw0;process();}}}private PrintWriter select_output()throws
FileNotFoundException{if(nameOut !=null){return new PrintWriter(nameOut);}return
new PrintWriter(System.out);}}
//end net/leksi/contest/Solver.java
//begin net/leksi/contest/demo/IntArraySorter.java
static private class IntArraySorter{public static void sort(int[]arr){Arrays.sort
(arr);}}
//end net/leksi/contest/demo/IntArraySorter.java
}
