/*
 * Decompiled with CFR 0.152.
 */
package javafx.util.converter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javafx.util.StringConverter;

public class DateTimeStringConverter
extends StringConverter<Date> {
    protected final Locale locale;
    protected final String pattern;
    protected final DateFormat dateFormat;
    protected final int dateStyle;
    protected final int timeStyle;

    public DateTimeStringConverter() {
        this(null, null, null, 2, 2);
    }

    public DateTimeStringConverter(int n, int n2) {
        this(null, null, null, n, n2);
    }

    public DateTimeStringConverter(Locale locale) {
        this(locale, null, null, 2, 2);
    }

    public DateTimeStringConverter(Locale locale, int n, int n2) {
        this(locale, null, null, n, n2);
    }

    public DateTimeStringConverter(String string) {
        this(null, string, null, 2, 2);
    }

    public DateTimeStringConverter(Locale locale, String string) {
        this(locale, string, null, 2, 2);
    }

    public DateTimeStringConverter(DateFormat dateFormat) {
        this(null, null, dateFormat, 2, 2);
    }

    DateTimeStringConverter(Locale locale, String string, DateFormat dateFormat, int n, int n2) {
        this.locale = locale != null ? locale : Locale.getDefault(Locale.Category.FORMAT);
        this.pattern = string;
        this.dateFormat = dateFormat;
        this.dateStyle = n;
        this.timeStyle = n2;
    }

    @Override
    public Date fromString(String string) {
        try {
            if (string == null) {
                return null;
            }
            if ((string = string.trim()).length() < 1) {
                return null;
            }
            DateFormat dateFormat = this.getDateFormat();
            return dateFormat.parse(string);
        }
        catch (ParseException parseException) {
            throw new RuntimeException(parseException);
        }
    }

    @Override
    public String toString(Date date) {
        if (date == null) {
            return "";
        }
        DateFormat dateFormat = this.getDateFormat();
        return dateFormat.format(date);
    }

    protected DateFormat getDateFormat() {
        DateFormat dateFormat = null;
        if (this.dateFormat != null) {
            return this.dateFormat;
        }
        dateFormat = this.pattern != null ? new SimpleDateFormat(this.pattern, this.locale) : DateFormat.getDateTimeInstance(this.dateStyle, this.timeStyle, this.locale);
        dateFormat.setLenient(false);
        return dateFormat;
    }
}

