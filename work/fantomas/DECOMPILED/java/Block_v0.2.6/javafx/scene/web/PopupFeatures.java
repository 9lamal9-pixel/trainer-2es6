/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.web;

import javafx.beans.NamedArg;

public final class PopupFeatures {
    private final boolean menu;
    private final boolean status;
    private final boolean toolbar;
    private final boolean resizable;

    public PopupFeatures(@NamedArg(value="menu") boolean bl, @NamedArg(value="status") boolean bl2, @NamedArg(value="toolbar") boolean bl3, @NamedArg(value="resizable") boolean bl4) {
        this.menu = bl;
        this.status = bl2;
        this.toolbar = bl3;
        this.resizable = bl4;
    }

    public final boolean hasMenu() {
        return this.menu;
    }

    public final boolean hasStatus() {
        return this.status;
    }

    public final boolean hasToolbar() {
        return this.toolbar;
    }

    public final boolean isResizable() {
        return this.resizable;
    }
}

