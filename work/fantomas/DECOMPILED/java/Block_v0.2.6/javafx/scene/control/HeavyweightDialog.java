/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.FXDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

class HeavyweightDialog
extends FXDialog {
    final Stage stage = new Stage(){

        @Override
        public void centerOnScreen() {
            Window window = HeavyweightDialog.this.getOwner();
            if (window != null) {
                HeavyweightDialog.this.positionStage();
            } else if (this.getWidth() > 0.0 && this.getHeight() > 0.0) {
                super.centerOnScreen();
            }
        }
    };
    private Scene scene;
    private final Parent DUMMY_ROOT = new Region();
    private final Dialog<?> dialog;
    private DialogPane dialogPane;
    private double prefX = Double.NaN;
    private double prefY = Double.NaN;

    HeavyweightDialog(Dialog<?> dialog) {
        this.dialog = dialog;
        this.stage.setResizable(false);
        this.stage.setOnCloseRequest(windowEvent -> {
            if (this.requestPermissionToClose(dialog)) {
                dialog.close();
            } else {
                windowEvent.consume();
            }
        });
        this.stage.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ESCAPE && !keyEvent.isConsumed() && this.requestPermissionToClose(dialog)) {
                dialog.close();
                keyEvent.consume();
            }
        });
    }

    @Override
    void initStyle(StageStyle stageStyle) {
        this.stage.initStyle(stageStyle);
    }

    @Override
    StageStyle getStyle() {
        return this.stage.getStyle();
    }

    @Override
    public void initOwner(Window window) {
        this.updateStageBindings(this.stage.getOwner(), window);
        this.stage.initOwner(window);
    }

    @Override
    public Window getOwner() {
        return this.stage.getOwner();
    }

    @Override
    public void initModality(Modality modality) {
        this.stage.initModality(modality == null ? Modality.APPLICATION_MODAL : modality);
    }

    @Override
    public Modality getModality() {
        return this.stage.getModality();
    }

    @Override
    public void setDialogPane(DialogPane dialogPane) {
        this.dialogPane = dialogPane;
        if (this.scene == null) {
            this.scene = new Scene(dialogPane);
            this.stage.setScene(this.scene);
        } else {
            this.scene.setRoot(dialogPane);
        }
        dialogPane.autosize();
        this.stage.sizeToScene();
    }

    @Override
    public void show() {
        this.scene.setRoot(this.dialogPane);
        this.stage.centerOnScreen();
        this.stage.show();
    }

    @Override
    public void showAndWait() {
        this.scene.setRoot(this.dialogPane);
        this.stage.centerOnScreen();
        this.stage.showAndWait();
    }

    @Override
    public void close() {
        if (this.stage.isShowing()) {
            this.stage.hide();
        }
        if (this.scene != null) {
            this.scene.setRoot(this.DUMMY_ROOT);
        }
    }

    @Override
    public ReadOnlyBooleanProperty showingProperty() {
        return this.stage.showingProperty();
    }

    @Override
    public Window getWindow() {
        return this.stage;
    }

    @Override
    public Node getRoot() {
        return this.stage.getScene().getRoot();
    }

    @Override
    public double getX() {
        return this.stage.getX();
    }

    @Override
    public void setX(double d) {
        this.stage.setX(d);
    }

    @Override
    public ReadOnlyDoubleProperty xProperty() {
        return this.stage.xProperty();
    }

    @Override
    public double getY() {
        return this.stage.getY();
    }

    @Override
    public void setY(double d) {
        this.stage.setY(d);
    }

    @Override
    public ReadOnlyDoubleProperty yProperty() {
        return this.stage.yProperty();
    }

    @Override
    ReadOnlyDoubleProperty heightProperty() {
        return this.stage.heightProperty();
    }

    @Override
    void setHeight(double d) {
        this.stage.setHeight(d);
    }

    @Override
    double getSceneHeight() {
        return this.scene == null ? 0.0 : this.scene.getHeight();
    }

    @Override
    ReadOnlyDoubleProperty widthProperty() {
        return this.stage.widthProperty();
    }

    @Override
    void setWidth(double d) {
        this.stage.setWidth(d);
    }

    @Override
    BooleanProperty resizableProperty() {
        return this.stage.resizableProperty();
    }

    @Override
    StringProperty titleProperty() {
        return this.stage.titleProperty();
    }

    @Override
    ReadOnlyBooleanProperty focusedProperty() {
        return this.stage.focusedProperty();
    }

    @Override
    public void sizeToScene() {
        this.stage.sizeToScene();
    }

    private void positionStage() {
        double d = this.getX();
        double d2 = this.getY();
        if (!Double.isNaN(d) && !Double.isNaN(d2) && Double.compare(d, this.prefX) != 0 && Double.compare(d2, this.prefY) != 0) {
            this.setX(d);
            this.setY(d2);
            return;
        }
        this.dialogPane.applyCss();
        this.dialogPane.layout();
        Window window = this.getOwner();
        Scene scene = window.getScene();
        double d3 = scene.getY();
        double d4 = this.dialogPane.prefWidth(-1.0);
        double d5 = this.dialogPane.prefHeight(d4);
        d = window.getX() + scene.getWidth() / 2.0 - d4 / 2.0;
        d2 = window.getY() + d3 / 2.0 + scene.getHeight() / 2.0 - d5 / 2.0;
        this.prefX = d;
        this.prefY = d2;
        this.setX(d);
        this.setY(d2);
    }

    private void updateStageBindings(Window window, Window window2) {
        Scene scene;
        Stage stage;
        Scene scene2 = this.stage.getScene();
        if (window != null && window instanceof Stage) {
            stage = (Stage)window;
            Bindings.unbindContent(this.stage.getIcons(), stage.getIcons());
            scene = stage.getScene();
            if (this.scene != null && scene2 != null) {
                Bindings.unbindContent(scene2.getStylesheets(), scene.getStylesheets());
            }
        }
        if (window2 instanceof Stage) {
            stage = (Stage)window2;
            Bindings.bindContent(this.stage.getIcons(), stage.getIcons());
            scene = stage.getScene();
            if (this.scene != null && scene2 != null) {
                Bindings.bindContent(scene2.getStylesheets(), scene.getStylesheets());
            }
        }
    }
}

