# java-contest-assistant
The library is intended to simplify some routines one faces using Java for "Online Judges" contests.
+ Quick coding input data structure and read it;
+ Using one's favorite algorithms without copying sources into your submission manually.
## Preface
This library would be useful for one who likes to solve programming problems, take part in online programming contests and use Java for these purposes. First, one needs everytime coding the same routines to read input data. Second, If one have one's favorite homemade classes implementing various algorithms one has to copy-paste them into the source file to submit into "Online Judges" system as it acceps single file. Thus, this library helps to concentrate on the problem itself.
## How It Works
The work falls into three steps.
1. Run **Wizard** with class name and one-line script describing input rule to get a stub with input data structure supplied.
2. Write code as a body of `protected void solve() throws IOException` method. Import any custom libraries developed earlier (given that their sources can be found through classpath).
3. Compile and run developed class with test samples. Let's call it **preprocessing**. The resulting source to submit to judge system will be found at the sources directory with underscore before name.

See **Examples** below.

## 1. Running the Wizard
Running class `net.leksi.contest.Wizard` one gets a *stub* with all input data structure is coded and ready to be read without (almost) any actions. An empty test input file is created as well. The usage of the `net.leksi.contest.Wizard`:

    Usage: java java_options -jar net.leksi.contest.assistant.jar wizard_options class-name script
    java_options:           java options like -classpath;
    wizard_options:
        -stdout                 - write to stdout (default creates file <class-name>.java);
        -src <directory>        - the directory to generate source into (default .);
        -in <name>              - generate input file <name> (default <class-name>.in);
        -package package        - the package of class to generate (default empty);
        -force                  - overwrite existing files (default throws exception for source file and leaves input file);
    class-name:             name of class to generate;
    script:                 input script;

    
The "input script" means the script on lightweight special language. This script follows the task input description.
### The Script Language Grammar

    script                      ::=     '*'? input-of-test
    input-of-test               ::=     input-of-cycle
    input-of-cycle              ::=     cycle* (variables-group cycle+)* variables-group?
    cycle                       ::=     '(' variable-name | number ';' input-of-cycle ')'
    variables-group             ::=     same-type-variables-group (';' same-type-variables-group | new-line)*
    variable-name               ::=     <Java's  legal identifier>
    same-type-variables-group   ::=     type variable-definition (',' variable-definition)
    new-line                    ::=     '/'
    type                        ::=     'i' | 'l' | 'd' | 's'
    variable-definition         ::=     variable-name ('[' array-length? ']')?
    array-length                ::=     variable-name | number
    
`'*'` at the beginning of script means that there are multiple test at one submission run, as the number of test itself does not matter the `'*'` is all one needs to support that case. Futher one codes as if there is only test at submission run.
`new-line` literal means an expected end of line of input.
`type` means type of variable, `'i'`, `'l'`, `'d'`, `'s'` stand for `int`, `long`, `double` and `java.lang.String` respectively.
if the variable is an array its definition should end with `'['`, optional `length` and `']'`, if the `length` is present the variable will take exactly `length` elements from iunput, otherwise the variable will take all elements till the end of line. So, one should not use `length` in the case the array implied to take all the line.
`java.lang.String` variable takes all chars till the end of line, `java.lang.String` *array* variable takes substrings split with spaces at the rule described before. Space chars may present between any terms.

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
`java ... -jar net.leksi.contest.assistant.jar A "*in"`

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
`java ... -jar net.leksi.contest.assistant.jar A "*in,k/ia[n]"`
or preferable
`java ... -jar net.leksi.contest.assistant.jar A "*in,k/ia[]"`

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

1. `nameIn` - name of input file if the problem requires file input or while testing. Otherwise leave it unchanged (default `null` means console input). If the input file is not found then console input is used, so you may test using file input and submit into "Online Judges" system with console input without changing.
2. `nameOut` - name of output file if the problem requires file output or while testing, otherwise leave it unchanged (default `null` means console output). **Unlike the previous case you should set `null` if the problem requires console output**.
3. `singleTest` - set `false` (or leave unchanged default `false`) if the problem will be run at multiple test cases. In this alternative first line of input will contain one integer - the number of test cases and you need not care to read and process it. Otherwise set `true`.
4. `doNotPreprocess` - set `true` if you need not to compile single source file for "Online Judges" system (for example you don't use user library or are concentrated on testing). Otherwise leave it unchanged (default `false` means single source file compilation at every run).
5. `preprocessDebug` - set `true` if you want to see what happens at compiling process. Otherwise leave it unchanged (default `false` means no debugging info).

There are some methods to simplify routine actions:

    protected int[] lineToIntArray() throws IOException;

    protected long[] lineToLongArray() throws IOException;
    
    protected String intArrayToString(final int[] a);

    protected String longArrayToString(final long[] a);
    
    protected List<Long> longArrayToList(final long[] a);

    protected List<Integer> intArrayToList(final int[] a);
    
    protected List<Long> intArrayToLongList(final int[] a);
    
1. `lineToIntArray` parses rest of input line as `int[]` delimited with spaces.
1. `lineToLongArray` parses rest of input line as `long[]` delimited with spaces.
1. `intArrayToString` joins `int[]` into `String` delimited with spaces.
1. `longArrayToString` joins `long[]` into `String` delimited with spaces.
1. `longArrayToList` creates  `List<Long>` from `long[]`.
1. `intArrayToList` creates  `List<Integer>` from `int[]`.
1. `intArrayToLongList` creates  `List<Long>` from `int[]`.

Also there are predefined protected fields for reading input and writing to output:

    protected Scanner sc;
    protected PrintWriter pw;
    
both are supplied in base class in dependence of `nameIn` and `nameOut` fields

### Examples

At the `demo/src` directory one can find files `p00009A.java`, `p001284A.java` and `p001417A.java`. They was generated by te Wizard. In comments one can find a link to the problem and the script used by the Wizard to generate it. The files starting with underscore are the sources ready to be submitted to judge system.

For example: 
1. `java -jar net.leksi.contest.assistant.jar p001417A "*in,k/ia[]"` gives us `p001417A.java` stub file:
````import java.io.IOException;
import net.leksi.contest.Solver;
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
````
2. We write our code, using custom external class `net.leksi.contest.demo.IntArraySorter`, change the location of the sample input file and have:
````import java.io.IOException;
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
````
3. Compile `javac -classpath net.leksi.contest.assistant.jar demo/src/p001417A.java` then run `java -classpath net.leksi.contest.assistant.jar;demo/src p001417A` and get: 
````import java.io.FileInputStream;
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
static private class p001417A extends Solver{public p001417A(){nameIn="demo/p001417A.in"
;singleTest=false;}int n;int k;int[]a;@Override protected void solve(){IntArraySorter
.sort(a);int res=Arrays.stream(a,1,n).map(v->(k-v)/a[0]).sum();pw.println(res);}
@Override public void readInput()throws IOException{n=sc.nextInt();k=sc.nextInt(
);sc.nextLine();a=Arrays.stream(sc.nextLine().trim().split("\\s+")).mapToInt(Integer
::valueOf).toArray();}static public void _main(String[]args)throws IOException{new
p001417A().run();}}
//end p001417A.java
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
//begin net/leksi/contest/demo/IntArraySorter.java
static private class IntArraySorter{public static void sort(int[]arr){Arrays.sort
(arr);}}
//end net/leksi/contest/demo/IntArraySorter.java
}

````
