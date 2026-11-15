/*
 * Decompiled with CFR 0.152.
 */
package javafx.css.converter;

import com.sun.javafx.cursor.CursorType;
import com.sun.javafx.logging.PlatformLogger;
import com.sun.javafx.util.Logging;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javafx.css.ParsedValue;
import javafx.css.StyleConverter;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.geometry.VPos;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.BlurType;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.paint.CycleMethod;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public final class EnumConverter<E extends Enum<E>>
extends StyleConverter<String, E> {
    final Class<E> enumClass;
    private static Map<String, StyleConverter<?, ?>> converters;

    public EnumConverter(Class<E> clazz) {
        this.enumClass = clazz;
    }

    @Override
    public E convert(ParsedValue<String, E> parsedValue, Font font) {
        if (this.enumClass == null) {
            return null;
        }
        String string = parsedValue.getValue();
        int n = string.lastIndexOf(46);
        if (n > -1) {
            string = string.substring(n + 1);
        }
        try {
            string = string.replace('-', '_');
            return Enum.valueOf(this.enumClass, string.toUpperCase(Locale.ROOT));
        }
        catch (IllegalArgumentException illegalArgumentException) {
            return Enum.valueOf(this.enumClass, string);
        }
    }

    @Override
    public void writeBinary(DataOutputStream dataOutputStream, StyleConverter.StringStore stringStore) throws IOException {
        super.writeBinary(dataOutputStream, stringStore);
        String string = this.enumClass.getName();
        int n = stringStore.addString(string);
        dataOutputStream.writeShort(n);
    }

    public static StyleConverter<?, ?> readBinary(DataInputStream dataInputStream, String[] stringArray) throws IOException {
        String string;
        short s = dataInputStream.readShort();
        String string2 = string = 0 <= s && s <= stringArray.length ? stringArray[s] : null;
        if (string == null || string.isEmpty()) {
            return null;
        }
        if (converters == null || !converters.containsKey(string)) {
            PlatformLogger platformLogger;
            StyleConverter<?, ?> styleConverter = EnumConverter.getInstance(string);
            if (styleConverter == null && (platformLogger = Logging.getCSSLogger()).isLoggable(PlatformLogger.Level.SEVERE)) {
                platformLogger.severe("could not deserialize EnumConverter for " + string);
            }
            if (converters == null) {
                converters = new HashMap();
            }
            converters.put(string, styleConverter);
            return styleConverter;
        }
        return converters.get(string);
    }

    public static StyleConverter<?, ?> getInstance(String string) {
        EnumConverter<Enum> enumConverter = null;
        switch (string) {
            case "com.sun.javafx.cursor.CursorType": {
                enumConverter = new EnumConverter<CursorType>(CursorType.class);
                break;
            }
            case "javafx.scene.layout.BackgroundRepeat": 
            case "com.sun.javafx.scene.layout.region.Repeat": {
                enumConverter = new EnumConverter<BackgroundRepeat>(BackgroundRepeat.class);
                break;
            }
            case "javafx.geometry.HPos": {
                enumConverter = new EnumConverter<HPos>(HPos.class);
                break;
            }
            case "javafx.geometry.Orientation": {
                enumConverter = new EnumConverter<Orientation>(Orientation.class);
                break;
            }
            case "javafx.geometry.Pos": {
                enumConverter = new EnumConverter<Pos>(Pos.class);
                break;
            }
            case "javafx.geometry.Side": {
                enumConverter = new EnumConverter<Side>(Side.class);
                break;
            }
            case "javafx.geometry.VPos": {
                enumConverter = new EnumConverter<VPos>(VPos.class);
                break;
            }
            case "javafx.scene.effect.BlendMode": {
                enumConverter = new EnumConverter<BlendMode>(BlendMode.class);
                break;
            }
            case "javafx.scene.effect.BlurType": {
                enumConverter = new EnumConverter<BlurType>(BlurType.class);
                break;
            }
            case "javafx.scene.paint.CycleMethod": {
                enumConverter = new EnumConverter<CycleMethod>(CycleMethod.class);
                break;
            }
            case "javafx.scene.shape.ArcType": {
                enumConverter = new EnumConverter<ArcType>(ArcType.class);
                break;
            }
            case "javafx.scene.shape.StrokeLineCap": {
                enumConverter = new EnumConverter<StrokeLineCap>(StrokeLineCap.class);
                break;
            }
            case "javafx.scene.shape.StrokeLineJoin": {
                enumConverter = new EnumConverter<StrokeLineJoin>(StrokeLineJoin.class);
                break;
            }
            case "javafx.scene.shape.StrokeType": {
                enumConverter = new EnumConverter<StrokeType>(StrokeType.class);
                break;
            }
            case "javafx.scene.text.FontPosture": {
                enumConverter = new EnumConverter<FontPosture>(FontPosture.class);
                break;
            }
            case "javafx.scene.text.FontSmoothingType": {
                enumConverter = new EnumConverter<FontSmoothingType>(FontSmoothingType.class);
                break;
            }
            case "javafx.scene.text.FontWeight": {
                enumConverter = new EnumConverter<FontWeight>(FontWeight.class);
                break;
            }
            case "javafx.scene.text.TextAlignment": {
                enumConverter = new EnumConverter<TextAlignment>(TextAlignment.class);
                break;
            }
            default: {
                assert (false) : "EnumConverter<" + string + "> not expected";
                PlatformLogger platformLogger = Logging.getCSSLogger();
                if (!platformLogger.isLoggable(PlatformLogger.Level.SEVERE)) break;
                platformLogger.severe("EnumConverter : converter Class is null for : " + string);
            }
        }
        return enumConverter;
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object == null || !(object instanceof EnumConverter)) {
            return false;
        }
        return this.enumClass.equals(((EnumConverter)object).enumClass);
    }

    public int hashCode() {
        return this.enumClass.hashCode();
    }

    public String toString() {
        return "EnumConveter[" + this.enumClass.getName() + "]";
    }
}

