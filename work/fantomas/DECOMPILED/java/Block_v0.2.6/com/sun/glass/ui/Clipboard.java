/*
 * Decompiled with CFR 0.152.
 */
package com.sun.glass.ui;

import com.sun.glass.ui.Application;
import com.sun.glass.ui.ClipboardAssistance;
import com.sun.glass.ui.DelayedCallback;
import com.sun.glass.ui.Pixels;
import com.sun.glass.ui.PlatformFactory;
import com.sun.glass.ui.delegate.ClipboardDelegate;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Clipboard {
    public static final String TEXT_TYPE = "text/plain";
    public static final String HTML_TYPE = "text/html";
    public static final String RTF_TYPE = "text/rtf";
    public static final String URI_TYPE = "text/uri-list";
    public static final String FILE_LIST_TYPE = "application/x-java-file-list";
    public static final String RAW_IMAGE_TYPE = "application/x-java-rawimage";
    public static final String DRAG_IMAGE = "application/x-java-drag-image";
    public static final String DRAG_IMAGE_OFFSET = "application/x-java-drag-image-offset";
    public static final String IE_URL_SHORTCUT_FILENAME = "text/ie-shortcut-filename";
    public static final int ACTION_NONE = 0;
    public static final int ACTION_COPY = 1;
    public static final int ACTION_MOVE = 2;
    public static final int ACTION_REFERENCE = 0x40000000;
    public static final int ACTION_COPY_OR_MOVE = 3;
    public static final int ACTION_ANY = 0x4FFFFFFF;
    public static final String DND = "DND";
    public static final String SYSTEM = "SYSTEM";
    public static final String SELECTION = "SELECTION";
    private static final Map<String, Clipboard> clipboards = new HashMap<String, Clipboard>();
    private static final ClipboardDelegate delegate = PlatformFactory.getPlatformFactory().createClipboardDelegate();
    private final HashSet<ClipboardAssistance> assistants = new HashSet();
    private final String name;
    private final Object localDataProtector = new Object();
    private HashMap<String, Object> localSharedData;
    private ClipboardAssistance dataSource;
    protected int supportedActions = 1;

    protected Clipboard(String string) {
        Application.checkEventThread();
        this.name = string;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void add(ClipboardAssistance clipboardAssistance) {
        Application.checkEventThread();
        HashSet<ClipboardAssistance> hashSet = this.assistants;
        synchronized (hashSet) {
            this.assistants.add(clipboardAssistance);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void remove(ClipboardAssistance clipboardAssistance) {
        boolean bl;
        Application.checkEventThread();
        Object object = this.localDataProtector;
        synchronized (object) {
            if (clipboardAssistance == this.dataSource) {
                this.dataSource = null;
            }
        }
        Object object2 = this.assistants;
        synchronized (object2) {
            this.assistants.remove(clipboardAssistance);
            bl = this.assistants.isEmpty();
        }
        if (bl) {
            object2 = clipboards;
            synchronized (object2) {
                clipboards.remove(this.name);
            }
            this.close();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void setSharedData(ClipboardAssistance clipboardAssistance, HashMap<String, Object> hashMap, int n) {
        Application.checkEventThread();
        Object object = this.localDataProtector;
        synchronized (object) {
            this.localSharedData = (HashMap)hashMap.clone();
            this.supportedActions = n;
            this.dataSource = clipboardAssistance;
        }
    }

    public void flush(ClipboardAssistance clipboardAssistance, HashMap<String, Object> hashMap, int n) {
        Application.checkEventThread();
        this.setSharedData(clipboardAssistance, hashMap, n);
        this.contentChanged();
    }

    public int getSupportedSourceActions() {
        Application.checkEventThread();
        return this.supportedActions;
    }

    public void setTargetAction(int n) {
        Application.checkEventThread();
        this.actionPerformed(n);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void contentChanged() {
        HashSet hashSet;
        Application.checkEventThread();
        HashSet<ClipboardAssistance> hashSet2 = this.assistants;
        synchronized (hashSet2) {
            hashSet = (HashSet)this.assistants.clone();
        }
        for (ClipboardAssistance clipboardAssistance : hashSet) {
            clipboardAssistance.contentChanged();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void actionPerformed(int n) {
        Application.checkEventThread();
        Object object = this.localDataProtector;
        synchronized (object) {
            if (null != this.dataSource) {
                this.dataSource.actionPerformed(n);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Object getData(String string) {
        Application.checkEventThread();
        Object object = this.localDataProtector;
        synchronized (object) {
            if (this.localSharedData == null) {
                return null;
            }
            Object object2 = this.localSharedData.get(string);
            Object object3 = object2 instanceof DelayedCallback ? ((DelayedCallback)object2).providedData() : object2;
            return object3;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String[] getMimeTypes() {
        Application.checkEventThread();
        Object object = this.localDataProtector;
        synchronized (object) {
            if (this.localSharedData == null) {
                return null;
            }
            Set<String> set = this.localSharedData.keySet();
            String[] stringArray = new String[set.size()];
            int n = 0;
            for (String string : set) {
                stringArray[n++] = string;
            }
            return stringArray;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected static Clipboard get(String string) {
        Application.checkEventThread();
        Map<String, Clipboard> map = clipboards;
        synchronized (map) {
            if (!clipboards.keySet().contains(string)) {
                Clipboard clipboard = delegate.createClipboard(string);
                if (clipboard == null) {
                    clipboard = new Clipboard(string);
                }
                clipboards.put(string, clipboard);
            }
            return clipboards.get(string);
        }
    }

    public Pixels getPixelsForRawImage(byte[] byArray) {
        Application.checkEventThread();
        ByteBuffer byteBuffer = ByteBuffer.wrap(byArray, 0, 8);
        int n = byteBuffer.getInt();
        int n2 = byteBuffer.getInt();
        ByteBuffer byteBuffer2 = ByteBuffer.wrap(byArray, 8, byArray.length - 8);
        return Application.GetApplication().createPixels(n, n2, byteBuffer2.slice());
    }

    public String toString() {
        return "Clipboard: " + this.name + "@" + this.hashCode();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void close() {
        Application.checkEventThread();
        Object object = this.localDataProtector;
        synchronized (object) {
            this.dataSource = null;
        }
    }

    public String getName() {
        Application.checkEventThread();
        return this.name;
    }

    public static String getActionString(int n) {
        Application.checkEventThread();
        StringBuilder stringBuilder = new StringBuilder("");
        int[] nArray = new int[]{1, 2, 0x40000000};
        String[] stringArray = new String[]{"copy", "move", "link"};
        for (int i = 0; i < 3; ++i) {
            if ((nArray[i] & n) <= 0) continue;
            if (stringBuilder.length() > 0) {
                stringBuilder.append(",");
            }
            stringBuilder.append(stringArray[i]);
        }
        return stringBuilder.toString();
    }
}

