/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.cell;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

class CellUtils {
    static int TREE_VIEW_HBOX_GRAPHIC_PADDING = 3;
    private static final StringConverter<?> defaultStringConverter = new StringConverter<Object>(){

        @Override
        public String toString(Object object) {
            return object == null ? null : object.toString();
        }

        @Override
        public Object fromString(String string) {
            return string;
        }
    };
    private static final StringConverter<?> defaultTreeItemStringConverter = new StringConverter<TreeItem<?>>(){

        @Override
        public String toString(TreeItem<?> treeItem) {
            return treeItem == null || treeItem.getValue() == null ? "" : treeItem.getValue().toString();
        }

        @Override
        public TreeItem<?> fromString(String string) {
            return new TreeItem<String>(string);
        }
    };

    CellUtils() {
    }

    static <T> StringConverter<T> defaultStringConverter() {
        return defaultStringConverter;
    }

    static <T> StringConverter<TreeItem<T>> defaultTreeItemStringConverter() {
        return defaultTreeItemStringConverter;
    }

    private static <T> String getItemText(Cell<T> cell, StringConverter<T> stringConverter) {
        return stringConverter == null ? (cell.getItem() == null ? "" : cell.getItem().toString()) : stringConverter.toString(cell.getItem());
    }

    static Node getGraphic(TreeItem<?> treeItem) {
        return treeItem == null ? null : treeItem.getGraphic();
    }

    static <T> void updateItem(Cell<T> cell, StringConverter<T> stringConverter, ChoiceBox<T> choiceBox) {
        CellUtils.updateItem(cell, stringConverter, null, null, choiceBox);
    }

    static <T> void updateItem(Cell<T> cell, StringConverter<T> stringConverter, HBox hBox, Node node, ChoiceBox<T> choiceBox) {
        if (cell.isEmpty()) {
            cell.setText(null);
            cell.setGraphic(null);
        } else if (cell.isEditing()) {
            if (choiceBox != null) {
                choiceBox.getSelectionModel().select(cell.getItem());
            }
            cell.setText(null);
            if (node != null) {
                hBox.getChildren().setAll((Node[])new Node[]{node, choiceBox});
                cell.setGraphic(hBox);
            } else {
                cell.setGraphic(choiceBox);
            }
        } else {
            cell.setText(CellUtils.getItemText(cell, stringConverter));
            cell.setGraphic(node);
        }
    }

    static <T> ChoiceBox<T> createChoiceBox(Cell<T> cell, ObservableList<T> observableList, ObjectProperty<StringConverter<T>> objectProperty) {
        ChoiceBox choiceBox = new ChoiceBox(observableList);
        choiceBox.setMaxWidth(Double.MAX_VALUE);
        choiceBox.converterProperty().bind(objectProperty);
        choiceBox.showingProperty().addListener(observable -> {
            if (!choiceBox.isShowing()) {
                cell.commitEdit(choiceBox.getSelectionModel().getSelectedItem());
            }
        });
        return choiceBox;
    }

    static <T> void updateItem(Cell<T> cell, StringConverter<T> stringConverter, TextField textField) {
        CellUtils.updateItem(cell, stringConverter, null, null, textField);
    }

    static <T> void updateItem(Cell<T> cell, StringConverter<T> stringConverter, HBox hBox, Node node, TextField textField) {
        if (cell.isEmpty()) {
            cell.setText(null);
            cell.setGraphic(null);
        } else if (cell.isEditing()) {
            if (textField != null) {
                textField.setText(CellUtils.getItemText(cell, stringConverter));
            }
            cell.setText(null);
            if (node != null) {
                hBox.getChildren().setAll((Node[])new Node[]{node, textField});
                cell.setGraphic(hBox);
            } else {
                cell.setGraphic(textField);
            }
        } else {
            cell.setText(CellUtils.getItemText(cell, stringConverter));
            cell.setGraphic(node);
        }
    }

    static <T> void startEdit(Cell<T> cell, StringConverter<T> stringConverter, HBox hBox, Node node, TextField textField) {
        if (textField != null) {
            textField.setText(CellUtils.getItemText(cell, stringConverter));
        }
        cell.setText(null);
        if (node != null) {
            hBox.getChildren().setAll((Node[])new Node[]{node, textField});
            cell.setGraphic(hBox);
        } else {
            cell.setGraphic(textField);
        }
        textField.selectAll();
        textField.requestFocus();
    }

