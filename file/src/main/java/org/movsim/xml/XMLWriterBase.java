/*
 * Copyright (C) 2010, 2011  Martin Budden, Ralph Germ, Arne Kesting, and Martin Treiber.
 *
 * This file is part of MovSim.
 *
 * MovSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MovSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MovSim.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.movsim.xml;

@SuppressWarnings({ "nls" })
public class XMLWriterBase {
    int currentIndent;
    static final int INDENT_DELTA = 4;

    public static void write(String string) {
        System.out.println(string.replace(".000000\"", ".0\"").replace(".500000\"", ".5\""));
    }

    static String padLeft(String s, int n) {
        if (n <= 0) {
            return s;
        }
        n += s.length();
        final String ret = String.format("%1$#" + n + "s", s);
        return ret;
    }

    public void startTag(String tag) {
        write(padLeft("<" + tag + ">", currentIndent));
        currentIndent += INDENT_DELTA;
    }

    public void startTag(String tag, String attributes) {
        if (attributes == null) {
            write(padLeft("<" + tag + ">", currentIndent));
        } else {
            write(padLeft("<" + tag + " " + attributes + ">", currentIndent));
        }
        currentIndent += INDENT_DELTA;
    }

    public void startTagWithComment(String tag, String attributes, String comment) {
        if (attributes == null) {
            write(padLeft("<" + tag + ">" + "<!--" + comment + "-->", currentIndent));
        } else {
            write(padLeft("<" + tag + " " + attributes + ">" + "<!--" + comment + "-->", currentIndent));
        }
        currentIndent += INDENT_DELTA;
    }

    public void endTag(String tag) {
        currentIndent -= INDENT_DELTA;
        write(padLeft("</" + tag + ">", currentIndent));
    }

    public void writeTag(String tag) {
        write(padLeft("<" + tag + " />", currentIndent));
    }

    public void writeTag(String tag, String attributes) {
        if (attributes == null) {
            write(padLeft("<" + tag + " />", currentIndent));
        } else {
            write(padLeft("<" + tag + " " + attributes + " />", currentIndent));
        }
    }

    public void writeTagWithComment(String tag, String attributes, String comment) {
        if (attributes == null) {
            write(padLeft("<" + tag + " />" + "<!--" + comment + "-->", currentIndent));
        } else {
            write(padLeft("<" + tag + " " + attributes + " />" + "<!--" + comment + "-->", currentIndent));
        }
    }
}
