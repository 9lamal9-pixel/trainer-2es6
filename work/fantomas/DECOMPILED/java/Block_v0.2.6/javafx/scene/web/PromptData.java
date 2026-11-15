/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.web;

import javafx.beans.NamedArg;

public final class PromptData {
    private final String message;
    private final String defaultValue;

    public PromptData(@NamedArg(value="message") String string, @NamedArg(value="defaultValue") String string2) {
        this.message = string;
        this.defaultValue = string2;
    }

    public final String getMessage() {
        return this.message;
    }

    public final String getDefaultValue() {
        return this.defaultValue;
    }
}

