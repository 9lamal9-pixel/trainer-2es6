/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.input;

import com.sun.javafx.util.WeakReferenceQueue;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class DataFormat {
    private static final WeakReferenceQueue<DataFormat> DATA_FORMAT_LIST = new WeakReferenceQueue();
    public static final DataFormat PLAIN_TEXT = new DataFormat("text/plain");
    public static final DataFormat HTML = new DataFormat("text/html");
    public static final DataFormat RTF = new DataFormat("text/rtf");
    public static final DataFormat URL = new DataFormat("text/uri-list");
    public static final DataFormat IMAGE = new DataFormat("application/x-java-rawimage");
    public static final DataFormat FILES = new DataFormat("application/x-java-file-list", "java.file-list");
    private static final DataFormat DRAG_IMAGE = new DataFormat("application/x-java-drag-image");
    private static final DataFormat DRAG_IMAGE_OFFSET = new DataFormat("application/x-java-drag-image-offset");
    private final Set<String> identifier;

    public DataFormat(String ... stringArray) {
        DATA_FORMAT_LIST.cleanup();
        if (stringArray != null) {
            for (String string : stringArray) {
                if (DataFormat.lookupMimeType(string) == null) continue;
                throw new IllegalArgumentException("DataFormat '" + string + "' already exists.");
            }
            this.identifier = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(stringArray)));
        } else {
            this.identifier = Collections.emptySet();
        }
        DATA_FORMAT_LIST.add(this);
    }

    public final Set<String> getIdentifiers() {
        return this.identifier;
    }

    public String toString() {
        if (this.identifier.isEmpty()) {
            return "[]";
        }
        if (this.identifier.size() == 1) {
            StringBuilder stringBuilder = new StringBuilder("[");
            stringBuilder.append(this.identifier.iterator().next());
            return stringBuilder.append("]").toString();
        }
        StringBuilder stringBuilder = new StringBuilder("[");
        Iterator<String> iterator = this.identifier.iterator();
        while (iterator.hasNext()) {
            stringBuilder = stringBuilder.append(iterator.next());
            if (!iterator.hasNext()) continue;
            stringBuilder = stringBuilder.append(", ");
        }
        stringBuilder = stringBuilder.append("]");
        return stringBuilder.toString();
    }

    public int hashCode() {
        int n = 7;
        for (String string : this.identifier) {
            n = 31 * n + string.hashCode();
        }
        return n;
    }

    public boolean equals(Object object) {
        if (object == null || !(object instanceof DataFormat)) {
            return false;
        }
        DataFormat dataFormat = (DataFormat)object;
        return this.identifier.equals(dataFormat.identifier);
    }

    public static DataFormat lookupMimeType(String string) {
        if (string == null || string.length() == 0) {
            return null;
        }
        Iterator<DataFormat> iterator = DATA_FORMAT_LIST.iterator();
        while (iterator.hasNext()) {
            DataFormat dataFormat = iterator.next();
            if (!dataFormat.getIdentifiers().contains(string)) continue;
            return dataFormat;
        }
        return null;
    }
}

