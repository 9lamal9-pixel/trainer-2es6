/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.input;

import javafx.beans.NamedArg;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.input.KeyCombination;

public class Mnemonic {
    private KeyCombination keyCombination;
    private Node node;

    public Mnemonic(@NamedArg(value="node") Node node, @NamedArg(value="keyCombination") KeyCombination keyCombination) {
        this.node = node;
        this.keyCombination = keyCombination;
    }

    public KeyCombination getKeyCombination() {
        return this.keyCombination;
    }

    public void setKeyCombination(KeyCombination keyCombination) {
        this.keyCombination = keyCombination;
    }

    public Node getNode() {
        return this.node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public void fire() {
        if (this.node != null) {
            this.node.fireEvent(new ActionEvent());
        }
    }
}

