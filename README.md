# contest-compiler
The library is intended to simplify some routines one faces using Java for "Online Judges" contests.
+ Quick coding input data structure and read it;
+ Using one's favorite algorithms without copying sources into your submission manually.
## Preface
This library would be useful for one who likes to solve programming problems, take part in online programming contests and use Java for these purposes. First, one needs everytime coding the same routines to read input data. Second, If one have one's favorite homemade classes implementing various algorithms one has to copy-paste them into the source file to submit into "Online Judges" system as it acceps single file. Thus, this library helps to concentrate on the problem itself.
## Input Data reading support
Running class `net.leksi.contest.Wizard` one gets a "stub" with all input data structure is coded and ready to be read without (almost) any actions. The usage of the `net.leksi.contest.Wizard`:

    Usage: java java_options net.leksi.contest.Wizard wizard_options class-name script
        java_options:           java options like -classpath;
        wizard_options:
            -stdout             - write to stdout (default creates file <class-name>.java);
            -src directory      - the directory to generate sources into (default .);
            -package package    - the package of class to generate (default empty);
            -force              - overwrite existing file (default throws exception);
        class-name:             name of class to generate;
        script:                 input script;
    
The "input script" means the script on lightweight special language. This script follows the task input description.
### The Script Language Grammar in Backus–Naur form

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
    
`'*'` at the beginning of script means that there are multiple test at one submission run, as the number of test itself does not matter the `'*'` is all one needs to support that case. Futher one codes as if there only test at submission run.
`new-line` literal means an expected end of line of input.
`type` means type of variable, `'i'`, `'l'`, `'d'`, `'s'` stand for `int`, `long`, `double` and `java.lang.String` respectively.
if the variable is an array its definition should end with `'['`, optional `length` and `']'`, if the `length` is present the variable will take exactly `length` elements from iunput, otherwise the variable will take all elements till the end of line. So, one should not use `length` in the case the array implied to take all the line.
`java.lang.String` variable takes all chars till the end of line, `java.lang.String` *array* variable takes chars split with spaces

## API
The frontend is an abstract class  `net.leksi.contest.Solver`. One should extend it overriding two abstract methods

    abstract public void readInput() throws IOException;
    abstract public void solve() throws IOException;
    
and optionally reassigning protected fields

    protected String nameIn = null;
    protected String nameOut = null;
    protected boolean singleTest = false;
    protected boolean doNotPreprocess = false;
    protected boolean preprocessDebug = false;

Let's regard the fields first. 
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

### Demo



