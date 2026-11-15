/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import java.util.Base64;

public final class Base64Util {
    private static final Base64.Encoder encoder = Base64.getEncoder();

    private Base64Util() {
    }

    public static String encode(String str) {
        return str != null ? ((Base64.Encoder)encoder).encodeToString(str.getBytes()) : null;
    }
}

