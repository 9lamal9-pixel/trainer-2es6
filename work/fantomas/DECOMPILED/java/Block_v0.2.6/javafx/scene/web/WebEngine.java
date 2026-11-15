/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.web;

import com.sun.glass.ui.Application;
import com.sun.javafx.logging.PlatformLogger;
import com.sun.javafx.scene.web.Debugger;
import com.sun.javafx.scene.web.Printable;
import com.sun.javafx.tk.TKPulseListener;
import com.sun.javafx.tk.Toolkit;
import com.sun.javafx.webkit.Accessor;
import com.sun.javafx.webkit.CursorManagerImpl;
import com.sun.javafx.webkit.EventLoopImpl;
import com.sun.javafx.webkit.ThemeClientImpl;
import com.sun.javafx.webkit.UIClientImpl;
import com.sun.javafx.webkit.UtilitiesImpl;
import com.sun.javafx.webkit.WebPageClientImpl;
import com.sun.javafx.webkit.prism.PrismGraphicsManager;
import com.sun.javafx.webkit.prism.PrismInvoker;
import com.sun.javafx.webkit.prism.theme.PrismRenderer;
import com.sun.javafx.webkit.theme.RenderThemeImpl;
import com.sun.javafx.webkit.theme.Renderer;
import com.sun.webkit.CursorManager;
import com.sun.webkit.Disposer;
import com.sun.webkit.DisposerRecord;
import com.sun.webkit.EventLoop;
import com.sun.webkit.InspectorClient;
import com.sun.webkit.Invoker;
import com.sun.webkit.LoadListenerClient;
import com.sun.webkit.ThemeClient;
import com.sun.webkit.Timer;
import com.sun.webkit.Utilities;
import com.sun.webkit.WebPage;
import com.sun.webkit.graphics.WCGraphicsManager;
import com.sun.webkit.network.URLs;
import com.sun.webkit.network.Util;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Objects;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectPropertyBase;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Rectangle2D;
import javafx.print.PageLayout;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.web.DirectoryLock;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.PromptData;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import org.w3c.dom.Document;

public final class WebEngine {
    private static final PlatformLogger logger;
    private static int instanceCount;
    private final ObjectProperty<WebView> view = new SimpleObjectProperty<WebView>(this, "view");
    private final LoadWorker loadWorker = new LoadWorker();
    private final WebPage page;
    private final SelfDisposer disposer;
    private final DebuggerImpl debugger = new DebuggerImpl();
    private boolean userDataDirectoryApplied = false;
    private final DocumentProperty document = new DocumentProperty();
    private final ReadOnlyStringWrapper location = new ReadOnlyStringWrapper(this, "location");
    private final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper(this, "title");
    private BooleanProperty javaScriptEnabled;
    private StringProperty userStyleSheetLocation;
    private final ObjectProperty<File> userDataDirectory = new SimpleObjectProperty<File>(this, "userDataDirectory");
    private StringProperty userAgent;
    private final ObjectProperty<EventHandler<WebEvent<String>>> onAlert = new SimpleObjectProperty<EventHandler<WebEvent<String>>>(this, "onAlert");
    private final ObjectProperty<EventHandler<WebEvent<String>>> onStatusChanged = new SimpleObjectProperty<EventHandler<WebEvent<String>>>(this, "onStatusChanged");
    private final ObjectProperty<EventHandler<WebEvent<Rectangle2D>>> onResized = new SimpleObjectProperty<EventHandler<WebEvent<Rectangle2D>>>(this, "onResized");
    private final ObjectProperty<EventHandler<WebEvent<Boolean>>> onVisibilityChanged = new SimpleObjectProperty<EventHandler<WebEvent<Boolean>>>(this, "onVisibilityChanged");
    private final ObjectProperty<Callback<PopupFeatures, WebEngine>> createPopupHandler = new SimpleObjectProperty<Callback<PopupFeatures, WebEngine>>(this, "createPopupHandler", popupFeatures -> this);
    private final ObjectProperty<Callback<String, Boolean>> confirmHandler = new SimpleObjectProperty<Callback<String, Boolean>>(this, "confirmHandler");
    private final ObjectProperty<Callback<PromptData, String>> promptHandler = new SimpleObjectProperty<Callback<PromptData, String>>(this, "promptHandler");
    private final ObjectProperty<EventHandler<WebErrorEvent>> onError = new SimpleObjectProperty<EventHandler<WebErrorEvent>>(this, "onError");
    private final WebHistory history;

