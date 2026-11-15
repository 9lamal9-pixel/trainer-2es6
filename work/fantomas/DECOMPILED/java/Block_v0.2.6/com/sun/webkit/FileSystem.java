/*
 * Decompiled with CFR 0.152.
 */
package com.sun.webkit;

import com.sun.javafx.logging.PlatformLogger;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;

final class FileSystem {
    private static final int TYPE_UNKNOWN = 0;
    private static final int TYPE_FILE = 1;
    private static final int TYPE_DIRECTORY = 2;
    private static final PlatformLogger logger = PlatformLogger.getLogger(FileSystem.class.getName());

    private FileSystem() {
        throw new AssertionError();
    }

    private static boolean fwkFileExists(String string) {
        return new File(string).exists();
    }

    private static long fwkGetFileSize(String string) {
        try {
            File file = new File(string);
            if (file.exists()) {
                return file.length();
            }
        }
        catch (SecurityException securityException) {
            logger.fine(String.format("Error determining size of file [%s]", string), securityException);
        }
        return -1L;
    }

    private static boolean fwkGetFileMetadata(String string, long[] lArray) {
        try {
            File file = new File(string);
            if (file.exists()) {
                lArray[0] = file.lastModified();
                lArray[1] = file.length();
                lArray[2] = file.isDirectory() ? 2L : (file.isFile() ? 1L : 0L);
                return true;
            }
        }
        catch (SecurityException securityException) {
            logger.fine(String.format("Error determining Metadata for file [%s]", string), securityException);
        }
        return false;
    }

    private static String fwkPathByAppendingComponent(String string, String string2) {
        return new File(string, string2).getPath();
    }

    private static boolean fwkMakeAllDirectories(String string) {
        try {
            Files.createDirectories(Paths.get(string, new String[0]), new FileAttribute[0]);
            return true;
        }
        catch (IOException | InvalidPathException exception) {
            logger.fine(String.format("Error creating directory [%s]", string), exception);
            return false;
        }
    }

    private static String fwkPathGetFileName(String string) {
        return new File(string).getName();
    }
}

