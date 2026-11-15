/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.webkit;

import com.sun.javafx.tk.Toolkit;
import com.sun.javafx.webkit.Accessor;
import com.sun.webkit.UIClient;
import com.sun.webkit.WebPage;
import com.sun.webkit.graphics.WCImage;
import com.sun.webkit.graphics.WCRectangle;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.PromptData;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javax.imageio.ImageIO;

public final class UIClientImpl
implements UIClient {
    private final Accessor accessor;
    private FileChooser chooser;
    private static final Map<String, FileExtensionInfo> fileExtensionMap = new HashMap<String, FileExtensionInfo>();
    private ClipboardContent content;
    private static final DataFormat DF_DRAG_IMAGE;
    private static final DataFormat DF_DRAG_IMAGE_OFFSET;

    public UIClientImpl(Accessor accessor) {
        this.accessor = accessor;
    }

    private WebEngine getWebEngine() {
        return this.accessor.getEngine();
    }

    private AccessControlContext getAccessContext() {
        return this.accessor.getPage().getAccessControlContext();
    }

    @Override
    public WebPage createPage(boolean bl, boolean bl2, boolean bl3, boolean bl4) {
        WebEngine webEngine = this.getWebEngine();
        if (webEngine != null && webEngine.getCreatePopupHandler() != null) {
            PopupFeatures popupFeatures = new PopupFeatures(bl, bl2, bl3, bl4);
            WebEngine webEngine2 = AccessController.doPrivileged(() -> webEngine.getCreatePopupHandler().call(popupFeatures), this.getAccessContext());
            return Accessor.getPageFor(webEngine2);
        }
        return null;
    }

    private void dispatchWebEvent(EventHandler eventHandler, WebEvent webEvent) {
        AccessController.doPrivileged(() -> {
            eventHandler.handle(webEvent);
            return null;
        }, this.getAccessContext());
    }

    private void notifyVisibilityChanged(boolean bl) {
        WebEngine webEngine = this.getWebEngine();
        if (webEngine != null && webEngine.getOnVisibilityChanged() != null) {
            this.dispatchWebEvent(webEngine.getOnVisibilityChanged(), new WebEvent<Boolean>((Object)webEngine, WebEvent.VISIBILITY_CHANGED, bl));
        }
    }

    @Override
    public void closePage() {
        this.notifyVisibilityChanged(false);
    }

    @Override
    public void showView() {
        this.notifyVisibilityChanged(true);
    }

    @Override
    public WCRectangle getViewBounds() {
        WebView webView = this.accessor.getView();
        Window window = null;
        if (webView != null && webView.getScene() != null && (window = webView.getScene().getWindow()) != null) {
            return new WCRectangle((float)window.getX(), (float)window.getY(), (float)window.getWidth(), (float)window.getHeight());
        }
        return null;
    }

    @Override
    public void setViewBounds(WCRectangle wCRectangle) {
        WebEngine webEngine = this.getWebEngine();
        if (webEngine != null && webEngine.getOnResized() != null) {
            this.dispatchWebEvent(webEngine.getOnResized(), new WebEvent<Rectangle2D>((Object)webEngine, WebEvent.RESIZED, new Rectangle2D(wCRectangle.getX(), wCRectangle.getY(), wCRectangle.getWidth(), wCRectangle.getHeight())));
        }
    }

    @Override
    public void setStatusbarText(String string) {
        WebEngine webEngine = this.getWebEngine();
        if (webEngine != null && webEngine.getOnStatusChanged() != null) {
            this.dispatchWebEvent(webEngine.getOnStatusChanged(), new WebEvent<String>((Object)webEngine, WebEvent.STATUS_CHANGED, string));
        }
    }

    @Override
    public void alert(String string) {
        WebEngine webEngine = this.getWebEngine();
        if (webEngine != null && webEngine.getOnAlert() != null) {
            this.dispatchWebEvent(webEngine.getOnAlert(), new WebEvent<String>((Object)webEngine, WebEvent.ALERT, string));
        }
    }

    @Override
    public boolean confirm(String string) {
        WebEngine webEngine = this.getWebEngine();
        if (webEngine != null && webEngine.getConfirmHandler() != null) {
            return AccessController.doPrivileged(() -> webEngine.getConfirmHandler().call(string), this.getAccessContext());
        }
        return false;
    }

    @Override
    public String prompt(String string, String string2) {
        WebEngine webEngine = this.getWebEngine();
        if (webEngine != null && webEngine.getPromptHandler() != null) {
            PromptData promptData = new PromptData(string, string2);
            return AccessController.doPrivileged(() -> webEngine.getPromptHandler().call(promptData), this.getAccessContext());
        }
        return "";
    }

    @Override
    public boolean canRunBeforeUnloadConfirmPanel() {
        return false;
    }

    @Override
    public boolean runBeforeUnloadConfirmPanel(String string) {
        return false;
    }

    @Override
    public String[] chooseFile(String string, boolean bl, String string2) {
        String[] stringArray;
        Object object;
        Window window = null;
        WebView webView = this.accessor.getView();
        if (webView != null && webView.getScene() != null) {
            window = webView.getScene().getWindow();
        }
        if (this.chooser == null) {
            this.chooser = new FileChooser();
        }
        this.chooser.getExtensionFilters().clear();
        if (string2 != null && !string2.isEmpty()) {
            this.addMimeFilters(this.chooser, string2);
        }
        this.chooser.getExtensionFilters().addAll((FileChooser.ExtensionFilter[])new FileChooser.ExtensionFilter[]{new FileChooser.ExtensionFilter("All Files", "*.*")});
        if (string != null) {
            for (object = new File(string); object != null && !((File)object).isDirectory(); object = ((File)object).getParentFile()) {
            }
            this.chooser.setInitialDirectory((File)object);
        }
        if (bl) {
            object = this.chooser.showOpenMultipleDialog(window);
            if (object != null) {
                int n = object.size();
                String[] stringArray2 = new String[n];
                for (int i = 0; i < n; ++i) {
                    stringArray2[i] = ((File)object.get(i)).getAbsolutePath();
                }
                return stringArray2;
            }
            return null;
        }
        object = this.chooser.showOpenDialog(window);
        if (object != null) {
            String[] stringArray3 = new String[1];
            stringArray = stringArray3;
            stringArray3[0] = ((File)object).getAbsolutePath();
        } else {
            stringArray = null;
        }
        return stringArray;
    }

    private void addSpecificFilters(FileChooser fileChooser, String string) {
        if (string.contains("/")) {
            FileChooser.ExtensionFilter extensionFilter;
            String[] stringArray = string.split("/");
            String string2 = stringArray[0];
            String string3 = stringArray[1];
            FileExtensionInfo fileExtensionInfo = fileExtensionMap.get(string2);
            if (fileExtensionInfo != null && (extensionFilter = fileExtensionInfo.getExtensionFilter(string3)) != null) {
                fileChooser.getExtensionFilters().addAll((FileChooser.ExtensionFilter[])new FileChooser.ExtensionFilter[]{extensionFilter});
            }
        }
    }

    private void addMimeFilters(FileChooser fileChooser, String string) {
        if (string.contains(",")) {
            String[] stringArray;
            for (String string2 : stringArray = string.split(",")) {
                this.addSpecificFilters(fileChooser, string2);
            }
        } else {
            this.addSpecificFilters(fileChooser, string);
        }
    }

    @Override
    public void print() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static DataFormat getDataFormat(String string) {
        Class<DataFormat> clazz = DataFormat.class;
        synchronized (DataFormat.class) {
            DataFormat dataFormat = DataFormat.lookupMimeType(string);
            if (dataFormat == null) {
                dataFormat = new DataFormat(string);
            }
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return dataFormat;
        }
    }

    @Override
    public void startDrag(WCImage wCImage, int n, int n2, int n3, int n4, String[] stringArray, Object[] objectArray, boolean bl) {
        this.content = new ClipboardContent();
        for (int i = 0; i < stringArray.length; ++i) {
            if (objectArray[i] == null) continue;
            try {
                this.content.put(UIClientImpl.getDataFormat(stringArray[i]), "text/ie-shortcut-filename".equals(stringArray[i]) ? ByteBuffer.wrap(((String)objectArray[i]).getBytes("UTF-16LE")) : objectArray[i]);
                continue;
            }
            catch (UnsupportedEncodingException unsupportedEncodingException) {
                // empty catch block
            }
        }
        if (wCImage != null) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(8);
            byteBuffer.rewind();
            byteBuffer.putInt(n);
            byteBuffer.putInt(n2);
            this.content.put(DF_DRAG_IMAGE_OFFSET, byteBuffer);
            int n5 = wCImage.getWidth();
            int n6 = wCImage.getHeight();
            ByteBuffer byteBuffer2 = wCImage.getPixelBuffer();
            ByteBuffer byteBuffer3 = ByteBuffer.allocate(8 + n5 * n6 * 4);
            byteBuffer3.putInt(n5);
            byteBuffer3.putInt(n6);
            byteBuffer3.put(byteBuffer2);
            this.content.put(DF_DRAG_IMAGE, byteBuffer3);
            if (bl) {
                Object object = wCImage.getWidth() > 0 && wCImage.getHeight() > 0 ? wCImage.getPlatformImage() : null;
                String string = wCImage.getFileExtension();
                if (object != null) {
                    try {
                        File file = File.createTempFile("jfx", "." + string);
                        file.deleteOnExit();
                        ImageIO.write((RenderedImage)UIClientImpl.toBufferedImage(Toolkit.getImageAccessor().fromPlatformImage(Toolkit.getToolkit().loadPlatformImage(object))), string, file);
                        this.content.put(DataFormat.FILES, Arrays.asList(file));
                    }
                    catch (IOException | SecurityException exception) {
                        // empty catch block
                    }
                }
            }
        }
    }

    @Override
    public void confirmStartDrag() {
        WebView webView = this.accessor.getView();
        if (webView != null && this.content != null) {
            Dragboard dragboard = webView.startDragAndDrop(TransferMode.ANY);
            dragboard.setContent(this.content);
        }
        this.content = null;
    }

    @Override
    public boolean isDragConfirmed() {
        return this.accessor.getView() != null && this.content != null;
    }

    private static int getBestBufferedImageType(PixelFormat<?> pixelFormat, BufferedImage bufferedImage, boolean bl) {
        int n;
        if (bufferedImage != null && ((n = bufferedImage.getType()) == 2 || n == 3 || bl && (n == 4 || n == 1))) {
            return n;
        }
        switch (pixelFormat.getType()) {
            default: {
                return 3;
            }
            case BYTE_BGRA: 
            case INT_ARGB: {
                return 2;
            }
            case BYTE_RGB: {
                return 1;
            }
            case BYTE_INDEXED: 
        }
        return pixelFormat.isPremultiplied() ? 3 : 2;
    }

    private static WritablePixelFormat<IntBuffer> getAssociatedPixelFormat(BufferedImage bufferedImage) {
        switch (bufferedImage.getType()) {
            case 1: 
            case 3: {
                return PixelFormat.getIntArgbPreInstance();
            }
            case 2: {
                return PixelFormat.getIntArgbInstance();
            }
        }
        throw new InternalError("Failed to validate BufferedImage type");
    }

    private static boolean checkFXImageOpaque(PixelReader pixelReader, int n, int n2) {
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n2; ++j) {
                Color color = pixelReader.getColor(i, j);
                if (color.getOpacity() == 1.0) continue;
                return false;
            }
        }
        return true;
    }

    private static BufferedImage fromFXImage(Image image, BufferedImage bufferedImage) {
        PixelReader pixelReader = image.getPixelReader();
        if (pixelReader == null) {
            return null;
        }
        int n = (int)image.getWidth();
        int n2 = (int)image.getHeight();
        PixelFormat pixelFormat = pixelReader.getPixelFormat();
        boolean bl = false;
        switch (pixelFormat.getType()) {
            case BYTE_BGRA_PRE: 
            case INT_ARGB_PRE: 
            case BYTE_BGRA: 
            case INT_ARGB: {
                if (bufferedImage == null || bufferedImage.getType() != 4 && bufferedImage.getType() != 1) break;
                bl = UIClientImpl.checkFXImageOpaque(pixelReader, n, n2);
                break;
            }
            case BYTE_RGB: {
                bl = true;
            }
        }
        int n3 = UIClientImpl.getBestBufferedImageType(pixelReader.getPixelFormat(), bufferedImage, bl);
        if (bufferedImage != null) {
            int n4 = bufferedImage.getWidth();
            int n5 = bufferedImage.getHeight();
            if (n4 < n || n5 < n2 || bufferedImage.getType() != n3) {
                bufferedImage = null;
            } else if (n < n4 || n2 < n5) {
                Graphics2D graphics2D = bufferedImage.createGraphics();
                graphics2D.setComposite(AlphaComposite.Clear);
                graphics2D.fillRect(0, 0, n4, n5);
                graphics2D.dispose();
            }
        }
        if (bufferedImage == null) {
            bufferedImage = new BufferedImage(n, n2, n3);
        }
        DataBufferInt dataBufferInt = (DataBufferInt)bufferedImage.getRaster().getDataBuffer();
        int[] nArray = dataBufferInt.getData();
        int n6 = bufferedImage.getRaster().getDataBuffer().getOffset();
        int n7 = 0;
        SampleModel sampleModel = bufferedImage.getRaster().getSampleModel();
        if (sampleModel instanceof SinglePixelPackedSampleModel) {
            n7 = ((SinglePixelPackedSampleModel)sampleModel).getScanlineStride();
        }
        WritablePixelFormat<IntBuffer> writablePixelFormat = UIClientImpl.getAssociatedPixelFormat(bufferedImage);
        pixelReader.getPixels(0, 0, n, n2, writablePixelFormat, nArray, n6, n7);
        return bufferedImage;
    }

    public static BufferedImage toBufferedImage(Image image) {
        try {
            return UIClientImpl.fromFXImage(image, null);
        }
        catch (Exception exception) {
            exception.printStackTrace(System.err);
            return null;
        }
    }

    static {
        FileExtensionInfo.add("video", "Video Files", "*.webm", "*.mp4", "*.ogg");
        FileExtensionInfo.add("audio", "Audio Files", "*.mp3", "*.aac", "*.wav");
        FileExtensionInfo.add("text", "Text Files", "*.txt", "*.csv", "*.text", "*.ttf", "*.sdf", "*.srt", "*.htm", "*.html");
        FileExtensionInfo.add("image", "Image Files", "*.png", "*.jpg", "*.gif", "*.bmp", "*.jpeg");
        DF_DRAG_IMAGE = UIClientImpl.getDataFormat("application/x-java-drag-image");
        DF_DRAG_IMAGE_OFFSET = UIClientImpl.getDataFormat("application/x-java-drag-image-offset");
    }

    private static class FileExtensionInfo {
        private String description;
        private List<String> extensions;

        private FileExtensionInfo() {
        }

        static void add(String string, String string2, String ... stringArray) {
            FileExtensionInfo fileExtensionInfo = new FileExtensionInfo();
            fileExtensionInfo.description = string2;
            fileExtensionInfo.extensions = Arrays.asList(stringArray);
            fileExtensionMap.put(string, fileExtensionInfo);
        }

        private FileChooser.ExtensionFilter getExtensionFilter(String string) {
            String string2 = "*." + string;
            String string3 = this.description + " ";
            if (string.equals("*")) {
                string3 = string3 + this.extensions.stream().collect(Collectors.joining(", ", "(", ")"));
                return new FileChooser.ExtensionFilter(string3, this.extensions);
            }
            if (this.extensions.contains(string2)) {
                string3 = string3 + "(" + string2 + ")";
                return new FileChooser.ExtensionFilter(string3, string2);
            }
            return null;
        }
    }
}

