/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.iio.common;

import com.sun.javafx.iio.ImageFormatDescription;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ImageDescriptor
implements ImageFormatDescription {
    private final String formatName;
    private final List<String> extensions;
    private final List<ImageFormatDescription.Signature> signatures;

    public ImageDescriptor(String string, String[] stringArray, ImageFormatDescription.Signature[] signatureArray) {
        this.formatName = string;
        this.extensions = Collections.unmodifiableList(Arrays.asList(stringArray));
        this.signatures = Collections.unmodifiableList(Arrays.asList(signatureArray));
    }

    @Override
    public String getFormatName() {
        return this.formatName;
    }

    @Override
    public List<String> getExtensions() {
        return this.extensions;
    }

    @Override
    public List<ImageFormatDescription.Signature> getSignatures() {
        return this.signatures;
    }
}

