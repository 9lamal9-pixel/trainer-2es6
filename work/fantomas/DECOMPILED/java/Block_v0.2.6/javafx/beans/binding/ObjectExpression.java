/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.binding;

import com.sun.javafx.binding.StringFormatter;
import java.util.Locale;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableObjectValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public abstract class ObjectExpression<T>
implements ObservableObjectValue<T> {
    @Override
    public T getValue() {
        return this.get();
    }

    public static <T> ObjectExpression<T> objectExpression(final ObservableObjectValue<T> observableObjectValue) {
        if (observableObjectValue == null) {
            throw new NullPointerException("Value must be specified.");
        }
        return observableObjectValue instanceof ObjectExpression ? (ObjectExpression<T>)observableObjectValue : new ObjectBinding<T>(){
            {
                super.bind(observableObjectValue);
            }

            @Override
            public void dispose() {
                super.unbind(observableObjectValue);
            }

            @Override
            protected T computeValue() {
                return observableObjectValue.get();
            }

            @Override
            public ObservableList<ObservableObjectValue<T>> getDependencies() {
                return FXCollections.singletonObservableList(observableObjectValue);
            }
        };
    }

    public BooleanBinding isEqualTo(ObservableObjectValue<?> observableObjectValue) {
        return Bindings.equal(this, observableObjectValue);
    }

    public BooleanBinding isEqualTo(Object object) {
        return Bindings.equal(this, object);
    }

    public BooleanBinding isNotEqualTo(ObservableObjectValue<?> observableObjectValue) {
        return Bindings.notEqual(this, observableObjectValue);
    }

    public BooleanBinding isNotEqualTo(Object object) {
        return Bindings.notEqual(this, object);
    }

    public BooleanBinding isNull() {
        return Bindings.isNull(this);
    }

    public BooleanBinding isNotNull() {
        return Bindings.isNotNull(this);
    }

    public StringBinding asString() {
        return (StringBinding)StringFormatter.convert(this);
    }

    public StringBinding asString(String string) {
        return (StringBinding)Bindings.format(string, this);
    }

    public StringBinding asString(Locale locale, String string) {
        return (StringBinding)Bindings.format(locale, string, this);
    }
}