    public final Worker<Void> getLoadWorker() {
        return this.loadWorker;
    }

    public final Document getDocument() {
        return (Document)this.document.getValue();
    }

    public final ReadOnlyObjectProperty<Document> documentProperty() {
        return this.document;
    }

    public final String getLocation() {
        return this.location.getValue();
    }

    public final ReadOnlyStringProperty locationProperty() {
        return this.location.getReadOnlyProperty();
    }

    private void updateLocation(String string) {
        this.location.set(string);
        this.document.invalidate(false);
        this.title.set(null);
    }

    public final String getTitle() {
        return this.title.getValue();
    }

    public final ReadOnlyStringProperty titleProperty() {
        return this.title.getReadOnlyProperty();
    }

    private void updateTitle() {
        this.title.set(this.page.getTitle(this.page.getMainFrame()));
    }

    public final void setJavaScriptEnabled(boolean bl) {
        this.javaScriptEnabledProperty().set(bl);
    }

    public final boolean isJavaScriptEnabled() {
        return this.javaScriptEnabled == null ? true : this.javaScriptEnabled.get();
    }

    public final BooleanProperty javaScriptEnabledProperty() {
        if (this.javaScriptEnabled == null) {
            this.javaScriptEnabled = new BooleanPropertyBase(true){

                @Override
                public void invalidated() {
                    WebEngine.checkThread();
                    WebEngine.this.page.setJavaScriptEnabled(this.get());
                }

                @Override
                public Object getBean() {
                    return WebEngine.this;
                }

                @Override
                public String getName() {
                    return "javaScriptEnabled";
                }
            };
        }
        return this.javaScriptEnabled;
    }

    public final void setUserStyleSheetLocation(String string) {
        this.userStyleSheetLocationProperty().set(string);
    }

    public final String getUserStyleSheetLocation() {
        return this.userStyleSheetLocation == null ? null : (String)this.userStyleSheetLocation.get();
    }

    private byte[] readFully(BufferedInputStream bufferedInputStream) throws IOException {
        int n;
        int n2 = 0;
        ArrayList<byte[]> arrayList = new ArrayList<byte[]>();
        byte[] byArray = new byte[4096];
        while ((n = bufferedInputStream.read(byArray)) >= 0) {
            byte[] byArray2;
            if (n == byArray.length) {
                byArray2 = byArray;
                byArray = new byte[4096];
            } else {
                byArray2 = new byte[n];
                System.arraycopy(byArray, 0, byArray2, 0, n);
            }
            arrayList.add(byArray2);
            n2 += n;
        }
        byte[] byArray3 = new byte[n2];
        int n3 = 0;
        for (byte[] byArray4 : arrayList) {
            System.arraycopy(byArray4, 0, byArray3, n3, byArray4.length);
            n3 += byArray4.length;
        }
        return byArray3;
    }

    public final StringProperty userStyleSheetLocationProperty() {
        if (this.userStyleSheetLocation == null) {
            this.userStyleSheetLocation = new StringPropertyBase(null){
                private static final String DATA_PREFIX = "data:text/css;charset=utf-8;base64,";

                @Override
                public void invalidated() {
                    Object object;
                    WebEngine.checkThread();
                    String string = this.get();
                    if (string == null || string.length() <= 0) {
                        object = null;
                    } else if (string.startsWith(DATA_PREFIX)) {
                        object = string;
                    } else if (string.startsWith("file:") || string.startsWith("jar:") || string.startsWith("data:")) {
                        try {
                            URLConnection uRLConnection = URLs.newURL(string).openConnection();
                            uRLConnection.connect();
                            BufferedInputStream bufferedInputStream = new BufferedInputStream(uRLConnection.getInputStream());
                            byte[] byArray = WebEngine.this.readFully(bufferedInputStream);
                            String string2 = Base64.getMimeEncoder().encodeToString(byArray);
                            object = DATA_PREFIX + string2;
                        }
                        catch (IOException iOException) {
                            throw new RuntimeException(iOException);
                        }
                    } else {
                        throw new IllegalArgumentException("Invalid stylesheet URL");
                    }
                    WebEngine.this.page.setUserStyleSheetLocation((String)object);
                }

                @Override
                public Object getBean() {
                    return WebEngine.this;
                }

                @Override
                public String getName() {
                    return "userStyleSheetLocation";
                }
            };
        }
        return this.userStyleSheetLocation;
    }

