# java-contest-assistant
The library is intended to simplify some routines when using Java for "Online Judges" contests.
+ Quick coding input data structure and read it;
+ Using one's favorite algorithms without copying sources into your submission manually.
+ Testing locally on a set of tests if there is a single-test judging.
## Preface
This library would be useful for one who likes to solve programming problems, take part in online programming contests and use Java for these purposes. First, one needs everytime coding the same routines to read input data. Second, If one have one's favorite homemade classes implementing various algorithms one has to copy-paste them into the source file to submit into "Online Judges" system as it acceps single file. Thus, this library helps to concentrate on the problem itself.
## How It Works
The work falls into three steps.
1. Run **Wizard** with class name and one-line script describing input rule to get a stub with input data structure supplied.
2. Write code as a body of `protected void solve() throws IOException` method. Import any custom libraries developed earlier (given that their sources can be found through classpath).
3. Compile and run developed class with test samples. Let's call it **preprocessing**. The resulting source to submit to judge system will be found at the sources directory with underscore before name. **NOTE:** Java Development Kit is needed to be installed!

See **Examples** below.

## 1. Running the Wizard
Running class `net.leksi.contest.Wizard` one gets a *stub* with all input data structure is coded and ready to be read without (almost) any actions. An empty test input file is created as well. The usage of the `net.leksi.contest.Wizard`:

    Usage: java java_options -jar net.leksi.contest.compiler.jar wizard_options class-name script
    java_options:           java options like -classpath;
    wizard_options:
        -stdout                 - write to stdout (default creates file <class-name>.java);
        -src <directory>        - the directory to generate source into (default .);
        -in <directory>         - the directory to generate input file into (default .);
        -infile <name>          - generate input file <name> (default <class-name>.in);
        -package package        - the package of class to generate (default empty);
        -force                  - overwrite existing files (default throws exception for source file) and leaves input file;
        -version                - shows current version and checks if it is latest, then returns;
        -usage, -help, ?        - shows this info, then returns;
    class-name:             name of class to generate;
    script:                 input script;


    
The "input script" means the script on lightweight special language. This script follows the task input description.
### The Script Language Grammar

    script                      ::=     ('+' | '?')? input-of-cycle
    input-of-cycle              ::=     cycle* (variables-group cycle+)* variables-group?
    cycle                       ::=     data-cycle | loop-cycle
    input-of-data-cycle         ::=     data-cycle* (variables-group data-cycle+)* variables-group?
    data-cycle                  ::=     '(' <proper-java-expression>) ';' input-of-data-cycle ')'
    loop-cycle                  ::=     '{' (<proper-java-expression> | '+') ';' input-of-cycle '}'
    variables-group             ::=     new-line* same-type-variables-group (';' same-type-variables-group | new-line)*
    variable-name               ::=     <Java's  legal identifier>
    same-type-variables-group   ::=     type variable-definition (',' variable-definition)*
    new-line                    ::=     '/'
    type                        ::=     'c' | 'i' | 'l' | 'd' | 's' | 't'
    variable-definition         ::=     variable-name ('[' <proper-java-expression>? ']')?
    
`'+'` at the beginning of script means that there are multiple test at each submission run. Futher one codes as if there is only test at submission run.

`'?'` at the beginning of script means that there is a single test at one submission run, but multiple ones at a local testing. The production source will be generated for single test.

`type` means type of variable, `'c'`, `'i'`, `'l'`, `'d'`, `'s'`, `'t'` stand for `char` (casted to `int`), `int`, `long`, `double`, `java.lang.String` and `Token` (`java.lang.String` before next space or end of line) respectively.
if the variable is an array its definition should end with `'['`, optional `length` and `']'`, if the `length` is present the variable will take exactly `length` elements from iunput, otherwise the variable will take all elements till the end of line. So, one should not use `length` in the case the array implied to take all the line.

`data-cycle` means that a responsible data structure is created as array of objects.

`loop-cycle` means that a loop with local variables is created. `'+'` means that the cycle repeats until end of input.

`java.lang.String` variable takes all chars till the end of line, `java.lang.String` *array* variable takes substrings split with spaces at the rule described before. 

