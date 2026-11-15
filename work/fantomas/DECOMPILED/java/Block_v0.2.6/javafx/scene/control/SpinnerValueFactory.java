/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import javafx.beans.NamedArg;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.scene.control.Spinner;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;

public abstract class SpinnerValueFactory<T> {
    private ObjectProperty<T> value = new SimpleObjectProperty(this, "value");
    private ObjectProperty<StringConverter<T>> converter = new SimpleObjectProperty<StringConverter<T>>(this, "converter");
    private BooleanProperty wrapAround;

    public abstract void decrement(int var1);

    public abstract void increment(int var1);

    public final T getValue() {
        return this.value.get();
    }

    public final void setValue(T t) {
        this.value.set(t);
    }

    public final ObjectProperty<T> valueProperty() {
        return this.value;
    }

    public final StringConverter<T> getConverter() {
        return (StringConverter)this.converter.get();
    }

    public final void setConverter(StringConverter<T> stringConverter) {
        this.converter.set(stringConverter);
    }

    public final ObjectProperty<StringConverter<T>> converterProperty() {
        return this.converter;
    }

    public final void setWrapAround(boolean bl) {
        this.wrapAroundProperty().set(bl);
    }

    public final boolean isWrapAround() {
        return this.wrapAround == null ? false : this.wrapAround.get();
    }

    public final BooleanProperty wrapAroundProperty() {
        if (this.wrapAround == null) {
            this.wrapAround = new SimpleBooleanProperty(this, "wrapAround", false);
        }
        return this.wrapAround;
    }

    static class LocalTimeSpinnerValueFactory
    extends SpinnerValueFactory<LocalTime> {
        private ObjectProperty<LocalTime> min = new SimpleObjectProperty<LocalTime>((Object)this, "min"){

            @Override
            protected void invalidated() {
                LocalTime localTime = (LocalTime)this.getValue();
                if (localTime == null) {
                    return;
                }
                LocalTime localTime2 = (LocalTime)this.get();
                if (localTime2.isAfter(this.getMax())) {
                    this.setMin(this.getMax());
                    return;
                }
                if (localTime.isBefore(localTime2)) {
                    this.setValue(localTime2);
                }
            }
        };
        private ObjectProperty<LocalTime> max = new SimpleObjectProperty<LocalTime>((Object)this, "max"){

            @Override
            protected void invalidated() {
                LocalTime localTime = (LocalTime)this.getValue();
                if (localTime == null) {
                    return;
                }
                LocalTime localTime2 = (LocalTime)this.get();
                if (localTime2.isBefore(this.getMin())) {
                    this.setMax(this.getMin());
                    return;
                }
                if (localTime.isAfter(localTime2)) {
                    this.setValue(localTime2);
                }
            }
        };
        private ObjectProperty<TemporalUnit> temporalUnit = new SimpleObjectProperty<TemporalUnit>(this, "temporalUnit");
        private LongProperty amountToStepBy = new SimpleLongProperty(this, "amountToStepBy");

        public LocalTimeSpinnerValueFactory() {
            this(LocalTime.now());
        }

        public LocalTimeSpinnerValueFactory(@NamedArg(value="initialValue") LocalTime localTime) {
            this(LocalTime.MIN, LocalTime.MAX, localTime);
        }

        public LocalTimeSpinnerValueFactory(@NamedArg(value="min") LocalTime localTime, @NamedArg(value="min") LocalTime localTime2, @NamedArg(value="initialValue") LocalTime localTime3) {
            this(localTime, localTime2, localTime3, 1L, ChronoUnit.HOURS);
        }

        public LocalTimeSpinnerValueFactory(@NamedArg(value="min") LocalTime localTime3, @NamedArg(value="min") LocalTime localTime4, @NamedArg(value="initialValue") LocalTime localTime5, @NamedArg(value="amountToStepBy") long l, @NamedArg(value="temporalUnit") TemporalUnit temporalUnit) {
            this.setMin(localTime3);
            this.setMax(localTime4);
            this.setAmountToStepBy(l);
            this.setTemporalUnit(temporalUnit);
            this.setConverter(new StringConverter<LocalTime>(){
                private DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);

                @Override
                public String toString(LocalTime localTime) {
                    if (localTime == null) {
                        return "";
                    }
                    return localTime.format(this.dtf);
                }

                @Override
                public LocalTime fromString(String string) {
                    return LocalTime.parse(string);
                }
            });
            this.valueProperty().addListener((observableValue, localTime, localTime2) -> {
                if (this.getMin() != null && localTime2.isBefore(this.getMin())) {
                    this.setValue(this.getMin());
                } else if (this.getMax() != null && localTime2.isAfter(this.getMax())) {
                    this.setValue(this.getMax());
                }
            });
            this.setValue(localTime5 != null ? localTime5 : LocalTime.now());
        }

