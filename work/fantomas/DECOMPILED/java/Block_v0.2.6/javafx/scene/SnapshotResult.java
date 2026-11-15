/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene;

import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;

public class SnapshotResult {
    private WritableImage image;
    private Object source;
    private SnapshotParameters params;

    SnapshotResult(WritableImage writableImage, Object object, SnapshotParameters snapshotParameters) {
        this.image = writableImage;
        this.source = object;
        this.params = snapshotParameters;
    }

    public WritableImage getImage() {
        return this.image;
    }

    public Object getSource() {
        return this.source;
    }

    public SnapshotParameters getSnapshotParameters() {
        return this.params;
    }
}

