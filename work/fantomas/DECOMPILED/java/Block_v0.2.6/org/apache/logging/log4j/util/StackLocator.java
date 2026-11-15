/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.util.StackLocator$FqcnCallerLocator
 */
package org.apache.logging.log4j.util;

import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;
import org.apache.logging.log4j.util.PrivateSecurityManagerStackTraceUtil;
import org.apache.logging.log4j.util.StackLocator;

public class StackLocator {
    private static final StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    private static final StackWalker stackWalker = StackWalker.getInstance();
    private static final StackLocator INSTANCE = new StackLocator();
    private static final ThreadLocal<String> FQCN = new ThreadLocal();
    private static final FqcnCallerLocator LOCATOR = new FqcnCallerLocator();

    public static StackLocator getInstance() {
        return INSTANCE;
    }

    private StackLocator() {
    }

    public Class<?> getCallerClass(String fqcn) {
        return this.getCallerClass(fqcn, "");
    }

    public Class<?> getCallerClass(String fqcn, String pkg) {
        return walker.walk(s -> s.dropWhile(f -> !f.getClassName().equals(fqcn)).dropWhile(f -> f.getClassName().equals(fqcn)).dropWhile(f -> !f.getClassName().startsWith(pkg)).findFirst()).map(StackWalker.StackFrame::getDeclaringClass).orElse(null);
    }

    public Class<?> getCallerClass(Class<?> anchor) {
        return walker.walk(s -> s.dropWhile(f -> !f.getDeclaringClass().equals(anchor)).dropWhile(f -> f.getDeclaringClass().equals(anchor)).findFirst()).map(StackWalker.StackFrame::getDeclaringClass).orElse(null);
    }

    public Class<?> getCallerClass(int depth) {
        return walker.walk(s -> s.skip(depth).findFirst()).map(StackWalker.StackFrame::getDeclaringClass).orElse(null);
    }

    public Stack<Class<?>> getCurrentStackTrace() {
        if (PrivateSecurityManagerStackTraceUtil.isEnabled()) {
            return PrivateSecurityManagerStackTraceUtil.getCurrentStackTrace();
        }
        Stack stack = new Stack();
        List classes = walker.walk(s -> s.map(f -> f.getDeclaringClass()).collect(Collectors.toList()));
        stack.addAll(classes);
        return stack;
    }

    public StackTraceElement calcLocation(String fqcnOfLogger) {
        FQCN.set(fqcnOfLogger);
        StackTraceElement element = ((StackWalker.StackFrame)walker.walk(LOCATOR)).toStackTraceElement();
        FQCN.set(null);
        return element;
    }

    public StackTraceElement getStackTraceElement(int depth) {
        return ((StackWalker.StackFrame)stackWalker.walk(s -> s.skip(depth).findFirst()).get()).toStackTraceElement();
    }

    static /* synthetic */ ThreadLocal access$000() {
        return FQCN;
    }
}

