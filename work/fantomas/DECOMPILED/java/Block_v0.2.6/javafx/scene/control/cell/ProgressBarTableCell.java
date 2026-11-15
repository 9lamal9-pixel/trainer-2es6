/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.cell;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class ProgressBarTableCell<S>
extends TableCell<S, Double> {
    private final ProgressBar progressBar;
    private ObservableValue<Double> observable;

    public static <S> Callback<TableColumn<S, Double>, TableCell<S, Double>> forTableColumn() {
        return tableColumn -> new ProgressBarTableCell();
    }

    public ProgressBarTableCell() {
        this.getStyleClass().add("progress-bar-table-cell");
        this.progressBar = new ProgressBar();
        this.progressBar.setMaxWidth(Double.MAX_VALUE);
    }

    @Override
    public void updateItem(Double d, boolean bl) {
        super.updateItem(d, bl);
        if (bl) {
            this.setGraphic(null);
        } else {
            this.progressBar.progressProperty().unbind();
            TableColumn tableColumn = this.getTableColumn();
            ObservableValue<Object> observableValue = this.observable = tableColumn == null ? null : tableColumn.getCellObservableValue(this.getIndex());
            if (this.observable != null) {
                this.progressBar.progressProperty().bind(this.observable);
            } else if (d != null) {
                this.progressBar.setProgress(d);
            }
            this.setGraphic(this.progressBar);
        }
    }
}

