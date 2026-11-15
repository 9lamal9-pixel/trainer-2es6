/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.input;

import com.sun.javafx.scene.input.ClipboardHelper;
import com.sun.javafx.tk.PermissionHelper;
import com.sun.javafx.tk.TKClipboard;
import com.sun.javafx.tk.Toolkit;
import java.io.File;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.scene.image.Image;
import javafx.scene.input.DataFormat;
import javafx.util.Pair;

public class Clipboard {
    private boolean contentPut = false;
    private final AccessControlContext acc = AccessController.getContext();
    TKClipboard peer;
    private static Clipboard systemClipboard;
    private static Clipboard localClipboard;

    public static Clipboard getSystemClipboard() {
        try {
            PermissionHelper.checkClipboardPermission();
            return Clipboard.getSystemClipboardImpl();
        }
        catch (SecurityException securityException) {
            return Clipboard.getLocalClipboardImpl();
        }
    }

    Clipboard(TKClipboard tKClipboard) {
        Toolkit.getToolkit().checkFxUserThread();
        if (tKClipboard == null) {
            throw new NullPointerException();
        }
        tKClipboard.setSecurityContext(this.acc);
        this.peer = tKClipboard;
    }

    public final void clear() {
        this.setContent(null);
    }

    public final Set<DataFormat> getContentTypes() {
        return this.peer.getContentTypes();
    }

    public final boolean setContent(Map<DataFormat, Object> map) {
        Toolkit.getToolkit().checkFxUserThread();
        if (map == null) {
            this.contentPut = false;
            this.peer.putContent(new Pair[0]);
            return true;
        }
        Pair[] pairArray = new Pair[map.size()];
        int n = 0;
        for (Map.Entry<DataFormat, Object> entry : map.entrySet()) {
            pairArray[n++] = new Pair<DataFormat, Object>(entry.getKey(), entry.getValue());
        }
        this.contentPut = this.peer.putContent(pairArray);
        return this.contentPut;
    }

    public final Object getContent(DataFormat dataFormat) {
        Toolkit.getToolkit().checkFxUserThread();
        return this.getContentImpl(dataFormat);
    }

    Object getContentImpl(DataFormat dataFormat) {
        return this.peer.getContent(dataFormat);
    }

    public final boolean hasContent(DataFormat dataFormat) {
        Toolkit.getToolkit().checkFxUserThread();
        return this.peer.hasContent(dataFormat);
    }

    public final boolean hasString() {
        return this.hasContent(DataFormat.PLAIN_TEXT);
    }

    public final String getString() {
        return (String)this.getContent(DataFormat.PLAIN_TEXT);
    }

    public final boolean hasUrl() {
        return this.hasContent(DataFormat.URL);
    }

    public final String getUrl() {
        return (String)this.getContent(DataFormat.URL);
    }

    public final boolean hasHtml() {
        return this.hasContent(DataFormat.HTML);
    }

    public final String getHtml() {
        return (String)this.getContent(DataFormat.HTML);
    }

    public final boolean hasRtf() {
        return this.hasContent(DataFormat.RTF);
    }

    public final String getRtf() {
        return (String)this.getContent(DataFormat.RTF);
    }

    public final boolean hasImage() {
        return this.hasContent(DataFormat.IMAGE);
    }

    public final Image getImage() {
        return (Image)this.getContent(DataFormat.IMAGE);
    }

    public final boolean hasFiles() {
        return this.hasContent(DataFormat.FILES);
    }

    public final List<File> getFiles() {
        return (List)this.getContent(DataFormat.FILES);
    }

    boolean contentPut() {
        return this.contentPut;
    }

    private static synchronized Clipboard getSystemClipboardImpl() {
        if (systemClipboard == null) {
            systemClipboard = new Clipboard(Toolkit.getToolkit().getSystemClipboard());
        }
        return systemClipboard;
    }

    private static synchronized Clipboard getLocalClipboardImpl() {
        if (localClipboard == null) {
            localClipboard = new Clipboard(Toolkit.getToolkit().createLocalClipboard());
        }
        return localClipboard;
    }

    static {
        ClipboardHelper.setClipboardAccessor(new ClipboardHelper.ClipboardAccessor(){

            @Override
            public boolean contentPut(Clipboard clipboard) {
                return clipboard.contentPut();
            }
        });
    }
}

