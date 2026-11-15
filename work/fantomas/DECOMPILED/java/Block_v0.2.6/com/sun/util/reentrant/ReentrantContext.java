/*
 * Decompiled with CFR 0.152.
 */
package com.sun.util.reentrant;

import java.lang.ref.Reference;

public class ReentrantContext {
    byte usage = 0;
    Reference<? extends ReentrantContext> reference = null;
}

