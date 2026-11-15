/*
 * Decompiled with CFR 0.152.
 */
package javafx.util.converter;

import java.text.NumberFormat;
import java.util.Locale;
import javafx.util.converter.NumberStringConverter;

public class PercentageStringConverter
extends NumberStringConverter {
    public PercentageStringConverter() {
        this(Locale.getDefault());
    }

    public PercentageStringConverter(Locale locale) {
        super(locale, null, null);
    }

    public PercentageStringConverter(NumberFormat numberFormat) {
        super(null, null, numberFormat);
    }

    @Override
    public NumberFormat getNumberFormat() {
        Locale locale;
        Locale locale2 = locale = this.locale == null ? Locale.getDefault() : this.locale;
        if (this.numberFormat != null) {
            return this.numberFormat;
        }
        return NumberFormat.getPercentInstance(locale);
    }
}

