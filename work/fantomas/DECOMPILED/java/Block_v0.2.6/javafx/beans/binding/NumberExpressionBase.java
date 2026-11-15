/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.binding;

import com.sun.javafx.binding.StringFormatter;
import java.util.Locale;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.binding.FloatExpression;
import javafx.beans.binding.IntegerExpression;
import javafx.beans.binding.LongExpression;
import javafx.beans.binding.NumberBinding;
import javafx.beans.binding.NumberExpression;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableFloatValue;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableLongValue;
import javafx.beans.value.ObservableNumberValue;

public abstract class NumberExpressionBase
implements NumberExpression {
    public static <S extends Number> NumberExpressionBase numberExpression(ObservableNumberValue observableNumberValue) {
        if (observableNumberValue == null) {
            throw new NullPointerException("Value must be specified.");
        }
        NumberExpressionBase numberExpressionBase = (NumberExpressionBase)(observableNumberValue instanceof NumberExpressionBase ? observableNumberValue : (observableNumberValue instanceof ObservableIntegerValue ? IntegerExpression.integerExpression((ObservableIntegerValue)observableNumberValue) : (observableNumberValue instanceof ObservableDoubleValue ? DoubleExpression.doubleExpression((ObservableDoubleValue)observableNumberValue) : (observableNumberValue instanceof ObservableFloatValue ? FloatExpression.floatExpression((ObservableFloatValue)observableNumberValue) : (observableNumberValue instanceof ObservableLongValue ? LongExpression.longExpression((ObservableLongValue)observableNumberValue) : null)))));
        if (numberExpressionBase != null) {
            return numberExpressionBase;
        }
        throw new IllegalArgumentException("Unsupported Type");
    }

    @Override
    public NumberBinding add(ObservableNumberValue observableNumberValue) {
        return Bindings.add((ObservableNumberValue)this, observableNumberValue);
    }

    @Override
    public NumberBinding subtract(ObservableNumberValue observableNumberValue) {
        return Bindings.subtract((ObservableNumberValue)this, observableNumberValue);
    }

    @Override
    public NumberBinding multiply(ObservableNumberValue observableNumberValue) {
        return Bindings.multiply((ObservableNumberValue)this, observableNumberValue);
    }

    @Override
    public NumberBinding divide(ObservableNumberValue observableNumberValue) {
        return Bindings.divide((ObservableNumberValue)this, observableNumberValue);
    }

    @Override
    public BooleanBinding isEqualTo(ObservableNumberValue observableNumberValue) {
        return Bindings.equal((ObservableNumberValue)this, observableNumberValue);
    }

    @Override
    public BooleanBinding isEqualTo(ObservableNumberValue observableNumberValue, double d) {
        return Bindings.equal((ObservableNumberValue)this, observableNumberValue, d);
    }

    @Override
    public BooleanBinding isEqualTo(double d, double d2) {
        return Bindings.equal((ObservableNumberValue)this, d, d2);
    }

    @Override
    public BooleanBinding isEqualTo(float f, double d) {
        return Bindings.equal((ObservableNumberValue)this, f, d);
    }

    @Override
    public BooleanBinding isEqualTo(long l) {
        return Bindings.equal((ObservableNumberValue)this, l);
    }

    @Override
    public BooleanBinding isEqualTo(long l, double d) {
        return Bindings.equal((ObservableNumberValue)this, l, d);
    }

    @Override
    public BooleanBinding isEqualTo(int n) {
        return Bindings.equal((ObservableNumberValue)this, n);
    }

    @Override
    public BooleanBinding isEqualTo(int n, double d) {
        return Bindings.equal((ObservableNumberValue)this, n, d);
    }

    @Override
    public BooleanBinding isNotEqualTo(ObservableNumberValue observableNumberValue) {
        return Bindings.notEqual((ObservableNumberValue)this, observableNumberValue);
    }

    @Override
    public BooleanBinding isNotEqualTo(ObservableNumberValue observableNumberValue, double d) {
        return Bindings.notEqual((ObservableNumberValue)this, observableNumberValue, d);
    }

    @Override
    public BooleanBinding isNotEqualTo(double d, double d2) {
        return Bindings.notEqual((ObservableNumberValue)this, d, d2);
    }

    @Override
    public BooleanBinding isNotEqualTo(float f, double d) {
        return Bindings.notEqual((ObservableNumberValue)this, f, d);
    }

    @Override
    public BooleanBinding isNotEqualTo(long l) {
        return Bindings.notEqual((ObservableNumberValue)this, l);
    }

    @Override
    public BooleanBinding isNotEqualTo(long l, double d) {
        return Bindings.notEqual((ObservableNumberValue)this, l, d);
    }

    @Override
    public BooleanBinding isNotEqualTo(int n) {
        return Bindings.notEqual((ObservableNumberValue)this, n);
    }

    @Override
    public BooleanBinding isNotEqualTo(int n, double d) {
        return Bindings.notEqual((ObservableNumberValue)this, n, d);
    }

    @Override
    public BooleanBinding greaterThan(ObservableNumberValue observableNumberValue) {
        return Bindings.greaterThan((ObservableNumberValue)this, observableNumberValue);
    }

    @Override
    public BooleanBinding greaterThan(double d) {
        return Bindings.greaterThan((ObservableNumberValue)this, d);
    }

    @Override
    public BooleanBinding greaterThan(float f) {
        return Bindings.greaterThan((ObservableNumberValue)this, f);
    }

    @Override
    public BooleanBinding greaterThan(long l) {
        return Bindings.greaterThan((ObservableNumberValue)this, l);
    }

    @Override
    public BooleanBinding greaterThan(int n) {
        return Bindings.greaterThan((ObservableNumberValue)this, n);
    }

    @Override
    public BooleanBinding lessThan(ObservableNumberValue observableNumberValue) {
        return Bindings.lessThan((ObservableNumberValue)this, observableNumberValue);
    }

    @Override
    public BooleanBinding lessThan(double d) {
        return Bindings.lessThan((ObservableNumberValue)this, d);
    }

    @Override
    public BooleanBinding lessThan(float f) {
        return Bindings.lessThan((ObservableNumberValue)this, f);
    }

    @Override
    public BooleanBinding lessThan(long l) {
        return Bindings.lessThan((ObservableNumberValue)this, l);
    }

    @Override
    public BooleanBinding lessThan(int n) {
        return Bindings.lessThan((ObservableNumberValue)this, n);
    }

    @Override
    public BooleanBinding greaterThanOrEqualTo(ObservableNumberValue observableNumberValue) {
        return Bindings.greaterThanOrEqual((ObservableNumberValue)this, observableNumberValue);
    }

    @Override
    public BooleanBinding greaterThanOrEqualTo(double d) {
        return Bindings.greaterThanOrEqual((ObservableNumberValue)this, d);
    }

    @Override
    public BooleanBinding greaterThanOrEqualTo(float f) {
        return Bindings.greaterThanOrEqual((ObservableNumberValue)this, f);
    }

    @Override
    public BooleanBinding greaterThanOrEqualTo(long l) {
        return Bindings.greaterThanOrEqual((ObservableNumberValue)this, l);
    }

    @Override
    public BooleanBinding greaterThanOrEqualTo(int n) {
        return Bindings.greaterThanOrEqual((ObservableNumberValue)this, n);
    }

    @Override
    public BooleanBinding lessThanOrEqualTo(ObservableNumberValue observableNumberValue) {
        return Bindings.lessThanOrEqual((ObservableNumberValue)this, observableNumberValue);
    }

    @Override
    public BooleanBinding lessThanOrEqualTo(double d) {
        return Bindings.lessThanOrEqual((ObservableNumberValue)this, d);
    }

    @Override
    public BooleanBinding lessThanOrEqualTo(float f) {
        return Bindings.lessThanOrEqual((ObservableNumberValue)this, f);
    }

    @Override
    public BooleanBinding lessThanOrEqualTo(long l) {
        return Bindings.lessThanOrEqual((ObservableNumberValue)this, l);
    }

    @Override
    public BooleanBinding lessThanOrEqualTo(int n) {
        return Bindings.lessThanOrEqual((ObservableNumberValue)this, n);
    }

    @Override
    public StringBinding asString() {
        return (StringBinding)StringFormatter.convert(this);
    }

    @Override
    public StringBinding asString(String string) {
        return (StringBinding)Bindings.format(string, this);
    }

    @Override
    public StringBinding asString(Locale locale, String string) {
        return (StringBinding)Bindings.format(locale, string, this);
    }
}

