import java.io.File;import java.io.FileInputStream;import java.io.FileNotFoundException;
import java.io.IOException;import java.io.InputStream;import java.io.PrintWriter;
import java.security.AccessControlException;import java.util.Arrays;public class _p001417A {
static public void main(final String[] args) throws IOException{p001417A._main(args);}

static class p001417A extends Solver{public p001417A(){}@Override public void solve()throws 
IOException{int n=sc.nextInt();int k=sc.nextInt();sc.nextLine();int[]a=Arrays.stream(sc.nextLine().trim().split("\\s+")).mapToInt(Integer::parseInt).toArray();
IntArraySorter.sort(a);int res=Arrays.stream(a,1,n).map(v->(k-v)/a[0]).sum();pw.println(res);
}static public void _main(String[]args)throws IOException{new p001417A().run();}}
static class MyScanner{private StringBuilder cache=new StringBuilder();int cache_pos
=0;private int first_linebreak=-1;private int second_linebreak=-1;private StringBuilder 
sb=new StringBuilder();private InputStream is=null;public MyScanner(final InputStream 
is){this.is=is;}public String charToString(final int c){return String.format("'%s'",
c=='\n'?"\\n":(c=='\r'?"\\r":String.valueOf((char)c)));}public int get(){int res
=-1;if(cache_pos<cache.length()){res=cache.charAt(cache_pos);cache_pos++;if(cache_pos
==cache.length()){cache.delete(0,cache_pos);cache_pos=0;}}else{try{res=is.read();
}catch(IOException ex){throw new RuntimeException(ex);}}return res;}private void 
unget(final int c){if(cache_pos==0){cache.insert(0,(char)c);}else{cache_pos--;}}
public String nextLine(){sb.delete(0,sb.length());int c;boolean done=false;boolean 
end=false;while((c=get())!=-1){if(check_linebreak(c)){done=true;if(c==first_linebreak)
{if(!end){end=true;}else{cache.append((char)c);break;}}else if(second_linebreak!=
-1 && c==second_linebreak){break;}}if(end && c!=first_linebreak && c!=second_linebreak)
{cache.append((char)c);break;}if(!done){sb.append((char)c);}}return sb.toString();
}private boolean check_linebreak(int c){if(c=='\n'|| c=='\r'){if(first_linebreak
==-1){first_linebreak=c;}else if(c!=first_linebreak && second_linebreak==-1){second_linebreak
=c;}return true;}return false;}public int nextInt(){return Integer.parseInt(next());
}public long nextLong(){return Long.parseLong(next());}public boolean hasNext(){
boolean res=false;int c;while((c=get())!=-1){if(!check_linebreak(c)&& c!=' '&& c
!='\t'){res=true;unget(c);break;}}return res;}public String next(){sb.delete(0,sb.length());
boolean started=false;int c;while((c=get())!=-1){if(check_linebreak(c)|| c==' '|| 
c=='\t'){if(started){unget(c);break;}}else{started=true;sb.append((char)c);}}return 
sb.toString();}public int nextChar(){return get();}public boolean eof(){int c=get();
boolean res=false;if(c!=-1){unget(c);}else{res=true;}return res;}public double nextDouble()
{return Double.parseDouble(next());}}static abstract class Solver{protected String 
nameIn=null;protected String nameOut=null;protected boolean singleTest=false;protected 
MyScanner sc=null;protected PrintWriter pw=null;private int current_test=0;private 
int count_tests=0;protected int currentTest(){return current_test;}protected int 
countTests(){return count_tests;}private void process()throws IOException{if(!singleTest)
{count_tests=sc.nextInt();sc.nextLine();for(current_test=1;current_test<=count_tests;
current_test++){solve();pw.flush();}}else{count_tests=1;current_test=1;solve();}
}abstract protected void solve()throws IOException;public void run()throws IOException
{boolean done=false;try{if(nameIn!=null){if(new File(nameIn).exists()){try(FileInputStream 
fis=new FileInputStream(nameIn);PrintWriter pw0=select_output();){select_output();
done=true;sc=new MyScanner(fis);pw=pw0;process();}}else{throw new RuntimeException("File "
+new File(nameIn).getAbsolutePath()+" does not exist!");}}}catch(IOException | AccessControlException 
ex){}if(!done){try(PrintWriter pw0=select_output();){sc=new MyScanner(System.in);
pw=pw0;process();}}}private PrintWriter select_output()throws FileNotFoundException
{if(nameOut!=null){return new PrintWriter(nameOut);}return new PrintWriter(System.out);
}}static class IntArraySorter{public static void sort(int[]arr){Arrays.sort(arr);
}}}
