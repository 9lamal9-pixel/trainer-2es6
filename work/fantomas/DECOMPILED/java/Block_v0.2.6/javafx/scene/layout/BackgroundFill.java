/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.layout;

import javafx.beans.NamedArg;
import javafx.geometry.Insets;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public final class BackgroundFill {
    final Paint fill;
    final CornerRadii radii;
    final Insets insets;
    private final int hash;

    public final Paint getFill() {
        return this.fill;
    }

    public final CornerRadii getRadii() {
        return this.radii;
    }

    public final Insets getInsets() {
        return this.insets;
    }

    public BackgroundFill(@NamedArg(value="fill") Paint paint, @NamedArg(value="radii") CornerRadii cornerRadii, @NamedArg(value="insets") Insets insets) {
        this.fill = paint == null ? Color.TRANSPARENT : paint;
        this.radii = cornerRadii == null ? CornerRadii.EMPTY : cornerRadii;
        this.insets = insets == null ? Insets.EMPTY : insets;
        int n = this.fill.hashCode();
        n = 31 * n + this.radii.hashCode();
        this.hash = n = 31 * n + this.insets.hashCode();
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        BackgroundFill backgroundFill = (BackgroundFill)object;
        if (this.hash != backgroundFill.hash) {
            return false;
        }
        if (!this.fill.equals(backgroundFill.fill)) {
            return false;
        }
        if (!this.insets.equals(backgroundFill.insets)) {
            return false;
        }
        return this.radii.equals(backgroundFill.radii);
    }

    public int hashCode() {
        return this.hash;
    }
}

