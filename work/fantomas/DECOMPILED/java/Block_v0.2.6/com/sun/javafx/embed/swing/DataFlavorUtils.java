/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.embed.swing;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.scene.input.DataFormat;

final class DataFlavorUtils {
    DataFlavorUtils() {
    }

    static String getFxMimeType(DataFlavor dataFlavor) {
        return dataFlavor.getPrimaryType() + "/" + dataFlavor.getSubType();
    }

    static DataFlavor[] getDataFlavors(String[] stringArray) {
        ArrayList<DataFlavor> arrayList = new ArrayList<DataFlavor>(stringArray.length);
        for (String string : stringArray) {
            DataFlavor dataFlavor = null;
            try {
                dataFlavor = new DataFlavor(string);
            }
            catch (ClassNotFoundException | IllegalArgumentException exception) {
                continue;
            }
            arrayList.add(dataFlavor);
        }
        return arrayList.toArray(new DataFlavor[0]);
    }

    static DataFlavor getDataFlavor(DataFormat dataFormat) {
        DataFlavor[] dataFlavorArray = DataFlavorUtils.getDataFlavors(dataFormat.getIdentifiers().toArray(new String[1]));
        return dataFlavorArray.length == 0 ? null : dataFlavorArray[0];
    }

    static String getMimeType(DataFormat dataFormat) {
        Iterator<String> iterator = dataFormat.getIdentifiers().iterator();
        if (iterator.hasNext()) {
            String string = iterator.next();
            return string;
        }
        return null;
    }

    static DataFormat getDataFormat(DataFlavor dataFlavor) {
        String string = DataFlavorUtils.getFxMimeType(dataFlavor);
        DataFormat dataFormat = DataFormat.lookupMimeType(string);
        if (dataFormat == null) {
            dataFormat = new DataFormat(string);
        }
        return dataFormat;
    }

    static Object adjustFxData(DataFlavor dataFlavor, Object object) throws UnsupportedEncodingException {
        if (object instanceof String) {
            if (dataFlavor.isRepresentationClassInputStream()) {
                String string = dataFlavor.getParameter("charset");
                return new ByteArrayInputStream(string != null ? ((String)object).getBytes(string) : ((String)object).getBytes());
            }
            if (dataFlavor.isRepresentationClassByteBuffer()) {
                // empty if block
            }
        }
        if (object instanceof ByteBuffer && dataFlavor.isRepresentationClassInputStream()) {
            return new ByteBufferInputStream((ByteBuffer)object);
        }
        return object;
    }

    static Object adjustSwingData(DataFlavor dataFlavor, String string, Object object) {
        if (object == null) {
            return object;
        }
        if (dataFlavor.isFlavorJavaFileListType()) {
            List list = (List)object;
            String[] stringArray = new String[list.size()];
            int n = 0;
            for (File file : list) {
                stringArray[n++] = file.getPath();
            }
            return stringArray;
        }
        DataFormat dataFormat = DataFormat.lookupMimeType(string);
        if (DataFormat.PLAIN_TEXT.equals(dataFormat)) {
            if (dataFlavor.isFlavorTextType()) {
                if (object instanceof InputStream) {
                    InputStream inputStream = (InputStream)object;
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    byte[] byArray = new byte[64];
                    try {
                        int n = inputStream.read(byArray);
                        while (n != -1) {
                            byteArrayOutputStream.write(byArray, 0, n);
                            n = inputStream.read(byArray);
                        }
                        byteArrayOutputStream.close();
                        return new String(byteArrayOutputStream.toByteArray());
                    }
                    catch (Exception exception) {}
                }
            } else if (object != null) {
                return object.toString();
            }
        }
        return object;
    }

    static Map<String, DataFlavor> adjustSwingDataFlavors(DataFlavor[] dataFlavorArray) {
        HashMap hashMap = new HashMap(dataFlavorArray.length);
        for (DataFlavor dataFlavorArray2 : dataFlavorArray) {
            Set<DataFlavor> set;
            String string = DataFlavorUtils.getFxMimeType(dataFlavorArray2);
            if (hashMap.containsKey(string)) {
                set = (Set)hashMap.get(string);
                try {
                    set.add(dataFlavorArray2);
                }
                catch (UnsupportedOperationException unsupportedOperationException) {}
                continue;
            }
            set = new HashSet();
            if (dataFlavorArray2.isFlavorTextType()) {
                set.add(DataFlavor.stringFlavor);
                set = Collections.unmodifiableSet(set);
            } else {
                set.add(dataFlavorArray2);
            }
            hashMap.put(string, set);
        }
        HashMap hashMap2 = new HashMap();
        for (String string : hashMap.keySet()) {
            DataFlavor[] dataFlavorArray2 = ((Set)hashMap.get(string)).toArray(new DataFlavor[0]);
            if (dataFlavorArray2.length == 1) {
                hashMap2.put(string, dataFlavorArray2[0]);
                continue;
            }
            hashMap2.put(string, dataFlavorArray2[0]);
        }
        return hashMap2;
    }

    private static Object readData(Transferable transferable, DataFlavor dataFlavor) {
        Object object = null;
        try {
            object = transferable.getTransferData(dataFlavor);
        }
        catch (UnsupportedFlavorException unsupportedFlavorException) {
            unsupportedFlavorException.printStackTrace(System.err);
        }
        catch (IOException iOException) {
            iOException.printStackTrace(System.err);
        }
        return object;
    }

    static Map<String, Object> readAllData(Transferable transferable, Map<String, DataFlavor> map, boolean bl) {
        Object object;
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        for (DataFlavor dataFlavor : transferable.getTransferDataFlavors()) {
            Object object2 = object = bl ? DataFlavorUtils.readData(transferable, dataFlavor) : null;
            if (object == null && bl) continue;
            String string = DataFlavorUtils.getFxMimeType(dataFlavor);
            object = DataFlavorUtils.adjustSwingData(dataFlavor, string, object);
            hashMap.put(string, object);
        }
        for (Map.Entry entry : map.entrySet()) {
            DataFlavor dataFlavor;
            String string = (String)entry.getKey();
            dataFlavor = (DataFlavor)entry.getValue();
            Object object3 = object = bl ? DataFlavorUtils.readData(transferable, dataFlavor) : null;
            if (object == null && bl) continue;
            object = DataFlavorUtils.adjustSwingData(dataFlavor, string, object);
            hashMap.put((String)entry.getKey(), object);
        }
        return hashMap;
    }

    private static class ByteBufferInputStream
    extends InputStream {
        private final ByteBuffer bb;

        private ByteBufferInputStream(ByteBuffer byteBuffer) {
            this.bb = byteBuffer;
        }

        @Override
        public int available() {
            return this.bb.remaining();
        }

        @Override
        public int read() throws IOException {
            if (!this.bb.hasRemaining()) {
                return -1;
            }
            return this.bb.get() & 0xFF;
        }

        @Override
        public int read(byte[] byArray, int n, int n2) throws IOException {
            if (!this.bb.hasRemaining()) {
                return -1;
            }
            n2 = Math.min(n2, this.bb.remaining());
            this.bb.get(byArray, n, n2);
            return n2;
        }
    }
}

