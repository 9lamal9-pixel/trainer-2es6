/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.input;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javafx.scene.image.Image;
import javafx.scene.input.DataFormat;

public class ClipboardContent
extends HashMap<DataFormat, Object> {
    public final boolean hasString() {
        return this.containsKey(DataFormat.PLAIN_TEXT);
    }

    public final boolean putString(String string) {
        if (string == null) {
            this.remove(DataFormat.PLAIN_TEXT);
        } else {
            this.put(DataFormat.PLAIN_TEXT, string);
        }
        return true;
    }

    public final String getString() {
        return (String)this.get(DataFormat.PLAIN_TEXT);
    }

    public final boolean hasUrl() {
        return this.containsKey(DataFormat.URL);
    }

    public final boolean putUrl(String string) {
        if (string == null) {
            this.remove(DataFormat.URL);
        } else {
            this.put(DataFormat.URL, string);
        }
        return true;
    }

    public final String getUrl() {
        return (String)this.get(DataFormat.URL);
    }

    public final boolean hasHtml() {
        return this.containsKey(DataFormat.HTML);
    }

    public final boolean putHtml(String string) {
        if (string == null) {
            this.remove(DataFormat.HTML);
        } else {
            this.put(DataFormat.HTML, string);
        }
        return true;
    }

    public final String getHtml() {
        return (String)this.get(DataFormat.HTML);
    }

    public final boolean hasRtf() {
        return this.containsKey(DataFormat.RTF);
    }

    public final boolean putRtf(String string) {
        if (string == null) {
            this.remove(DataFormat.RTF);
        } else {
            this.put(DataFormat.RTF, string);
        }
        return true;
    }

    public final String getRtf() {
        return (String)this.get(DataFormat.RTF);
    }

    public final boolean hasImage() {
        return this.containsKey(DataFormat.IMAGE);
    }

    public final boolean putImage(Image image) {
        if (image == null) {
            this.remove(DataFormat.IMAGE);
        } else {
            this.put(DataFormat.IMAGE, image);
        }
        return true;
    }

    public final Image getImage() {
        return (Image)this.get(DataFormat.IMAGE);
    }

    public final boolean hasFiles() {
        return this.containsKey(DataFormat.FILES);
    }

    public final boolean putFiles(List<File> list) {
        if (list == null) {
            this.remove(DataFormat.FILES);
        } else {
            this.put(DataFormat.FILES, list);
        }
        return true;
    }

    public final boolean putFilesByPath(List<String> list) {
        ArrayList<File> arrayList = new ArrayList<File>(list.size());
        for (String string : list) {
            arrayList.add(new File(string));
        }
        return this.putFiles(arrayList);
    }

    public final List<File> getFiles() {
        return (List)this.get(DataFormat.FILES);
    }
}

