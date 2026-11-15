/*
 * Decompiled with CFR 0.152.
 */
package javafx.animation;

import com.sun.javafx.animation.KeyValueHelper;
import com.sun.javafx.animation.KeyValueType;
import javafx.animation.Interpolator;
import javafx.beans.NamedArg;
import javafx.beans.value.WritableBooleanValue;
import javafx.beans.value.WritableDoubleValue;
import javafx.beans.value.WritableFloatValue;
import javafx.beans.value.WritableIntegerValue;
import javafx.beans.value.WritableLongValue;
import javafx.beans.value.WritableNumberValue;
import javafx.beans.value.WritableValue;

public final class KeyValue {
    private static final Interpolator DEFAULT_INTERPOLATOR = Interpolator.LINEAR;
    private final KeyValueType type;
    private final WritableValue<?> target;
    private final Object endValue;
    private final Interpolator interpolator;

    KeyValueType getType() {
        return this.type;
    }

    public WritableValue<?> getTarget() {
        return this.target;
    }

    public Object getEndValue() {
        return this.endValue;
    }

    public Interpolator getInterpolator() {
        return this.interpolator;
    }

    public <T> KeyValue(@NamedArg(value="target") WritableValue<T> writableValue, @NamedArg(value="endValue") T t, @NamedArg(value="interpolator") Interpolator interpolator) {
        if (writableValue == null) {
            throw new NullPointerException("Target needs to be specified");
        }
        if (interpolator == null) {
            throw new NullPointerException("Interpolator needs to be specified");
        }
        this.target = writableValue;
        this.endValue = t;
        this.interpolator = interpolator;
        this.type = writableValue instanceof WritableNumberValue ? (writableValue instanceof WritableDoubleValue ? KeyValueType.DOUBLE : (writableValue instanceof WritableIntegerValue ? KeyValueType.INTEGER : (writableValue instanceof WritableFloatValue ? KeyValueType.FLOAT : (writableValue instanceof WritableLongValue ? KeyValueType.LONG : KeyValueType.OBJECT)))) : (writableValue instanceof WritableBooleanValue ? KeyValueType.BOOLEAN : KeyValueType.OBJECT);
    }

    public <T> KeyValue(@NamedArg(value="target") WritableValue<T> writableValue, @NamedArg(value="endValue") T t) {
        this(writableValue, t, DEFAULT_INTERPOLATOR);
    }

    public String toString() {
        return "KeyValue [target=" + this.target + ", endValue=" + this.endValue + ", interpolator=" + this.interpolator + "]";
    }

    public int hashCode() {
        assert (this.target != null && this.interpolator != null);
        int n = 1;
        n = 31 * n + this.target.hashCode();
        n = 31 * n + (this.endValue == null ? 0 : this.endValue.hashCode());
        n = 31 * n + this.interpolator.hashCode();
        return n;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof KeyValue) {
            KeyValue keyValue = (KeyValue)object;
            assert (this.target != null && this.interpolator != null && keyValue.target != null && keyValue.interpolator != null);
            return this.target.equals(keyValue.target) && (this.endValue == null ? keyValue.endValue == null : this.endValue.equals(keyValue.endValue)) && this.interpolator.equals(keyValue.interpolator);
        }
        return false;
    }

    static {
        KeyValueHelper.setKeyValueAccessor(new KeyValueHelper.KeyValueAccessor(){

            @Override
            public KeyValueType getType(KeyValue keyValue) {
                return keyValue.getType();
            }
        });
    }
}

