/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.input;

import com.sun.javafx.scene.input.DragboardHelper;
import com.sun.javafx.tk.PermissionHelper;
import com.sun.javafx.tk.TKClipboard;
import java.util.Set;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.input.TransferMode;

public final class Dragboard
extends Clipboard {
    private boolean dataAccessRestricted = true;

    Dragboard(TKClipboard tKClipboard) {
        super(tKClipboard);
    }

    @Override
    Object getContentImpl(DataFormat dataFormat) {
        if (this.dataAccessRestricted) {
            PermissionHelper.checkClipboardPermission();
        }
        return super.getContentImpl(dataFormat);
    }

    public final Set<TransferMode> getTransferModes() {
        return this.peer.getTransferModes();
    }

    TKClipboard getPeer() {
        return this.peer;
    }

    static Dragboard createDragboard(TKClipboard tKClipboard) {
        return new Dragboard(tKClipboard);
    }

    public void setDragView(Image image, double d, double d2) {
        this.peer.setDragView(image);
        this.peer.setDragViewOffsetX(d);
        this.peer.setDragViewOffsetY(d2);
    }

    public void setDragView(Image image) {
        this.peer.setDragView(image);
    }

    public void setDragViewOffsetX(double d) {
        this.peer.setDragViewOffsetX(d);
    }

    public void setDragViewOffsetY(double d) {
        this.peer.setDragViewOffsetY(d);
    }

    public Image getDragView() {
        return this.peer.getDragView();
    }

    public double getDragViewOffsetX() {
        return this.peer.getDragViewOffsetX();
    }

    public double getDragViewOffsetY() {
        return this.peer.getDragViewOffsetY();
    }

    static {
        DragboardHelper.setDragboardAccessor(new DragboardHelper.DragboardAccessor(){

            @Override
            public void setDataAccessRestriction(Dragboard dragboard, boolean bl) {
                dragboard.dataAccessRestricted = bl;
            }

            @Override
            public TKClipboard getPeer(Dragboard dragboard) {
                return dragboard.getPeer();
            }

            @Override
            public Dragboard createDragboard(TKClipboard tKClipboard) {
                return Dragboard.createDragboard(tKClipboard);
            }
        });
    }
}

