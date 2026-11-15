/*
 * Decompiled with CFR 0.152.
 */
package javafx.css;

import com.sun.javafx.css.Combinator;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import javafx.css.CompoundSelector;
import javafx.css.Match;
import javafx.css.PseudoClass;
import javafx.css.Rule;
import javafx.css.SimpleSelector;
import javafx.css.StyleConverter;
import javafx.css.Styleable;

public abstract class Selector {
    private Rule rule;
    private int ordinal = -1;
    private static final int TYPE_SIMPLE = 1;
    private static final int TYPE_COMPOUND = 2;

    static Selector getUniversalSelector() {
        return UniversalSelector.INSTANCE;
    }

    void setRule(Rule rule) {
        this.rule = rule;
    }

    public Rule getRule() {
        return this.rule;
    }

    public void setOrdinal(int n) {
        this.ordinal = n;
    }

    public int getOrdinal() {
        return this.ordinal;
    }

    public abstract Match createMatch();

    public abstract boolean applies(Styleable var1);

    public abstract boolean applies(Styleable var1, Set<PseudoClass>[] var2, int var3);

    public abstract boolean stateMatches(Styleable var1, Set<PseudoClass> var2);

    protected void writeBinary(DataOutputStream dataOutputStream, StyleConverter.StringStore stringStore) throws IOException {
        if (this instanceof SimpleSelector) {
            dataOutputStream.writeByte(1);
        } else {
            dataOutputStream.writeByte(2);
        }
    }

    static Selector readBinary(int n, DataInputStream dataInputStream, String[] stringArray) throws IOException {
        byte by = dataInputStream.readByte();
        if (by == 1) {
            return SimpleSelector.readBinary(n, dataInputStream, stringArray);
        }
        return CompoundSelector.readBinary(n, dataInputStream, stringArray);
    }

    public static Selector createSelector(String string) {
        int n;
        if (string == null || string.length() == 0) {
            return null;
        }
        ArrayList<SimpleSelector> arrayList = new ArrayList<SimpleSelector>();
        ArrayList<Combinator> arrayList2 = new ArrayList<Combinator>();
        ArrayList<String> arrayList3 = new ArrayList<String>();
        int n2 = 0;
        int n3 = -1;
        char c = '\u0000';
        for (n = 0; n < string.length(); ++n) {
            char c2 = string.charAt(n);
            if (c2 == ' ') {
                if (c != '\u0000') continue;
                c = c2;
                n3 = n;
                continue;
            }
            if (c2 == '>') {
                if (c == '\u0000') {
                    n3 = n;
                }
                c = c2;
                continue;
            }
            if (c == '\u0000') continue;
            arrayList3.add(string.substring(n2, n3));
            n2 = n;
            arrayList2.add(c == ' ' ? Combinator.DESCENDANT : Combinator.CHILD);
            c = '\u0000';
        }
        arrayList3.add(string.substring(n2));
        for (n = 0; n < arrayList3.size(); ++n) {
            String string2 = (String)arrayList3.get(n);
            if (string2 == null || string2.equals("")) continue;
            String[] stringArray = string2.split(":");
            ArrayList<String> arrayList4 = new ArrayList<String>();
            for (int i = 1; i < stringArray.length; ++i) {
                if (stringArray[i] == null || stringArray[i].equals("")) continue;
                arrayList4.add(stringArray[i].trim());
            }
            String string3 = stringArray[0].trim();
            String[] stringArray2 = string3.split("\\.");
            ArrayList<String> arrayList5 = new ArrayList<String>();
            for (int i = 1; i < stringArray2.length; ++i) {
                if (stringArray2[i] == null || stringArray2[i].equals("")) continue;
                arrayList5.add(stringArray2[i].trim());
            }
            String string4 = null;
            String string5 = null;
            if (!stringArray2[0].equals("")) {
                if (stringArray2[0].charAt(0) == '#') {
                    string5 = stringArray2[0].substring(1).trim();
                } else {
                    string4 = stringArray2[0].trim();
                }
            }
            arrayList.add(new SimpleSelector(string4, arrayList5, arrayList4, string5));
        }
        if (arrayList.size() == 1) {
            return (Selector)arrayList.get(0);
        }
        return new CompoundSelector(arrayList, arrayList2);
    }

    private static class UniversalSelector {
        private static final Selector INSTANCE = new SimpleSelector("*", null, null, null);

        private UniversalSelector() {
        }
    }
}

