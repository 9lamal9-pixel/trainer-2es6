/*
 * Decompiled with CFR 0.152.
 */
package javafx.css;

import java.util.List;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.scene.Node;

public interface Styleable {
    public String getTypeSelector();

    public String getId();

    public ObservableList<String> getStyleClass();

    public String getStyle();

    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData();

    public Styleable getStyleableParent();

    public ObservableSet<PseudoClass> getPseudoClassStates();

    default public Node getStyleableNode() {
        return null;
    }
}

