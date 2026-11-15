/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.charts;

import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.StringProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;

public class Legend
extends TilePane {
    private static final int GAP = 5;
    private ListChangeListener<LegendItem> itemsListener = change -> {
        List list = this.getItems().stream().map(legendItem -> ((LegendItem)legendItem).label).collect(Collectors.toList());
        this.getChildren().setAll(list);
        if (this.isVisible()) {
            this.requestLayout();
        }
    };
    private BooleanProperty vertical = new BooleanPropertyBase(false){

        @Override
        protected void invalidated() {
            Legend.this.setOrientation(this.get() ? Orientation.VERTICAL : Orientation.HORIZONTAL);
        }

        @Override
        public Object getBean() {
            return Legend.this;
        }

        @Override
        public String getName() {
            return "vertical";
        }
    };
    private ObjectProperty<ObservableList<LegendItem>> items = new ObjectPropertyBase<ObservableList<LegendItem>>(){
        ObservableList<LegendItem> oldItems = null;

        @Override
        protected void invalidated() {
            if (this.oldItems != null) {
                this.oldItems.removeListener(Legend.this.itemsListener);
            }
            Legend.this.getChildren().clear();
            ObservableList observableList = (ObservableList)this.get();
            if (observableList != null) {
                observableList.addListener(Legend.this.itemsListener);
                List list = observableList.stream().map(legendItem -> ((LegendItem)legendItem).label).collect(Collectors.toList());
                Legend.this.getChildren().addAll(list);
            }
            this.oldItems = observableList;
            Legend.this.requestLayout();
        }

        @Override
        public Object getBean() {
            return Legend.this;
        }

        @Override
        public String getName() {
            return "items";
        }
    };

    public final boolean isVertical() {
        return this.vertical.get();
    }

    public final void setVertical(boolean bl) {
        this.vertical.set(bl);
    }

    public final BooleanProperty verticalProperty() {
        return this.vertical;
    }

    public final void setItems(ObservableList<LegendItem> observableList) {
        this.itemsProperty().set(observableList);
    }

    public final ObservableList<LegendItem> getItems() {
        return (ObservableList)this.items.get();
    }

    public final ObjectProperty<ObservableList<LegendItem>> itemsProperty() {
        return this.items;
    }

    public Legend() {
        super(5.0, 5.0);
        this.setTileAlignment(Pos.CENTER_LEFT);
        this.setItems(FXCollections.observableArrayList());
        this.getStyleClass().setAll((String[])new String[]{"chart-legend"});
    }

    @Override
    protected double computePrefWidth(double d) {
        return this.getItems().size() > 0 ? super.computePrefWidth(d) : 0.0;
    }

    @Override
    protected double computePrefHeight(double d) {
        return this.getItems().size() > 0 ? super.computePrefHeight(d) : 0.0;
    }

    public static class LegendItem {
        private Label label = new Label();
        private StringProperty text = new StringPropertyBase(){

            @Override
            protected void invalidated() {
                label.setText(this.get());
            }

            @Override
            public Object getBean() {
                return this;
            }

            @Override
            public String getName() {
                return "text";
            }
        };
        private ObjectProperty<Node> symbol = new ObjectPropertyBase<Node>((Node)new Region()){

            @Override
            protected void invalidated() {
                Node node = (Node)this.get();
                if (node != null) {
                    node.getStyleClass().setAll((String[])new String[]{"chart-legend-item-symbol"});
                }
                label.setGraphic(node);
            }

            @Override
            public Object getBean() {
                return this;
            }

            @Override
            public String getName() {
                return "symbol";
            }
        };

        public final String getText() {
            return this.text.getValue();
        }

        public final void setText(String string) {
            this.text.setValue(string);
        }

        public final StringProperty textProperty() {
            return this.text;
        }

        public final Node getSymbol() {
            return (Node)this.symbol.getValue();
        }

        public final void setSymbol(Node node) {
            this.symbol.setValue(node);
        }

        public final ObjectProperty<Node> symbolProperty() {
            return this.symbol;
        }

        public LegendItem(String string) {
            this.setText(string);
            this.label.getStyleClass().add("chart-legend-item");
            this.label.setAlignment(Pos.CENTER_LEFT);
            this.label.setContentDisplay(ContentDisplay.LEFT);
            this.label.setGraphic(this.getSymbol());
            this.getSymbol().getStyleClass().setAll((String[])new String[]{"chart-legend-item-symbol"});
        }

        public LegendItem(String string, Node node) {
            this(string);
            this.setSymbol(node);
        }
    }
}

