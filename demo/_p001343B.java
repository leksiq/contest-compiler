import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.stream.Collectors;
public class _p001343B {
    static public void main(final String[] args) throws java.io.IOException {
        p001343B._main(args);
    }
//begin p001343B.java
static private class p001343B{static public void _main(String[]args)throws IOException
{new Solver(){{{nameIn="demo/p001343B.in";singleTest=false;}}@Override public void
process(BufferedReader br,PrintWriter pw)throws IOException{int n=readIntArray(br
)[0];int[]res=new int[n];int n2=n/2;if((n2)% 2==0){for(int i=0;i<n2;i++){res[i]=
(i+1)*2;}for(int i=n2;i<n-1;i++){res[i]=1+2*(i-n2);}res[n-1]=n2*(n2+1)-Arrays.stream
(res,n2,n-1).sum();}pw.println(res[0]==0?"NO":"YES\n"+intArrayToString(res));}}.run
();}}
//end p001343B.java
//begin net/leksi/contest/Solver.java
static private abstract class Solver{protected String nameIn=null;protected String
nameOut=null;protected boolean singleTest=false;protected boolean preprocessDebug
=false;protected boolean doNotPreprocess=false;private void preProcess(final BufferedReader
br,final PrintWriter pw)throws IOException{if(!singleTest){int t=Integer.valueOf
(br.readLine().trim());while(t-->0){process(br,pw);}}else{process(br,pw);}}abstract
public void process(final BufferedReader br,final PrintWriter pw)throws IOException
;protected int[]readIntArray(final BufferedReader br)throws IOException{return Arrays
.stream(br.readLine().trim().split("\\s+")).mapToInt(v->Integer.valueOf(v)).toArray
();}protected long[]readLongArray(final BufferedReader br)throws IOException{return
Arrays.stream(br.readLine().trim().split("\\s+")).mapToLong(v->Long.valueOf(v)).toArray
();}protected String readString(final BufferedReader br)throws IOException{return
br.readLine().trim();}protected String intArrayToString(final int[]a){return Arrays
.stream(a).mapToObj(v->Integer.toString(v)).collect(Collectors.joining(" "));}protected
String longArrayToString(final long[]a){return Arrays.stream(a).mapToObj(v->Long
.toString(v)).collect(Collectors.joining(" "));}public void run()throws IOException
{try{try(FileReader fr=new FileReader(nameIn);BufferedReader br=new BufferedReader
(fr);PrintWriter pw=select_output();){preProcess(br,pw);}}catch(Exception ex){try
(InputStreamReader fr=new InputStreamReader(System.in);BufferedReader br=new BufferedReader
(fr);PrintWriter pw=select_output();){preProcess(br,pw);}}}private PrintWriter select_output
()throws FileNotFoundException{if(nameOut !=null){return new PrintWriter(nameOut
);}return new PrintWriter(System.out);}}
//end net/leksi/contest/Solver.java
}
