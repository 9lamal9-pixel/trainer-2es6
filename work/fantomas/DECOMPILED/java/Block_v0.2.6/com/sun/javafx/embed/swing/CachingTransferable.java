/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.embed.swing;

import com.sun.javafx.embed.swing.DataFlavorUtils;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;

public class CachingTransferable
implements Transferable {
    private Map<String, Object> mimeType2Data = Collections.EMPTY_MAP;

    @Override
    public Object getTransferData(DataFlavor dataFlavor) throws UnsupportedEncodingException {
        String string = DataFlavorUtils.getFxMimeType(dataFlavor);
        return DataFlavorUtils.adjustFxData(dataFlavor, this.getData(string));
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        String[] stringArray = this.getMimeTypes();
        return DataFlavorUtils.getDataFlavors(stringArray);
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor dataFlavor) {
        return this.isMimeTypeAvailable(DataFlavorUtils.getFxMimeType(dataFlavor));
    }

    public void updateData(Transferable transferable, boolean bl) {
        Map<String, DataFlavor> map = DataFlavorUtils.adjustSwingDataFlavors(transferable.getTransferDataFlavors());
        try {
            this.mimeType2Data = DataFlavorUtils.readAllData(transferable, map, bl);
        }
        catch (Exception exception) {
            this.mimeType2Data = Collections.EMPTY_MAP;
        }
    }

    public void updateData(Clipboard clipboard, boolean bl) {
        this.mimeType2Data = new HashMap<String, Object>();
        for (DataFormat dataFormat : clipboard.getContentTypes()) {
            this.mimeType2Data.put(DataFlavorUtils.getMimeType(dataFormat), bl ? clipboard.getContent(dataFormat) : null);
        }
    }

    public Object getData(String string) {
        return this.mimeType2Data.get(string);
    }

    public String[] getMimeTypes() {
        return this.mimeType2Data.keySet().toArray(new String[0]);
    }

    public boolean isMimeTypeAvailable(String string) {
        return Arrays.asList(this.getMimeTypes()).contains(string);
    }
}

