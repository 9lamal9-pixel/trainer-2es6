/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.behavior;

import com.sun.javafx.PlatformUtil;
import com.sun.javafx.geom.transform.Affine3D;
import com.sun.javafx.scene.control.Properties;
import com.sun.javafx.scene.control.behavior.TextFieldBehavior;
import com.sun.javafx.scene.control.behavior.TextInputControlBehavior;
import com.sun.javafx.scene.control.behavior.TwoLevelFocusBehavior;
import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.scene.control.inputmap.KeyBinding;
import com.sun.javafx.stage.WindowHelper;
import com.sun.javafx.util.Utils;
import java.util.function.Predicate;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.skin.TextAreaSkin;
import javafx.scene.control.skin.TextInputControlSkin;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.HitInfo;
import javafx.stage.Screen;
import javafx.stage.Window;

public class TextAreaBehavior
extends TextInputControlBehavior<TextArea> {
    private TextAreaSkin skin;
    private TwoLevelFocusBehavior tlFocus;
    private boolean focusGainedByMouseClick = false;
    private boolean shiftDown = false;
    private boolean deferClick = false;

    public TextAreaBehavior(TextArea textArea) {
        super(textArea);
        if (Properties.IS_TOUCH_SUPPORTED) {
            this.contextMenu.getStyleClass().add("text-input-context-menu");
        }
        Predicate<KeyEvent> predicate = keyEvent -> !textArea.isEditable();
        InputMap<TextArea> inputMap = new InputMap<TextArea>(textArea);
        inputMap.getMappings().addAll(this.keyMapping(KeyCode.HOME, (KeyEvent keyEvent) -> this.lineStart(false)), this.keyMapping(KeyCode.END, (KeyEvent keyEvent) -> this.lineEnd(false)), this.keyMapping(KeyCode.UP, (KeyEvent keyEvent) -> this.skin.moveCaret(TextInputControlSkin.TextUnit.LINE, TextInputControlSkin.Direction.UP, false)), this.keyMapping(KeyCode.DOWN, (KeyEvent keyEvent) -> this.skin.moveCaret(TextInputControlSkin.TextUnit.LINE, TextInputControlSkin.Direction.DOWN, false)), this.keyMapping(KeyCode.PAGE_UP, (KeyEvent keyEvent) -> this.skin.moveCaret(TextInputControlSkin.TextUnit.PAGE, TextInputControlSkin.Direction.UP, false)), this.keyMapping(KeyCode.PAGE_DOWN, (KeyEvent keyEvent) -> this.skin.moveCaret(TextInputControlSkin.TextUnit.PAGE, TextInputControlSkin.Direction.DOWN, false)), this.keyMapping(new KeyBinding(KeyCode.HOME).shift(), (KeyEvent keyEvent) -> this.lineStart(true)), this.keyMapping(new KeyBinding(KeyCode.END).shift(), (KeyEvent keyEvent) -> this.lineEnd(true)), this.keyMapping(new KeyBinding(KeyCode.UP).shift(), (KeyEvent keyEvent) -> this.skin.moveCaret(TextInputControlSkin.TextUnit.LINE, TextInputControlSkin.Direction.UP, true)), this.keyMapping(new KeyBinding(KeyCode.DOWN).shift(), (KeyEvent keyEvent) -> this.skin.moveCaret(TextInputControlSkin.TextUnit.LINE, TextInputControlSkin.Direction.DOWN, true)), this.keyMapping(new KeyBinding(KeyCode.PAGE_UP).shift(), (KeyEvent keyEvent) -> this.skin.moveCaret(TextInputControlSkin.TextUnit.PAGE, TextInputControlSkin.Direction.UP, true)), this.keyMapping(new KeyBinding(KeyCode.PAGE_DOWN).shift(), (KeyEvent keyEvent) -> this.skin.moveCaret(TextInputControlSkin.TextUnit.PAGE, TextInputControlSkin.Direction.DOWN, true)), this.keyMapping(new KeyBinding(KeyCode.ENTER), keyEvent -> this.insertNewLine(), predicate), this.keyMapping(new KeyBinding(KeyCode.TAB), keyEvent -> this.insertTab(), predicate));
        this.addDefaultChildMap(this.getInputMap(), inputMap);
        InputMap<TextArea> inputMap2 = new InputMap<TextArea>(textArea);
        inputMap2.setInterceptor(event -> !PlatformUtil.isMac());
        inputMap2.getMappings().addAll(this.keyMapping(new KeyBinding(KeyCode.LEFT).shortcut(), (KeyEvent keyEvent) -> this.lineStart(false)), this.keyMapping(new KeyBinding(KeyCode.RIGHT).shortcut(), (KeyEvent keyEvent) -> this.lineEnd(false)), this.keyMapping(new KeyBinding(KeyCode.UP).shortcut(), (KeyEvent keyEvent) -> textArea.home()), this.keyMapping(new KeyBinding(KeyCode.DOWN).shortcut(), (KeyEvent keyEvent) -> textArea.end()), this.keyMapping(new KeyBinding(KeyCode.LEFT).shortcut().shift(), (KeyEvent keyEvent) -> this.lineStart(true)), this.keyMapping(new KeyBinding(KeyCode.RIGHT).shortcut().shift(), (KeyEvent keyEvent) -> this.lineEnd(true)), this.keyMapping(new KeyBinding(KeyCode.UP).shortcut().shift(), (KeyEvent keyEvent) -> this.selectHomeExtend()), this.keyMapping(new KeyBinding(KeyCode.DOWN).shortcut().shift(), (KeyEvent keyEvent) -> this.selectEndExtend()), this.keyMapping(new KeyBinding(KeyCode.UP).alt(), (KeyEvent keyEvent) -> this.skin.moveCaret(TextInputControlSkin.TextUnit.PARAGRAPH, TextInputControlSkin.Direction.UP, false)), this.keyMapping(new KeyBinding(KeyCode.DOWN).alt(), (KeyEvent keyEvent) -> this.skin.moveCaret(TextInputControlSkin.TextUnit.PARAGRAPH, TextInputControlSkin.Direction.DOWN, false)), this.keyMapping(new KeyBinding(KeyCode.UP).alt().shift(), (KeyEvent keyEvent) -> this.skin.moveCaret(TextInputControlSkin.TextUnit.PARAGRAPH, TextInputControlSkin.Direction.UP, true)), this.keyMapping(new KeyBinding(KeyCode.DOWN).alt().shift(), (KeyEvent keyEvent) -> this.skin.moveCaret(TextInputControlSkin.TextUnit.PARAGRAPH, TextInputControlSkin.Direction.DOWN, true)));
        this.addDefaultChildMap(inputMap, inputMap2);
        InputMap<TextArea> inputMap3 = new InputMap<TextArea>(textArea);
        inputMap3.setInterceptor(event -> PlatformUtil.isMac());
        inputMap3.getMappings().addAll(this.keyMapping(new KeyBinding(KeyCode.UP).ctrl(), (KeyEvent keyEvent) -> this.skin.moveCaret(TextInputControlSkin.TextUnit.PARAGRAPH, TextInputControlSkin.Direction.UP, false)), this.keyMapping(new KeyBinding(KeyCode.DOWN).ctrl(), (KeyEvent keyEvent) -> this.skin.moveCaret(TextInputControlSkin.TextUnit.PARAGRAPH, TextInputControlSkin.Direction.DOWN, false)), this.keyMapping(new KeyBinding(KeyCode.UP).ctrl().shift(), (KeyEvent keyEvent) -> this.skin.moveCaret(TextInputControlSkin.TextUnit.PARAGRAPH, TextInputControlSkin.Direction.UP, true)), this.keyMapping(new KeyBinding(KeyCode.DOWN).ctrl().shift(), (KeyEvent keyEvent) -> this.skin.moveCaret(TextInputControlSkin.TextUnit.PARAGRAPH, TextInputControlSkin.Direction.DOWN, true)));
        this.addDefaultChildMap(inputMap, inputMap3);
        this.addKeyPadMappings(inputMap);
        textArea.focusedProperty().addListener(new ChangeListener<Boolean>(){

            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean bl, Boolean bl2) {
                TextArea textArea = (TextArea)TextAreaBehavior.this.getNode();
                if (textArea.isFocused()) {
                    if (PlatformUtil.isIOS()) {
                        Bounds bounds = textArea.getBoundsInParent();
                        double d = bounds.getWidth();
                        double d2 = bounds.getHeight();
                        Affine3D affine3D = TextFieldBehavior.calculateNodeToSceneTransform(textArea);
                        String string = textArea.textProperty().getValueSafe();
                        WindowHelper.getPeer(textArea.getScene().getWindow()).requestInput(string, TextFieldBehavior.TextInputTypes.TEXT_AREA.ordinal(), d, d2, affine3D.getMxx(), affine3D.getMxy(), affine3D.getMxz(), affine3D.getMxt(), affine3D.getMyx(), affine3D.getMyy(), affine3D.getMyz(), affine3D.getMyt(), affine3D.getMzx(), affine3D.getMzy(), affine3D.getMzz(), affine3D.getMzt());
                    }
                    if (!TextAreaBehavior.this.focusGainedByMouseClick) {
                        TextAreaBehavior.this.setCaretAnimating(true);
                    }
                } else {
                    if (PlatformUtil.isIOS() && textArea.getScene() != null) {
                        WindowHelper.getPeer(textArea.getScene().getWindow()).releaseInput();
                    }
                    TextAreaBehavior.this.focusGainedByMouseClick = false;
                    TextAreaBehavior.this.setCaretAnimating(false);
                }
            }
        });
        if (com.sun.javafx.scene.control.skin.Utils.isTwoLevelFocus()) {
            this.tlFocus = new TwoLevelFocusBehavior(textArea);
        }
    }

    @Override
    public void dispose() {
        if (this.tlFocus != null) {
            this.tlFocus.dispose();
        }
        super.dispose();
    }

    public void setTextAreaSkin(TextAreaSkin textAreaSkin) {
        this.skin = textAreaSkin;
    }

    private void insertNewLine() {
        this.setEditing(true);
        ((TextArea)this.getNode()).replaceSelection("\n");
        this.setEditing(false);
    }

    private void insertTab() {
        this.setEditing(true);
        ((TextArea)this.getNode()).replaceSelection("\t");
        this.setEditing(false);
    }

    @Override
    protected void deleteChar(boolean bl) {
        if (bl) {
            ((TextArea)this.getNode()).deletePreviousChar();
        } else {
            ((TextArea)this.getNode()).deleteNextChar();
        }
    }

    @Override
    protected void deleteFromLineStart() {
        TextArea textArea = (TextArea)this.getNode();
        int n = textArea.getCaretPosition();
        if (n > 0) {
            this.lineStart(false);
            int n2 = textArea.getCaretPosition();
            if (n > n2) {
                this.replaceText(n2, n, "");
            }
        }
    }

    private void lineStart(boolean bl) {
        this.skin.moveCaret(TextInputControlSkin.TextUnit.LINE, TextInputControlSkin.Direction.BEGINNING, bl);
    }

    private void lineEnd(boolean bl) {
        this.skin.moveCaret(TextInputControlSkin.TextUnit.LINE, TextInputControlSkin.Direction.END, bl);
    }

    @Override
    protected void replaceText(int n, int n2, String string) {
        ((TextArea)this.getNode()).replaceText(n, n2, string);
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        TextArea textArea = (TextArea)this.getNode();
        if (!textArea.isDisabled()) {
            if (!textArea.isFocused()) {
                this.focusGainedByMouseClick = true;
                textArea.requestFocus();
            }
            this.setCaretAnimating(false);
            if (mouseEvent.getButton() == MouseButton.PRIMARY && !mouseEvent.isMiddleButtonDown() && !mouseEvent.isSecondaryButtonDown()) {
                HitInfo hitInfo = this.skin.getIndex(mouseEvent.getX(), mouseEvent.getY());
                int n = hitInfo.getInsertionIndex();
                int n2 = textArea.getAnchor();
                int n3 = textArea.getCaretPosition();
                if (mouseEvent.getClickCount() < 2 && (mouseEvent.isSynthesized() || n2 != n3 && (n > n2 && n < n3 || n < n2 && n > n3))) {
                    this.deferClick = true;
                } else if (!(mouseEvent.isControlDown() || mouseEvent.isAltDown() || mouseEvent.isShiftDown() || mouseEvent.isMetaDown() || mouseEvent.isShortcutDown())) {
                    switch (mouseEvent.getClickCount()) {
                        case 1: {
                            this.skin.positionCaret(hitInfo, false);
                            break;
                        }
                        case 2: {
                            this.mouseDoubleClick(hitInfo);
                            break;
                        }
                        case 3: {
                            this.mouseTripleClick(hitInfo);
                            break;
                        }
                    }
                } else if (!(!mouseEvent.isShiftDown() || mouseEvent.isControlDown() || mouseEvent.isAltDown() || mouseEvent.isMetaDown() || mouseEvent.isShortcutDown() || mouseEvent.getClickCount() != 1)) {
                    this.shiftDown = true;
                    if (PlatformUtil.isMac()) {
                        textArea.extendSelection(n);
                    } else {
                        this.skin.positionCaret(hitInfo, true);
                    }
                }
            }
            if (this.contextMenu.isShowing()) {
                this.contextMenu.hide();
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        TextArea textArea = (TextArea)this.getNode();
        if (!(textArea.isDisabled() || mouseEvent.isSynthesized() || mouseEvent.getButton() != MouseButton.PRIMARY || mouseEvent.isMiddleButtonDown() || mouseEvent.isSecondaryButtonDown() || mouseEvent.isControlDown() || mouseEvent.isAltDown() || mouseEvent.isShiftDown() || mouseEvent.isMetaDown())) {
            this.skin.positionCaret(this.skin.getIndex(mouseEvent.getX(), mouseEvent.getY()), true);
        }
        this.deferClick = false;
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        TextArea textArea = (TextArea)this.getNode();
        if (!textArea.isDisabled()) {
            this.setCaretAnimating(false);
            if (this.deferClick) {
                this.deferClick = false;
                this.skin.positionCaret(this.skin.getIndex(mouseEvent.getX(), mouseEvent.getY()), this.shiftDown);
                this.shiftDown = false;
            }
            this.setCaretAnimating(true);
        }
    }

    @Override
    public void contextMenuRequested(ContextMenuEvent contextMenuEvent) {
        TextArea textArea = (TextArea)this.getNode();
        if (this.contextMenu.isShowing()) {
            this.contextMenu.hide();
        } else if (textArea.getContextMenu() == null && textArea.getOnContextMenuRequested() == null) {
            Object object;
            double d = contextMenuEvent.getScreenX();
            double d2 = contextMenuEvent.getScreenY();
            double d3 = contextMenuEvent.getSceneX();
            if (Properties.IS_TOUCH_SUPPORTED) {
                Point2D point2D;
                if (textArea.getSelection().getLength() == 0) {
                    this.skin.positionCaret(this.skin.getIndex(contextMenuEvent.getX(), contextMenuEvent.getY()), false);
                    point2D = this.skin.getMenuPosition();
                } else {
                    point2D = this.skin.getMenuPosition();
                    if (point2D != null && (point2D.getX() <= 0.0 || point2D.getY() <= 0.0)) {
                        this.skin.positionCaret(this.skin.getIndex(contextMenuEvent.getX(), contextMenuEvent.getY()), false);
                        point2D = this.skin.getMenuPosition();
                    }
                }
                if (point2D != null) {
                    Point2D point2D2 = ((TextArea)this.getNode()).localToScene(point2D);
                    Scene scene = ((TextArea)this.getNode()).getScene();
                    Window window = scene.getWindow();
                    object = new Point2D(window.getX() + scene.getX() + point2D2.getX(), window.getY() + scene.getY() + point2D2.getY());
                    d = ((Point2D)object).getX();
                    d3 = point2D2.getX();
                    d2 = ((Point2D)object).getY();
                }
            }
            this.populateContextMenu();
            double d4 = this.contextMenu.prefWidth(-1.0);
            double d5 = d - (Properties.IS_TOUCH_SUPPORTED ? d4 / 2.0 : 0.0);
            object = Utils.getScreenForPoint(d, 0.0);
            Rectangle2D rectangle2D = ((Screen)object).getBounds();
            if (d5 < rectangle2D.getMinX()) {
                ((TextArea)this.getNode()).getProperties().put("CONTEXT_MENU_SCREEN_X", d);
                ((TextArea)this.getNode()).getProperties().put("CONTEXT_MENU_SCENE_X", d3);
                this.contextMenu.show((Node)this.getNode(), rectangle2D.getMinX(), d2);
            } else if (d + d4 > rectangle2D.getMaxX()) {
                double d6 = d4 - (rectangle2D.getMaxX() - d);
                ((TextArea)this.getNode()).getProperties().put("CONTEXT_MENU_SCREEN_X", d);
                ((TextArea)this.getNode()).getProperties().put("CONTEXT_MENU_SCENE_X", d3);
                this.contextMenu.show((Node)this.getNode(), d - d6, d2);
            } else {
                ((TextArea)this.getNode()).getProperties().put("CONTEXT_MENU_SCREEN_X", 0);
                ((TextArea)this.getNode()).getProperties().put("CONTEXT_MENU_SCENE_X", 0);
                this.contextMenu.show((Node)this.getNode(), d5, d2);
            }
        }
        contextMenuEvent.consume();
    }

    @Override
    protected void setCaretAnimating(boolean bl) {
        this.skin.setCaretAnimating(bl);
    }

    protected void mouseDoubleClick(HitInfo hitInfo) {
        TextArea textArea = (TextArea)this.getNode();
        textArea.previousWord();
        if (PlatformUtil.isWindows()) {
            textArea.selectNextWord();
        } else {
            textArea.selectEndOfNextWord();
        }
    }

    protected void mouseTripleClick(HitInfo hitInfo) {
        this.skin.moveCaret(TextInputControlSkin.TextUnit.PARAGRAPH, TextInputControlSkin.Direction.BEGINNING, false);
        this.skin.moveCaret(TextInputControlSkin.TextUnit.PARAGRAPH, TextInputControlSkin.Direction.END, true);
    }
}

