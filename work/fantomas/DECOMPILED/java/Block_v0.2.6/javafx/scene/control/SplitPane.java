/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.WeakHashMap;
import javafx.beans.DefaultProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.EnumConverter;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.SplitPaneSkin;

@DefaultProperty(value="items")
public class SplitPane
extends Control {
    private static final String RESIZABLE_WITH_PARENT = "resizable-with-parent";
    private ObjectProperty<Orientation> orientation;
    private final ObservableList<Node> items = FXCollections.observableArrayList();
    private final ObservableList<Divider> dividers = FXCollections.observableArrayList();
    private final ObservableList<Divider> unmodifiableDividers = FXCollections.unmodifiableObservableList(this.dividers);
    private final WeakHashMap<Integer, Double> dividerCache = new WeakHashMap();
    private static final String DEFAULT_STYLE_CLASS = "split-pane";
    private static final PseudoClass VERTICAL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("vertical");
    private static final PseudoClass HORIZONTAL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("horizontal");

    public static void setResizableWithParent(Node node, Boolean bl) {
        if (bl == null) {
            node.getProperties().remove(RESIZABLE_WITH_PARENT);
        } else {
            node.getProperties().put(RESIZABLE_WITH_PARENT, bl);
        }
    }

    public static Boolean isResizableWithParent(Node node) {
        Object v;
        if (node.hasProperties() && (v = node.getProperties().get(RESIZABLE_WITH_PARENT)) != null) {
            return (Boolean)v;
        }
        return true;
    }

    public SplitPane() {
        this(null);
    }

    public SplitPane(Node ... nodeArray) {
        this.getStyleClass().setAll((String[])new String[]{DEFAULT_STYLE_CLASS});
        ((StyleableProperty)((Object)this.focusTraversableProperty())).applyStyle(null, Boolean.FALSE);
        this.getItems().addListener(new ListChangeListener<Node>(){

            @Override
            public void onChanged(ListChangeListener.Change<? extends Node> change) {
                int n;
                while (change.next()) {
                    int n2;
                    int n3 = n = change.getFrom();
                    for (n2 = 0; n2 < change.getRemovedSize(); ++n2) {
                        if (n3 < SplitPane.this.dividers.size()) {
                            SplitPane.this.dividerCache.put(n3, Double.MAX_VALUE);
                        } else if (n3 == SplitPane.this.dividers.size() && !SplitPane.this.dividers.isEmpty()) {
                            if (change.wasReplaced()) {
                                SplitPane.this.dividerCache.put(n3 - 1, ((Divider)SplitPane.this.dividers.get(n3 - 1)).getPosition());
                            } else {
                                SplitPane.this.dividerCache.put(n3 - 1, Double.MAX_VALUE);
                            }
                        }
                        ++n3;
                    }
                    for (n2 = 0; n2 < SplitPane.this.dividers.size(); ++n2) {
                        if (SplitPane.this.dividerCache.get(n2) != null) continue;
                        SplitPane.this.dividerCache.put(n2, ((Divider)SplitPane.this.dividers.get(n2)).getPosition());
                    }
                }
                SplitPane.this.dividers.clear();
                for (n = 0; n < SplitPane.this.getItems().size() - 1; ++n) {
                    if (SplitPane.this.dividerCache.containsKey(n) && (Double)SplitPane.this.dividerCache.get(n) != Double.MAX_VALUE) {
                        Divider divider = new Divider();
                        divider.setPosition((Double)SplitPane.this.dividerCache.get(n));
                        SplitPane.this.dividers.add(divider);
                    } else {
                        SplitPane.this.dividers.add(new Divider());
                    }
                    SplitPane.this.dividerCache.remove(n);
                }
            }
        });
        if (nodeArray != null) {
            this.getItems().addAll((Node[])nodeArray);
        }
        this.pseudoClassStateChanged(HORIZONTAL_PSEUDOCLASS_STATE, true);
    }

    public final void setOrientation(Orientation orientation) {
        this.orientationProperty().set(orientation);
    }

    public final Orientation getOrientation() {
        return this.orientation == null ? Orientation.HORIZONTAL : (Orientation)((Object)this.orientation.get());
    }

    public final ObjectProperty<Orientation> orientationProperty() {
        if (this.orientation == null) {
            this.orientation = new StyleableObjectProperty<Orientation>(Orientation.HORIZONTAL){

                @Override
                public void invalidated() {
                    boolean bl = this.get() == Orientation.VERTICAL;
                    SplitPane.this.pseudoClassStateChanged(VERTICAL_PSEUDOCLASS_STATE, bl);
                    SplitPane.this.pseudoClassStateChanged(HORIZONTAL_PSEUDOCLASS_STATE, !bl);
                }

                @Override
                public CssMetaData<SplitPane, Orientation> getCssMetaData() {
                    return StyleableProperties.ORIENTATION;
                }

                @Override
                public Object getBean() {
                    return SplitPane.this;
                }

                @Override
                public String getName() {
                    return "orientation";
                }
            };
        }
        return this.orientation;
    }

    public ObservableList<Node> getItems() {
        return this.items;
    }

    public ObservableList<Divider> getDividers() {
        return this.unmodifiableDividers;
    }

    public void setDividerPosition(int n, double d) {
        if (this.getDividers().size() <= n) {
            this.dividerCache.put(n, d);
            return;
        }
        if (n >= 0) {
            ((Divider)this.getDividers().get(n)).setPosition(d);
        }
    }

    public void setDividerPositions(double ... dArray) {
        if (this.dividers.isEmpty()) {
            for (int i = 0; i < dArray.length; ++i) {
                this.dividerCache.put(i, dArray[i]);
            }
            return;
        }
        for (int i = 0; i < dArray.length && i < this.dividers.size(); ++i) {
            ((Divider)this.dividers.get(i)).setPosition(dArray[i]);
        }
    }

    public double[] getDividerPositions() {
        double[] dArray = new double[this.dividers.size()];
        for (int i = 0; i < this.dividers.size(); ++i) {
            dArray[i] = ((Divider)this.dividers.get(i)).getPosition();
        }
        return dArray;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SplitPaneSkin(this);
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return SplitPane.getClassCssMetaData();
    }

    @Override
    protected Boolean getInitialFocusTraversable() {
        return Boolean.FALSE;
    }

    public static class Divider {
        private DoubleProperty position;

        public final void setPosition(double d) {
            this.positionProperty().set(d);
        }

        public final double getPosition() {
            return this.position == null ? 0.5 : this.position.get();
        }

        public final DoubleProperty positionProperty() {
            if (this.position == null) {
                this.position = new SimpleDoubleProperty(this, "position", 0.5);
            }
            return this.position;
        }
    }

    private static class StyleableProperties {
        private static final CssMetaData<SplitPane, Orientation> ORIENTATION = new CssMetaData<SplitPane, Orientation>("-fx-orientation", new EnumConverter<Orientation>(Orientation.class), Orientation.HORIZONTAL){

            @Override
            public Orientation getInitialValue(SplitPane splitPane) {
                return splitPane.getOrientation();
            }

            @Override
            public boolean isSettable(SplitPane splitPane) {
                return splitPane.orientation == null || !splitPane.orientation.isBound();
            }

            @Override
            public StyleableProperty<Orientation> getStyleableProperty(SplitPane splitPane) {
                return (StyleableProperty)((Object)splitPane.orientationProperty());
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList arrayList = new ArrayList(Control.getClassCssMetaData());
            arrayList.add(ORIENTATION);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }
}

