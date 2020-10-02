package net.leksi.contest;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class Solver {
    
    protected String nameIn = null;
    protected String nameOut = null;
    protected boolean singleTest = false;

    protected boolean preprocessDebug = false;
    protected boolean doNotPreprocess = false;
    
    /*+Preprocess-DONOTCOPY*/
    private void Preprocess_DONOTCOPY() {
        if(!doNotPreprocess) {
            String running = getClass().getName();
            running = running.substring(0, running.indexOf("$"));
            Preprocessor pp = new Preprocessor();
            pp.debug(preprocessDebug);
            pp.skipPrefix("Preprocess_DONOTCOPY");
            pp.run(running);
            if(preprocessDebug) {
                System.out.println("----- " + running + " output -----");
            }
        }
    }
    /*-Preprocess-DONOTCOPY*/
    private void preProcess(final BufferedReader br, final PrintWriter pw) throws IOException {
        /*+Preprocess-DONOTCOPY*/
        Preprocess_DONOTCOPY();
        /*-Preprocess-DONOTCOPY*/
        if(!singleTest) {
            int t = Integer.valueOf(br.readLine().trim());
            while(t-- > 0) {
                process(br, pw);
            }
        } else {
            process(br, pw);
        }
    }
    
    abstract public void process(final BufferedReader br, final PrintWriter pw) throws IOException;

    protected int[] readIntArray(final BufferedReader br) throws IOException {
        return Arrays.stream(br.readLine().trim().split("\\s+")).mapToInt(v -> Integer.valueOf(v)).toArray();
    }

    protected long[] readLongArray(final BufferedReader br) throws IOException {
        return Arrays.stream(br.readLine().trim().split("\\s+")).mapToLong(v -> Long.valueOf(v)).toArray();
    }
    
    protected String readString(final BufferedReader br) throws IOException {
        return br.readLine().trim();
    }

    protected String intArrayToString(final int[] a) {
        return Arrays.stream(a).mapToObj(v -> Integer.toString(v)).collect(Collectors.joining(" "));
    }

    protected String longArrayToString(final long[] a) {
        return Arrays.stream(a).mapToObj(v -> Long.toString(v)).collect(Collectors.joining(" "));
    }

    public void run() throws IOException {
        try {
            try (
                FileReader fr = new FileReader(nameIn);
                BufferedReader br = new BufferedReader(fr);
                PrintWriter pw = select_output();
            ) {
                preProcess(br, pw);
            }
        } catch(Exception ex) {
            try (
                InputStreamReader fr = new InputStreamReader(System.in);
                BufferedReader br = new BufferedReader(fr);
                PrintWriter pw = select_output();
            ) {
                preProcess(br, pw);
            }
        }
    }

    private PrintWriter select_output() throws FileNotFoundException {
        if (nameOut != null) {
            return new PrintWriter(nameOut);
        }
        return new PrintWriter(System.out);
    }
}
