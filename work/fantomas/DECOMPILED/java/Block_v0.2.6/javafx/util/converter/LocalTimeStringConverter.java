/*
 * Decompiled with CFR 0.152.
 */
package javafx.util.converter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import javafx.util.StringConverter;
import javafx.util.converter.LocalDateTimeStringConverter;

public class LocalTimeStringConverter
extends StringConverter<LocalTime> {
    LocalDateTimeStringConverter.LdtConverter<LocalTime> ldtConverter;

    public LocalTimeStringConverter() {
        this.ldtConverter = new LocalDateTimeStringConverter.LdtConverter<LocalTime>(LocalTime.class, null, null, null, null, null, null);
    }

    public LocalTimeStringConverter(FormatStyle formatStyle) {
        this.ldtConverter = new LocalDateTimeStringConverter.LdtConverter<LocalTime>(LocalTime.class, null, null, null, formatStyle, null, null);
    }

    public LocalTimeStringConverter(FormatStyle formatStyle, Locale locale) {
        this.ldtConverter = new LocalDateTimeStringConverter.LdtConverter<LocalTime>(LocalTime.class, null, null, null, formatStyle, locale, null);
    }

    public LocalTimeStringConverter(DateTimeFormatter dateTimeFormatter, DateTimeFormatter dateTimeFormatter2) {
        this.ldtConverter = new LocalDateTimeStringConverter.LdtConverter<LocalTime>(LocalTime.class, dateTimeFormatter, dateTimeFormatter2, null, null, null, null);
    }

    @Override
    public LocalTime fromString(String string) {
        return (LocalTime)this.ldtConverter.fromString(string);
    }

    @Override
    public String toString(LocalTime localTime) {
        return this.ldtConverter.toString(localTime);
    }
}

