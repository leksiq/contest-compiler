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

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author alexei
 */
public class MyScanner {

    private StringBuilder cache = new StringBuilder();
    int cache_pos = 0;
    private int first_linebreak = -1;
    private int second_linebreak = -1;
    private StringBuilder sb = new StringBuilder();
    private InputStream is = null;
    
    public MyScanner(final InputStream is) {
        this.is = is;
    }
    
    private String charToString(final int c) {
        return String.format("'%s'", c == '\n' ? "\\n" : (c == '\r' ? "\\r" : String.valueOf((char)c)));
    }
    
    public int get() {
        int res = -1;
        if(cache_pos < cache.length()) {
            res = cache.charAt(cache_pos);
            cache_pos++;
            if(cache_pos == cache.length()) {
                cache.delete(0, cache_pos);
                cache_pos = 0;
            }
        } else {
            try {
                res = is.read();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
//        System.out.println("get: " + charToString(res));
        return res;
    }
    
    private void unget(final int c) {
//        System.out.println("unget: " + charToString(c));
        if(cache_pos == 0) {
            cache.insert(0, (char)c);
        } else {
            cache_pos--;
        }
    }
    
    public String nextLine() {
//        System.out.println("nextLine:");
        sb.delete(0, sb.length());
        int c;
        boolean done = false;
        boolean end = false;
        while((c = get()) != -1) {
            if(check_linebreak(c)) {
                done = true;
                if(c == first_linebreak) {
                    if(!end) {
                        end = true;
                    } else {
                        cache.append((char)c);
                        break;
                    }
                } else if(second_linebreak != -1 && c == second_linebreak) {
                    break;
                }
            }
            if(end && c != first_linebreak && c != second_linebreak) {
                cache.append((char)c);
                break;
            }
            if(!done) {
                sb.append((char)c);
            }
        }
        return sb.toString();
    }

    private boolean check_linebreak(int c) {
        if(c == '\n' || c == '\r') {
            if(first_linebreak == -1) {
                first_linebreak = c;
    //                    System.out.println("main_linebreak: " + charToString(first_linebreak));
            } else if(c != first_linebreak && second_linebreak == -1) {
                second_linebreak = c;
    //                    System.out.println("second_linebreak: " + charToString(second_linebreak));
            }
            return true;
        }
        return false;
    }
    
    public int nextInt() {
//        System.out.println("nextInt:");
        int sign = 0;
        boolean done = false;
        boolean started = false;
        int res = 0;
        int c;
        while((c = get()) != -1) {
            check_linebreak(c);
            if (sign == 0 && c == '-') {
                started = true;
                sign = -1;
            } else if (c >= '0' && c <= '9') {
                if(sign == 0) {
                    sign = 1;
                }
                started = true;
                res *= 10;
                res += c - '0';
                done = true;
            } else if (!done) {
                if(started) {
                    unget(c);
                    if(sign == -1) {
                        unget('-');
                    }
                    break;
                }
            } else {
                unget(c);
                break;
            }
        }
        if(done) {
            return res * sign;
        }
        throw new RuntimeException();
    }

    public long nextLong() {
//        System.out.println("nextLong:");
        int sign = 0;
        boolean done = false;
        boolean started = false;
        long res = 0;
        int c;
        while((c = get()) != -1) {
            check_linebreak(c);
            if (sign == 0 && c == '-') {
                started = true;
                sign = -1;
            } else if (c >= '0' && c <= '9') {
                if(sign == 0) {
                    sign = 1;
                }
                started = true;
                res *= 10;
                res += c - '0';
                done = true;
            } else if (!done) {
                if(started) {
                    unget(c);
                    if(sign == -1) {
                        unget('-');
                    }
                    break;
                }
            } else {
                unget(c);
                break;
            }
        }
        if(done) {
            return res * sign;
        }
        throw new RuntimeException();
    }

    public boolean hasNext() {
        boolean res = false;
        int c;
        while((c = get()) != -1) {
            if(!check_linebreak(c) && c != ' ' && c != '\t') {
                res = true;
                unget(c);
                break;
            }
        }
        return res;
    }

    public String next() {
        sb.delete(0, sb.length());
        boolean started = false;
        int c;
        while((c = get()) != -1) {
            if(check_linebreak(c) || c == ' ' || c == '\t') {
                if(started) {
                    unget(c);
                    break;
                }
            } else {
                started = true;
                sb.append((char)c);
            }
        }
        return sb.toString();
    }

    public int nextChar() {
        return get();
    }
    
    public boolean eof() {
        int c = get();
        boolean res = false;
        if(c != -1) {
            unget(c);
        } else {
            res = true;
        }
        return res;
    }
    
}
