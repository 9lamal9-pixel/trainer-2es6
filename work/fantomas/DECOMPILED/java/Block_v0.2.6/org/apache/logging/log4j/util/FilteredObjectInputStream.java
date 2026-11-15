/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class FilteredObjectInputStream
extends ObjectInputStream {
    private static final List<String> REQUIRED_JAVA_CLASSES = Arrays.asList("java.math.BigDecimal", "java.math.BigInteger", "java.rmi.MarshalledObject", "[B");
    private static final List<String> REQUIRED_JAVA_PACKAGES = Arrays.asList("java.lang.", "java.time", "java.util.", "org.apache.logging.log4j.", "[Lorg.apache.logging.log4j.");
    private final Collection<String> allowedClasses;

    public FilteredObjectInputStream() throws IOException, SecurityException {
        this.allowedClasses = new HashSet<String>();
    }

    public FilteredObjectInputStream(InputStream in) throws IOException {
        super(in);
        this.allowedClasses = new HashSet<String>();
    }

    public FilteredObjectInputStream(Collection<String> allowedClasses) throws IOException, SecurityException {
        this.allowedClasses = allowedClasses;
    }

    public FilteredObjectInputStream(InputStream in, Collection<String> allowedClasses) throws IOException {
        super(in);
        this.allowedClasses = allowedClasses;
    }

    public Collection<String> getAllowedClasses() {
        return this.allowedClasses;
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        String name = desc.getName();
        if (!FilteredObjectInputStream.isAllowedByDefault(name) && !this.allowedClasses.contains(name)) {
            throw new InvalidObjectException("Class is not allowed for deserialization: " + name);
        }
        return super.resolveClass(desc);
    }

    private static boolean isAllowedByDefault(String name) {
        return FilteredObjectInputStream.isRequiredPackage(name) || REQUIRED_JAVA_CLASSES.contains(name);
    }

    private static boolean isRequiredPackage(String name) {
        for (String packageName : REQUIRED_JAVA_PACKAGES) {
            if (!name.startsWith(packageName)) continue;
            return true;
        }
        return false;
    }
}

