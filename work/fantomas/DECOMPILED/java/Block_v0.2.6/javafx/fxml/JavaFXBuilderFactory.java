/*
 * Decompiled with CFR 0.152.
 */
package javafx.fxml;

import com.sun.javafx.fxml.BeanAdapter;
import com.sun.javafx.fxml.builder.JavaFXFontBuilder;
import com.sun.javafx.fxml.builder.JavaFXImageBuilder;
import com.sun.javafx.fxml.builder.JavaFXSceneBuilder;
import com.sun.javafx.fxml.builder.ProxyBuilder;
import com.sun.javafx.fxml.builder.TriangleMeshBuilder;
import com.sun.javafx.fxml.builder.URLBuilder;
import com.sun.javafx.logging.PlatformLogger;
import com.sun.javafx.reflect.ConstructorUtil;
import com.sun.javafx.reflect.MethodUtil;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.text.Font;
import javafx.util.Builder;
import javafx.util.BuilderFactory;

public final class JavaFXBuilderFactory
implements BuilderFactory {
    private final ClassLoader classLoader;
    private final boolean webSupported;
    private static final String WEBVIEW_NAME = "javafx.scene.web.WebView";
    private static final String WEBVIEW_BUILDER_NAME = "com.sun.javafx.fxml.builder.web.WebViewBuilder";

    public JavaFXBuilderFactory() {
        this(FXMLLoader.getDefaultClassLoader());
    }

    public JavaFXBuilderFactory(ClassLoader classLoader) {
        if (classLoader == null) {
            throw new NullPointerException();
        }
        this.classLoader = classLoader;
        this.webSupported = Platform.isSupported(ConditionalFeature.WEB);
    }

    @Override
    public Builder<?> getBuilder(Class<?> clazz) {
        Builder<Scene> builder;
        if (clazz == null) {
            throw new NullPointerException();
        }
        if (clazz == Scene.class) {
            builder = new JavaFXSceneBuilder();
        } else if (clazz == Font.class) {
            builder = new JavaFXFontBuilder();
        } else if (clazz == Image.class) {
            builder = new JavaFXImageBuilder();
        } else if (clazz == URL.class) {
            builder = new URLBuilder(this.classLoader);
        } else if (clazz == TriangleMesh.class) {
            builder = new TriangleMeshBuilder();
        } else if (this.webSupported && clazz.getName().equals(WEBVIEW_NAME)) {
            try {
                Class<?> clazz2 = this.classLoader.loadClass(WEBVIEW_BUILDER_NAME);
                ObjectBuilderWrapper objectBuilderWrapper = new ObjectBuilderWrapper(clazz2);
                builder = objectBuilderWrapper.createBuilder();
            }
            catch (Exception exception) {
                builder = null;
            }
        } else {
            builder = this.scanForConstructorAnnotations(clazz) ? new ProxyBuilder(clazz) : null;
        }
        return builder;
    }

    private boolean scanForConstructorAnnotations(Class<?> clazz) {
        Constructor<?>[] constructorArray;
        for (Constructor<?> constructor : constructorArray = ConstructorUtil.getConstructors(clazz)) {
            Annotation[][] annotationArray = constructor.getParameterAnnotations();
            for (int i = 0; i < constructor.getParameterTypes().length; ++i) {
                for (Annotation annotation : annotationArray[i]) {
                    if (!(annotation instanceof NamedArg)) continue;
                    return true;
                }
            }
        }
        return false;
    }

    private static final class ObjectBuilderWrapper {
        private static final Object[] NO_ARGS = new Object[0];
        private static final Class<?>[] NO_SIG = new Class[0];
        private final Class<?> builderClass;
        private final Method createMethod;
        private final Method buildMethod;
        private final Map<String, Method> methods = new HashMap<String, Method>();
        private final Map<String, Method> getters = new HashMap<String, Method>();
        private final Map<String, Method> setters = new HashMap<String, Method>();

        ObjectBuilderWrapper() {
            this.builderClass = null;
            this.createMethod = null;
            this.buildMethod = null;
        }

        ObjectBuilderWrapper(Class<?> clazz) throws NoSuchMethodException, InstantiationException, IllegalAccessException {
            this.builderClass = clazz;
            this.createMethod = MethodUtil.getMethod(clazz, "create", NO_SIG);
            this.buildMethod = MethodUtil.getMethod(clazz, "build", NO_SIG);
            assert (Modifier.isStatic(this.createMethod.getModifiers()));
            assert (!Modifier.isStatic(this.buildMethod.getModifiers()));
        }

        Builder<Object> createBuilder() {
            return new ObjectBuilder();
        }

        private Method findMethod(String object) {
            if (((String)object).length() > 1 && Character.isUpperCase(((String)object).charAt(1))) {
                object = Character.toUpperCase(((String)object).charAt(0)) + ((String)object).substring(1);
            }
            for (Method method : MethodUtil.getMethods(this.builderClass)) {
                if (!method.getName().equals(object)) continue;
                return method;
            }
            throw new IllegalArgumentException("Method " + (String)object + " could not be found at class " + this.builderClass.getName());
        }

        public Class<?> getTargetClass() {
            return this.buildMethod.getReturnType();
        }

        final class ObjectBuilder
        extends AbstractMap<String, Object>
        implements Builder<Object> {
            private final Map<String, Object> containers = new HashMap<String, Object>();
            private Object builder = null;
            private Map<Object, Object> properties;

            private ObjectBuilder() {
                try {
                    this.builder = ObjectBuilderWrapper.this.createMethod.invoke(null, NO_ARGS);
                }
                catch (Exception exception) {
                    throw new RuntimeException("Creation of the builder " + ObjectBuilderWrapper.this.builderClass.getName() + " failed.", exception);
                }
            }

            @Override
            public Object build() {
                Object object;
                for (Map.Entry<String, Object> entry : this.containers.entrySet()) {
                    this.put(entry.getKey(), entry.getValue());
                }
                try {
                    object = ObjectBuilderWrapper.this.buildMethod.invoke(this.builder, NO_ARGS);
                    if (this.properties != null && object instanceof Node) {
                        ((Node)object).getProperties().putAll(this.properties);
                    }
                }
                catch (InvocationTargetException invocationTargetException) {
                    throw new RuntimeException(invocationTargetException);
                }
                catch (IllegalAccessException illegalAccessException) {
                    throw new RuntimeException(illegalAccessException);
                }
                finally {
                    this.builder = null;
                }
                return object;
            }

            @Override
            public int size() {
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
            public Object put(String string, Object object) {
                if (Node.class.isAssignableFrom(ObjectBuilderWrapper.this.getTargetClass()) && "properties".equals(string)) {
                    this.properties = (Map)object;
                    return null;
                }
                try {
                    Method method = (Method)ObjectBuilderWrapper.this.methods.get(string);
                    if (method == null) {
                        method = ObjectBuilderWrapper.this.findMethod(string);
                        ObjectBuilderWrapper.this.methods.put(string, method);
                    }
                    try {
                        Class<?> clazz = method.getParameterTypes()[0];
                        if (clazz.isArray()) {
                            List<String> list = object instanceof List ? (List<String>)object : Arrays.asList(object.toString().split(","));
                            Class<?> clazz2 = clazz.getComponentType();
                            Object object2 = Array.newInstance(clazz2, list.size());
                            for (int i = 0; i < list.size(); ++i) {
                                Array.set(object2, i, BeanAdapter.coerce(list.get(i), clazz2));
                            }
                            object = object2;
                        }
                        method.invoke(this.builder, BeanAdapter.coerce(object, clazz));
                    }
                    catch (Exception exception) {
                        String string2 = "Method " + method.getName() + " failed";
                        PlatformLogger.getLogger(ObjectBuilderWrapper.class.getName()).warning(string2, exception);
                    }
                    return null;
                }
                catch (Exception exception) {
                    String string3 = "Failed to set " + ObjectBuilderWrapper.this.getTargetClass() + "." + string + " using " + ObjectBuilderWrapper.this.builderClass;
                    PlatformLogger.getLogger(ObjectBuilderWrapper.class.getName()).warning(string3, exception);
                    return null;
                }
            }

            Object getReadOnlyProperty(String string) {
                GenericDeclaration genericDeclaration;
                Class<?> clazz;
                if (ObjectBuilderWrapper.this.setters.get(string) != null) {
                    return null;
                }
                Method method = (Method)ObjectBuilderWrapper.this.getters.get(string);
                if (method == null) {
                    clazz = null;
                    genericDeclaration = ObjectBuilderWrapper.this.getTargetClass();
                    String string2 = Character.toUpperCase(string.charAt(0)) + string.substring(1);
                    try {
                        method = MethodUtil.getMethod(genericDeclaration, "get" + string2, NO_SIG);
                        clazz = MethodUtil.getMethod(genericDeclaration, "set" + string2, new Class[]{method.getReturnType()});
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    if (method != null) {
                        ObjectBuilderWrapper.this.getters.put(string, method);
                        ObjectBuilderWrapper.this.setters.put(string, clazz);
                    }
                    if (clazz != null) {
                        return null;
                    }
                }
                if (method == null) {
                    genericDeclaration = ObjectBuilderWrapper.this.findMethod(string);
                    if (genericDeclaration == null) {
                        return null;
                    }
                    clazz = ((Method)genericDeclaration).getParameterTypes()[0];
                    if (clazz.isArray()) {
                        clazz = List.class;
                    }
                } else {
                    clazz = method.getReturnType();
                }
                if (ObservableMap.class.isAssignableFrom(clazz)) {
                    return FXCollections.observableMap(new HashMap());
                }
                if (Map.class.isAssignableFrom(clazz)) {
                    return new HashMap();
                }
                if (ObservableList.class.isAssignableFrom(clazz)) {
                    return FXCollections.observableArrayList();
                }
                if (List.class.isAssignableFrom(clazz)) {
                    return new ArrayList();
                }
                if (Set.class.isAssignableFrom(clazz)) {
                    return new HashSet();
                }
                return null;
            }

            public Object getTemporaryContainer(String string) {
                Object object = this.containers.get(string);
                if (object == null && (object = this.getReadOnlyProperty(string)) != null) {
                    this.containers.put(string, object);
                }
                return object;
            }

            @Override
            public Object remove(Object object) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void putAll(Map<? extends String, ? extends Object> map) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void clear() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Set<String> keySet() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Collection<Object> values() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Set<Map.Entry<String, Object>> entrySet() {
                throw new UnsupportedOperationException();
            }
        }
    }
}

