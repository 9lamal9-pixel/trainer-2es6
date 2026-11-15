/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.behavior;

import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.scene.control.inputmap.KeyBinding;
import com.sun.javafx.scene.traversal.Direction;
import java.util.ArrayList;
import java.util.List;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class FocusTraversalInputMap<N extends Node> {
    private static final List<InputMap.Mapping<?>> mappings = new ArrayList();

    private FocusTraversalInputMap() {
    }

    public static InputMap.Mapping<?>[] getFocusTraversalMappings() {
        return mappings.toArray(new InputMap.Mapping[mappings.size()]);
    }

    public static <N extends Node> InputMap<N> createInputMap(N n) {
        InputMap<N> inputMap = new InputMap<N>(n);
        inputMap.getMappings().addAll(FocusTraversalInputMap.getFocusTraversalMappings());
        return inputMap;
    }

    public static void traverse(Node node, Direction direction) {
        if (node == null) {
            throw new IllegalArgumentException("Attempting to traverse on a null Node. Most probably a KeyEvent has been fired with a null target specified.");
        }
        NodeHelper.traverse(node, direction);
    }

    public static final void traverseUp(KeyEvent keyEvent) {
        FocusTraversalInputMap.traverse(FocusTraversalInputMap.getNode(keyEvent), Direction.UP);
    }

    public static final void traverseDown(KeyEvent keyEvent) {
        FocusTraversalInputMap.traverse(FocusTraversalInputMap.getNode(keyEvent), Direction.DOWN);
    }

    public static final void traverseLeft(KeyEvent keyEvent) {
        FocusTraversalInputMap.traverse(FocusTraversalInputMap.getNode(keyEvent), Direction.LEFT);
    }

    public static final void traverseRight(KeyEvent keyEvent) {
        FocusTraversalInputMap.traverse(FocusTraversalInputMap.getNode(keyEvent), Direction.RIGHT);
    }

    public static final void traverseNext(KeyEvent keyEvent) {
        FocusTraversalInputMap.traverse(FocusTraversalInputMap.getNode(keyEvent), Direction.NEXT);
    }

    public static final void traversePrevious(KeyEvent keyEvent) {
        FocusTraversalInputMap.traverse(FocusTraversalInputMap.getNode(keyEvent), Direction.PREVIOUS);
    }

    private static Node getNode(KeyEvent keyEvent) {
        EventTarget eventTarget = keyEvent.getTarget();
        if (eventTarget instanceof Node) {
            return (Node)eventTarget;
        }
        return null;
    }

    static {
        mappings.add(new InputMap.KeyMapping(KeyCode.UP, keyEvent -> FocusTraversalInputMap.traverseUp(keyEvent)));
        mappings.add(new InputMap.KeyMapping(KeyCode.DOWN, keyEvent -> FocusTraversalInputMap.traverseDown(keyEvent)));
        mappings.add(new InputMap.KeyMapping(KeyCode.LEFT, keyEvent -> FocusTraversalInputMap.traverseLeft(keyEvent)));
        mappings.add(new InputMap.KeyMapping(KeyCode.RIGHT, keyEvent -> FocusTraversalInputMap.traverseRight(keyEvent)));
        mappings.add(new InputMap.KeyMapping(KeyCode.TAB, keyEvent -> FocusTraversalInputMap.traverseNext(keyEvent)));
        mappings.add(new InputMap.KeyMapping(new KeyBinding(KeyCode.TAB).shift(), keyEvent -> FocusTraversalInputMap.traversePrevious(keyEvent)));
        mappings.add(new InputMap.KeyMapping(new KeyBinding(KeyCode.UP).shift().alt().ctrl(), keyEvent -> FocusTraversalInputMap.traverseUp(keyEvent)));
        mappings.add(new InputMap.KeyMapping(new KeyBinding(KeyCode.DOWN).shift().alt().ctrl(), keyEvent -> FocusTraversalInputMap.traverseDown(keyEvent)));
        mappings.add(new InputMap.KeyMapping(new KeyBinding(KeyCode.LEFT).shift().alt().ctrl(), keyEvent -> FocusTraversalInputMap.traverseLeft(keyEvent)));
        mappings.add(new InputMap.KeyMapping(new KeyBinding(KeyCode.RIGHT).shift().alt().ctrl(), keyEvent -> FocusTraversalInputMap.traverseRight(keyEvent)));
        mappings.add(new InputMap.KeyMapping(new KeyBinding(KeyCode.TAB).shift().alt().ctrl(), keyEvent -> FocusTraversalInputMap.traverseNext(keyEvent)));
        mappings.add(new InputMap.KeyMapping(new KeyBinding(KeyCode.TAB).alt().ctrl(), keyEvent -> FocusTraversalInputMap.traversePrevious(keyEvent)));
    }
}

