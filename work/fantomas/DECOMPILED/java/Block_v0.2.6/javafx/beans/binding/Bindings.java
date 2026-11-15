/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.binding;

import com.sun.javafx.binding.BidirectionalBinding;
import com.sun.javafx.binding.BidirectionalContentBinding;
import com.sun.javafx.binding.ContentBinding;
import com.sun.javafx.binding.DoubleConstant;
import com.sun.javafx.binding.FloatConstant;
import com.sun.javafx.binding.IntegerConstant;
import com.sun.javafx.binding.Logging;
import com.sun.javafx.binding.LongConstant;
import com.sun.javafx.binding.ObjectConstant;
import com.sun.javafx.binding.SelectBinding;
import com.sun.javafx.binding.StringConstant;
import com.sun.javafx.binding.StringFormatter;
import com.sun.javafx.collections.ImmutableObservableList;
import java.lang.ref.WeakReference;
import java.text.Format;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.FloatBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.LongBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.binding.StringExpression;
import javafx.beans.binding.When;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableFloatValue;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableLongValue;
import javafx.beans.value.ObservableNumberValue;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableFloatArray;
import javafx.collections.ObservableIntegerArray;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.util.StringConverter;

public final class Bindings {
    private Bindings() {
    }

    public static BooleanBinding createBooleanBinding(final Callable<Boolean> callable, final Observable ... observableArray) {
        return new BooleanBinding(){
            {
                this.bind(observableArray);
            }

            @Override
            protected boolean computeValue() {
                try {
                    return (Boolean)callable.call();
                }
                catch (Exception exception) {
                    Logging.getLogger().warning("Exception while evaluating binding", exception);
                    return false;
                }
            }

            @Override
            public void dispose() {
                super.unbind(observableArray);
            }

            @Override
            public ObservableList<?> getDependencies() {
                return observableArray == null || observableArray.length == 0 ? FXCollections.emptyObservableList() : (observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray));
            }
        };
    }

    public static DoubleBinding createDoubleBinding(final Callable<Double> callable, final Observable ... observableArray) {
        return new DoubleBinding(){
            {
                this.bind(observableArray);
            }

            @Override
            protected double computeValue() {
                try {
                    return (Double)callable.call();
                }
                catch (Exception exception) {
                    Logging.getLogger().warning("Exception while evaluating binding", exception);
                    return 0.0;
                }
            }

            @Override
            public void dispose() {
                super.unbind(observableArray);
            }

            @Override
            public ObservableList<?> getDependencies() {
                return observableArray == null || observableArray.length == 0 ? FXCollections.emptyObservableList() : (observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray));
            }
        };
    }

    public static FloatBinding createFloatBinding(final Callable<Float> callable, final Observable ... observableArray) {
        return new FloatBinding(){
            {
                this.bind(observableArray);
            }

            @Override
            protected float computeValue() {
                try {
                    return ((Float)callable.call()).floatValue();
                }
                catch (Exception exception) {
                    Logging.getLogger().warning("Exception while evaluating binding", exception);
                    return 0.0f;
                }
            }

            @Override
            public void dispose() {
                super.unbind(observableArray);
            }

            @Override
            public ObservableList<?> getDependencies() {
                return observableArray == null || observableArray.length == 0 ? FXCollections.emptyObservableList() : (observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray));
            }
        };
    }

    public static IntegerBinding createIntegerBinding(final Callable<Integer> callable, final Observable ... observableArray) {
        return new IntegerBinding(){
            {
                this.bind(observableArray);
            }

            @Override
            protected int computeValue() {
                try {
                    return (Integer)callable.call();
                }
                catch (Exception exception) {
                    Logging.getLogger().warning("Exception while evaluating binding", exception);
                    return 0;
                }
            }

            @Override
            public void dispose() {
                super.unbind(observableArray);
            }

            @Override
            public ObservableList<?> getDependencies() {
                return observableArray == null || observableArray.length == 0 ? FXCollections.emptyObservableList() : (observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray));
            }
        };
    }

    public static LongBinding createLongBinding(final Callable<Long> callable, final Observable ... observableArray) {
        return new LongBinding(){
            {
                this.bind(observableArray);
            }

            @Override
            protected long computeValue() {
                try {
                    return (Long)callable.call();
                }
                catch (Exception exception) {
                    Logging.getLogger().warning("Exception while evaluating binding", exception);
                    return 0L;
                }
            }

            @Override
            public void dispose() {
                super.unbind(observableArray);
            }

            @Override
            public ObservableList<?> getDependencies() {
                return observableArray == null || observableArray.length == 0 ? FXCollections.emptyObservableList() : (observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray));
            }
        };
    }

    public static <T> ObjectBinding<T> createObjectBinding(final Callable<T> callable, final Observable ... observableArray) {
        return new ObjectBinding<T>(){
            {
                this.bind(observableArray);
            }

            @Override
            protected T computeValue() {
                try {
                    return callable.call();
                }
                catch (Exception exception) {
                    Logging.getLogger().warning("Exception while evaluating binding", exception);
                    return null;
                }
            }

            @Override
            public void dispose() {
                super.unbind(observableArray);
            }

            @Override
            public ObservableList<?> getDependencies() {
                return observableArray == null || observableArray.length == 0 ? FXCollections.emptyObservableList() : (observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray));
            }
        };
    }

    public static StringBinding createStringBinding(final Callable<String> callable, final Observable ... observableArray) {
        return new StringBinding(){
            {
                this.bind(observableArray);
            }

            @Override
            protected String computeValue() {
                try {
                    return (String)callable.call();
                }
                catch (Exception exception) {
                    Logging.getLogger().warning("Exception while evaluating binding", exception);
                    return "";
                }
            }

            @Override
            public void dispose() {
                super.unbind(observableArray);
            }

            @Override
            public ObservableList<?> getDependencies() {
                return observableArray == null || observableArray.length == 0 ? FXCollections.emptyObservableList() : (observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray));
            }
        };
    }

    public static <T> ObjectBinding<T> select(ObservableValue<?> observableValue, String ... stringArray) {
        return new SelectBinding.AsObject(observableValue, stringArray);
    }

    public static DoubleBinding selectDouble(ObservableValue<?> observableValue, String ... stringArray) {
        return new SelectBinding.AsDouble(observableValue, stringArray);
    }

    public static FloatBinding selectFloat(ObservableValue<?> observableValue, String ... stringArray) {
        return new SelectBinding.AsFloat(observableValue, stringArray);
    }

    public static IntegerBinding selectInteger(ObservableValue<?> observableValue, String ... stringArray) {
        return new SelectBinding.AsInteger(observableValue, stringArray);
    }

    public static LongBinding selectLong(ObservableValue<?> observableValue, String ... stringArray) {
        return new SelectBinding.AsLong(observableValue, stringArray);
    }

    public static BooleanBinding selectBoolean(ObservableValue<?> observableValue, String ... stringArray) {
        return new SelectBinding.AsBoolean(observableValue, stringArray);
    }

    public static StringBinding selectString(ObservableValue<?> observableValue, String ... stringArray) {
        return new SelectBinding.AsString(observableValue, stringArray);
    }

    public static <T> ObjectBinding<T> select(Object object, String ... stringArray) {
        return new SelectBinding.AsObject(object, stringArray);
    }

    public static DoubleBinding selectDouble(Object object, String ... stringArray) {
        return new SelectBinding.AsDouble(object, stringArray);
    }

    public static FloatBinding selectFloat(Object object, String ... stringArray) {
        return new SelectBinding.AsFloat(object, stringArray);
    }

    public static IntegerBinding selectInteger(Object object, String ... stringArray) {
        return new SelectBinding.AsInteger(object, stringArray);
    }

    public static LongBinding selectLong(Object object, String ... stringArray) {
        return new SelectBinding.AsLong(object, stringArray);
    }

    public static BooleanBinding selectBoolean(Object object, String ... stringArray) {
        return new SelectBinding.AsBoolean(object, stringArray);
    }

    public static StringBinding selectString(Object object, String ... stringArray) {
        return new SelectBinding.AsString(object, stringArray);
    }

    public static When when(ObservableBooleanValue observableBooleanValue) {
        return new When(observableBooleanValue);
    }

    public static <T> void bindBidirectional(Property<T> property, Property<T> property2) {
        BidirectionalBinding.bind(property, property2);
    }

    public static <T> void unbindBidirectional(Property<T> property, Property<T> property2) {
        BidirectionalBinding.unbind(property, property2);
    }

    public static void unbindBidirectional(Object object, Object object2) {
        BidirectionalBinding.unbind(object, object2);
    }

    public static void bindBidirectional(Property<String> property, Property<?> property2, Format format) {
        BidirectionalBinding.bind(property, property2, format);
    }

    public static <T> void bindBidirectional(Property<String> property, Property<T> property2, StringConverter<T> stringConverter) {
        BidirectionalBinding.bind(property, property2, stringConverter);
    }

    public static <E> void bindContentBidirectional(ObservableList<E> observableList, ObservableList<E> observableList2) {
        BidirectionalContentBinding.bind(observableList, observableList2);
    }

    public static <E> void bindContentBidirectional(ObservableSet<E> observableSet, ObservableSet<E> observableSet2) {
        BidirectionalContentBinding.bind(observableSet, observableSet2);
    }

    public static <K, V> void bindContentBidirectional(ObservableMap<K, V> observableMap, ObservableMap<K, V> observableMap2) {
        BidirectionalContentBinding.bind(observableMap, observableMap2);
    }

    public static void unbindContentBidirectional(Object object, Object object2) {
        BidirectionalContentBinding.unbind(object, object2);
    }

    public static <E> void bindContent(List<E> list, ObservableList<? extends E> observableList) {
        ContentBinding.bind(list, observableList);
    }

    public static <E> void bindContent(Set<E> set, ObservableSet<? extends E> observableSet) {
        ContentBinding.bind(set, observableSet);
    }

    public static <K, V> void bindContent(Map<K, V> map, ObservableMap<? extends K, ? extends V> observableMap) {
        ContentBinding.bind(map, observableMap);
    }

    public static void unbindContent(Object object, Object object2) {
        ContentBinding.unbind(object, object2);
    }

    public static NumberBinding negate(final ObservableNumberValue observableNumberValue) {
        if (observableNumberValue == null) {
            throw new NullPointerException("Operand cannot be null.");
        }
        if (observableNumberValue instanceof ObservableDoubleValue) {
            return new DoubleBinding(){
                {
                    super.bind(observableNumberValue);
                }

                @Override
                public void dispose() {
                    super.unbind(observableNumberValue);
                }

                @Override
                protected double computeValue() {
                    return -observableNumberValue.doubleValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return FXCollections.singletonObservableList(observableNumberValue);
                }
            };
        }
        if (observableNumberValue instanceof ObservableFloatValue) {
            return new FloatBinding(){
                {
                    super.bind(observableNumberValue);
                }

                @Override
                public void dispose() {
                    super.unbind(observableNumberValue);
                }

                @Override
                protected float computeValue() {
                    return -observableNumberValue.floatValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return FXCollections.singletonObservableList(observableNumberValue);
                }
            };
        }
        if (observableNumberValue instanceof ObservableLongValue) {
            return new LongBinding(){
                {
                    super.bind(observableNumberValue);
                }

                @Override
                public void dispose() {
                    super.unbind(observableNumberValue);
                }

                @Override
                protected long computeValue() {
                    return -observableNumberValue.longValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return FXCollections.singletonObservableList(observableNumberValue);
                }
            };
        }
        return new IntegerBinding(){
            {
                super.bind(observableNumberValue);
            }

            @Override
            public void dispose() {
                super.unbind(observableNumberValue);
            }

            @Override
            protected int computeValue() {
                return -observableNumberValue.intValue();
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(observableNumberValue);
            }
        };
    }

    private static NumberBinding add(final ObservableNumberValue observableNumberValue, final ObservableNumberValue observableNumberValue2, final Observable ... observableArray) {
        if (observableNumberValue == null || observableNumberValue2 == null) {
            throw new NullPointerException("Operands cannot be null.");
        }
        assert (observableArray != null && observableArray.length > 0);
        if (observableNumberValue instanceof ObservableDoubleValue || observableNumberValue2 instanceof ObservableDoubleValue) {
            return new DoubleBinding(){
                {
                    super.bind(observableArray);
                }

                @Override
                public void dispose() {
                    super.unbind(observableArray);
                }

                @Override
                protected double computeValue() {
                    return observableNumberValue.doubleValue() + observableNumberValue2.doubleValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
                }
            };
        }
        if (observableNumberValue instanceof ObservableFloatValue || observableNumberValue2 instanceof ObservableFloatValue) {
            return new FloatBinding(){
                {
                    super.bind(observableArray);
                }

                @Override
                public void dispose() {
                    super.unbind(observableArray);
                }

                @Override
                protected float computeValue() {
                    return observableNumberValue.floatValue() + observableNumberValue2.floatValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
                }
            };
        }
        if (observableNumberValue instanceof ObservableLongValue || observableNumberValue2 instanceof ObservableLongValue) {
            return new LongBinding(){
                {
                    super.bind(observableArray);
                }

                @Override
                public void dispose() {
                    super.unbind(observableArray);
                }

                @Override
                protected long computeValue() {
                    return observableNumberValue.longValue() + observableNumberValue2.longValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
                }
            };
        }
        return new IntegerBinding(){
            {
                super.bind(observableArray);
            }

            @Override
            public void dispose() {
                super.unbind(observableArray);
            }

            @Override
            protected int computeValue() {
                return observableNumberValue.intValue() + observableNumberValue2.intValue();
            }

            @Override
            public ObservableList<?> getDependencies() {
                return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
            }
        };
    }

    public static NumberBinding add(ObservableNumberValue observableNumberValue, ObservableNumberValue observableNumberValue2) {
        return Bindings.add(observableNumberValue, observableNumberValue2, observableNumberValue, observableNumberValue2);
    }

    public static DoubleBinding add(ObservableNumberValue observableNumberValue, double d) {
        return (DoubleBinding)Bindings.add(observableNumberValue, DoubleConstant.valueOf(d), observableNumberValue);
    }

    public static DoubleBinding add(double d, ObservableNumberValue observableNumberValue) {
        return (DoubleBinding)Bindings.add(DoubleConstant.valueOf(d), observableNumberValue, observableNumberValue);
    }

    public static NumberBinding add(ObservableNumberValue observableNumberValue, float f) {
        return Bindings.add(observableNumberValue, FloatConstant.valueOf(f), observableNumberValue);
    }

    public static NumberBinding add(float f, ObservableNumberValue observableNumberValue) {
        return Bindings.add(FloatConstant.valueOf(f), observableNumberValue, observableNumberValue);
    }

    public static NumberBinding add(ObservableNumberValue observableNumberValue, long l) {
        return Bindings.add(observableNumberValue, LongConstant.valueOf(l), observableNumberValue);
    }

    public static NumberBinding add(long l, ObservableNumberValue observableNumberValue) {
        return Bindings.add(LongConstant.valueOf(l), observableNumberValue, observableNumberValue);
    }

    public static NumberBinding add(ObservableNumberValue observableNumberValue, int n) {
        return Bindings.add(observableNumberValue, IntegerConstant.valueOf(n), observableNumberValue);
    }

    public static NumberBinding add(int n, ObservableNumberValue observableNumberValue) {
        return Bindings.add(IntegerConstant.valueOf(n), observableNumberValue, observableNumberValue);
    }

    private static NumberBinding subtract(final ObservableNumberValue observableNumberValue, final ObservableNumberValue observableNumberValue2, final Observable ... observableArray) {
        if (observableNumberValue == null || observableNumberValue2 == null) {
            throw new NullPointerException("Operands cannot be null.");
        }
        assert (observableArray != null && observableArray.length > 0);
        if (observableNumberValue instanceof ObservableDoubleValue || observableNumberValue2 instanceof ObservableDoubleValue) {
            return new DoubleBinding(){
                {
                    super.bind(observableArray);
                }

                @Override
                public void dispose() {
                    super.unbind(observableArray);
                }

                @Override
                protected double computeValue() {
                    return observableNumberValue.doubleValue() - observableNumberValue2.doubleValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
                }
            };
        }
        if (observableNumberValue instanceof ObservableFloatValue || observableNumberValue2 instanceof ObservableFloatValue) {
            return new FloatBinding(){
                {
                    super.bind(observableArray);
                }

                @Override
                public void dispose() {
                    super.unbind(observableArray);
                }

                @Override
                protected float computeValue() {
                    return observableNumberValue.floatValue() - observableNumberValue2.floatValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
                }
            };
        }
        if (observableNumberValue instanceof ObservableLongValue || observableNumberValue2 instanceof ObservableLongValue) {
            return new LongBinding(){
                {
                    super.bind(observableArray);
                }

                @Override
                public void dispose() {
                    super.unbind(observableArray);
                }

                @Override
                protected long computeValue() {
                    return observableNumberValue.longValue() - observableNumberValue2.longValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
                }
            };
        }
        return new IntegerBinding(){
            {
                super.bind(observableArray);
            }

            @Override
            public void dispose() {
                super.unbind(observableArray);
            }

            @Override
            protected int computeValue() {
                return observableNumberValue.intValue() - observableNumberValue2.intValue();
            }

            @Override
            public ObservableList<?> getDependencies() {
                return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
            }
        };
    }

    public static NumberBinding subtract(ObservableNumberValue observableNumberValue, ObservableNumberValue observableNumberValue2) {
        return Bindings.subtract(observableNumberValue, observableNumberValue2, observableNumberValue, observableNumberValue2);
    }

    public static DoubleBinding subtract(ObservableNumberValue observableNumberValue, double d) {
        return (DoubleBinding)Bindings.subtract(observableNumberValue, DoubleConstant.valueOf(d), observableNumberValue);
    }

    public static DoubleBinding subtract(double d, ObservableNumberValue observableNumberValue) {
        return (DoubleBinding)Bindings.subtract(DoubleConstant.valueOf(d), observableNumberValue, observableNumberValue);
    }

    public static NumberBinding subtract(ObservableNumberValue observableNumberValue, float f) {
        return Bindings.subtract(observableNumberValue, FloatConstant.valueOf(f), observableNumberValue);
    }

    public static NumberBinding subtract(float f, ObservableNumberValue observableNumberValue) {
        return Bindings.subtract(FloatConstant.valueOf(f), observableNumberValue, observableNumberValue);
    }

    public static NumberBinding subtract(ObservableNumberValue observableNumberValue, long l) {
        return Bindings.subtract(observableNumberValue, LongConstant.valueOf(l), observableNumberValue);
    }

    public static NumberBinding subtract(long l, ObservableNumberValue observableNumberValue) {
        return Bindings.subtract(LongConstant.valueOf(l), observableNumberValue, observableNumberValue);
    }

    public static NumberBinding subtract(ObservableNumberValue observableNumberValue, int n) {
        return Bindings.subtract(observableNumberValue, IntegerConstant.valueOf(n), observableNumberValue);
    }

    public static NumberBinding subtract(int n, ObservableNumberValue observableNumberValue) {
        return Bindings.subtract(IntegerConstant.valueOf(n), observableNumberValue, observableNumberValue);
    }

    private static NumberBinding multiply(final ObservableNumberValue observableNumberValue, final ObservableNumberValue observableNumberValue2, final Observable ... observableArray) {
        if (observableNumberValue == null || observableNumberValue2 == null) {
            throw new NullPointerException("Operands cannot be null.");
        }
        assert (observableArray != null && observableArray.length > 0);
        if (observableNumberValue instanceof ObservableDoubleValue || observableNumberValue2 instanceof ObservableDoubleValue) {
            return new DoubleBinding(){
                {
                    super.bind(observableArray);
                }

                @Override
                public void dispose() {
                    super.unbind(observableArray);
                }

                @Override
                protected double computeValue() {
                    return observableNumberValue.doubleValue() * observableNumberValue2.doubleValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
                }
            };
        }
        if (observableNumberValue instanceof ObservableFloatValue || observableNumberValue2 instanceof ObservableFloatValue) {
            return new FloatBinding(){
                {
                    super.bind(observableArray);
                }

                @Override
                public void dispose() {
                    super.unbind(observableArray);
                }

                @Override
                protected float computeValue() {
                    return observableNumberValue.floatValue() * observableNumberValue2.floatValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
                }
            };
        }
        if (observableNumberValue instanceof ObservableLongValue || observableNumberValue2 instanceof ObservableLongValue) {
            return new LongBinding(){
                {
                    super.bind(observableArray);
                }

                @Override
                public void dispose() {
                    super.unbind(observableArray);
                }

                @Override
                protected long computeValue() {
                    return observableNumberValue.longValue() * observableNumberValue2.longValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
                }
            };
        }
        return new IntegerBinding(){
            {
                super.bind(observableArray);
            }

            @Override
            public void dispose() {
                super.unbind(observableArray);
            }

            @Override
            protected int computeValue() {
                return observableNumberValue.intValue() * observableNumberValue2.intValue();
            }

            @Override
            public ObservableList<?> getDependencies() {
                return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
            }
        };
    }

    public static NumberBinding multiply(ObservableNumberValue observableNumberValue, ObservableNumberValue observableNumberValue2) {
        return Bindings.multiply(observableNumberValue, observableNumberValue2, observableNumberValue, observableNumberValue2);
    }

    public static DoubleBinding multiply(ObservableNumberValue observableNumberValue, double d) {
        return (DoubleBinding)Bindings.multiply(observableNumberValue, DoubleConstant.valueOf(d), observableNumberValue);
    }

    public static DoubleBinding multiply(double d, ObservableNumberValue observableNumberValue) {
        return (DoubleBinding)Bindings.multiply(DoubleConstant.valueOf(d), observableNumberValue, observableNumberValue);
    }

    public static NumberBinding multiply(ObservableNumberValue observableNumberValue, float f) {
        return Bindings.multiply(observableNumberValue, FloatConstant.valueOf(f), observableNumberValue);
    }

    public static NumberBinding multiply(float f, ObservableNumberValue observableNumberValue) {
        return Bindings.multiply(FloatConstant.valueOf(f), observableNumberValue, observableNumberValue);
    }

    public static NumberBinding multiply(ObservableNumberValue observableNumberValue, long l) {
        return Bindings.multiply(observableNumberValue, LongConstant.valueOf(l), observableNumberValue);
    }

    public static NumberBinding multiply(long l, ObservableNumberValue observableNumberValue) {
        return Bindings.multiply(LongConstant.valueOf(l), observableNumberValue, observableNumberValue);
    }

    public static NumberBinding multiply(ObservableNumberValue observableNumberValue, int n) {
        return Bindings.multiply(observableNumberValue, IntegerConstant.valueOf(n), observableNumberValue);
    }

    public static NumberBinding multiply(int n, ObservableNumberValue observableNumberValue) {
        return Bindings.multiply(IntegerConstant.valueOf(n), observableNumberValue, observableNumberValue);
    }

    private static NumberBinding divide(final ObservableNumberValue observableNumberValue, final ObservableNumberValue observableNumberValue2, final Observable ... observableArray) {
        if (observableNumberValue == null || observableNumberValue2 == null) {
            throw new NullPointerException("Operands cannot be null.");
        }
        assert (observableArray != null && observableArray.length > 0);
        if (observableNumberValue instanceof ObservableDoubleValue || observableNumberValue2 instanceof ObservableDoubleValue) {
            return new DoubleBinding(){
                {
                    super.bind(observableArray);
                }

                @Override
                public void dispose() {
                    super.unbind(observableArray);
                }

                @Override
                protected double computeValue() {
                    return observableNumberValue.doubleValue() / observableNumberValue2.doubleValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
                }
            };
        }
        if (observableNumberValue instanceof ObservableFloatValue || observableNumberValue2 instanceof ObservableFloatValue) {
            return new FloatBinding(){
                {
                    super.bind(observableArray);
                }

                @Override
                public void dispose() {
                    super.unbind(observableArray);
                }

                @Override
                protected float computeValue() {
                    return observableNumberValue.floatValue() / observableNumberValue2.floatValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
                }
            };
        }
        if (observableNumberValue instanceof ObservableLongValue || observableNumberValue2 instanceof ObservableLongValue) {
            return new LongBinding(){
                {
                    super.bind(observableArray);
                }

                @Override
                public void dispose() {
                    super.unbind(observableArray);
                }

                @Override
                protected long computeValue() {
                    return observableNumberValue.longValue() / observableNumberValue2.longValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
                }
            };
        }
        return new IntegerBinding(){
            {
                super.bind(observableArray);
            }

            @Override
            public void dispose() {
                super.unbind(observableArray);
            }

            @Override
            protected int computeValue() {
                return observableNumberValue.intValue() / observableNumberValue2.intValue();
            }

            @Override
            public ObservableList<?> getDependencies() {
                return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
            }
        };
    }

    public static NumberBinding divide(ObservableNumberValue observableNumberValue, ObservableNumberValue observableNumberValue2) {
        return Bindings.divide(observableNumberValue, observableNumberValue2, observableNumberValue, observableNumberValue2);
    }

    public static DoubleBinding divide(ObservableNumberValue observableNumberValue, double d) {
        return (DoubleBinding)Bindings.divide(observableNumberValue, DoubleConstant.valueOf(d), observableNumberValue);
    }

    public static DoubleBinding divide(double d, ObservableNumberValue observableNumberValue) {
        return (DoubleBinding)Bindings.divide(DoubleConstant.valueOf(d), observableNumberValue, observableNumberValue);
    }

    public static NumberBinding divide(ObservableNumberValue observableNumberValue, float f) {
        return Bindings.divide(observableNumberValue, FloatConstant.valueOf(f), observableNumberValue);
    }

    public static NumberBinding divide(float f, ObservableNumberValue observableNumberValue) {
        return Bindings.divide(FloatConstant.valueOf(f), observableNumberValue, observableNumberValue);
    }

    public static NumberBinding divide(ObservableNumberValue observableNumberValue, long l) {
        return Bindings.divide(observableNumberValue, LongConstant.valueOf(l), observableNumberValue);
    }

    public static NumberBinding divide(long l, ObservableNumberValue observableNumberValue) {
        return Bindings.divide(LongConstant.valueOf(l), observableNumberValue, observableNumberValue);
    }

    public static NumberBinding divide(ObservableNumberValue observableNumberValue, int n) {
        return Bindings.divide(observableNumberValue, IntegerConstant.valueOf(n), observableNumberValue);
    }

    public static NumberBinding divide(int n, ObservableNumberValue observableNumberValue) {
        return Bindings.divide(IntegerConstant.valueOf(n), observableNumberValue, observableNumberValue);
    }

    private static BooleanBinding equal(final ObservableNumberValue observableNumberValue, final ObservableNumberValue observableNumberValue2, final double d, final Observable ... observableArray) {
        if (observableNumberValue == null || observableNumberValue2 == null) {
            throw new NullPointerException("Operands cannot be null.");
        }
        assert (observableArray != null && observableArray.length > 0);
        if (observableNumberValue instanceof ObservableDoubleValue || observableNumberValue2 instanceof ObservableDoubleValue) {
            return new BooleanBinding(){
                {
                    super.bind(observableArray);
                }

                @Override
                public void dispose() {
                    super.unbind(observableArray);
                }

                @Override
                protected boolean computeValue() {
                    return Math.abs(observableNumberValue.doubleValue() - observableNumberValue2.doubleValue()) <= d;
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
                }
            };
        }
        if (observableNumberValue instanceof ObservableFloatValue || observableNumberValue2 instanceof ObservableFloatValue) {
            return new BooleanBinding(){
                {
                    super.bind(observableArray);
                }

                @Override
                public void dispose() {
                    super.unbind(observableArray);
                }

                @Override
                protected boolean computeValue() {
                    return (double)Math.abs(observableNumberValue.floatValue() - observableNumberValue2.floatValue()) <= d;
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
                }
            };
        }
        if (observableNumberValue instanceof ObservableLongValue || observableNumberValue2 instanceof ObservableLongValue) {
            return new BooleanBinding(){
                {
                    super.bind(observableArray);
                }

                @Override
                public void dispose() {
                    super.unbind(observableArray);
                }

                @Override
                protected boolean computeValue() {
                    return (double)Math.abs(observableNumberValue.longValue() - observableNumberValue2.longValue()) <= d;
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
                }
            };
        }
        return new BooleanBinding(){
            {
                super.bind(observableArray);
            }

            @Override
            public void dispose() {
                super.unbind(observableArray);
            }

            @Override
            protected boolean computeValue() {
                return (double)Math.abs(observableNumberValue.intValue() - observableNumberValue2.intValue()) <= d;
            }

            @Override
            public ObservableList<?> getDependencies() {
                return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
            }
        };
    }

    public static BooleanBinding equal(ObservableNumberValue observableNumberValue, ObservableNumberValue observableNumberValue2, double d) {
        return Bindings.equal(observableNumberValue, observableNumberValue2, d, observableNumberValue, observableNumberValue2);
    }

    public static BooleanBinding equal(ObservableNumberValue observableNumberValue, ObservableNumberValue observableNumberValue2) {
        return Bindings.equal(observableNumberValue, observableNumberValue2, 0.0, observableNumberValue, observableNumberValue2);
    }

    public static BooleanBinding equal(ObservableNumberValue observableNumberValue, double d, double d2) {
        return Bindings.equal(observableNumberValue, DoubleConstant.valueOf(d), d2, observableNumberValue);
    }

    public static BooleanBinding equal(double d, ObservableNumberValue observableNumberValue, double d2) {
        return Bindings.equal(DoubleConstant.valueOf(d), observableNumberValue, d2, observableNumberValue);
    }

    public static BooleanBinding equal(ObservableNumberValue observableNumberValue, float f, double d) {
        return Bindings.equal(observableNumberValue, FloatConstant.valueOf(f), d, observableNumberValue);
    }

    public static BooleanBinding equal(float f, ObservableNumberValue observableNumberValue, double d) {
        return Bindings.equal(FloatConstant.valueOf(f), observableNumberValue, d, observableNumberValue);
    }

    public static BooleanBinding equal(ObservableNumberValue observableNumberValue, long l, double d) {
        return Bindings.equal(observableNumberValue, LongConstant.valueOf(l), d, observableNumberValue);
    }

    public static BooleanBinding equal(ObservableNumberValue observableNumberValue, long l) {
        return Bindings.equal(observableNumberValue, LongConstant.valueOf(l), 0.0, observableNumberValue);
    }

    public static BooleanBinding equal(long l, ObservableNumberValue observableNumberValue, double d) {
        return Bindings.equal(LongConstant.valueOf(l), observableNumberValue, d, observableNumberValue);
    }

    public static BooleanBinding equal(long l, ObservableNumberValue observableNumberValue) {
        return Bindings.equal(LongConstant.valueOf(l), observableNumberValue, 0.0, observableNumberValue);
    }

    public static BooleanBinding equal(ObservableNumberValue observableNumberValue, int n, double d) {
        return Bindings.equal(observableNumberValue, IntegerConstant.valueOf(n), d, observableNumberValue);
    }

    public static BooleanBinding equal(ObservableNumberValue observableNumberValue, int n) {
        return Bindings.equal(observableNumberValue, IntegerConstant.valueOf(n), 0.0, observableNumberValue);
    }

    public static BooleanBinding equal(int n, ObservableNumberValue observableNumberValue, double d) {
        return Bindings.equal(IntegerConstant.valueOf(n), observableNumberValue, d, observableNumberValue);
    }

    public static BooleanBinding equal(int n, ObservableNumberValue observableNumberValue) {
        return Bindings.equal(IntegerConstant.valueOf(n), observableNumberValue, 0.0, observableNumberValue);
    }

    private static BooleanBinding notEqual(final ObservableNumberValue observableNumberValue, final ObservableNumberValue observableNumberValue2, final double d, final Observable ... observableArray) {
        if (observableNumberValue == null || observableNumberValue2 == null) {
            throw new NullPointerException("Operands cannot be null.");
        }
        assert (observableArray != null && observableArray.length > 0);
        if (observableNumberValue instanceof ObservableDoubleValue || observableNumberValue2 instanceof ObservableDoubleValue) {
            return new BooleanBinding(){
                {
                    super.bind(observableArray);
                }

                @Override
                public void dispose() {
                    super.unbind(observableArray);
                }

                @Override
                protected boolean computeValue() {
                    return Math.abs(observableNumberValue.doubleValue() - observableNumberValue2.doubleValue()) > d;
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
                }
            };
        }
        if (observableNumberValue instanceof ObservableFloatValue || observableNumberValue2 instanceof ObservableFloatValue) {
            return new BooleanBinding(){
                {
                    super.bind(observableArray);
                }

                @Override
                public void dispose() {
                    super.unbind(observableArray);
                }

                @Override
                protected boolean computeValue() {
                    return (double)Math.abs(observableNumberValue.floatValue() - observableNumberValue2.floatValue()) > d;
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
                }
            };
        }
        if (observableNumberValue instanceof ObservableLongValue || observableNumberValue2 instanceof ObservableLongValue) {
            return new BooleanBinding(){
                {
                    super.bind(observableArray);
                }

                @Override
                public void dispose() {
                    super.unbind(observableArray);
                }

                @Override
                protected boolean computeValue() {
                    return (double)Math.abs(observableNumberValue.longValue() - observableNumberValue2.longValue()) > d;
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
                }
            };
        }
        return new BooleanBinding(){
            {
                super.bind(observableArray);
            }

            @Override
            public void dispose() {
                super.unbind(observableArray);
            }

            @Override
            protected boolean computeValue() {
                return (double)Math.abs(observableNumberValue.intValue() - observableNumberValue2.intValue()) > d;
            }

            @Override
            public ObservableList<?> getDependencies() {
                return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
            }
        };
    }

    public static BooleanBinding notEqual(ObservableNumberValue observableNumberValue, ObservableNumberValue observableNumberValue2, double d) {
        return Bindings.notEqual(observableNumberValue, observableNumberValue2, d, observableNumberValue, observableNumberValue2);
    }

    public static BooleanBinding notEqual(ObservableNumberValue observableNumberValue, ObservableNumberValue observableNumberValue2) {
        return Bindings.notEqual(observableNumberValue, observableNumberValue2, 0.0, observableNumberValue, observableNumberValue2);
    }

    public static BooleanBinding notEqual(ObservableNumberValue observableNumberValue, double d, double d2) {
        return Bindings.notEqual(observableNumberValue, DoubleConstant.valueOf(d), d2, observableNumberValue);
    }

    public static BooleanBinding notEqual(double d, ObservableNumberValue observableNumberValue, double d2) {
        return Bindings.notEqual(DoubleConstant.valueOf(d), observableNumberValue, d2, observableNumberValue);
    }

    public static BooleanBinding notEqual(ObservableNumberValue observableNumberValue, float f, double d) {
        return Bindings.notEqual(observableNumberValue, FloatConstant.valueOf(f), d, observableNumberValue);
    }

    public static BooleanBinding notEqual(float f, ObservableNumberValue observableNumberValue, double d) {
        return Bindings.notEqual(FloatConstant.valueOf(f), observableNumberValue, d, observableNumberValue);
    }

    public static BooleanBinding notEqual(ObservableNumberValue observableNumberValue, long l, double d) {
        return Bindings.notEqual(observableNumberValue, LongConstant.valueOf(l), d, observableNumberValue);
    }

    public static BooleanBinding notEqual(ObservableNumberValue observableNumberValue, long l) {
        return Bindings.notEqual(observableNumberValue, LongConstant.valueOf(l), 0.0, observableNumberValue);
    }

    public static BooleanBinding notEqual(long l, ObservableNumberValue observableNumberValue, double d) {
        return Bindings.notEqual(LongConstant.valueOf(l), observableNumberValue, d, observableNumberValue);
    }

    public static BooleanBinding notEqual(long l, ObservableNumberValue observableNumberValue) {
        return Bindings.notEqual(LongConstant.valueOf(l), observableNumberValue, 0.0, observableNumberValue);
    }

    public static BooleanBinding notEqual(ObservableNumberValue observableNumberValue, int n, double d) {
        return Bindings.notEqual(observableNumberValue, IntegerConstant.valueOf(n), d, observableNumberValue);
    }

    public static BooleanBinding notEqual(ObservableNumberValue observableNumberValue, int n) {
        return Bindings.notEqual(observableNumberValue, IntegerConstant.valueOf(n), 0.0, observableNumberValue);
    }

    public static BooleanBinding notEqual(int n, ObservableNumberValue observableNumberValue, double d) {
        return Bindings.notEqual(IntegerConstant.valueOf(n), observableNumberValue, d, observableNumberValue);
    }

    public static BooleanBinding notEqual(int n, ObservableNumberValue observableNumberValue) {
        return Bindings.notEqual(IntegerConstant.valueOf(n), observableNumberValue, 0.0, observableNumberValue);
    }

    private static BooleanBinding greaterThan(final ObservableNumberValue observableNumberValue, final ObservableNumberValue observableNumberValue2, final Observable ... observableArray) {
        if (observableNumberValue == null || observableNumberValue2 == null) {
            throw new NullPointerException("Operands cannot be null.");
        }
        assert (observableArray != null && observableArray.length > 0);
        if (observableNumberValue instanceof ObservableDoubleValue || observableNumberValue2 instanceof ObservableDoubleValue) {
            return new BooleanBinding(){
                {
                    super.bind(observableArray);
                }

                @Override
                public void dispose() {
                    super.unbind(observableArray);
                }

                @Override
                protected boolean computeValue() {
                    return observableNumberValue.doubleValue() > observableNumberValue2.doubleValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
                }
            };
        }
        if (observableNumberValue instanceof ObservableFloatValue || observableNumberValue2 instanceof ObservableFloatValue) {
            return new BooleanBinding(){
                {
                    super.bind(observableArray);
                }

                @Override
                public void dispose() {
                    super.unbind(observableArray);
                }

                @Override
                protected boolean computeValue() {
                    return observableNumberValue.floatValue() > observableNumberValue2.floatValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
                }
            };
        }
        if (observableNumberValue instanceof ObservableLongValue || observableNumberValue2 instanceof ObservableLongValue) {
            return new BooleanBinding(){
                {
                    super.bind(observableArray);
                }

                @Override
                public void dispose() {
                    super.unbind(observableArray);
                }

                @Override
                protected boolean computeValue() {
                    return observableNumberValue.longValue() > observableNumberValue2.longValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
                }
            };
        }
        return new BooleanBinding(){
            {
                super.bind(observableArray);
            }

            @Override
            public void dispose() {
                super.unbind(observableArray);
            }

            @Override
            protected boolean computeValue() {
                return observableNumberValue.intValue() > observableNumberValue2.intValue();
            }

            @Override
            public ObservableList<?> getDependencies() {
                return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
            }
        };
    }

    public static BooleanBinding greaterThan(ObservableNumberValue observableNumberValue, ObservableNumberValue observableNumberValue2) {
        return Bindings.greaterThan(observableNumberValue, observableNumberValue2, observableNumberValue, observableNumberValue2);
    }

    public static BooleanBinding greaterThan(ObservableNumberValue observableNumberValue, double d) {
        return Bindings.greaterThan(observableNumberValue, DoubleConstant.valueOf(d), observableNumberValue);
    }

    public static BooleanBinding greaterThan(double d, ObservableNumberValue observableNumberValue) {
        return Bindings.greaterThan(DoubleConstant.valueOf(d), observableNumberValue, observableNumberValue);
    }

    public static BooleanBinding greaterThan(ObservableNumberValue observableNumberValue, float f) {
        return Bindings.greaterThan(observableNumberValue, FloatConstant.valueOf(f), observableNumberValue);
    }

    public static BooleanBinding greaterThan(float f, ObservableNumberValue observableNumberValue) {
        return Bindings.greaterThan(FloatConstant.valueOf(f), observableNumberValue, observableNumberValue);
    }

    public static BooleanBinding greaterThan(ObservableNumberValue observableNumberValue, long l) {
        return Bindings.greaterThan(observableNumberValue, LongConstant.valueOf(l), observableNumberValue);
    }

    public static BooleanBinding greaterThan(long l, ObservableNumberValue observableNumberValue) {
        return Bindings.greaterThan(LongConstant.valueOf(l), observableNumberValue, observableNumberValue);
    }

    public static BooleanBinding greaterThan(ObservableNumberValue observableNumberValue, int n) {
        return Bindings.greaterThan(observableNumberValue, IntegerConstant.valueOf(n), observableNumberValue);
    }

    public static BooleanBinding greaterThan(int n, ObservableNumberValue observableNumberValue) {
        return Bindings.greaterThan(IntegerConstant.valueOf(n), observableNumberValue, observableNumberValue);
    }

    private static BooleanBinding lessThan(ObservableNumberValue observableNumberValue, ObservableNumberValue observableNumberValue2, Observable ... observableArray) {
        return Bindings.greaterThan(observableNumberValue2, observableNumberValue, observableArray);
    }

    public static BooleanBinding lessThan(ObservableNumberValue observableNumberValue, ObservableNumberValue observableNumberValue2) {
        return Bindings.lessThan(observableNumberValue, observableNumberValue2, observableNumberValue, observableNumberValue2);
    }

    public static BooleanBinding lessThan(ObservableNumberValue observableNumberValue, double d) {
        return Bindings.lessThan(observableNumberValue, DoubleConstant.valueOf(d), observableNumberValue);
    }

    public static BooleanBinding lessThan(double d, ObservableNumberValue observableNumberValue) {
        return Bindings.lessThan(DoubleConstant.valueOf(d), observableNumberValue, observableNumberValue);
    }

    public static BooleanBinding lessThan(ObservableNumberValue observableNumberValue, float f) {
        return Bindings.lessThan(observableNumberValue, FloatConstant.valueOf(f), observableNumberValue);
    }

    public static BooleanBinding lessThan(float f, ObservableNumberValue observableNumberValue) {
        return Bindings.lessThan(FloatConstant.valueOf(f), observableNumberValue, observableNumberValue);
    }

    public static BooleanBinding lessThan(ObservableNumberValue observableNumberValue, long l) {
        return Bindings.lessThan(observableNumberValue, LongConstant.valueOf(l), observableNumberValue);
    }

    public static BooleanBinding lessThan(long l, ObservableNumberValue observableNumberValue) {
        return Bindings.lessThan(LongConstant.valueOf(l), observableNumberValue, observableNumberValue);
    }

    public static BooleanBinding lessThan(ObservableNumberValue observableNumberValue, int n) {
        return Bindings.lessThan(observableNumberValue, IntegerConstant.valueOf(n), observableNumberValue);
    }

    public static BooleanBinding lessThan(int n, ObservableNumberValue observableNumberValue) {
        return Bindings.lessThan(IntegerConstant.valueOf(n), observableNumberValue, observableNumberValue);
    }

    private static BooleanBinding greaterThanOrEqual(final ObservableNumberValue observableNumberValue, final ObservableNumberValue observableNumberValue2, final Observable ... observableArray) {
        if (observableNumberValue == null || observableNumberValue2 == null) {
            throw new NullPointerException("Operands cannot be null.");
        }
        assert (observableArray != null && observableArray.length > 0);
        if (observableNumberValue instanceof ObservableDoubleValue || observableNumberValue2 instanceof ObservableDoubleValue) {
            return new BooleanBinding(){
                {
                    super.bind(observableArray);
                }

                @Override
                public void dispose() {
                    super.unbind(observableArray);
                }

                @Override
                protected boolean computeValue() {
                    return observableNumberValue.doubleValue() >= observableNumberValue2.doubleValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
                }
            };
        }
        if (observableNumberValue instanceof ObservableFloatValue || observableNumberValue2 instanceof ObservableFloatValue) {
            return new BooleanBinding(){
                {
                    super.bind(observableArray);
                }

                @Override
                public void dispose() {
                    super.unbind(observableArray);
                }

                @Override
                protected boolean computeValue() {
                    return observableNumberValue.floatValue() >= observableNumberValue2.floatValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
                }
            };
        }
        if (observableNumberValue instanceof ObservableLongValue || observableNumberValue2 instanceof ObservableLongValue) {
            return new BooleanBinding(){
                {
                    super.bind(observableArray);
                }

                @Override
                public void dispose() {
                    super.unbind(observableArray);
                }

                @Override
                protected boolean computeValue() {
                    return observableNumberValue.longValue() >= observableNumberValue2.longValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
                }
            };
        }
        return new BooleanBinding(){
            {
                super.bind(observableArray);
            }

            @Override
            public void dispose() {
                super.unbind(observableArray);
            }

            @Override
            protected boolean computeValue() {
                return observableNumberValue.intValue() >= observableNumberValue2.intValue();
            }

            @Override
            public ObservableList<?> getDependencies() {
                return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
            }
        };
    }

    public static BooleanBinding greaterThanOrEqual(ObservableNumberValue observableNumberValue, ObservableNumberValue observableNumberValue2) {
        return Bindings.greaterThanOrEqual(observableNumberValue, observableNumberValue2, observableNumberValue, observableNumberValue2);
    }

    public static BooleanBinding greaterThanOrEqual(ObservableNumberValue observableNumberValue, double d) {
        return Bindings.greaterThanOrEqual(observableNumberValue, DoubleConstant.valueOf(d), observableNumberValue);
    }

    public static BooleanBinding greaterThanOrEqual(double d, ObservableNumberValue observableNumberValue) {
        return Bindings.greaterThanOrEqual(DoubleConstant.valueOf(d), observableNumberValue, observableNumberValue);
    }

    public static BooleanBinding greaterThanOrEqual(ObservableNumberValue observableNumberValue, float f) {
        return Bindings.greaterThanOrEqual(observableNumberValue, FloatConstant.valueOf(f), observableNumberValue);
    }

    public static BooleanBinding greaterThanOrEqual(float f, ObservableNumberValue observableNumberValue) {
        return Bindings.greaterThanOrEqual(FloatConstant.valueOf(f), observableNumberValue, observableNumberValue);
    }

    public static BooleanBinding greaterThanOrEqual(ObservableNumberValue observableNumberValue, long l) {
        return Bindings.greaterThanOrEqual(observableNumberValue, LongConstant.valueOf(l), observableNumberValue);
    }

    public static BooleanBinding greaterThanOrEqual(long l, ObservableNumberValue observableNumberValue) {
        return Bindings.greaterThanOrEqual(LongConstant.valueOf(l), observableNumberValue, observableNumberValue);
    }

    public static BooleanBinding greaterThanOrEqual(ObservableNumberValue observableNumberValue, int n) {
        return Bindings.greaterThanOrEqual(observableNumberValue, IntegerConstant.valueOf(n), observableNumberValue);
    }

    public static BooleanBinding greaterThanOrEqual(int n, ObservableNumberValue observableNumberValue) {
        return Bindings.greaterThanOrEqual(IntegerConstant.valueOf(n), observableNumberValue, observableNumberValue);
    }

    private static BooleanBinding lessThanOrEqual(ObservableNumberValue observableNumberValue, ObservableNumberValue observableNumberValue2, Observable ... observableArray) {
        return Bindings.greaterThanOrEqual(observableNumberValue2, observableNumberValue, observableArray);
    }

    public static BooleanBinding lessThanOrEqual(ObservableNumberValue observableNumberValue, ObservableNumberValue observableNumberValue2) {
        return Bindings.lessThanOrEqual(observableNumberValue, observableNumberValue2, observableNumberValue, observableNumberValue2);
    }

    public static BooleanBinding lessThanOrEqual(ObservableNumberValue observableNumberValue, double d) {
        return Bindings.lessThanOrEqual(observableNumberValue, DoubleConstant.valueOf(d), observableNumberValue);
    }

    public static BooleanBinding lessThanOrEqual(double d, ObservableNumberValue observableNumberValue) {
        return Bindings.lessThanOrEqual(DoubleConstant.valueOf(d), observableNumberValue, observableNumberValue);
    }

    public static BooleanBinding lessThanOrEqual(ObservableNumberValue observableNumberValue, float f) {
        return Bindings.lessThanOrEqual(observableNumberValue, FloatConstant.valueOf(f), observableNumberValue);
    }

    public static BooleanBinding lessThanOrEqual(float f, ObservableNumberValue observableNumberValue) {
        return Bindings.lessThanOrEqual(FloatConstant.valueOf(f), observableNumberValue, observableNumberValue);
    }

    public static BooleanBinding lessThanOrEqual(ObservableNumberValue observableNumberValue, long l) {
        return Bindings.lessThanOrEqual(observableNumberValue, LongConstant.valueOf(l), observableNumberValue);
    }

    public static BooleanBinding lessThanOrEqual(long l, ObservableNumberValue observableNumberValue) {
        return Bindings.lessThanOrEqual(LongConstant.valueOf(l), observableNumberValue, observableNumberValue);
    }

    public static BooleanBinding lessThanOrEqual(ObservableNumberValue observableNumberValue, int n) {
        return Bindings.lessThanOrEqual(observableNumberValue, IntegerConstant.valueOf(n), observableNumberValue);
    }

    public static BooleanBinding lessThanOrEqual(int n, ObservableNumberValue observableNumberValue) {
        return Bindings.lessThanOrEqual(IntegerConstant.valueOf(n), observableNumberValue, observableNumberValue);
    }

    private static NumberBinding min(final ObservableNumberValue observableNumberValue, final ObservableNumberValue observableNumberValue2, final Observable ... observableArray) {
        if (observableNumberValue == null || observableNumberValue2 == null) {
            throw new NullPointerException("Operands cannot be null.");
        }
        assert (observableArray != null && observableArray.length > 0);
        if (observableNumberValue instanceof ObservableDoubleValue || observableNumberValue2 instanceof ObservableDoubleValue) {
            return new DoubleBinding(){
                {
                    super.bind(observableArray);
                }

                @Override
                public void dispose() {
                    super.unbind(observableArray);
                }

                @Override
                protected double computeValue() {
                    return Math.min(observableNumberValue.doubleValue(), observableNumberValue2.doubleValue());
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
                }
            };
        }
        if (observableNumberValue instanceof ObservableFloatValue || observableNumberValue2 instanceof ObservableFloatValue) {
            return new FloatBinding(){
                {
                    super.bind(observableArray);
                }

                @Override
                public void dispose() {
                    super.unbind(observableArray);
                }

                @Override
                protected float computeValue() {
                    return Math.min(observableNumberValue.floatValue(), observableNumberValue2.floatValue());
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
                }
            };
        }
        if (observableNumberValue instanceof ObservableLongValue || observableNumberValue2 instanceof ObservableLongValue) {
            return new LongBinding(){
                {
                    super.bind(observableArray);
                }

                @Override
                public void dispose() {
                    super.unbind(observableArray);
                }

                @Override
                protected long computeValue() {
                    return Math.min(observableNumberValue.longValue(), observableNumberValue2.longValue());
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
                }
            };
        }
        return new IntegerBinding(){
            {
                super.bind(observableArray);
            }

            @Override
            public void dispose() {
                super.unbind(observableArray);
            }

            @Override
            protected int computeValue() {
                return Math.min(observableNumberValue.intValue(), observableNumberValue2.intValue());
            }

            @Override
            public ObservableList<?> getDependencies() {
                return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
            }
        };
    }

    public static NumberBinding min(ObservableNumberValue observableNumberValue, ObservableNumberValue observableNumberValue2) {
        return Bindings.min(observableNumberValue, observableNumberValue2, observableNumberValue, observableNumberValue2);
    }

    public static DoubleBinding min(ObservableNumberValue observableNumberValue, double d) {
        return (DoubleBinding)Bindings.min(observableNumberValue, DoubleConstant.valueOf(d), observableNumberValue);
    }

    public static DoubleBinding min(double d, ObservableNumberValue observableNumberValue) {
        return (DoubleBinding)Bindings.min(DoubleConstant.valueOf(d), observableNumberValue, observableNumberValue);
    }

    public static NumberBinding min(ObservableNumberValue observableNumberValue, float f) {
        return Bindings.min(observableNumberValue, FloatConstant.valueOf(f), observableNumberValue);
    }

    public static NumberBinding min(float f, ObservableNumberValue observableNumberValue) {
        return Bindings.min(FloatConstant.valueOf(f), observableNumberValue, observableNumberValue);
    }

    public static NumberBinding min(ObservableNumberValue observableNumberValue, long l) {
        return Bindings.min(observableNumberValue, LongConstant.valueOf(l), observableNumberValue);
    }

    public static NumberBinding min(long l, ObservableNumberValue observableNumberValue) {
        return Bindings.min(LongConstant.valueOf(l), observableNumberValue, observableNumberValue);
    }

    public static NumberBinding min(ObservableNumberValue observableNumberValue, int n) {
        return Bindings.min(observableNumberValue, IntegerConstant.valueOf(n), observableNumberValue);
    }

    public static NumberBinding min(int n, ObservableNumberValue observableNumberValue) {
        return Bindings.min(IntegerConstant.valueOf(n), observableNumberValue, observableNumberValue);
    }

    private static NumberBinding max(final ObservableNumberValue observableNumberValue, final ObservableNumberValue observableNumberValue2, final Observable ... observableArray) {
        if (observableNumberValue == null || observableNumberValue2 == null) {
            throw new NullPointerException("Operands cannot be null.");
        }
        assert (observableArray != null && observableArray.length > 0);
        if (observableNumberValue instanceof ObservableDoubleValue || observableNumberValue2 instanceof ObservableDoubleValue) {
            return new DoubleBinding(){
                {
                    super.bind(observableArray);
                }

                @Override
                public void dispose() {
                    super.unbind(observableArray);
                }

                @Override
                protected double computeValue() {
                    return Math.max(observableNumberValue.doubleValue(), observableNumberValue2.doubleValue());
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
                }
            };
        }
        if (observableNumberValue instanceof ObservableFloatValue || observableNumberValue2 instanceof ObservableFloatValue) {
            return new FloatBinding(){
                {
                    super.bind(observableArray);
                }

                @Override
                public void dispose() {
                    super.unbind(observableArray);
                }

                @Override
                protected float computeValue() {
                    return Math.max(observableNumberValue.floatValue(), observableNumberValue2.floatValue());
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
                }
            };
        }
        if (observableNumberValue instanceof ObservableLongValue || observableNumberValue2 instanceof ObservableLongValue) {
            return new LongBinding(){
                {
                    super.bind(observableArray);
                }

                @Override
                public void dispose() {
                    super.unbind(observableArray);
                }

                @Override
                protected long computeValue() {
                    return Math.max(observableNumberValue.longValue(), observableNumberValue2.longValue());
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
                }
            };
        }
        return new IntegerBinding(){
            {
                super.bind(observableArray);
            }

            @Override
            public void dispose() {
                super.unbind(observableArray);
            }

            @Override
            protected int computeValue() {
                return Math.max(observableNumberValue.intValue(), observableNumberValue2.intValue());
            }

            @Override
            public ObservableList<?> getDependencies() {
                return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
            }
        };
    }

    public static NumberBinding max(ObservableNumberValue observableNumberValue, ObservableNumberValue observableNumberValue2) {
        return Bindings.max(observableNumberValue, observableNumberValue2, observableNumberValue, observableNumberValue2);
    }

    public static DoubleBinding max(ObservableNumberValue observableNumberValue, double d) {
        return (DoubleBinding)Bindings.max(observableNumberValue, DoubleConstant.valueOf(d), observableNumberValue);
    }

    public static DoubleBinding max(double d, ObservableNumberValue observableNumberValue) {
        return (DoubleBinding)Bindings.max(DoubleConstant.valueOf(d), observableNumberValue, observableNumberValue);
    }

    public static NumberBinding max(ObservableNumberValue observableNumberValue, float f) {
        return Bindings.max(observableNumberValue, FloatConstant.valueOf(f), observableNumberValue);
    }

    public static NumberBinding max(float f, ObservableNumberValue observableNumberValue) {
        return Bindings.max(FloatConstant.valueOf(f), observableNumberValue, observableNumberValue);
    }

    public static NumberBinding max(ObservableNumberValue observableNumberValue, long l) {
        return Bindings.max(observableNumberValue, LongConstant.valueOf(l), observableNumberValue);
    }

    public static NumberBinding max(long l, ObservableNumberValue observableNumberValue) {
        return Bindings.max(LongConstant.valueOf(l), observableNumberValue, observableNumberValue);
    }

    public static NumberBinding max(ObservableNumberValue observableNumberValue, int n) {
        return Bindings.max(observableNumberValue, IntegerConstant.valueOf(n), observableNumberValue);
    }

    public static NumberBinding max(int n, ObservableNumberValue observableNumberValue) {
        return Bindings.max(IntegerConstant.valueOf(n), observableNumberValue, observableNumberValue);
    }

    public static BooleanBinding and(ObservableBooleanValue observableBooleanValue, ObservableBooleanValue observableBooleanValue2) {
        if (observableBooleanValue == null || observableBooleanValue2 == null) {
            throw new NullPointerException("Operands cannot be null.");
        }
        return new BooleanAndBinding(observableBooleanValue, observableBooleanValue2);
    }

    public static BooleanBinding or(ObservableBooleanValue observableBooleanValue, ObservableBooleanValue observableBooleanValue2) {
        if (observableBooleanValue == null || observableBooleanValue2 == null) {
            throw new NullPointerException("Operands cannot be null.");
        }
        return new BooleanOrBinding(observableBooleanValue, observableBooleanValue2);
    }

    public static BooleanBinding not(final ObservableBooleanValue observableBooleanValue) {
        if (observableBooleanValue == null) {
            throw new NullPointerException("Operand cannot be null.");
        }
        return new BooleanBinding(){
            {
                super.bind(observableBooleanValue);
            }

            @Override
            public void dispose() {
                super.unbind(observableBooleanValue);
            }

            @Override
            protected boolean computeValue() {
                return !observableBooleanValue.get();
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(observableBooleanValue);
            }
        };
    }

    public static BooleanBinding equal(final ObservableBooleanValue observableBooleanValue, final ObservableBooleanValue observableBooleanValue2) {
        if (observableBooleanValue == null || observableBooleanValue2 == null) {
            throw new NullPointerException("Operands cannot be null.");
        }
        return new BooleanBinding(){
            {
                super.bind(observableBooleanValue, observableBooleanValue2);
            }

            @Override
            public void dispose() {
                super.unbind(observableBooleanValue, observableBooleanValue2);
            }

            @Override
            protected boolean computeValue() {
                return observableBooleanValue.get() == observableBooleanValue2.get();
            }

            @Override
            public ObservableList<?> getDependencies() {
                return new ImmutableObservableList<ObservableBooleanValue>(observableBooleanValue, observableBooleanValue2);
            }
        };
    }

    public static BooleanBinding notEqual(final ObservableBooleanValue observableBooleanValue, final ObservableBooleanValue observableBooleanValue2) {
        if (observableBooleanValue == null || observableBooleanValue2 == null) {
            throw new NullPointerException("Operands cannot be null.");
        }
        return new BooleanBinding(){
            {
                super.bind(observableBooleanValue, observableBooleanValue2);
            }

            @Override
            public void dispose() {
                super.unbind(observableBooleanValue, observableBooleanValue2);
            }

            @Override
            protected boolean computeValue() {
                return observableBooleanValue.get() != observableBooleanValue2.get();
            }

            @Override
            public ObservableList<?> getDependencies() {
                return new ImmutableObservableList<ObservableBooleanValue>(observableBooleanValue, observableBooleanValue2);
            }
        };
    }

    public static StringExpression convert(ObservableValue<?> observableValue) {
        return StringFormatter.convert(observableValue);
    }

    public static StringExpression concat(Object ... objectArray) {
        return StringFormatter.concat(objectArray);
    }

    public static StringExpression format(String string, Object ... objectArray) {
        return StringFormatter.format(string, objectArray);
    }

    public static StringExpression format(Locale locale, String string, Object ... objectArray) {
        return StringFormatter.format(locale, string, objectArray);
    }

    private static String getStringSafe(String string) {
        return string == null ? "" : string;
    }

    private static BooleanBinding equal(final ObservableStringValue observableStringValue, final ObservableStringValue observableStringValue2, final Observable ... observableArray) {
        if (observableStringValue == null || observableStringValue2 == null) {
            throw new NullPointerException("Operands cannot be null.");
        }
        assert (observableArray != null && observableArray.length > 0);
        return new BooleanBinding(){
            {
                super.bind(observableArray);
            }

            @Override
            public void dispose() {
                super.unbind(observableArray);
            }

            @Override
            protected boolean computeValue() {
                String string = Bindings.getStringSafe((String)observableStringValue.get());
                String string2 = Bindings.getStringSafe((String)observableStringValue2.get());
                return string.equals(string2);
            }

            @Override
            public ObservableList<?> getDependencies() {
                return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
            }
        };
    }

    public static BooleanBinding equal(ObservableStringValue observableStringValue, ObservableStringValue observableStringValue2) {
        return Bindings.equal(observableStringValue, observableStringValue2, new Observable[]{observableStringValue, observableStringValue2});
    }

    public static BooleanBinding equal(ObservableStringValue observableStringValue, String string) {
        return Bindings.equal(observableStringValue, StringConstant.valueOf(string), new Observable[]{observableStringValue});
    }

    public static BooleanBinding equal(String string, ObservableStringValue observableStringValue) {
        return Bindings.equal(StringConstant.valueOf(string), observableStringValue, new Observable[]{observableStringValue});
    }

    private static BooleanBinding notEqual(final ObservableStringValue observableStringValue, final ObservableStringValue observableStringValue2, final Observable ... observableArray) {
        if (observableStringValue == null || observableStringValue2 == null) {
            throw new NullPointerException("Operands cannot be null.");
        }
        assert (observableArray != null && observableArray.length > 0);
        return new BooleanBinding(){
            {
                super.bind(observableArray);
            }

            @Override
            public void dispose() {
                super.unbind(observableArray);
            }

            @Override
            protected boolean computeValue() {
                String string;
                String string2 = Bindings.getStringSafe((String)observableStringValue.get());
                return !string2.equals(string = Bindings.getStringSafe((String)observableStringValue2.get()));
            }

            @Override
            public ObservableList<?> getDependencies() {
                return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
            }
        };
    }

    public static BooleanBinding notEqual(ObservableStringValue observableStringValue, ObservableStringValue observableStringValue2) {
        return Bindings.notEqual(observableStringValue, observableStringValue2, new Observable[]{observableStringValue, observableStringValue2});
    }

    public static BooleanBinding notEqual(ObservableStringValue observableStringValue, String string) {
        return Bindings.notEqual(observableStringValue, StringConstant.valueOf(string), new Observable[]{observableStringValue});
    }

    public static BooleanBinding notEqual(String string, ObservableStringValue observableStringValue) {
        return Bindings.notEqual(StringConstant.valueOf(string), observableStringValue, new Observable[]{observableStringValue});
    }

    private static BooleanBinding equalIgnoreCase(final ObservableStringValue observableStringValue, final ObservableStringValue observableStringValue2, final Observable ... observableArray) {
        if (observableStringValue == null || observableStringValue2 == null) {
            throw new NullPointerException("Operands cannot be null.");
        }
        assert (observableArray != null && observableArray.length > 0);
        return new BooleanBinding(){
            {
                super.bind(observableArray);
            }

            @Override
            public void dispose() {
                super.unbind(observableArray);
            }

            @Override
            protected boolean computeValue() {
                String string = Bindings.getStringSafe((String)observableStringValue.get());
                String string2 = Bindings.getStringSafe((String)observableStringValue2.get());
                return string.equalsIgnoreCase(string2);
            }

            @Override
            public ObservableList<?> getDependencies() {
                return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
            }
        };
    }

    public static BooleanBinding equalIgnoreCase(ObservableStringValue observableStringValue, ObservableStringValue observableStringValue2) {
        return Bindings.equalIgnoreCase(observableStringValue, observableStringValue2, observableStringValue, observableStringValue2);
    }

    public static BooleanBinding equalIgnoreCase(ObservableStringValue observableStringValue, String string) {
        return Bindings.equalIgnoreCase(observableStringValue, StringConstant.valueOf(string), observableStringValue);
    }

    public static BooleanBinding equalIgnoreCase(String string, ObservableStringValue observableStringValue) {
        return Bindings.equalIgnoreCase(StringConstant.valueOf(string), observableStringValue, observableStringValue);
    }

    private static BooleanBinding notEqualIgnoreCase(final ObservableStringValue observableStringValue, final ObservableStringValue observableStringValue2, final Observable ... observableArray) {
        if (observableStringValue == null || observableStringValue2 == null) {
            throw new NullPointerException("Operands cannot be null.");
        }
        assert (observableArray != null && observableArray.length > 0);
        return new BooleanBinding(){
            {
                super.bind(observableArray);
            }

            @Override
            public void dispose() {
                super.unbind(observableArray);
            }

            @Override
            protected boolean computeValue() {
                String string;
                String string2 = Bindings.getStringSafe((String)observableStringValue.get());
                return !string2.equalsIgnoreCase(string = Bindings.getStringSafe((String)observableStringValue2.get()));
            }

            @Override
            public ObservableList<?> getDependencies() {
                return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
            }
        };
    }

    public static BooleanBinding notEqualIgnoreCase(ObservableStringValue observableStringValue, ObservableStringValue observableStringValue2) {
        return Bindings.notEqualIgnoreCase(observableStringValue, observableStringValue2, observableStringValue, observableStringValue2);
    }

    public static BooleanBinding notEqualIgnoreCase(ObservableStringValue observableStringValue, String string) {
        return Bindings.notEqualIgnoreCase(observableStringValue, StringConstant.valueOf(string), observableStringValue);
    }

    public static BooleanBinding notEqualIgnoreCase(String string, ObservableStringValue observableStringValue) {
        return Bindings.notEqualIgnoreCase(StringConstant.valueOf(string), observableStringValue, observableStringValue);
    }

    private static BooleanBinding greaterThan(final ObservableStringValue observableStringValue, final ObservableStringValue observableStringValue2, final Observable ... observableArray) {
        if (observableStringValue == null || observableStringValue2 == null) {
            throw new NullPointerException("Operands cannot be null.");
        }
        assert (observableArray != null && observableArray.length > 0);
        return new BooleanBinding(){
            {
                super.bind(observableArray);
            }

            @Override
            public void dispose() {
                super.unbind(observableArray);
            }

            @Override
            protected boolean computeValue() {
                String string;
                String string2 = Bindings.getStringSafe((String)observableStringValue.get());
                return string2.compareTo(string = Bindings.getStringSafe((String)observableStringValue2.get())) > 0;
            }

            @Override
            public ObservableList<?> getDependencies() {
                return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
            }
        };
    }

    public static BooleanBinding greaterThan(ObservableStringValue observableStringValue, ObservableStringValue observableStringValue2) {
        return Bindings.greaterThan(observableStringValue, observableStringValue2, observableStringValue, observableStringValue2);
    }

    public static BooleanBinding greaterThan(ObservableStringValue observableStringValue, String string) {
        return Bindings.greaterThan(observableStringValue, StringConstant.valueOf(string), observableStringValue);
    }

    public static BooleanBinding greaterThan(String string, ObservableStringValue observableStringValue) {
        return Bindings.greaterThan(StringConstant.valueOf(string), observableStringValue, observableStringValue);
    }

    private static BooleanBinding lessThan(ObservableStringValue observableStringValue, ObservableStringValue observableStringValue2, Observable ... observableArray) {
        return Bindings.greaterThan(observableStringValue2, observableStringValue, observableArray);
    }

    public static BooleanBinding lessThan(ObservableStringValue observableStringValue, ObservableStringValue observableStringValue2) {
        return Bindings.lessThan(observableStringValue, observableStringValue2, observableStringValue, observableStringValue2);
    }

    public static BooleanBinding lessThan(ObservableStringValue observableStringValue, String string) {
        return Bindings.lessThan(observableStringValue, StringConstant.valueOf(string), observableStringValue);
    }

    public static BooleanBinding lessThan(String string, ObservableStringValue observableStringValue) {
        return Bindings.lessThan(StringConstant.valueOf(string), observableStringValue, observableStringValue);
    }

    private static BooleanBinding greaterThanOrEqual(final ObservableStringValue observableStringValue, final ObservableStringValue observableStringValue2, final Observable ... observableArray) {
        if (observableStringValue == null || observableStringValue2 == null) {
            throw new NullPointerException("Operands cannot be null.");
        }
        assert (observableArray != null && observableArray.length > 0);
        return new BooleanBinding(){
            {
                super.bind(observableArray);
            }

            @Override
            public void dispose() {
                super.unbind(observableArray);
            }

            @Override
            protected boolean computeValue() {
                String string;
                String string2 = Bindings.getStringSafe((String)observableStringValue.get());
                return string2.compareTo(string = Bindings.getStringSafe((String)observableStringValue2.get())) >= 0;
            }

            @Override
            public ObservableList<?> getDependencies() {
                return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
            }
        };
    }

    public static BooleanBinding greaterThanOrEqual(ObservableStringValue observableStringValue, ObservableStringValue observableStringValue2) {
        return Bindings.greaterThanOrEqual(observableStringValue, observableStringValue2, observableStringValue, observableStringValue2);
    }

    public static BooleanBinding greaterThanOrEqual(ObservableStringValue observableStringValue, String string) {
        return Bindings.greaterThanOrEqual(observableStringValue, StringConstant.valueOf(string), observableStringValue);
    }

    public static BooleanBinding greaterThanOrEqual(String string, ObservableStringValue observableStringValue) {
        return Bindings.greaterThanOrEqual(StringConstant.valueOf(string), observableStringValue, observableStringValue);
    }

    private static BooleanBinding lessThanOrEqual(ObservableStringValue observableStringValue, ObservableStringValue observableStringValue2, Observable ... observableArray) {
        return Bindings.greaterThanOrEqual(observableStringValue2, observableStringValue, observableArray);
    }

    public static BooleanBinding lessThanOrEqual(ObservableStringValue observableStringValue, ObservableStringValue observableStringValue2) {
        return Bindings.lessThanOrEqual(observableStringValue, observableStringValue2, observableStringValue, observableStringValue2);
    }

    public static BooleanBinding lessThanOrEqual(ObservableStringValue observableStringValue, String string) {
        return Bindings.lessThanOrEqual(observableStringValue, StringConstant.valueOf(string), observableStringValue);
    }

    public static BooleanBinding lessThanOrEqual(String string, ObservableStringValue observableStringValue) {
        return Bindings.lessThanOrEqual(StringConstant.valueOf(string), observableStringValue, observableStringValue);
    }

    public static IntegerBinding length(final ObservableStringValue observableStringValue) {
        if (observableStringValue == null) {
            throw new NullPointerException("Operand cannot be null");
        }
        return new IntegerBinding(){
            {
                super.bind(observableStringValue);
            }

            @Override
            public void dispose() {
                super.unbind(observableStringValue);
            }

            @Override
            protected int computeValue() {
                return Bindings.getStringSafe((String)observableStringValue.get()).length();
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(observableStringValue);
            }
        };
    }

    public static BooleanBinding isEmpty(final ObservableStringValue observableStringValue) {
        if (observableStringValue == null) {
            throw new NullPointerException("Operand cannot be null");
        }
        return new BooleanBinding(){
            {
                super.bind(observableStringValue);
            }

            @Override
            public void dispose() {
                super.unbind(observableStringValue);
            }

            @Override
            protected boolean computeValue() {
                return Bindings.getStringSafe((String)observableStringValue.get()).isEmpty();
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(observableStringValue);
            }
        };
    }

    public static BooleanBinding isNotEmpty(final ObservableStringValue observableStringValue) {
        if (observableStringValue == null) {
            throw new NullPointerException("Operand cannot be null");
        }
        return new BooleanBinding(){
            {
                super.bind(observableStringValue);
            }

            @Override
            public void dispose() {
                super.unbind(observableStringValue);
            }

            @Override
            protected boolean computeValue() {
                return !Bindings.getStringSafe((String)observableStringValue.get()).isEmpty();
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(observableStringValue);
            }
        };
    }

    private static BooleanBinding equal(final ObservableObjectValue<?> observableObjectValue, final ObservableObjectValue<?> observableObjectValue2, final Observable ... observableArray) {
        if (observableObjectValue == null || observableObjectValue2 == null) {
            throw new NullPointerException("Operands cannot be null.");
        }
        assert (observableArray != null && observableArray.length > 0);
        return new BooleanBinding(){
            {
                super.bind(observableArray);
            }

            @Override
            public void dispose() {
                super.unbind(observableArray);
            }

            @Override
            protected boolean computeValue() {
                Object t = observableObjectValue.get();
                Object t2 = observableObjectValue2.get();
                return t == null ? t2 == null : t.equals(t2);
            }

            @Override
            public ObservableList<?> getDependencies() {
                return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
            }
        };
    }

    public static BooleanBinding equal(ObservableObjectValue<?> observableObjectValue, ObservableObjectValue<?> observableObjectValue2) {
        return Bindings.equal(observableObjectValue, observableObjectValue2, observableObjectValue, observableObjectValue2);
    }

    public static BooleanBinding equal(ObservableObjectValue<?> observableObjectValue, Object object) {
        return Bindings.equal(observableObjectValue, ObjectConstant.valueOf(object), observableObjectValue);
    }

    public static BooleanBinding equal(Object object, ObservableObjectValue<?> observableObjectValue) {
        return Bindings.equal(ObjectConstant.valueOf(object), observableObjectValue, observableObjectValue);
    }

    private static BooleanBinding notEqual(final ObservableObjectValue<?> observableObjectValue, final ObservableObjectValue<?> observableObjectValue2, final Observable ... observableArray) {
        if (observableObjectValue == null || observableObjectValue2 == null) {
            throw new NullPointerException("Operands cannot be null.");
        }
        assert (observableArray != null && observableArray.length > 0);
        return new BooleanBinding(){
            {
                super.bind(observableArray);
            }

            @Override
            public void dispose() {
                super.unbind(observableArray);
            }

            @Override
            protected boolean computeValue() {
                Object t = observableObjectValue.get();
                Object t2 = observableObjectValue2.get();
                return t == null ? t2 != null : !t.equals(t2);
            }

            @Override
            public ObservableList<?> getDependencies() {
                return observableArray.length == 1 ? FXCollections.singletonObservableList(observableArray[0]) : new ImmutableObservableList<Observable>(observableArray);
            }
        };
    }

    public static BooleanBinding notEqual(ObservableObjectValue<?> observableObjectValue, ObservableObjectValue<?> observableObjectValue2) {
        return Bindings.notEqual(observableObjectValue, observableObjectValue2, observableObjectValue, observableObjectValue2);
    }

    public static BooleanBinding notEqual(ObservableObjectValue<?> observableObjectValue, Object object) {
        return Bindings.notEqual(observableObjectValue, ObjectConstant.valueOf(object), observableObjectValue);
    }

    public static BooleanBinding notEqual(Object object, ObservableObjectValue<?> observableObjectValue) {
        return Bindings.notEqual(ObjectConstant.valueOf(object), observableObjectValue, observableObjectValue);
    }

    public static BooleanBinding isNull(final ObservableObjectValue<?> observableObjectValue) {
        if (observableObjectValue == null) {
            throw new NullPointerException("Operand cannot be null.");
        }
        return new BooleanBinding(){
            {
                super.bind(observableObjectValue);
            }

            @Override
            public void dispose() {
                super.unbind(observableObjectValue);
            }

            @Override
            protected boolean computeValue() {
                return observableObjectValue.get() == null;
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(observableObjectValue);
            }
        };
    }

    public static BooleanBinding isNotNull(final ObservableObjectValue<?> observableObjectValue) {
        if (observableObjectValue == null) {
            throw new NullPointerException("Operand cannot be null.");
        }
        return new BooleanBinding(){
            {
                super.bind(observableObjectValue);
            }

            @Override
            public void dispose() {
                super.unbind(observableObjectValue);
            }

            @Override
            protected boolean computeValue() {
                return observableObjectValue.get() != null;
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(observableObjectValue);
            }
        };
    }

    public static <E> IntegerBinding size(final ObservableList<E> observableList) {
        if (observableList == null) {
            throw new NullPointerException("List cannot be null.");
        }
        return new IntegerBinding(){
            {
                super.bind(observableList);
            }

            @Override
            public void dispose() {
                super.unbind(observableList);
            }

            @Override
            protected int computeValue() {
                return observableList.size();
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(observableList);
            }
        };
    }

    public static <E> BooleanBinding isEmpty(final ObservableList<E> observableList) {
        if (observableList == null) {
            throw new NullPointerException("List cannot be null.");
        }
        return new BooleanBinding(){
            {
                super.bind(observableList);
            }

            @Override
            public void dispose() {
                super.unbind(observableList);
            }

            @Override
            protected boolean computeValue() {
                return observableList.isEmpty();
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(observableList);
            }
        };
    }

    public static <E> BooleanBinding isNotEmpty(final ObservableList<E> observableList) {
        if (observableList == null) {
            throw new NullPointerException("List cannot be null.");
        }
        return new BooleanBinding(){
            {
                super.bind(observableList);
            }

            @Override
            public void dispose() {
                super.unbind(observableList);
            }

            @Override
            protected boolean computeValue() {
                return !observableList.isEmpty();
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(observableList);
            }
        };
    }

    public static <E> ObjectBinding<E> valueAt(final ObservableList<E> observableList, final int n) {
        if (observableList == null) {
            throw new NullPointerException("List cannot be null.");
        }
        if (n < 0) {
            throw new IllegalArgumentException("Index cannot be negative");
        }
        return new ObjectBinding<E>(){
            {
                super.bind(observableList);
            }

            @Override
            public void dispose() {
                super.unbind(observableList);
            }

            @Override
            protected E computeValue() {
                try {
                    return observableList.get(n);
                }
                catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                    Logging.getLogger().fine("Exception while evaluating binding", indexOutOfBoundsException);
                    return null;
                }
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(observableList);
            }
        };
    }

    public static <E> ObjectBinding<E> valueAt(ObservableList<E> observableList, ObservableIntegerValue observableIntegerValue) {
        return Bindings.valueAt(observableList, (ObservableNumberValue)observableIntegerValue);
    }

    public static <E> ObjectBinding<E> valueAt(final ObservableList<E> observableList, final ObservableNumberValue observableNumberValue) {
        if (observableList == null || observableNumberValue == null) {
            throw new NullPointerException("Operands cannot be null.");
        }
        return new ObjectBinding<E>(){
            {
                super.bind(observableList, observableNumberValue);
            }

            @Override
            public void dispose() {
                super.unbind(observableList, observableNumberValue);
            }

            @Override
            protected E computeValue() {
                try {
                    return observableList.get(observableNumberValue.intValue());
                }
                catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                    Logging.getLogger().fine("Exception while evaluating binding", indexOutOfBoundsException);
                    return null;
                }
            }

            @Override
            public ObservableList<?> getDependencies() {
                return new ImmutableObservableList<Observable>(observableList, observableNumberValue);
            }
        };
    }

    public static BooleanBinding booleanValueAt(final ObservableList<Boolean> observableList, final int n) {
        if (observableList == null) {
            throw new NullPointerException("List cannot be null.");
        }
        if (n < 0) {
            throw new IllegalArgumentException("Index cannot be negative");
        }
        return new BooleanBinding(){
            {
                super.bind(observableList);
            }

            @Override
            public void dispose() {
                super.unbind(observableList);
            }

            @Override
            protected boolean computeValue() {
                try {
                    Boolean bl = (Boolean)observableList.get(n);
                    if (bl != null) {
                        return bl;
                    }
                    Logging.getLogger().fine("List element is null, returning default value instead.", new NullPointerException());
                }
                catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                    Logging.getLogger().fine("Exception while evaluating binding", indexOutOfBoundsException);
                }
                return false;
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(observableList);
            }
        };
    }

    public static BooleanBinding booleanValueAt(ObservableList<Boolean> observableList, ObservableIntegerValue observableIntegerValue) {
        return Bindings.booleanValueAt(observableList, (ObservableNumberValue)observableIntegerValue);
    }

    public static BooleanBinding booleanValueAt(final ObservableList<Boolean> observableList, final ObservableNumberValue observableNumberValue) {
        if (observableList == null || observableNumberValue == null) {
            throw new NullPointerException("Operands cannot be null.");
        }
        return new BooleanBinding(){
            {
                super.bind(observableList, observableNumberValue);
            }

            @Override
            public void dispose() {
                super.unbind(observableList, observableNumberValue);
            }

            @Override
            protected boolean computeValue() {
                try {
                    Boolean bl = (Boolean)observableList.get(observableNumberValue.intValue());
                    if (bl != null) {
                        return bl;
                    }
                    Logging.getLogger().fine("List element is null, returning default value instead.", new NullPointerException());
                }
                catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                    Logging.getLogger().fine("Exception while evaluating binding", indexOutOfBoundsException);
                }
                return false;
            }

            @Override
            public ObservableList<?> getDependencies() {
                return new ImmutableObservableList<Observable>(observableList, observableNumberValue);
            }
        };
    }

    public static DoubleBinding doubleValueAt(final ObservableList<? extends Number> observableList, final int n) {
        if (observableList == null) {
            throw new NullPointerException("List cannot be null.");
        }
        if (n < 0) {
            throw new IllegalArgumentException("Index cannot be negative");
        }
        return new DoubleBinding(){
            {
                super.bind(observableList);
            }

            @Override
            public void dispose() {
                super.unbind(observableList);
            }

            @Override
            protected double computeValue() {
                try {
                    Number number = (Number)observableList.get(n);
                    if (number != null) {
                        return number.doubleValue();
                    }
                    Logging.getLogger().fine("List element is null, returning default value instead.", new NullPointerException());
                }
                catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                    Logging.getLogger().fine("Exception while evaluating binding", indexOutOfBoundsException);
                }
                return 0.0;
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(observableList);
            }
        };
    }

    public static DoubleBinding doubleValueAt(ObservableList<? extends Number> observableList, ObservableIntegerValue observableIntegerValue) {
        return Bindings.doubleValueAt(observableList, (ObservableNumberValue)observableIntegerValue);
    }

    public static DoubleBinding doubleValueAt(final ObservableList<? extends Number> observableList, final ObservableNumberValue observableNumberValue) {
        if (observableList == null || observableNumberValue == null) {
            throw new NullPointerException("Operands cannot be null.");
        }
        return new DoubleBinding(){
            {
                super.bind(observableList, observableNumberValue);
            }

            @Override
            public void dispose() {
                super.unbind(observableList, observableNumberValue);
            }

            @Override
            protected double computeValue() {
                try {
                    Number number = (Number)observableList.get(observableNumberValue.intValue());
                    if (number != null) {
                        return number.doubleValue();
                    }
                    Logging.getLogger().fine("List element is null, returning default value instead.", new NullPointerException());
                }
                catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                    Logging.getLogger().fine("Exception while evaluating binding", indexOutOfBoundsException);
                }
                return 0.0;
            }

            @Override
            public ObservableList<?> getDependencies() {
                return new ImmutableObservableList<Observable>(observableList, observableNumberValue);
            }
        };
    }

    public static FloatBinding floatValueAt(final ObservableList<? extends Number> observableList, final int n) {
        if (observableList == null) {
            throw new NullPointerException("List cannot be null.");
        }
        if (n < 0) {
            throw new IllegalArgumentException("Index cannot be negative");
        }
        return new FloatBinding(){
            {
                super.bind(observableList);
            }

            @Override
            public void dispose() {
                super.unbind(observableList);
            }

            @Override
            protected float computeValue() {
                try {
                    Number number = (Number)observableList.get(n);
                    if (number != null) {
                        return number.floatValue();
                    }
                    Logging.getLogger().fine("List element is null, returning default value instead.", new NullPointerException());
                }
                catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                    Logging.getLogger().fine("Exception while evaluating binding", indexOutOfBoundsException);
                }
                return 0.0f;
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(observableList);
            }
        };
    }

    public static FloatBinding floatValueAt(ObservableList<? extends Number> observableList, ObservableIntegerValue observableIntegerValue) {
        return Bindings.floatValueAt(observableList, (ObservableNumberValue)observableIntegerValue);
    }

    public static FloatBinding floatValueAt(final ObservableList<? extends Number> observableList, final ObservableNumberValue observableNumberValue) {
        if (observableList == null || observableNumberValue == null) {
            throw new NullPointerException("Operands cannot be null.");
        }
        return new FloatBinding(){
            {
                super.bind(observableList, observableNumberValue);
            }

            @Override
            public void dispose() {
                super.unbind(observableList, observableNumberValue);
            }

            @Override
            protected float computeValue() {
                try {
                    Number number = (Number)observableList.get(observableNumberValue.intValue());
                    if (number != null) {
                        return number.floatValue();
                    }
                    Logging.getLogger().fine("List element is null, returning default value instead.", new NullPointerException());
                }
                catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                    Logging.getLogger().fine("Exception while evaluating binding", indexOutOfBoundsException);
                }
                return 0.0f;
            }

            @Override
            public ObservableList<?> getDependencies() {
                return new ImmutableObservableList<Observable>(observableList, observableNumberValue);
            }
        };
    }

    public static IntegerBinding integerValueAt(final ObservableList<? extends Number> observableList, final int n) {
        if (observableList == null) {
            throw new NullPointerException("List cannot be null.");
        }
        if (n < 0) {
            throw new IllegalArgumentException("Index cannot be negative");
        }
        return new IntegerBinding(){
            {
                super.bind(observableList);
            }

            @Override
            public void dispose() {
                super.unbind(observableList);
            }

            @Override
            protected int computeValue() {
                try {
                    Number number = (Number)observableList.get(n);
                    if (number != null) {
                        return number.intValue();
                    }
                    Logging.getLogger().fine("List element is null, returning default value instead.", new NullPointerException());
                }
                catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                    Logging.getLogger().fine("Exception while evaluating binding", indexOutOfBoundsException);
                }
                return 0;
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(observableList);
            }
        };
    }

    public static IntegerBinding integerValueAt(ObservableList<? extends Number> observableList, ObservableIntegerValue observableIntegerValue) {
        return Bindings.integerValueAt(observableList, (ObservableNumberValue)observableIntegerValue);
    }

    public static IntegerBinding integerValueAt(final ObservableList<? extends Number> observableList, final ObservableNumberValue observableNumberValue) {
        if (observableList == null || observableNumberValue == null) {
            throw new NullPointerException("Operands cannot be null.");
        }
        return new IntegerBinding(){
            {
                super.bind(observableList, observableNumberValue);
            }

            @Override
            public void dispose() {
                super.unbind(observableList, observableNumberValue);
            }

            @Override
            protected int computeValue() {
                try {
                    Number number = (Number)observableList.get(observableNumberValue.intValue());
                    if (number != null) {
                        return number.intValue();
                    }
                    Logging.getLogger().fine("List element is null, returning default value instead.", new NullPointerException());
                }
                catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                    Logging.getLogger().fine("Exception while evaluating binding", indexOutOfBoundsException);
                }
                return 0;
            }

            @Override
            public ObservableList<?> getDependencies() {
                return new ImmutableObservableList<Observable>(observableList, observableNumberValue);
            }
        };
    }

    public static LongBinding longValueAt(final ObservableList<? extends Number> observableList, final int n) {
        if (observableList == null) {
            throw new NullPointerException("List cannot be null.");
        }
        if (n < 0) {
            throw new IllegalArgumentException("Index cannot be negative");
        }
        return new LongBinding(){
            {
                super.bind(observableList);
            }

            @Override
            public void dispose() {
                super.unbind(observableList);
            }

            @Override
            protected long computeValue() {
                try {
                    Number number = (Number)observableList.get(n);
                    if (number != null) {
                        return number.longValue();
                    }
                    Logging.getLogger().fine("List element is null, returning default value instead.", new NullPointerException());
                }
                catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                    Logging.getLogger().fine("Exception while evaluating binding", indexOutOfBoundsException);
                }
                return 0L;
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(observableList);
            }
        };
    }

    public static LongBinding longValueAt(ObservableList<? extends Number> observableList, ObservableIntegerValue observableIntegerValue) {
        return Bindings.longValueAt(observableList, (ObservableNumberValue)observableIntegerValue);
    }

    public static LongBinding longValueAt(final ObservableList<? extends Number> observableList, final ObservableNumberValue observableNumberValue) {
        if (observableList == null || observableNumberValue == null) {
            throw new NullPointerException("Operands cannot be null.");
        }
        return new LongBinding(){
            {
                super.bind(observableList, observableNumberValue);
            }

            @Override
            public void dispose() {
                super.unbind(observableList, observableNumberValue);
            }

            @Override
            protected long computeValue() {
                try {
                    Number number = (Number)observableList.get(observableNumberValue.intValue());
                    if (number != null) {
                        return number.longValue();
                    }
                    Logging.getLogger().fine("List element is null, returning default value instead.", new NullPointerException());
                }
                catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                    Logging.getLogger().fine("Exception while evaluating binding", indexOutOfBoundsException);
                }
                return 0L;
            }

            @Override
            public ObservableList<?> getDependencies() {
                return new ImmutableObservableList<Observable>(observableList, observableNumberValue);
            }
        };
    }

    public static StringBinding stringValueAt(final ObservableList<String> observableList, final int n) {
        if (observableList == null) {
            throw new NullPointerException("List cannot be null.");
        }
        if (n < 0) {
            throw new IllegalArgumentException("Index cannot be negative");
        }
        return new StringBinding(){
            {
                super.bind(observableList);
            }

            @Override
            public void dispose() {
                super.unbind(observableList);
            }

            @Override
            protected String computeValue() {
                try {
                    return (String)observableList.get(n);
                }
                catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                    Logging.getLogger().fine("Exception while evaluating binding", indexOutOfBoundsException);
                    return null;
                }
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(observableList);
            }
        };
    }

    public static StringBinding stringValueAt(ObservableList<String> observableList, ObservableIntegerValue observableIntegerValue) {
        return Bindings.stringValueAt(observableList, (ObservableNumberValue)observableIntegerValue);
    }

    public static StringBinding stringValueAt(final ObservableList<String> observableList, final ObservableNumberValue observableNumberValue) {
        if (observableList == null || observableNumberValue == null) {
            throw new NullPointerException("Operands cannot be null.");
        }
        return new StringBinding(){
            {
                super.bind(observableList, observableNumberValue);
            }

            @Override
            public void dispose() {
                super.unbind(observableList, observableNumberValue);
            }

            @Override
            protected String computeValue() {
                try {
                    return (String)observableList.get(observableNumberValue.intValue());
                }
                catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                    Logging.getLogger().fine("Exception while evaluating binding", indexOutOfBoundsException);
                    return null;
                }
            }

            @Override
            public ObservableList<?> getDependencies() {
                return new ImmutableObservableList<Observable>(observableList, observableNumberValue);
            }
        };
    }

    public static <E> IntegerBinding size(final ObservableSet<E> observableSet) {
        if (observableSet == null) {
            throw new NullPointerException("Set cannot be null.");
        }
        return new IntegerBinding(){
            {
                super.bind(observableSet);
            }

            @Override
            public void dispose() {
                super.unbind(observableSet);
            }

            @Override
            protected int computeValue() {
                return observableSet.size();
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(observableSet);
            }
        };
    }

    public static <E> BooleanBinding isEmpty(final ObservableSet<E> observableSet) {
        if (observableSet == null) {
            throw new NullPointerException("Set cannot be null.");
        }
        return new BooleanBinding(){
            {
                super.bind(observableSet);
            }

            @Override
            public void dispose() {
                super.unbind(observableSet);
            }

            @Override
            protected boolean computeValue() {
                return observableSet.isEmpty();
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(observableSet);
            }
        };
    }

    public static <E> BooleanBinding isNotEmpty(final ObservableSet<E> observableSet) {
        if (observableSet == null) {
            throw new NullPointerException("List cannot be null.");
        }
        return new BooleanBinding(){
            {
                super.bind(observableSet);
            }

            @Override
            public void dispose() {
                super.unbind(observableSet);
            }

            @Override
            protected boolean computeValue() {
                return !observableSet.isEmpty();
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(observableSet);
            }
        };
    }

    public static IntegerBinding size(final ObservableArray observableArray) {
        if (observableArray == null) {
            throw new NullPointerException("Array cannot be null.");
        }
        return new IntegerBinding(){
            {
                super.bind(observableArray);
            }

            @Override
            public void dispose() {
                super.unbind(observableArray);
            }

            @Override
            protected int computeValue() {
                return observableArray.size();
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(observableArray);
            }
        };
    }

    public static FloatBinding floatValueAt(final ObservableFloatArray observableFloatArray, final int n) {
        if (observableFloatArray == null) {
            throw new NullPointerException("Array cannot be null.");
        }
        if (n < 0) {
            throw new IllegalArgumentException("Index cannot be negative");
        }
        return new FloatBinding(){
            {
                super.bind(observableFloatArray);
            }

            @Override
            public void dispose() {
                super.unbind(observableFloatArray);
            }

            @Override
            protected float computeValue() {
                try {
                    return observableFloatArray.get(n);
                }
                catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                    Logging.getLogger().fine("Exception while evaluating binding", indexOutOfBoundsException);
                    return 0.0f;
                }
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(observableFloatArray);
            }
        };
    }

    public static FloatBinding floatValueAt(ObservableFloatArray observableFloatArray, ObservableIntegerValue observableIntegerValue) {
        return Bindings.floatValueAt(observableFloatArray, (ObservableNumberValue)observableIntegerValue);
    }

    public static FloatBinding floatValueAt(final ObservableFloatArray observableFloatArray, final ObservableNumberValue observableNumberValue) {
        if (observableFloatArray == null || observableNumberValue == null) {
            throw new NullPointerException("Operands cannot be null.");
        }
        return new FloatBinding(){
            {
                super.bind(observableFloatArray, observableNumberValue);
            }

            @Override
            public void dispose() {
                super.unbind(observableFloatArray, observableNumberValue);
            }

            @Override
            protected float computeValue() {
                try {
                    return observableFloatArray.get(observableNumberValue.intValue());
                }
                catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                    Logging.getLogger().fine("Exception while evaluating binding", indexOutOfBoundsException);
                    return 0.0f;
                }
            }

            @Override
            public ObservableList<?> getDependencies() {
                return new ImmutableObservableList<Observable>(observableFloatArray, observableNumberValue);
            }
        };
    }

    public static IntegerBinding integerValueAt(final ObservableIntegerArray observableIntegerArray, final int n) {
        if (observableIntegerArray == null) {
            throw new NullPointerException("Array cannot be null.");
        }
        if (n < 0) {
            throw new IllegalArgumentException("Index cannot be negative");
        }
        return new IntegerBinding(){
            {
                super.bind(observableIntegerArray);
            }

            @Override
            public void dispose() {
                super.unbind(observableIntegerArray);
            }

            @Override
            protected int computeValue() {
                try {
                    return observableIntegerArray.get(n);
                }
                catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                    Logging.getLogger().fine("Exception while evaluating binding", indexOutOfBoundsException);
                    return 0;
                }
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(observableIntegerArray);
            }
        };
    }

    public static IntegerBinding integerValueAt(ObservableIntegerArray observableIntegerArray, ObservableIntegerValue observableIntegerValue) {
        return Bindings.integerValueAt(observableIntegerArray, (ObservableNumberValue)observableIntegerValue);
    }

    public static IntegerBinding integerValueAt(final ObservableIntegerArray observableIntegerArray, final ObservableNumberValue observableNumberValue) {
        if (observableIntegerArray == null || observableNumberValue == null) {
            throw new NullPointerException("Operands cannot be null.");
        }
        return new IntegerBinding(){
            {
                super.bind(observableIntegerArray, observableNumberValue);
            }

            @Override
            public void dispose() {
                super.unbind(observableIntegerArray, observableNumberValue);
            }

            @Override
            protected int computeValue() {
                try {
                    return observableIntegerArray.get(observableNumberValue.intValue());
                }
                catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                    Logging.getLogger().fine("Exception while evaluating binding", indexOutOfBoundsException);
                    return 0;
                }
            }

            @Override
            public ObservableList<?> getDependencies() {
                return new ImmutableObservableList<Observable>(observableIntegerArray, observableNumberValue);
            }
        };
    }

    public static <K, V> IntegerBinding size(final ObservableMap<K, V> observableMap) {
        if (observableMap == null) {
            throw new NullPointerException("Map cannot be null.");
        }
        return new IntegerBinding(){
            {
                super.bind(observableMap);
            }

            @Override
            public void dispose() {
                super.unbind(observableMap);
            }

            @Override
            protected int computeValue() {
                return observableMap.size();
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(observableMap);
            }
        };
    }

    public static <K, V> BooleanBinding isEmpty(final ObservableMap<K, V> observableMap) {
        if (observableMap == null) {
            throw new NullPointerException("Map cannot be null.");
        }
        return new BooleanBinding(){
            {
                super.bind(observableMap);
            }

            @Override
            public void dispose() {
                super.unbind(observableMap);
            }

            @Override
            protected boolean computeValue() {
                return observableMap.isEmpty();
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(observableMap);
            }
        };
    }

    public static <K, V> BooleanBinding isNotEmpty(final ObservableMap<K, V> observableMap) {
        if (observableMap == null) {
            throw new NullPointerException("List cannot be null.");
        }
        return new BooleanBinding(){
            {
                super.bind(observableMap);
            }

            @Override
            public void dispose() {
                super.unbind(observableMap);
            }

            @Override
            protected boolean computeValue() {
                return !observableMap.isEmpty();
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(observableMap);
            }
        };
    }

    public static <K, V> ObjectBinding<V> valueAt(final ObservableMap<K, V> observableMap, final K k) {
        if (observableMap == null) {
            throw new NullPointerException("Map cannot be null.");
        }
        return new ObjectBinding<V>(){
            {
                super.bind(observableMap);
            }

            @Override
            public void dispose() {
                super.unbind(observableMap);
            }

            @Override
            protected V computeValue() {
                try {
                    return observableMap.get(k);
                }
                catch (ClassCastException classCastException) {
                    Logging.getLogger().warning("Exception while evaluating binding", classCastException);
                }
                catch (NullPointerException nullPointerException) {
                    Logging.getLogger().warning("Exception while evaluating binding", nullPointerException);
                }
                return null;
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(observableMap);
            }
        };
    }

    public static <K, V> ObjectBinding<V> valueAt(final ObservableMap<K, V> observableMap, final ObservableValue<? extends K> observableValue) {
        if (observableMap == null || observableValue == null) {
            throw new NullPointerException("Operands cannot be null.");
        }
        return new ObjectBinding<V>(){
            {
                super.bind(observableMap, observableValue);
            }

            @Override
            public void dispose() {
                super.unbind(observableMap);
            }

            @Override
            protected V computeValue() {
                try {
                    return observableMap.get(observableValue.getValue());
                }
                catch (ClassCastException classCastException) {
                    Logging.getLogger().warning("Exception while evaluating binding", classCastException);
                }
                catch (NullPointerException nullPointerException) {
                    Logging.getLogger().warning("Exception while evaluating binding", nullPointerException);
                }
                return null;
            }

            @Override
            public ObservableList<?> getDependencies() {
                return new ImmutableObservableList<Observable>(observableMap, observableValue);
            }
        };
    }

    public static <K> BooleanBinding booleanValueAt(final ObservableMap<K, Boolean> observableMap, final K k) {
        if (observableMap == null) {
            throw new NullPointerException("Map cannot be null.");
        }
        return new BooleanBinding(){
            {
                super.bind(observableMap);
            }

            @Override
            public void dispose() {
                super.unbind(observableMap);
            }

            @Override
            protected boolean computeValue() {
                try {
                    Boolean bl = (Boolean)observableMap.get(k);
                    if (bl != null) {
                        return bl;
                    }
                    Logging.getLogger().fine("Element not found in map, returning default value instead.", new NullPointerException());
                }
                catch (ClassCastException classCastException) {
                    Logging.getLogger().warning("Exception while evaluating binding", classCastException);
                }
                catch (NullPointerException nullPointerException) {
                    Logging.getLogger().warning("Exception while evaluating binding", nullPointerException);
                }
                return false;
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(observableMap);
            }
        };
    }

    public static <K> BooleanBinding booleanValueAt(final ObservableMap<K, Boolean> observableMap, final ObservableValue<? extends K> observableValue) {
        if (observableMap == null || observableValue == null) {
            throw new NullPointerException("Operands cannot be null.");
        }
        return new BooleanBinding(){
            {
                super.bind(observableMap, observableValue);
            }

            @Override
            public void dispose() {
                super.unbind(observableMap, observableValue);
            }

            @Override
            protected boolean computeValue() {
                try {
                    Boolean bl = (Boolean)observableMap.get(observableValue.getValue());
                    if (bl != null) {
                        return bl;
                    }
                    Logging.getLogger().fine("Element not found in map, returning default value instead.", new NullPointerException());
                }
                catch (ClassCastException classCastException) {
                    Logging.getLogger().warning("Exception while evaluating binding", classCastException);
                }
                catch (NullPointerException nullPointerException) {
                    Logging.getLogger().warning("Exception while evaluating binding", nullPointerException);
                }
                return false;
            }

            @Override
            public ObservableList<?> getDependencies() {
                return new ImmutableObservableList<Observable>(observableMap, observableValue);
            }
        };
    }

    public static <K> DoubleBinding doubleValueAt(final ObservableMap<K, ? extends Number> observableMap, final K k) {
        if (observableMap == null) {
            throw new NullPointerException("Map cannot be null.");
        }
        return new DoubleBinding(){
            {
                super.bind(observableMap);
            }

            @Override
            public void dispose() {
                super.unbind(observableMap);
            }

            @Override
            protected double computeValue() {
                try {
                    Number number = (Number)observableMap.get(k);
                    if (number != null) {
                        return number.doubleValue();
                    }
                    Logging.getLogger().fine("Element not found in map, returning default value instead.", new NullPointerException());
                }
                catch (ClassCastException classCastException) {
                    Logging.getLogger().warning("Exception while evaluating binding", classCastException);
                }
                catch (NullPointerException nullPointerException) {
                    Logging.getLogger().warning("Exception while evaluating binding", nullPointerException);
                }
                return 0.0;
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(observableMap);
            }
        };
    }

    public static <K> DoubleBinding doubleValueAt(final ObservableMap<K, ? extends Number> observableMap, final ObservableValue<? extends K> observableValue) {
        if (observableMap == null || observableValue == null) {
            throw new NullPointerException("Operands cannot be null.");
        }
        return new DoubleBinding(){
            {
                super.bind(observableMap, observableValue);
            }

            @Override
            public void dispose() {
                super.unbind(observableMap, observableValue);
            }

            @Override
            protected double computeValue() {
                try {
                    Number number = (Number)observableMap.get(observableValue.getValue());
                    if (number != null) {
                        return number.doubleValue();
                    }
                    Logging.getLogger().fine("Element not found in map, returning default value instead.", new NullPointerException());
                }
                catch (ClassCastException classCastException) {
                    Logging.getLogger().warning("Exception while evaluating binding", classCastException);
                }
                catch (NullPointerException nullPointerException) {
                    Logging.getLogger().warning("Exception while evaluating binding", nullPointerException);
                }
                return 0.0;
            }

            @Override
            public ObservableList<?> getDependencies() {
                return new ImmutableObservableList<Observable>(observableMap, observableValue);
            }
        };
    }

    public static <K> FloatBinding floatValueAt(final ObservableMap<K, ? extends Number> observableMap, final K k) {
        if (observableMap == null) {
            throw new NullPointerException("Map cannot be null.");
        }
        return new FloatBinding(){
            {
                super.bind(observableMap);
            }

            @Override
            public void dispose() {
                super.unbind(observableMap);
            }

            @Override
            protected float computeValue() {
                try {
                    Number number = (Number)observableMap.get(k);
                    if (number != null) {
                        return number.floatValue();
                    }
                    Logging.getLogger().fine("Element not found in map, returning default value instead.", new NullPointerException());
                }
                catch (ClassCastException classCastException) {
                    Logging.getLogger().warning("Exception while evaluating binding", classCastException);
                }
                catch (NullPointerException nullPointerException) {
                    Logging.getLogger().warning("Exception while evaluating binding", nullPointerException);
                }
                return 0.0f;
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(observableMap);
            }
        };
    }

    public static <K> FloatBinding floatValueAt(final ObservableMap<K, ? extends Number> observableMap, final ObservableValue<? extends K> observableValue) {
        if (observableMap == null || observableValue == null) {
            throw new NullPointerException("Operands cannot be null.");
        }
        return new FloatBinding(){
            {
                super.bind(observableMap, observableValue);
            }

            @Override
            public void dispose() {
                super.unbind(observableMap, observableValue);
            }

            @Override
            protected float computeValue() {
                try {
                    Number number = (Number)observableMap.get(observableValue.getValue());
                    if (number != null) {
                        return number.floatValue();
                    }
                    Logging.getLogger().fine("Element not found in map, returning default value instead.", new NullPointerException());
                }
                catch (ClassCastException classCastException) {
                    Logging.getLogger().warning("Exception while evaluating binding", classCastException);
                }
                catch (NullPointerException nullPointerException) {
                    Logging.getLogger().warning("Exception while evaluating binding", nullPointerException);
                }
                return 0.0f;
            }

            @Override
            public ObservableList<?> getDependencies() {
                return new ImmutableObservableList<Observable>(observableMap, observableValue);
            }
        };
    }

    public static <K> IntegerBinding integerValueAt(final ObservableMap<K, ? extends Number> observableMap, final K k) {
        if (observableMap == null) {
            throw new NullPointerException("Map cannot be null.");
        }
        return new IntegerBinding(){
            {
                super.bind(observableMap);
            }

            @Override
            public void dispose() {
                super.unbind(observableMap);
            }

            @Override
            protected int computeValue() {
                try {
                    Number number = (Number)observableMap.get(k);
                    if (number != null) {
                        return number.intValue();
                    }
                    Logging.getLogger().fine("Element not found in map, returning default value instead.", new NullPointerException());
                }
                catch (ClassCastException classCastException) {
                    Logging.getLogger().warning("Exception while evaluating binding", classCastException);
                }
                catch (NullPointerException nullPointerException) {
                    Logging.getLogger().warning("Exception while evaluating binding", nullPointerException);
                }
                return 0;
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(observableMap);
            }
        };
    }

    public static <K> IntegerBinding integerValueAt(final ObservableMap<K, ? extends Number> observableMap, final ObservableValue<? extends K> observableValue) {
        if (observableMap == null || observableValue == null) {
            throw new NullPointerException("Operands cannot be null.");
        }
        return new IntegerBinding(){
            {
                super.bind(observableMap, observableValue);
            }

            @Override
            public void dispose() {
                super.unbind(observableMap, observableValue);
            }

            @Override
            protected int computeValue() {
                try {
                    Number number = (Number)observableMap.get(observableValue.getValue());
                    if (number != null) {
                        return number.intValue();
                    }
                    Logging.getLogger().fine("Element not found in map, returning default value instead.", new NullPointerException());
                }
                catch (ClassCastException classCastException) {
                    Logging.getLogger().warning("Exception while evaluating binding", classCastException);
                }
                catch (NullPointerException nullPointerException) {
                    Logging.getLogger().warning("Exception while evaluating binding", nullPointerException);
                }
                return 0;
            }

            @Override
            public ObservableList<?> getDependencies() {
                return new ImmutableObservableList<Observable>(observableMap, observableValue);
            }
        };
    }

    public static <K> LongBinding longValueAt(final ObservableMap<K, ? extends Number> observableMap, final K k) {
        if (observableMap == null) {
            throw new NullPointerException("Map cannot be null.");
        }
        return new LongBinding(){
            {
                super.bind(observableMap);
            }

            @Override
            public void dispose() {
                super.unbind(observableMap);
            }

            @Override
            protected long computeValue() {
                try {
                    Number number = (Number)observableMap.get(k);
                    if (number != null) {
                        return number.longValue();
                    }
                    Logging.getLogger().fine("Element not found in map, returning default value instead.", new NullPointerException());
                }
                catch (ClassCastException classCastException) {
                    Logging.getLogger().warning("Exception while evaluating binding", classCastException);
                }
                catch (NullPointerException nullPointerException) {
                    Logging.getLogger().warning("Exception while evaluating binding", nullPointerException);
                }
                return 0L;
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(observableMap);
            }
        };
    }

    public static <K> LongBinding longValueAt(final ObservableMap<K, ? extends Number> observableMap, final ObservableValue<? extends K> observableValue) {
        if (observableMap == null || observableValue == null) {
            throw new NullPointerException("Operands cannot be null.");
        }
        return new LongBinding(){
            {
                super.bind(observableMap, observableValue);
            }

            @Override
            public void dispose() {
                super.unbind(observableMap, observableValue);
            }

            @Override
            protected long computeValue() {
                try {
                    Number number = (Number)observableMap.get(observableValue.getValue());
                    if (number != null) {
                        return number.longValue();
                    }
                    Logging.getLogger().fine("Element not found in map, returning default value instead.", new NullPointerException());
                }
                catch (ClassCastException classCastException) {
                    Logging.getLogger().warning("Exception while evaluating binding", classCastException);
                }
                catch (NullPointerException nullPointerException) {
                    Logging.getLogger().warning("Exception while evaluating binding", nullPointerException);
                }
                return 0L;
            }

            @Override
            public ObservableList<?> getDependencies() {
                return new ImmutableObservableList<Observable>(observableMap, observableValue);
            }
        };
    }

    public static <K> StringBinding stringValueAt(final ObservableMap<K, String> observableMap, final K k) {
        if (observableMap == null) {
            throw new NullPointerException("Map cannot be null.");
        }
        return new StringBinding(){
            {
                super.bind(observableMap);
            }

            @Override
            public void dispose() {
                super.unbind(observableMap);
            }

            @Override
            protected String computeValue() {
                try {
                    return (String)observableMap.get(k);
                }
                catch (ClassCastException classCastException) {
                    Logging.getLogger().warning("Exception while evaluating binding", classCastException);
                }
                catch (NullPointerException nullPointerException) {
                    Logging.getLogger().warning("Exception while evaluating binding", nullPointerException);
                }
                return null;
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(observableMap);
            }
        };
    }

    public static <K> StringBinding stringValueAt(final ObservableMap<K, String> observableMap, final ObservableValue<? extends K> observableValue) {
        if (observableMap == null || observableValue == null) {
            throw new NullPointerException("Operands cannot be null.");
        }
        return new StringBinding(){
            {
                super.bind(observableMap, observableValue);
            }

            @Override
            public void dispose() {
                super.unbind(observableMap, observableValue);
            }

            @Override
            protected String computeValue() {
                try {
                    return (String)observableMap.get(observableValue.getValue());
                }
                catch (ClassCastException classCastException) {
                    Logging.getLogger().warning("Exception while evaluating binding", classCastException);
                }
                catch (NullPointerException nullPointerException) {
                    Logging.getLogger().warning("Exception while evaluating binding", nullPointerException);
                }
                return null;
            }

            @Override
            public ObservableList<?> getDependencies() {
                return new ImmutableObservableList<Observable>(observableMap, observableValue);
            }
        };
    }

    private static class ShortCircuitOrInvalidator
    implements InvalidationListener {
        private final WeakReference<BooleanOrBinding> ref;

        private ShortCircuitOrInvalidator(BooleanOrBinding booleanOrBinding) {
            assert (booleanOrBinding != null);
            this.ref = new WeakReference<BooleanOrBinding>(booleanOrBinding);
        }

        @Override
        public void invalidated(Observable observable) {
            BooleanOrBinding booleanOrBinding = (BooleanOrBinding)this.ref.get();
            if (booleanOrBinding == null) {
                observable.removeListener(this);
            } else if (booleanOrBinding.op1.equals(observable) || booleanOrBinding.isValid() && !booleanOrBinding.op1.get()) {
                booleanOrBinding.invalidate();
            }
        }
    }

    private static class BooleanOrBinding
    extends BooleanBinding {
        private final ObservableBooleanValue op1;
        private final ObservableBooleanValue op2;
        private final InvalidationListener observer;

        public BooleanOrBinding(ObservableBooleanValue observableBooleanValue, ObservableBooleanValue observableBooleanValue2) {
            this.op1 = observableBooleanValue;
            this.op2 = observableBooleanValue2;
            this.observer = new ShortCircuitOrInvalidator(this);
            observableBooleanValue.addListener(this.observer);
            observableBooleanValue2.addListener(this.observer);
        }

        @Override
        public void dispose() {
            this.op1.removeListener(this.observer);
            this.op2.removeListener(this.observer);
        }

        @Override
        protected boolean computeValue() {
            return this.op1.get() || this.op2.get();
        }

        @Override
        public ObservableList<?> getDependencies() {
            return new ImmutableObservableList<ObservableBooleanValue>(this.op1, this.op2);
        }
    }

    private static class ShortCircuitAndInvalidator
    implements InvalidationListener {
        private final WeakReference<BooleanAndBinding> ref;

        private ShortCircuitAndInvalidator(BooleanAndBinding booleanAndBinding) {
            assert (booleanAndBinding != null);
            this.ref = new WeakReference<BooleanAndBinding>(booleanAndBinding);
        }

        @Override
        public void invalidated(Observable observable) {
            BooleanAndBinding booleanAndBinding = (BooleanAndBinding)this.ref.get();
            if (booleanAndBinding == null) {
                observable.removeListener(this);
            } else if (booleanAndBinding.op1.equals(observable) || booleanAndBinding.isValid() && booleanAndBinding.op1.get()) {
                booleanAndBinding.invalidate();
            }
        }
    }

    private static class BooleanAndBinding
    extends BooleanBinding {
        private final ObservableBooleanValue op1;
        private final ObservableBooleanValue op2;
        private final InvalidationListener observer;

        public BooleanAndBinding(ObservableBooleanValue observableBooleanValue, ObservableBooleanValue observableBooleanValue2) {
            this.op1 = observableBooleanValue;
            this.op2 = observableBooleanValue2;
            this.observer = new ShortCircuitAndInvalidator(this);
            observableBooleanValue.addListener(this.observer);
            observableBooleanValue2.addListener(this.observer);
        }

        @Override
        public void dispose() {
            this.op1.removeListener(this.observer);
            this.op2.removeListener(this.observer);
        }

        @Override
        protected boolean computeValue() {
            return this.op1.get() && this.op2.get();
        }

        @Override
        public ObservableList<?> getDependencies() {
            return new ImmutableObservableList<ObservableBooleanValue>(this.op1, this.op2);
        }
    }
}

