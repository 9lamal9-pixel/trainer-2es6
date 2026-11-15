/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import com.sun.javafx.scene.control.skin.resources.ControlResources;
import javafx.beans.NamedArg;
import javafx.scene.control.ButtonBar;

public final class ButtonType {
    public static final ButtonType APPLY = new ButtonType("Dialog.apply.button", null, ButtonBar.ButtonData.APPLY);
    public static final ButtonType OK = new ButtonType("Dialog.ok.button", null, ButtonBar.ButtonData.OK_DONE);
    public static final ButtonType CANCEL = new ButtonType("Dialog.cancel.button", null, ButtonBar.ButtonData.CANCEL_CLOSE);
    public static final ButtonType CLOSE = new ButtonType("Dialog.close.button", null, ButtonBar.ButtonData.CANCEL_CLOSE);
    public static final ButtonType YES = new ButtonType("Dialog.yes.button", null, ButtonBar.ButtonData.YES);
    public static final ButtonType NO = new ButtonType("Dialog.no.button", null, ButtonBar.ButtonData.NO);
    public static final ButtonType FINISH = new ButtonType("Dialog.finish.button", null, ButtonBar.ButtonData.FINISH);
    public static final ButtonType NEXT = new ButtonType("Dialog.next.button", null, ButtonBar.ButtonData.NEXT_FORWARD);
    public static final ButtonType PREVIOUS = new ButtonType("Dialog.previous.button", null, ButtonBar.ButtonData.BACK_PREVIOUS);
    private final String key;
    private final String text;
    private final ButtonBar.ButtonData buttonData;

    public ButtonType(@NamedArg(value="text") String string) {
        this(string, ButtonBar.ButtonData.OTHER);
    }

    public ButtonType(@NamedArg(value="text") String string, @NamedArg(value="buttonData") ButtonBar.ButtonData buttonData) {
        this(null, string, buttonData);
    }

    private ButtonType(String string, String string2, ButtonBar.ButtonData buttonData) {
        this.key = string;
        this.text = string2;
        this.buttonData = buttonData;
    }

    public final ButtonBar.ButtonData getButtonData() {
        return this.buttonData;
    }

    public final String getText() {
        if (this.text == null && this.key != null) {
            return ControlResources.getString(this.key);
        }
        return this.text;
    }

    public String toString() {
        return "ButtonType [text=" + this.getText() + ", buttonData=" + this.getButtonData() + "]";
    }
}

