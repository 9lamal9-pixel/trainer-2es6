/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.cell;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;

public class ProgressBarTreeTableCell<S>
extends TreeTableCell<S, Double> {
    private final ProgressBar progressBar;
    private ObservableValue<Double> observable;

    public static <S> Callback<TreeTableColumn<S, Double>, TreeTableCell<S, Double>> forTreeTableColumn() {
        return treeTableColumn -> new ProgressBarTreeTableCell();
    }

    public ProgressBarTreeTableCell() {
        this.getStyleClass().add("progress-bar-tree-table-cell");
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
            TreeTableColumn treeTableColumn = this.getTableColumn();
            ObservableValue<Object> observableValue = this.observable = treeTableColumn == null ? null : treeTableColumn.getCellObservableValue(this.getIndex());
            if (this.observable != null) {
                this.progressBar.progressProperty().bind(this.observable);
            } else if (d != null) {
                this.progressBar.setProgress(d);
            }
            this.setGraphic(this.progressBar);
        }
    }
}

