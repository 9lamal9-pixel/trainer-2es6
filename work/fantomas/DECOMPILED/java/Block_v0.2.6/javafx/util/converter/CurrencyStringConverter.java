/*
 * Decompiled with CFR 0.152.
 */
package javafx.util.converter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import javafx.util.converter.NumberStringConverter;

public class CurrencyStringConverter
extends NumberStringConverter {
    public CurrencyStringConverter() {
        this(Locale.getDefault());
    }

    public CurrencyStringConverter(Locale locale) {
        this(locale, null);
    }

    public CurrencyStringConverter(String string) {
        this(Locale.getDefault(), string);
    }

    public CurrencyStringConverter(Locale locale, String string) {
        super(locale, string, null);
    }

    public CurrencyStringConverter(NumberFormat numberFormat) {
        super(null, null, numberFormat);
    }

    @Override
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
        return NumberFormat.getCurrencyInstance(locale);
    }
}

