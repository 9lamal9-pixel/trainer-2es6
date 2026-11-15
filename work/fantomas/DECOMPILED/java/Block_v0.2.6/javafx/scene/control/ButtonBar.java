/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import com.sun.javafx.util.Utils;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.css.StyleableProperty;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.ButtonBarSkin;

public class ButtonBar
extends Control {
    public static final String BUTTON_ORDER_WINDOWS = "L_E+U+FBXI_YNOCAH_R";
    public static final String BUTTON_ORDER_MAC_OS = "L_HE+U+FBIX_NCYOA_R";
    public static final String BUTTON_ORDER_LINUX = "L_HE+UNYACBXIO_R";
    public static final String BUTTON_ORDER_NONE = "";
    private ObservableList<Node> buttons = FXCollections.observableArrayList();
    private final StringProperty buttonOrderProperty = new SimpleStringProperty(this, "buttonOrder");
    private final DoubleProperty buttonMinWidthProperty = new SimpleDoubleProperty(this, "buttonMinWidthProperty");

    public static void setButtonData(Node node, ButtonData buttonData) {
        ObservableMap<Object, Object> observableMap = node.getProperties();
        ObjectProperty objectProperty = observableMap.getOrDefault("javafx.scene.control.ButtonBar.ButtonData", new SimpleObjectProperty<ButtonData>(node, "buttonData", buttonData));
        objectProperty.set(buttonData);
        observableMap.putIfAbsent("javafx.scene.control.ButtonBar.ButtonData", objectProperty);
    }

    public static ButtonData getButtonData(Node node) {
        ObservableMap<Object, Object> observableMap = node.getProperties();
        if (observableMap.containsKey("javafx.scene.control.ButtonBar.ButtonData")) {
            ObjectProperty objectProperty = (ObjectProperty)observableMap.get("javafx.scene.control.ButtonBar.ButtonData");
            return objectProperty == null ? null : (ButtonData)((Object)objectProperty.get());
        }
        return null;
    }

    public static void setButtonUniformSize(Node node, boolean bl) {
        if (bl) {
            node.getProperties().remove("javafx.scene.control.ButtonBar.independentSize");
        } else {
            node.getProperties().put("javafx.scene.control.ButtonBar.independentSize", bl);
        }
    }

    public static boolean isButtonUniformSize(Node node) {
        return (Boolean)node.getProperties().getOrDefault("javafx.scene.control.ButtonBar.independentSize", true);
    }

    public ButtonBar() {
        this(null);
    }

    public ButtonBar(String string) {
        boolean bl;
        this.getStyleClass().add("button-bar");
        ((StyleableProperty)((Object)this.focusTraversableProperty())).applyStyle(null, Boolean.FALSE);
        boolean bl2 = bl = string == null || string.isEmpty();
        if (Utils.isMac()) {
            this.setButtonOrder(bl ? BUTTON_ORDER_MAC_OS : string);
            this.setButtonMinWidth(70.0);
        } else if (Utils.isUnix()) {
            this.setButtonOrder(bl ? BUTTON_ORDER_LINUX : string);
            this.setButtonMinWidth(85.0);
        } else {
            this.setButtonOrder(bl ? BUTTON_ORDER_WINDOWS : string);
            this.setButtonMinWidth(75.0);
        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ButtonBarSkin(this);
    }

    public final ObservableList<Node> getButtons() {
        return this.buttons;
    }

    public final StringProperty buttonOrderProperty() {
        return this.buttonOrderProperty;
    }

    public final void setButtonOrder(String string) {
        this.buttonOrderProperty.set(string);
    }

    public final String getButtonOrder() {
        return (String)this.buttonOrderProperty.get();
    }

    public final DoubleProperty buttonMinWidthProperty() {
        return this.buttonMinWidthProperty;
    }

    public final void setButtonMinWidth(double d) {
        this.buttonMinWidthProperty.set(d);
    }

    public final double getButtonMinWidth() {
        return this.buttonMinWidthProperty.get();
    }

    @Override
    protected Boolean getInitialFocusTraversable() {
        return Boolean.FALSE;
    }

    public static enum ButtonData {
        LEFT("L", false, false),
        RIGHT("R", false, false),
        HELP("H", false, false),
        HELP_2("E", false, false),
        YES("Y", false, true),
        NO("N", true, false),
        NEXT_FORWARD("X", false, true),
        BACK_PREVIOUS("B", false, false),
        FINISH("I", false, true),
        APPLY("A", false, false),
        CANCEL_CLOSE("C", true, false),
        OK_DONE("O", false, true),
        OTHER("U", false, false),
        BIG_GAP("+", false, false),
        SMALL_GAP("_", false, false);

        private final String typeCode;
        private final boolean cancelButton;
        private final boolean defaultButton;

        private ButtonData(String string2, boolean bl, boolean bl2) {
            this.typeCode = string2;
            this.cancelButton = bl;
            this.defaultButton = bl2;
        }

        public String getTypeCode() {
            return this.typeCode;
        }

        public final boolean isCancelButton() {
            return this.cancelButton;
        }

        public final boolean isDefaultButton() {
            return this.defaultButton;
        }
    }
}

