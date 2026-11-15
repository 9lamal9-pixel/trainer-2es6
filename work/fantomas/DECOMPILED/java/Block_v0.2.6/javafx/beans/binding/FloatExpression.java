/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.binding;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.FloatBinding;
import javafx.beans.binding.NumberExpressionBase;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.ObjectExpression;
import javafx.beans.value.ObservableFloatValue;
import javafx.beans.value.ObservableNumberValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public abstract class FloatExpression
extends NumberExpressionBase
implements ObservableFloatValue {
    @Override
    public int intValue() {
        return (int)this.get();
    }

    @Override
    public long longValue() {
        return (long)this.get();
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
    public Float getValue() {
        return Float.valueOf(this.get());
    }

    public static FloatExpression floatExpression(final ObservableFloatValue observableFloatValue) {
        if (observableFloatValue == null) {
            throw new NullPointerException("Value must be specified.");
        }
        return observableFloatValue instanceof FloatExpression ? (FloatExpression)observableFloatValue : new FloatBinding(){
            {
                super.bind(observableFloatValue);
            }

            @Override
            public void dispose() {
                super.unbind(observableFloatValue);
            }

            @Override
            protected float computeValue() {
                return observableFloatValue.get();
            }

            @Override
            public ObservableList<ObservableFloatValue> getDependencies() {
                return FXCollections.singletonObservableList(observableFloatValue);
            }
        };
    }

    public static <T extends Number> FloatExpression floatExpression(final ObservableValue<T> observableValue) {
        if (observableValue == null) {
            throw new NullPointerException("Value must be specified.");
        }
        return observableValue instanceof FloatExpression ? (FloatExpression)observableValue : new FloatBinding(){
            {
                super.bind(observableValue);
            }

            @Override
            public void dispose() {
                super.unbind(observableValue);
            }

            @Override
            protected float computeValue() {
                Number number = (Number)observableValue.getValue();
                return number == null ? 0.0f : number.floatValue();
            }

            @Override
            public ObservableList<ObservableValue<T>> getDependencies() {
                return FXCollections.singletonObservableList(observableValue);
            }
        };
    }

    @Override
    public FloatBinding negate() {
        return (FloatBinding)Bindings.negate(this);
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
    public FloatBinding add(long l) {
        return (FloatBinding)Bindings.add((ObservableNumberValue)this, l);
    }

    @Override
    public FloatBinding add(int n) {
        return (FloatBinding)Bindings.add((ObservableNumberValue)this, n);
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
    public FloatBinding subtract(long l) {
        return (FloatBinding)Bindings.subtract((ObservableNumberValue)this, l);
    }

    @Override
    public FloatBinding subtract(int n) {
        return (FloatBinding)Bindings.subtract((ObservableNumberValue)this, n);
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
    public FloatBinding multiply(long l) {
        return (FloatBinding)Bindings.multiply((ObservableNumberValue)this, l);
    }

    @Override
    public FloatBinding multiply(int n) {
        return (FloatBinding)Bindings.multiply((ObservableNumberValue)this, n);
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
    public FloatBinding divide(long l) {
        return (FloatBinding)Bindings.divide((ObservableNumberValue)this, l);
    }

    @Override
    public FloatBinding divide(int n) {
        return (FloatBinding)Bindings.divide((ObservableNumberValue)this, n);
    }

    public ObjectExpression<Float> asObject() {
        return new ObjectBinding<Float>(){
            {
                this.bind(FloatExpression.this);
            }

            @Override
            public void dispose() {
                this.unbind(FloatExpression.this);
            }

            @Override
            protected Float computeValue() {
                return FloatExpression.this.getValue();
            }
        };
    }
}

