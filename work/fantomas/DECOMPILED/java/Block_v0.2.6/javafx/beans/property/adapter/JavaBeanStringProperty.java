/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property.adapter;

import com.sun.javafx.binding.ExpressionHelper;
import com.sun.javafx.property.MethodHelper;
import com.sun.javafx.property.adapter.Disposer;
import com.sun.javafx.property.adapter.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.util.Objects;
import javafx.beans.InvalidationListener;
import javafx.beans.property.StringProperty;
import javafx.beans.property.adapter.DescriptorListenerCleaner;
import javafx.beans.property.adapter.JavaBeanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public final class JavaBeanStringProperty
extends StringProperty
implements JavaBeanProperty<String> {
    private final PropertyDescriptor descriptor;
    private final PropertyDescriptor.Listener<String> listener;
    private ObservableValue<? extends String> observable = null;
    private ExpressionHelper<String> helper = null;
    private final AccessControlContext acc = AccessController.getContext();

    JavaBeanStringProperty(PropertyDescriptor propertyDescriptor, Object object) {
        this.descriptor = propertyDescriptor;
        PropertyDescriptor propertyDescriptor2 = propertyDescriptor;
        Objects.requireNonNull(propertyDescriptor2);
        this.listener = propertyDescriptor2.new PropertyDescriptor.Listener<String>(object, this);
        propertyDescriptor.addListener(this.listener);
        Disposer.addRecord(this, new DescriptorListenerCleaner(propertyDescriptor, this.listener));
    }

    @Override
    public String get() {
        return AccessController.doPrivileged(() -> {
            try {
                return (String)MethodHelper.invoke(this.descriptor.getGetter(), this.getBean(), null);
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
    public void set(String string) {
        if (this.isBound()) {
            throw new RuntimeException("A bound value cannot be set.");
        }
        AccessController.doPrivileged(() -> {
            try {
                MethodHelper.invoke(this.descriptor.getSetter(), this.getBean(), new Object[]{string});
                ExpressionHelper.fireValueChangedEvent(this.helper);
            }
            catch (IllegalAccessException illegalAccessException) {
                throw new UndeclaredThrowableException(illegalAccessException);
            }
            catch (InvocationTargetException invocationTargetException) {
                throw new UndeclaredThrowableException(invocationTargetException);
            }
            return null;
        }, this.acc);
    }

    @Override
    public void bind(ObservableValue<? extends String> observableValue) {
        if (observableValue == null) {
            throw new NullPointerException("Cannot bind to null");
        }
        if (!observableValue.equals(this.observable)) {
            this.unbind();
            this.set(observableValue.getValue());
            this.observable = observableValue;
            this.observable.addListener(this.listener);
        }
    }

    @Override
    public void unbind() {
        if (this.observable != null) {
            this.observable.removeListener(this.listener);
            this.observable = null;
        }
    }

    @Override
    public boolean isBound() {
        return this.observable != null;
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
    public void addListener(ChangeListener<? super String> changeListener) {
        this.helper = ExpressionHelper.addListener(this.helper, this, changeListener);
    }

    @Override
    public void removeListener(ChangeListener<? super String> changeListener) {
        this.helper = ExpressionHelper.removeListener(this.helper, changeListener);
    }

    @Override
    public void addListener(InvalidationListener invalidationListener) {
        this.helper = ExpressionHelper.addListener(this.helper, this, invalidationListener);
    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {
        this.helper = ExpressionHelper.removeListener(this.helper, invalidationListener);
    }

    @Override
    public void fireValueChangedEvent() {
        ExpressionHelper.fireValueChangedEvent(this.helper);
    }

    @Override
    public void dispose() {
        this.descriptor.removeListener(this.listener);
    }
}

