/*
 * Decompiled with CFR 0.152.
 */
package javafx.util.converter;

import java.math.BigInteger;
import javafx.util.StringConverter;

public class BigIntegerStringConverter
extends StringConverter<BigInteger> {
    @Override
    public BigInteger fromString(String string) {
        if (string == null) {
            return null;
        }
        if ((string = string.trim()).length() < 1) {
            return null;
        }
        return new BigInteger(string);
    }

    @Override
    public String toString(BigInteger bigInteger) {
        if (bigInteger == null) {
            return "";
        }
        return bigInteger.toString();
    }
}

