/*
 * The MIT License
 *
 * Copyright 2020 Alexey Zakharov <leksi@leksi.net>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.leksi.contest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ClassesCollector {

    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            new ClassesCollector().run(args[0]);
        }
    }

    final String sym = "+-/*(){}&|?:<>=^[];.,";
    
    private void run(final String arg) {
        File classes = new File(arg);
        String[] package0 = new String[]{null};
        StringBuilder sb = new StringBuilder();
        Stream.of(classes.listFiles()).filter(file -> !file.isDirectory() && file.getName().endsWith(".java")).forEach(v -> collectClass(v, sb, package0, null));
        if (package0[0] != null) {
            compress(sb);
            sb.insert(0, "// begin import " + package0[0] + "\n");
            sb.append("\n// end import " + package0[0]);
        }
        System.out.println(sb.toString());
    }
    
    public void collectClass(BufferedReader br, final StringBuilder sb, final String[] package0, final TreeSet<String> imports, final String class_name) {
        boolean[] donotcopy = new boolean[]{false};
        String content = br.lines().map(line -> line.trim()).filter(line -> {
            if ("/*+Preprocess-DONOTCOPY*/".equals(line.trim())) {
                donotcopy[0] = true;
                return false;
            }
            if ("/*-Preprocess-DONOTCOPY*/".equals(line.trim())) {
                donotcopy[0] = false;
                return false;
            }
            if (donotcopy[0]) {
                return false;
            }
            if (imports != null && line.startsWith("import ")) {
                imports.add(line);
            }
            return true;
        }).collect(Collectors.joining("\n"));
        if (package0 != null && package0[0] == null) {
            int pos0 = content.indexOf("package ");
            if (pos0 >= 0) {
                int pos4 = content.indexOf(";", pos0);
                if (pos4 >= 0) {
                    package0[0] = content.substring(pos0, pos4 + 1);
                }
            }
        }
        int pos1 = content.indexOf(class_name);
        if (pos1 >= 0) {
            int selector = 0;
            int pos2 = content.lastIndexOf("abstract ", pos1);
            if (pos2 < 0) {
                selector = 1;
                pos2 = content.lastIndexOf("class ", pos1);
            }
            if (pos2 < 0) {
                selector = 2;
                pos2 = content.lastIndexOf("interface ", pos1);
            }
            if (pos2 < 0) {
                selector = 3;
                pos2 = content.lastIndexOf("enum ", pos1);
            }
            if (pos2 >= 0) {
                int pos3 = content.lastIndexOf("}");
                if (pos3 >= 0) {
                    if (selector <= 1) {
                        sb.append("static private ");
                    }
                    sb.append(content.substring(pos2, pos3 + 1).trim());
                }
            }
        }
    }
    
    public void collectClass(final File file, final StringBuilder sb, final String[] package0, final TreeSet<String> imports) {
            String class_name = file.getName().substring(0, file.getName().length() - ".java".length());
            try (
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
            ) {
                collectClass(br, sb, package0, imports, class_name);
            } catch (IOException ex) {
                Logger.getLogger(ClassesCollector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    public void compress(final StringBuilder sb) {
        char quote = '\0';
        int strlen = 0;
        boolean space = false;
        boolean escape = false;
        int comment = 0;
        for (int i = 0; i < sb.length(); i++) {
            char c = sb.charAt(i);
            boolean skip = false;
            if (quote != '\0') {
                if (!escape) {
                    if (c == '\\') {
                        escape = true;
                    } else if (c == quote) {
                        quote = '\0';
                    }
                } else {
                    escape = false;
                }
            } else if (comment > 0) {
                skip = true;
                if(comment == 2 && c == '*') {
                    comment = 3;
                } else if(comment == 3) {
                    if(c == '/') {
                        comment = 0;
                    } else {
                        comment = 2;
                    }
                } else if (comment == 1 && (c == '\n' || c == '\r')) {
                    comment = 0;
                }
                if (comment == 0) {
                    sb.setCharAt(i, ' ');
                    i--;
                    continue;
                }
            } else {
                if (c == '/' && i + 1 < sb.length()) {
                    if (sb.charAt(i + 1) == '/') {
                        comment = 1;
                    } else if (sb.charAt(i + 1) == '*') {
                        comment = 2;
                    }
                    if (comment > 0) {
                        sb.delete(i, i + 2);
                        i--;
                        continue;
                    }
                }
                if (c == '\t' || c == '\n' || c == '\r') {
                    c = ' ';
                    sb.setCharAt(i, c);
                }
                if ((sym.indexOf(c) >= 0 || c == ' ') && c != '=' && c != '&' && c != '|' && c != '+' && c != '-' && c != '>' && c != '<' && strlen >= 80) {
                    strlen = 0;
                    sb.insert(i, '\n');
                    space = true;
                    i++;
                }
                if (c == ' ') {
                    if (!space) {
                        if (i + 1 < sb.length() && sym.indexOf(sb.charAt(i + 1)) >= 0 || i - 1 >= 0 && sym.indexOf(sb.charAt(i - 1)) >= 0) {
                            skip = true;
                        } else {
                            space = true;
                        }
                    } else {
                        skip = true;
                    }
                } else {
                    space = false;
                    if (c == '\'' || c == '"') {
                        quote = c;
                    }
                }
            }
            if (skip) {
                sb.deleteCharAt(i);
                i--;
            } else {
                strlen++;
            }
        }
    }
}
