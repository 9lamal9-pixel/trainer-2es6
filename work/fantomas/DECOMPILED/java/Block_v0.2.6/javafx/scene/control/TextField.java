/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import com.sun.javafx.binding.ExpressionHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableIntegerProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.EnumConverter;
import javafx.css.converter.SizeConverter;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Skin;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.skin.TextFieldSkin;

public class TextField
extends TextInputControl {
    public static final int DEFAULT_PREF_COLUMN_COUNT = 12;
    private IntegerProperty prefColumnCount = new StyleableIntegerProperty(12){
        private int oldValue;
        {
            this.oldValue = this.get();
        }

        @Override
        protected void invalidated() {
            int n = this.get();
            if (n < 0) {
                if (this.isBound()) {
                    this.unbind();
                }
                this.set(this.oldValue);
                throw new IllegalArgumentException("value cannot be negative.");
            }
            this.oldValue = n;
        }

        @Override
        public CssMetaData<TextField, Number> getCssMetaData() {
            return StyleableProperties.PREF_COLUMN_COUNT;
        }

        @Override
        public Object getBean() {
            return TextField.this;
        }

        @Override
        public String getName() {
            return "prefColumnCount";
        }
    };
    private ObjectProperty<EventHandler<ActionEvent>> onAction = new ObjectPropertyBase<EventHandler<ActionEvent>>(){

        @Override
        protected void invalidated() {
            TextField.this.setEventHandler(ActionEvent.ACTION, (EventHandler)this.get());
        }

        @Override
        public Object getBean() {
            return TextField.this;
        }

        @Override
        public String getName() {
            return "onAction";
        }
    };
    private ObjectProperty<Pos> alignment;

    public TextField() {
        this("");
    }

    public TextField(String string) {
        super(new TextFieldContent());
        this.getStyleClass().add("text-field");
        this.setAccessibleRole(AccessibleRole.TEXT_FIELD);
        this.setText(string);
    }

    public CharSequence getCharacters() {
        return ((TextFieldContent)this.getContent()).characters;
    }

    public final IntegerProperty prefColumnCountProperty() {
        return this.prefColumnCount;
    }

    public final int getPrefColumnCount() {
        return this.prefColumnCount.getValue();
    }

    public final void setPrefColumnCount(int n) {
        this.prefColumnCount.setValue(n);
    }

    public final ObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
        return this.onAction;
    }

    public final EventHandler<ActionEvent> getOnAction() {
        return (EventHandler)this.onActionProperty().get();
    }

    public final void setOnAction(EventHandler<ActionEvent> eventHandler) {
        this.onActionProperty().set(eventHandler);
    }

    public final ObjectProperty<Pos> alignmentProperty() {
        if (this.alignment == null) {
            this.alignment = new StyleableObjectProperty<Pos>(Pos.CENTER_LEFT){

                @Override
                public CssMetaData<TextField, Pos> getCssMetaData() {
                    return StyleableProperties.ALIGNMENT;
                }

                @Override
                public Object getBean() {
                    return TextField.this;
                }

                @Override
                public String getName() {
                    return "alignment";
                }
            };
        }
        return this.alignment;
    }

    public final void setAlignment(Pos pos) {
        this.alignmentProperty().set(pos);
    }

    public final Pos getAlignment() {
        return this.alignment == null ? Pos.CENTER_LEFT : (Pos)((Object)this.alignment.get());
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new TextFieldSkin(this);
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return TextField.getClassCssMetaData();
    }

    private static class StyleableProperties {
        private static final CssMetaData<TextField, Pos> ALIGNMENT = new CssMetaData<TextField, Pos>("-fx-alignment", new EnumConverter<Pos>(Pos.class), Pos.CENTER_LEFT){

            @Override
            public boolean isSettable(TextField textField) {
                return textField.alignment == null || !textField.alignment.isBound();
            }

            @Override
            public StyleableProperty<Pos> getStyleableProperty(TextField textField) {
                return (StyleableProperty)((Object)textField.alignmentProperty());
            }
        };
        private static final CssMetaData<TextField, Number> PREF_COLUMN_COUNT = new CssMetaData<TextField, Number>("-fx-pref-column-count", SizeConverter.getInstance(), (Number)12){

            @Override
            public boolean isSettable(TextField textField) {
                return textField.prefColumnCount == null || !textField.prefColumnCount.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(TextField textField) {
                return (StyleableProperty)((Object)textField.prefColumnCountProperty());
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList arrayList = new ArrayList(TextInputControl.getClassCssMetaData());
            arrayList.add(ALIGNMENT);
            arrayList.add(PREF_COLUMN_COUNT);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }

    private static final class TextFieldContent
    implements TextInputControl.Content {
        private ExpressionHelper<String> helper = null;
        private StringBuilder characters = new StringBuilder();

        private TextFieldContent() {
        }

        @Override
        public String get(int n, int n2) {
            return this.characters.substring(n, n2);
        }

        @Override
        public void insert(int n, String string, boolean bl) {
            if (!(string = TextInputControl.filterInput(string, true, true)).isEmpty()) {
                this.characters.insert(n, string);
                if (bl) {
                    ExpressionHelper.fireValueChangedEvent(this.helper);
                }
            }
        }

        @Override
        public void delete(int n, int n2, boolean bl) {
            if (n2 > n) {
                this.characters.delete(n, n2);
                if (bl) {
                    ExpressionHelper.fireValueChangedEvent(this.helper);
                }
            }
        }

        @Override
        public int length() {
            return this.characters.length();
        }

        @Override
        public String get() {
            return this.characters.toString();
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
        public String getValue() {
            return this.get();
        }

        @Override
        public void addListener(InvalidationListener invalidationListener) {
            this.helper = ExpressionHelper.addListener(this.helper, this, invalidationListener);
        }

        @Override
        public void removeListener(InvalidationListener invalidationListener) {
            this.helper = ExpressionHelper.removeListener(this.helper, invalidationListener);
        }
    }
}

