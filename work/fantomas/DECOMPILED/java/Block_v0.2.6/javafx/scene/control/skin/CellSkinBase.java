/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.css.CssMetaData;
import javafx.css.StyleOrigin;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.scene.control.Cell;
import javafx.scene.control.SkinBase;
import javafx.scene.control.skin.LabeledSkinBase;

public class CellSkinBase<C extends Cell>
extends LabeledSkinBase<C> {
    private DoubleProperty cellSize;
    static final double DEFAULT_CELL_SIZE = 24.0;

    public CellSkinBase(C c) {
        super(c);
        this.consumeMouseEvents(false);
    }

    public final double getCellSize() {
        return this.cellSize == null ? 24.0 : this.cellSize.get();
    }

    public final ReadOnlyDoubleProperty cellSizeProperty() {
        return this.cellSizePropertyImpl();
    }

    private DoubleProperty cellSizePropertyImpl() {
        if (this.cellSize == null) {
            this.cellSize = new StyleableDoubleProperty(24.0){

                @Override
                public void applyStyle(StyleOrigin styleOrigin, Number number) {
                    double d = number == null ? 24.0 : number.doubleValue();
                    super.applyStyle(styleOrigin, d <= 0.0 ? 24.0 : d);
                }

                @Override
                public void set(double d) {
                    super.set(d);
                    ((Cell)CellSkinBase.this.getSkinnable()).requestLayout();
                }

                @Override
                public Object getBean() {
                    return CellSkinBase.this;
                }

                @Override
                public String getName() {
                    return "cellSize";
                }

                @Override
                public CssMetaData<Cell<?>, Number> getCssMetaData() {
                    return StyleableProperties.CELL_SIZE;
                }
            };
        }
        return this.cellSize;
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return CellSkinBase.getClassCssMetaData();
    }

    private static class StyleableProperties {
        private static final CssMetaData<Cell<?>, Number> CELL_SIZE = new CssMetaData<Cell<?>, Number>("-fx-cell-size", SizeConverter.getInstance(), 24.0){

            @Override
            public boolean isSettable(Cell<?> cell) {
                CellSkinBase cellSkinBase = (CellSkinBase)cell.getSkin();
                return cellSkinBase.cellSize == null || !cellSkinBase.cellSize.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(Cell<?> cell) {
                CellSkinBase cellSkinBase = (CellSkinBase)cell.getSkin();
                return (StyleableProperty)((Object)cellSkinBase.cellSizePropertyImpl());
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList arrayList = new ArrayList(SkinBase.getClassCssMetaData());
            arrayList.add(CELL_SIZE);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }
}

