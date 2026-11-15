/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.Window;

abstract class FXDialog {
    protected Object owner;

    protected FXDialog() {
    }

    public boolean requestPermissionToClose(Dialog<?> dialog) {
        boolean bl = true;
        DialogPane dialogPane = dialog.getDialogPane();
        if (dialogPane != null) {
            ObservableList<ButtonType> observableList = dialogPane.getButtonTypes();
            if (observableList.size() == 1) {
                bl = false;
            } else {
                for (ButtonType buttonType : observableList) {
                    ButtonBar.ButtonData buttonData;
                    if (buttonType == null || (buttonData = buttonType.getButtonData()) == null || buttonData != ButtonBar.ButtonData.CANCEL_CLOSE && !buttonData.isCancelButton()) continue;
                    bl = false;
                    break;
                }
            }
        }
        return !bl;
    }

    public abstract void show();

    public abstract void showAndWait();

    public abstract void close();

    public abstract void initOwner(Window var1);

    public abstract Window getOwner();

    public abstract void initModality(Modality var1);

    public abstract Modality getModality();

    public abstract ReadOnlyBooleanProperty showingProperty();

    public abstract Window getWindow();

    public abstract void sizeToScene();

    public abstract double getX();

    public abstract void setX(double var1);

    public abstract ReadOnlyDoubleProperty xProperty();

    public abstract double getY();

    public abstract void setY(double var1);

    public abstract ReadOnlyDoubleProperty yProperty();

    abstract BooleanProperty resizableProperty();

    abstract ReadOnlyBooleanProperty focusedProperty();

    abstract StringProperty titleProperty();

    public abstract void setDialogPane(DialogPane var1);

    public abstract Node getRoot();

    abstract ReadOnlyDoubleProperty widthProperty();

    abstract void setWidth(double var1);

    abstract ReadOnlyDoubleProperty heightProperty();

    abstract void setHeight(double var1);

    abstract void initStyle(StageStyle var1);

    abstract StageStyle getStyle();

    abstract double getSceneHeight();
}

