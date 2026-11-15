/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.behavior;

import com.sun.javafx.PlatformUtil;
import com.sun.javafx.application.PlatformImpl;
import com.sun.javafx.scene.control.Properties;
import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.FocusTraversalInputMap;
import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.scene.control.inputmap.KeyBinding;
import com.sun.javafx.scene.control.skin.FXVK;
import com.sun.javafx.scene.control.skin.resources.ControlResources;
import java.text.Bidi;
import java.util.function.Predicate;
import javafx.application.ConditionalFeature;
import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.IndexRange;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.skin.TextInputControlSkin;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public abstract class TextInputControlBehavior<T extends TextInputControl>
extends BehaviorBase<T> {
    static final boolean SHOW_HANDLES = Properties.IS_TOUCH_SUPPORTED && !PlatformUtil.isIOS();
    public static final String DISABLE_FORWARD_TO_PARENT = "TextInputControlBehavior.disableForwardToParent";
    final T textInputControl;
    protected ContextMenu contextMenu;
    private InvalidationListener textListener = observable -> this.invalidateBidi();
    private final InputMap<T> inputMap;
    private Bidi bidi = null;
    private Boolean mixed = null;
    private Boolean rtlText = null;
    private boolean editing = false;
    private final MenuItem undoMI = new ContextMenuItem("Undo", actionEvent -> this.undo());
    private final MenuItem redoMI = new ContextMenuItem("Redo", actionEvent -> this.redo());
    private final MenuItem cutMI = new ContextMenuItem("Cut", actionEvent -> this.cut());
    private final MenuItem copyMI = new ContextMenuItem("Copy", actionEvent -> ((TextInputControl)this.getNode()).copy());
    private final MenuItem pasteMI = new ContextMenuItem("Paste", actionEvent -> this.paste());
    private final MenuItem deleteMI = new ContextMenuItem("DeleteSelection", actionEvent -> this.deleteSelection());
    private final MenuItem selectWordMI = new ContextMenuItem("SelectWord", actionEvent -> this.selectWord());
    private final MenuItem selectAllMI = new ContextMenuItem("SelectAll", actionEvent -> this.selectAll());
    private final MenuItem separatorMI = new SeparatorMenuItem();

    public TextInputControlBehavior(T t) {
        super(t);
        this.textInputControl = t;
        ((TextInputControl)this.textInputControl).textProperty().addListener(this.textListener);
        this.inputMap = this.createInputMap();
        Predicate<KeyEvent> predicate = keyEvent -> !t.isEditable();
        Predicate<KeyEvent> predicate2 = keyEvent -> !PlatformUtil.isWindows();
        Predicate<KeyEvent> predicate3 = keyEvent -> !PlatformUtil.isLinux();
        InputMap.Mapping[] mappingArray = new InputMap.Mapping[40];
        mappingArray[0] = this.keyMapping(KeyCode.RIGHT, (KeyEvent keyEvent) -> this.nextCharacterVisually(true));
        mappingArray[1] = this.keyMapping(KeyCode.LEFT, (KeyEvent keyEvent) -> this.nextCharacterVisually(false));
        mappingArray[2] = this.keyMapping(KeyCode.UP, (KeyEvent keyEvent) -> t.home());
        mappingArray[3] = this.keyMapping(KeyCode.HOME, (KeyEvent keyEvent) -> t.home());
        mappingArray[4] = this.keyMapping(KeyCode.DOWN, (KeyEvent keyEvent) -> t.end());
        mappingArray[5] = this.keyMapping(KeyCode.END, (KeyEvent keyEvent) -> t.end());
        mappingArray[6] = this.keyMapping(KeyCode.ENTER, this::fire);
        mappingArray[7] = this.keyMapping(new KeyBinding(KeyCode.HOME).shortcut(), (KeyEvent keyEvent) -> t.home());
        mappingArray[8] = this.keyMapping(new KeyBinding(KeyCode.END).shortcut(), (KeyEvent keyEvent) -> t.end());
        mappingArray[9] = this.keyMapping(new KeyBinding(KeyCode.BACK_SPACE), keyEvent -> this.deletePreviousChar(), predicate);
        mappingArray[10] = this.keyMapping(new KeyBinding(KeyCode.BACK_SPACE).shift(), keyEvent -> this.deletePreviousChar(), predicate);
        mappingArray[11] = this.keyMapping(new KeyBinding(KeyCode.DELETE), keyEvent -> this.deleteNextChar(), predicate);
        mappingArray[12] = this.keyMapping(new KeyBinding(KeyCode.X).shortcut(), keyEvent -> this.cut(), predicate);
        mappingArray[13] = this.keyMapping(new KeyBinding(KeyCode.CUT), keyEvent -> this.cut(), predicate);
        mappingArray[14] = this.keyMapping(new KeyBinding(KeyCode.C).shortcut(), (KeyEvent keyEvent) -> t.copy());
        mappingArray[15] = this.keyMapping(new KeyBinding(KeyCode.INSERT).shortcut(), (KeyEvent keyEvent) -> t.copy());
        mappingArray[16] = this.keyMapping(KeyCode.COPY, (KeyEvent keyEvent) -> t.copy());
        mappingArray[17] = this.keyMapping(new KeyBinding(KeyCode.V).shortcut(), keyEvent -> this.paste(), predicate);
        mappingArray[18] = this.keyMapping(new KeyBinding(KeyCode.PASTE), keyEvent -> this.paste(), predicate);
        mappingArray[19] = this.keyMapping(new KeyBinding(KeyCode.INSERT).shift(), keyEvent -> this.paste(), predicate);
        mappingArray[20] = this.keyMapping(new KeyBinding(KeyCode.RIGHT).shift(), (KeyEvent keyEvent) -> this.selectRight());
        mappingArray[21] = this.keyMapping(new KeyBinding(KeyCode.LEFT).shift(), (KeyEvent keyEvent) -> this.selectLeft());
        mappingArray[22] = this.keyMapping(new KeyBinding(KeyCode.UP).shift(), (KeyEvent keyEvent) -> this.selectHome());
        mappingArray[23] = this.keyMapping(new KeyBinding(KeyCode.DOWN).shift(), (KeyEvent keyEvent) -> this.selectEnd());
        mappingArray[24] = this.keyMapping(new KeyBinding(KeyCode.HOME).shortcut().shift(), (KeyEvent keyEvent) -> this.selectHome());
        mappingArray[25] = this.keyMapping(new KeyBinding(KeyCode.END).shortcut().shift(), (KeyEvent keyEvent) -> this.selectEnd());
        mappingArray[26] = this.keyMapping(new KeyBinding(KeyCode.A).shortcut(), (KeyEvent keyEvent) -> t.selectAll());
        mappingArray[27] = new InputMap.KeyMapping(new KeyBinding(KeyCode.TAB), FocusTraversalInputMap::traverseNext);
        mappingArray[28] = new InputMap.KeyMapping(new KeyBinding(KeyCode.TAB).shift(), FocusTraversalInputMap::traversePrevious);
        mappingArray[29] = new InputMap.KeyMapping(new KeyBinding(KeyCode.TAB).ctrl(), FocusTraversalInputMap::traverseNext);
        mappingArray[30] = new InputMap.KeyMapping(new KeyBinding(KeyCode.TAB).ctrl().shift(), FocusTraversalInputMap::traversePrevious);
        InputMap.KeyMapping keyMapping = new InputMap.KeyMapping(KeyCode.ESCAPE, this::cancelEdit);
        mappingArray[31] = keyMapping;
        mappingArray[32] = this.keyMapping(new KeyBinding(KeyCode.Z).shortcut(), (KeyEvent keyEvent) -> this.undo());
        mappingArray[33] = this.keyMapping(new KeyBinding(null, KeyEvent.KEY_TYPED).alt(KeyBinding.OptionalBoolean.ANY).shift(KeyBinding.OptionalBoolean.ANY).ctrl(KeyBinding.OptionalBoolean.ANY).meta(KeyBinding.OptionalBoolean.ANY), this::defaultKeyTyped);
        InputMap.KeyMapping keyMapping2 = this.keyMapping(new KeyBinding(null, KeyEvent.KEY_PRESSED).shift(KeyBinding.OptionalBoolean.ANY), (KeyEvent keyEvent) -> {
            if (!keyEvent.getCode().isFunctionKey()) {
                keyEvent.consume();
            }
        });
        mappingArray[34] = keyMapping2;
        mappingArray[35] = new InputMap.KeyMapping(new KeyBinding(KeyCode.DIGIT9).ctrl().shift(), keyEvent -> FXVK.toggleUseVK(this.textInputControl), keyEvent -> !PlatformImpl.isSupported(ConditionalFeature.VIRTUAL_KEYBOARD));
        mappingArray[36] = new InputMap.MouseMapping(MouseEvent.MOUSE_PRESSED, this::mousePressed);
        mappingArray[37] = new InputMap.MouseMapping(MouseEvent.MOUSE_DRAGGED, this::mouseDragged);
        mappingArray[38] = new InputMap.MouseMapping(MouseEvent.MOUSE_RELEASED, this::mouseReleased);
        mappingArray[39] = new InputMap.Mapping<ContextMenuEvent>(ContextMenuEvent.CONTEXT_MENU_REQUESTED, this::contextMenuRequested){

            @Override
            public int getSpecificity(Event event) {
                return 1;
            }
        };
        this.addDefaultMapping(this.inputMap, mappingArray);
        keyMapping.setAutoConsume(false);
        keyMapping2.setAutoConsume(false);
        InputMap<T> inputMap = new InputMap<T>(t);
        inputMap.setInterceptor(event -> !PlatformUtil.isMac());
        inputMap.getMappings().addAll(this.keyMapping(new KeyBinding(KeyCode.HOME).shift(), (KeyEvent keyEvent) -> this.selectHomeExtend()), this.keyMapping(new KeyBinding(KeyCode.END).shift(), (KeyEvent keyEvent) -> this.selectEndExtend()), this.keyMapping(new KeyBinding(KeyCode.LEFT).shortcut(), (KeyEvent keyEvent) -> t.home()), this.keyMapping(new KeyBinding(KeyCode.RIGHT).shortcut(), (KeyEvent keyEvent) -> t.end()), this.keyMapping(new KeyBinding(KeyCode.LEFT).alt(), (KeyEvent keyEvent) -> this.leftWord()), this.keyMapping(new KeyBinding(KeyCode.RIGHT).alt(), (KeyEvent keyEvent) -> this.rightWord()), this.keyMapping(new KeyBinding(KeyCode.DELETE).alt(), (KeyEvent keyEvent) -> this.deleteNextWord()), this.keyMapping(new KeyBinding(KeyCode.BACK_SPACE).alt(), (KeyEvent keyEvent) -> this.deletePreviousWord()), this.keyMapping(new KeyBinding(KeyCode.BACK_SPACE).shortcut(), (KeyEvent keyEvent) -> this.deleteFromLineStart()), this.keyMapping(new KeyBinding(KeyCode.Z).shortcut().shift(), (KeyEvent keyEvent) -> this.redo()), this.keyMapping(new KeyBinding(KeyCode.LEFT).shortcut().shift(), (KeyEvent keyEvent) -> this.selectHomeExtend()), this.keyMapping(new KeyBinding(KeyCode.RIGHT).shortcut().shift(), (KeyEvent keyEvent) -> this.selectEndExtend()), this.keyMapping(new KeyBinding(KeyCode.LEFT).shift().alt(), (KeyEvent keyEvent) -> this.selectLeftWord()), this.keyMapping(new KeyBinding(KeyCode.RIGHT).shift().alt(), (KeyEvent keyEvent) -> this.selectRightWord()));
        this.addDefaultChildMap(this.inputMap, inputMap);
        InputMap<T> inputMap2 = new InputMap<T>(t);
        inputMap2.setInterceptor(event -> PlatformUtil.isMac());
        inputMap2.getMappings().addAll(this.keyMapping(new KeyBinding(KeyCode.HOME).shift(), (KeyEvent keyEvent) -> this.selectHome()), this.keyMapping(new KeyBinding(KeyCode.END).shift(), (KeyEvent keyEvent) -> this.selectEnd()), this.keyMapping(new KeyBinding(KeyCode.LEFT).ctrl(), (KeyEvent keyEvent) -> this.leftWord()), this.keyMapping(new KeyBinding(KeyCode.RIGHT).ctrl(), (KeyEvent keyEvent) -> this.rightWord()), this.keyMapping(new KeyBinding(KeyCode.H).ctrl(), (KeyEvent keyEvent) -> this.deletePreviousChar()), this.keyMapping(new KeyBinding(KeyCode.DELETE).ctrl(), (KeyEvent keyEvent) -> this.deleteNextWord()), this.keyMapping(new KeyBinding(KeyCode.BACK_SPACE).ctrl(), (KeyEvent keyEvent) -> this.deletePreviousWord()), this.keyMapping(new KeyBinding(KeyCode.BACK_SLASH).ctrl(), (KeyEvent keyEvent) -> t.deselect()), this.keyMapping(new KeyBinding(KeyCode.Y).ctrl(), keyEvent -> this.redo(), predicate2), this.keyMapping(new KeyBinding(KeyCode.Z).ctrl().shift(), keyEvent -> this.redo(), predicate3), this.keyMapping(new KeyBinding(KeyCode.LEFT).ctrl().shift(), (KeyEvent keyEvent) -> this.selectLeftWord()), this.keyMapping(new KeyBinding(KeyCode.RIGHT).ctrl().shift(), (KeyEvent keyEvent) -> this.selectRightWord()));
        this.addDefaultChildMap(this.inputMap, inputMap2);
        this.addKeyPadMappings(this.inputMap);
        ((TextInputControl)this.textInputControl).textProperty().addListener(this.textListener);
        this.contextMenu = new ContextMenu();
    }

    @Override
    public InputMap<T> getInputMap() {
        return this.inputMap;
    }

    protected void addKeyPadMappings(InputMap<T> inputMap) {
        InputMap<TextInputControl> inputMap2 = new InputMap<TextInputControl>((TextInputControl)this.getNode());
        for (Object e : inputMap.getMappings()) {
            InputMap.KeyMapping keyMapping;
            KeyBinding keyBinding;
            if (!(e instanceof InputMap.KeyMapping) || (keyBinding = (KeyBinding)(keyMapping = (InputMap.KeyMapping)e).getMappingKey()).getCode() == null) continue;
            KeyCode keyCode = null;
            switch (keyBinding.getCode()) {
                case LEFT: {
                    keyCode = KeyCode.KP_LEFT;
                    break;
                }
                case RIGHT: {
                    keyCode = KeyCode.KP_RIGHT;
                    break;
                }
                case UP: {
                    keyCode = KeyCode.KP_UP;
                    break;
                }
                case DOWN: {
                    keyCode = KeyCode.KP_DOWN;
                    break;
                }
            }
            if (keyCode == null) continue;
            KeyBinding keyBinding2 = new KeyBinding(keyCode).shift(keyBinding.getShift()).ctrl(keyBinding.getCtrl()).alt(keyBinding.getAlt()).meta(keyBinding.getMeta());
            inputMap2.getMappings().add(new InputMap.KeyMapping(keyBinding2, keyMapping.getEventHandler()));
        }
        for (Object e : inputMap2.getMappings()) {
            inputMap.getMappings().add((InputMap.KeyMapping)e);
        }
        for (Object e : inputMap.getChildInputMaps()) {
            this.addKeyPadMappings((InputMap)e);
        }
    }

    protected InputMap.KeyMapping keyMapping(KeyCode keyCode, EventHandler<KeyEvent> eventHandler) {
        return this.keyMapping(new KeyBinding(keyCode), eventHandler);
    }

    protected InputMap.KeyMapping keyMapping(KeyBinding keyBinding, EventHandler<KeyEvent> eventHandler) {
        return this.keyMapping(keyBinding, eventHandler, null);
    }

    protected InputMap.KeyMapping keyMapping(KeyBinding keyBinding, EventHandler<KeyEvent> eventHandler, Predicate<KeyEvent> predicate) {
        return new InputMap.KeyMapping(keyBinding, keyEvent -> {
            this.setCaretAnimating(false);
            eventHandler.handle((KeyEvent)keyEvent);
            this.setCaretAnimating(true);
        }, predicate);
    }

    @Override
    public void dispose() {
        ((TextInputControl)this.textInputControl).textProperty().removeListener(this.textListener);
        super.dispose();
    }

    protected abstract void deleteChar(boolean var1);

    protected abstract void replaceText(int var1, int var2, String var3);

    protected abstract void setCaretAnimating(boolean var1);

    protected abstract void deleteFromLineStart();

    protected abstract void mousePressed(MouseEvent var1);

    protected abstract void mouseDragged(MouseEvent var1);

    protected abstract void mouseReleased(MouseEvent var1);

    protected abstract void contextMenuRequested(ContextMenuEvent var1);

    private void defaultKeyTyped(KeyEvent keyEvent) {
        TextInputControl textInputControl = (TextInputControl)this.getNode();
        if (!textInputControl.isEditable() || textInputControl.isDisabled()) {
            return;
        }
        String string = keyEvent.getCharacter();
        if (string.length() == 0) {
            return;
        }
        if ((keyEvent.isControlDown() || keyEvent.isAltDown() || PlatformUtil.isMac() && keyEvent.isMetaDown()) && (!keyEvent.isControlDown() && !PlatformUtil.isMac() || !keyEvent.isAltDown())) {
            return;
        }
        this.setEditing(true);
        if (string.charAt(0) > '\u001f' && string.charAt(0) != '\u007f' && !keyEvent.isMetaDown()) {
            IndexRange indexRange = textInputControl.getSelection();
            int n = indexRange.getStart();
            int n2 = indexRange.getEnd();
            this.replaceText(n, n2, string);
        }
        this.setEditing(false);
    }

    private void invalidateBidi() {
        this.bidi = null;
        this.mixed = null;
        this.rtlText = null;
    }

    private Bidi getBidi() {
        if (this.bidi == null) {
            this.bidi = new Bidi(((TextInputControl)this.textInputControl).textProperty().getValueSafe(), ((Node)this.textInputControl).getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT ? 1 : 0);
        }
        return this.bidi;
    }

    protected boolean isMixed() {
        if (this.mixed == null) {
            this.mixed = this.getBidi().isMixed();
        }
        return this.mixed;
    }

    protected boolean isRTLText() {
        if (this.rtlText == null) {
            Bidi bidi = this.getBidi();
            this.rtlText = bidi.isRightToLeft() || this.isMixed() && ((Node)this.textInputControl).getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT;
        }
        return this.rtlText;
    }

    private void nextCharacterVisually(boolean bl) {
        if (this.isMixed()) {
            TextInputControlSkin textInputControlSkin = (TextInputControlSkin)((Control)this.textInputControl).getSkin();
            textInputControlSkin.moveCaret(TextInputControlSkin.TextUnit.CHARACTER, bl ? TextInputControlSkin.Direction.RIGHT : TextInputControlSkin.Direction.LEFT, false);
        } else if (bl != this.isRTLText()) {
            ((TextInputControl)this.textInputControl).forward();
        } else {
            ((TextInputControl)this.textInputControl).backward();
        }
    }

    private void selectLeft() {
        if (this.isRTLText()) {
            ((TextInputControl)this.textInputControl).selectForward();
        } else {
            ((TextInputControl)this.textInputControl).selectBackward();
        }
    }

    private void selectRight() {
        if (this.isRTLText()) {
            ((TextInputControl)this.textInputControl).selectBackward();
        } else {
            ((TextInputControl)this.textInputControl).selectForward();
        }
    }

    private void deletePreviousChar() {
        this.setEditing(true);
        this.deleteChar(true);
        this.setEditing(false);
    }

    private void deleteNextChar() {
        this.setEditing(true);
        this.deleteChar(false);
        this.setEditing(false);
    }

    protected void deletePreviousWord() {
        this.setEditing(true);
        TextInputControl textInputControl = (TextInputControl)this.getNode();
        int n = textInputControl.getCaretPosition();
        if (n > 0) {
            textInputControl.previousWord();
            int n2 = textInputControl.getCaretPosition();
            this.replaceText(n2, n, "");
        }
        this.setEditing(false);
    }

    protected void deleteNextWord() {
        this.setEditing(true);
        TextInputControl textInputControl = (TextInputControl)this.getNode();
        int n = textInputControl.getCaretPosition();
        if (n < textInputControl.getLength()) {
            this.nextWord();
            int n2 = textInputControl.getCaretPosition();
            this.replaceText(n, n2, "");
        }
        this.setEditing(false);
    }

    public void deleteSelection() {
        this.setEditing(true);
        TextInputControl textInputControl = (TextInputControl)this.getNode();
        IndexRange indexRange = textInputControl.getSelection();
        if (indexRange.getLength() > 0) {
            this.deleteChar(false);
        }
        this.setEditing(false);
    }

    public void cut() {
        this.setEditing(true);
        ((TextInputControl)this.getNode()).cut();
        this.setEditing(false);
    }

    public void paste() {
        this.setEditing(true);
        ((TextInputControl)this.getNode()).paste();
        this.setEditing(false);
    }

    public void undo() {
        this.setEditing(true);
        ((TextInputControl)this.getNode()).undo();
        this.setEditing(false);
    }

    public void redo() {
        this.setEditing(true);
        ((TextInputControl)this.getNode()).redo();
        this.setEditing(false);
    }

    protected void selectPreviousWord() {
        ((TextInputControl)this.getNode()).selectPreviousWord();
    }

    public void selectNextWord() {
        TextInputControl textInputControl = (TextInputControl)this.getNode();
        if (PlatformUtil.isMac() || PlatformUtil.isLinux()) {
            textInputControl.selectEndOfNextWord();
        } else {
            textInputControl.selectNextWord();
        }
    }

    private void selectLeftWord() {
        if (this.isRTLText()) {
            this.selectNextWord();
        } else {
            this.selectPreviousWord();
        }
    }

    private void selectRightWord() {
        if (this.isRTLText()) {
            this.selectPreviousWord();
        } else {
            this.selectNextWord();
        }
    }

    protected void selectWord() {
        TextInputControl textInputControl = (TextInputControl)this.getNode();
        textInputControl.previousWord();
        if (PlatformUtil.isWindows()) {
            textInputControl.selectNextWord();
        } else {
            textInputControl.selectEndOfNextWord();
        }
        if (SHOW_HANDLES && this.contextMenu.isShowing()) {
            this.populateContextMenu();
        }
    }

    protected void selectAll() {
        ((TextInputControl)this.getNode()).selectAll();
        if (SHOW_HANDLES && this.contextMenu.isShowing()) {
            this.populateContextMenu();
        }
    }

    protected void previousWord() {
        ((TextInputControl)this.getNode()).previousWord();
    }

    protected void nextWord() {
        TextInputControl textInputControl = (TextInputControl)this.getNode();
        if (PlatformUtil.isMac() || PlatformUtil.isLinux()) {
            textInputControl.endOfNextWord();
        } else {
            textInputControl.nextWord();
        }
    }

    private void leftWord() {
        if (this.isRTLText()) {
            this.nextWord();
        } else {
            this.previousWord();
        }
    }

    private void rightWord() {
        if (this.isRTLText()) {
            this.previousWord();
        } else {
            this.nextWord();
        }
    }

    protected void fire(KeyEvent keyEvent) {
    }

    protected void cancelEdit(KeyEvent keyEvent) {
        this.forwardToParent(keyEvent);
    }

    protected void forwardToParent(KeyEvent keyEvent) {
        if (((TextInputControl)this.getNode()).getProperties().containsKey(DISABLE_FORWARD_TO_PARENT)) {
            return;
        }
        if (((TextInputControl)this.getNode()).getParent() != null) {
            ((TextInputControl)this.getNode()).getParent().fireEvent(keyEvent);
        }
    }

    protected void selectHome() {
        ((TextInputControl)this.getNode()).selectHome();
    }

    protected void selectEnd() {
        ((TextInputControl)this.getNode()).selectEnd();
    }

    protected void selectHomeExtend() {
        ((TextInputControl)this.getNode()).extendSelection(0);
    }

    protected void selectEndExtend() {
        TextInputControl textInputControl = (TextInputControl)this.getNode();
        textInputControl.extendSelection(textInputControl.getLength());
    }

    protected void setEditing(boolean bl) {
        this.editing = bl;
    }

    public boolean isEditing() {
        return this.editing;
    }

    protected void populateContextMenu() {
        TextInputControl textInputControl = (TextInputControl)this.getNode();
        boolean bl = textInputControl.isEditable();
        boolean bl2 = textInputControl.getLength() > 0;
        boolean bl3 = textInputControl.getSelection().getLength() > 0;
        boolean bl4 = textInputControl.getSelection().getLength() == textInputControl.getLength();
        boolean bl5 = textInputControl instanceof PasswordField;
        ObservableList<MenuItem> observableList = this.contextMenu.getItems();
        if (SHOW_HANDLES) {
            observableList.clear();
            if (!bl5 && bl3) {
                if (bl) {
                    observableList.add(this.cutMI);
                }
                observableList.add(this.copyMI);
            }
            if (bl && Clipboard.getSystemClipboard().hasString()) {
                observableList.add(this.pasteMI);
            }
            if (bl2 && !bl4) {
                if (!bl3 && !(textInputControl instanceof PasswordField)) {
                    observableList.add(this.selectWordMI);
                }
                observableList.add(this.selectAllMI);
            }
            this.selectWordMI.getProperties().put("refreshMenu", Boolean.TRUE);
            this.selectAllMI.getProperties().put("refreshMenu", Boolean.TRUE);
        } else {
            if (bl) {
                observableList.setAll((MenuItem[])new MenuItem[]{this.undoMI, this.redoMI, this.cutMI, this.copyMI, this.pasteMI, this.deleteMI, this.separatorMI, this.selectAllMI});
            } else {
                observableList.setAll((MenuItem[])new MenuItem[]{this.copyMI, this.separatorMI, this.selectAllMI});
            }
            this.undoMI.setDisable(!((TextInputControl)this.getNode()).isUndoable());
            this.redoMI.setDisable(!((TextInputControl)this.getNode()).isRedoable());
            this.cutMI.setDisable(bl5 || !bl3);
            this.copyMI.setDisable(bl5 || !bl3);
            this.pasteMI.setDisable(!Clipboard.getSystemClipboard().hasString());
            this.deleteMI.setDisable(!bl3);
        }
    }

    private static class ContextMenuItem
    extends MenuItem {
        ContextMenuItem(String string, EventHandler<ActionEvent> eventHandler) {
            super(ControlResources.getString("TextInputControl.menu." + string));
            this.setOnAction(eventHandler);
        }
    }
}

