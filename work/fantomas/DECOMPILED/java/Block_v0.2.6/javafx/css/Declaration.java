/*
 * Decompiled with CFR 0.152.
 */
package javafx.css;

import com.sun.javafx.css.ParsedValueImpl;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javafx.css.ParsedValue;
import javafx.css.Rule;
import javafx.css.StyleConverter;
import javafx.css.StyleOrigin;
import javafx.css.converter.URLConverter;

public final class Declaration {
    final String property;
    final ParsedValue parsedValue;
    final boolean important;
    Rule rule;

    Declaration(String string, ParsedValue parsedValue, boolean bl) {
        this.property = string;
        this.parsedValue = parsedValue;
        this.important = bl;
        if (string == null) {
            throw new IllegalArgumentException("propertyName cannot be null");
        }
        if (parsedValue == null) {
            throw new IllegalArgumentException("parsedValue cannot be null");
        }
    }

    public ParsedValue getParsedValue() {
        return this.parsedValue;
    }

    public String getProperty() {
        return this.property;
    }

    public Rule getRule() {
        return this.rule;
    }

    public final boolean isImportant() {
        return this.important;
    }

    private StyleOrigin getOrigin() {
        Rule rule = this.getRule();
        if (rule != null) {
            return rule.getOrigin();
        }
        return null;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (this.getClass() != object.getClass()) {
            return false;
        }
        Declaration declaration = (Declaration)object;
        if (this.important != declaration.important) {
            return false;
        }
        if (this.getOrigin() != declaration.getOrigin()) {
            return false;
        }
        if (this.property == null ? declaration.property != null : !this.property.equals(declaration.property)) {
            return false;
        }
        return this.parsedValue == declaration.parsedValue || this.parsedValue != null && this.parsedValue.equals(declaration.parsedValue);
    }

    public int hashCode() {
        int n = 5;
        n = 89 * n + (this.property != null ? this.property.hashCode() : 0);
        n = 89 * n + (this.parsedValue != null ? this.parsedValue.hashCode() : 0);
        n = 89 * n + (this.important ? 1 : 0);
        return n;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(this.property);
        stringBuilder.append(": ");
        stringBuilder.append(this.parsedValue);
        if (this.important) {
            stringBuilder.append(" !important");
        }
        return stringBuilder.toString();
    }

    void fixUrl(String string) {
        if (string == null) {
            return;
        }
        StyleConverter styleConverter = this.parsedValue.getConverter();
        if (styleConverter == URLConverter.getInstance()) {
            ParsedValue[] parsedValueArray = (ParsedValue[])this.parsedValue.getValue();
            parsedValueArray[1] = new ParsedValueImpl(string, null);
        } else if (styleConverter == URLConverter.SequenceConverter.getInstance()) {
            ParsedValue[] parsedValueArray = (ParsedValue[])this.parsedValue.getValue();
            for (int i = 0; i < parsedValueArray.length; ++i) {
                ParsedValue[] parsedValueArray2 = (ParsedValue[])parsedValueArray[i].getValue();
                parsedValueArray2[1] = new ParsedValueImpl(string, null);
            }
        }
    }

    final void writeBinary(DataOutputStream dataOutputStream, StyleConverter.StringStore stringStore) throws IOException {
        if (this.parsedValue instanceof ParsedValueImpl) {
            dataOutputStream.writeShort(stringStore.addString(this.getProperty()));
            ((ParsedValueImpl)this.parsedValue).writeBinary(dataOutputStream, stringStore);
            dataOutputStream.writeBoolean(this.isImportant());
        }
    }

    static Declaration readBinary(int n, DataInputStream dataInputStream, String[] stringArray) throws IOException {
        String string = stringArray[dataInputStream.readShort()];
        ParsedValueImpl parsedValueImpl = ParsedValueImpl.readBinary(n, dataInputStream, stringArray);
        boolean bl = dataInputStream.readBoolean();
        return new Declaration(string, parsedValueImpl, bl);
    }
}

