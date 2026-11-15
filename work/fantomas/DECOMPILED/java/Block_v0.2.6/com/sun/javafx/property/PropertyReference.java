/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.property;

import com.sun.javafx.property.MethodHelper;
import com.sun.javafx.reflect.ReflectUtil;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javafx.beans.property.ReadOnlyProperty;

public final class PropertyReference<T> {
    private String name;
    private Method getter;
    private Method setter;
    private Method propertyGetter;
    private Class<?> clazz;
    private Class<?> type;
    private boolean reflected = false;

    public PropertyReference(Class<?> clazz, String string) {
        if (string == null) {
            throw new NullPointerException("Name must be specified");
        }
        if (string.trim().length() == 0) {
            throw new IllegalArgumentException("Name must be specified");
        }
        if (clazz == null) {
            throw new NullPointerException("Class must be specified");
        }
        ReflectUtil.checkPackageAccess(clazz);
        this.name = string;
        this.clazz = clazz;
    }

    public boolean isWritable() {
        this.reflect();
        return this.setter != null;
    }

    public boolean isReadable() {
        this.reflect();
        return this.getter != null;
    }

    public boolean hasProperty() {
        this.reflect();
        return this.propertyGetter != null;
    }

    public String getName() {
        return this.name;
    }

    public Class<?> getContainingClass() {
        return this.clazz;
    }

    public Class<?> getType() {
        this.reflect();
        return this.type;
    }

    public void set(Object object, T t) {
        if (!this.isWritable()) {
            throw new IllegalStateException("Cannot write to readonly property " + this.name);
        }
        assert (this.setter != null);
        try {
            MethodHelper.invoke(this.setter, object, new Object[]{t});
        }
        catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public T get(Object object) {
        if (!this.isReadable()) {
            throw new IllegalStateException("Cannot read from unreadable property " + this.name);
        }
        assert (this.getter != null);
        try {
            return (T)MethodHelper.invoke(this.getter, object, null);
        }
        catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public ReadOnlyProperty<T> getProperty(Object object) {
        if (!this.hasProperty()) {
            throw new IllegalStateException("Cannot get property " + this.name);
        }
        assert (this.propertyGetter != null);
        try {
            return (ReadOnlyProperty)MethodHelper.invoke(this.propertyGetter, object, null);
        }
        catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public String toString() {
        return this.name;
    }

    private void reflect() {
        block19: {
            if (!this.reflected) {
                this.reflected = true;
                try {
                    Object object;
                    Object object2;
                    String string = this.name.length() == 1 ? this.name.substring(0, 1).toUpperCase() : Character.toUpperCase(this.name.charAt(0)) + this.name.substring(1);
                    this.type = null;
                    String string2 = "get" + string;
                    try {
                        object2 = this.clazz.getMethod(string2, new Class[0]);
                        if (Modifier.isPublic(((Method)object2).getModifiers())) {
                            this.getter = object2;
                        }
                    }
                    catch (NoSuchMethodException noSuchMethodException) {
                        // empty catch block
                    }
                    if (this.getter == null) {
                        string2 = "is" + string;
                        try {
                            object2 = this.clazz.getMethod(string2, new Class[0]);
                            if (Modifier.isPublic(((Method)object2).getModifiers())) {
                                this.getter = object2;
                            }
                        }
                        catch (NoSuchMethodException noSuchMethodException) {
                            // empty catch block
                        }
                    }
                    object2 = "set" + string;
                    if (this.getter != null) {
                        this.type = this.getter.getReturnType();
                        try {
                            object = this.clazz.getMethod((String)object2, this.type);
                            if (Modifier.isPublic(((Method)object).getModifiers())) {
                                this.setter = object;
                            }
                        }
                        catch (NoSuchMethodException noSuchMethodException) {}
                    } else {
                        for (Method method : object = this.clazz.getMethods()) {
                            Class<?>[] classArray = method.getParameterTypes();
                            if (!((String)object2).equals(method.getName()) || classArray.length != 1 || !Modifier.isPublic(method.getModifiers())) continue;
                            this.setter = method;
                            this.type = classArray[0];
                            break;
                        }
                    }
                    object = this.name + "Property";
                    try {
                        Method method = this.clazz.getMethod((String)object, new Class[0]);
                        if (Modifier.isPublic(method.getModifiers())) {
                            this.propertyGetter = method;
                            break block19;
                        }
                        this.propertyGetter = null;
                    }
                    catch (NoSuchMethodException noSuchMethodException) {}
                }
                catch (RuntimeException runtimeException) {
                    System.err.println("Failed to introspect property " + this.name);
                }
            }
        }
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof PropertyReference)) {
            return false;
        }
        PropertyReference propertyReference = (PropertyReference)object;
        if (!(this.name == propertyReference.name || this.name != null && this.name.equals(propertyReference.name))) {
            return false;
        }
        return this.clazz == propertyReference.clazz || this.clazz != null && this.clazz.equals(propertyReference.clazz);
    }

    public int hashCode() {
        int n = 5;
        n = 97 * n + (this.name != null ? this.name.hashCode() : 0);
        n = 97 * n + (this.clazz != null ? this.clazz.hashCode() : 0);
        return n;
    }
}