        public final void setMin(LocalTime localTime) {
            this.min.set(localTime);
        }

        public final LocalTime getMin() {
            return (LocalTime)this.min.get();
        }

        public final ObjectProperty<LocalTime> minProperty() {
            return this.min;
        }

        public final void setMax(LocalTime localTime) {
            this.max.set(localTime);
        }

        public final LocalTime getMax() {
            return (LocalTime)this.max.get();
        }

        public final ObjectProperty<LocalTime> maxProperty() {
            return this.max;
        }

        public final void setTemporalUnit(TemporalUnit temporalUnit) {
            this.temporalUnit.set(temporalUnit);
        }

        public final TemporalUnit getTemporalUnit() {
            return (TemporalUnit)this.temporalUnit.get();
        }

        public final ObjectProperty<TemporalUnit> temporalUnitProperty() {
            return this.temporalUnit;
        }

        public final void setAmountToStepBy(long l) {
            this.amountToStepBy.set(l);
        }

        public final long getAmountToStepBy() {
            return this.amountToStepBy.get();
        }

        public final LongProperty amountToStepByProperty() {
            return this.amountToStepBy;
        }

        @Override
        public void decrement(int n) {
            LocalTime localTime = (LocalTime)this.getValue();
            LocalTime localTime2 = this.getMin();
            Duration duration = Duration.of(this.getAmountToStepBy() * (long)n, this.getTemporalUnit());
            long l = duration.toMinutes() * 60L;
            long l2 = localTime.toSecondOfDay();
            if (!this.isWrapAround() && l > l2) {
                this.setValue(localTime2 == null ? LocalTime.MIN : localTime2);
            } else {
                this.setValue(localTime.minus(duration));
            }
        }

