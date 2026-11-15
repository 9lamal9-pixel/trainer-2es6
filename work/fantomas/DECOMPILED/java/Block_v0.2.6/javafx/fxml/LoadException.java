/*
 * Decompiled with CFR 0.152.
 */
package javafx.fxml;

import java.io.IOException;

public class LoadException
extends IOException {
    private static final long serialVersionUID = 0L;

    public LoadException() {
    }

    public LoadException(String string) {
        super(string);
    }

    public LoadException(Throwable throwable) {
        super(throwable);
    }

    public LoadException(String string, Throwable throwable) {
        super(string, throwable);
    }
}

