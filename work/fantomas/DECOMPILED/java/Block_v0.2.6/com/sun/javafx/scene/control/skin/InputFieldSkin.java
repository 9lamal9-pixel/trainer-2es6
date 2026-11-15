/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.skin;

import com.sun.javafx.event.EventDispatchChainImpl;
import com.sun.javafx.scene.control.InputField;
import java.util.Collection;
import javafx.beans.InvalidationListener;
import javafx.event.EventDispatchChain;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;

public abstract class InputFieldSkin
implements Skin<InputField> {
    protected InputField control;
    private InnerTextField textField;
    private InvalidationListener InputFieldFocusListener;
    private InvalidationListener InputFieldStyleClassListener;

    public InputFieldSkin(InputField inputField) {
        this.control = inputField;
        this.textField = new InnerTextField(){

            @Override
            public void replaceText(int n, int n2, String string) {
                Object object = InputFieldSkin.this.textField.getText() == null ? "" : InputFieldSkin.this.textField.getText();
                if (InputFieldSkin.this.accept((String)(object = ((String)object).substring(0, n) + string + ((String)object).substring(n2)))) {
                    super.replaceText(n, n2, string);
                }
            }

            @Override
            public void replaceSelection(String string) {
                Object object = InputFieldSkin.this.textField.getText() == null ? "" : InputFieldSkin.this.textField.getText();
                int n = Math.min(InputFieldSkin.this.textField.getAnchor(), InputFieldSkin.this.textField.getCaretPosition());
                int n2 = Math.max(InputFieldSkin.this.textField.getAnchor(), InputFieldSkin.this.textField.getCaretPosition());
                if (InputFieldSkin.this.accept((String)(object = ((String)object).substring(0, n) + string + ((String)object).substring(n2)))) {
                    super.replaceSelection(string);
                }
            }
        };
        this.textField.setId("input-text-field");
        this.textField.setFocusTraversable(false);
        inputField.getStyleClass().addAll((Collection<String>)this.textField.getStyleClass());
        this.textField.getStyleClass().setAll((Collection<String>)inputField.getStyleClass());
        this.InputFieldStyleClassListener = observable -> this.textField.getStyleClass().setAll((Collection<String>)inputField.getStyleClass());
        inputField.getStyleClass().addListener(this.InputFieldStyleClassListener);
        this.textField.promptTextProperty().bind(inputField.promptTextProperty());
        this.textField.prefColumnCountProperty().bind(inputField.prefColumnCountProperty());
        this.textField.textProperty().addListener(observable -> this.updateValue());
        this.InputFieldFocusListener = observable -> this.textField.handleFocus(inputField.isFocused());
        inputField.focusedProperty().addListener(this.InputFieldFocusListener);
        this.updateText();
    }

    @Override
    public InputField getSkinnable() {
        return this.control;
    }

    @Override
    public Node getNode() {
        return this.textField;
    }

    @Override
    public void dispose() {
        this.control.getStyleClass().removeListener(this.InputFieldStyleClassListener);
        this.control.focusedProperty().removeListener(this.InputFieldFocusListener);
        this.textField = null;
    }

    protected abstract boolean accept(String var1);

    protected abstract void updateText();

    protected abstract void updateValue();

    protected TextField getTextField() {
        return this.textField;
    }

    private class InnerTextField
    extends TextField {
        private InnerTextField() {
        }

        public void handleFocus(boolean bl) {
            this.setFocused(bl);
        }

        @Override
        public EventDispatchChain buildEventDispatchChain(EventDispatchChain eventDispatchChain) {
            EventDispatchChainImpl eventDispatchChainImpl = new EventDispatchChainImpl();
            eventDispatchChainImpl.append(InputFieldSkin.this.textField.getEventDispatcher());
            return eventDispatchChainImpl;
        }
    }
}

