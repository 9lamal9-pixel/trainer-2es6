/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.layout;

public enum Priority {
    ALWAYS,
    SOMETIMES,
    NEVER;


    public static Priority max(Priority priority, Priority priority2) {
        if (priority == ALWAYS || priority2 == ALWAYS) {
            return ALWAYS;
        }
        if (priority == SOMETIMES || priority2 == SOMETIMES) {
            return SOMETIMES;
        }
        return NEVER;
    }

    public static Priority min(Priority priority, Priority priority2) {
        if (priority == NEVER || priority2 == NEVER) {
            return NEVER;
        }
        if (priority == SOMETIMES || priority2 == SOMETIMES) {
            return SOMETIMES;
        }
        return ALWAYS;
    }
}

