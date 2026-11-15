/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import com.sun.javafx.scene.control.FakeFocusTextField;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.NamedArg;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.SimpleStyleableObjectProperty;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.css.converter.DurationConverter;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.SpinnerSkin;
import javafx.util.Duration;
import javafx.util.StringConverter;

public class Spinner<T>
extends Control {
    private static final String DEFAULT_STYLE_CLASS = "spinner";
    public static final String STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL = "arrows-on-right-horizontal";
    public static final String STYLE_CLASS_ARROWS_ON_LEFT_VERTICAL = "arrows-on-left-vertical";
    public static final String STYLE_CLASS_ARROWS_ON_LEFT_HORIZONTAL = "arrows-on-left-horizontal";
    public static final String STYLE_CLASS_SPLIT_ARROWS_VERTICAL = "split-arrows-vertical";
    public static final String STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL = "split-arrows-horizontal";
    private ReadOnlyObjectWrapper<T> value = new ReadOnlyObjectWrapper(this, "value");
    private ObjectProperty<SpinnerValueFactory<T>> valueFactory = new SimpleObjectProperty<SpinnerValueFactory<T>>(this, "valueFactory"){

        @Override
        protected void invalidated() {
            Spinner.this.value.unbind();
            SpinnerValueFactory spinnerValueFactory = (SpinnerValueFactory)this.get();
            if (spinnerValueFactory != null) {
                Spinner.this.value.bind(spinnerValueFactory.valueProperty());
            }
        }
    };
    private BooleanProperty editable;
    private TextField textField;
    private ReadOnlyObjectWrapper<TextField> editor;
    private final ObjectProperty<Duration> initialDelay = new SimpleStyleableObjectProperty<Duration>(INITIAL_DELAY, this, "initialDelay", new Duration(300.0));
    private final ObjectProperty<Duration> repeatDelay = new SimpleStyleableObjectProperty<Duration>(REPEAT_DELAY, this, "repeatDelay", new Duration(60.0));
    private static final CssMetaData<Spinner<?>, Duration> INITIAL_DELAY = new CssMetaData<Spinner<?>, Duration>("-fx-initial-delay", DurationConverter.getInstance(), new Duration(300.0)){

        @Override
        public boolean isSettable(Spinner<?> spinner) {
            return !spinner.initialDelayProperty().isBound();
        }

        @Override
        public StyleableProperty<Duration> getStyleableProperty(Spinner<?> spinner) {
            return (StyleableProperty)((Object)spinner.initialDelayProperty());
        }
    };
    private static final CssMetaData<Spinner<?>, Duration> REPEAT_DELAY = new CssMetaData<Spinner<?>, Duration>("-fx-repeat-delay", DurationConverter.getInstance(), new Duration(60.0)){

        @Override
        public boolean isSettable(Spinner<?> spinner) {
            return !spinner.repeatDelayProperty().isBound();
        }

        @Override
        public StyleableProperty<Duration> getStyleableProperty(Spinner<?> spinner) {
            return (StyleableProperty)((Object)spinner.repeatDelayProperty());
        }
    };
    private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

    public Spinner() {
        this.getStyleClass().add(DEFAULT_STYLE_CLASS);
        this.setAccessibleRole(AccessibleRole.SPINNER);
        this.getEditor().setOnAction(actionEvent -> this.commitValue());
        this.getEditor().editableProperty().bind(this.editableProperty());
        this.value.addListener((observableValue, object, object2) -> this.setText(object2));
        this.getProperties().addListener(change -> {
            if (change.wasAdded() && change.getKey() == "FOCUSED") {
                this.setFocused((Boolean)change.getValueAdded());
                this.getProperties().remove("FOCUSED");
            }
        });
        this.focusedProperty().addListener(observable -> {
            if (!this.isFocused()) {
                this.commitValue();
            }
        });
    }

    public Spinner(@NamedArg(value="min") int n, @NamedArg(value="max") int n2, @NamedArg(value="initialValue") int n3) {
        this(new SpinnerValueFactory.IntegerSpinnerValueFactory(n, n2, n3));
    }

    public Spinner(@NamedArg(value="min") int n, @NamedArg(value="max") int n2, @NamedArg(value="initialValue") int n3, @NamedArg(value="amountToStepBy") int n4) {
        this(new SpinnerValueFactory.IntegerSpinnerValueFactory(n, n2, n3, n4));
    }

    public Spinner(@NamedArg(value="min") double d, @NamedArg(value="max") double d2, @NamedArg(value="initialValue") double d3) {
        this(new SpinnerValueFactory.DoubleSpinnerValueFactory(d, d2, d3));
    }

    public Spinner(@NamedArg(value="min") double d, @NamedArg(value="max") double d2, @NamedArg(value="initialValue") double d3, @NamedArg(value="amountToStepBy") double d4) {
        this(new SpinnerValueFactory.DoubleSpinnerValueFactory(d, d2, d3, d4));
    }

    Spinner(@NamedArg(value="min") LocalDate localDate, @NamedArg(value="max") LocalDate localDate2, @NamedArg(value="initialValue") LocalDate localDate3) {
        this(new SpinnerValueFactory.LocalDateSpinnerValueFactory(localDate, localDate2, localDate3));
    }

    Spinner(@NamedArg(value="min") LocalDate localDate, @NamedArg(value="max") LocalDate localDate2, @NamedArg(value="initialValue") LocalDate localDate3, @NamedArg(value="amountToStepBy") long l, @NamedArg(value="temporalUnit") TemporalUnit temporalUnit) {
        this(new SpinnerValueFactory.LocalDateSpinnerValueFactory(localDate, localDate2, localDate3, l, temporalUnit));
    }

    Spinner(@NamedArg(value="min") LocalTime localTime, @NamedArg(value="max") LocalTime localTime2, @NamedArg(value="initialValue") LocalTime localTime3) {
        this(new SpinnerValueFactory.LocalTimeSpinnerValueFactory(localTime, localTime2, localTime3));
    }

    Spinner(@NamedArg(value="min") LocalTime localTime, @NamedArg(value="max") LocalTime localTime2, @NamedArg(value="initialValue") LocalTime localTime3, @NamedArg(value="amountToStepBy") long l, @NamedArg(value="temporalUnit") TemporalUnit temporalUnit) {
        this(new SpinnerValueFactory.LocalTimeSpinnerValueFactory(localTime, localTime2, localTime3, l, temporalUnit));
    }

    public Spinner(@NamedArg(value="items") ObservableList<T> observableList) {
        this(new SpinnerValueFactory.ListSpinnerValueFactory<T>(observableList));
    }

    public Spinner(@NamedArg(value="valueFactory") SpinnerValueFactory<T> spinnerValueFactory) {
        this();
        this.setValueFactory(spinnerValueFactory);
    }

    public void increment() {
        this.increment(1);
    }

    public void increment(int n) {
        SpinnerValueFactory<T> spinnerValueFactory = this.getValueFactory();
        if (spinnerValueFactory == null) {
            throw new IllegalStateException("Can't increment Spinner with a null SpinnerValueFactory");
        }
        this.commitValue();
        spinnerValueFactory.increment(n);
    }

    public void decrement() {
        this.decrement(1);
    }

    public void decrement(int n) {
        SpinnerValueFactory<T> spinnerValueFactory = this.getValueFactory();
        if (spinnerValueFactory == null) {
            throw new IllegalStateException("Can't decrement Spinner with a null SpinnerValueFactory");
        }
        this.commitValue();
        spinnerValueFactory.decrement(n);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SpinnerSkin(this);
    }

    public final void commitValue() {
        StringConverter<T> stringConverter;
        if (!this.isEditable()) {
            return;
        }
        String string = this.getEditor().getText();
        SpinnerValueFactory<T> spinnerValueFactory = this.getValueFactory();
        if (spinnerValueFactory != null && (stringConverter = spinnerValueFactory.getConverter()) != null) {
            T t = stringConverter.fromString(string);
            spinnerValueFactory.setValue(t);
        }
    }

    public final void cancelEdit() {
        StringConverter<T> stringConverter;
        if (!this.isEditable()) {
            return;
        }
        T t = this.getValue();
        SpinnerValueFactory<T> spinnerValueFactory = this.getValueFactory();
        if (spinnerValueFactory != null && (stringConverter = spinnerValueFactory.getConverter()) != null) {
            String string = stringConverter.toString(t);
            this.getEditor().setText(string);
        }
    }

    public final T getValue() {
        return this.value.get();
    }

    public final ReadOnlyObjectProperty<T> valueProperty() {
        return this.value;
    }

    public final void setValueFactory(SpinnerValueFactory<T> spinnerValueFactory) {
        this.valueFactory.setValue(spinnerValueFactory);
    }

    public final SpinnerValueFactory<T> getValueFactory() {
        return (SpinnerValueFactory)this.valueFactory.get();
    }

    public final ObjectProperty<SpinnerValueFactory<T>> valueFactoryProperty() {
        return this.valueFactory;
    }

    public final void setEditable(boolean bl) {
        this.editableProperty().set(bl);
    }

    public final boolean isEditable() {
        return this.editable == null ? true : this.editable.get();
    }

    public final BooleanProperty editableProperty() {
        if (this.editable == null) {
            this.editable = new SimpleBooleanProperty(this, "editable", false);
        }
        return this.editable;
    }

    public final ReadOnlyObjectProperty<TextField> editorProperty() {
        if (this.editor == null) {
            this.editor = new ReadOnlyObjectWrapper(this, "editor");
            this.textField = new FakeFocusTextField();
            this.textField.tooltipProperty().bind(this.tooltipProperty());
            this.editor.set(this.textField);
        }
        return this.editor.getReadOnlyProperty();
    }

    public final TextField getEditor() {
        return (TextField)this.editorProperty().get();
    }

    public final StringProperty promptTextProperty() {
        return this.getEditor().promptTextProperty();
    }

    public final String getPromptText() {
        return this.getEditor().getPromptText();
    }

    public final void setPromptText(String string) {
        this.getEditor().setPromptText(string);
    }

    public final ObjectProperty<Duration> initialDelayProperty() {
        return this.initialDelay;
    }

    public final void setInitialDelay(Duration duration) {
        if (duration != null) {
            this.initialDelay.set(duration);
        }
    }

    public final Duration getInitialDelay() {
        return (Duration)this.initialDelay.get();
    }

    public final ObjectProperty<Duration> repeatDelayProperty() {
        return this.repeatDelay;
    }

    public final void setRepeatDelay(Duration duration) {
        if (duration != null) {
            this.repeatDelay.set(duration);
        }
    }

    public final Duration getRepeatDelay() {
        return (Duration)this.repeatDelay.get();
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return Spinner.getClassCssMetaData();
    }

    private void setText(T t) {
        StringConverter<T> stringConverter;
        String string = null;
        SpinnerValueFactory<T> spinnerValueFactory = this.getValueFactory();
        if (spinnerValueFactory != null && (stringConverter = spinnerValueFactory.getConverter()) != null) {
            string = stringConverter.toString(t);
        }
        this.notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
        if (string == null) {
            if (t == null) {
                this.getEditor().clear();
                return;
            }
            string = t.toString();
        }
        this.getEditor().setText(string);
    }

    static int wrapValue(int n, int n2, int n3) {
        if (n3 == 0) {
            throw new RuntimeException();
        }
        int n4 = n % n3;
        if (n4 > n2 && n3 < n2) {
            n4 = n4 + n3 - n2;
        } else if (n4 < n2 && n3 > n2) {
            n4 = n4 + n3 - n2;
        }
        return n4;
    }

    static BigDecimal wrapValue(BigDecimal bigDecimal, BigDecimal bigDecimal2, BigDecimal bigDecimal3) {
        if (bigDecimal3.doubleValue() == 0.0) {
            throw new RuntimeException();
        }
        if (bigDecimal.compareTo(bigDecimal2) < 0) {
            return bigDecimal3;
        }
        if (bigDecimal.compareTo(bigDecimal3) > 0) {
            return bigDecimal2;
        }
        return bigDecimal;
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case TEXT: {
                StringConverter<T> stringConverter;
                T t = this.getValue();
                SpinnerValueFactory<T> spinnerValueFactory = this.getValueFactory();
                if (spinnerValueFactory != null && (stringConverter = spinnerValueFactory.getConverter()) != null) {
                    return stringConverter.toString(t);
                }
                return t != null ? t.toString() : "";
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }

    @Override
    public void executeAccessibleAction(AccessibleAction accessibleAction, Object ... objectArray) {
        switch (accessibleAction) {
            case INCREMENT: {
                this.increment();
                break;
            }
            case DECREMENT: {
                this.decrement();
                break;
            }
            default: {
                super.executeAccessibleAction(accessibleAction, new Object[0]);
            }
        }
    }

    static {
        ArrayList arrayList = new ArrayList(Control.getClassCssMetaData());
        arrayList.add(INITIAL_DELAY);
        arrayList.add(REPEAT_DELAY);
        STYLEABLES = Collections.unmodifiableList(arrayList);
    }
}

