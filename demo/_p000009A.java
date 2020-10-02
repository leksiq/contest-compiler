import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
public class _p000009A {
    static public void main(final String[] args) throws java.io.IOException {
        p000009A._main(args);
    }
//begin p000009A.java
static private class p000009A{static public void _main(String[]args)throws IOException
{new Solver(){{{nameIn="demo/p000009A.in";singleTest=true;}}@Override public void
process(BufferedReader br,PrintWriter pw)throws IOException{int[]YW=readIntArray
(br);int A=7-Math.max(YW[0],YW[1]);int B=6;int nod=Utility.GreatestCommonFactor(A
,B);A/=nod;B/=nod;pw.println(A+"/"+B);}}.run();}}
//end p000009A.java
//begin net/leksi/cf/Utility.java
static private class Utility{public static void countSort(final int[]src,final int
[]dst,final int min,final int max,final int[]map){int n=max-min;int[]cnt=new int
[n];Arrays.fill(cnt,0);int[]pos=new int[n];for(int i=0;i<src.length;i++){cnt[map
[i]-min]++;}pos[0]=0;for(int i=1;i<n;i++){pos[i]=pos[i-1]+cnt[i-1];}for(int i=0;
i<src.length;i++){dst[pos[map[i]-min]++]=src[i];}}public static void countSort(final
int[]src,final int[]dst,final int min,final int max){countSort(src,dst,min,max,null
);}public static<T>void countSort(final T[]src,final T[]dst,final int min,final int
max,final int[]map){int[]idst=new int[src.length];int[]isrc=IntStream.range(0,src
.length).toArray();countSort(isrc,idst,min,max,map);for(int i=0;i<src.length;i++
){dst[i]=src[idst[i]];}}public static void suffixes(final int[]src,final int[]dst
){suffixes(src,dst,null);}public static void suffixes(final int[]src,final int[]
dst,final int[]lcp){int n=src.length+1;int[]prev=new int[2];int[]c=new int[n];int
[]c1=new int[n];int[][][]c0=new int[2][n][2];int[]sel=new int[]{0};int min=Integer
.MAX_VALUE;int max=0;for(int i=0;i<n;i++){if(i<n-1){if(src[i]-1<min){min=src[i]-
1;}if(src[i]+1>max){max=src[i]+1;}}c0[sel[0]][i][0]=c0[sel[0]][i][1]=i;c[i]=i<n-
1?src[i]:min;}countSort(c0[sel[0]],c0[sel[0]^1],min,max,c);sel[0]^=1;for(int off
=1;;off<<=1){int cc=-1;boolean cc_rep=false;for(int i=0;i<n;i++){if(i==0||prev[0
]!=c[c0[sel[0]][i][0]]||prev[1]!=c[c0[sel[0]][i][1]]){prev[0]=c[c0[sel[0]][i][0]
];prev[1]=c[c0[sel[0]][i][1]];cc++;}else{cc_rep=true;}c1[c0[sel[0]][i][0]]=cc;c0
[sel[0]][i][1]=c0[sel[0]][i][0];}for(int i=0;i<n;i++){if(cc_rep){int j=(c0[sel[0
]][i][1]-off+n)% n;c0[sel[0]][i][0]=j;}c[i]=c1[i];}if(!cc_rep){break;}countSort(c0
[sel[0]],c0[sel[0]^1],0,n,Arrays.stream(c0[sel[0]]).mapToInt(v->c[v[0]]).toArray
());sel[0]^=1;}for(int i=0;i<c1.length;i++){dst[i]=c0[sel[0]][i][0];}if(lcp !=null
){int k=0;for(int i=0;i<n-1;i++){int pi=c[i];int j=dst[pi-1];while(i+k<src.length
&&j+k<src.length&&src[i+k]==src[j+k]){k++;}lcp[pi-1]=k;k=Math.max(k-1,0);}}}public
static void z(final int[]src,final int[]dst){int n=src.length;int l=0;int r=0;Arrays
.fill(dst,0);for(int i=1;i<n;i++){if(i<=r){dst[i]=Math.min(dst[i-l],r-i+1);}while
(dst[i]+i<n&&src[dst[i]]==src[dst[i]+i]){dst[i]++;}if(i+dst[i]-1>r){l=i;r=i+dst[i
]-1;}}}public static<T>int binarySearch(final List<T>ar,final T el,final Comparator<T>
compare_fn){int m=0,n=ar.size()-1 ;while(m<=n){int k=(n+m)>>1,cmp=compare_fn.compare
(el,ar.get(k));System.out.println(m+", "+n+", "+k+", "+cmp);if(cmp>0){m=k+1;}else
if(cmp<0){n=k-1;}else{return k;}}return-m-1;}public static boolean partitionProblem
(final int[]s){return partitionProblem(s,Arrays.stream(s).sum());}public static boolean
partitionProblem(final int[]s,final int sum){int sum1=sum/2;boolean[][]mat=new boolean
[sum1+1][s.length+1];for(int i=0;i<mat[0].length;i++){mat[0][i]=true;}for(int i=
1;i<mat.length;i++){mat[i][0]=false;}for(int i=1;i<=sum1;i++){for(int j=1;j<=s.length
;j++){if(i-s[j-1]>=0){mat[i][j]=mat[i][j-1]||mat[i-s[j-1]][j-1];}else{mat[i][j]=
mat[i][j-1];}}}return mat[sum1][s.length];}public static int GreatestCommonFactor
(int a,int b){int max=Math.max(a,b);int min=Math.min(a,b);while(min>1){int r=max
% min;if(r==0){return min;}max=min;min=r;}return 1;}}
//end net/leksi/cf/Utility.java
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
