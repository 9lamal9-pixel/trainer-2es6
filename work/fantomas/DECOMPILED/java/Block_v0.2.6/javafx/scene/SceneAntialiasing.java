/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene;

public final class SceneAntialiasing {
    public static final SceneAntialiasing DISABLED = new SceneAntialiasing("DISABLED");
    public static final SceneAntialiasing BALANCED = new SceneAntialiasing("BALANCED");
    private final String val;

    private SceneAntialiasing(String string) {
        this.val = string;
    }

    public String toString() {
        return this.val;
    }
}

