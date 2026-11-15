/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import com.sun.javafx.event.EventHandlerManager;
import com.sun.javafx.tk.Toolkit;
import java.lang.ref.WeakReference;
import java.util.Optional;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.DialogPane;
import javafx.scene.control.FXDialog;
import javafx.scene.control.HeavyweightDialog;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Callback;

public class Dialog<R>
implements EventTarget {
    final FXDialog dialog;
    private boolean isClosing;
    private ObjectProperty<DialogPane> dialogPane = new SimpleObjectProperty<DialogPane>((Object)this, "dialogPane", new DialogPane()){
        final InvalidationListener expandedListener;
        final InvalidationListener headerListener;
        WeakReference<DialogPane> dialogPaneRef;
        {
            this.expandedListener = observable -> {
                DialogPane dialogPane = Dialog.this.getDialogPane();
                if (dialogPane == null) {
                    return;
                }
                Node node = dialogPane.getExpandableContent();
                boolean bl = node == null ? false : node.isVisible();
                Dialog.this.setResizable(bl);
                Dialog.this.dialog.sizeToScene();
            };
            this.headerListener = observable -> Dialog.this.updatePseudoClassState();
            this.dialogPaneRef = new WeakReference<Object>(null);
        }

        @Override
        protected void invalidated() {
            DialogPane dialogPane;
            DialogPane dialogPane2 = (DialogPane)this.dialogPaneRef.get();
            if (dialogPane2 != null) {
                dialogPane2.expandedProperty().removeListener(this.expandedListener);
                dialogPane2.headerProperty().removeListener(this.headerListener);
                dialogPane2.headerTextProperty().removeListener(this.headerListener);
                dialogPane2.setDialog(null);
            }
            if ((dialogPane = Dialog.this.getDialogPane()) != null) {
                dialogPane.setDialog(Dialog.this);
                dialogPane.getButtonTypes().addListener((ListChangeListener.Change<? super E> change) -> dialogPane.requestLayout());
                dialogPane.expandedProperty().addListener(this.expandedListener);
                dialogPane.headerProperty().addListener(this.headerListener);
                dialogPane.headerTextProperty().addListener(this.headerListener);
                Dialog.this.updatePseudoClassState();
                dialogPane.requestLayout();
            }
            Dialog.this.dialog.setDialogPane(dialogPane);
            this.dialogPaneRef = new WeakReference<DialogPane>(dialogPane);
        }
    };
    private final ObjectProperty<R> resultProperty = new SimpleObjectProperty<R>(){

        @Override
        protected void invalidated() {
            Dialog.this.close();
        }
    };
    private final ObjectProperty<Callback<ButtonType, R>> resultConverterProperty = new SimpleObjectProperty<Callback<ButtonType, R>>(this, "resultConverter");
    private final EventHandlerManager eventHandlerManager = new EventHandlerManager(this);
    private ObjectProperty<EventHandler<DialogEvent>> onShowing;
    private ObjectProperty<EventHandler<DialogEvent>> onShown;
    private ObjectProperty<EventHandler<DialogEvent>> onHiding;
    private ObjectProperty<EventHandler<DialogEvent>> onHidden;
    private ObjectProperty<EventHandler<DialogEvent>> onCloseRequest;
    private static final PseudoClass HEADER_PSEUDO_CLASS = PseudoClass.getPseudoClass("header");
    private static final PseudoClass NO_HEADER_PSEUDO_CLASS = PseudoClass.getPseudoClass("no-header");

    public Dialog() {
        this.dialog = new HeavyweightDialog(this);
        this.setDialogPane(new DialogPane());
        this.initModality(Modality.APPLICATION_MODAL);
    }

    public final void show() {
        Toolkit.getToolkit().checkFxUserThread();
        Event.fireEvent(this, new DialogEvent(this, DialogEvent.DIALOG_SHOWING));
        if (Double.isNaN(this.getWidth()) && Double.isNaN(this.getHeight())) {
            this.dialog.sizeToScene();
        }
        this.dialog.show();
        Event.fireEvent(this, new DialogEvent(this, DialogEvent.DIALOG_SHOWN));
    }

    public final Optional<R> showAndWait() {
        Toolkit.getToolkit().checkFxUserThread();
        if (!Toolkit.getToolkit().canStartNestedEventLoop()) {
            throw new IllegalStateException("showAndWait is not allowed during animation or layout processing");
        }
        Event.fireEvent(this, new DialogEvent(this, DialogEvent.DIALOG_SHOWING));
        if (Double.isNaN(this.getWidth()) && Double.isNaN(this.getHeight())) {
            this.dialog.sizeToScene();
        }
        Event.fireEvent(this, new DialogEvent(this, DialogEvent.DIALOG_SHOWN));
        this.dialog.showAndWait();
        return Optional.ofNullable(this.getResult());
    }

    public final void close() {
        Object object;
        if (this.isClosing) {
            return;
        }
        this.isClosing = true;
        R r = this.getResult();
        if (r == null && !this.dialog.requestPermissionToClose(this)) {
            this.isClosing = false;
            return;
        }
        if (r == null) {
            object = null;
            for (ButtonType buttonType : this.getDialogPane().getButtonTypes()) {
                ButtonBar.ButtonData buttonData = buttonType.getButtonData();
                if (buttonData == null) continue;
                if (buttonData == ButtonBar.ButtonData.CANCEL_CLOSE) {
                    object = buttonType;
                    break;
                }
                if (!buttonData.isCancelButton()) continue;
                object = buttonType;
            }
            this.setResultAndClose((ButtonType)object, false);
        }
        Event.fireEvent(this, new DialogEvent(this, DialogEvent.DIALOG_HIDING));
        object = new DialogEvent(this, DialogEvent.DIALOG_CLOSE_REQUEST);
        Event.fireEvent(this, (Event)object);
        if (((Event)object).isConsumed()) {
            this.isClosing = false;
            return;
        }
        this.dialog.close();
        Event.fireEvent(this, new DialogEvent(this, DialogEvent.DIALOG_HIDDEN));
        this.isClosing = false;
    }

    public final void hide() {
        this.close();
    }

    public final void initModality(Modality modality) {
        this.dialog.initModality(modality);
    }

    public final Modality getModality() {
        return this.dialog.getModality();
    }

    public final void initStyle(StageStyle stageStyle) {
        this.dialog.initStyle(stageStyle);
    }

    public final void initOwner(Window window) {
        this.dialog.initOwner(window);
    }

    public final Window getOwner() {
        return this.dialog.getOwner();
    }

    public final ObjectProperty<DialogPane> dialogPaneProperty() {
        return this.dialogPane;
    }

    public final DialogPane getDialogPane() {
        return (DialogPane)this.dialogPane.get();
    }

    public final void setDialogPane(DialogPane dialogPane) {
        this.dialogPane.set(dialogPane);
    }

    public final StringProperty contentTextProperty() {
        return this.getDialogPane().contentTextProperty();
    }

    public final String getContentText() {
        return this.getDialogPane().getContentText();
    }

    public final void setContentText(String string) {
        this.getDialogPane().setContentText(string);
    }

    public final StringProperty headerTextProperty() {
        return this.getDialogPane().headerTextProperty();
    }

    public final String getHeaderText() {
        return this.getDialogPane().getHeaderText();
    }

    public final void setHeaderText(String string) {
        this.getDialogPane().setHeaderText(string);
    }

    public final ObjectProperty<Node> graphicProperty() {
        return this.getDialogPane().graphicProperty();
    }

    public final Node getGraphic() {
        return this.getDialogPane().getGraphic();
    }

    public final void setGraphic(Node node) {
        this.getDialogPane().setGraphic(node);
    }

    public final ObjectProperty<R> resultProperty() {
        return this.resultProperty;
    }

    public final R getResult() {
        return (R)this.resultProperty().get();
    }

    public final void setResult(R r) {
        this.resultProperty().set(r);
    }

    public final ObjectProperty<Callback<ButtonType, R>> resultConverterProperty() {
        return this.resultConverterProperty;
    }

    public final Callback<ButtonType, R> getResultConverter() {
        return (Callback)this.resultConverterProperty().get();
    }

    public final void setResultConverter(Callback<ButtonType, R> callback) {
        this.resultConverterProperty().set(callback);
    }

    public final ReadOnlyBooleanProperty showingProperty() {
        return this.dialog.showingProperty();
    }

    public final boolean isShowing() {
        return this.showingProperty().get();
    }

    public final BooleanProperty resizableProperty() {
        return this.dialog.resizableProperty();
    }

    public final boolean isResizable() {
        return this.resizableProperty().get();
    }

    public final void setResizable(boolean bl) {
        this.resizableProperty().set(bl);
    }

    public final ReadOnlyDoubleProperty widthProperty() {
        return this.dialog.widthProperty();
    }

    public final double getWidth() {
        return this.widthProperty().get();
    }

    public final void setWidth(double d) {
        this.dialog.setWidth(d);
    }

    public final ReadOnlyDoubleProperty heightProperty() {
        return this.dialog.heightProperty();
    }

    public final double getHeight() {
        return this.heightProperty().get();
    }

    public final void setHeight(double d) {
        this.dialog.setHeight(d);
    }

    public final StringProperty titleProperty() {
        return this.dialog.titleProperty();
    }

    public final String getTitle() {
        return (String)this.dialog.titleProperty().get();
    }

    public final void setTitle(String string) {
        this.dialog.titleProperty().set(string);
    }

    public final double getX() {
        return this.dialog.getX();
    }

    public final void setX(double d) {
        this.dialog.setX(d);
    }

    public final ReadOnlyDoubleProperty xProperty() {
        return this.dialog.xProperty();
    }

    public final double getY() {
        return this.dialog.getY();
    }

    public final void setY(double d) {
        this.dialog.setY(d);
    }

    public final ReadOnlyDoubleProperty yProperty() {
        return this.dialog.yProperty();
    }

    @Override
    public EventDispatchChain buildEventDispatchChain(EventDispatchChain eventDispatchChain) {
        return eventDispatchChain.prepend(this.eventHandlerManager);
    }

    public final void setOnShowing(EventHandler<DialogEvent> eventHandler) {
        this.onShowingProperty().set(eventHandler);
    }

    public final EventHandler<DialogEvent> getOnShowing() {
        return this.onShowing == null ? null : (EventHandler)this.onShowing.get();
    }

    public final ObjectProperty<EventHandler<DialogEvent>> onShowingProperty() {
        if (this.onShowing == null) {
            this.onShowing = new SimpleObjectProperty<EventHandler<DialogEvent>>((Object)this, "onShowing"){

                @Override
                protected void invalidated() {
                    Dialog.this.eventHandlerManager.setEventHandler(DialogEvent.DIALOG_SHOWING, (EventHandler)this.get());
                }
            };
        }
        return this.onShowing;
    }

    public final void setOnShown(EventHandler<DialogEvent> eventHandler) {
        this.onShownProperty().set(eventHandler);
    }

    public final EventHandler<DialogEvent> getOnShown() {
        return this.onShown == null ? null : (EventHandler)this.onShown.get();
    }

    public final ObjectProperty<EventHandler<DialogEvent>> onShownProperty() {
        if (this.onShown == null) {
            this.onShown = new SimpleObjectProperty<EventHandler<DialogEvent>>((Object)this, "onShown"){

                @Override
                protected void invalidated() {
                    Dialog.this.eventHandlerManager.setEventHandler(DialogEvent.DIALOG_SHOWN, (EventHandler)this.get());
                }
            };
        }
        return this.onShown;
    }

    public final void setOnHiding(EventHandler<DialogEvent> eventHandler) {
        this.onHidingProperty().set(eventHandler);
    }

    public final EventHandler<DialogEvent> getOnHiding() {
        return this.onHiding == null ? null : (EventHandler)this.onHiding.get();
    }

    public final ObjectProperty<EventHandler<DialogEvent>> onHidingProperty() {
        if (this.onHiding == null) {
            this.onHiding = new SimpleObjectProperty<EventHandler<DialogEvent>>((Object)this, "onHiding"){

                @Override
                protected void invalidated() {
                    Dialog.this.eventHandlerManager.setEventHandler(DialogEvent.DIALOG_HIDING, (EventHandler)this.get());
                }
            };
        }
        return this.onHiding;
    }

    public final void setOnHidden(EventHandler<DialogEvent> eventHandler) {
        this.onHiddenProperty().set(eventHandler);
    }

    public final EventHandler<DialogEvent> getOnHidden() {
        return this.onHidden == null ? null : (EventHandler)this.onHidden.get();
    }

    public final ObjectProperty<EventHandler<DialogEvent>> onHiddenProperty() {
        if (this.onHidden == null) {
            this.onHidden = new SimpleObjectProperty<EventHandler<DialogEvent>>((Object)this, "onHidden"){

                @Override
                protected void invalidated() {
                    Dialog.this.eventHandlerManager.setEventHandler(DialogEvent.DIALOG_HIDDEN, (EventHandler)this.get());
                }
            };
        }
        return this.onHidden;
    }

    public final void setOnCloseRequest(EventHandler<DialogEvent> eventHandler) {
        this.onCloseRequestProperty().set(eventHandler);
    }

    public final EventHandler<DialogEvent> getOnCloseRequest() {
        return this.onCloseRequest != null ? (EventHandler)this.onCloseRequest.get() : null;
    }

    public final ObjectProperty<EventHandler<DialogEvent>> onCloseRequestProperty() {
        if (this.onCloseRequest == null) {
            this.onCloseRequest = new SimpleObjectProperty<EventHandler<DialogEvent>>((Object)this, "onCloseRequest"){

                @Override
                protected void invalidated() {
                    Dialog.this.eventHandlerManager.setEventHandler(DialogEvent.DIALOG_CLOSE_REQUEST, (EventHandler)this.get());
                }
            };
        }
        return this.onCloseRequest;
    }

    void setResultAndClose(ButtonType buttonType, boolean bl) {
        Callback<ButtonType, R> callback = this.getResultConverter();
        R r = this.getResult();
        ButtonType buttonType2 = null;
        buttonType2 = callback == null ? buttonType : callback.call(buttonType);
        this.setResult(buttonType2);
        if (bl && r == buttonType2) {
            this.close();
        }
    }

    private void updatePseudoClassState() {
        DialogPane dialogPane = this.getDialogPane();
        if (dialogPane != null) {
            boolean bl = this.getDialogPane().hasHeader();
            dialogPane.pseudoClassStateChanged(HEADER_PSEUDO_CLASS, bl);
            dialogPane.pseudoClassStateChanged(NO_HEADER_PSEUDO_CLASS, !bl);
        }
    }
}