    public final File getUserDataDirectory() {
        return (File)this.userDataDirectory.get();
    }

    public final void setUserDataDirectory(File file) {
        this.userDataDirectory.set(file);
    }

    public final ObjectProperty<File> userDataDirectoryProperty() {
        return this.userDataDirectory;
    }

    public final void setUserAgent(String string) {
        this.userAgentProperty().set(string);
    }

    public final String getUserAgent() {
        return this.userAgent == null ? this.page.getUserAgent() : (String)this.userAgent.get();
    }

    public final StringProperty userAgentProperty() {
        if (this.userAgent == null) {
            this.userAgent = new StringPropertyBase(this.page.getUserAgent()){

                @Override
                public void invalidated() {
                    WebEngine.checkThread();
                    WebEngine.this.page.setUserAgent(this.get());
                }

                @Override
                public Object getBean() {
                    return WebEngine.this;
                }

                @Override
                public String getName() {
                    return "userAgent";
                }
            };
        }
        return this.userAgent;
    }

    public final EventHandler<WebEvent<String>> getOnAlert() {
        return (EventHandler)this.onAlert.get();
    }

    public final void setOnAlert(EventHandler<WebEvent<String>> eventHandler) {
        this.onAlert.set(eventHandler);
    }

    public final ObjectProperty<EventHandler<WebEvent<String>>> onAlertProperty() {
        return this.onAlert;
    }

    public final EventHandler<WebEvent<String>> getOnStatusChanged() {
        return (EventHandler)this.onStatusChanged.get();
    }

    public final void setOnStatusChanged(EventHandler<WebEvent<String>> eventHandler) {
        this.onStatusChanged.set(eventHandler);
    }

    public final ObjectProperty<EventHandler<WebEvent<String>>> onStatusChangedProperty() {
        return this.onStatusChanged;
    }

    public final EventHandler<WebEvent<Rectangle2D>> getOnResized() {
        return (EventHandler)this.onResized.get();
    }

    public final void setOnResized(EventHandler<WebEvent<Rectangle2D>> eventHandler) {
        this.onResized.set(eventHandler);
    }

    public final ObjectProperty<EventHandler<WebEvent<Rectangle2D>>> onResizedProperty() {
        return this.onResized;
    }

    public final EventHandler<WebEvent<Boolean>> getOnVisibilityChanged() {
        return (EventHandler)this.onVisibilityChanged.get();
    }

    public final void setOnVisibilityChanged(EventHandler<WebEvent<Boolean>> eventHandler) {
        this.onVisibilityChanged.set(eventHandler);
    }

    public final ObjectProperty<EventHandler<WebEvent<Boolean>>> onVisibilityChangedProperty() {
        return this.onVisibilityChanged;
    }

    public final Callback<PopupFeatures, WebEngine> getCreatePopupHandler() {
        return (Callback)this.createPopupHandler.get();
    }

    public final void setCreatePopupHandler(Callback<PopupFeatures, WebEngine> callback) {
        this.createPopupHandler.set(callback);
    }

    public final ObjectProperty<Callback<PopupFeatures, WebEngine>> createPopupHandlerProperty() {
        return this.createPopupHandler;
    }

    public final Callback<String, Boolean> getConfirmHandler() {
        return (Callback)this.confirmHandler.get();
    }

    public final void setConfirmHandler(Callback<String, Boolean> callback) {
        this.confirmHandler.set(callback);
    }

    public final ObjectProperty<Callback<String, Boolean>> confirmHandlerProperty() {
        return this.confirmHandler;
    }

    public final Callback<PromptData, String> getPromptHandler() {
        return (Callback)this.promptHandler.get();
    }

