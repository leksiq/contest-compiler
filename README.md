# contest-compiler
Compiles single Java source file from multiple source files for "Online Judges".
## Preface
This library would be useful for you if you like to solve programming problems, take part in online programming contests and use Java for these purposes. If you have your favorite homemade classes implementing various algorithms you have to copy-paste them into the source file to submit into "Online Judges" system as it acceps single file. Besides, this library simplifies input-output routines that permits to concentrate on the problem itself.
## API
The frontend is an abstract class named Solver. You should extend it overriding an abstract method

    abstract public void process(final BufferedReader br, final PrintWriter pw) throws IOException;
    
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

The `process` method supplies you with `BufferedReader br` and `PrintWriter pw` arguments for reading from input and writing to output respectively. Thus, you may use the following template in most cases:

    //necessary imports are here
    import net.leksi.contest.Solver;
    
    public class MyDecision {
        public static void main(String[] ags) throws IOException {
            new Solver() {{{ nameIn = "some_input_file_name"; singleTest = true;}}
                @Override
                public void process(BufferedReader br, PrintWriter pw) throws IOException {
                
                    /* your creartive code here */
                    
                }
            }.run();
        }
    }

There are some methods to simplify routine actions:

    protected int[] readIntArray(final BufferedReader br) throws IOException;

    protected long[] readLongArray(final BufferedReader br) throws IOException;
    
    protected String readString(final BufferedReader br) throws IOException;
    
    protected String intArrayToString(final int[] a);

    protected String longArrayToString(final long[] a);
    
    protected List<Long> longArrayToList(final long[] a);

    protected List<Integer> intArrayToList(final int[] a);
    
    protected List<Long> intArrayToLongList(final int[] a);
    
1. `readIntArray` splits input line delimited with spaces into `int[]`.
1. `readLongArray` splits input line delimited with spaces into `long[]`.
1. `readString` reads whole input line. 
1. `intArrayToString` joins `int[]` into `String` delimited with spaces.
1. `longArrayToString` joins `long[]` into `String` delimited with spaces.
1. `longArrayToList` creates  `List<Long>` from `long[]`.
1. `intArrayToList` creates  `List<Integer>` from `int[]`.
1. `intArrayToLongList` creates  `List<Long>` from `int[]`.

### Demo



