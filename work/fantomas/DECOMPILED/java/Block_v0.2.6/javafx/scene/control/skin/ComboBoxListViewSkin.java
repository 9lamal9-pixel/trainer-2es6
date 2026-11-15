/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.scene.control.behavior.ComboBoxBaseBehavior;
import com.sun.javafx.scene.control.behavior.ComboBoxListViewBehavior;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventTarget;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Control;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.ComboBoxPopupControl;
import javafx.scene.control.skin.ListViewSkin;
import javafx.scene.control.skin.VirtualContainerBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class ComboBoxListViewSkin<T>
extends ComboBoxPopupControl<T> {
    private static final String COMBO_BOX_ROWS_TO_MEASURE_WIDTH_KEY = "comboBoxRowsToMeasureWidth";
    private final ComboBox<T> comboBox;
    private ObservableList<T> comboBoxItems;
    private ListCell<T> buttonCell;
    private Callback<ListView<T>, ListCell<T>> cellFactory;
    private final ListView<T> listView;
    private ObservableList<T> listViewItems;
    private boolean listSelectionLock = false;
    private boolean listViewSelectionDirty = false;
    private final ComboBoxListViewBehavior behavior;
    private boolean itemCountDirty;
    private final ListChangeListener<T> listViewItemsListener = new ListChangeListener<T>(){

        @Override
        public void onChanged(ListChangeListener.Change<? extends T> change) {
            ComboBoxListViewSkin.this.itemCountDirty = true;
            ((ComboBoxBase)ComboBoxListViewSkin.this.getSkinnable()).requestLayout();
        }
    };
    private final InvalidationListener itemsObserver;
    private final WeakListChangeListener<T> weakListViewItemsListener = new WeakListChangeListener<T>(this.listViewItemsListener);
    private final BooleanProperty hideOnClick = new SimpleBooleanProperty(this, "hideOnClick", true);
    private static final PseudoClass PSEUDO_CLASS_SELECTED = PseudoClass.getPseudoClass("selected");
    private static final PseudoClass PSEUDO_CLASS_EMPTY = PseudoClass.getPseudoClass("empty");
    private static final PseudoClass PSEUDO_CLASS_FILLED = PseudoClass.getPseudoClass("filled");

    public ComboBoxListViewSkin(ComboBox<T> comboBox) {
        super(comboBox);
        this.behavior = new ComboBoxListViewBehavior<T>(comboBox);
        this.comboBox = comboBox;
        this.updateComboBoxItems();
        this.itemsObserver = observable -> {
            this.updateComboBoxItems();
            this.updateListViewItems();
        };
        comboBox.itemsProperty().addListener(new WeakInvalidationListener(this.itemsObserver));
        this.listView = this.createListView();
        this.listView.setManaged(false);
        this.getChildren().add(this.listView);
        this.updateListViewItems();
        this.updateCellFactory();
        this.updateButtonCell();
        this.updateValue();
        this.registerChangeListener(comboBox.itemsProperty(), observableValue -> {
            this.updateComboBoxItems();
            this.updateListViewItems();
        });
        this.registerChangeListener(comboBox.promptTextProperty(), observableValue -> this.updateDisplayNode());
        this.registerChangeListener(comboBox.cellFactoryProperty(), observableValue -> this.updateCellFactory());
        this.registerChangeListener(comboBox.visibleRowCountProperty(), observableValue -> {
            if (this.listView == null) {
                return;
            }
            this.listView.requestLayout();
        });
        this.registerChangeListener(comboBox.converterProperty(), observableValue -> this.updateListViewItems());
        this.registerChangeListener(comboBox.buttonCellProperty(), observableValue -> {
            this.updateButtonCell();
            this.updateDisplayArea();
        });
        this.registerChangeListener(comboBox.valueProperty(), observableValue -> {
            this.updateValue();
            comboBox.fireEvent(new ActionEvent());
        });
        this.registerChangeListener(comboBox.editableProperty(), observableValue -> this.updateEditable());
        if (this.comboBox.isShowing()) {
            this.show();
        }
        this.comboBox.sceneProperty().addListener(observable -> {
            if (((ObservableValue)observable).getValue() == null) {
                this.comboBox.hide();
            }
        });
    }

    public final BooleanProperty hideOnClickProperty() {
        return this.hideOnClick;
    }

    public final boolean isHideOnClick() {
        return this.hideOnClick.get();
    }

    public final void setHideOnClick(boolean bl) {
        this.hideOnClick.set(bl);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (this.behavior != null) {
            this.behavior.dispose();
        }
    }

    @Override
    protected TextField getEditor() {
        return ((ComboBoxBase)this.getSkinnable()).isEditable() ? ((ComboBox)this.getSkinnable()).getEditor() : null;
    }

    @Override
    protected StringConverter<T> getConverter() {
        return ((ComboBox)this.getSkinnable()).getConverter();
    }

    @Override
    public Node getDisplayNode() {
        Control control = this.comboBox.isEditable() ? this.getEditableInputNode() : this.buttonCell;
        this.updateDisplayNode();
        return control;
    }

    @Override
    public Node getPopupContent() {
        return this.listView;
    }

    @Override
    protected double computeMinWidth(double d, double d2, double d3, double d4, double d5) {
        this.reconfigurePopup();
        return 50.0;
    }

    @Override
    protected double computePrefWidth(double d, double d2, double d3, double d4, double d5) {
        double d6 = super.computePrefWidth(d, d2, d3, d4, d5);
        double d7 = this.listView.prefWidth(d);
        double d8 = Math.max(d6, d7);
        this.reconfigurePopup();
        return d8;
    }

    @Override
    protected double computeMaxWidth(double d, double d2, double d3, double d4, double d5) {
        this.reconfigurePopup();
        return super.computeMaxWidth(d, d2, d3, d4, d5);
    }

    @Override
    protected double computeMinHeight(double d, double d2, double d3, double d4, double d5) {
        this.reconfigurePopup();
        return super.computeMinHeight(d, d2, d3, d4, d5);
    }

    @Override
    protected double computePrefHeight(double d, double d2, double d3, double d4, double d5) {
        this.reconfigurePopup();
        return super.computePrefHeight(d, d2, d3, d4, d5);
    }

    @Override
    protected double computeMaxHeight(double d, double d2, double d3, double d4, double d5) {
        this.reconfigurePopup();
        return super.computeMaxHeight(d, d2, d3, d4, d5);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void layoutChildren(double d, double d2, double d3, double d4) {
        if (this.listViewSelectionDirty) {
            try {
                this.listSelectionLock = true;
                Object t = this.comboBox.getSelectionModel().getSelectedItem();
                this.listView.getSelectionModel().clearSelection();
                this.listView.getSelectionModel().select(t);
            }
            finally {
                this.listSelectionLock = false;
                this.listViewSelectionDirty = false;
            }
        }
        super.layoutChildren(d, d2, d3, d4);
    }

    @Override
    void updateDisplayNode() {
        if (this.getEditor() != null) {
            super.updateDisplayNode();
        } else {
            Object t = this.comboBox.getValue();
            int n = this.getIndexOfComboBoxValueInItemsList();
            if (n > -1) {
                this.buttonCell.setItem(null);
                this.buttonCell.updateIndex(n);
            } else {
                this.buttonCell.updateIndex(-1);
                boolean bl = this.updateDisplayText(this.buttonCell, t, false);
                this.buttonCell.pseudoClassStateChanged(PSEUDO_CLASS_EMPTY, bl);
                this.buttonCell.pseudoClassStateChanged(PSEUDO_CLASS_FILLED, !bl);
                this.buttonCell.pseudoClassStateChanged(PSEUDO_CLASS_SELECTED, true);
            }
        }
    }

    @Override
    ComboBoxBaseBehavior getBehavior() {
        return this.behavior;
    }

    private void updateComboBoxItems() {
        this.comboBoxItems = this.comboBox.getItems();
        this.comboBoxItems = this.comboBoxItems == null ? FXCollections.emptyObservableList() : this.comboBoxItems;
    }

    private void updateListViewItems() {
        if (this.listViewItems != null) {
            this.listViewItems.removeListener(this.weakListViewItemsListener);
        }
        this.listViewItems = this.comboBoxItems;
        this.listView.setItems(this.listViewItems);
        if (this.listViewItems != null) {
            this.listViewItems.addListener(this.weakListViewItemsListener);
        }
        this.itemCountDirty = true;
        ((ComboBoxBase)this.getSkinnable()).requestLayout();
    }

    private void updateValue() {
        Object t = this.comboBox.getValue();
        MultipleSelectionModel multipleSelectionModel = this.listView.getSelectionModel();
        int n = this.getIndexOfComboBoxValueInItemsList();
        if (t == null && n == -1) {
            multipleSelectionModel.clearSelection();
        } else if (n == -1) {
            this.listSelectionLock = true;
            multipleSelectionModel.clearSelection();
            this.listSelectionLock = false;
        } else {
            int n2 = this.comboBox.getSelectionModel().getSelectedIndex();
            if (n2 >= 0 && n2 < this.comboBoxItems.size()) {
                Object e = this.comboBoxItems.get(n2);
                if (e != null && e.equals(t) || e == null && t == null) {
                    multipleSelectionModel.select(n2);
                } else {
                    multipleSelectionModel.select(t);
                }
            } else {
                int n3 = this.comboBoxItems.indexOf(t);
                if (n3 == -1) {
                    this.updateDisplayNode();
                } else {
                    multipleSelectionModel.select(n3);
                }
            }
        }
    }

    private boolean updateDisplayText(ListCell<T> listCell, T t, boolean bl) {
        if (bl) {
            if (listCell == null) {
                return true;
            }
            listCell.setGraphic(null);
            listCell.setText(null);
            return true;
        }
        if (t instanceof Node) {
            Node node = listCell.getGraphic();
            Node node2 = (Node)t;
            if (node == null || !node.equals(node2)) {
                listCell.setText(null);
                listCell.setGraphic(node2);
            }
            return node2 == null;
        }
        StringConverter<T> stringConverter = this.comboBox.getConverter();
        String string = this.comboBox.getPromptText();
        String string2 = t == null && string != null ? string : (stringConverter == null ? (t == null ? null : t.toString()) : stringConverter.toString(t));
        listCell.setText(string2);
        listCell.setGraphic(null);
        return string2 == null || string2.isEmpty();
    }

    private int getIndexOfComboBoxValueInItemsList() {
        Object t = this.comboBox.getValue();
        int n = this.comboBoxItems.indexOf(t);
        return n;
    }

    private void updateButtonCell() {
        this.buttonCell = this.comboBox.getButtonCell() != null ? this.comboBox.getButtonCell() : this.getDefaultCellFactory().call(this.listView);
        this.buttonCell.setMouseTransparent(true);
        this.buttonCell.updateListView(this.listView);
        this.buttonCell.setAccessibleRole(AccessibleRole.NODE);
    }

    private void updateCellFactory() {
        Callback<ListView<T>, ListCell<T>> callback = this.comboBox.getCellFactory();
        this.cellFactory = callback != null ? callback : this.getDefaultCellFactory();
        this.listView.setCellFactory(this.cellFactory);
    }

    private Callback<ListView<T>, ListCell<T>> getDefaultCellFactory() {
        return new Callback<ListView<T>, ListCell<T>>(){

            @Override
            public ListCell<T> call(ListView<T> listView) {
                return new ListCell<T>(){

                    @Override
                    public void updateItem(T t, boolean bl) {
                        super.updateItem(t, bl);
                        ComboBoxListViewSkin.this.updateDisplayText(this, t, bl);
                    }
                };
            }
        };
    }

    private ListView<T> createListView() {
        ListView listView = new ListView<T>(){
            {
                this.getProperties().put("selectFirstRowByDefault", false);
            }

            @Override
            protected double computeMinHeight(double d) {
                return 30.0;
            }

            @Override
            protected double computePrefWidth(double d) {
                double d2;
                if (this.getSkin() instanceof ListViewSkin) {
                    ListViewSkin listViewSkin = (ListViewSkin)this.getSkin();
                    if (ComboBoxListViewSkin.this.itemCountDirty) {
                        listViewSkin.updateItemCount();
                        ComboBoxListViewSkin.this.itemCountDirty = false;
                    }
                    int n = -1;
                    if (ComboBoxListViewSkin.this.comboBox.getProperties().containsKey(ComboBoxListViewSkin.COMBO_BOX_ROWS_TO_MEASURE_WIDTH_KEY)) {
                        n = (Integer)ComboBoxListViewSkin.this.comboBox.getProperties().get(ComboBoxListViewSkin.COMBO_BOX_ROWS_TO_MEASURE_WIDTH_KEY);
                    }
                    d2 = Math.max(ComboBoxListViewSkin.this.comboBox.getWidth(), listViewSkin.getMaxCellWidth(n) + 30.0);
                } else {
                    d2 = Math.max(100.0, ComboBoxListViewSkin.this.comboBox.getWidth());
                }
                if (this.getItems().isEmpty() && this.getPlaceholder() != null) {
                    d2 = Math.max(super.computePrefWidth(d), d2);
                }
                return Math.max(50.0, d2);
            }

            @Override
            protected double computePrefHeight(double d) {
                return ComboBoxListViewSkin.this.getListViewPrefHeight();
            }
        };
        listView.setId("list-view");
        listView.placeholderProperty().bind(this.comboBox.placeholderProperty());
        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        listView.setFocusTraversable(false);
        listView.getSelectionModel().selectedIndexProperty().addListener(observable -> {
            if (this.listSelectionLock) {
                return;
            }
            int n = this.listView.getSelectionModel().getSelectedIndex();
            this.comboBox.getSelectionModel().select(n);
            this.updateDisplayNode();
            this.comboBox.notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
        });
        this.comboBox.getSelectionModel().selectedItemProperty().addListener(observable -> {
            this.listViewSelectionDirty = true;
        });
        listView.addEventFilter(MouseEvent.MOUSE_RELEASED, mouseEvent -> {
            ObservableList<String> observableList;
            EventTarget eventTarget = mouseEvent.getTarget();
            if (eventTarget instanceof Parent && ((observableList = ((Parent)eventTarget).getStyleClass()).contains("thumb") || observableList.contains("track") || observableList.contains("decrement-arrow") || observableList.contains("increment-arrow"))) {
                return;
            }
            if (this.isHideOnClick()) {
                this.comboBox.hide();
            }
        });
        listView.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER || keyEvent.getCode() == KeyCode.SPACE || keyEvent.getCode() == KeyCode.ESCAPE) {
                this.comboBox.hide();
            }
        });
        return listView;
    }

    private double getListViewPrefHeight() {
        double d;
        if (this.listView.getSkin() instanceof VirtualContainerBase) {
            int n = this.comboBox.getVisibleRowCount();
            VirtualContainerBase virtualContainerBase = (VirtualContainerBase)this.listView.getSkin();
            d = virtualContainerBase.getVirtualFlowPreferredHeight(n);
        } else {
            double d2 = this.comboBoxItems.size() * 25;
            d = Math.min(d2, 200.0);
        }
        return d;
    }

    ListView<T> getListView() {
        return this.listView;
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case FOCUS_ITEM: {
                if (this.comboBox.isShowing()) {
                    return this.listView.queryAccessibleAttribute(accessibleAttribute, objectArray);
                }
                return null;
            }
            case TEXT: {
                String string;
                String string2 = this.comboBox.getAccessibleText();
                if (string2 != null && !string2.isEmpty()) {
                    return string2;
                }
                String string3 = string = this.comboBox.isEditable() ? this.getEditor().getText() : this.buttonCell.getText();
                if (string == null || string.isEmpty()) {
                    string = this.comboBox.getPromptText();
                }
                return string;
            }
            case SELECTION_START: {
                return this.getEditor() != null ? Integer.valueOf(this.getEditor().getSelection().getStart()) : null;
            }
            case SELECTION_END: {
                return this.getEditor() != null ? Integer.valueOf(this.getEditor().getSelection().getEnd()) : null;
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }
}

