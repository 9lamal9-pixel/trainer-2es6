/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.binding;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.FloatBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.LongBinding;
import javafx.beans.binding.NumberExpressionBase;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.ObjectExpression;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableNumberValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public abstract class IntegerExpression
extends NumberExpressionBase
implements ObservableIntegerValue {
    @Override
    public int intValue() {
        return this.get();
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
    public Integer getValue() {
        return this.get();
    }

    public static IntegerExpression integerExpression(final ObservableIntegerValue observableIntegerValue) {
        if (observableIntegerValue == null) {
            throw new NullPointerException("Value must be specified.");
        }
        return observableIntegerValue instanceof IntegerExpression ? (IntegerExpression)observableIntegerValue : new IntegerBinding(){
            {
                super.bind(observableIntegerValue);
            }

            @Override
            public void dispose() {
                super.unbind(observableIntegerValue);
            }

            @Override
            protected int computeValue() {
                return observableIntegerValue.get();
            }

            @Override
            public ObservableList<ObservableIntegerValue> getDependencies() {
                return FXCollections.singletonObservableList(observableIntegerValue);
            }
        };
    }

    public static <T extends Number> IntegerExpression integerExpression(final ObservableValue<T> observableValue) {
        if (observableValue == null) {
            throw new NullPointerException("Value must be specified.");
        }
        return observableValue instanceof IntegerExpression ? (IntegerExpression)observableValue : new IntegerBinding(){
            {
                super.bind(observableValue);
            }

            @Override
            public void dispose() {
                super.unbind(observableValue);
            }

            @Override
            protected int computeValue() {
                Number number = (Number)observableValue.getValue();
                return number == null ? 0 : number.intValue();
            }

            @Override
            public ObservableList<ObservableValue<T>> getDependencies() {
                return FXCollections.singletonObservableList(observableValue);
            }
        };
    }

    @Override
    public IntegerBinding negate() {
        return (IntegerBinding)Bindings.negate(this);
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
    public IntegerBinding add(int n) {
        return (IntegerBinding)Bindings.add((ObservableNumberValue)this, n);
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
    public IntegerBinding subtract(int n) {
        return (IntegerBinding)Bindings.subtract((ObservableNumberValue)this, n);
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
    public IntegerBinding multiply(int n) {
        return (IntegerBinding)Bindings.multiply((ObservableNumberValue)this, n);
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
    public IntegerBinding divide(int n) {
        return (IntegerBinding)Bindings.divide((ObservableNumberValue)this, n);
    }

    public ObjectExpression<Integer> asObject() {
        return new ObjectBinding<Integer>(){
            {
                this.bind(IntegerExpression.this);
            }

            @Override
            public void dispose() {
                this.unbind(IntegerExpression.this);
            }

            @Override
            protected Integer computeValue() {
                return IntegerExpression.this.getValue();
            }
        };
    }
}

