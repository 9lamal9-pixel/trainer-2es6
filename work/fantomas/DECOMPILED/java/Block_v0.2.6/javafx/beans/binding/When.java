/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.binding;

import com.sun.javafx.binding.DoubleConstant;
import com.sun.javafx.binding.FloatConstant;
import com.sun.javafx.binding.IntegerConstant;
import com.sun.javafx.binding.Logging;
import com.sun.javafx.binding.LongConstant;
import java.lang.ref.WeakReference;
import javafx.beans.InvalidationListener;
import javafx.beans.NamedArg;
import javafx.beans.Observable;
import javafx.beans.binding.Binding;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.FloatBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.LongBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableFloatValue;
import javafx.beans.value.ObservableLongValue;
import javafx.beans.value.ObservableNumberValue;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class When {
    private final ObservableBooleanValue condition;

    public When(@NamedArg(value="condition") ObservableBooleanValue observableBooleanValue) {
        if (observableBooleanValue == null) {
            throw new NullPointerException("Condition must be specified.");
        }
        this.condition = observableBooleanValue;
    }

    private static NumberBinding createNumberCondition(final ObservableBooleanValue observableBooleanValue, final ObservableNumberValue observableNumberValue, final ObservableNumberValue observableNumberValue2) {
        if (observableNumberValue instanceof ObservableDoubleValue || observableNumberValue2 instanceof ObservableDoubleValue) {
            return new DoubleBinding(){
                final InvalidationListener observer;
                {
                    this.observer = new WhenListener(this, observableBooleanValue, observableNumberValue, observableNumberValue2);
                    observableBooleanValue.addListener(this.observer);
                    observableNumberValue.addListener(this.observer);
                    observableNumberValue2.addListener(this.observer);
                }

                @Override
                public void dispose() {
                    observableBooleanValue.removeListener(this.observer);
                    observableNumberValue.removeListener(this.observer);
                    observableNumberValue2.removeListener(this.observer);
                }

                @Override
                protected double computeValue() {
                    boolean bl = observableBooleanValue.get();
                    Logging.getLogger().finest("Condition of ternary binding expression was evaluated: {0}", bl);
                    return bl ? observableNumberValue.doubleValue() : observableNumberValue2.doubleValue();
                }

                @Override
                public ObservableList<ObservableValue<?>> getDependencies() {
                    return FXCollections.unmodifiableObservableList(FXCollections.observableArrayList(observableBooleanValue, observableNumberValue, observableNumberValue2));
                }
            };
        }
        if (observableNumberValue instanceof ObservableFloatValue || observableNumberValue2 instanceof ObservableFloatValue) {
            return new FloatBinding(){
                final InvalidationListener observer;
                {
                    this.observer = new WhenListener(this, observableBooleanValue, observableNumberValue, observableNumberValue2);
                    observableBooleanValue.addListener(this.observer);
                    observableNumberValue.addListener(this.observer);
                    observableNumberValue2.addListener(this.observer);
                }

                @Override
                public void dispose() {
                    observableBooleanValue.removeListener(this.observer);
                    observableNumberValue.removeListener(this.observer);
                    observableNumberValue2.removeListener(this.observer);
                }

                @Override
                protected float computeValue() {
                    boolean bl = observableBooleanValue.get();
                    Logging.getLogger().finest("Condition of ternary binding expression was evaluated: {0}", bl);
                    return bl ? observableNumberValue.floatValue() : observableNumberValue2.floatValue();
                }

                @Override
                public ObservableList<ObservableValue<?>> getDependencies() {
                    return FXCollections.unmodifiableObservableList(FXCollections.observableArrayList(observableBooleanValue, observableNumberValue, observableNumberValue2));
                }
            };
        }
        if (observableNumberValue instanceof ObservableLongValue || observableNumberValue2 instanceof ObservableLongValue) {
            return new LongBinding(){
                final InvalidationListener observer;
                {
                    this.observer = new WhenListener(this, observableBooleanValue, observableNumberValue, observableNumberValue2);
                    observableBooleanValue.addListener(this.observer);
                    observableNumberValue.addListener(this.observer);
                    observableNumberValue2.addListener(this.observer);
                }

                @Override
                public void dispose() {
                    observableBooleanValue.removeListener(this.observer);
                    observableNumberValue.removeListener(this.observer);
                    observableNumberValue2.removeListener(this.observer);
                }

                @Override
                protected long computeValue() {
                    boolean bl = observableBooleanValue.get();
                    Logging.getLogger().finest("Condition of ternary binding expression was evaluated: {0}", bl);
                    return bl ? observableNumberValue.longValue() : observableNumberValue2.longValue();
                }

                @Override
                public ObservableList<ObservableValue<?>> getDependencies() {
                    return FXCollections.unmodifiableObservableList(FXCollections.observableArrayList(observableBooleanValue, observableNumberValue, observableNumberValue2));
                }
            };
        }
        return new IntegerBinding(){
            final InvalidationListener observer;
            {
                this.observer = new WhenListener(this, observableBooleanValue, observableNumberValue, observableNumberValue2);
                observableBooleanValue.addListener(this.observer);
                observableNumberValue.addListener(this.observer);
                observableNumberValue2.addListener(this.observer);
            }

            @Override
            public void dispose() {
                observableBooleanValue.removeListener(this.observer);
                observableNumberValue.removeListener(this.observer);
                observableNumberValue2.removeListener(this.observer);
            }

            @Override
            protected int computeValue() {
                boolean bl = observableBooleanValue.get();
                Logging.getLogger().finest("Condition of ternary binding expression was evaluated: {0}", bl);
                return bl ? observableNumberValue.intValue() : observableNumberValue2.intValue();
            }

            @Override
            public ObservableList<ObservableValue<?>> getDependencies() {
                return FXCollections.unmodifiableObservableList(FXCollections.observableArrayList(observableBooleanValue, observableNumberValue, observableNumberValue2));
            }
        };
    }

    public NumberConditionBuilder then(ObservableNumberValue observableNumberValue) {
        if (observableNumberValue == null) {
            throw new NullPointerException("Value needs to be specified");
        }
        return new NumberConditionBuilder(observableNumberValue);
    }

    public NumberConditionBuilder then(double d) {
        return new NumberConditionBuilder(DoubleConstant.valueOf(d));
    }

    public NumberConditionBuilder then(float f) {
        return new NumberConditionBuilder(FloatConstant.valueOf(f));
    }

    public NumberConditionBuilder then(long l) {
        return new NumberConditionBuilder(LongConstant.valueOf(l));
    }

    public NumberConditionBuilder then(int n) {
        return new NumberConditionBuilder(IntegerConstant.valueOf(n));
    }

    public BooleanConditionBuilder then(ObservableBooleanValue observableBooleanValue) {
        if (observableBooleanValue == null) {
            throw new NullPointerException("Value needs to be specified");
        }
        return new BooleanConditionBuilder(observableBooleanValue);
    }

    public BooleanConditionBuilder then(boolean bl) {
        return new BooleanConditionBuilder(bl);
    }

    public StringConditionBuilder then(ObservableStringValue observableStringValue) {
        if (observableStringValue == null) {
            throw new NullPointerException("Value needs to be specified");
        }
        return new StringConditionBuilder(observableStringValue);
    }

    public StringConditionBuilder then(String string) {
        return new StringConditionBuilder(string);
    }

    public <T> ObjectConditionBuilder<T> then(ObservableObjectValue<T> observableObjectValue) {
        if (observableObjectValue == null) {
            throw new NullPointerException("Value needs to be specified");
        }
        return new ObjectConditionBuilder(observableObjectValue, null);
    }

    public <T> ObjectConditionBuilder<T> then(T t) {
        return new ObjectConditionBuilder(t);
    }

    public class ObjectConditionBuilder<T> {
        private ObservableObjectValue<T> trueResult;
        private T trueResultValue;

        private ObjectConditionBuilder(ObservableObjectValue<T> observableObjectValue) {
            this.trueResult = observableObjectValue;
        }

        private ObjectConditionBuilder(T t) {
            this.trueResultValue = t;
        }

        public ObjectBinding<T> otherwise(ObservableObjectValue<T> observableObjectValue) {
            if (observableObjectValue == null) {
                throw new NullPointerException("Value needs to be specified");
            }
            if (this.trueResult != null) {
                return new ObjectCondition((ObservableObjectValue)this.trueResult, (ObservableObjectValue)observableObjectValue, null);
            }
            return new ObjectCondition(this.trueResultValue, observableObjectValue, null);
        }

        public ObjectBinding<T> otherwise(T t) {
            if (this.trueResult != null) {
                return new ObjectCondition(this.trueResult, t, null);
            }
            return new ObjectCondition(this.trueResultValue, t);
        }

        /* synthetic */ ObjectConditionBuilder(ObservableObjectValue observableObjectValue, 1 var3_3) {
            this(observableObjectValue);
        }
    }

    private class ObjectCondition<T>
    extends ObjectBinding<T> {
        private final ObservableObjectValue<T> trueResult;
        private final T trueResultValue;
        private final ObservableObjectValue<T> falseResult;
        private final T falseResultValue;
        private final InvalidationListener observer;

        private ObjectCondition(ObservableObjectValue<T> observableObjectValue, ObservableObjectValue<T> observableObjectValue2) {
            this.trueResult = observableObjectValue;
            this.trueResultValue = null;
            this.falseResult = observableObjectValue2;
            this.falseResultValue = null;
            this.observer = new WhenListener(this, When.this.condition, observableObjectValue, observableObjectValue2);
            When.this.condition.addListener(this.observer);
            observableObjectValue.addListener(this.observer);
            observableObjectValue2.addListener(this.observer);
        }

        private ObjectCondition(T t, ObservableObjectValue<T> observableObjectValue) {
            this.trueResult = null;
            this.trueResultValue = t;
            this.falseResult = observableObjectValue;
            this.falseResultValue = null;
            this.observer = new WhenListener(this, When.this.condition, null, observableObjectValue);
            When.this.condition.addListener(this.observer);
            observableObjectValue.addListener(this.observer);
        }

        private ObjectCondition(ObservableObjectValue<T> observableObjectValue, T t) {
            this.trueResult = observableObjectValue;
            this.trueResultValue = null;
            this.falseResult = null;
            this.falseResultValue = t;
            this.observer = new WhenListener(this, When.this.condition, observableObjectValue, null);
            When.this.condition.addListener(this.observer);
            observableObjectValue.addListener(this.observer);
        }

        private ObjectCondition(T t, T t2) {
            this.trueResult = null;
            this.trueResultValue = t;
            this.falseResult = null;
            this.falseResultValue = t2;
            this.observer = null;
            super.bind(When.this.condition);
        }

        @Override
        protected T computeValue() {
            boolean bl = When.this.condition.get();
            Logging.getLogger().finest("Condition of ternary binding expression was evaluated: {0}", bl);
            return bl ? (this.trueResult != null ? this.trueResult.get() : this.trueResultValue) : (this.falseResult != null ? this.falseResult.get() : this.falseResultValue);
        }

        @Override
        public void dispose() {
            if (this.observer == null) {
                super.unbind(When.this.condition);
            } else {
                When.this.condition.removeListener(this.observer);
                if (this.trueResult != null) {
                    this.trueResult.removeListener(this.observer);
                }
                if (this.falseResult != null) {
                    this.falseResult.removeListener(this.observer);
                }
            }
        }

        @Override
        public ObservableList<ObservableValue<?>> getDependencies() {
            assert (When.this.condition != null);
            ObservableList<ObservableValue> observableList = FXCollections.observableArrayList(When.this.condition);
            if (this.trueResult != null) {
                observableList.add(this.trueResult);
            }
            if (this.falseResult != null) {
                observableList.add(this.falseResult);
            }
            return FXCollections.unmodifiableObservableList(observableList);
        }

        /* synthetic */ ObjectCondition(ObservableObjectValue observableObjectValue, ObservableObjectValue observableObjectValue2, 1 var4_4) {
            this(observableObjectValue, observableObjectValue2);
        }

        /* synthetic */ ObjectCondition(Object object, ObservableObjectValue observableObjectValue, 1 var4_4) {
            this((ObservableObjectValue)object, observableObjectValue);
        }

        /* synthetic */ ObjectCondition(ObservableObjectValue observableObjectValue, Object object, 1 var4_4) {
            this((Object)observableObjectValue, object);
        }
    }

    public class StringConditionBuilder {
        private ObservableStringValue trueResult;
        private String trueResultValue;

        private StringConditionBuilder(ObservableStringValue observableStringValue) {
            this.trueResult = observableStringValue;
        }

        private StringConditionBuilder(String string) {
            this.trueResultValue = string;
        }

        public StringBinding otherwise(ObservableStringValue observableStringValue) {
            if (this.trueResult != null) {
                return new StringCondition(this.trueResult, observableStringValue);
            }
            return new StringCondition(this.trueResultValue, observableStringValue);
        }

        public StringBinding otherwise(String string) {
            if (this.trueResult != null) {
                return new StringCondition(this.trueResult, string);
            }
            return new StringCondition(this.trueResultValue, string);
        }
    }

    private class StringCondition
    extends StringBinding {
        private final ObservableStringValue trueResult;
        private final String trueResultValue;
        private final ObservableStringValue falseResult;
        private final String falseResultValue;
        private final InvalidationListener observer;

        private StringCondition(ObservableStringValue observableStringValue, ObservableStringValue observableStringValue2) {
            this.trueResult = observableStringValue;
            this.trueResultValue = "";
            this.falseResult = observableStringValue2;
            this.falseResultValue = "";
            this.observer = new WhenListener(this, When.this.condition, observableStringValue, observableStringValue2);
            When.this.condition.addListener(this.observer);
            observableStringValue.addListener(this.observer);
            observableStringValue2.addListener(this.observer);
        }

        private StringCondition(String string, ObservableStringValue observableStringValue) {
            this.trueResult = null;
            this.trueResultValue = string;
            this.falseResult = observableStringValue;
            this.falseResultValue = "";
            this.observer = new WhenListener(this, When.this.condition, null, observableStringValue);
            When.this.condition.addListener(this.observer);
            observableStringValue.addListener(this.observer);
        }

        private StringCondition(ObservableStringValue observableStringValue, String string) {
            this.trueResult = observableStringValue;
            this.trueResultValue = "";
            this.falseResult = null;
            this.falseResultValue = string;
            this.observer = new WhenListener(this, When.this.condition, observableStringValue, null);
            When.this.condition.addListener(this.observer);
            observableStringValue.addListener(this.observer);
        }

        private StringCondition(String string, String string2) {
            this.trueResult = null;
            this.trueResultValue = string;
            this.falseResult = null;
            this.falseResultValue = string2;
            this.observer = null;
            super.bind(When.this.condition);
        }

        @Override
        protected String computeValue() {
            boolean bl = When.this.condition.get();
            Logging.getLogger().finest("Condition of ternary binding expression was evaluated: {0}", bl);
            return bl ? (this.trueResult != null ? (String)this.trueResult.get() : this.trueResultValue) : (this.falseResult != null ? (String)this.falseResult.get() : this.falseResultValue);
        }

        @Override
        public void dispose() {
            if (this.observer == null) {
                super.unbind(When.this.condition);
            } else {
                When.this.condition.removeListener(this.observer);
                if (this.trueResult != null) {
                    this.trueResult.removeListener(this.observer);
                }
                if (this.falseResult != null) {
                    this.falseResult.removeListener(this.observer);
                }
            }
        }

        @Override
        public ObservableList<ObservableValue<?>> getDependencies() {
            assert (When.this.condition != null);
            ObservableList<ObservableValue> observableList = FXCollections.observableArrayList(When.this.condition);
            if (this.trueResult != null) {
                observableList.add(this.trueResult);
            }
            if (this.falseResult != null) {
                observableList.add(this.falseResult);
            }
            return FXCollections.unmodifiableObservableList(observableList);
        }
    }

    public class BooleanConditionBuilder {
        private ObservableBooleanValue trueResult;
        private boolean trueResultValue;

        private BooleanConditionBuilder(ObservableBooleanValue observableBooleanValue) {
            this.trueResult = observableBooleanValue;
        }

        private BooleanConditionBuilder(boolean bl) {
            this.trueResultValue = bl;
        }

        public BooleanBinding otherwise(ObservableBooleanValue observableBooleanValue) {
            if (observableBooleanValue == null) {
                throw new NullPointerException("Value needs to be specified");
            }
            if (this.trueResult != null) {
                return new BooleanCondition(this.trueResult, observableBooleanValue);
            }
            return new BooleanCondition(this.trueResultValue, observableBooleanValue);
        }

        public BooleanBinding otherwise(boolean bl) {
            if (this.trueResult != null) {
                return new BooleanCondition(this.trueResult, bl);
            }
            return new BooleanCondition(this.trueResultValue, bl);
        }
    }

    private class BooleanCondition
    extends BooleanBinding {
        private final ObservableBooleanValue trueResult;
        private final boolean trueResultValue;
        private final ObservableBooleanValue falseResult;
        private final boolean falseResultValue;
        private final InvalidationListener observer;

        private BooleanCondition(ObservableBooleanValue observableBooleanValue, ObservableBooleanValue observableBooleanValue2) {
            this.trueResult = observableBooleanValue;
            this.trueResultValue = false;
            this.falseResult = observableBooleanValue2;
            this.falseResultValue = false;
            this.observer = new WhenListener(this, When.this.condition, observableBooleanValue, observableBooleanValue2);
            When.this.condition.addListener(this.observer);
            observableBooleanValue.addListener(this.observer);
            observableBooleanValue2.addListener(this.observer);
        }

        private BooleanCondition(boolean bl, ObservableBooleanValue observableBooleanValue) {
            this.trueResult = null;
            this.trueResultValue = bl;
            this.falseResult = observableBooleanValue;
            this.falseResultValue = false;
            this.observer = new WhenListener(this, When.this.condition, null, observableBooleanValue);
            When.this.condition.addListener(this.observer);
            observableBooleanValue.addListener(this.observer);
        }

        private BooleanCondition(ObservableBooleanValue observableBooleanValue, boolean bl) {
            this.trueResult = observableBooleanValue;
            this.trueResultValue = false;
            this.falseResult = null;
            this.falseResultValue = bl;
            this.observer = new WhenListener(this, When.this.condition, observableBooleanValue, null);
            When.this.condition.addListener(this.observer);
            observableBooleanValue.addListener(this.observer);
        }

        private BooleanCondition(boolean bl, boolean bl2) {
            this.trueResult = null;
            this.trueResultValue = bl;
            this.falseResult = null;
            this.falseResultValue = bl2;
            this.observer = null;
            super.bind(When.this.condition);
        }

        @Override
        protected boolean computeValue() {
            boolean bl = When.this.condition.get();
            Logging.getLogger().finest("Condition of ternary binding expression was evaluated: {0}", bl);
            return bl ? (this.trueResult != null ? this.trueResult.get() : this.trueResultValue) : (this.falseResult != null ? this.falseResult.get() : this.falseResultValue);
        }

        @Override
        public void dispose() {
            if (this.observer == null) {
                super.unbind(When.this.condition);
            } else {
                When.this.condition.removeListener(this.observer);
                if (this.trueResult != null) {
                    this.trueResult.removeListener(this.observer);
                }
                if (this.falseResult != null) {
                    this.falseResult.removeListener(this.observer);
                }
            }
        }

        @Override
        public ObservableList<ObservableValue<?>> getDependencies() {
            assert (When.this.condition != null);
            ObservableList<ObservableValue> observableList = FXCollections.observableArrayList(When.this.condition);
            if (this.trueResult != null) {
                observableList.add(this.trueResult);
            }
            if (this.falseResult != null) {
                observableList.add(this.falseResult);
            }
            return FXCollections.unmodifiableObservableList(observableList);
        }
    }

    public class NumberConditionBuilder {
        private ObservableNumberValue thenValue;

        private NumberConditionBuilder(ObservableNumberValue observableNumberValue) {
            this.thenValue = observableNumberValue;
        }

        public NumberBinding otherwise(ObservableNumberValue observableNumberValue) {
            if (observableNumberValue == null) {
                throw new NullPointerException("Value needs to be specified");
            }
            return When.createNumberCondition(When.this.condition, this.thenValue, observableNumberValue);
        }

        public DoubleBinding otherwise(double d) {
            return (DoubleBinding)this.otherwise(DoubleConstant.valueOf(d));
        }

        public NumberBinding otherwise(float f) {
            return this.otherwise(FloatConstant.valueOf(f));
        }

        public NumberBinding otherwise(long l) {
            return this.otherwise(LongConstant.valueOf(l));
        }

        public NumberBinding otherwise(int n) {
            return this.otherwise(IntegerConstant.valueOf(n));
        }
    }

    private static class WhenListener
    implements InvalidationListener {
        private final ObservableBooleanValue condition;
        private final ObservableValue<?> thenValue;
        private final ObservableValue<?> otherwiseValue;
        private final WeakReference<Binding<?>> ref;

        private WhenListener(Binding<?> binding, ObservableBooleanValue observableBooleanValue, ObservableValue<?> observableValue, ObservableValue<?> observableValue2) {
            this.ref = new WeakReference(binding);
            this.condition = observableBooleanValue;
            this.thenValue = observableValue;
            this.otherwiseValue = observableValue2;
        }

        @Override
        public void invalidated(Observable observable) {
            Binding binding = (Binding)this.ref.get();
            if (binding == null) {
                this.condition.removeListener(this);
                if (this.thenValue != null) {
                    this.thenValue.removeListener(this);
                }
                if (this.otherwiseValue != null) {
                    this.otherwiseValue.removeListener(this);
                }
            } else if (this.condition.equals(observable) || binding.isValid() && this.condition.get() == observable.equals(this.thenValue)) {
                binding.invalidate();
            }
        }
    }
}

