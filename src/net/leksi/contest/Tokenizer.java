/*
 * MIT License
 * 
 * Copyright (c) 2020 Alexey Zakharov <leksi@leksi.net>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.leksi.contest;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Tokenizer {
    StringBuilder sb = new StringBuilder();
    TreeMap<Integer,String[]> grammar = new TreeMap<>();
    
    public Tokenizer() {
        grammar.put((int)'/', new String[]{"/*", "//", "/=", "/"});
        grammar.put((int)'!', new String[]{"!=", "!"});
        grammar.put((int)'?', new String[]{"?"});
        grammar.put((int)':', new String[]{"::", ":"});
        grammar.put((int)'=', new String[]{"==", "="});
        grammar.put((int)'+', new String[]{"++", "+=", "+"});
        grammar.put((int)'-', new String[]{"--", "-=", "->", "-"});
        grammar.put((int)'*', new String[]{"*=", "*/", "*"});
        grammar.put((int)'%', new String[]{"%=", "%"});
        grammar.put((int)'<', new String[]{"<<=", "<<", "<=", "<"});
        grammar.put((int)'>', new String[]{">>=", ">>", ">=", ">"});
        grammar.put((int)'(', new String[]{"("});
        grammar.put((int)')', new String[]{")"});
        grammar.put((int)'[', new String[]{"["});
        grammar.put((int)']', new String[]{"]"});
        grammar.put((int)'{', new String[]{"{"});
        grammar.put((int)'}', new String[]{"}"});
        grammar.put((int)';', new String[]{";"});
        grammar.put((int)'.', new String[]{"."});
        grammar.put((int)',', new String[]{","});
    }
    
    String comment = null;
    char quote = '\0';
    int escaped = 0;
    
    ArrayList<String> tokens = new ArrayList<>();
    
    public List<String> tokenize(Reader r) throws IOException {
        StringBuilder token = new StringBuilder();
        int c;
        char[] buf = new char[256];
        int buf_end = 0;
        tokens.clear();
        while((c = r.read()) >= 0) {
            while(true) {
                if(buf_end == 0) {
                    buf[0] = (char)c;
                    buf_end++;
                }
                int read = 0;
//                System.out.println("char:'" + buf[0] + "' (" + (int)buf[0] + ")");
                if(grammar.containsKey((int)buf[0])) {
                    if(quote == '\0') {
                        String[] ss = grammar.get((int)buf[0]);
                        if(ss[0].length() - buf_end > 0) {
                            int n = r.read(buf, buf_end, ss[0].length() - buf_end);
                            if(n < 0) {
                                break;
                            }
                            buf_end += n;
                        }
                        int pos = 0;
                        String s1 = String.valueOf(buf, 0, buf_end);
                        for(; pos < ss.length; pos++) {
                            if(s1.substring(0, ss[pos].length()).equals(ss[pos])) {
                                break;
                            }
                        }
                        if(comment == null) {
                            if(pos == ss.length) {
                                throw new RuntimeException("unexpected char: '" + buf[0] + "'");
                            }
                            read = ss[pos].length();
                            if(token.length() > 0) {
                                onToken(false, token.toString());
                                token.delete(0, token.length());
                            }
                            if("/*".equals(ss[pos])) {
                                comment = "*/";
                                token.append(ss[pos]);
                            } else if("//".equals(ss[pos])) {
                                comment = "\n";
                                token.append(ss[pos]);
                            } else {
                                onToken(true, ss[pos]);
                            }
                        } else {
                            if(pos < ss.length && comment.equals(ss[pos])) {
                                read = ss[pos].length();
                                token.append(ss[pos]);
                                comment = null;
                                onToken(true, token.toString());
                                token.delete(0, token.length());
                            } else {
                                token.append(buf[0]);
                                read = 1;
                            }
                        }
                    } else {
                        token.append(buf[0]);
                        read = 1;
                    }
                } else {
                    if(buf[0] == ' ' || buf[0] == '\t' || buf[0] == '\n' || buf[0] == '\r') {
                        if(comment != null) {
                            if(buf[0] == comment.charAt(0)) {
                                comment = null;
                                onToken(true, token.toString());
                                token.delete(0, token.length());
                                token.append(" ");
                            } else {
                                token.append(buf[0]);
                            }
                        } else if(quote != '\0') {
                                token.append(buf[0]);
                        } else {
                            if(token.length() > 0) {
                                if(token.length() == 1 && token.charAt(0) == ' ') {
                                    // 
                                } else {
                                    onToken(false, token.toString());
                                    token.delete(0, token.length());
                                    token.append(" ");
                                }
                            } else {
                                token.append(" ");
                            }
                        }
                    } else {
                        if(quote != '\0') {
                            if(quote == buf[0] && escaped == 0) {
                                token.append(buf[0]);
                                onToken(true, token.toString());
                                token.delete(0, token.length());
                                quote = '\0';
                            } else {
                                token.append(buf[0]);
                                if(buf[0] == '\\' || escaped == 1) {
                                    escaped ^= 1;
                                }
                            }
                        } else {
                            if(comment == null && (buf[0] == '"' || buf[0] == '\'')) {
                                quote = buf[0];
                                if(token.length() > 0) {
                                    onToken(false, token.toString());
                                    token.delete(0, token.length());
                                }
                            } else if(token.length() == 1 && token.charAt(0) == ' ') {
                                onToken(false, token.toString());
                                token.delete(0, token.length());
                            }
                            token.append(buf[0]);
                        }
                    }
                    read = 1;
                }
                if(read <= buf_end) {
                    buf_end -= read;
                }
                if(buf_end == 0) {
                    break;
                }
                System.arraycopy(buf, read, buf, 0, buf_end);
            }
        }
        if(token.length() > 0) {
            onToken(false, token.toString());
            token.delete(0, token.length());
        }
        return tokens.stream().collect(Collectors.toList());
    }
    
    public static void main(String[] args) throws IOException {
        try(FileReader r = new FileReader(args[0]);) {
            System.out.println(new Tokenizer().tokenize(r).stream().map(t -> " ".equals(t) ? "__SPACE__" : t).collect(Collectors.joining("\n")));
        }
    }

    private void onToken(boolean strong, String token) {
        tokens.add(" ".equals(token) ? " " : (strong ? "!" : "?") + token);
    }
}
