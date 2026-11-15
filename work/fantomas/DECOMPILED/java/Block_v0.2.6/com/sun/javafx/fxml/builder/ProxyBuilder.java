/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.fxml.builder;

import com.sun.javafx.fxml.BeanAdapter;
import com.sun.javafx.fxml.ModuleHelper;
import com.sun.javafx.reflect.ConstructorUtil;
import com.sun.javafx.reflect.ReflectUtil;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javafx.beans.NamedArg;
import javafx.util.Builder;

public class ProxyBuilder<T>
extends AbstractMap<String, Object>
implements Builder<T> {
    private Class<?> type;
    private final Map<Constructor, Map<String, AnnotationValue>> constructorsMap;
    private final Map<String, Property> propertiesMap;
    private final Set<Constructor> constructors;
    private Set<String> propertyNames;
    private boolean hasDefaultConstructor = false;
    private Constructor defaultConstructor;
    private static final String SETTER_PREFIX = "set";
    private static final String GETTER_PREFIX = "get";
    private final Comparator<Constructor> constructorComparator = (constructor, constructor2) -> {
        int n = constructor.getParameterCount();
        int n2 = constructor2.getParameterCount();
        int n3 = Math.min(n, n2);
        for (int i = 0; i < n3; ++i) {
            Class<?> clazz;
            Class<?> clazz2 = constructor.getParameterTypes()[i];
            if (clazz2.equals(clazz = constructor2.getParameterTypes()[i])) continue;
            if (clazz2.equals(Integer.TYPE) && clazz.equals(Double.TYPE)) {
                return -1;
            }
            if (clazz2.equals(Double.TYPE) && clazz.equals(Integer.TYPE)) {
                return 1;
            }
            return clazz2.getCanonicalName().compareTo(clazz.getCanonicalName());
        }
        return n - n2;
    };
    private final Map<String, Object> userValues = new HashMap<String, Object>();
    private final Map<String, Object> containers = new HashMap<String, Object>();
    private static final Map<Class<?>, Object> defaultsMap = new HashMap();

    public ProxyBuilder(Class<?> clazz) {
        Constructor<?>[] constructorArray;
        this.type = clazz;
        this.constructorsMap = new HashMap<Constructor, Map<String, AnnotationValue>>();
        for (Constructor<?> constructor3 : constructorArray = ConstructorUtil.getConstructors(this.type)) {
            Class<?>[] classArray = constructor3.getParameterTypes();
            Annotation[][] annotationArray = constructor3.getParameterAnnotations();
            if (classArray.length == 0) {
                this.hasDefaultConstructor = true;
                this.defaultConstructor = constructor3;
                continue;
            }
            int n = 0;
            boolean bl = true;
            LinkedHashMap<String, AnnotationValue> linkedHashMap = new LinkedHashMap<String, AnnotationValue>();
            for (Class<?> clazz2 : classArray) {
                NamedArg namedArg = null;
                for (Annotation annotation : annotationArray[n]) {
                    if (!(annotation instanceof NamedArg)) continue;
                    namedArg = (NamedArg)annotation;
                    break;
                }
                if (namedArg == null) {
                    bl = false;
                    break;
                }
                AnnotationValue annotationValue = new AnnotationValue(namedArg.value(), namedArg.defaultValue(), clazz2);
                linkedHashMap.put(namedArg.value(), annotationValue);
                ++n;
            }
            if (!bl) continue;
            this.constructorsMap.put(constructor3, linkedHashMap);
        }
        if (!this.hasDefaultConstructor && this.constructorsMap.isEmpty()) {
            throw new RuntimeException("Cannot create instance of " + this.type.getCanonicalName() + " the constructor is not properly annotated.");
        }
        this.constructors = new TreeSet<Constructor>(this.constructorComparator);
        this.constructors.addAll(this.constructorsMap.keySet());
        this.propertiesMap = this.scanForSetters();
    }

    @Override
    public Object put(String string, Object object) {
        this.userValues.put(string, object);
        return null;
    }

    private Object getTemporaryContainer(String string) {
        Object object = this.containers.get(string);
        if (object == null && (object = this.getReadOnlyProperty(string)) != null) {
            this.containers.put(string, object);
        }
        return object;
    }

    private Object getReadOnlyProperty(String string) {
        return new ArrayListWrapper();
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Map.Entry<String, Object>> entrySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsKey(Object object) {
        return this.getTemporaryContainer(object.toString()) != null;
    }

    @Override
    public boolean containsValue(Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object get(Object object) {
        return this.getTemporaryContainer(object.toString());
    }

    @Override
    public T build() {
        Set<Object> set;
        Object object2 = null;
        for (Map.Entry<String, Object> object3 : this.containers.entrySet()) {
            this.put(object3.getKey(), object3.getValue());
        }
        this.propertyNames = this.userValues.keySet();
        for (Constructor constructor : this.constructors) {
            set = this.getArgumentNames(constructor);
            if (!this.propertyNames.equals(set) || (object2 = this.createObjectWithExactArguments(constructor, set)) == null) continue;
            return (T)object2;
        }
        Set<String> set2 = this.propertiesMap.keySet();
        if (set2.containsAll(this.propertyNames) && this.hasDefaultConstructor && (object2 = this.createObjectFromDefaultConstructor()) != null) {
            return (T)object2;
        }
        HashSet<String> hashSet = new HashSet<String>(this.propertyNames);
        hashSet.retainAll(set2);
        set = this.chooseBestConstructors(set2);
        for (Constructor constructor : set) {
            object2 = this.createObjectFromConstructor(constructor, hashSet);
            if (object2 == null) continue;
            return (T)object2;
        }
        if (object2 == null) {
            throw new RuntimeException("Cannot create instance of " + this.type.getCanonicalName() + " with given set of properties: " + this.userValues.keySet().toString());
        }
        return (T)object2;
    }

    private Set<Constructor> chooseBestConstructors(Set<String> set) {
        HashSet<String> hashSet = new HashSet<String>(this.propertyNames);
        hashSet.removeAll(set);
        HashSet<String> hashSet2 = new HashSet<String>(this.propertyNames);
        hashSet2.retainAll(set);
        int n = Integer.MAX_VALUE;
        int n2 = Integer.MAX_VALUE;
        TreeSet<Constructor> treeSet = new TreeSet<Constructor>(this.constructorComparator);
        Set set2 = null;
        for (Constructor constructor : this.constructors) {
            Set<String> set3 = this.getArgumentNames(constructor);
            if (!set3.containsAll(hashSet)) continue;
            HashSet<String> hashSet3 = new HashSet<String>(set3);
            hashSet3.removeAll(this.propertyNames);
            HashSet<String> hashSet4 = new HashSet<String>(hashSet2);
            hashSet4.removeAll(set3);
            int n3 = hashSet3.size();
            if (n == n3 && n2 == hashSet4.size()) {
                treeSet.add(constructor);
            }
            if (n <= n3 && (n != n3 || n2 <= hashSet4.size())) continue;
            n = n3;
            n2 = hashSet4.size();
            treeSet.clear();
            treeSet.add(constructor);
        }
        if (set2 != null && !set2.isEmpty()) {
            throw new RuntimeException("Cannot create instance of " + this.type.getCanonicalName() + " no constructor contains all properties specified in FXML.");
        }
        return treeSet;
    }

    private Set<String> getArgumentNames(Constructor constructor) {
        Map<String, AnnotationValue> map = this.constructorsMap.get(constructor);
        Set<String> set = null;
        if (map != null) {
            set = map.keySet();
        }
        return set;
    }

    private Object createObjectFromDefaultConstructor() throws RuntimeException {
        Object object = null;
        try {
            object = this.createInstance(this.defaultConstructor, new Object[0]);
        }
        catch (Exception exception) {
            throw new RuntimeException(exception);
        }
        for (String string : this.propertyNames) {
            try {
                Property property = this.propertiesMap.get(string);
                property.invoke(object, this.getUserValue(string, property.getType()));
            }
            catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }
        return object;
    }

    private Object createObjectFromConstructor(Constructor constructor, Set<String> set) {
        Object object;
        Object object2 = null;
        Map<String, AnnotationValue> map = this.constructorsMap.get(constructor);
        Object[] objectArray = new Object[map.size()];
        int n = 0;
        HashSet<String> hashSet = new HashSet<String>(set);
        for (AnnotationValue object3 : map.values()) {
            object = this.getUserValue(object3.getName(), object3.getType());
            if (object != null) {
                try {
                    objectArray[n] = BeanAdapter.coerce(object, object3.getType());
                }
                catch (Exception exception) {
                    return null;
                }
            } else if (!object3.getDefaultValue().isEmpty()) {
                try {
                    objectArray[n] = BeanAdapter.coerce(object3.getDefaultValue(), object3.getType());
                }
                catch (Exception exception) {
                    return null;
                }
            } else {
                objectArray[n] = ProxyBuilder.getDefaultValue(object3.getType());
            }
            hashSet.remove(object3.getName());
            ++n;
        }
        try {
            object2 = this.createInstance(constructor, objectArray);
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (object2 != null) {
            for (String string : hashSet) {
                try {
                    object = this.propertiesMap.get(string);
                    ((Property)object).invoke(object2, this.getUserValue(string, ((Property)object).getType()));
                }
                catch (Exception exception) {
                    return null;
                }
            }
        }
        return object2;
    }

    private Object getUserValue(String string, Class<?> clazz) {
        Object object = this.userValues.get(string);
        if (object == null) {
            return null;
        }
        if (clazz.isAssignableFrom(object.getClass())) {
            return object;
        }
        if (clazz.isArray()) {
            try {
                return ProxyBuilder.convertListToArray(object, clazz);
            }
            catch (RuntimeException runtimeException) {
                // empty catch block
            }
        }
        if (ArrayListWrapper.class.equals(object.getClass())) {
            List list = (List)object;
            return list.get(0);
        }
        return object;
    }

    private Object createObjectWithExactArguments(Constructor constructor, Set<String> set) {
        Object object = null;
        Object[] objectArray = new Object[set.size()];
        Map<String, AnnotationValue> map = this.constructorsMap.get(constructor);
        int n = 0;
        for (String string : set) {
            Class<?> clazz = map.get(string).getType();
            Object object2 = this.getUserValue(string, clazz);
            try {
                objectArray[n++] = BeanAdapter.coerce(object2, clazz);
            }
            catch (Exception exception) {
                return null;
            }
        }
        try {
            object = this.createInstance(constructor, objectArray);
        }
        catch (Exception exception) {
            // empty catch block
        }
        return object;
    }

    private Object createInstance(Constructor constructor, Object[] objectArray) throws Exception {
        Object var3_3 = null;
        ReflectUtil.checkPackageAccess(this.type);
        var3_3 = constructor.newInstance(objectArray);
        return var3_3;
    }

    private Map<String, Property> scanForSetters() {
        HashMap<String, Property> hashMap = new HashMap<String, Property>();
        HashMap<String, LinkedList<Method>> hashMap2 = ProxyBuilder.getClassMethodCache(this.type);
        for (String string : hashMap2.keySet()) {
            Class<?>[] classArray;
            Class<?> clazz;
            List list;
            Object object;
            if (string.startsWith(SETTER_PREFIX) && string.length() > SETTER_PREFIX.length()) {
                object = string.substring(SETTER_PREFIX.length());
                object = Character.toLowerCase(((String)object).charAt(0)) + ((String)object).substring(1);
                list = (List)hashMap2.get(string);
                for (Method method : list) {
                    clazz = method.getReturnType();
                    classArray = method.getParameterTypes();
                    if (!clazz.equals(Void.TYPE) || classArray.length != 1) continue;
                    hashMap.put((String)object, new Setter(method, classArray[0]));
                }
            }
            if (!string.startsWith(GETTER_PREFIX) || string.length() <= GETTER_PREFIX.length()) continue;
            object = string.substring(GETTER_PREFIX.length());
            object = Character.toLowerCase(((String)object).charAt(0)) + ((String)object).substring(1);
            list = (List)hashMap2.get(string);
            for (Method method : list) {
                clazz = method.getReturnType();
                classArray = method.getParameterTypes();
                if (!Collection.class.isAssignableFrom(clazz) || classArray.length != 0) continue;
                hashMap.put((String)object, new Getter(method, clazz));
            }
        }
        return hashMap;
    }

    private static HashMap<String, LinkedList<Method>> getClassMethodCache(Class<?> clazz) {
        Method[] methodArray;
        HashMap<String, LinkedList<Method>> hashMap = new HashMap<String, LinkedList<Method>>();
        ReflectUtil.checkPackageAccess(clazz);
        for (Method method : methodArray = clazz.getMethods()) {
            int n = method.getModifiers();
            if (!Modifier.isPublic(n) || Modifier.isStatic(n)) continue;
            String string = method.getName();
            LinkedList<Method> linkedList = hashMap.get(string);
            if (linkedList == null) {
                linkedList = new LinkedList();
                hashMap.put(string, linkedList);
            }
            linkedList.add(method);
        }
        return hashMap;
    }

    private static Object[] convertListToArray(Object object, Class<?> clazz) {
        Class<?> clazz2 = clazz.getComponentType();
        List list = BeanAdapter.coerce(object, List.class);
        return list.toArray((Object[])Array.newInstance(clazz2, 0));
    }

    private static Object getDefaultValue(Class clazz) {
        return defaultsMap.get(clazz);
    }

    static {
        defaultsMap.put(Byte.TYPE, (byte)0);
        defaultsMap.put(Short.TYPE, (short)0);
        defaultsMap.put(Integer.TYPE, 0);
        defaultsMap.put(Long.TYPE, 0L);
        defaultsMap.put(Integer.TYPE, 0);
        defaultsMap.put(Float.TYPE, Float.valueOf(0.0f));
        defaultsMap.put(Double.TYPE, 0.0);
        defaultsMap.put(Character.TYPE, Character.valueOf('\u0000'));
        defaultsMap.put(Boolean.TYPE, false);
        defaultsMap.put(Object.class, null);
    }

    private static class AnnotationValue {
        private final String name;
        private final String defaultValue;
        private final Class<?> type;

        public AnnotationValue(String string, String string2, Class<?> clazz) {
            this.name = string;
            this.defaultValue = string2;
            this.type = clazz;
        }

        public String getName() {
            return this.name;
        }

        public String getDefaultValue() {
            return this.defaultValue;
        }

        public Class<?> getType() {
            return this.type;
        }
    }

    private static class Getter
    extends Property {
        public Getter(Method method, Class<?> clazz) {
            super(method, clazz);
        }

        @Override
        public void invoke(Object object, Object object2) throws Exception {
            Collection collection = (Collection)ModuleHelper.invoke(this.method, object, new Object[0]);
            if (object2 instanceof Collection) {
                Collection collection2 = (Collection)object2;
                collection.addAll(collection2);
            } else {
                collection.add(object2);
            }
        }
    }

    private static class Setter
    extends Property {
        public Setter(Method method, Class<?> clazz) {
            super(method, clazz);
        }

        @Override
        public void invoke(Object object, Object object2) throws Exception {
            Object[] objectArray = new Object[]{BeanAdapter.coerce(object2, this.type)};
            ModuleHelper.invoke(this.method, object, objectArray);
        }
    }

    private static abstract class Property {
        protected final Method method;
        protected final Class<?> type;

        public Property(Method method, Class<?> clazz) {
            this.method = method;
            this.type = clazz;
        }

        public Class<?> getType() {
            return this.type;
        }

        public abstract void invoke(Object var1, Object var2) throws Exception;
    }

    private static class ArrayListWrapper<T>
    extends ArrayList<T> {
        private ArrayListWrapper() {
        }
    }
}

