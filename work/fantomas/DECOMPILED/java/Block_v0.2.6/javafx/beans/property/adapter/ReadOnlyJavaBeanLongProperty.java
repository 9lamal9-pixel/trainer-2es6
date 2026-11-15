/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property.adapter;

import com.sun.javafx.property.MethodHelper;
import com.sun.javafx.property.adapter.Disposer;
import com.sun.javafx.property.adapter.ReadOnlyPropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.util.Objects;
import javafx.beans.property.ReadOnlyLongPropertyBase;
import javafx.beans.property.adapter.DescriptorListenerCleaner;
import javafx.beans.property.adapter.ReadOnlyJavaBeanProperty;

public final class ReadOnlyJavaBeanLongProperty
extends ReadOnlyLongPropertyBase
implements ReadOnlyJavaBeanProperty<Number> {
    private final ReadOnlyPropertyDescriptor descriptor;
    private final ReadOnlyPropertyDescriptor.ReadOnlyListener<Number> listener;
    private final AccessControlContext acc = AccessController.getContext();

    ReadOnlyJavaBeanLongProperty(ReadOnlyPropertyDescriptor readOnlyPropertyDescriptor, Object object) {
        this.descriptor = readOnlyPropertyDescriptor;
        ReadOnlyPropertyDescriptor readOnlyPropertyDescriptor2 = readOnlyPropertyDescriptor;
        Objects.requireNonNull(readOnlyPropertyDescriptor2);
        this.listener = readOnlyPropertyDescriptor2.new ReadOnlyPropertyDescriptor.ReadOnlyListener<Number>(object, this);
        readOnlyPropertyDescriptor.addListener(this.listener);
        Disposer.addRecord(this, new DescriptorListenerCleaner(readOnlyPropertyDescriptor, this.listener));
    }

    @Override
    public long get() {
        return AccessController.doPrivileged(() -> {
            try {
                return ((Number)MethodHelper.invoke(this.descriptor.getGetter(), this.getBean(), null)).longValue();
            }
            catch (IllegalAccessException illegalAccessException) {
                throw new UndeclaredThrowableException(illegalAccessException);
            }
            catch (InvocationTargetException invocationTargetException) {
                throw new UndeclaredThrowableException(invocationTargetException);
            }
        }, this.acc);
    }

    @Override
    public Object getBean() {
        return this.listener.getBean();
    }

    @Override
    public String getName() {
        return this.descriptor.getName();
    }

    @Override
    public void fireValueChangedEvent() {
        super.fireValueChangedEvent();
    }

    @Override
    public void dispose() {
        this.descriptor.removeListener(this.listener);
    }
}

