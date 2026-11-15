/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import com.sun.javafx.scene.control.skin.resources.ControlResources;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import javafx.beans.InvalidationListener;
import javafx.beans.NamedArg;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;

public class Alert
extends Dialog<ButtonType> {
    private WeakReference<DialogPane> dialogPaneRef;
    private boolean installingDefaults = false;
    private boolean hasCustomButtons = false;
    private boolean hasCustomTitle = false;
    private boolean hasCustomHeaderText = false;
    private final InvalidationListener headerTextListener = observable -> {
        if (!this.installingDefaults) {
            this.hasCustomHeaderText = true;
        }
    };
    private final InvalidationListener titleListener = observable -> {
        if (!this.installingDefaults) {
            this.hasCustomTitle = true;
        }
    };
    private final ListChangeListener<ButtonType> buttonsListener = change -> {
        if (!this.installingDefaults) {
            this.hasCustomButtons = true;
        }
    };
    private final ObjectProperty<AlertType> alertType = new SimpleObjectProperty<AlertType>(null){
        final String[] styleClasses;
        {
            this.styleClasses = new String[]{"information", "warning", "error", "confirmation"};
        }

        @Override
        protected void invalidated() {
            DialogPane dialogPane;
            String string = "";
            String string2 = "";
            String string3 = "";
            ButtonType[] buttonTypeArray = new ButtonType[]{ButtonType.OK};
            switch (Alert.this.getAlertType()) {
                case NONE: {
                    buttonTypeArray = new ButtonType[]{};
                    break;
                }
                case INFORMATION: {
                    string = ControlResources.getString("Dialog.info.title");
                    string2 = ControlResources.getString("Dialog.info.header");
                    string3 = "information";
                    break;
                }
                case WARNING: {
                    string = ControlResources.getString("Dialog.warning.title");
                    string2 = ControlResources.getString("Dialog.warning.header");
                    string3 = "warning";
                    break;
                }
                case ERROR: {
                    string = ControlResources.getString("Dialog.error.title");
                    string2 = ControlResources.getString("Dialog.error.header");
                    string3 = "error";
                    break;
                }
                case CONFIRMATION: {
                    string = ControlResources.getString("Dialog.confirm.title");
                    string2 = ControlResources.getString("Dialog.confirm.header");
                    string3 = "confirmation";
                    buttonTypeArray = new ButtonType[]{ButtonType.OK, ButtonType.CANCEL};
                }
            }
            Alert.this.installingDefaults = true;
            if (!Alert.this.hasCustomTitle) {
                Alert.this.setTitle(string);
            }
            if (!Alert.this.hasCustomHeaderText) {
                Alert.this.setHeaderText(string2);
            }
            if (!Alert.this.hasCustomButtons) {
                Alert.this.getButtonTypes().setAll((ButtonType[])buttonTypeArray);
            }
            if ((dialogPane = Alert.this.getDialogPane()) != null) {
                ArrayList<String> arrayList = new ArrayList<String>(Arrays.asList(this.styleClasses));
                arrayList.remove(string3);
                dialogPane.getStyleClass().removeAll((Collection<?>)arrayList);
                if (!dialogPane.getStyleClass().contains(string3)) {
                    dialogPane.getStyleClass().add(string3);
                }
            }
            Alert.this.installingDefaults = false;
        }
    };

    public Alert(@NamedArg(value="alertType") AlertType alertType) {
        this(alertType, "", new ButtonType[0]);
    }

    public Alert(@NamedArg(value="alertType") AlertType alertType, @NamedArg(value="contentText") String string, ButtonType ... buttonTypeArray) {
        DialogPane dialogPane = this.getDialogPane();
        dialogPane.setContentText(string);
        this.getDialogPane().getStyleClass().add("alert");
        this.dialogPaneRef = new WeakReference<DialogPane>(dialogPane);
        boolean bl = this.hasCustomButtons = buttonTypeArray != null && buttonTypeArray.length > 0;
        if (this.hasCustomButtons) {
            for (ButtonType buttonType : buttonTypeArray) {
                dialogPane.getButtonTypes().addAll((ButtonType[])new ButtonType[]{buttonType});
            }
        }
        this.setAlertType(alertType);
        this.dialogPaneProperty().addListener(observable -> this.updateListeners());
        this.titleProperty().addListener(this.titleListener);
        this.updateListeners();
    }

    public final AlertType getAlertType() {
        return (AlertType)((Object)this.alertType.get());
    }

    public final void setAlertType(AlertType alertType) {
        this.alertType.setValue(alertType);
    }

    public final ObjectProperty<AlertType> alertTypeProperty() {
        return this.alertType;
    }

    public final ObservableList<ButtonType> getButtonTypes() {
        return this.getDialogPane().getButtonTypes();
    }

    private void updateListeners() {
        DialogPane dialogPane;
        DialogPane dialogPane2 = (DialogPane)this.dialogPaneRef.get();
        if (dialogPane2 != null) {
            dialogPane2.headerTextProperty().removeListener(this.headerTextListener);
            dialogPane2.getButtonTypes().removeListener(this.buttonsListener);
        }
        if ((dialogPane = this.getDialogPane()) != null) {
            dialogPane.headerTextProperty().addListener(this.headerTextListener);
            dialogPane.getButtonTypes().addListener(this.buttonsListener);
        }
        this.dialogPaneRef = new WeakReference<DialogPane>(dialogPane);
    }

    public static enum AlertType {
        NONE,
        INFORMATION,
        WARNING,
        CONFIRMATION,
        ERROR;

    }
}

