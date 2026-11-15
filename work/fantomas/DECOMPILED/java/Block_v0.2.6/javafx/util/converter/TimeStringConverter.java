/*
 * Decompiled with CFR 0.152.
 */
package javafx.util.converter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import javafx.util.converter.DateTimeStringConverter;

public class TimeStringConverter
extends DateTimeStringConverter {
    public TimeStringConverter() {
        this(null, null, null, 2);
    }

    public TimeStringConverter(int n) {
        this(null, null, null, n);
    }

    public TimeStringConverter(Locale locale) {
        this(locale, null, null, 2);
    }

    public TimeStringConverter(Locale locale, int n) {
        this(locale, null, null, n);
    }

    public TimeStringConverter(String string) {
        this(null, string, null, 2);
    }

    public TimeStringConverter(Locale locale, String string) {
        this(locale, string, null, 2);
    }

    public TimeStringConverter(DateFormat dateFormat) {
        this(null, null, dateFormat, 2);
    }

    private TimeStringConverter(Locale locale, String string, DateFormat dateFormat, int n) {
        super(locale, string, dateFormat, 2, n);
    }

    @Override
    protected DateFormat getDateFormat() {
        DateFormat dateFormat = null;
        if (this.dateFormat != null) {
            return this.dateFormat;
        }
        dateFormat = this.pattern != null ? new SimpleDateFormat(this.pattern, this.locale) : DateFormat.getTimeInstance(this.timeStyle, this.locale);
        dateFormat.setLenient(false);
        return dateFormat;
    }
}

