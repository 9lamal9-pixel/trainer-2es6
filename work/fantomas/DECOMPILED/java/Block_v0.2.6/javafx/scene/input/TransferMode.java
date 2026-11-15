/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.input;

public enum TransferMode {
    COPY,
    MOVE,
    LINK;

    public static final TransferMode[] ANY;
    public static final TransferMode[] COPY_OR_MOVE;
    public static final TransferMode[] NONE;

    static {
        ANY = new TransferMode[]{COPY, MOVE, LINK};
        COPY_OR_MOVE = new TransferMode[]{COPY, MOVE};
        NONE = new TransferMode[0];
    }
}

