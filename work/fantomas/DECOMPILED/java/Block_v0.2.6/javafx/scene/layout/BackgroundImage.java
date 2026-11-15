/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.layout;

import javafx.beans.NamedArg;
import javafx.scene.image.Image;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;

public final class BackgroundImage {
    final Image image;
    final BackgroundRepeat repeatX;
    final BackgroundRepeat repeatY;
    final BackgroundPosition position;
    final BackgroundSize size;
    Boolean opaque = null;
    private final int hash;

    public final Image getImage() {
        return this.image;
    }

    public final BackgroundRepeat getRepeatX() {
        return this.repeatX;
    }

    public final BackgroundRepeat getRepeatY() {
        return this.repeatY;
    }

    public final BackgroundPosition getPosition() {
        return this.position;
    }

    public final BackgroundSize getSize() {
        return this.size;
    }

    public BackgroundImage(@NamedArg(value="image") Image image, @NamedArg(value="repeatX") BackgroundRepeat backgroundRepeat, @NamedArg(value="repeatY") BackgroundRepeat backgroundRepeat2, @NamedArg(value="position") BackgroundPosition backgroundPosition, @NamedArg(value="size") BackgroundSize backgroundSize) {
        if (image == null) {
            throw new NullPointerException("Image cannot be null");
        }
        this.image = image;
        this.repeatX = backgroundRepeat == null ? BackgroundRepeat.REPEAT : backgroundRepeat;
        this.repeatY = backgroundRepeat2 == null ? BackgroundRepeat.REPEAT : backgroundRepeat2;
        this.position = backgroundPosition == null ? BackgroundPosition.DEFAULT : backgroundPosition;
        this.size = backgroundSize == null ? BackgroundSize.DEFAULT : backgroundSize;
        int n = this.image.hashCode();
        n = 31 * n + this.repeatX.hashCode();
        n = 31 * n + this.repeatY.hashCode();
        n = 31 * n + this.position.hashCode();
        this.hash = n = 31 * n + this.size.hashCode();
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        BackgroundImage backgroundImage = (BackgroundImage)object;
        if (this.hash != backgroundImage.hash) {
            return false;
        }
        if (!this.image.equals(backgroundImage.image)) {
            return false;
        }
        if (!this.position.equals(backgroundImage.position)) {
            return false;
        }
        if (this.repeatX != backgroundImage.repeatX) {
            return false;
        }
        if (this.repeatY != backgroundImage.repeatY) {
            return false;
        }
        return this.size.equals(backgroundImage.size);
    }

    public int hashCode() {
        return this.hash;
    }
}

