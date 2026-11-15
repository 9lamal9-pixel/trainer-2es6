/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.binding;

import com.sun.javafx.binding.StringConstant;
import java.util.ArrayList;
import java.util.Locale;
import javafx.beans.binding.StringBinding;
import javafx.beans.binding.StringExpression;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public abstract class StringFormatter
extends StringBinding {
    private static Object extractValue(Object object) {
        return object instanceof ObservableValue ? ((ObservableValue)object).getValue() : object;
    }

    private static Object[] extractValues(Object[] objectArray) {
        int n = objectArray.length;
        Object[] objectArray2 = new Object[n];
        for (int i = 0; i < n; ++i) {
            objectArray2[i] = StringFormatter.extractValue(objectArray[i]);
        }
        return objectArray2;
    }

    private static ObservableValue<?>[] extractDependencies(Object ... objectArray) {
        ArrayList<ObservableValue> arrayList = new ArrayList<ObservableValue>();
        for (Object object : objectArray) {
            if (!(object instanceof ObservableValue)) continue;
            arrayList.add((ObservableValue)object);
        }
        return arrayList.toArray(new ObservableValue[arrayList.size()]);
    }

    public static StringExpression convert(final ObservableValue<?> observableValue) {
        if (observableValue == null) {
            throw new NullPointerException("ObservableValue must be specified");
        }
        if (observableValue instanceof StringExpression) {
            return (StringExpression)observableValue;
        }
        return new StringBinding(){
            {
                super.bind(observableValue);
            }

            @Override
            public void dispose() {
                super.unbind(observableValue);
            }

            @Override
            protected String computeValue() {
                Object t = observableValue.getValue();
                return t == null ? "null" : t.toString();
            }

            @Override
            public ObservableList<ObservableValue<?>> getDependencies() {
                return FXCollections.singletonObservableList(observableValue);
            }
        };
    }

    public static StringExpression concat(final Object ... objectArray) {
        if (objectArray == null || objectArray.length == 0) {
            return StringConstant.valueOf("");
        }
        if (objectArray.length == 1) {
            Object object = objectArray[0];
            return object instanceof ObservableValue ? StringFormatter.convert((ObservableValue)object) : StringConstant.valueOf(object.toString());
        }
        if (StringFormatter.extractDependencies(objectArray).length == 0) {
            StringBuilder stringBuilder = new StringBuilder();
            for (Object object : objectArray) {
                stringBuilder.append(object);
            }
            return StringConstant.valueOf(stringBuilder.toString());
        }
        return new StringFormatter(){
            {
                super.bind(StringFormatter.extractDependencies(objectArray));
            }

            @Override
            public void dispose() {
                super.unbind(StringFormatter.extractDependencies(objectArray));
            }

            @Override
            protected String computeValue() {
                StringBuilder stringBuilder = new StringBuilder();
                for (Object object : objectArray) {
                    stringBuilder.append(StringFormatter.extractValue(object));
                }
                return stringBuilder.toString();
            }

            @Override
            public ObservableList<ObservableValue<?>> getDependencies() {
                return FXCollections.unmodifiableObservableList(FXCollections.observableArrayList(StringFormatter.extractDependencies(objectArray)));
            }
        };
    }

    public static StringExpression format(final Locale locale, final String string, final Object ... objectArray) {
        if (string == null) {
            throw new NullPointerException("Format cannot be null.");
        }
        if (StringFormatter.extractDependencies(objectArray).length == 0) {
            return StringConstant.valueOf(String.format(locale, string, objectArray));
        }
        StringFormatter stringFormatter = new StringFormatter(){
            {
                super.bind(StringFormatter.extractDependencies(objectArray));
            }

            @Override
            public void dispose() {
                super.unbind(StringFormatter.extractDependencies(objectArray));
            }

            @Override
            protected String computeValue() {
                Object[] objectArray2 = StringFormatter.extractValues(objectArray);
                return String.format(locale, string, objectArray2);
            }

            @Override
            public ObservableList<ObservableValue<?>> getDependencies() {
                return FXCollections.unmodifiableObservableList(FXCollections.observableArrayList(StringFormatter.extractDependencies(objectArray)));
            }
        };
        stringFormatter.get();
        return stringFormatter;
    }

    public static StringExpression format(final String string, final Object ... objectArray) {
        if (string == null) {
            throw new NullPointerException("Format cannot be null.");
        }
        if (StringFormatter.extractDependencies(objectArray).length == 0) {
            return StringConstant.valueOf(String.format(string, objectArray));
        }
        StringFormatter stringFormatter = new StringFormatter(){
            {
                super.bind(StringFormatter.extractDependencies(objectArray));
            }

            @Override
            public void dispose() {
                super.unbind(StringFormatter.extractDependencies(objectArray));
            }

            @Override
            protected String computeValue() {
                Object[] objectArray2 = StringFormatter.extractValues(objectArray);
                return String.format(string, objectArray2);
            }

            @Override
            public ObservableList<ObservableValue<?>> getDependencies() {
                return FXCollections.unmodifiableObservableList(FXCollections.observableArrayList(StringFormatter.extractDependencies(objectArray)));
            }
        };
        stringFormatter.get();
        return stringFormatter;
    }
}

