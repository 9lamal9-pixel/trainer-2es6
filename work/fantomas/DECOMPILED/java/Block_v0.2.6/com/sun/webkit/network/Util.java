/*
 * Decompiled with CFR 0.152.
 */
package com.sun.webkit.network;

import com.sun.webkit.network.URLs;
import java.net.MalformedURLException;

public final class Util {
    private Util() {
        throw new AssertionError();
    }

    public static String adjustUrlForWebKit(String object) throws MalformedURLException {
        int n;
        if (URLs.newURL((String)object).getProtocol().equals("file") && (n = "file:".length()) < ((String)object).length() && ((String)object).charAt(n) != '/') {
            object = ((String)object).substring(0, n) + "///" + ((String)object).substring(n);
        }
        return object;
    }

    static String formatHeaders(String string) {
        return string.trim().replaceAll("(?m)^", "    ");
    }
}

