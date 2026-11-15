/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import com.sun.javafx.scene.control.FakeFocusTextField;
import com.sun.javafx.scene.control.skin.resources.ControlResources;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.BooleanConverter;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Control;
import javafx.scene.control.DateCell;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.DatePickerSkin;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.LocalDateStringConverter;

public class DatePicker
extends ComboBoxBase<LocalDate> {
    private LocalDate lastValidDate = null;
    private Chronology lastValidChronology = IsoChronology.INSTANCE;
    private ObjectProperty<Callback<DatePicker, DateCell>> dayCellFactory;
    private ObjectProperty<Chronology> chronology = new SimpleObjectProperty<Object>(this, "chronology", null);
    private BooleanProperty showWeekNumbers;
    private ObjectProperty<StringConverter<LocalDate>> converter = new SimpleObjectProperty<Object>(this, "converter", null);
    private StringConverter<LocalDate> defaultConverter = new LocalDateStringConverter(FormatStyle.SHORT, null, this.getChronology());
    private ReadOnlyObjectWrapper<TextField> editor;
    private static final String DEFAULT_STYLE_CLASS = "date-picker";

    public DatePicker() {
        this(null);
        this.valueProperty().addListener(observable -> {
            LocalDate localDate = (LocalDate)this.getValue();
            Chronology chronology = this.getChronology();
            if (this.validateDate(chronology, localDate)) {
                this.lastValidDate = localDate;
            } else {
                System.err.println("Restoring value to " + (this.lastValidDate == null ? "null" : this.getConverter().toString(this.lastValidDate)));
                this.setValue(this.lastValidDate);
            }
        });
        this.chronologyProperty().addListener(observable -> {
            LocalDate localDate = (LocalDate)this.getValue();
            Chronology chronology = this.getChronology();
            if (this.validateDate(chronology, localDate)) {
                this.lastValidChronology = chronology;
                this.defaultConverter = new LocalDateStringConverter(FormatStyle.SHORT, null, chronology);
            } else {
                System.err.println("Restoring value to " + this.lastValidChronology);
                this.setChronology(this.lastValidChronology);
            }
        });
    }

    private boolean validateDate(Chronology chronology, LocalDate localDate) {
        try {
            if (localDate != null) {
                chronology.date(localDate);
            }
            return true;
        }
        catch (DateTimeException dateTimeException) {
            System.err.println(dateTimeException);
            return false;
        }
    }

    public DatePicker(LocalDate localDate) {
        this.setValue(localDate);
        this.getStyleClass().add(DEFAULT_STYLE_CLASS);
        this.setAccessibleRole(AccessibleRole.DATE_PICKER);
        this.setEditable(true);
    }

    public final void setDayCellFactory(Callback<DatePicker, DateCell> callback) {
        this.dayCellFactoryProperty().set(callback);
    }

    public final Callback<DatePicker, DateCell> getDayCellFactory() {
        return this.dayCellFactory != null ? (Callback)this.dayCellFactory.get() : null;
    }

    public final ObjectProperty<Callback<DatePicker, DateCell>> dayCellFactoryProperty() {
        if (this.dayCellFactory == null) {
            this.dayCellFactory = new SimpleObjectProperty<Callback<DatePicker, DateCell>>(this, "dayCellFactory");
        }
        return this.dayCellFactory;
    }

    public final ObjectProperty<Chronology> chronologyProperty() {
        return this.chronology;
    }

    public final Chronology getChronology() {
        Chronology chronology = (Chronology)this.chronology.get();
        if (chronology == null) {
            try {
                chronology = Chronology.ofLocale(Locale.getDefault(Locale.Category.FORMAT));
            }
            catch (Exception exception) {
                System.err.println(exception);
            }
            if (chronology == null) {
                chronology = IsoChronology.INSTANCE;
            }
        }
        return chronology;
    }

    public final void setChronology(Chronology chronology) {
        this.chronology.setValue(chronology);
    }

    public final BooleanProperty showWeekNumbersProperty() {
        if (this.showWeekNumbers == null) {
            String string = Locale.getDefault(Locale.Category.FORMAT).getCountry();
            boolean bl = !string.isEmpty() && ControlResources.getNonTranslatableString("DatePicker.showWeekNumbers").contains(string);
            this.showWeekNumbers = new StyleableBooleanProperty(bl){

                @Override
                public CssMetaData<DatePicker, Boolean> getCssMetaData() {
                    return StyleableProperties.SHOW_WEEK_NUMBERS;
                }

                @Override
                public Object getBean() {
                    return DatePicker.this;
                }

                @Override
                public String getName() {
                    return "showWeekNumbers";
                }
            };
        }
        return this.showWeekNumbers;
    }

    public final void setShowWeekNumbers(boolean bl) {
        this.showWeekNumbersProperty().setValue(bl);
    }

    public final boolean isShowWeekNumbers() {
        return this.showWeekNumbersProperty().getValue();
    }

    public final ObjectProperty<StringConverter<LocalDate>> converterProperty() {
        return this.converter;
    }

    public final void setConverter(StringConverter<LocalDate> stringConverter) {
        this.converterProperty().set(stringConverter);
    }

    public final StringConverter<LocalDate> getConverter() {
        StringConverter stringConverter = (StringConverter)this.converterProperty().get();
        if (stringConverter != null) {
            return stringConverter;
        }
        return this.defaultConverter;
    }

    public final TextField getEditor() {
        return (TextField)this.editorProperty().get();
    }

    public final ReadOnlyObjectProperty<TextField> editorProperty() {
        if (this.editor == null) {
            this.editor = new ReadOnlyObjectWrapper(this, "editor");
            this.editor.set(new FakeFocusTextField());
        }
        return this.editor.getReadOnlyProperty();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new DatePickerSkin(this);
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return DatePicker.getClassCssMetaData();
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case DATE: {
                return this.getValue();
            }
            case TEXT: {
                String string = this.getAccessibleText();
                if (string != null && !string.isEmpty()) {
                    return string;
                }
                LocalDate localDate = (LocalDate)this.getValue();
                StringConverter<LocalDate> stringConverter = this.getConverter();
                if (localDate != null && stringConverter != null) {
                    return stringConverter.toString(localDate);
                }
                return "";
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }

    private static class StyleableProperties {
        private static final String country = Locale.getDefault(Locale.Category.FORMAT).getCountry();
        private static final CssMetaData<DatePicker, Boolean> SHOW_WEEK_NUMBERS = new CssMetaData<DatePicker, Boolean>("-fx-show-week-numbers", BooleanConverter.getInstance(), Boolean.valueOf(!country.isEmpty() && ControlResources.getNonTranslatableString("DatePicker.showWeekNumbers").contains(country))){

            @Override
            public boolean isSettable(DatePicker datePicker) {
                return datePicker.showWeekNumbers == null || !datePicker.showWeekNumbers.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(DatePicker datePicker) {
                return (StyleableProperty)((Object)datePicker.showWeekNumbersProperty());
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList arrayList = new ArrayList(Control.getClassCssMetaData());
            Collections.addAll(arrayList, SHOW_WEEK_NUMBERS);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }
}

