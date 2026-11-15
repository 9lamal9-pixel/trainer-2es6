/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.input;

import com.sun.javafx.PlatformUtil;
import com.sun.javafx.util.Utils;
import javafx.beans.NamedArg;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

public final class KeyCodeCombination
extends KeyCombination {
    private KeyCode code;

    public final KeyCode getCode() {
        return this.code;
    }

    public KeyCodeCombination(@NamedArg(value="code") KeyCode keyCode, @NamedArg(value="shift") KeyCombination.ModifierValue modifierValue, @NamedArg(value="control") KeyCombination.ModifierValue modifierValue2, @NamedArg(value="alt") KeyCombination.ModifierValue modifierValue3, @NamedArg(value="meta") KeyCombination.ModifierValue modifierValue4, @NamedArg(value="shortcut") KeyCombination.ModifierValue modifierValue5) {
        super(modifierValue, modifierValue2, modifierValue3, modifierValue4, modifierValue5);
        KeyCodeCombination.validateKeyCode(keyCode);
        this.code = keyCode;
    }

    public KeyCodeCombination(@NamedArg(value="code") KeyCode keyCode, KeyCombination.Modifier ... modifierArray) {
        super(modifierArray);
        KeyCodeCombination.validateKeyCode(keyCode);
        this.code = keyCode;
    }

    @Override
    public boolean match(KeyEvent keyEvent) {
        return keyEvent.getCode() == this.getCode() && super.match(keyEvent);
    }

    @Override
    public String getName() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(super.getName());
        if (stringBuilder.length() > 0) {
            stringBuilder.append("+");
        }
        return stringBuilder.append(this.code.getName()).toString();
    }

    @Override
    public String getDisplayText() {
        String[] stringArray;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(super.getDisplayText());
        int n = stringBuilder.length();
        char c = KeyCodeCombination.getSingleChar(this.code);
        if (c != '\u0000') {
            stringBuilder.append(c);
            return stringBuilder.toString();
        }
        String string = this.code.toString();
        for (String string2 : stringArray = Utils.split(string, "_")) {
            if (stringBuilder.length() > n) {
                stringBuilder.append(' ');
            }
            stringBuilder.append(string2.charAt(0));
            stringBuilder.append(string2.substring(1).toLowerCase());
        }
        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof KeyCodeCombination)) {
            return false;
        }
        return this.getCode() == ((KeyCodeCombination)object).getCode() && super.equals(object);
    }

    @Override
    public int hashCode() {
        return 23 * super.hashCode() + this.code.hashCode();
    }

    private static void validateKeyCode(KeyCode keyCode) {
        if (keyCode == null) {
            throw new NullPointerException("Key code must not be null!");
        }
        if (KeyCodeCombination.getModifier(keyCode.getName()) != null) {
            throw new IllegalArgumentException("Key code must not match modifier key!");
        }
        if (keyCode == KeyCode.UNDEFINED) {
            throw new IllegalArgumentException("Key code must differ from undefined value!");
        }
    }

    private static char getSingleChar(KeyCode keyCode) {
        switch (keyCode) {
            case ENTER: {
                return '\u21b5';
            }
            case LEFT: {
                return '\u2190';
            }
            case UP: {
                return '\u2191';
            }
            case RIGHT: {
                return '\u2192';
            }
            case DOWN: {
                return '\u2193';
            }
            case COMMA: {
                return ',';
            }
            case MINUS: {
                return '-';
            }
            case PERIOD: {
                return '.';
            }
            case SLASH: {
                return '/';
            }
            case SEMICOLON: {
                return ';';
            }
            case EQUALS: {
                return '=';
            }
            case OPEN_BRACKET: {
                return '[';
            }
            case BACK_SLASH: {
                return '\\';
            }
            case CLOSE_BRACKET: {
                return ']';
            }
            case MULTIPLY: {
                return '*';
            }
            case ADD: {
                return '+';
            }
            case SUBTRACT: {
                return '-';
            }
            case DECIMAL: {
                return '.';
            }
            case DIVIDE: {
                return '/';
            }
            case BACK_QUOTE: {
                return '`';
            }
            case QUOTE: {
                return '\"';
            }
            case AMPERSAND: {
                return '&';
            }
            case ASTERISK: {
                return '*';
            }
            case LESS: {
                return '<';
            }
            case GREATER: {
                return '>';
            }
            case BRACELEFT: {
                return '{';
            }
            case BRACERIGHT: {
                return '}';
            }
            case AT: {
                return '@';
            }
            case COLON: {
                return ':';
            }
            case CIRCUMFLEX: {
                return '^';
            }
            case DOLLAR: {
                return '$';
            }
            case EURO_SIGN: {
                return '\u20ac';
            }
            case EXCLAMATION_MARK: {
                return '!';
            }
            case LEFT_PARENTHESIS: {
                return '(';
            }
            case NUMBER_SIGN: {
                return '#';
            }
            case PLUS: {
                return '+';
            }
            case RIGHT_PARENTHESIS: {
                return ')';
            }
            case UNDERSCORE: {
                return '_';
            }
            case DIGIT0: {
                return '0';
            }
            case DIGIT1: {
                return '1';
            }
            case DIGIT2: {
                return '2';
            }
            case DIGIT3: {
                return '3';
            }
            case DIGIT4: {
                return '4';
            }
            case DIGIT5: {
                return '5';
            }
            case DIGIT6: {
                return '6';
            }
            case DIGIT7: {
                return '7';
            }
            case DIGIT8: {
                return '8';
            }
            case DIGIT9: {
                return '9';
            }
        }
        if (PlatformUtil.isMac()) {
            switch (keyCode) {
                case BACK_SPACE: {
                    return '\u232b';
                }
                case ESCAPE: {
                    return '\u238b';
                }
                case DELETE: {
                    return '\u2326';
                }
            }
        }
        return '\u0000';
    }
}

