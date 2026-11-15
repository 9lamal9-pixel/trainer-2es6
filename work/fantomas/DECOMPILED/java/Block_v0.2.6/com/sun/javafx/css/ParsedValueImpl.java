/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.css;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javafx.css.ParsedValue;
import javafx.css.Size;
import javafx.css.SizeUnits;
import javafx.css.StyleConverter;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class ParsedValueImpl<V, T>
extends ParsedValue<V, T> {
    private final boolean lookup;
    private final boolean containsLookups;
    private static int indent = 0;
    private int hash = Integer.MIN_VALUE;
    private static final byte NULL_VALUE = 0;
    private static final byte VALUE = 1;
    private static final byte VALUE_ARRAY = 2;
    private static final byte ARRAY_OF_VALUE_ARRAY = 3;
    private static final byte STRING = 4;
    private static final byte COLOR = 5;
    private static final byte ENUM = 6;
    private static final byte BOOLEAN = 7;
    private static final byte URL = 8;
    private static final byte SIZE = 9;

    @Override
    public final boolean isLookup() {
        return this.lookup;
    }

    @Override
    public final boolean isContainsLookups() {
        return this.containsLookups;
    }

    private static boolean getContainsLookupsFlag(Object object) {
        boolean bl;
        block4: {
            block6: {
                block5: {
                    block3: {
                        bl = false;
                        if (!(object instanceof Size)) break block3;
                        bl = false;
                        break block4;
                    }
                    if (!(object instanceof ParsedValueImpl)) break block5;
                    ParsedValueImpl parsedValueImpl = (ParsedValueImpl)object;
                    bl = parsedValueImpl.lookup || parsedValueImpl.containsLookups;
                    break block4;
                }
                if (!(object instanceof ParsedValueImpl[])) break block6;
                ParsedValueImpl[] parsedValueImplArray = (ParsedValueImpl[])object;
                for (int i = 0; i < parsedValueImplArray.length && !bl; ++i) {
                    if (parsedValueImplArray[i] == null) continue;
                    bl = bl || parsedValueImplArray[i].lookup || parsedValueImplArray[i].containsLookups;
                }
                break block4;
            }
            if (!(object instanceof ParsedValueImpl[][])) break block4;
            ParsedValueImpl[][] parsedValueImplArray = (ParsedValueImpl[][])object;
            for (int i = 0; i < parsedValueImplArray.length && !bl; ++i) {
                if (parsedValueImplArray[i] == null) continue;
                for (int j = 0; j < parsedValueImplArray[i].length && !bl; ++j) {
                    if (parsedValueImplArray[i][j] == null) continue;
                    bl = bl || parsedValueImplArray[i][j].lookup || parsedValueImplArray[i][j].containsLookups;
                }
            }
        }
        return bl;
    }

    public static boolean containsFontRelativeSize(ParsedValue parsedValue, boolean bl) {
        boolean bl2;
        block4: {
            Object v;
            block6: {
                block5: {
                    block3: {
                        bl2 = false;
                        v = parsedValue.getValue();
                        if (!(v instanceof Size)) break block3;
                        Size size = (Size)v;
                        bl2 = size.getUnits() == SizeUnits.PERCENT ? bl : !size.isAbsolute();
                        break block4;
                    }
                    if (!(v instanceof ParsedValue)) break block5;
                    ParsedValueImpl parsedValueImpl = (ParsedValueImpl)v;
                    bl2 = ParsedValueImpl.containsFontRelativeSize(parsedValueImpl, bl);
                    break block4;
                }
                if (!(v instanceof ParsedValue[])) break block6;
                ParsedValue[] parsedValueArray = (ParsedValue[])v;
                for (int i = 0; i < parsedValueArray.length && !bl2; ++i) {
                    if (parsedValueArray[i] == null) continue;
                    bl2 = ParsedValueImpl.containsFontRelativeSize(parsedValueArray[i], bl);
                }
                break block4;
            }
            if (!(v instanceof ParsedValueImpl[][])) break block4;
            ParsedValueImpl[][] parsedValueImplArray = (ParsedValueImpl[][])v;
            for (int i = 0; i < parsedValueImplArray.length && !bl2; ++i) {
                if (parsedValueImplArray[i] == null) continue;
                for (int j = 0; j < parsedValueImplArray[i].length && !bl2; ++j) {
                    if (parsedValueImplArray[i][j] == null) continue;
                    bl2 = ParsedValueImpl.containsFontRelativeSize(parsedValueImplArray[i][j], bl);
                }
            }
        }
        return bl2;
    }

    public ParsedValueImpl(V v, StyleConverter<V, T> styleConverter, boolean bl) {
        super(v, styleConverter);
        this.lookup = bl;
        this.containsLookups = bl || ParsedValueImpl.getContainsLookupsFlag(v);
    }

    public ParsedValueImpl(V v, StyleConverter<V, T> styleConverter) {
        this(v, styleConverter, false);
    }

    @Override
    public T convert(Font font) {
        return (T)(this.converter != null ? this.converter.convert(this, font) : this.value);
    }

    private static String spaces() {
        return new String(new char[indent]).replace('\u0000', ' ');
    }

    private static void indent() {
        indent += 2;
    }

    private static void outdent() {
        indent = Math.max(0, indent - 2);
    }

    public String toString() {
        String string = System.lineSeparator();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ParsedValueImpl.spaces()).append(this.lookup ? "<Value lookup=\"true\">" : "<Value>").append(string);
        ParsedValueImpl.indent();
        if (this.value != null) {
            this.appendValue(stringBuilder, this.value, "value");
        } else {
            this.appendValue(stringBuilder, "null", "value");
        }
        stringBuilder.append(ParsedValueImpl.spaces()).append("<converter>").append(this.converter).append("</converter>").append(string);
        ParsedValueImpl.outdent();
        stringBuilder.append(ParsedValueImpl.spaces()).append("</Value>");
        return stringBuilder.toString();
    }

    private void appendValue(StringBuilder stringBuilder, Object object, String string) {
        String string2 = System.lineSeparator();
        if (object instanceof ParsedValueImpl[][]) {
            ParsedValueImpl[][] parsedValueImplArray = (ParsedValueImpl[][])object;
            stringBuilder.append(ParsedValueImpl.spaces()).append('<').append(string).append(" layers=\"").append(parsedValueImplArray.length).append("\">").append(string2);
            ParsedValueImpl.indent();
            for (ParsedValueImpl[] parsedValueImplArray2 : parsedValueImplArray) {
                stringBuilder.append(ParsedValueImpl.spaces()).append("<layer>").append(string2);
                ParsedValueImpl.indent();
                if (parsedValueImplArray2 == null) {
                    stringBuilder.append(ParsedValueImpl.spaces()).append("null").append(string2);
                    continue;
                }
                for (ParsedValueImpl parsedValueImpl : parsedValueImplArray2) {
                    if (parsedValueImpl == null) {
                        stringBuilder.append(ParsedValueImpl.spaces()).append("null").append(string2);
                        continue;
                    }
                    stringBuilder.append(parsedValueImpl);
                }
                ParsedValueImpl.outdent();
                stringBuilder.append(ParsedValueImpl.spaces()).append("</layer>").append(string2);
            }
            ParsedValueImpl.outdent();
            stringBuilder.append(ParsedValueImpl.spaces()).append("</").append(string).append('>').append(string2);
        } else if (object instanceof ParsedValueImpl[]) {
            ParsedValueImpl[] parsedValueImplArray = (ParsedValueImpl[])object;
            stringBuilder.append(ParsedValueImpl.spaces()).append('<').append(string).append(" values=\"").append(parsedValueImplArray.length).append("\">").append(string2);
            ParsedValueImpl.indent();
            for (ParsedValueImpl parsedValueImpl : parsedValueImplArray) {
                if (parsedValueImpl == null) {
                    stringBuilder.append(ParsedValueImpl.spaces()).append("null").append(string2);
                    continue;
                }
                stringBuilder.append(parsedValueImpl);
            }
            ParsedValueImpl.outdent();
            stringBuilder.append(ParsedValueImpl.spaces()).append("</").append(string).append('>').append(string2);
        } else if (object instanceof ParsedValueImpl) {
            stringBuilder.append(ParsedValueImpl.spaces()).append('<').append(string).append('>').append(string2);
            ParsedValueImpl.indent();
            stringBuilder.append(object);
            ParsedValueImpl.outdent();
            stringBuilder.append(ParsedValueImpl.spaces()).append("</").append(string).append('>').append(string2);
        } else {
            stringBuilder.append(ParsedValueImpl.spaces()).append('<').append(string).append('>');
            stringBuilder.append(object);
            stringBuilder.append("</").append(string).append('>').append(string2);
        }
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object == null || object.getClass() != this.getClass()) {
            return false;
        }
        ParsedValueImpl parsedValueImpl = (ParsedValueImpl)object;
        if (this.hash != parsedValueImpl.hash) {
            return false;
        }
        if (this.value instanceof ParsedValueImpl[][]) {
            if (!(parsedValueImpl.value instanceof ParsedValueImpl[][])) {
                return false;
            }
            ParsedValueImpl[][] parsedValueImplArray = (ParsedValueImpl[][])this.value;
            ParsedValueImpl[][] parsedValueImplArray2 = (ParsedValueImpl[][])parsedValueImpl.value;
            if (parsedValueImplArray.length != parsedValueImplArray2.length) {
                return false;
            }
            for (int i = 0; i < parsedValueImplArray.length; ++i) {
                if (parsedValueImplArray[i] == null && parsedValueImplArray2[i] == null) continue;
                if (parsedValueImplArray[i] == null || parsedValueImplArray2[i] == null) {
                    return false;
                }
                if (parsedValueImplArray[i].length != parsedValueImplArray2[i].length) {
                    return false;
                }
                for (int j = 0; j < parsedValueImplArray[i].length; ++j) {
                    ParsedValueImpl parsedValueImpl2 = parsedValueImplArray[i][j];
                    ParsedValueImpl parsedValueImpl3 = parsedValueImplArray2[i][j];
                    if (!(parsedValueImpl2 != null ? !parsedValueImpl2.equals(parsedValueImpl3) : parsedValueImpl3 != null)) continue;
                    return false;
                }
            }
            return true;
        }
        if (this.value instanceof ParsedValueImpl[]) {
            if (!(parsedValueImpl.value instanceof ParsedValueImpl[])) {
                return false;
            }
            ParsedValueImpl[] parsedValueImplArray = (ParsedValueImpl[])this.value;
            ParsedValueImpl[] parsedValueImplArray3 = (ParsedValueImpl[])parsedValueImpl.value;
            if (parsedValueImplArray.length != parsedValueImplArray3.length) {
                return false;
            }
            for (int i = 0; i < parsedValueImplArray.length; ++i) {
                ParsedValueImpl parsedValueImpl4 = parsedValueImplArray[i];
                ParsedValueImpl parsedValueImpl5 = parsedValueImplArray3[i];
                if (!(parsedValueImpl4 != null ? !parsedValueImpl4.equals(parsedValueImpl5) : parsedValueImpl5 != null)) continue;
                return false;
            }
            return true;
        }
        if (this.value instanceof String && parsedValueImpl.value instanceof String) {
            return this.value.toString().equalsIgnoreCase(parsedValueImpl.value.toString());
        }
        return this.value != null ? this.value.equals(parsedValueImpl.value) : parsedValueImpl.value == null;
    }

    public int hashCode() {
        if (this.hash == Integer.MIN_VALUE) {
            this.hash = 17;
            if (this.value instanceof ParsedValueImpl[][]) {
                ParsedValueImpl[][] parsedValueImplArray = (ParsedValueImpl[][])this.value;
                for (int i = 0; i < parsedValueImplArray.length; ++i) {
                    for (int j = 0; j < parsedValueImplArray[i].length; ++j) {
                        ParsedValueImpl parsedValueImpl = parsedValueImplArray[i][j];
                        this.hash = 37 * this.hash + (parsedValueImpl != null && parsedValueImpl.value != null ? parsedValueImpl.value.hashCode() : 0);
                    }
                }
            } else if (this.value instanceof ParsedValueImpl[]) {
                ParsedValueImpl[] parsedValueImplArray = (ParsedValueImpl[])this.value;
                for (int i = 0; i < parsedValueImplArray.length; ++i) {
                    if (parsedValueImplArray[i] == null || parsedValueImplArray[i].value == null) continue;
                    ParsedValueImpl parsedValueImpl = parsedValueImplArray[i];
                    this.hash = 37 * this.hash + (parsedValueImpl != null && parsedValueImpl.value != null ? parsedValueImpl.value.hashCode() : 0);
                }
            } else {
                this.hash = 37 * this.hash + (this.value != null ? this.value.hashCode() : 0);
            }
        }
        return this.hash;
    }

    public final void writeBinary(DataOutputStream dataOutputStream, StyleConverter.StringStore stringStore) throws IOException {
        dataOutputStream.writeBoolean(this.lookup);
        if (this.converter != null) {
            dataOutputStream.writeBoolean(true);
            this.converter.writeBinary(dataOutputStream, stringStore);
        } else {
            dataOutputStream.writeBoolean(false);
        }
        if (this.value instanceof ParsedValue) {
            dataOutputStream.writeByte(1);
            ParsedValue parsedValue = (ParsedValue)this.value;
            if (parsedValue instanceof ParsedValueImpl) {
                ((ParsedValueImpl)parsedValue).writeBinary(dataOutputStream, stringStore);
            } else {
                ParsedValueImpl parsedValueImpl = new ParsedValueImpl(parsedValue.getValue(), parsedValue.getConverter());
                parsedValueImpl.writeBinary(dataOutputStream, stringStore);
            }
        } else if (this.value instanceof ParsedValue[]) {
            dataOutputStream.writeByte(2);
            ParsedValue[] parsedValueArray = (ParsedValue[])this.value;
            if (parsedValueArray != null) {
                dataOutputStream.writeByte(1);
            } else {
                dataOutputStream.writeByte(0);
            }
            int n = parsedValueArray != null ? parsedValueArray.length : 0;
            dataOutputStream.writeInt(n);
            for (int i = 0; i < n; ++i) {
                if (parsedValueArray[i] != null) {
                    dataOutputStream.writeByte(1);
                    ParsedValue parsedValue = parsedValueArray[i];
                    if (parsedValue instanceof ParsedValueImpl) {
                        ((ParsedValueImpl)parsedValue).writeBinary(dataOutputStream, stringStore);
                        continue;
                    }
                    ParsedValueImpl parsedValueImpl = new ParsedValueImpl(parsedValue.getValue(), parsedValue.getConverter());
                    parsedValueImpl.writeBinary(dataOutputStream, stringStore);
                    continue;
                }
                dataOutputStream.writeByte(0);
            }
        } else if (this.value instanceof ParsedValue[][]) {
            dataOutputStream.writeByte(3);
            ParsedValue[][] parsedValueArray = (ParsedValue[][])this.value;
            if (parsedValueArray != null) {
                dataOutputStream.writeByte(1);
            } else {
                dataOutputStream.writeByte(0);
            }
            int n = parsedValueArray != null ? parsedValueArray.length : 0;
            dataOutputStream.writeInt(n);
            for (int i = 0; i < n; ++i) {
                ParsedValue[] parsedValueArray2 = parsedValueArray[i];
                if (parsedValueArray2 != null) {
                    dataOutputStream.writeByte(1);
                } else {
                    dataOutputStream.writeByte(0);
                }
                int n2 = parsedValueArray2 != null ? parsedValueArray2.length : 0;
                dataOutputStream.writeInt(n2);
                for (int j = 0; j < n2; ++j) {
                    if (parsedValueArray2[j] != null) {
                        dataOutputStream.writeByte(1);
                        ParsedValue parsedValue = parsedValueArray2[j];
                        if (parsedValue instanceof ParsedValueImpl) {
                            ((ParsedValueImpl)parsedValue).writeBinary(dataOutputStream, stringStore);
                            continue;
                        }
                        ParsedValueImpl parsedValueImpl = new ParsedValueImpl(parsedValue.getValue(), parsedValue.getConverter());
                        parsedValueImpl.writeBinary(dataOutputStream, stringStore);
                        continue;
                    }
                    dataOutputStream.writeByte(0);
                }
            }
        } else if (this.value instanceof Color) {
            Color color = (Color)this.value;
            dataOutputStream.writeByte(5);
            dataOutputStream.writeLong(Double.doubleToLongBits(color.getRed()));
            dataOutputStream.writeLong(Double.doubleToLongBits(color.getGreen()));
            dataOutputStream.writeLong(Double.doubleToLongBits(color.getBlue()));
            dataOutputStream.writeLong(Double.doubleToLongBits(color.getOpacity()));
        } else if (this.value instanceof Enum) {
            Enum enum_ = (Enum)this.value;
            int n = stringStore.addString(enum_.name());
            dataOutputStream.writeByte(6);
            dataOutputStream.writeShort(n);
        } else if (this.value instanceof Boolean) {
            Boolean bl = (Boolean)this.value;
            dataOutputStream.writeByte(7);
            dataOutputStream.writeBoolean(bl);
        } else if (this.value instanceof Size) {
            Size size = (Size)this.value;
            dataOutputStream.writeByte(9);
            double d = size.getValue();
            long l = Double.doubleToLongBits(d);
            dataOutputStream.writeLong(l);
            int n = stringStore.addString(size.getUnits().name());
            dataOutputStream.writeShort(n);
        } else if (this.value instanceof String) {
            dataOutputStream.writeByte(4);
            int n = stringStore.addString((String)this.value);
            dataOutputStream.writeShort(n);
        } else if (this.value instanceof URL) {
            dataOutputStream.writeByte(8);
            int n = stringStore.addString(this.value.toString());
            dataOutputStream.writeShort(n);
        } else if (this.value == null) {
            dataOutputStream.writeByte(0);
        } else {
            throw new InternalError("cannot writeBinary " + this);
        }
    }

    public static ParsedValueImpl readBinary(int n, DataInputStream dataInputStream, String[] stringArray) throws IOException {
        boolean bl = dataInputStream.readBoolean();
        boolean bl2 = dataInputStream.readBoolean();
        StyleConverter<?, ?> styleConverter = bl2 ? StyleConverter.readBinary(dataInputStream, stringArray) : null;
        byte by = dataInputStream.readByte();
        if (by == 1) {
            ParsedValueImpl parsedValueImpl = ParsedValueImpl.readBinary(n, dataInputStream, stringArray);
            return new ParsedValueImpl(parsedValueImpl, styleConverter, bl);
        }
        if (by == 2) {
            int n2;
            if (n >= 4) {
                dataInputStream.readByte();
            }
            ParsedValueImpl[] parsedValueImplArray = (n2 = dataInputStream.readInt()) > 0 ? new ParsedValueImpl[n2] : null;
            for (int i = 0; i < n2; ++i) {
                byte by2 = dataInputStream.readByte();
                parsedValueImplArray[i] = by2 == 1 ? ParsedValueImpl.readBinary(n, dataInputStream, stringArray) : null;
            }
            return new ParsedValueImpl(parsedValueImplArray, styleConverter, bl);
        }
        if (by == 3) {
            int n3;
            if (n >= 4) {
                dataInputStream.readByte();
            }
            ParsedValueImpl[][] parsedValueImplArray = (n3 = dataInputStream.readInt()) > 0 ? new ParsedValueImpl[n3][0] : null;
            for (int i = 0; i < n3; ++i) {
                int n4;
                if (n >= 4) {
                    dataInputStream.readByte();
                }
                parsedValueImplArray[i] = (n4 = dataInputStream.readInt()) > 0 ? new ParsedValueImpl[n4] : null;
                for (int j = 0; j < n4; ++j) {
                    byte by3 = dataInputStream.readByte();
                    parsedValueImplArray[i][j] = by3 == 1 ? ParsedValueImpl.readBinary(n, dataInputStream, stringArray) : null;
                }
            }
            return new ParsedValueImpl(parsedValueImplArray, styleConverter, bl);
        }
        if (by == 5) {
            double d = Double.longBitsToDouble(dataInputStream.readLong());
            double d2 = Double.longBitsToDouble(dataInputStream.readLong());
            double d3 = Double.longBitsToDouble(dataInputStream.readLong());
            double d4 = Double.longBitsToDouble(dataInputStream.readLong());
            return new ParsedValueImpl(Color.color(d, d2, d3, d4), styleConverter, bl);
        }
        if (by == 6) {
            short s;
            short s2 = dataInputStream.readShort();
            String string = stringArray[s2];
            if (n == 2 && (s = dataInputStream.readShort()) >= stringArray.length) {
                throw new IllegalArgumentException("bad version " + n);
            }
            ParsedValueImpl parsedValueImpl = new ParsedValueImpl(string, styleConverter, bl);
            return parsedValueImpl;
        }
        if (by == 7) {
            Boolean bl3 = dataInputStream.readBoolean();
            return new ParsedValueImpl(bl3, styleConverter, bl);
        }
        if (by == 9) {
            double d = Double.longBitsToDouble(dataInputStream.readLong());
            SizeUnits sizeUnits = SizeUnits.PX;
            String string = stringArray[dataInputStream.readShort()];
            try {
                sizeUnits = Enum.valueOf(SizeUnits.class, string);
            }
            catch (IllegalArgumentException illegalArgumentException) {
                System.err.println(illegalArgumentException.toString());
            }
            catch (NullPointerException nullPointerException) {
                System.err.println(nullPointerException.toString());
            }
            return new ParsedValueImpl(new Size(d, sizeUnits), styleConverter, bl);
        }
        if (by == 4) {
            String string = stringArray[dataInputStream.readShort()];
            return new ParsedValueImpl(string, styleConverter, bl);
        }
        if (by == 8) {
            String string = stringArray[dataInputStream.readShort()];
            try {
                URL uRL = new URL(string);
                return new ParsedValueImpl(uRL, styleConverter, bl);
            }
            catch (MalformedURLException malformedURLException) {
                throw new InternalError("Exception in Value.readBinary: " + malformedURLException);
            }
        }
        if (by == 0) {
            return new ParsedValueImpl(null, styleConverter, bl);
        }
        throw new InternalError("unknown type: " + by);
    }
}

