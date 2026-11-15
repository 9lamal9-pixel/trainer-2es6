/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import com.sun.javafx.collections.TrackableObservableList;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.StyleableProperty;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.TitledPane;
import javafx.scene.control.skin.AccordionSkin;

public class Accordion
extends Control {
    private boolean biasDirty = true;
    private Orientation bias;
    private final ObservableList<TitledPane> panes = new TrackableObservableList<TitledPane>(){

        @Override
        protected void onChanged(ListChangeListener.Change<TitledPane> change) {
            block0: while (change.next()) {
                if (!change.wasRemoved() || Accordion.this.expandedPane.isBound()) continue;
                for (TitledPane titledPane : change.getRemoved()) {
                    if (change.getAddedSubList().contains(titledPane) || Accordion.this.getExpandedPane() != titledPane) continue;
                    Accordion.this.setExpandedPane(null);
                    continue block0;
                }
            }
        }
    };
    private ObjectProperty<TitledPane> expandedPane = new ObjectPropertyBase<TitledPane>(){
        private TitledPane oldValue;

        @Override
        protected void invalidated() {
            TitledPane titledPane = (TitledPane)this.get();
            if (titledPane != null) {
                titledPane.setExpanded(true);
            } else if (this.oldValue != null) {
                this.oldValue.setExpanded(false);
            }
            this.oldValue = titledPane;
        }

        @Override
        public String getName() {
            return "expandedPane";
        }

        @Override
        public Object getBean() {
            return Accordion.this;
        }
    };
    private static final String DEFAULT_STYLE_CLASS = "accordion";

    public Accordion() {
        this(null);
    }

    public Accordion(TitledPane ... titledPaneArray) {
        this.getStyleClass().setAll((String[])new String[]{DEFAULT_STYLE_CLASS});
        if (titledPaneArray != null) {
            this.getPanes().addAll((TitledPane[])titledPaneArray);
        }
        ((StyleableProperty)((Object)this.focusTraversableProperty())).applyStyle(null, Boolean.FALSE);
    }

    public final void setExpandedPane(TitledPane titledPane) {
        this.expandedPaneProperty().set(titledPane);
    }

    public final TitledPane getExpandedPane() {
        return (TitledPane)this.expandedPane.get();
    }

    public final ObjectProperty<TitledPane> expandedPaneProperty() {
        return this.expandedPane;
    }

    public final ObservableList<TitledPane> getPanes() {
        return this.panes;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new AccordionSkin(this);
    }

    @Override
    public void requestLayout() {
        this.biasDirty = true;
        this.bias = null;
        super.requestLayout();
    }

    @Override
    public Orientation getContentBias() {
        if (this.biasDirty) {
            this.bias = null;
            List list = this.getManagedChildren();
            for (Node node : list) {
                Orientation orientation = node.getContentBias();
                if (orientation == null) continue;
                this.bias = orientation;
                if (orientation != Orientation.HORIZONTAL) continue;
                break;
            }
            this.biasDirty = false;
        }
        return this.bias;
    }

    @Override
    protected Boolean getInitialFocusTraversable() {
        return Boolean.FALSE;
    }
}