    public final void setPromptHandler(Callback<PromptData, String> callback) {
        this.promptHandler.set(callback);
    }

    public final ObjectProperty<Callback<PromptData, String>> promptHandlerProperty() {
        return this.promptHandler;
    }

    public final EventHandler<WebErrorEvent> getOnError() {
        return (EventHandler)this.onError.get();
    }

    public final void setOnError(EventHandler<WebErrorEvent> eventHandler) {
        this.onError.set(eventHandler);
    }

    public final ObjectProperty<EventHandler<WebErrorEvent>> onErrorProperty() {
        return this.onError;
    }

    public WebEngine() {
        this(null, false);
    }

    public WebEngine(String string) {
        this(string, true);
    }

    private WebEngine(String string, boolean bl) {
        WebEngine.checkThread();
        AccessorImpl accessorImpl = new AccessorImpl(this);
        this.page = new WebPage(new WebPageClientImpl(accessorImpl), new UIClientImpl(accessorImpl), null, new InspectorClientImpl(this), new ThemeClientImpl(accessorImpl), false);
        this.page.addLoadListenerClient(new PageLoadListener(this));
        this.history = new WebHistory(this.page);
        this.disposer = new SelfDisposer(this.page);
        Disposer.addRecord(this, this.disposer);
        if (bl) {
            this.load(string);
        }
        if (instanceCount == 0 && Timer.getMode() == Timer.Mode.PLATFORM_TICKS) {
            PulseTimer.start();
        }
        ++instanceCount;
    }

    public void load(String string) {
        WebEngine.checkThread();
        this.loadWorker.cancelAndReset();
        if (string == null || string.equals("") || string.equals("about:blank")) {
            string = "";
        } else {
            try {
                string = Util.adjustUrlForWebKit(string);
            }
            catch (MalformedURLException malformedURLException) {
                this.loadWorker.dispatchLoadEvent(this.getMainFrame(), 0, string, null, 0.0, 0);
                this.loadWorker.dispatchLoadEvent(this.getMainFrame(), 5, string, null, 0.0, 2);
                return;
            }
        }
        this.applyUserDataDirectory();
        this.page.open(this.page.getMainFrame(), string);
    }

    public void loadContent(String string) {
        this.loadContent(string, "text/html");
    }

    public void loadContent(String string, String string2) {
        WebEngine.checkThread();
        this.loadWorker.cancelAndReset();
        this.applyUserDataDirectory();
        this.page.load(this.page.getMainFrame(), string, string2);
    }

    public void reload() {
        WebEngine.checkThread();
        this.page.refresh(this.page.getMainFrame());
    }

    public WebHistory getHistory() {
        return this.history;
    }

    public Object executeScript(String string) {
        WebEngine.checkThread();
        this.applyUserDataDirectory();
        return this.page.executeScript(this.page.getMainFrame(), string);
    }

    private long getMainFrame() {
        return this.page.getMainFrame();
    }

    WebPage getPage() {
        return this.page;
    }

    void setView(WebView webView) {
        this.view.setValue(webView);
    }

    private void stop() {
        WebEngine.checkThread();
        this.page.stop(this.page.getMainFrame());
    }

