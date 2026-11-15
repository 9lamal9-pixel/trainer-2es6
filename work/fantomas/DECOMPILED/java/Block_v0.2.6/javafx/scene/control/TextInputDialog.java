/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import com.sun.javafx.scene.control.skin.resources.ControlResources;
import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class TextInputDialog
extends Dialog<String> {
    private final GridPane grid;
    private final Label label;
    private final TextField textField;
    private final String defaultValue;

    public TextInputDialog() {
        this("");
    }

    public TextInputDialog(@NamedArg(value="defaultValue") String string) {
        DialogPane dialogPane = this.getDialogPane();
        this.textField = new TextField(string);
        this.textField.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(this.textField, Priority.ALWAYS);
        GridPane.setFillWidth(this.textField, true);
        this.label = DialogPane.createContentLabel(dialogPane.getContentText());
        this.label.setPrefWidth(-1.0);
        this.label.textProperty().bind(dialogPane.contentTextProperty());
        this.defaultValue = string;
        this.grid = new GridPane();
        this.grid.setHgap(10.0);
        this.grid.setMaxWidth(Double.MAX_VALUE);
        this.grid.setAlignment(Pos.CENTER_LEFT);
        dialogPane.contentTextProperty().addListener(observable -> this.updateGrid());
        this.setTitle(ControlResources.getString("Dialog.confirm.title"));
        dialogPane.setHeaderText(ControlResources.getString("Dialog.confirm.header"));
        dialogPane.getStyleClass().add("text-input-dialog");
        dialogPane.getButtonTypes().addAll((ButtonType[])new ButtonType[]{ButtonType.OK, ButtonType.CANCEL});
        this.updateGrid();
        this.setResultConverter(buttonType -> {
            ButtonBar.ButtonData buttonData = buttonType == null ? null : buttonType.getButtonData();
            return buttonData == ButtonBar.ButtonData.OK_DONE ? this.textField.getText() : null;
        });
    }

    public final TextField getEditor() {
        return this.textField;
    }

    public final String getDefaultValue() {
        return this.defaultValue;
    }

    private void updateGrid() {
        this.grid.getChildren().clear();
        this.grid.add(this.label, 0, 0);
        this.grid.add(this.textField, 1, 0);
        this.getDialogPane().setContent(this.grid);
        Platform.runLater(() -> this.textField.requestFocus());
    }
}

