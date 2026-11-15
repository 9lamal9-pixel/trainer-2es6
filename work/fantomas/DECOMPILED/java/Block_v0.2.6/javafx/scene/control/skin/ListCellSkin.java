/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.ListCellBehavior;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.geometry.Orientation;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.CellSkinBase;

public class ListCellSkin<T>
extends CellSkinBase<ListCell<T>> {
    private double fixedCellSize;
    private boolean fixedCellSizeEnabled;
    private final BehaviorBase<ListCell<T>> behavior;

    public ListCellSkin(ListCell<T> listCell) {
        super(listCell);
        this.behavior = new ListCellBehavior<T>(listCell);
        this.setupListeners();
    }

    private void setupListeners() {
        ListView listView = ((ListCell)this.getSkinnable()).getListView();
        if (listView == null) {
            ((ListCell)this.getSkinnable()).listViewProperty().addListener(new InvalidationListener(){

                @Override
                public void invalidated(Observable observable) {
                    ((ListCell)ListCellSkin.this.getSkinnable()).listViewProperty().removeListener(this);
                    ListCellSkin.this.setupListeners();
                }
            });
        } else {
            this.fixedCellSize = listView.getFixedCellSize();
            this.fixedCellSizeEnabled = this.fixedCellSize > 0.0;
            this.registerChangeListener(listView.fixedCellSizeProperty(), observableValue -> {
                this.fixedCellSize = ((ListCell)this.getSkinnable()).getListView().getFixedCellSize();
                this.fixedCellSizeEnabled = this.fixedCellSize > 0.0;
            });
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (this.behavior != null) {
            this.behavior.dispose();
        }
    }

    @Override
    protected double computePrefWidth(double d, double d2, double d3, double d4, double d5) {
        double d6 = super.computePrefWidth(d, d2, d3, d4, d5);
        ListView listView = ((ListCell)this.getSkinnable()).getListView();
        return listView == null ? 0.0 : (listView.getOrientation() == Orientation.VERTICAL ? d6 : Math.max(d6, this.getCellSize()));
    }

    @Override
    protected double computePrefHeight(double d, double d2, double d3, double d4, double d5) {
        if (this.fixedCellSizeEnabled) {
            return this.fixedCellSize;
        }
        double d6 = this.getCellSize();
        double d7 = d6 == 24.0 ? super.computePrefHeight(d, d2, d3, d4, d5) : d6;
        return d7;
    }

    @Override
    protected double computeMinHeight(double d, double d2, double d3, double d4, double d5) {
        if (this.fixedCellSizeEnabled) {
            return this.fixedCellSize;
        }
        return super.computeMinHeight(d, d2, d3, d4, d5);
    }

    @Override
    protected double computeMaxHeight(double d, double d2, double d3, double d4, double d5) {
        if (this.fixedCellSizeEnabled) {
            return this.fixedCellSize;
        }
        return super.computeMaxHeight(d, d2, d3, d4, d5);
    }
}

