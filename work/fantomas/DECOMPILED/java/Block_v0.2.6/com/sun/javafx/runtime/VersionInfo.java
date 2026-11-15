/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.runtime;

public class VersionInfo {
    private static final String BUILD_TIMESTAMP = "2019-01-16-101123";
    private static final String HUDSON_JOB_NAME = ".";
    private static final String HUDSON_BUILD_NUMBER = "10";
    private static final String PROMOTED_BUILD_NUMBER = "1";
    private static final String RELEASE_VERSION = "11.0.2";
    private static final String RELEASE_SUFFIX = "";
    private static final String VERSION;
    private static final String RUNTIME_VERSION;

    public static synchronized void setupSystemProperties() {
        if (System.getProperty("javafx.version") == null) {
            System.setProperty("javafx.version", VersionInfo.getVersion());
            System.setProperty("javafx.runtime.version", VersionInfo.getRuntimeVersion());
        }
    }

    public static String getBuildTimestamp() {
        return BUILD_TIMESTAMP;
    }

    public static String getHudsonJobName() {
        if (HUDSON_JOB_NAME.equals("not_hudson")) {
            return RELEASE_SUFFIX;
        }
        return HUDSON_JOB_NAME;
    }

    public static String getHudsonBuildNumber() {
        return HUDSON_BUILD_NUMBER;
    }

    public static String getReleaseMilestone() {
        String string = RELEASE_SUFFIX;
        if (string.startsWith("-")) {
            string = string.substring(1);
        }
        return string;
    }

    public static String getVersion() {
        return VERSION;
    }

    public static String getRuntimeVersion() {
        return RUNTIME_VERSION;
    }

    static {
        Object object = RELEASE_VERSION;
        VERSION = object = (String)object;
        object = (String)object + "+1";
        if (VersionInfo.getHudsonJobName().length() == 0) {
            object = (String)object + "-2019-01-16-101123";
        }
        RUNTIME_VERSION = object;
    }
}

