/*
 * Decompiled with CFR 0.152.
 */
package javafx.stage;

import com.sun.javafx.FXPermissions;
import com.sun.javafx.collections.TrackableObservableList;
import com.sun.javafx.collections.VetoableListDecorator;
import com.sun.javafx.scene.SceneHelper;
import com.sun.javafx.stage.StageHelper;
import com.sun.javafx.stage.StagePeerListener;
import com.sun.javafx.tk.TKStage;
import com.sun.javafx.tk.Toolkit;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.NamedArg;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.NodeOrientation;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class Stage
extends Window {
    private boolean inNestedEventLoop = false;
    private static final StagePeerListener.StageAccessor STAGE_ACCESSOR;
    private boolean primary = false;
    private boolean securityDialog = false;
    private boolean important = true;
    private StageStyle style;
    private Modality modality = Modality.NONE;
    private Window owner = null;
    private ReadOnlyBooleanWrapper fullScreen;
    private ObservableList<Image> icons = new VetoableListDecorator<Image>((ObservableList)new TrackableObservableList<Image>(){

        @Override
        protected void onChanged(ListChangeListener.Change<Image> change) {
            ArrayList<Object> arrayList = new ArrayList<Object>();
            for (Image image : Stage.this.icons) {
                arrayList.add(Toolkit.getImageAccessor().getPlatformImage(image));
            }
            if (Stage.this.getPeer() != null) {
                Stage.this.getPeer().setIcons(arrayList);
            }
        }
    }){

        @Override
        protected void onProposedChange(List<Image> list, int[] nArray) {
            for (Image image : list) {
                if (image != null) continue;
                throw new NullPointerException("icon can not be null.");
            }
        }
    };
    private StringProperty title;
    private ReadOnlyBooleanWrapper iconified;
    private ReadOnlyBooleanWrapper maximized;
    private ReadOnlyBooleanWrapper alwaysOnTop;
    private BooleanProperty resizable;
    private DoubleProperty minWidth;
    private DoubleProperty minHeight;
    private DoubleProperty maxWidth;
    private DoubleProperty maxHeight;
    private final ObjectProperty<KeyCombination> fullScreenExitCombination = new SimpleObjectProperty<Object>(this, "fullScreenExitCombination", null);
    private final ObjectProperty<String> fullScreenExitHint = new SimpleObjectProperty<Object>(this, "fullScreenExitHint", null);

    public Stage() {
        this(StageStyle.DECORATED);
    }

    public Stage(@NamedArg(value="style", defaultValue="DECORATED") StageStyle stageStyle) {
        Toolkit.getToolkit().checkFxUserThread();
        this.initStyle(stageStyle);
        StageHelper.initHelper(this);
    }

    @Override
    public final void setScene(Scene scene) {
        Toolkit.getToolkit().checkFxUserThread();
        super.setScene(scene);
    }

    @Override
    public final void show() {
        super.show();
    }

    final void initSecurityDialog(boolean bl) {
        if (this.hasBeenVisible) {
            throw new IllegalStateException("Cannot set securityDialog once stage has been set visible");
        }
        this.securityDialog = bl;
    }

    final boolean isSecurityDialog() {
        return this.securityDialog;
    }

    void setPrimary(boolean bl) {
        this.primary = bl;
    }

    boolean isPrimary() {
        return this.primary;
    }

    void setImportant(boolean bl) {
        this.important = bl;
    }

    private boolean isImportant() {
        return this.important;
    }

    public void showAndWait() {
        Toolkit.getToolkit().checkFxUserThread();
        if (this.isPrimary()) {
            throw new IllegalStateException("Cannot call this method on primary stage");
        }
        if (this.isShowing()) {
            throw new IllegalStateException("Stage already visible");
        }
        if (!Toolkit.getToolkit().canStartNestedEventLoop()) {
            throw new IllegalStateException("showAndWait is not allowed during animation or layout processing");
        }
        assert (!this.inNestedEventLoop);
        this.show();
        this.inNestedEventLoop = true;
        Toolkit.getToolkit().enterNestedEventLoop(this);
    }

    public final void initStyle(StageStyle stageStyle) {
        if (this.hasBeenVisible) {
            throw new IllegalStateException("Cannot set style once stage has been set visible");
        }
        this.style = stageStyle;
    }

    public final StageStyle getStyle() {
        return this.style;
    }

    public final void initModality(Modality modality) {
        if (this.hasBeenVisible) {
            throw new IllegalStateException("Cannot set modality once stage has been set visible");
        }
        if (this.isPrimary()) {
            throw new IllegalStateException("Cannot set modality for the primary stage");
        }
        this.modality = modality;
    }

    public final Modality getModality() {
        return this.modality;
    }

    public final void initOwner(Window window) {
        if (this.hasBeenVisible) {
            throw new IllegalStateException("Cannot set owner once stage has been set visible");
        }
        if (this.isPrimary()) {
            throw new IllegalStateException("Cannot set owner for the primary stage");
        }
        this.owner = window;
        Scene scene = this.getScene();
        if (scene != null) {
            SceneHelper.parentEffectiveOrientationInvalidated(scene);
        }
    }

    public final Window getOwner() {
        return this.owner;
    }

    public final void setFullScreen(boolean bl) {
        Toolkit.getToolkit().checkFxUserThread();
        this.fullScreenPropertyImpl().set(bl);
        if (this.getPeer() != null) {
            this.getPeer().setFullScreen(bl);
        }
    }

    public final boolean isFullScreen() {
        return this.fullScreen == null ? false : this.fullScreen.get();
    }

    public final ReadOnlyBooleanProperty fullScreenProperty() {
        return this.fullScreenPropertyImpl().getReadOnlyProperty();
    }

    private ReadOnlyBooleanWrapper fullScreenPropertyImpl() {
        if (this.fullScreen == null) {
            this.fullScreen = new ReadOnlyBooleanWrapper(this, "fullScreen");
        }
        return this.fullScreen;
    }

    public final ObservableList<Image> getIcons() {
        return this.icons;
    }

    public final void setTitle(String string) {
        this.titleProperty().set(string);
    }

    public final String getTitle() {
        return this.title == null ? null : (String)this.title.get();
    }

    public final StringProperty titleProperty() {
        if (this.title == null) {
            this.title = new StringPropertyBase(){

                @Override
                protected void invalidated() {
                    if (Stage.this.getPeer() != null) {
                        Stage.this.getPeer().setTitle(this.get());
                    }
                }

                @Override
                public Object getBean() {
                    return Stage.this;
                }

                @Override
                public String getName() {
                    return "title";
                }
            };
        }
        return this.title;
    }

    public final void setIconified(boolean bl) {
        this.iconifiedPropertyImpl().set(bl);
        if (this.getPeer() != null) {
            this.getPeer().setIconified(bl);
        }
    }

    public final boolean isIconified() {
        return this.iconified == null ? false : this.iconified.get();
    }

    public final ReadOnlyBooleanProperty iconifiedProperty() {
        return this.iconifiedPropertyImpl().getReadOnlyProperty();
    }

    private final ReadOnlyBooleanWrapper iconifiedPropertyImpl() {
        if (this.iconified == null) {
            this.iconified = new ReadOnlyBooleanWrapper(this, "iconified");
        }
        return this.iconified;
    }

    public final void setMaximized(boolean bl) {
        this.maximizedPropertyImpl().set(bl);
        if (this.getPeer() != null) {
            this.getPeer().setMaximized(bl);
        }
    }

    public final boolean isMaximized() {
        return this.maximized == null ? false : this.maximized.get();
    }

    public final ReadOnlyBooleanProperty maximizedProperty() {
        return this.maximizedPropertyImpl().getReadOnlyProperty();
    }

    private final ReadOnlyBooleanWrapper maximizedPropertyImpl() {
        if (this.maximized == null) {
            this.maximized = new ReadOnlyBooleanWrapper(this, "maximized");
        }
        return this.maximized;
    }

    public final void setAlwaysOnTop(boolean bl) {
        this.alwaysOnTopPropertyImpl().set(bl);
        if (this.getPeer() != null) {
            this.getPeer().setAlwaysOnTop(bl);
        }
    }

    public final boolean isAlwaysOnTop() {
        return this.alwaysOnTop == null ? false : this.alwaysOnTop.get();
    }

    public final ReadOnlyBooleanProperty alwaysOnTopProperty() {
        return this.alwaysOnTopPropertyImpl().getReadOnlyProperty();
    }

    private ReadOnlyBooleanWrapper alwaysOnTopPropertyImpl() {
        if (this.alwaysOnTop == null) {
            this.alwaysOnTop = new ReadOnlyBooleanWrapper(this, "alwaysOnTop");
        }
        return this.alwaysOnTop;
    }

    public final void setResizable(boolean bl) {
        this.resizableProperty().set(bl);
    }

    public final boolean isResizable() {
        return this.resizable == null ? true : this.resizable.get();
    }

    public final BooleanProperty resizableProperty() {
        if (this.resizable == null) {
            this.resizable = new ResizableProperty();
        }
        return this.resizable;
    }

    public final void setMinWidth(double d) {
        this.minWidthProperty().set(d);
    }

    public final double getMinWidth() {
        return this.minWidth == null ? 0.0 : this.minWidth.get();
    }

    public final DoubleProperty minWidthProperty() {
        if (this.minWidth == null) {
            this.minWidth = new DoublePropertyBase(0.0){

                @Override
                protected void invalidated() {
                    if (Stage.this.getPeer() != null) {
                        Stage.this.getPeer().setMinimumSize((int)Math.ceil(this.get()), (int)Math.ceil(Stage.this.getMinHeight()));
                    }
                    if (Stage.this.getWidth() < Stage.this.getMinWidth()) {
                        Stage.this.setWidth(Stage.this.getMinWidth());
                    }
                }

                @Override
                public Object getBean() {
                    return Stage.this;
                }

                @Override
                public String getName() {
                    return "minWidth";
                }
            };
        }
        return this.minWidth;
    }

    public final void setMinHeight(double d) {
        this.minHeightProperty().set(d);
    }

    public final double getMinHeight() {
        return this.minHeight == null ? 0.0 : this.minHeight.get();
    }

    public final DoubleProperty minHeightProperty() {
        if (this.minHeight == null) {
            this.minHeight = new DoublePropertyBase(0.0){

                @Override
                protected void invalidated() {
                    if (Stage.this.getPeer() != null) {
                        Stage.this.getPeer().setMinimumSize((int)Math.ceil(Stage.this.getMinWidth()), (int)Math.ceil(this.get()));
                    }
                    if (Stage.this.getHeight() < Stage.this.getMinHeight()) {
                        Stage.this.setHeight(Stage.this.getMinHeight());
                    }
                }

                @Override
                public Object getBean() {
                    return Stage.this;
                }

                @Override
                public String getName() {
                    return "minHeight";
                }
            };
        }
        return this.minHeight;
    }

    public final void setMaxWidth(double d) {
        this.maxWidthProperty().set(d);
    }

    public final double getMaxWidth() {
        return this.maxWidth == null ? Double.MAX_VALUE : this.maxWidth.get();
    }

    public final DoubleProperty maxWidthProperty() {
        if (this.maxWidth == null) {
            this.maxWidth = new DoublePropertyBase(Double.MAX_VALUE){

                @Override
                protected void invalidated() {
                    if (Stage.this.getPeer() != null) {
                        Stage.this.getPeer().setMaximumSize((int)Math.floor(this.get()), (int)Math.floor(Stage.this.getMaxHeight()));
                    }
                    if (Stage.this.getWidth() > Stage.this.getMaxWidth()) {
                        Stage.this.setWidth(Stage.this.getMaxWidth());
                    }
                }

                @Override
                public Object getBean() {
                    return Stage.this;
                }

                @Override
                public String getName() {
                    return "maxWidth";
                }
            };
        }
        return this.maxWidth;
    }

    public final void setMaxHeight(double d) {
        this.maxHeightProperty().set(d);
    }

    public final double getMaxHeight() {
        return this.maxHeight == null ? Double.MAX_VALUE : this.maxHeight.get();
    }

    public final DoubleProperty maxHeightProperty() {
        if (this.maxHeight == null) {
            this.maxHeight = new DoublePropertyBase(Double.MAX_VALUE){

                @Override
                protected void invalidated() {
                    if (Stage.this.getPeer() != null) {
                        Stage.this.getPeer().setMaximumSize((int)Math.floor(Stage.this.getMaxWidth()), (int)Math.floor(this.get()));
                    }
                    if (Stage.this.getHeight() > Stage.this.getMaxHeight()) {
                        Stage.this.setHeight(Stage.this.getMaxHeight());
                    }
                }

                @Override
                public Object getBean() {
                    return Stage.this;
                }

                @Override
                public String getName() {
                    return "maxHeight";
                }
            };
        }
        return this.maxHeight;
    }

    private void doVisibleChanging(boolean bl) {
        Toolkit toolkit = Toolkit.getToolkit();
        if (bl && this.getPeer() == null) {
            SecurityManager securityManager;
            Window window = this.getOwner();
            TKStage tKStage = window == null ? null : window.getPeer();
            Scene scene = this.getScene();
            boolean bl2 = scene != null && scene.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT;
            StageStyle stageStyle = this.getStyle();
            if (stageStyle == StageStyle.TRANSPARENT && (securityManager = System.getSecurityManager()) != null) {
                try {
                    securityManager.checkPermission(FXPermissions.CREATE_TRANSPARENT_WINDOW_PERMISSION);
                }
                catch (SecurityException securityException) {
                    stageStyle = StageStyle.UNDECORATED;
                }
            }
            this.setPeer(toolkit.createTKStage(this, this.isSecurityDialog(), stageStyle, this.isPrimary(), this.getModality(), tKStage, bl2, this.acc));
            this.getPeer().setMinimumSize((int)Math.ceil(this.getMinWidth()), (int)Math.ceil(this.getMinHeight()));
            this.getPeer().setMaximumSize((int)Math.floor(this.getMaxWidth()), (int)Math.floor(this.getMaxHeight()));
            this.setPeerListener(new StagePeerListener(this, STAGE_ACCESSOR));
        }
    }

    private void doVisibleChanged(boolean bl) {
        if (bl) {
            TKStage tKStage = this.getPeer();
            tKStage.setImportant(this.isImportant());
            tKStage.setResizable(this.isResizable());
            tKStage.setFullScreen(this.isFullScreen());
            tKStage.setAlwaysOnTop(this.isAlwaysOnTop());
            tKStage.setIconified(this.isIconified());
            tKStage.setMaximized(this.isMaximized());
            tKStage.setTitle(this.getTitle());
            ArrayList<Object> arrayList = new ArrayList<Object>();
            for (Image image : this.icons) {
                arrayList.add(Toolkit.getImageAccessor().getPlatformImage(image));
            }
            if (tKStage != null) {
                tKStage.setIcons(arrayList);
            }
        }
        if (!bl && this.inNestedEventLoop) {
            this.inNestedEventLoop = false;
            Toolkit.getToolkit().exitNestedEventLoop(this, null);
        }
    }

    public void toFront() {
        if (this.getPeer() != null) {
            this.getPeer().toFront();
        }
    }

    public void toBack() {
        if (this.getPeer() != null) {
            this.getPeer().toBack();
        }
    }

    public void close() {
        this.hide();
    }

    @Override
    Window getWindowOwner() {
        return this.getOwner();
    }

    public final void setFullScreenExitKeyCombination(KeyCombination keyCombination) {
        this.fullScreenExitCombination.set(keyCombination);
    }

    public final KeyCombination getFullScreenExitKeyCombination() {
        return (KeyCombination)this.fullScreenExitCombination.get();
    }

    public final ObjectProperty<KeyCombination> fullScreenExitKeyProperty() {
        return this.fullScreenExitCombination;
    }

    public final void setFullScreenExitHint(String string) {
        this.fullScreenExitHint.set(string);
    }

    public final String getFullScreenExitHint() {
        return (String)this.fullScreenExitHint.get();
    }

    public final ObjectProperty<String> fullScreenExitHintProperty() {
        return this.fullScreenExitHint;
    }

    static {
        StageHelper.setStageAccessor(new StageHelper.StageAccessor(){

            @Override
            public void doVisibleChanging(Window window, boolean bl) {
                ((Stage)window).doVisibleChanging(bl);
            }

            @Override
            public void doVisibleChanged(Window window, boolean bl) {
                ((Stage)window).doVisibleChanged(bl);
            }

            @Override
            public void initSecurityDialog(Stage stage, boolean bl) {
                stage.initSecurityDialog(bl);
            }

            @Override
            public void setPrimary(Stage stage, boolean bl) {
                stage.setPrimary(bl);
            }

            @Override
            public void setImportant(Stage stage, boolean bl) {
                stage.setImportant(bl);
            }
        });
        STAGE_ACCESSOR = new StagePeerListener.StageAccessor(){

            @Override
            public void setIconified(Stage stage, boolean bl) {
                stage.iconifiedPropertyImpl().set(bl);
            }

            @Override
            public void setMaximized(Stage stage, boolean bl) {
                stage.maximizedPropertyImpl().set(bl);
            }

            @Override
            public void setResizable(Stage stage, boolean bl) {
                ((ResizableProperty)stage.resizableProperty()).setNoInvalidate(bl);
            }

            @Override
            public void setFullScreen(Stage stage, boolean bl) {
                stage.fullScreenPropertyImpl().set(bl);
            }

            @Override
            public void setAlwaysOnTop(Stage stage, boolean bl) {
                stage.alwaysOnTopPropertyImpl().set(bl);
            }
        };
    }

    private class ResizableProperty
    extends SimpleBooleanProperty {
        private boolean noInvalidate;

        public ResizableProperty() {
            super(Stage.this, "resizable", true);
        }

        void setNoInvalidate(boolean bl) {
            this.noInvalidate = true;
            this.set(bl);
            this.noInvalidate = false;
        }

        @Override
        protected void invalidated() {
            if (this.noInvalidate) {
                return;
            }
            if (Stage.this.getPeer() != null) {
                Stage.this.applyBounds();
                Stage.this.getPeer().setResizable(this.get());
            }
        }

        @Override
        public void bind(ObservableValue<? extends Boolean> observableValue) {
            throw new RuntimeException("Resizable property cannot be bound");
        }
    }
}

