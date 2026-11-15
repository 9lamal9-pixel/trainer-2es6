/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.input;

import com.sun.javafx.tk.Toolkit;
import javafx.beans.NamedArg;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

public final class KeyCharacterCombination
extends KeyCombination {
    private String character = "";

    public final String getCharacter() {
        return this.character;
    }

    public KeyCharacterCombination(@NamedArg(value="character") String string, @NamedArg(value="shift") KeyCombination.ModifierValue modifierValue, @NamedArg(value="control") KeyCombination.ModifierValue modifierValue2, @NamedArg(value="alt") KeyCombination.ModifierValue modifierValue3, @NamedArg(value="meta") KeyCombination.ModifierValue modifierValue4, @NamedArg(value="shortcut") KeyCombination.ModifierValue modifierValue5) {
        super(modifierValue, modifierValue2, modifierValue3, modifierValue4, modifierValue5);
        KeyCharacterCombination.validateKeyCharacter(string);
        this.character = string;
    }

    public KeyCharacterCombination(@NamedArg(value="character") String string, KeyCombination.Modifier ... modifierArray) {
        super(modifierArray);
        KeyCharacterCombination.validateKeyCharacter(string);
        this.character = string;
    }

    @Override
    public boolean match(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.UNDEFINED) {
            return false;
        }
        return keyEvent.getCode().getCode() == Toolkit.getToolkit().getKeyCodeForChar(this.getCharacter()) && super.match(keyEvent);
    }

    @Override
    public String getName() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(super.getName());
        if (stringBuilder.length() > 0) {
            stringBuilder.append("+");
        }
        return stringBuilder.append('\'').append(this.character.replace("'", "\\'")).append('\'').toString();
    }

    @Override
    public String getDisplayText() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(super.getDisplayText());
        stringBuilder.append(this.getCharacter());
        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof KeyCharacterCombination)) {
            return false;
        }
        return this.character.equals(((KeyCharacterCombination)object).getCharacter()) && super.equals(object);
    }

    @Override
    public int hashCode() {
        return 23 * super.hashCode() + this.character.hashCode();
    }

    private static void validateKeyCharacter(String string) {
        if (string == null) {
            throw new NullPointerException("Key character must not be null!");
        }
    }
}

