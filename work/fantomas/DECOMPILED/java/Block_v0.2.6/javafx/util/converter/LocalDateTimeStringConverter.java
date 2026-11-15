/*
 * Decompiled with CFR 0.152.
 */
package javafx.util.converter;

import com.sun.javafx.binding.Logging;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DecimalStyle;
import java.time.format.FormatStyle;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;
import javafx.util.StringConverter;

public class LocalDateTimeStringConverter
extends StringConverter<LocalDateTime> {
    LdtConverter<LocalDateTime> ldtConverter;

    public LocalDateTimeStringConverter() {
        this.ldtConverter = new LdtConverter<LocalDateTime>(LocalDateTime.class, null, null, null, null, null, null);
    }

    public LocalDateTimeStringConverter(FormatStyle formatStyle, FormatStyle formatStyle2) {
        this.ldtConverter = new LdtConverter<LocalDateTime>(LocalDateTime.class, null, null, formatStyle, formatStyle2, null, null);
    }

    public LocalDateTimeStringConverter(DateTimeFormatter dateTimeFormatter, DateTimeFormatter dateTimeFormatter2) {
        this.ldtConverter = new LdtConverter<LocalDateTime>(LocalDateTime.class, dateTimeFormatter, dateTimeFormatter2, null, null, null, null);
    }

    public LocalDateTimeStringConverter(FormatStyle formatStyle, FormatStyle formatStyle2, Locale locale, Chronology chronology) {
        this.ldtConverter = new LdtConverter<LocalDateTime>(LocalDateTime.class, null, null, formatStyle, formatStyle2, locale, chronology);
    }

    @Override
    public LocalDateTime fromString(String string) {
        return (LocalDateTime)this.ldtConverter.fromString(string);
    }

    @Override
    public String toString(LocalDateTime localDateTime) {
        return this.ldtConverter.toString(localDateTime);
    }

    static class LdtConverter<T extends Temporal>
    extends StringConverter<T> {
        private Class<T> type;
        Locale locale;
        Chronology chronology;
        DateTimeFormatter formatter;
        DateTimeFormatter parser;
        FormatStyle dateStyle;
        FormatStyle timeStyle;

        LdtConverter(Class<T> clazz, DateTimeFormatter dateTimeFormatter, DateTimeFormatter dateTimeFormatter2, FormatStyle formatStyle, FormatStyle formatStyle2, Locale locale, Chronology chronology) {
            this.type = clazz;
            this.formatter = dateTimeFormatter;
            this.parser = dateTimeFormatter2 != null ? dateTimeFormatter2 : dateTimeFormatter;
            this.locale = locale != null ? locale : Locale.getDefault(Locale.Category.FORMAT);
            Chronology chronology2 = this.chronology = chronology != null ? chronology : IsoChronology.INSTANCE;
            if (clazz == LocalDate.class || clazz == LocalDateTime.class) {
                FormatStyle formatStyle3 = this.dateStyle = formatStyle != null ? formatStyle : FormatStyle.SHORT;
            }
            if (clazz == LocalTime.class || clazz == LocalDateTime.class) {
                this.timeStyle = formatStyle2 != null ? formatStyle2 : FormatStyle.SHORT;
            }
        }

        @Override
        public T fromString(String string) {
            if (string == null || string.isEmpty()) {
                return null;
            }
            string = string.trim();
            if (this.parser == null) {
                this.parser = this.getDefaultParser();
            }
            TemporalAccessor temporalAccessor = this.parser.parse(string);
            if (this.type == LocalDate.class) {
                return (T)LocalDate.from(this.chronology.date(temporalAccessor));
            }
            if (this.type == LocalTime.class) {
                return (T)LocalTime.from(temporalAccessor);
            }
            return (T)LocalDateTime.from(this.chronology.localDateTime(temporalAccessor));
        }

        @Override
        public String toString(T t) {
            if (t == null) {
                return "";
            }
            if (this.formatter == null) {
                this.formatter = this.getDefaultFormatter();
            }
            if (t instanceof LocalDate) {
                ChronoLocalDate chronoLocalDate;
                try {
                    chronoLocalDate = this.chronology.date((TemporalAccessor)t);
                }
                catch (DateTimeException dateTimeException) {
                    Logging.getLogger().warning("Converting LocalDate " + t + " to " + this.chronology + " failed, falling back to IsoChronology.", dateTimeException);
                    this.chronology = IsoChronology.INSTANCE;
                    chronoLocalDate = (LocalDate)t;
                }
                return this.formatter.format(chronoLocalDate);
            }
            if (t instanceof LocalDateTime) {
                LocalDateTime localDateTime;
                try {
                    localDateTime = this.chronology.localDateTime((TemporalAccessor)t);
                }
                catch (DateTimeException dateTimeException) {
                    Logging.getLogger().warning("Converting LocalDateTime " + t + " to " + this.chronology + " failed, falling back to IsoChronology.", dateTimeException);
                    this.chronology = IsoChronology.INSTANCE;
                    localDateTime = (LocalDateTime)t;
                }
                return this.formatter.format(localDateTime);
            }
            return this.formatter.format((TemporalAccessor)t);
        }

        private DateTimeFormatter getDefaultParser() {
            String string = DateTimeFormatterBuilder.getLocalizedDateTimePattern(this.dateStyle, this.timeStyle, this.chronology, this.locale);
            return new DateTimeFormatterBuilder().parseLenient().appendPattern(string).toFormatter().withChronology(this.chronology).withDecimalStyle(DecimalStyle.of(this.locale));
        }

        private DateTimeFormatter getDefaultFormatter() {
            DateTimeFormatter dateTimeFormatter = this.dateStyle != null && this.timeStyle != null ? DateTimeFormatter.ofLocalizedDateTime(this.dateStyle, this.timeStyle) : (this.dateStyle != null ? DateTimeFormatter.ofLocalizedDate(this.dateStyle) : DateTimeFormatter.ofLocalizedTime(this.timeStyle));
            dateTimeFormatter = dateTimeFormatter.withLocale(this.locale).withChronology(this.chronology).withDecimalStyle(DecimalStyle.of(this.locale));
            if (this.dateStyle != null) {
                dateTimeFormatter = this.fixFourDigitYear(dateTimeFormatter, this.dateStyle, this.timeStyle, this.chronology, this.locale);
            }
            return dateTimeFormatter;
        }

        private DateTimeFormatter fixFourDigitYear(DateTimeFormatter dateTimeFormatter, FormatStyle formatStyle, FormatStyle formatStyle2, Chronology chronology, Locale locale) {
            String string = DateTimeFormatterBuilder.getLocalizedDateTimePattern(formatStyle, formatStyle2, chronology, locale);
            if (string.contains("yy") && !string.contains("yyy")) {
                String string2 = string.replace("yy", "yyyy");
                dateTimeFormatter = DateTimeFormatter.ofPattern(string2).withDecimalStyle(DecimalStyle.of(locale));
            }
            return dateTimeFormatter;
        }
    }
}