        @Override
        public void increment(int n) {
            LocalTime localTime = (LocalTime)this.getValue();
            LocalTime localTime2 = this.getMax();
            Duration duration = Duration.of(this.getAmountToStepBy() * (long)n, this.getTemporalUnit());
            long l = duration.toMinutes() * 60L;
            long l2 = localTime.toSecondOfDay();
            if (!this.isWrapAround() && l > (long)LocalTime.MAX.toSecondOfDay() - l2) {
                this.setValue(localTime2 == null ? LocalTime.MAX : localTime2);
            } else {
                this.setValue(localTime.plus(duration));
            }
        }
    }

    static class LocalDateSpinnerValueFactory
    extends SpinnerValueFactory<LocalDate> {
        private ObjectProperty<LocalDate> min = new SimpleObjectProperty<LocalDate>((Object)this, "min"){

            @Override
            protected void invalidated() {
                LocalDate localDate = (LocalDate)this.getValue();
                if (localDate == null) {
                    return;
                }
                LocalDate localDate2 = (LocalDate)this.get();
                if (localDate2.isAfter(this.getMax())) {
                    this.setMin(this.getMax());
                    return;
                }
                if (localDate.isBefore(localDate2)) {
                    this.setValue(localDate2);
                }
            }
        };
        private ObjectProperty<LocalDate> max = new SimpleObjectProperty<LocalDate>((Object)this, "max"){

            @Override
            protected void invalidated() {
                LocalDate localDate = (LocalDate)this.getValue();
                if (localDate == null) {
                    return;
                }
                LocalDate localDate2 = (LocalDate)this.get();
                if (localDate2.isBefore(this.getMin())) {
                    this.setMax(this.getMin());
                    return;
                }
                if (localDate.isAfter(localDate2)) {
                    this.setValue(localDate2);
                }
            }
        };
        private ObjectProperty<TemporalUnit> temporalUnit = new SimpleObjectProperty<TemporalUnit>(this, "temporalUnit");
        private LongProperty amountToStepBy = new SimpleLongProperty(this, "amountToStepBy");

        public LocalDateSpinnerValueFactory() {
            this(LocalDate.now());
        }

        public LocalDateSpinnerValueFactory(@NamedArg(value="initialValue") LocalDate localDate) {
            this(LocalDate.MIN, LocalDate.MAX, localDate);
        }

        public LocalDateSpinnerValueFactory(@NamedArg(value="min") LocalDate localDate, @NamedArg(value="min") LocalDate localDate2, @NamedArg(value="initialValue") LocalDate localDate3) {
            this(localDate, localDate2, localDate3, 1L, ChronoUnit.DAYS);
        }

        public LocalDateSpinnerValueFactory(@NamedArg(value="min") LocalDate localDate3, @NamedArg(value="min") LocalDate localDate4, @NamedArg(value="initialValue") LocalDate localDate5, @NamedArg(value="amountToStepBy") long l, @NamedArg(value="temporalUnit") TemporalUnit temporalUnit) {
            this.setMin(localDate3);
            this.setMax(localDate4);
            this.setAmountToStepBy(l);
            this.setTemporalUnit(temporalUnit);
            this.setConverter(new StringConverter<LocalDate>(){

                @Override
                public String toString(LocalDate localDate) {
                    if (localDate == null) {
                        return "";
                    }
                    return localDate.toString();
                }

                @Override
                public LocalDate fromString(String string) {
                    return LocalDate.parse(string);
                }
            });
            this.valueProperty().addListener((observableValue, localDate, localDate2) -> {
                if (this.getMin() != null && localDate2.isBefore(this.getMin())) {
                    this.setValue(this.getMin());
                } else if (this.getMax() != null && localDate2.isAfter(this.getMax())) {
                    this.setValue(this.getMax());
                }
            });
            this.setValue(localDate5 != null ? localDate5 : LocalDate.now());
        }

        public final void setMin(LocalDate localDate) {
            this.min.set(localDate);
        }

        public final LocalDate getMin() {
            return (LocalDate)this.min.get();
        }

        public final ObjectProperty<LocalDate> minProperty() {
            return this.min;
        }

        public final void setMax(LocalDate localDate) {
            this.max.set(localDate);
        }

        public final LocalDate getMax() {
            return (LocalDate)this.max.get();
        }

        public final ObjectProperty<LocalDate> maxProperty() {
            return this.max;
        }

        public final void setTemporalUnit(TemporalUnit temporalUnit) {
            this.temporalUnit.set(temporalUnit);
        }

        public final TemporalUnit getTemporalUnit() {
            return (TemporalUnit)this.temporalUnit.get();
        }

        public final ObjectProperty<TemporalUnit> temporalUnitProperty() {
            return this.temporalUnit;
        }

        public final void setAmountToStepBy(long l) {
            this.amountToStepBy.set(l);
        }

        public final long getAmountToStepBy() {
            return this.amountToStepBy.get();
        }

        public final LongProperty amountToStepByProperty() {
            return this.amountToStepBy;
        }

        @Override
        public void decrement(int n) {
            LocalDate localDate = (LocalDate)this.getValue();
            LocalDate localDate2 = this.getMin();
            LocalDate localDate3 = localDate.minus(this.getAmountToStepBy() * (long)n, this.getTemporalUnit());
            if (localDate2 != null && this.isWrapAround() && localDate3.isBefore(localDate2)) {
                localDate3 = this.getMax();
            }
            this.setValue(localDate3);
        }

        @Override
        public void increment(int n) {
            LocalDate localDate = (LocalDate)this.getValue();
            LocalDate localDate2 = this.getMax();
            LocalDate localDate3 = localDate.plus(this.getAmountToStepBy() * (long)n, this.getTemporalUnit());
            if (localDate2 != null && this.isWrapAround() && localDate3.isAfter(localDate2)) {
                localDate3 = this.getMin();
            }
            this.setValue(localDate3);
        }
    }

    public static class DoubleSpinnerValueFactory
    extends SpinnerValueFactory<Double> {
        private DoubleProperty min = new SimpleDoubleProperty(this, "min"){

            @Override
            protected void invalidated() {
                Double d = (Double)this.getValue();
                if (d == null) {
                    return;
                }
                double d2 = this.get();
                if (d2 > this.getMax()) {
                    this.setMin(this.getMax());
                    return;
                }
                if (d < d2) {
                    this.setValue(d2);
                }
            }
        };
        private DoubleProperty max = new SimpleDoubleProperty(this, "max"){

            @Override
            protected void invalidated() {
                Double d = (Double)this.getValue();
                if (d == null) {
                    return;
                }
                double d2 = this.get();
                if (d2 < this.getMin()) {
                    this.setMax(this.getMin());
                    return;
                }
                if (d > d2) {
                    this.setValue(d2);
                }
            }
        };
        private DoubleProperty amountToStepBy = new SimpleDoubleProperty(this, "amountToStepBy");

        public DoubleSpinnerValueFactory(@NamedArg(value="min") double d, @NamedArg(value="max") double d2) {
            this(d, d2, d);
        }

        public DoubleSpinnerValueFactory(@NamedArg(value="min") double d, @NamedArg(value="max") double d2, @NamedArg(value="initialValue") double d3) {
            this(d, d2, d3, 1.0);
        }

        public DoubleSpinnerValueFactory(@NamedArg(value="min") double d3, @NamedArg(value="max") double d4, @NamedArg(value="initialValue") double d5, @NamedArg(value="amountToStepBy") double d6) {
            this.setMin(d3);
            this.setMax(d4);
            this.setAmountToStepBy(d6);
            this.setConverter(new StringConverter<Double>(){
                private final DecimalFormat df = new DecimalFormat("#.##");

                @Override
                public String toString(Double d) {
                    if (d == null) {
                        return "";
                    }
                    return this.df.format(d);
                }

                @Override
                public Double fromString(String string) {
                    try {
                        if (string == null) {
                            return null;
                        }
                        if ((string = string.trim()).length() < 1) {
                            return null;
                        }
                        return this.df.parse(string).doubleValue();
                    }
                    catch (ParseException parseException) {
                        throw new RuntimeException(parseException);
                    }
                }
            });
            this.valueProperty().addListener((observableValue, d, d2) -> {
                if (d2 == null) {
                    return;
                }
                if (d2 < this.getMin()) {
                    this.setValue(this.getMin());
                } else if (d2 > this.getMax()) {
                    this.setValue(this.getMax());
                }
            });
            this.setValue(d5 >= d3 && d5 <= d4 ? d5 : d3);
        }

        public final void setMin(double d) {
            this.min.set(d);
        }

        public final double getMin() {
            return this.min.get();
        }

        public final DoubleProperty minProperty() {
            return this.min;
        }

        public final void setMax(double d) {
            this.max.set(d);
        }

        public final double getMax() {
            return this.max.get();
        }

        public final DoubleProperty maxProperty() {
            return this.max;
        }

        public final void setAmountToStepBy(double d) {
            this.amountToStepBy.set(d);
        }

        public final double getAmountToStepBy() {
            return this.amountToStepBy.get();
        }

        public final DoubleProperty amountToStepByProperty() {
            return this.amountToStepBy;
        }

        @Override
        public void decrement(int n) {
            BigDecimal bigDecimal = BigDecimal.valueOf((Double)this.getValue());
            BigDecimal bigDecimal2 = BigDecimal.valueOf(this.getMin());
            BigDecimal bigDecimal3 = BigDecimal.valueOf(this.getMax());
            BigDecimal bigDecimal4 = BigDecimal.valueOf(this.getAmountToStepBy());
            BigDecimal bigDecimal5 = bigDecimal.subtract(bigDecimal4.multiply(BigDecimal.valueOf(n)));
            this.setValue(bigDecimal5.compareTo(bigDecimal2) >= 0 ? bigDecimal5.doubleValue() : (this.isWrapAround() ? Spinner.wrapValue(bigDecimal5, bigDecimal2, bigDecimal3).doubleValue() : this.getMin()));
        }

        @Override
        public void increment(int n) {
            BigDecimal bigDecimal = BigDecimal.valueOf((Double)this.getValue());
            BigDecimal bigDecimal2 = BigDecimal.valueOf(this.getMin());
            BigDecimal bigDecimal3 = BigDecimal.valueOf(this.getMax());
            BigDecimal bigDecimal4 = BigDecimal.valueOf(this.getAmountToStepBy());
            BigDecimal bigDecimal5 = bigDecimal.add(bigDecimal4.multiply(BigDecimal.valueOf(n)));
            this.setValue(bigDecimal5.compareTo(bigDecimal3) <= 0 ? bigDecimal5.doubleValue() : (this.isWrapAround() ? Spinner.wrapValue(bigDecimal5, bigDecimal2, bigDecimal3).doubleValue() : this.getMax()));
        }
    }

    public static class IntegerSpinnerValueFactory
    extends SpinnerValueFactory<Integer> {
        private IntegerProperty min = new SimpleIntegerProperty(this, "min"){

            @Override
            protected void invalidated() {
                Integer n = (Integer)this.getValue();
                if (n == null) {
                    return;
                }
                int n2 = this.get();
                if (n2 > this.getMax()) {
                    this.setMin(this.getMax());
                    return;
                }
                if (n < n2) {
                    this.setValue(n2);
                }
            }
        };
        private IntegerProperty max = new SimpleIntegerProperty(this, "max"){

            @Override
            protected void invalidated() {
                Integer n = (Integer)this.getValue();
                if (n == null) {
                    return;
                }
                int n2 = this.get();
                if (n2 < this.getMin()) {
                    this.setMax(this.getMin());
                    return;
                }
                if (n > n2) {
                    this.setValue(n2);
                }
            }
        };
        private IntegerProperty amountToStepBy = new SimpleIntegerProperty(this, "amountToStepBy");

        public IntegerSpinnerValueFactory(@NamedArg(value="min") int n, @NamedArg(value="max") int n2) {
            this(n, n2, n);
        }

        public IntegerSpinnerValueFactory(@NamedArg(value="min") int n, @NamedArg(value="max") int n2, @NamedArg(value="initialValue") int n3) {
            this(n, n2, n3, 1);
        }

        public IntegerSpinnerValueFactory(@NamedArg(value="min") int n3, @NamedArg(value="max") int n4, @NamedArg(value="initialValue") int n5, @NamedArg(value="amountToStepBy") int n6) {
            this.setMin(n3);
            this.setMax(n4);
            this.setAmountToStepBy(n6);
            this.setConverter(new IntegerStringConverter());
            this.valueProperty().addListener((observableValue, n, n2) -> {
                if (n2 < this.getMin()) {
                    this.setValue(this.getMin());
                } else if (n2 > this.getMax()) {
                    this.setValue(this.getMax());
                }
            });
            this.setValue(n5 >= n3 && n5 <= n4 ? n5 : n3);
        }

        public final void setMin(int n) {
            this.min.set(n);
        }

        public final int getMin() {
            return this.min.get();
        }

        public final IntegerProperty minProperty() {
            return this.min;
        }

        public final void setMax(int n) {
            this.max.set(n);
        }

        public final int getMax() {
            return this.max.get();
        }

        public final IntegerProperty maxProperty() {
            return this.max;
        }

        public final void setAmountToStepBy(int n) {
            this.amountToStepBy.set(n);
        }

        public final int getAmountToStepBy() {
            return this.amountToStepBy.get();
        }

        public final IntegerProperty amountToStepByProperty() {
            return this.amountToStepBy;
        }

        @Override
        public void decrement(int n) {
            int n2 = this.getMin();
            int n3 = this.getMax();
            int n4 = (Integer)this.getValue() - n * this.getAmountToStepBy();
            this.setValue(n4 >= n2 ? n4 : (this.isWrapAround() ? Spinner.wrapValue(n4, n2, n3) + 1 : n2));
        }

        @Override
        public void increment(int n) {
            int n2 = this.getMin();
            int n3 = this.getMax();
            int n4 = (Integer)this.getValue();
            int n5 = n4 + n * this.getAmountToStepBy();
            this.setValue(n5 <= n3 ? n5 : (this.isWrapAround() ? Spinner.wrapValue(n5, n2, n3) - 1 : n3));
        }
    }

    public static class ListSpinnerValueFactory<T>
    extends SpinnerValueFactory<T> {
        private int currentIndex = 0;
        private final ListChangeListener<T> itemsContentObserver = change -> this.updateCurrentIndex();
        private WeakListChangeListener<T> weakItemsContentObserver = new WeakListChangeListener<T>(this.itemsContentObserver);
        private ObjectProperty<ObservableList<T>> items;

        public ListSpinnerValueFactory(@NamedArg(value="items") ObservableList<T> observableList) {
            this.setItems(observableList);
            this.setConverter(new StringConverter<T>(){

                @Override
                public String toString(T t) {
                    if (t == null) {
                        return "";
                    }
                    return t.toString();
                }

                @Override
                public T fromString(String string) {
                    return string;
                }
            });
            this.valueProperty().addListener((observableValue, object, object2) -> {
                int n = -1;
                if (observableList.contains(object2)) {
                    n = observableList.indexOf(object2);
                } else {
                    observableList.add(object2);
                    n = observableList.indexOf(object2);
                }
                this.currentIndex = n;
            });
            this.setValue(this._getValue(this.currentIndex));
        }

        public final void setItems(ObservableList<T> observableList) {
            this.itemsProperty().set(observableList);
        }

        public final ObservableList<T> getItems() {
            return this.items == null ? null : (ObservableList)this.items.get();
        }

        public final ObjectProperty<ObservableList<T>> itemsProperty() {
            if (this.items == null) {
                this.items = new SimpleObjectProperty<ObservableList<T>>(this, "items"){
                    WeakReference<ObservableList<T>> oldItemsRef;

                    @Override
                    protected void invalidated() {
                        ObservableList observableList = this.oldItemsRef == null ? null : (ObservableList)this.oldItemsRef.get();
                        ObservableList observableList2 = this.getItems();
                        if (observableList != null) {
                            observableList.removeListener(weakItemsContentObserver);
                        }
                        if (observableList2 != null) {
                            observableList2.addListener(weakItemsContentObserver);
                        }
                        this.updateCurrentIndex();
                        this.oldItemsRef = new WeakReference(this.getItems());
                    }
                };
            }
            return this.items;
        }

        @Override
        public void decrement(int n) {
            int n2 = this.getItemsSize() - 1;
            int n3 = this.currentIndex - n;
            this.currentIndex = n3 >= 0 ? n3 : (this.isWrapAround() ? Spinner.wrapValue(n3, 0, n2 + 1) : 0);
            this.setValue(this._getValue(this.currentIndex));
        }

        @Override
        public void increment(int n) {
            int n2 = this.currentIndex + n;
            int n3 = this.getItemsSize() - 1;
            this.currentIndex = n2 <= n3 ? n2 : (this.isWrapAround() ? Spinner.wrapValue(n2, 0, n3 + 1) : n3);
            this.setValue(this._getValue(this.currentIndex));
        }

        private int getItemsSize() {
            ObservableList<T> observableList = this.getItems();
            return observableList == null ? 0 : observableList.size();
        }

        private void updateCurrentIndex() {
            int n = this.getItemsSize();
            if (this.currentIndex < 0 || this.currentIndex >= n) {
                this.currentIndex = 0;
            }
            this.setValue(this._getValue(this.currentIndex));
        }

        private T _getValue(int n) {
            ObservableList<T> observableList = this.getItems();
            return observableList == null ? null : (n >= 0 && n < observableList.size() ? observableList.get(n) : null);
        }
    }
}

