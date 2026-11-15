/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.inputmap;

import com.sun.javafx.tk.Toolkit;
import com.sun.javafx.util.Utils;
import java.util.Objects;
import javafx.event.EventType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class KeyBinding {
    private final KeyCode code;
    private final EventType<KeyEvent> eventType;
    private OptionalBoolean shift = OptionalBoolean.FALSE;
    private OptionalBoolean ctrl = OptionalBoolean.FALSE;
    private OptionalBoolean alt = OptionalBoolean.FALSE;
    private OptionalBoolean meta = OptionalBoolean.FALSE;

    public KeyBinding(KeyCode keyCode) {
        this(keyCode, null);
    }

    public KeyBinding(EventType<KeyEvent> eventType) {
        this(null, eventType);
    }

    public KeyBinding(KeyCode keyCode, EventType<KeyEvent> eventType) {
        this.code = keyCode;
        this.eventType = eventType != null ? eventType : KeyEvent.KEY_PRESSED;
    }

    public final KeyBinding shift() {
        return this.shift(OptionalBoolean.TRUE);
    }

    public final KeyBinding shift(OptionalBoolean optionalBoolean) {
        this.shift = optionalBoolean;
        return this;
    }

    public final KeyBinding ctrl() {
        return this.ctrl(OptionalBoolean.TRUE);
    }

    public final KeyBinding ctrl(OptionalBoolean optionalBoolean) {
        this.ctrl = optionalBoolean;
        return this;
    }

    public final KeyBinding alt() {
        return this.alt(OptionalBoolean.TRUE);
    }

    public final KeyBinding alt(OptionalBoolean optionalBoolean) {
        this.alt = optionalBoolean;
        return this;
    }

    public final KeyBinding meta() {
        return this.meta(OptionalBoolean.TRUE);
    }

    public final KeyBinding meta(OptionalBoolean optionalBoolean) {
        this.meta = optionalBoolean;
        return this;
    }

    public final KeyBinding shortcut() {
        if (Toolkit.getToolkit().getClass().getName().endsWith("StubToolkit")) {
            if (Utils.isMac()) {
                return this.meta();
            }
            return this.ctrl();
        }
        switch (Toolkit.getToolkit().getPlatformShortcutKey()) {
            case SHIFT: {
                return this.shift();
            }
            case CONTROL: {
                return this.ctrl();
            }
            case ALT: {
                return this.alt();
            }
            case META: {
                return this.meta();
            }
        }
        return this;
    }

    public final KeyCode getCode() {
        return this.code;
    }

    public final EventType<KeyEvent> getType() {
        return this.eventType;
    }

    public final OptionalBoolean getShift() {
        return this.shift;
    }

    public final OptionalBoolean getCtrl() {
        return this.ctrl;
    }

    public final OptionalBoolean getAlt() {
        return this.alt;
    }

    public final OptionalBoolean getMeta() {
        return this.meta;
    }

    public int getSpecificity(KeyEvent keyEvent) {
        int n = 0;
        if (this.code != null && this.code != keyEvent.getCode()) {
            return 0;
        }
        n = 1;
        if (!this.shift.equals(keyEvent.isShiftDown())) {
            return 0;
        }
        if (this.shift != OptionalBoolean.ANY) {
            ++n;
        }
        if (!this.ctrl.equals(keyEvent.isControlDown())) {
            return 0;
        }
        if (this.ctrl != OptionalBoolean.ANY) {
            ++n;
        }
        if (!this.alt.equals(keyEvent.isAltDown())) {
            return 0;
        }
        if (this.alt != OptionalBoolean.ANY) {
            ++n;
        }
        if (!this.meta.equals(keyEvent.isMetaDown())) {
            return 0;
        }
        if (this.meta != OptionalBoolean.ANY) {
            ++n;
        }
        if (this.eventType != null && this.eventType != keyEvent.getEventType()) {
            return 0;
        }
        return ++n;
    }

    public String toString() {
        return "KeyBinding [code=" + this.code + ", shift=" + this.shift + ", ctrl=" + this.ctrl + ", alt=" + this.alt + ", meta=" + this.meta + ", type=" + this.eventType + "]";
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof KeyBinding)) {
            return false;
        }
        KeyBinding keyBinding = (KeyBinding)object;
        return Objects.equals((Object)this.getCode(), (Object)keyBinding.getCode()) && Objects.equals(this.eventType, keyBinding.eventType) && Objects.equals((Object)this.getShift(), (Object)keyBinding.getShift()) && Objects.equals((Object)this.getCtrl(), (Object)keyBinding.getCtrl()) && Objects.equals((Object)this.getAlt(), (Object)keyBinding.getAlt()) && Objects.equals((Object)this.getMeta(), (Object)keyBinding.getMeta());
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.getCode(), this.eventType, this.getShift(), this.getCtrl(), this.getAlt(), this.getMeta()});
    }

    public static KeyBinding toKeyBinding(KeyEvent keyEvent) {
        KeyBinding keyBinding = new KeyBinding(keyEvent.getCode(), keyEvent.getEventType());
        if (keyEvent.isShiftDown()) {
            keyBinding.shift();
        }
        if (keyEvent.isControlDown()) {
            keyBinding.ctrl();
        }
        if (keyEvent.isAltDown()) {
            keyBinding.alt();
        }
        if (keyEvent.isShortcutDown()) {
            keyBinding.shortcut();
        }
        return keyBinding;
    }

    public static enum OptionalBoolean {
        TRUE,
        FALSE,
        ANY;


        public boolean equals(boolean bl) {
            if (this == ANY) {
                return true;
            }
            if (bl && this == TRUE) {
                return true;
            }
            return !bl && this == FALSE;
        }
    }
}

