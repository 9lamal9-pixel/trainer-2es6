/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import com.sun.javafx.scene.control.FormatterAccessor;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import javafx.beans.NamedArg;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.scene.control.Control;
import javafx.scene.control.IndexRange;
import javafx.util.StringConverter;

public class TextFormatter<V> {
    private final StringConverter<V> valueConverter;
    private final UnaryOperator<Change> filter;
    private Consumer<TextFormatter<?>> textUpdater;
    public static final StringConverter<String> IDENTITY_STRING_CONVERTER = new StringConverter<String>(){

        @Override
        public String toString(String string) {
            return string == null ? "" : string;
        }

        @Override
        public String fromString(String string) {
            return string;
        }
    };
    private final ObjectProperty<V> value = new ObjectPropertyBase<V>(){

        @Override
        public Object getBean() {
            return TextFormatter.this;
        }

        @Override
        public String getName() {
            return "value";
        }

        @Override
        protected void invalidated() {
            if (TextFormatter.this.valueConverter == null && this.get() != null) {
                if (this.isBound()) {
                    this.unbind();
                }
                throw new IllegalStateException("Value changes are not supported when valueConverter is not set");
            }
            TextFormatter.this.updateText();
        }
    };

    public TextFormatter(@NamedArg(value="filter") UnaryOperator<Change> unaryOperator) {
        this(null, null, unaryOperator);
    }

    public TextFormatter(@NamedArg(value="valueConverter") StringConverter<V> stringConverter, @NamedArg(value="defaultValue") V v, @NamedArg(value="filter") UnaryOperator<Change> unaryOperator) {
        this.filter = unaryOperator;
        this.valueConverter = stringConverter;
        this.setValue(v);
    }

    public TextFormatter(@NamedArg(value="valueConverter") StringConverter<V> stringConverter, @NamedArg(value="defaultValue") V v) {
        this(stringConverter, v, null);
    }

    public TextFormatter(@NamedArg(value="valueConverter") StringConverter<V> stringConverter) {
        this(stringConverter, null, null);
    }

    public final StringConverter<V> getValueConverter() {
        return this.valueConverter;
    }

    public final UnaryOperator<Change> getFilter() {
        return this.filter;
    }

    public final ObjectProperty<V> valueProperty() {
        return this.value;
    }

    public final void setValue(V v) {
        if (this.valueConverter == null && v != null) {
            throw new IllegalStateException("Value changes are not supported when valueConverter is not set");
        }
        this.value.set(v);
    }

    public final V getValue() {
        return (V)this.value.get();
    }

    private void updateText() {
        if (this.textUpdater != null) {
            this.textUpdater.accept(this);
        }
    }

    void bindToControl(Consumer<TextFormatter<?>> consumer) {
        if (this.textUpdater != null) {
            throw new IllegalStateException("Formatter is already used in other control");
        }
        this.textUpdater = consumer;
    }

    void unbindFromControl() {
        this.textUpdater = null;
    }

    void updateValue(String string) {
        if (!this.value.isBound()) {
            try {
                V v = this.valueConverter.fromString(string);
                this.setValue(v);
            }
            catch (Exception exception) {
                this.updateText();
            }
        }
    }

    public static final class Change
    implements Cloneable {
        private final FormatterAccessor accessor;
        private Control control;
        int start;
        int end;
        String text;
        int anchor;
        int caret;

        Change(Control control, FormatterAccessor formatterAccessor, int n, int n2) {
            this(control, formatterAccessor, n2, n2, "", n, n2);
        }

        Change(Control control, FormatterAccessor formatterAccessor, int n, int n2, String string) {
            this(control, formatterAccessor, n, n2, string, n + string.length(), n + string.length());
        }

        Change(Control control, FormatterAccessor formatterAccessor, int n, int n2, String string, int n3, int n4) {
            this.control = control;
            this.accessor = formatterAccessor;
            this.start = n;
            this.end = n2;
            this.text = string;
            this.anchor = n3;
            this.caret = n4;
        }

        public final Control getControl() {
            return this.control;
        }

        public final int getRangeStart() {
            return this.start;
        }

        public final int getRangeEnd() {
            return this.end;
        }

        public final void setRange(int n, int n2) {
            int n3 = this.accessor.getTextLength();
            if (n < 0 || n > n3 || n2 < 0 || n2 > n3) {
                throw new IndexOutOfBoundsException();
            }
            this.start = n;
            this.end = n2;
        }

        public final int getCaretPosition() {
            return this.caret;
        }

        public final int getAnchor() {
            return this.anchor;
        }

        public final int getControlCaretPosition() {
            return this.accessor.getCaret();
        }

        public final int getControlAnchor() {
            return this.accessor.getAnchor();
        }

        public final void selectRange(int n, int n2) {
            if (n < 0 || n > this.accessor.getTextLength() - (this.end - this.start) + this.text.length() || n2 < 0 || n2 > this.accessor.getTextLength() - (this.end - this.start) + this.text.length()) {
                throw new IndexOutOfBoundsException();
            }
            this.anchor = n;
            this.caret = n2;
        }

        public final IndexRange getSelection() {
            return IndexRange.normalize(this.anchor, this.caret);
        }

        public final void setAnchor(int n) {
            if (n < 0 || n > this.accessor.getTextLength() - (this.end - this.start) + this.text.length()) {
                throw new IndexOutOfBoundsException();
            }
            this.anchor = n;
        }

        public final void setCaretPosition(int n) {
            if (n < 0 || n > this.accessor.getTextLength() - (this.end - this.start) + this.text.length()) {
                throw new IndexOutOfBoundsException();
            }
            this.caret = n;
        }

        public final String getText() {
            return this.text;
        }

        public final void setText(String string) {
            if (string == null) {
                throw new NullPointerException();
            }
            this.text = string;
        }

        public final String getControlText() {
            return this.accessor.getText(0, this.accessor.getTextLength());
        }

        public final String getControlNewText() {
            return this.accessor.getText(0, this.start) + this.text + this.accessor.getText(this.end, this.accessor.getTextLength());
        }

        public final boolean isAdded() {
            return !this.text.isEmpty();
        }

        public final boolean isDeleted() {
            return this.start != this.end;
        }

        public final boolean isReplaced() {
            return this.isAdded() && this.isDeleted();
        }

        public final boolean isContentChange() {
            return this.isAdded() || this.isDeleted();
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder("TextInputControl.Change [");
            if (this.isReplaced()) {
                stringBuilder.append(" replaced \"").append(this.accessor.getText(this.start, this.end)).append("\" with \"").append(this.text).append("\" at (").append(this.start).append(", ").append(this.end).append(")");
            } else if (this.isDeleted()) {
                stringBuilder.append(" deleted \"").append(this.accessor.getText(this.start, this.end)).append("\" at (").append(this.start).append(", ").append(this.end).append(")");
            } else if (this.isAdded()) {
                stringBuilder.append(" added \"").append(this.text).append("\" at ").append(this.start);
            }
            if (this.isAdded() || this.isDeleted()) {
                stringBuilder.append("; ");
            } else {
                stringBuilder.append(" ");
            }
            stringBuilder.append("new selection (anchor, caret): [").append(this.anchor).append(", ").append(this.caret).append("]");
            stringBuilder.append(" ]");
            return stringBuilder.toString();
        }

        public Change clone() {
            try {
                return (Change)super.clone();
            }
            catch (CloneNotSupportedException cloneNotSupportedException) {
                throw new RuntimeException(cloneNotSupportedException);
            }
        }
    }
}

