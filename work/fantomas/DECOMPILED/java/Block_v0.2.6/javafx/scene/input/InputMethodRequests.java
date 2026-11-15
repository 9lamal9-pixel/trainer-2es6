/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.input;

import javafx.geometry.Point2D;

public interface InputMethodRequests {
    public Point2D getTextLocation(int var1);

    public int getLocationOffset(int var1, int var2);

    public void cancelLatestCommittedText();

    public String getSelectedText();
}

