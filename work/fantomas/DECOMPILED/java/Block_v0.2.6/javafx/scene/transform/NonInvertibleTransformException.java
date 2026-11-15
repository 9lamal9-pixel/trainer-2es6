/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.transform;

import javafx.beans.NamedArg;

public class NonInvertibleTransformException
extends Exception {
    public NonInvertibleTransformException(@NamedArg(value="message") String string) {
        super(string);
    }
}

