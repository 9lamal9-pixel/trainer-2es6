/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import com.sun.javafx.binding.ExpressionHelper;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.control.FormatterAccessor;
import com.sun.javafx.util.Utils;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.DefaultProperty;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.css.CssMetaData;
import javafx.css.FontCssMetaData;
import javafx.css.PseudoClass;
import javafx.css.StyleOrigin;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.control.Control;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.text.Font;
import javafx.util.StringConverter;

@DefaultProperty(value="text")
public abstract class TextInputControl
extends Control {
    private ObjectProperty<Font> font;
    private StringProperty promptText = new SimpleStringProperty(this, "promptText", ""){

        @Override
        protected void invalidated() {
            String string = this.get();
            if (string != null && string.contains("\n")) {
                string = string.replace("\n", "");
                this.set(string);
            }
        }
    };
    private final ObjectProperty<TextFormatter<?>> textFormatter = new ObjectPropertyBase<TextFormatter<?>>(){
        private TextFormatter<?> oldFormatter = null;

        @Override
        public Object getBean() {
            return TextInputControl.this;
        }

        @Override
        public String getName() {
            return "textFormatter";
        }

        @Override
        protected void invalidated() {
            TextFormatter textFormatter2 = (TextFormatter)this.get();
            try {
                if (textFormatter2 != null) {
                    try {
                        textFormatter2.bindToControl(textFormatter -> TextInputControl.this.updateText(textFormatter));
                    }
                    catch (IllegalStateException illegalStateException) {
                        if (this.isBound()) {
                            this.unbind();
                        }
                        this.set(null);
                        throw illegalStateException;
                    }
                    if (!TextInputControl.this.isFocused()) {
                        TextInputControl.this.updateText((TextFormatter)this.get());
                    }
                }
                if (this.oldFormatter != null) {
                    this.oldFormatter.unbindFromControl();
                }
            }
            finally {
                this.oldFormatter = textFormatter2;
            }
        }
    };
    private final Content content;
    private TextProperty text = new TextProperty();
    private ReadOnlyIntegerWrapper length = new ReadOnlyIntegerWrapper(this, "length");
    private BooleanProperty editable = new SimpleBooleanProperty(this, "editable", true){

        @Override
        protected void invalidated() {
            TextInputControl.this.pseudoClassStateChanged(PSEUDO_CLASS_READONLY, !this.get());
        }
    };
    private ReadOnlyObjectWrapper<IndexRange> selection = new ReadOnlyObjectWrapper<IndexRange>(this, "selection", new IndexRange(0, 0));
    private ReadOnlyStringWrapper selectedText = new ReadOnlyStringWrapper(this, "selectedText");
    private ReadOnlyIntegerWrapper anchor = new ReadOnlyIntegerWrapper(this, "anchor", 0);
    private ReadOnlyIntegerWrapper caretPosition = new ReadOnlyIntegerWrapper(this, "caretPosition", 0);
    private UndoRedoChange undoChangeHead;
    private UndoRedoChange undoChange = this.undoChangeHead = new UndoRedoChange();
    private boolean createNewUndoRecord = false;
    private final ReadOnlyBooleanWrapper undoable = new ReadOnlyBooleanWrapper(this, "undoable", false);
    private final ReadOnlyBooleanWrapper redoable = new ReadOnlyBooleanWrapper(this, "redoable", false);
    private BreakIterator charIterator;
    private BreakIterator wordIterator;
    private FormatterAccessor accessor;
    private static final PseudoClass PSEUDO_CLASS_READONLY = PseudoClass.getPseudoClass("readonly");

    protected TextInputControl(Content content) {
        this.content = content;
        content.addListener(observable -> {
            if (content.length() > 0) {
                this.text.textIsNull = false;
            }
            this.text.controlContentHasChanged();
        });
        this.length.bind(new IntegerBinding(){
            {
                this.bind(TextInputControl.this.text);
            }

            @Override
            protected int computeValue() {
                String string = TextInputControl.this.text.get();
                return string == null ? 0 : string.length();
            }
        });
        this.selectedText.bind(new StringBinding(){
            {
                this.bind(TextInputControl.this.selection, TextInputControl.this.text);
            }

            @Override
            protected String computeValue() {
                int n;
                String string = TextInputControl.this.text.get();
                IndexRange indexRange = (IndexRange)TextInputControl.this.selection.get();
                if (string == null || indexRange == null) {
                    return "";
                }
                int n2 = indexRange.getStart();
                int n3 = indexRange.getEnd();
                if (n3 > n2 + (n = string.length())) {
                    n3 = n;
                }
                if (n2 > n - 1) {
                    n3 = 0;
                    n2 = 0;
                }
                return string.substring(n2, n3);
            }
        });
        this.focusedProperty().addListener((observableValue, bl, bl2) -> {
            if (bl2.booleanValue()) {
                if (this.getTextFormatter() != null) {
                    this.updateText(this.getTextFormatter());
                }
            } else {
                this.commitValue();
            }
        });
        this.getStyleClass().add("text-input");
    }

    public final ObjectProperty<Font> fontProperty() {
        if (this.font == null) {
            this.font = new StyleableObjectProperty<Font>(Font.getDefault()){
                private boolean fontSetByCss;
                {
                    this.fontSetByCss = false;
                }

                @Override
                public void applyStyle(StyleOrigin styleOrigin, Font font) {
                    try {
                        this.fontSetByCss = true;
                        super.applyStyle(styleOrigin, font);
                    }
                    catch (Exception exception) {
                        throw exception;
                    }
                    finally {
                        this.fontSetByCss = false;
                    }
                }

                @Override
                public void set(Font font) {
                    Font font2 = (Font)this.get();
                    if (font == null ? font2 == null : font.equals(font2)) {
                        return;
                    }
                    super.set(font);
                }

                @Override
                protected void invalidated() {
                    if (!this.fontSetByCss) {
                        NodeHelper.reapplyCSS(TextInputControl.this);
                    }
                }

                @Override
                public CssMetaData<TextInputControl, Font> getCssMetaData() {
                    return StyleableProperties.FONT;
                }

                @Override
                public Object getBean() {
                    return TextInputControl.this;
                }

                @Override
                public String getName() {
                    return "font";
                }
            };
        }
        return this.font;
    }

    public final void setFont(Font font) {
        this.fontProperty().setValue(font);
    }

    public final Font getFont() {
        return this.font == null ? Font.getDefault() : (Font)this.font.getValue();
    }

    public final StringProperty promptTextProperty() {
        return this.promptText;
    }

    public final String getPromptText() {
        return (String)this.promptText.get();
    }

    public final void setPromptText(String string) {
        this.promptText.set(string);
    }

    public final ObjectProperty<TextFormatter<?>> textFormatterProperty() {
        return this.textFormatter;
    }

    public final TextFormatter<?> getTextFormatter() {
        return (TextFormatter)this.textFormatter.get();
    }

    public final void setTextFormatter(TextFormatter<?> textFormatter) {
        this.textFormatter.set(textFormatter);
    }

    protected final Content getContent() {
        return this.content;
    }

    public final String getText() {
        return this.text.get();
    }

    public final void setText(String string) {
        this.text.set(string);
    }

    public final StringProperty textProperty() {
        return this.text;
    }

    public final int getLength() {
        return this.length.get();
    }

    public final ReadOnlyIntegerProperty lengthProperty() {
        return this.length.getReadOnlyProperty();
    }

    public final boolean isEditable() {
        return this.editable.getValue();
    }

    public final void setEditable(boolean bl) {
        this.editable.setValue(bl);
    }

    public final BooleanProperty editableProperty() {
        return this.editable;
    }

    public final IndexRange getSelection() {
        return (IndexRange)this.selection.getValue();
    }

    public final ReadOnlyObjectProperty<IndexRange> selectionProperty() {
        return this.selection.getReadOnlyProperty();
    }

    public final String getSelectedText() {
        return this.selectedText.get();
    }

    public final ReadOnlyStringProperty selectedTextProperty() {
        return this.selectedText.getReadOnlyProperty();
    }

    public final int getAnchor() {
        return this.anchor.get();
    }

    public final ReadOnlyIntegerProperty anchorProperty() {
        return this.anchor.getReadOnlyProperty();
    }

    public final int getCaretPosition() {
        return this.caretPosition.get();
    }

    public final ReadOnlyIntegerProperty caretPositionProperty() {
        return this.caretPosition.getReadOnlyProperty();
    }

    public final boolean isUndoable() {
        return this.undoable.get();
    }

    public final ReadOnlyBooleanProperty undoableProperty() {
        return this.undoable.getReadOnlyProperty();
    }

    public final boolean isRedoable() {
        return this.redoable.get();
    }

    public final ReadOnlyBooleanProperty redoableProperty() {
        return this.redoable.getReadOnlyProperty();
    }

    public String getText(int n, int n2) {
        if (n > n2) {
            throw new IllegalArgumentException("The start must be <= the end");
        }
        if (n < 0 || n2 > this.getLength()) {
            throw new IndexOutOfBoundsException();
        }
        return this.getContent().get(n, n2);
    }

    public void appendText(String string) {
        this.insertText(this.getLength(), string);
    }

    public void insertText(int n, String string) {
        this.replaceText(n, n, string);
    }

    public void deleteText(IndexRange indexRange) {
        this.replaceText(indexRange, "");
    }

    public void deleteText(int n, int n2) {
        this.replaceText(n, n2, "");
    }

    public void replaceText(IndexRange indexRange, String string) {
        int n = indexRange.getStart();
        int n2 = n + indexRange.getLength();
        this.replaceText(n, n2, string);
    }

    public void replaceText(int n, int n2, String string) {
        if (n > n2) {
            throw new IllegalArgumentException();
        }
        if (string == null) {
            throw new NullPointerException();
        }
        if (n < 0 || n2 > this.getLength()) {
            throw new IndexOutOfBoundsException();
        }
        if (!this.text.isBound()) {
            int n3 = this.getLength();
            TextFormatter<?> textFormatter = this.getTextFormatter();
            TextFormatter.Change change = new TextFormatter.Change(this, this.getFormatterAccessor(), n, n2, string);
            if (textFormatter != null && textFormatter.getFilter() != null && (change = (TextFormatter.Change)textFormatter.getFilter().apply(change)) == null) {
                return;
            }
            this.updateContent(change, n3 == 0);
        }
    }

    private void updateContent(TextFormatter.Change change, boolean bl) {
        boolean bl2 = this.getSelection().getLength() > 0;
        String string = this.getText(change.start, change.end);
        int n = this.replaceText(change.start, change.end, change.text, change.getAnchor(), change.getCaretPosition());
        String string2 = this.getText(change.start, change.start + change.text.length() - n);
        if (string2.equals(string)) {
            return;
        }
        int n2 = this.undoChange == this.undoChangeHead ? -1 : this.undoChange.start + this.undoChange.newText.length();
        boolean bl3 = false;
        if (string2.equals(" ")) {
            if (!UndoRedoChange.isSpaceCharSequence()) {
                bl3 = true;
                UndoRedoChange.setSpaceCharSequence(true);
            }
        } else {
            UndoRedoChange.setSpaceCharSequence(false);
        }
        if (this.createNewUndoRecord || bl2 || n2 == -1 || bl || bl3 || UndoRedoChange.hasChangeDurationElapsed() || n2 != change.start && n2 != change.end || change.end - change.start > 0) {
            this.undoChange = this.undoChange.add(change.start, string, string2);
        } else if (change.start != change.end && change.text.isEmpty()) {
            if (this.undoChange.newText.length() > 0) {
                this.undoChange.newText = this.undoChange.newText.substring(0, change.start - this.undoChange.start);
                if (this.undoChange.newText.isEmpty()) {
                    this.undoChange = this.undoChange.discard();
                }
            } else if (change.start == n2) {
                this.undoChange.oldText = this.undoChange.oldText + string;
            } else {
                this.undoChange.oldText = string + this.undoChange.oldText;
                --this.undoChange.start;
            }
        } else {
            this.undoChange.newText = this.undoChange.newText + string2;
        }
        this.updateUndoRedoState();
    }

    public void cut() {
        this.copy();
        IndexRange indexRange = this.getSelection();
        this.deleteText(indexRange.getStart(), indexRange.getEnd());
    }

    public void copy() {
        String string = this.getSelectedText();
        if (string.length() > 0) {
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(string);
            Clipboard.getSystemClipboard().setContent(clipboardContent);
        }
    }

    public void paste() {
        String string;
        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.hasString() && (string = clipboard.getString()) != null) {
            this.createNewUndoRecord = true;
            try {
                this.replaceSelection(string);
            }
            finally {
                this.createNewUndoRecord = false;
            }
        }
    }

    public void selectBackward() {
        if (this.getCaretPosition() > 0 && this.getLength() > 0) {
            if (this.charIterator == null) {
                this.charIterator = BreakIterator.getCharacterInstance();
            }
            this.charIterator.setText(this.getText());
            this.selectRange(this.getAnchor(), this.charIterator.preceding(this.getCaretPosition()));
        }
    }

    public void selectForward() {
        int n = this.getLength();
        if (n > 0 && this.getCaretPosition() < n) {
            if (this.charIterator == null) {
                this.charIterator = BreakIterator.getCharacterInstance();
            }
            this.charIterator.setText(this.getText());
            this.selectRange(this.getAnchor(), this.charIterator.following(this.getCaretPosition()));
        }
    }

    public void previousWord() {
        this.previousWord(false);
    }

    public void nextWord() {
        this.nextWord(false);
    }

    public void endOfNextWord() {
        this.endOfNextWord(false);
    }

    public void selectPreviousWord() {
        this.previousWord(true);
    }

    public void selectNextWord() {
        this.nextWord(true);
    }

    public void selectEndOfNextWord() {
        this.endOfNextWord(true);
    }

    private void previousWord(boolean bl) {
        int n = this.getLength();
        String string = this.getText();
        if (n <= 0) {
            return;
        }
        if (this.wordIterator == null) {
            this.wordIterator = BreakIterator.getWordInstance();
        }
        this.wordIterator.setText(string);
        int n2 = this.wordIterator.preceding(Utils.clamp(0, this.getCaretPosition(), n));
        while (n2 != -1 && !Character.isLetterOrDigit(string.charAt(Utils.clamp(0, n2, n - 1)))) {
            n2 = this.wordIterator.preceding(Utils.clamp(0, n2, n));
        }
        this.selectRange(bl ? this.getAnchor() : n2, n2);
    }

    private void nextWord(boolean bl) {
        int n = this.getLength();
        String string = this.getText();
        if (n <= 0) {
            return;
        }
        if (this.wordIterator == null) {
            this.wordIterator = BreakIterator.getWordInstance();
        }
        this.wordIterator.setText(string);
        int n2 = this.wordIterator.following(Utils.clamp(0, this.getCaretPosition(), n - 1));
        int n3 = this.wordIterator.next();
        while (n3 != -1) {
            for (int i = n2; i <= n3; ++i) {
                char c = string.charAt(Utils.clamp(0, i, n - 1));
                if (c == ' ' || c == '\t') continue;
                if (bl) {
                    this.selectRange(this.getAnchor(), i);
                } else {
                    this.selectRange(i, i);
                }
                return;
            }
            n2 = n3;
            n3 = this.wordIterator.next();
        }
        if (bl) {
            this.selectRange(this.getAnchor(), n);
        } else {
            this.end();
        }
    }

    private void endOfNextWord(boolean bl) {
        int n = this.getLength();
        String string = this.getText();
        if (n <= 0) {
            return;
        }
        if (this.wordIterator == null) {
            this.wordIterator = BreakIterator.getWordInstance();
        }
        this.wordIterator.setText(string);
        int n2 = this.wordIterator.following(Utils.clamp(0, this.getCaretPosition(), n));
        int n3 = this.wordIterator.next();
        while (n3 != -1) {
            for (int i = n2; i <= n3; ++i) {
                if (Character.isLetterOrDigit(string.charAt(Utils.clamp(0, i, n - 1)))) continue;
                if (bl) {
                    this.selectRange(this.getAnchor(), i);
                } else {
                    this.selectRange(i, i);
                }
                return;
            }
            n2 = n3;
            n3 = this.wordIterator.next();
        }
        if (bl) {
            this.selectRange(this.getAnchor(), n);
        } else {
            this.end();
        }
    }

    public void selectAll() {
        this.selectRange(0, this.getLength());
    }

    public void home() {
        this.selectRange(0, 0);
    }

    public void end() {
        int n = this.getLength();
        if (n > 0) {
            this.selectRange(n, n);
        }
    }

    public void selectHome() {
        this.selectRange(this.getAnchor(), 0);
    }

    public void selectEnd() {
        int n = this.getLength();
        if (n > 0) {
            this.selectRange(this.getAnchor(), n);
        }
    }

    public boolean deletePreviousChar() {
        boolean bl = true;
        if (this.isEditable() && !this.isDisabled()) {
            int n;
            String string = this.getText();
            int n2 = this.getCaretPosition();
            if (n2 != (n = this.getAnchor())) {
                this.replaceSelection("");
                bl = false;
            } else if (n2 > 0) {
                int n3 = Character.offsetByCodePoints(string, n2, -1);
                this.deleteText(n3, n2);
                bl = false;
            }
        }
        return !bl;
    }

    public boolean deleteNextChar() {
        boolean bl = true;
        if (this.isEditable() && !this.isDisabled()) {
            int n;
            int n2 = this.getLength();
            String string = this.getText();
            int n3 = this.getCaretPosition();
            if (n3 != (n = this.getAnchor())) {
                this.replaceSelection("");
                bl = false;
            } else if (n2 > 0 && n3 < n2) {
                if (this.charIterator == null) {
                    this.charIterator = BreakIterator.getCharacterInstance();
                }
                this.charIterator.setText(string);
                int n4 = this.charIterator.following(n3);
                this.deleteText(n3, n4);
                bl = false;
            }
        }
        return !bl;
    }

    public void forward() {
        int n;
        int n2 = this.getLength();
        int n3 = this.getCaretPosition();
        if (n3 != (n = this.getAnchor())) {
            int n4 = Math.max(n3, n);
            this.selectRange(n4, n4);
        } else if (n3 < n2 && n2 > 0) {
            if (this.charIterator == null) {
                this.charIterator = BreakIterator.getCharacterInstance();
            }
            this.charIterator.setText(this.getText());
            int n5 = this.charIterator.following(n3);
            this.selectRange(n5, n5);
        }
        this.deselect();
    }

    public void backward() {
        int n;
        int n2 = this.getLength();
        int n3 = this.getCaretPosition();
        if (n3 != (n = this.getAnchor())) {
            int n4 = Math.min(n3, n);
            this.selectRange(n4, n4);
        } else if (n3 > 0 && n2 > 0) {
            if (this.charIterator == null) {
                this.charIterator = BreakIterator.getCharacterInstance();
            }
            this.charIterator.setText(this.getText());
            int n5 = this.charIterator.preceding(n3);
            this.selectRange(n5, n5);
        }
        this.deselect();
    }

    public void positionCaret(int n) {
        int n2 = Utils.clamp(0, n, this.getLength());
        this.selectRange(n2, n2);
    }

    public void selectPositionCaret(int n) {
        this.selectRange(this.getAnchor(), Utils.clamp(0, n, this.getLength()));
    }

    public void selectRange(int n, int n2) {
        n2 = Utils.clamp(0, n2, this.getLength());
        n = Utils.clamp(0, n, this.getLength());
        TextFormatter.Change change = new TextFormatter.Change(this, this.getFormatterAccessor(), n, n2);
        TextFormatter<?> textFormatter = this.getTextFormatter();
        if (textFormatter != null && textFormatter.getFilter() != null && (change = (TextFormatter.Change)textFormatter.getFilter().apply(change)) == null) {
            return;
        }
        this.updateContent(change, false);
    }

    private void doSelectRange(int n, int n2) {
        this.caretPosition.set(Utils.clamp(0, n2, this.getLength()));
        this.anchor.set(Utils.clamp(0, n, this.getLength()));
        this.selection.set(IndexRange.normalize(this.getAnchor(), this.getCaretPosition()));
        this.notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTION_START);
    }

    public void extendSelection(int n) {
        int n2 = Utils.clamp(0, n, this.getLength());
        int n3 = this.getCaretPosition();
        int n4 = this.getAnchor();
        int n5 = Math.min(n3, n4);
        int n6 = Math.max(n3, n4);
        if (n2 < n5) {
            this.selectRange(n6, n2);
        } else {
            this.selectRange(n5, n2);
        }
    }

    public void clear() {
        this.deselect();
        if (!this.text.isBound()) {
            this.setText("");
        }
    }

    public void deselect() {
        this.selectRange(this.getCaretPosition(), this.getCaretPosition());
    }

    public void replaceSelection(String string) {
        this.replaceText(this.getSelection(), string);
    }

    public final void undo() {
        if (this.isUndoable()) {
            int n = this.undoChange.start;
            String string = this.undoChange.newText;
            String string2 = this.undoChange.oldText;
            if (string != null) {
                this.getContent().delete(n, n + string.length(), string2.isEmpty());
            }
            if (string2 != null) {
                this.getContent().insert(n, string2, true);
                this.doSelectRange(n, n + string2.length());
            } else {
                this.doSelectRange(n, n + string.length());
            }
            this.undoChange = this.undoChange.prev;
        }
        this.updateUndoRedoState();
    }

    public final void redo() {
        if (this.isRedoable()) {
            this.undoChange = this.undoChange.next;
            int n = this.undoChange.start;
            String string = this.undoChange.newText;
            String string2 = this.undoChange.oldText;
            if (string2 != null) {
                this.getContent().delete(n, n + string2.length(), string.isEmpty());
            }
            if (string != null) {
                this.getContent().insert(n, string, true);
                this.doSelectRange(n + string.length(), n + string.length());
            } else {
                this.doSelectRange(n, n);
            }
        }
        this.updateUndoRedoState();
    }

    void textUpdated() {
    }

    private void resetUndoRedoState() {
        this.undoChange = this.undoChangeHead;
        this.undoChange.next = null;
        this.updateUndoRedoState();
    }

    private void updateUndoRedoState() {
        this.undoable.set(this.undoChange != this.undoChangeHead);
        this.redoable.set(this.undoChange.next != null);
    }

    private boolean filterAndSet(String string) {
        TextFormatter<?> textFormatter = this.getTextFormatter();
        int n = this.content.length();
        if (textFormatter != null && textFormatter.getFilter() != null && !this.text.isBound()) {
            TextFormatter.Change change = new TextFormatter.Change(this, this.getFormatterAccessor(), 0, n, string, 0, 0);
            change = (TextFormatter.Change)textFormatter.getFilter().apply(change);
            if (change == null) {
                return false;
            }
            this.replaceText(change.start, change.end, change.text, change.getAnchor(), change.getCaretPosition());
        } else {
            this.replaceText(0, n, string, 0, 0);
        }
        return true;
    }

    private int replaceText(int n, int n2, String string, int n3, int n4) {
        int n5 = this.getLength();
        int n6 = 0;
        if (n2 != n) {
            this.getContent().delete(n, n2, string.isEmpty());
            n5 -= n2 - n;
        }
        if (string != null) {
            this.getContent().insert(n, string, true);
            n6 = string.length() - (this.getLength() - n5);
            n3 -= n6;
            n4 -= n6;
        }
        this.doSelectRange(n3, n4);
        return n6;
    }

    private <T> void updateText(TextFormatter<T> textFormatter) {
        T t = textFormatter.getValue();
        StringConverter<T> stringConverter = textFormatter.getValueConverter();
        if (stringConverter != null) {
            String string = stringConverter.toString(t);
            if (string == null) {
                string = "";
            }
            this.replaceText(0, this.getLength(), string, string.length(), string.length());
        }
    }

    public final void commitValue() {
        if (this.getTextFormatter() != null) {
            this.getTextFormatter().updateValue(this.getText());
        }
    }

    public final void cancelEdit() {
        if (this.getTextFormatter() != null) {
            this.updateText(this.getTextFormatter());
        }
    }

    private FormatterAccessor getFormatterAccessor() {
        if (this.accessor == null) {
            this.accessor = new TextInputControlFromatterAccessor();
        }
        return this.accessor;
    }

    static String filterInput(String string, boolean bl, boolean bl2) {
        if (TextInputControl.containsInvalidCharacters(string, bl, bl2)) {
            StringBuilder stringBuilder = new StringBuilder(string.length());
            for (int i = 0; i < string.length(); ++i) {
                char c = string.charAt(i);
                if (TextInputControl.isInvalidCharacter(c, bl, bl2)) continue;
                stringBuilder.append(c);
            }
            string = stringBuilder.toString();
        }
        return string;
    }

    static boolean containsInvalidCharacters(String string, boolean bl, boolean bl2) {
        for (int i = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            if (!TextInputControl.isInvalidCharacter(c, bl, bl2)) continue;
            return true;
        }
        return false;
    }

    private static boolean isInvalidCharacter(char c, boolean bl, boolean bl2) {
        if (c == '\u007f') {
            return true;
        }
        if (c == '\n') {
            return bl;
        }
        if (c == '\t') {
            return bl2;
        }
        return c < ' ';
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return TextInputControl.getClassCssMetaData();
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case TEXT: {
                String string = this.getAccessibleText();
                if (string != null && !string.isEmpty()) {
                    return string;
                }
                String string2 = this.getText();
                if (string2 == null || string2.isEmpty()) {
                    string2 = this.getPromptText();
                }
                return string2;
            }
            case EDITABLE: {
                return this.isEditable();
            }
            case SELECTION_START: {
                return this.getSelection().getStart();
            }
            case SELECTION_END: {
                return this.getSelection().getEnd();
            }
            case CARET_OFFSET: {
                return this.getCaretPosition();
            }
            case FONT: {
                return this.getFont();
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }

    @Override
    public void executeAccessibleAction(AccessibleAction accessibleAction, Object ... objectArray) {
        switch (accessibleAction) {
            case SET_TEXT: {
                String string = (String)objectArray[0];
                if (string == null) break;
                this.setText(string);
                break;
            }
            case SET_TEXT_SELECTION: {
                Integer n = (Integer)objectArray[0];
                Integer n2 = (Integer)objectArray[1];
                if (n == null || n2 == null) break;
                this.selectRange(n, n2);
                break;
            }
            default: {
                super.executeAccessibleAction(accessibleAction, objectArray);
            }
        }
    }

    private class TextInputControlFromatterAccessor
    implements FormatterAccessor {
        private TextInputControlFromatterAccessor() {
        }

        @Override
        public int getTextLength() {
            return TextInputControl.this.getLength();
        }

        @Override
        public String getText(int n, int n2) {
            return TextInputControl.this.getText(n, n2);
        }

        @Override
        public int getCaret() {
            return TextInputControl.this.getCaretPosition();
        }

        @Override
        public int getAnchor() {
            return TextInputControl.this.getAnchor();
        }
    }

    private static class StyleableProperties {
        private static final FontCssMetaData<TextInputControl> FONT = new FontCssMetaData<TextInputControl>("-fx-font", Font.getDefault()){

            @Override
            public boolean isSettable(TextInputControl textInputControl) {
                return textInputControl.font == null || !textInputControl.font.isBound();
            }

            @Override
            public StyleableProperty<Font> getStyleableProperty(TextInputControl textInputControl) {
                return (StyleableProperty)((Object)textInputControl.fontProperty());
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList arrayList = new ArrayList(Control.getClassCssMetaData());
            arrayList.add(FONT);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }

    static class UndoRedoChange {
        static long prevRecordTime;
        static final long CHANGE_DURATION = 2500L;
        static boolean spaceCharSequence;
        int start;
        String oldText;
        String newText;
        UndoRedoChange prev;
        UndoRedoChange next;

        UndoRedoChange() {
        }

        public UndoRedoChange add(int n, String string, String string2) {
            UndoRedoChange undoRedoChange = new UndoRedoChange();
            undoRedoChange.start = n;
            undoRedoChange.oldText = string;
            undoRedoChange.newText = string2;
            undoRedoChange.prev = this;
            this.next = undoRedoChange;
            prevRecordTime = System.currentTimeMillis();
            return undoRedoChange;
        }

        static boolean hasChangeDurationElapsed() {
            return System.currentTimeMillis() - prevRecordTime > 2500L;
        }

        static void setSpaceCharSequence(boolean bl) {
            spaceCharSequence = bl;
        }

        static boolean isSpaceCharSequence() {
            return spaceCharSequence;
        }

        public UndoRedoChange discard() {
            this.prev.next = this.next;
            return this.prev;
        }

        void debugPrint() {
            UndoRedoChange undoRedoChange = this;
            System.out.print("[");
            while (undoRedoChange != null) {
                System.out.print(undoRedoChange.toString());
                if (undoRedoChange.next != null) {
                    System.out.print(", ");
                }
                undoRedoChange = undoRedoChange.next;
            }
            System.out.println("]");
        }

        public String toString() {
            if (this.oldText == null && this.newText == null) {
                return "head";
            }
            if (this.oldText.isEmpty() && !this.newText.isEmpty()) {
                return "added '" + this.newText + "' at index " + this.start;
            }
            if (!this.oldText.isEmpty() && !this.newText.isEmpty()) {
                return "replaced '" + this.oldText + "' with '" + this.newText + "' at index " + this.start;
            }
            return "deleted '" + this.oldText + "' at index " + this.start;
        }

        static {
            spaceCharSequence = false;
        }
    }

    private class TextProperty
    extends StringProperty {
        private ObservableValue<? extends String> observable = null;
        private InvalidationListener listener = null;
        private ExpressionHelper<String> helper = null;
        private boolean textIsNull = false;

        private TextProperty() {
        }

        @Override
        public String get() {
            return this.textIsNull ? null : (String)TextInputControl.this.content.get();
        }

        @Override
        public void set(String string) {
            if (this.isBound()) {
                throw new RuntimeException("A bound value cannot be set.");
            }
            this.doSet(string);
            this.markInvalid();
        }

        private void controlContentHasChanged() {
            this.markInvalid();
            TextInputControl.this.notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
        }

        @Override
        public void bind(ObservableValue<? extends String> observableValue) {
            if (observableValue == null) {
                throw new NullPointerException("Cannot bind to null");
            }
            if (!observableValue.equals(this.observable)) {
                this.unbind();
                this.observable = observableValue;
                if (this.listener == null) {
                    this.listener = new Listener();
                }
                this.observable.addListener(this.listener);
                this.markInvalid();
                this.doSet(observableValue.getValue());
            }
        }

        @Override
        public void unbind() {
            if (this.observable != null) {
                this.doSet(this.observable.getValue());
                this.observable.removeListener(this.listener);
                this.observable = null;
            }
        }

        @Override
        public boolean isBound() {
            return this.observable != null;
        }

        @Override
        public void addListener(InvalidationListener invalidationListener) {
            this.helper = ExpressionHelper.addListener(this.helper, this, invalidationListener);
        }

        @Override
        public void removeListener(InvalidationListener invalidationListener) {
            this.helper = ExpressionHelper.removeListener(this.helper, invalidationListener);
        }

        @Override
        public void addListener(ChangeListener<? super String> changeListener) {
            this.helper = ExpressionHelper.addListener(this.helper, this, changeListener);
        }

        @Override
        public void removeListener(ChangeListener<? super String> changeListener) {
            this.helper = ExpressionHelper.removeListener(this.helper, changeListener);
        }

        @Override
        public Object getBean() {
            return TextInputControl.this;
        }

        @Override
        public String getName() {
            return "text";
        }

        private void fireValueChangedEvent() {
            ExpressionHelper.fireValueChangedEvent(this.helper);
        }

        private void markInvalid() {
            this.fireValueChangedEvent();
        }

        private void doSet(String string) {
            boolean bl = this.textIsNull = string == null;
            if (string == null) {
                string = "";
            }
            if (!TextInputControl.this.filterAndSet(string)) {
                return;
            }
            if (TextInputControl.this.getTextFormatter() != null) {
                TextInputControl.this.getTextFormatter().updateValue(TextInputControl.this.getText());
            }
            TextInputControl.this.textUpdated();
            TextInputControl.this.resetUndoRedoState();
        }

        private class Listener
        implements InvalidationListener {
            private Listener() {
            }

            @Override
            public void invalidated(Observable observable) {
                TextProperty.this.doSet((String)TextProperty.this.observable.getValue());
            }
        }
    }

    protected static interface Content
    extends ObservableStringValue {
        public String get(int var1, int var2);

        public void insert(int var1, String var2, boolean var3);

        public void delete(int var1, int var2, boolean var3);

        public int length();
    }
}

