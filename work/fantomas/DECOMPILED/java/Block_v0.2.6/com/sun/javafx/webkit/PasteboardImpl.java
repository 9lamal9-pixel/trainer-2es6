/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.webkit;

import com.sun.javafx.tk.Toolkit;
import com.sun.javafx.webkit.UIClientImpl;
import com.sun.webkit.Pasteboard;
import com.sun.webkit.graphics.WCGraphicsManager;
import com.sun.webkit.graphics.WCImageFrame;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javax.imageio.ImageIO;

final class PasteboardImpl
implements Pasteboard {
    private final Clipboard clipboard = Clipboard.getSystemClipboard();

    PasteboardImpl() {
    }

    @Override
    public String getPlainText() {
        return this.clipboard.getString();
    }

    @Override
    public String getHtml() {
        return this.clipboard.getHtml();
    }

    @Override
    public void writePlainText(String string) {
        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(string);
        this.clipboard.setContent(clipboardContent);
    }

    @Override
    public void writeSelection(boolean bl, String string, String string2) {
        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(string);
        clipboardContent.putHtml(string2);
        this.clipboard.setContent(clipboardContent);
    }

    @Override
    public void writeImage(WCImageFrame wCImageFrame) {
        Object object = WCGraphicsManager.getGraphicsManager().toPlatformImage(wCImageFrame.getFrame());
        Image image = Toolkit.getImageAccessor().fromPlatformImage(object);
        if (image != null) {
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putImage(image);
            String string = wCImageFrame.getFrame().getFileExtension();
            try {
                File file = File.createTempFile("jfx", "." + string);
                file.deleteOnExit();
                ImageIO.write((RenderedImage)UIClientImpl.toBufferedImage(image), string, file);
                clipboardContent.putFiles(Arrays.asList(file));
            }
            catch (IOException | SecurityException exception) {
                // empty catch block
            }
            this.clipboard.setContent(clipboardContent);
        }
    }

    @Override
    public void writeUrl(String string, String string2) {
        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(string);
        clipboardContent.putHtml(string2);
        clipboardContent.putUrl(string);
        this.clipboard.setContent(clipboardContent);
    }
}

