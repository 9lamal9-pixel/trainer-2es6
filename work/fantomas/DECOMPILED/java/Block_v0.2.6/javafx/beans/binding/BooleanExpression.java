/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.binding;

import com.sun.javafx.binding.StringFormatter;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.ObjectExpression;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public abstract class BooleanExpression
implements ObservableBooleanValue {
    @Override
    public Boolean getValue() {
        return this.get();
    }

    public static BooleanExpression booleanExpression(final ObservableBooleanValue observableBooleanValue) {
        if (observableBooleanValue == null) {
            throw new NullPointerException("Value must be specified.");
        }
        return observableBooleanValue instanceof BooleanExpression ? (BooleanExpression)observableBooleanValue : new BooleanBinding(){
            {
                super.bind(observableBooleanValue);
            }

            @Override
            public void dispose() {
                super.unbind(observableBooleanValue);
            }

            @Override
            protected boolean computeValue() {
                return observableBooleanValue.get();
            }

            @Override
            public ObservableList<ObservableBooleanValue> getDependencies() {
                return FXCollections.singletonObservableList(observableBooleanValue);
            }
        };
    }

    public static BooleanExpression booleanExpression(final ObservableValue<Boolean> observableValue) {
        if (observableValue == null) {
            throw new NullPointerException("Value must be specified.");
        }
        return observableValue instanceof BooleanExpression ? (BooleanExpression)observableValue : new BooleanBinding(){
            {
                super.bind(observableValue);
            }

            @Override
            public void dispose() {
                super.unbind(observableValue);
            }

            @Override
            protected boolean computeValue() {
                Boolean bl = (Boolean)observableValue.getValue();
                return bl == null ? false : bl;
            }

            @Override
            public ObservableList<ObservableValue<Boolean>> getDependencies() {
                return FXCollections.singletonObservableList(observableValue);
            }
        };
    }

    public BooleanBinding and(ObservableBooleanValue observableBooleanValue) {
        return Bindings.and(this, observableBooleanValue);
    }

    public BooleanBinding or(ObservableBooleanValue observableBooleanValue) {
        return Bindings.or(this, observableBooleanValue);
    }

    public BooleanBinding not() {
        return Bindings.not(this);
    }

    public BooleanBinding isEqualTo(ObservableBooleanValue observableBooleanValue) {
        return Bindings.equal(this, observableBooleanValue);
    }

    public BooleanBinding isNotEqualTo(ObservableBooleanValue observableBooleanValue) {
        return Bindings.notEqual(this, observableBooleanValue);
    }

    public StringBinding asString() {
        return (StringBinding)StringFormatter.convert(this);
    }

    public ObjectExpression<Boolean> asObject() {
        return new ObjectBinding<Boolean>(){
            {
                this.bind(BooleanExpression.this);
            }

            @Override
            public void dispose() {
                this.unbind(BooleanExpression.this);
            }

            @Override
            protected Boolean computeValue() {
                return BooleanExpression.this.getValue();
            }
        };
    }
}

