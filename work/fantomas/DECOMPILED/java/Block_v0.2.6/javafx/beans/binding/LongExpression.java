/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.binding;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.FloatBinding;
import javafx.beans.binding.LongBinding;
import javafx.beans.binding.NumberExpressionBase;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.ObjectExpression;
import javafx.beans.value.ObservableLongValue;
import javafx.beans.value.ObservableNumberValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public abstract class LongExpression
extends NumberExpressionBase
implements ObservableLongValue {
    @Override
    public int intValue() {
        return (int)this.get();
    }

    @Override
    public long longValue() {
        return this.get();
    }

    @Override
    public float floatValue() {
        return this.get();
    }

    @Override
    public double doubleValue() {
        return this.get();
    }

    @Override
    public Long getValue() {
        return this.get();
    }

    public static LongExpression longExpression(final ObservableLongValue observableLongValue) {
        if (observableLongValue == null) {
            throw new NullPointerException("Value must be specified.");
        }
        return observableLongValue instanceof LongExpression ? (LongExpression)observableLongValue : new LongBinding(){
            {
                super.bind(observableLongValue);
            }

            @Override
            public void dispose() {
                super.unbind(observableLongValue);
            }

            @Override
            protected long computeValue() {
                return observableLongValue.get();
            }

            @Override
            public ObservableList<ObservableLongValue> getDependencies() {
                return FXCollections.singletonObservableList(observableLongValue);
            }
        };
    }

    public static <T extends Number> LongExpression longExpression(final ObservableValue<T> observableValue) {
        if (observableValue == null) {
            throw new NullPointerException("Value must be specified.");
        }
        return observableValue instanceof LongExpression ? (LongExpression)observableValue : new LongBinding(){
            {
                super.bind(observableValue);
            }

            @Override
            public void dispose() {
                super.unbind(observableValue);
            }

            @Override
            protected long computeValue() {
                Number number = (Number)observableValue.getValue();
                return number == null ? 0L : number.longValue();
            }

            @Override
            public ObservableList<ObservableValue<T>> getDependencies() {
                return FXCollections.singletonObservableList(observableValue);
            }
        };
    }

    @Override
    public LongBinding negate() {
        return (LongBinding)Bindings.negate(this);
    }

    @Override
    public DoubleBinding add(double d) {
        return Bindings.add((ObservableNumberValue)this, d);
    }

    @Override
    public FloatBinding add(float f) {
        return (FloatBinding)Bindings.add((ObservableNumberValue)this, f);
    }

    @Override
    public LongBinding add(long l) {
        return (LongBinding)Bindings.add((ObservableNumberValue)this, l);
    }

    @Override
    public LongBinding add(int n) {
        return (LongBinding)Bindings.add((ObservableNumberValue)this, n);
    }

    @Override
    public DoubleBinding subtract(double d) {
        return Bindings.subtract((ObservableNumberValue)this, d);
    }

    @Override
    public FloatBinding subtract(float f) {
        return (FloatBinding)Bindings.subtract((ObservableNumberValue)this, f);
    }

    @Override
    public LongBinding subtract(long l) {
        return (LongBinding)Bindings.subtract((ObservableNumberValue)this, l);
    }

    @Override
    public LongBinding subtract(int n) {
        return (LongBinding)Bindings.subtract((ObservableNumberValue)this, n);
    }

    @Override
    public DoubleBinding multiply(double d) {
        return Bindings.multiply((ObservableNumberValue)this, d);
    }

    @Override
    public FloatBinding multiply(float f) {
        return (FloatBinding)Bindings.multiply((ObservableNumberValue)this, f);
    }

    @Override
    public LongBinding multiply(long l) {
        return (LongBinding)Bindings.multiply((ObservableNumberValue)this, l);
    }

    @Override
    public LongBinding multiply(int n) {
        return (LongBinding)Bindings.multiply((ObservableNumberValue)this, n);
    }

    @Override
    public DoubleBinding divide(double d) {
        return Bindings.divide((ObservableNumberValue)this, d);
    }

    @Override
    public FloatBinding divide(float f) {
        return (FloatBinding)Bindings.divide((ObservableNumberValue)this, f);
    }

    @Override
    public LongBinding divide(long l) {
        return (LongBinding)Bindings.divide((ObservableNumberValue)this, l);
    }

    @Override
    public LongBinding divide(int n) {
        return (LongBinding)Bindings.divide((ObservableNumberValue)this, n);
    }

    public ObjectExpression<Long> asObject() {
        return new ObjectBinding<Long>(){
            {
                this.bind(LongExpression.this);
            }

            @Override
            public void dispose() {
                this.unbind(LongExpression.this);
            }

            @Override
            protected Long computeValue() {
                return LongExpression.this.getValue();
            }
        };
    }
}