`Token` variable takes all chars till the next space or  till the end of line if no spaces, `Token` *array* variable takes the same way as `java.lang.String` *array*.

Space chars may present between any terms of a script.

### The Script Language Examples
#### Example 1
##### Task input:
...
The first line of the input contains one integer t (1≤t≤10^4) — the number of test cases. Then t
test cases follow.

The only line of the test case contains one integer n
(2≤n≤2⋅10^5) — the length of the array.
...
##### Wizard run:
`java ... -jar net.leksi.contest.assistant.jar A "+in"`

#### Example 2
##### Task input:
...
The first line contains one integer T (1≤T≤500) — the number of test cases.

Each test case consists of two lines:
    the first line contains two integers n
and k (2≤n≤1000, 2≤k≤10^4);
the second line contains n
integers a1, a2, ..., an (1≤ai≤k). 
...
##### Wizard run:
`java ... -jar net.leksi.contest.assistant.jar A "+in,k/ia[n]"`
or preferable
`java ... -jar net.leksi.contest.assistant.jar A "+in,k/ia[]"`

#### Example 3
##### Task input:
...
The first line contains two integers n,m (1≤n,m≤20).

The next line contains n strings s1,s2,…,sn. Each string contains only lowercase letters, and they are separated by spaces. The length of each string is at least 1 and at most 10.

The next line contains m strings t1,t2,…,tm. Each string contains only lowercase letters, and they are separated by spaces. The length of each string is at least 1 and at most 10.

Among the given n+m strings may be duplicates (that is, they are not necessarily all different).

The next line contains a single integer q (1≤q≤2020).

In the next q lines, an integer y (1≤y≤10^9) is given, denoting the year we want to know the name for....
##### Wizard run:
`java ... -jar net.leksi.contest.assistant.jar A "in,m/ss[]/st[]/iq/(q;iy/)"`



## API
One should extend the abstract class  `net.leksi.contest.Solver` overriding two abstract methods

    abstract protected void readInput() throws IOException;
    abstract protected void solve() throws IOException;
    
(Note: `readInput` is already overriden at stub given by Wizard)

and optionally reassigning protected fields

    protected String nameIn = null;
    protected String nameOut = null;
    protected boolean singleTest = false;
    protected boolean doNotPreprocess = false;
    protected boolean preprocessDebug = false;
    protected PrintStream debugPrintStream = null;
    protected boolean localMultiTest = false;
    protected boolean localnameIn = "";
    protected boolean localShowTestCases = false;
    protected int localRunTester = 0;


