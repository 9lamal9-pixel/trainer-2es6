/*
 * Decompiled with CFR 0.152.
 */
package javafx.util.converter;

import java.math.BigDecimal;
import javafx.util.StringConverter;

public class BigDecimalStringConverter
extends StringConverter<BigDecimal> {
    @Override
    public BigDecimal fromString(String string) {
        if (string == null) {
            return null;
        }
        if ((string = string.trim()).length() < 1) {
            return null;
        }
        return new BigDecimal(string);
    }

    @Override
    public String toString(BigDecimal bigDecimal) {
        if (bigDecimal == null) {
            return "";
        }
        return bigDecimal.toString();
    }
}