    private void applyUserDataDirectory() {
        if (this.userDataDirectoryApplied) {
            return;
        }
        this.userDataDirectoryApplied = true;
        File file = this.getUserDataDirectory();
        while (true) {
            Exception exception;
            EventType<WebErrorEvent> eventType;
            String string;
            File file2;
            String string2;
            File file3;
            if (file == null) {
                file3 = WebEngine.defaultUserDataDirectory();
                string2 = String.format("null (%s)", file3);
            } else {
                file3 = file;
                string2 = file3.toString();
            }
            logger.fine("Trying to apply user data directory [{0}]", string2);
            try {
                File[] fileArray;
                file3 = DirectoryLock.canonicalize(file3);
                file2 = new File(file3, "localstorage");
                for (File file4 : fileArray = new File[]{file3, file2}) {
                    WebEngine.createDirectories(file4);
                    File file5 = new File(file4, ".test");
                    if (!file5.createNewFile()) continue;
                    file5.delete();
                }
                this.disposer.userDataDirectoryLock = new DirectoryLock(file3);
                this.page.setLocalStorageDatabasePath(file2.getPath());
                this.page.setLocalStorageEnabled(true);
                logger.fine("User data directory [{0}] has been applied successfully", string2);
                return;
            }
            catch (DirectoryLock.DirectoryAlreadyInUseException directoryAlreadyInUseException) {
                string = "User data directory [%s] is already in use";
                eventType = WebErrorEvent.USER_DATA_DIRECTORY_ALREADY_IN_USE;
                exception = directoryAlreadyInUseException;
            }
            catch (IOException iOException) {
                string = "An I/O error occurred while setting up user data directory [%s]";
                eventType = WebErrorEvent.USER_DATA_DIRECTORY_IO_ERROR;
                exception = iOException;
            }
            catch (SecurityException securityException) {
                string = "A security error occurred while setting up user data directory [%s]";
                eventType = WebErrorEvent.USER_DATA_DIRECTORY_SECURITY_ERROR;
                exception = securityException;
            }
            string = String.format(string, string2);
            logger.fine("{0}, calling error handler", string);
            file2 = file;
            this.fireError(eventType, string, exception);
            file = this.getUserDataDirectory();
            if (Objects.equals(file, file2)) {
                logger.fine("Error handler did not modify user data directory, continuing without user data directory");
                return;
            }
            logger.fine("Error handler has set user data directory to [{0}], retrying", file);
        }
    }

    private static File defaultUserDataDirectory() {
        return new File(Application.GetApplication().getDataDirectory(), "webview");
    }

