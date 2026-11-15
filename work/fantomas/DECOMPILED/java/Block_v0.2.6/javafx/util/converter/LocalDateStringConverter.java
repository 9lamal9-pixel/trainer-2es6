/*
 * Decompiled with CFR 0.152.
 */
package javafx.util.converter;

import java.time.LocalDate;
import java.time.chrono.Chronology;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import javafx.util.StringConverter;
import javafx.util.converter.LocalDateTimeStringConverter;

public class LocalDateStringConverter
extends StringConverter<LocalDate> {
    LocalDateTimeStringConverter.LdtConverter<LocalDate> ldtConverter;

    public LocalDateStringConverter() {
        this.ldtConverter = new LocalDateTimeStringConverter.LdtConverter<LocalDate>(LocalDate.class, null, null, null, null, null, null);
    }

    public LocalDateStringConverter(FormatStyle formatStyle) {
        this.ldtConverter = new LocalDateTimeStringConverter.LdtConverter<LocalDate>(LocalDate.class, null, null, formatStyle, null, null, null);
    }

    public LocalDateStringConverter(DateTimeFormatter dateTimeFormatter, DateTimeFormatter dateTimeFormatter2) {
        this.ldtConverter = new LocalDateTimeStringConverter.LdtConverter<LocalDate>(LocalDate.class, dateTimeFormatter, dateTimeFormatter2, null, null, null, null);
    }

    public LocalDateStringConverter(FormatStyle formatStyle, Locale locale, Chronology chronology) {
        this.ldtConverter = new LocalDateTimeStringConverter.LdtConverter<LocalDate>(LocalDate.class, null, null, formatStyle, null, locale, chronology);
    }

    @Override
    public LocalDate fromString(String string) {
        return (LocalDate)this.ldtConverter.fromString(string);
    }

    @Override
    public String toString(LocalDate localDate) {
        return this.ldtConverter.toString(localDate);
    }
}

