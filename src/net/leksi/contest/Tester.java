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

import java.util.List;
import java.util.Stack;

/**
 *
 * @author alexei
 */
public abstract class Tester {
    static public int getRandomInt(final int min, final int max) {
        return (min + (int)Math.floor(Math.random() * (max - min + 1)));
    }
    
    static public long getRandomLong(final long min, final long max) {
       return (min + (long)Math.floor(Math.random() * (max - min + 1)));
    }
    
    static public double getRandomDouble(final double min, final double maxExclusive) {
        return (min + Math.random() * (maxExclusive - min));
    }
    
    abstract protected void testOutput(final List<String> output_data);
    abstract protected void generateInput();
    abstract protected String inputDataToString();
    
    private boolean break_tester = false;
    
    protected void beforeTesting() {}
    
    protected void breakTester() {
        break_tester = true;
    }
    
    public boolean broken() {
        return break_tester;
    }
}
