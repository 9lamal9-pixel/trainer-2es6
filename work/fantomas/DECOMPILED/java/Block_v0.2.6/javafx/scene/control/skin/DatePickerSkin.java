/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.scene.control.DatePickerContent;
import com.sun.javafx.scene.control.DatePickerHijrahContent;
import com.sun.javafx.scene.control.behavior.ComboBoxBaseBehavior;
import com.sun.javafx.scene.control.behavior.DatePickerBehavior;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.chrono.HijrahChronology;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.ComboBoxPopupControl;
import javafx.util.StringConverter;

public class DatePickerSkin
extends ComboBoxPopupControl<LocalDate> {
    private final DatePicker datePicker;
    private TextField displayNode;
    private DatePickerContent datePickerContent;
    private final DatePickerBehavior behavior;

    public DatePickerSkin(DatePicker datePicker) {
        super(datePicker);
        this.datePicker = datePicker;
        this.behavior = new DatePickerBehavior(datePicker);
        this.arrow.paddingProperty().addListener(new InvalidationListener(){
            private boolean rounding = false;

            @Override
            public void invalidated(Observable observable) {
                Insets insets;
                Insets insets2;
                if (!this.rounding && !(insets2 = new Insets(Math.round((insets = DatePickerSkin.this.arrow.getPadding()).getTop()), Math.round(insets.getRight()), Math.round(insets.getBottom()), Math.round(insets.getLeft()))).equals(insets)) {
                    this.rounding = true;
                    DatePickerSkin.this.arrow.setPadding(insets2);
                    this.rounding = false;
                }
            }
        });
        this.registerChangeListener(datePicker.chronologyProperty(), observableValue -> {
            this.updateDisplayNode();
            this.datePickerContent = null;
            this.popup = null;
        });
        this.registerChangeListener(datePicker.converterProperty(), observableValue -> this.updateDisplayNode());
        this.registerChangeListener(datePicker.dayCellFactoryProperty(), observableValue -> {
            this.updateDisplayNode();
            this.datePickerContent = null;
            this.popup = null;
        });
        this.registerChangeListener(datePicker.showWeekNumbersProperty(), observableValue -> {
            if (this.datePickerContent != null) {
                this.datePickerContent.updateGrid();
                this.datePickerContent.updateWeeknumberDateCells();
            }
        });
        this.registerChangeListener(datePicker.valueProperty(), observableValue -> {
            this.updateDisplayNode();
            if (this.datePickerContent != null) {
                LocalDate localDate = (LocalDate)datePicker.getValue();
                this.datePickerContent.displayedYearMonthProperty().set(localDate != null ? YearMonth.from(localDate) : YearMonth.now());
                this.datePickerContent.updateValues();
            }
            datePicker.fireEvent(new ActionEvent());
        });
        this.registerChangeListener(datePicker.showingProperty(), observableValue -> {
            if (datePicker.isShowing()) {
                if (this.datePickerContent != null) {
                    LocalDate localDate = (LocalDate)datePicker.getValue();
                    this.datePickerContent.displayedYearMonthProperty().set(localDate != null ? YearMonth.from(localDate) : YearMonth.now());
                    this.datePickerContent.updateValues();
                }
                this.show();
            } else {
                this.hide();
            }
        });
        if (datePicker.isShowing()) {
            this.show();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (this.behavior != null) {
            this.behavior.dispose();
        }
    }

    @Override
    public Node getPopupContent() {
        if (this.datePickerContent == null) {
            this.datePickerContent = this.datePicker.getChronology() instanceof HijrahChronology ? new DatePickerHijrahContent(this.datePicker) : new DatePickerContent(this.datePicker);
        }
        return this.datePickerContent;
    }

    @Override
    protected double computeMinWidth(double d, double d2, double d3, double d4, double d5) {
        return 50.0;
    }

    @Override
    public void show() {
        super.show();
        this.datePickerContent.clearFocus();
    }

    @Override
    protected TextField getEditor() {
        return ((DatePicker)this.getSkinnable()).getEditor();
    }

    @Override
    protected StringConverter<LocalDate> getConverter() {
        return ((DatePicker)this.getSkinnable()).getConverter();
    }

    @Override
    public Node getDisplayNode() {
        if (this.displayNode == null) {
            this.displayNode = this.getEditableInputNode();
            this.displayNode.getStyleClass().add("date-picker-display-node");
            this.updateDisplayNode();
        }
        this.displayNode.setEditable(this.datePicker.isEditable());
        return this.displayNode;
    }

    @Override
    void focusLost() {
    }

    @Override
    ComboBoxBaseBehavior getBehavior() {
        return this.behavior;
    }
}

