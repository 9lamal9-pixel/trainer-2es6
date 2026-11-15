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
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.property.adapter.DescriptorListenerCleaner;
import javafx.beans.property.adapter.ReadOnlyJavaBeanProperty;

public final class ReadOnlyJavaBeanBooleanProperty
extends ReadOnlyBooleanPropertyBase
implements ReadOnlyJavaBeanProperty<Boolean> {
    private final ReadOnlyPropertyDescriptor descriptor;
    private final ReadOnlyPropertyDescriptor.ReadOnlyListener<Boolean> listener;
    private final AccessControlContext acc = AccessController.getContext();

    ReadOnlyJavaBeanBooleanProperty(ReadOnlyPropertyDescriptor readOnlyPropertyDescriptor, Object object) {
        this.descriptor = readOnlyPropertyDescriptor;
        ReadOnlyPropertyDescriptor readOnlyPropertyDescriptor2 = readOnlyPropertyDescriptor;
        Objects.requireNonNull(readOnlyPropertyDescriptor2);
        this.listener = readOnlyPropertyDescriptor2.new ReadOnlyPropertyDescriptor.ReadOnlyListener<Boolean>(object, this);
        readOnlyPropertyDescriptor.addListener(this.listener);
        Disposer.addRecord(this, new DescriptorListenerCleaner(readOnlyPropertyDescriptor, this.listener));
    }

    @Override
    public boolean get() {
        return AccessController.doPrivileged(() -> {
            try {
                return (Boolean)MethodHelper.invoke(this.descriptor.getGetter(), this.getBean(), null);
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