1. `nameIn` - name of input file if the problem requires file input or while testing. Otherwise leave it unchanged (default `null` means console input). If the input file is not found then console input is used, so you may test using file input and submit into "Online Judges" system with console input without changing.
2. `nameOut` - name of output file if the problem requires file output or while testing, otherwise leave it unchanged (default `null` means console output). **Unlike the previous case you should set `null` if the problem requires console output**.
3. `singleTest` - set `false` (or leave unchanged default `false`) if the problem will be run at multiple test cases. In this alternative first line of input will contain one integer - the number of test cases and you need not care to read and process it. Otherwise set `true`.
5. `doNotPreprocess` - set `true` if you need not to compile single source file for "Online Judges" system (for example you don't use user library or are concentrated on testing). Otherwise leave it unchanged (default `false` means single source file compilation at every run).
6. `preprocessDebug` - set `true` if you want to see what happens at compiling process. Otherwise leave it unchanged (default `false` means no debugging info).
7. `debugPrintStream` - set `System.err` if you want to get Exception message at the place it is thrown.
4. `localMultiTest` - set `true` if the problem will be run under single test case at judge system, but multiple ones at a local testing. In this alternative first line of input will contain one integer - the number of test cases and you need not care to read and process it. Otherwise set `false`  (or leave unchanged default `false`). 
8. `localnameIn` - set the path to the local input file even if the judge system use stdin. 
9. `localShowTestCases` - set `true` if you want to delimit test cases' output for visualization. 
10. `localRunTester` - set positive value to use "load testing". To use that you should overwrite the method 
`protected Object test_input()` and optionally `protected void test(final Object input_data, final List<String> output_data)`. See **LoadTesting** below for more information.

Note: all local* fields should be used inside special construction: 
````    
        /*+Preprocess-DONOTCOPY*/
        localMultiTest = true;
        localShowTestCases = true;
        localnameIn = '<some name>';
        localShowTestCases = true;
        localRunTester = 100;
        /*-Preprocess-DONOTCOPY*/
````
It hides its content from exposing to production.

There are predefined protected fields for reading input and writing to output:

    protected MyScanner sc;
    protected PrintWriter pw;
 
`MyScanner` is a lightweight cut implementation of `java.util.Scanner`,
both are supplied in base class in dependence of `nameIn` and `nameOut` fields

### Examples

At the `demo/src` directory one can find files `p00009A.java`, `p001284A.java` and `p001417A.java`. They was generated by te Wizard. In comments one can find a link to the problem and the script used by the Wizard to generate it. The files starting with underscore are the sources ready to be submitted to judge system.

For example: 
1. `java -jar net.leksi.contest.assistant.jar p001417A "+in,k/ia[]"` gives us `p001417A.java` stub file:
````/***********************************************/
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

        /*^^^^^^^^^^^^^^^^^^^^^^^^*/

    }
    static public void main(String[] args) throws IOException {
        new p001417A().run();
    }
}
````
2. We write our code, using custom external class `net.leksi.contest.demo.IntArraySorter`, change the location of the sample input file and have:
````/***********************************************/
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
````
3. Compile `javac -classpath net.leksi.contest.assistant.jar demo/src/p001417A.java` then run `java -classpath net.leksi.contest.assistant.jar;demo/src p001417A` and get production `_p001417A.java`: 
````import java.io.File;import java.io.FileInputStream;import java.io.FileNotFoundException;
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
````
## Troubleshooting
### `javap` is not found or not working
Try to use java option `-Dnet.leksi.solver.javap.dir=<directory-with-working-javap>`

For example: `java -Dnet.leksi.solver.javap.dir=f:/jdk1.8.0_221/bin -classpath net.leksi.contest.assistant.jar;demo/src p001417A`

## Load Testing
To find execution errors and compare the used algorithm with a knowingly proper brutal force algorithm you should overwrite two methods:
1. `protected Object test_input()` used to generate an input data object. Dispate of its internal structure, it must ovewrite `public String toString()` method appropriate to an expected input. For example:
````
class TestInput {
        int n; 
        int q;
        int[] a;
        int[][] op;
        public String toString() {
            return n + " " + q + "\n" + 
                    join(a) + "\n" +
                    Arrays.stream(op).map(v -> join(v)).collect(Collectors.joining("\n"));
        }
        TestInput() {
            n = getRandomInt(1, 100);
            q = 10;
            a = new int[n];
            for(int i = 0; i < n; i++) {
                a[i] = getRandomInt(1, 40);
            }
            op = new int[q][3];
            for(int i = 0; i < q; i++) {
                op[i][0] = getRandomInt(1, 2);
                if(op[i][0] == 1) {
                    op[i][1] = getRandomInt(1, n);
                    op[i][2] = getRandomInt(op[i][1], n);
                } else {
                    op[i][1] = getRandomInt(1, n);
                    op[i][2] = getRandomInt(1, 40);
                }
            }
        }
        ....
    }

````
2. `protected void test(final Object input_data, final List<String> output_data)` used to calculate the answer with another algorithm and to compare it with `solve()` output. `input_data` is an Object generated at `test_input()`, `output_data` is a list of output lines. You may throw an Exception or just print to `System.err` in case of difference.

There are some auxiliary methods to get random numbers:
````
    protected int getRandomInt(final int min, final int max);
    protected long getRandomLong(final long min, final long max);
    protected double getRandomDouble(final double min, final double maxExclusive);
````
All limits are inclusive except the Double maximum.

Note: all load testing staff should be used inside special construction: 
````    
        /*+Preprocess-DONOTCOPY*/
        ...
        /*-Preprocess-DONOTCOPY*/
````