    static <T> void cancelEdit(Cell<T> cell, StringConverter<T> stringConverter, Node node) {
        cell.setText(CellUtils.getItemText(cell, stringConverter));
        cell.setGraphic(node);
    }

    static <T> TextField createTextField(Cell<T> cell, StringConverter<T> stringConverter) {
        TextField textField = new TextField(CellUtils.getItemText(cell, stringConverter));
        textField.setOnAction(actionEvent -> {
            if (stringConverter == null) {
                throw new IllegalStateException("Attempting to convert text input into Object, but provided StringConverter is null. Be sure to set a StringConverter in your cell factory.");
            }
            cell.commitEdit(stringConverter.fromString(textField.getText()));
            actionEvent.consume();
        });
        textField.setOnKeyReleased(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ESCAPE) {
                cell.cancelEdit();
                keyEvent.consume();
            }
        });
        return textField;
    }

    static <T> void updateItem(Cell<T> cell, StringConverter<T> stringConverter, ComboBox<T> comboBox) {
        CellUtils.updateItem(cell, stringConverter, null, null, comboBox);
    }

    static <T> void updateItem(Cell<T> cell, StringConverter<T> stringConverter, HBox hBox, Node node, ComboBox<T> comboBox) {
        if (cell.isEmpty()) {
            cell.setText(null);
            cell.setGraphic(null);
        } else if (cell.isEditing()) {
            if (comboBox != null) {
                comboBox.getSelectionModel().select(cell.getItem());
            }
            cell.setText(null);
            if (node != null) {
                hBox.getChildren().setAll((Node[])new Node[]{node, comboBox});
                cell.setGraphic(hBox);
            } else {
                cell.setGraphic(comboBox);
            }
        } else {
            cell.setText(CellUtils.getItemText(cell, stringConverter));
            cell.setGraphic(node);
        }
    }

    static <T> ComboBox<T> createComboBox(final Cell<T> cell, ObservableList<T> observableList, ObjectProperty<StringConverter<T>> objectProperty) {
        final ComboBox<KeyEvent> comboBox = new ComboBox<KeyEvent>(observableList);
        comboBox.converterProperty().bind(objectProperty);
        comboBox.setMaxWidth(Double.MAX_VALUE);
        comboBox.addEventFilter(KeyEvent.KEY_RELEASED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                CellUtils.tryComboBoxCommit(comboBox, cell);
            } else if (keyEvent.getCode() == KeyCode.ESCAPE) {
                cell.cancelEdit();
            }
        });
        comboBox.getEditor().focusedProperty().addListener(observable -> {
            if (!comboBox.isFocused()) {
                CellUtils.tryComboBoxCommit(comboBox, cell);
            }
        });
        boolean bl = CellUtils.listenToComboBoxSkin(comboBox, cell);
        if (!bl) {
            comboBox.skinProperty().addListener(new InvalidationListener(){

                @Override
                public void invalidated(Observable observable) {
                    boolean bl = CellUtils.listenToComboBoxSkin(comboBox, cell);
                    if (bl) {
                        comboBox.skinProperty().removeListener(this);
                    }
                }
            });
        }
        return comboBox;
    }

    private static <T> void tryComboBoxCommit(ComboBox<T> comboBox, Cell<T> cell) {
        StringConverter<T> stringConverter = comboBox.getConverter();
        if (comboBox.isEditable() && stringConverter != null) {
            T t = stringConverter.fromString(comboBox.getEditor().getText());
            cell.commitEdit(t);
        } else {
            cell.commitEdit(comboBox.getValue());
        }
    }

    private static <T> boolean listenToComboBoxSkin(ComboBox<T> comboBox, Cell<T> cell) {
        ComboBoxListViewSkin comboBoxListViewSkin;
        Node node;
        Skin<?> skin = comboBox.getSkin();
        if (skin != null && skin instanceof ComboBoxListViewSkin && (node = (comboBoxListViewSkin = (ComboBoxListViewSkin)skin).getPopupContent()) != null && node instanceof ListView) {
            node.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEvent -> cell.commitEdit(comboBox.getValue()));
            return true;
        }
        return false;
    }
}