    private static void createDirectories(File file) throws IOException {
        Path path = file.toPath();
        try {
            Files.createDirectories(path, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------")));
        }
        catch (UnsupportedOperationException unsupportedOperationException) {
            Files.createDirectories(path, new FileAttribute[0]);
        }
    }

    private void fireError(EventType<WebErrorEvent> eventType, String string, Throwable throwable) {
        EventHandler<WebErrorEvent> eventHandler = this.getOnError();
        if (eventHandler != null) {
            eventHandler.handle(new WebErrorEvent(this, eventType, string, throwable));
        }
    }

    void dispose() {
        this.disposer.dispose();
    }

    static void checkThread() {
        Toolkit.getToolkit().checkFxUserThread();
    }

    Debugger getDebugger() {
        return this.debugger;
    }

    private static final boolean printStatusOK(PrinterJob printerJob) {
        switch (printerJob.getJobStatus()) {
            case NOT_STARTED: 
            case PRINTING: {
                return true;
            }
        }
        return false;
    }

    public void print(PrinterJob printerJob) {
        if (!WebEngine.printStatusOK(printerJob)) {
            return;
        }
        PageLayout pageLayout = printerJob.getJobSettings().getPageLayout();
        float f = (float)pageLayout.getPrintableWidth();
        float f2 = (float)pageLayout.getPrintableHeight();
        int n = this.page.beginPrinting(f, f2);
        for (int i = 0; i < n; ++i) {
            if (!WebEngine.printStatusOK(printerJob)) continue;
            Printable printable = new Printable(this.page, i, f);
            printerJob.printPage(printable);
        }
        this.page.endPrinting();
    }

    static {
        Accessor.setPageAccessor(webEngine -> webEngine == null ? null : webEngine.getPage());
        Invoker.setInvoker(new PrismInvoker());
        Renderer.setRenderer(new PrismRenderer());
        WCGraphicsManager.setGraphicsManager(new PrismGraphicsManager());
        CursorManager.setCursorManager(new CursorManagerImpl());
        EventLoop.setEventLoop(new EventLoopImpl());
        ThemeClient.setDefaultRenderTheme(new RenderThemeImpl());
        Utilities.setUtilities(new UtilitiesImpl());
        logger = PlatformLogger.getLogger(WebEngine.class.getName());
        instanceCount = 0;
    }

    private static final class InspectorClientImpl
    implements InspectorClient {
        private final WeakReference<WebEngine> engine;

        private InspectorClientImpl(WebEngine webEngine) {
            this.engine = new WeakReference<WebEngine>(webEngine);
        }

        @Override
        public boolean sendMessageToFrontend(String string) {
            Callback callback;
            boolean bl = false;
            WebEngine webEngine = (WebEngine)this.engine.get();
            if (webEngine != null && (callback = webEngine.debugger.messageCallback) != null) {
                AccessController.doPrivileged(() -> {
                    callback.call(string);
                    return null;
                }, webEngine.page.getAccessControlContext());
                bl = true;
            }
            return bl;
        }
    }

    private final class DebuggerImpl
    implements Debugger {
        private boolean enabled;
        private Callback<String, Void> messageCallback;

        private DebuggerImpl() {
        }

        @Override
        public boolean isEnabled() {
            WebEngine.checkThread();
            return this.enabled;
        }

        @Override
        public void setEnabled(boolean bl) {
            WebEngine.checkThread();
            if (bl != this.enabled) {
                if (bl) {
                    WebEngine.this.page.setDeveloperExtrasEnabled(true);
                    WebEngine.this.page.connectInspectorFrontend();
                } else {
                    WebEngine.this.page.disconnectInspectorFrontend();
                    WebEngine.this.page.setDeveloperExtrasEnabled(false);
                }
                this.enabled = bl;
            }
        }

        @Override
        public void sendMessage(String string) {
            WebEngine.checkThread();
            if (!this.enabled) {
                throw new IllegalStateException("Debugger is not enabled");
            }
            if (string == null) {
                throw new NullPointerException("message is null");
            }
            WebEngine.this.page.dispatchInspectorMessageFromFrontend(string);
        }

        @Override
        public Callback<String, Void> getMessageCallback() {
            WebEngine.checkThread();
            return this.messageCallback;
        }

        @Override
        public void setMessageCallback(Callback<String, Void> callback) {
            WebEngine.checkThread();
            this.messageCallback = callback;
        }
    }

    private final class DocumentProperty
    extends ReadOnlyObjectPropertyBase<Document> {
        private boolean available;
        private Document document;

        private DocumentProperty() {
        }

        private void invalidate(boolean bl) {
            if (this.available || bl) {
                this.available = bl;
                this.document = null;
                this.fireValueChangedEvent();
            }
        }

        @Override
        public Document get() {
            if (!this.available) {
                return null;
            }
            if (this.document == null) {
                this.document = WebEngine.this.page.getDocument(WebEngine.this.page.getMainFrame());
                if (this.document == null) {
                    this.available = false;
                }
            }
            return this.document;
        }

        @Override
        public Object getBean() {
            return WebEngine.this;
        }

        @Override
        public String getName() {
            return "document";
        }
    }

    private final class LoadWorker
    implements Worker<Void> {
        private final ReadOnlyObjectWrapper<Worker.State> state = new ReadOnlyObjectWrapper<Worker.State>(this, "state", Worker.State.READY);
        private final ReadOnlyObjectWrapper<Void> value = new ReadOnlyObjectWrapper<Object>(this, "value", null);
        private final ReadOnlyObjectWrapper<Throwable> exception = new ReadOnlyObjectWrapper(this, "exception");
        private final ReadOnlyDoubleWrapper workDone = new ReadOnlyDoubleWrapper(this, "workDone", -1.0);
        private final ReadOnlyDoubleWrapper totalWorkToBeDone = new ReadOnlyDoubleWrapper(this, "totalWork", -1.0);
        private final ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper(this, "progress", -1.0);
        private final ReadOnlyBooleanWrapper running = new ReadOnlyBooleanWrapper(this, "running", false);
        private final ReadOnlyStringWrapper message = new ReadOnlyStringWrapper(this, "message", "");
        private final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper(this, "title", "WebEngine Loader");

        private LoadWorker() {
        }

        @Override
        public final Worker.State getState() {
            WebEngine.checkThread();
            return (Worker.State)((Object)this.state.get());
        }

        @Override
        public final ReadOnlyObjectProperty<Worker.State> stateProperty() {
            WebEngine.checkThread();
            return this.state.getReadOnlyProperty();
        }

        private void updateState(Worker.State state) {
            WebEngine.checkThread();
            this.state.set(state);
            this.running.set(state == Worker.State.SCHEDULED || state == Worker.State.RUNNING);
        }

        @Override
        public final Void getValue() {
            WebEngine.checkThread();
            return (Void)this.value.get();
        }

        @Override
        public final ReadOnlyObjectProperty<Void> valueProperty() {
            WebEngine.checkThread();
            return this.value.getReadOnlyProperty();
        }

        @Override
        public final Throwable getException() {
            WebEngine.checkThread();
            return (Throwable)this.exception.get();
        }

        @Override
        public final ReadOnlyObjectProperty<Throwable> exceptionProperty() {
            WebEngine.checkThread();
            return this.exception.getReadOnlyProperty();
        }

        @Override
        public final double getWorkDone() {
            WebEngine.checkThread();
            return this.workDone.get();
        }

        @Override
        public final ReadOnlyDoubleProperty workDoneProperty() {
            WebEngine.checkThread();
            return this.workDone.getReadOnlyProperty();
        }

        @Override
        public final double getTotalWork() {
            WebEngine.checkThread();
            return this.totalWorkToBeDone.get();
        }

        @Override
        public final ReadOnlyDoubleProperty totalWorkProperty() {
            WebEngine.checkThread();
            return this.totalWorkToBeDone.getReadOnlyProperty();
        }

        @Override
        public final double getProgress() {
            WebEngine.checkThread();
            return this.progress.get();
        }

        @Override
        public final ReadOnlyDoubleProperty progressProperty() {
            WebEngine.checkThread();
            return this.progress.getReadOnlyProperty();
        }

        private void updateProgress(double d) {
            this.totalWorkToBeDone.set(100.0);
            this.workDone.set(d * 100.0);
            this.progress.set(d);
        }

        @Override
        public final boolean isRunning() {
            WebEngine.checkThread();
            return this.running.get();
        }

        @Override
        public final ReadOnlyBooleanProperty runningProperty() {
            WebEngine.checkThread();
            return this.running.getReadOnlyProperty();
        }

        @Override
        public final String getMessage() {
            return this.message.get();
        }

        @Override
        public final ReadOnlyStringProperty messageProperty() {
            return this.message.getReadOnlyProperty();
        }

        @Override
        public final String getTitle() {
            return this.title.get();
        }

        @Override
        public final ReadOnlyStringProperty titleProperty() {
            return this.title.getReadOnlyProperty();
        }

        @Override
        public boolean cancel() {
            if (this.isRunning()) {
                WebEngine.this.stop();
                return true;
            }
            return false;
        }

        private void cancelAndReset() {
            this.cancel();
            this.exception.set(null);
            this.message.set("");
            this.totalWorkToBeDone.set(-1.0);
            this.workDone.set(-1.0);
            this.progress.set(-1.0);
            this.updateState(Worker.State.READY);
            this.running.set(false);
        }

        private void dispatchLoadEvent(long l, int n, String string, String string2, double d, int n2) {
            if (l != WebEngine.this.getMainFrame()) {
                return;
            }
            switch (n) {
                case 0: {
                    this.message.set("Loading " + string);
                    WebEngine.this.updateLocation(string);
                    this.updateProgress(0.0);
                    this.updateState(Worker.State.SCHEDULED);
                    this.updateState(Worker.State.RUNNING);
                    break;
                }
                case 2: {
                    this.message.set("Loading " + string);
                    WebEngine.this.updateLocation(string);
                    break;
                }
                case 3: {
                    this.message.set("Replaced " + string);
                    WebEngine.this.location.set(string);
                    break;
                }
                case 1: {
                    this.message.set("Loading complete");
                    this.updateProgress(1.0);
                    this.updateState(Worker.State.SUCCEEDED);
                    break;
                }
                case 5: {
                    this.message.set("Loading failed");
                    this.exception.set(this.describeError(n2));
                    this.updateState(Worker.State.FAILED);
                    break;
                }
                case 6: {
                    this.message.set("Loading stopped");
                    this.updateState(Worker.State.CANCELLED);
                    break;
                }
                case 30: {
                    this.updateProgress(d);
                    break;
                }
                case 11: {
                    WebEngine.this.updateTitle();
                    break;
                }
                case 14: {
                    if (this.state.get() != Worker.State.RUNNING) {
                        this.dispatchLoadEvent(l, 0, string, string2, d, n2);
                    }
                    WebEngine.this.document.invalidate(true);
                }
            }
        }

        private Throwable describeError(int n) {
            String string = "Unknown error";
            switch (n) {
                case 1: {
                    string = "Unknown host";
                    break;
                }
                case 2: {
                    string = "Malformed URL";
                    break;
                }
                case 3: {
                    string = "SSL handshake failed";
                    break;
                }
                case 4: {
                    string = "Connection refused by server";
                    break;
                }
                case 5: {
                    string = "Connection reset by server";
                    break;
                }
                case 6: {
                    string = "No route to host";
                    break;
                }
                case 7: {
                    string = "Connection timed out";
                    break;
                }
                case 8: {
                    string = "Permission denied";
                    break;
                }
                case 9: {
                    string = "Invalid response from server";
                    break;
                }
                case 10: {
                    string = "Too many redirects";
                    break;
                }
                case 11: {
                    string = "File not found";
                }
            }
            return new Throwable(string);
        }
    }

    private static final class PageLoadListener
    implements LoadListenerClient {
        private final WeakReference<WebEngine> engine;

        private PageLoadListener(WebEngine webEngine) {
            this.engine = new WeakReference<WebEngine>(webEngine);
        }

        @Override
        public void dispatchLoadEvent(long l, int n, String string, String string2, double d, int n2) {
            WebEngine webEngine = (WebEngine)this.engine.get();
            if (webEngine != null) {
                webEngine.loadWorker.dispatchLoadEvent(l, n, string, string2, d, n2);
            }
        }

        @Override
        public void dispatchResourceLoadEvent(long l, int n, String string, String string2, double d, int n2) {
        }
    }

    private static final class PulseTimer {
        private static final AnimationTimer animation = new AnimationTimer(){

            @Override
            public void handle(long l) {
            }
        };
        private static final TKPulseListener listener = () -> Platform.runLater(() -> Timer.getTimer().notifyTick());

        private PulseTimer() {
        }

        private static void start() {
            Toolkit.getToolkit().addSceneTkPulseListener(listener);
            animation.start();
        }

        private static void stop() {
            Toolkit.getToolkit().removeSceneTkPulseListener(listener);
            animation.stop();
        }
    }

    private static final class AccessorImpl
    extends Accessor {
        private final WeakReference<WebEngine> engine;

        private AccessorImpl(WebEngine webEngine) {
            this.engine = new WeakReference<WebEngine>(webEngine);
        }

        @Override
        public WebEngine getEngine() {
            return (WebEngine)this.engine.get();
        }

        @Override
        public WebPage getPage() {
            WebEngine webEngine = this.getEngine();
            return webEngine == null ? null : webEngine.page;
        }

        @Override
        public WebView getView() {
            WebEngine webEngine = this.getEngine();
            return webEngine == null ? null : (WebView)webEngine.view.get();
        }

        @Override
        public void addChild(Node node) {
            WebView webView = this.getView();
            if (webView != null) {
                webView.getChildren().add(node);
            }
        }

        @Override
        public void removeChild(Node node) {
            WebView webView = this.getView();
            if (webView != null) {
                webView.getChildren().remove(node);
            }
        }

        @Override
        public void addViewListener(InvalidationListener invalidationListener) {
            WebEngine webEngine = this.getEngine();
            if (webEngine != null) {
                webEngine.view.addListener(invalidationListener);
            }
        }
    }

    private static final class SelfDisposer
    implements DisposerRecord {
        private WebPage page;
        private DirectoryLock userDataDirectoryLock;

        private SelfDisposer(WebPage webPage) {
            this.page = webPage;
        }

        @Override
        public void dispose() {
            if (this.page == null) {
                return;
            }
            this.page.dispose();
            this.page = null;
            if (this.userDataDirectoryLock != null) {
                this.userDataDirectoryLock.close();
            }
            instanceCount--;
            if (instanceCount == 0 && Timer.getMode() == Timer.Mode.PLATFORM_TICKS) {
                PulseTimer.stop();
            }
        }
    }
}

