/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import com.sun.javafx.scene.control.skin.resources.ControlResources;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class ChoiceDialog<T>
extends Dialog<T> {
    private final GridPane grid;
    private final Label label;
    private final ComboBox<T> comboBox;
    private final T defaultChoice;

    public ChoiceDialog() {
        this(null, (Object[])null);
    }

    public ChoiceDialog(T t, T ... TArray) {
        this(t, TArray == null ? Collections.emptyList() : Arrays.asList(TArray));
    }

    public ChoiceDialog(T t, Collection<T> collection) {
        DialogPane dialogPane = this.getDialogPane();
        this.grid = new GridPane();
        this.grid.setHgap(10.0);
        this.grid.setMaxWidth(Double.MAX_VALUE);
        this.grid.setAlignment(Pos.CENTER_LEFT);
        this.label = DialogPane.createContentLabel(dialogPane.getContentText());
        this.label.setPrefWidth(-1.0);
        this.label.textProperty().bind(dialogPane.contentTextProperty());
        dialogPane.contentTextProperty().addListener(observable -> this.updateGrid());
        this.setTitle(ControlResources.getString("Dialog.confirm.title"));
        dialogPane.setHeaderText(ControlResources.getString("Dialog.confirm.header"));
        dialogPane.getStyleClass().add("choice-dialog");
        dialogPane.getButtonTypes().addAll((ButtonType[])new ButtonType[]{ButtonType.OK, ButtonType.CANCEL});
        this.comboBox = new ComboBox();
        this.comboBox.setMinWidth(150.0);
        if (collection != null) {
            this.comboBox.getItems().addAll(collection);
        }
        this.comboBox.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(this.comboBox, Priority.ALWAYS);
        GridPane.setFillWidth(this.comboBox, true);
        Object object = this.defaultChoice = this.comboBox.getItems().contains(t) ? t : null;
        if (t == null) {
            this.comboBox.getSelectionModel().selectFirst();
        } else {
            this.comboBox.getSelectionModel().select(t);
        }
        this.updateGrid();
        this.setResultConverter(buttonType -> {
            ButtonBar.ButtonData buttonData = buttonType == null ? null : buttonType.getButtonData();
            return buttonData == ButtonBar.ButtonData.OK_DONE ? this.getSelectedItem() : null;
        });
    }

    public final T getSelectedItem() {
        return this.comboBox.getSelectionModel().getSelectedItem();
    }

    public final ReadOnlyObjectProperty<T> selectedItemProperty() {
        return this.comboBox.getSelectionModel().selectedItemProperty();
    }

    public final void setSelectedItem(T t) {
        this.comboBox.getSelectionModel().select(t);
    }

    public final ObservableList<T> getItems() {
        return this.comboBox.getItems();
    }

    public final T getDefaultChoice() {
        return this.defaultChoice;
    }

    private void updateGrid() {
        this.grid.getChildren().clear();
        this.grid.add(this.label, 0, 0);
        this.grid.add(this.comboBox, 1, 0);
        this.getDialogPane().setContent(this.grid);
        Platform.runLater(() -> this.comboBox.requestFocus());
    }
}

