/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.binding;

import com.sun.javafx.binding.StringFormatter;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;

public abstract class StringExpression
implements ObservableStringValue {
    @Override
    public String getValue() {
        return (String)this.get();
    }

    public final String getValueSafe() {
        String string = (String)this.get();
        return string == null ? "" : string;
    }

    public static StringExpression stringExpression(ObservableValue<?> observableValue) {
        if (observableValue == null) {
            throw new NullPointerException("Value must be specified.");
        }
        return StringFormatter.convert(observableValue);
    }

    public StringExpression concat(Object object) {
        return Bindings.concat(this, object);
    }

    public BooleanBinding isEqualTo(ObservableStringValue observableStringValue) {
        return Bindings.equal((ObservableStringValue)this, observableStringValue);
    }

    public BooleanBinding isEqualTo(String string) {
        return Bindings.equal((ObservableStringValue)this, string);
    }

    public BooleanBinding isNotEqualTo(ObservableStringValue observableStringValue) {
        return Bindings.notEqual((ObservableStringValue)this, observableStringValue);
    }

    public BooleanBinding isNotEqualTo(String string) {
        return Bindings.notEqual((ObservableStringValue)this, string);
    }

    public BooleanBinding isEqualToIgnoreCase(ObservableStringValue observableStringValue) {
        return Bindings.equalIgnoreCase((ObservableStringValue)this, observableStringValue);
    }

    public BooleanBinding isEqualToIgnoreCase(String string) {
        return Bindings.equalIgnoreCase((ObservableStringValue)this, string);
    }

    public BooleanBinding isNotEqualToIgnoreCase(ObservableStringValue observableStringValue) {
        return Bindings.notEqualIgnoreCase((ObservableStringValue)this, observableStringValue);
    }

    public BooleanBinding isNotEqualToIgnoreCase(String string) {
        return Bindings.notEqualIgnoreCase((ObservableStringValue)this, string);
    }

    public BooleanBinding greaterThan(ObservableStringValue observableStringValue) {
        return Bindings.greaterThan((ObservableStringValue)this, observableStringValue);
    }

    public BooleanBinding greaterThan(String string) {
        return Bindings.greaterThan((ObservableStringValue)this, string);
    }

    public BooleanBinding lessThan(ObservableStringValue observableStringValue) {
        return Bindings.lessThan((ObservableStringValue)this, observableStringValue);
    }

    public BooleanBinding lessThan(String string) {
        return Bindings.lessThan((ObservableStringValue)this, string);
    }

    public BooleanBinding greaterThanOrEqualTo(ObservableStringValue observableStringValue) {
        return Bindings.greaterThanOrEqual((ObservableStringValue)this, observableStringValue);
    }

    public BooleanBinding greaterThanOrEqualTo(String string) {
        return Bindings.greaterThanOrEqual((ObservableStringValue)this, string);
    }

    public BooleanBinding lessThanOrEqualTo(ObservableStringValue observableStringValue) {
        return Bindings.lessThanOrEqual((ObservableStringValue)this, observableStringValue);
    }

    public BooleanBinding lessThanOrEqualTo(String string) {
        return Bindings.lessThanOrEqual((ObservableStringValue)this, string);
    }

    public BooleanBinding isNull() {
        return Bindings.isNull(this);
    }

    public BooleanBinding isNotNull() {
        return Bindings.isNotNull(this);
    }

    public IntegerBinding length() {
        return Bindings.length(this);
    }

    public BooleanBinding isEmpty() {
        return Bindings.isEmpty(this);
    }

    public BooleanBinding isNotEmpty() {
        return Bindings.isNotEmpty(this);
    }
}

