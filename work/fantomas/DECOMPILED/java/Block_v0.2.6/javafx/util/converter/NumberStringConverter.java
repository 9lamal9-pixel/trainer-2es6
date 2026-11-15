/*
 * Decompiled with CFR 0.152.
 */
package javafx.util.converter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import javafx.util.StringConverter;

public class NumberStringConverter
extends StringConverter<Number> {
    final Locale locale;
    final String pattern;
    final NumberFormat numberFormat;

    public NumberStringConverter() {
        this(Locale.getDefault());
    }

    public NumberStringConverter(Locale locale) {
        this(locale, null);
    }

    public NumberStringConverter(String string) {
        this(Locale.getDefault(), string);
    }

    public NumberStringConverter(Locale locale, String string) {
        this(locale, string, null);
    }

    public NumberStringConverter(NumberFormat numberFormat) {
        this(null, null, numberFormat);
    }

    NumberStringConverter(Locale locale, String string, NumberFormat numberFormat) {
        this.locale = locale;
        this.pattern = string;
        this.numberFormat = numberFormat;
    }

    @Override
    public Number fromString(String string) {
        try {
            if (string == null) {
                return null;
            }
            if ((string = string.trim()).length() < 1) {
                return null;
            }
            NumberFormat numberFormat = this.getNumberFormat();
            return numberFormat.parse(string);
        }
        catch (ParseException parseException) {
            throw new RuntimeException(parseException);
        }
    }

    @Override
    public String toString(Number number) {
        if (number == null) {
            return "";
        }
        NumberFormat numberFormat = this.getNumberFormat();
        return numberFormat.format(number);
    }

    protected NumberFormat getNumberFormat() {
        Locale locale;
        Locale locale2 = locale = this.locale == null ? Locale.getDefault() : this.locale;
        if (this.numberFormat != null) {
            return this.numberFormat;
        }
        if (this.pattern != null) {
            DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(locale);
            return new DecimalFormat(this.pattern, decimalFormatSymbols);
        }
        return NumberFormat.getNumberInstance(locale);
    }
}

