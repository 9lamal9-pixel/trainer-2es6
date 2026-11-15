/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.DefaultProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableIntegerProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.PaginationSkin;
import javafx.util.Callback;

@DefaultProperty(value="pages")
public class Pagination
extends Control {
    private static final int DEFAULT_MAX_PAGE_INDICATOR_COUNT = 10;
    public static final String STYLE_CLASS_BULLET = "bullet";
    public static final int INDETERMINATE = Integer.MAX_VALUE;
    private int oldMaxPageIndicatorCount = 10;
    private IntegerProperty maxPageIndicatorCount;
    private int oldPageCount = Integer.MAX_VALUE;
    private IntegerProperty pageCount = new SimpleIntegerProperty(this, "pageCount", Integer.MAX_VALUE){

        @Override
        protected void invalidated() {
            if (!Pagination.this.pageCount.isBound()) {
                if (Pagination.this.getPageCount() < 1) {
                    Pagination.this.setPageCount(Pagination.this.oldPageCount);
                }
                Pagination.this.oldPageCount = Pagination.this.getPageCount();
            }
        }
    };
    private final IntegerProperty currentPageIndex = new SimpleIntegerProperty(this, "currentPageIndex", 0){

        @Override
        protected void invalidated() {
            if (!Pagination.this.currentPageIndex.isBound()) {
                if (Pagination.this.getCurrentPageIndex() < 0) {
                    Pagination.this.setCurrentPageIndex(0);
                } else if (Pagination.this.getCurrentPageIndex() > Pagination.this.getPageCount() - 1) {
                    Pagination.this.setCurrentPageIndex(Pagination.this.getPageCount() - 1);
                }
            }
        }

        @Override
        public void bind(ObservableValue<? extends Number> observableValue) {
            throw new UnsupportedOperationException("currentPageIndex supports only bidirectional binding");
        }
    };
    private ObjectProperty<Callback<Integer, Node>> pageFactory = new SimpleObjectProperty<Callback<Integer, Node>>(this, "pageFactory");
    private static final String DEFAULT_STYLE_CLASS = "pagination";

    public Pagination(int n, int n2) {
        this.getStyleClass().setAll((String[])new String[]{DEFAULT_STYLE_CLASS});
        this.setAccessibleRole(AccessibleRole.PAGINATION);
        this.setPageCount(n);
        this.setCurrentPageIndex(n2);
    }

    public Pagination(int n) {
        this(n, 0);
    }

    public Pagination() {
        this(Integer.MAX_VALUE, 0);
    }

    public final void setMaxPageIndicatorCount(int n) {
        this.maxPageIndicatorCountProperty().set(n);
    }

    public final int getMaxPageIndicatorCount() {
        return this.maxPageIndicatorCount == null ? 10 : this.maxPageIndicatorCount.get();
    }

    public final IntegerProperty maxPageIndicatorCountProperty() {
        if (this.maxPageIndicatorCount == null) {
            this.maxPageIndicatorCount = new StyleableIntegerProperty(10){

                @Override
                protected void invalidated() {
                    if (!Pagination.this.maxPageIndicatorCount.isBound()) {
                        if (Pagination.this.getMaxPageIndicatorCount() < 1 || Pagination.this.getMaxPageIndicatorCount() > Pagination.this.getPageCount()) {
                            Pagination.this.setMaxPageIndicatorCount(Pagination.this.oldMaxPageIndicatorCount);
                        }
                        Pagination.this.oldMaxPageIndicatorCount = Pagination.this.getMaxPageIndicatorCount();
                    }
                }

                @Override
                public CssMetaData<Pagination, Number> getCssMetaData() {
                    return StyleableProperties.MAX_PAGE_INDICATOR_COUNT;
                }

                @Override
                public Object getBean() {
                    return Pagination.this;
                }

                @Override
                public String getName() {
                    return "maxPageIndicatorCount";
                }
            };
        }
        return this.maxPageIndicatorCount;
    }

    public final void setPageCount(int n) {
        this.pageCount.set(n);
    }

    public final int getPageCount() {
        return this.pageCount.get();
    }

    public final IntegerProperty pageCountProperty() {
        return this.pageCount;
    }

    public final void setCurrentPageIndex(int n) {
        this.currentPageIndex.set(n);
    }

    public final int getCurrentPageIndex() {
        return this.currentPageIndex.get();
    }

    public final IntegerProperty currentPageIndexProperty() {
        return this.currentPageIndex;
    }

    public final void setPageFactory(Callback<Integer, Node> callback) {
        this.pageFactory.set(callback);
    }

    public final Callback<Integer, Node> getPageFactory() {
        return (Callback)this.pageFactory.get();
    }

    public final ObjectProperty<Callback<Integer, Node>> pageFactoryProperty() {
        return this.pageFactory;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PaginationSkin(this);
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return Pagination.getClassCssMetaData();
    }

    private static class StyleableProperties {
        private static final CssMetaData<Pagination, Number> MAX_PAGE_INDICATOR_COUNT = new CssMetaData<Pagination, Number>("-fx-max-page-indicator-count", SizeConverter.getInstance(), (Number)10){

            @Override
            public boolean isSettable(Pagination pagination) {
                return pagination.maxPageIndicatorCount == null || !pagination.maxPageIndicatorCount.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(Pagination pagination) {
                return (StyleableProperty)((Object)pagination.maxPageIndicatorCountProperty());
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList arrayList = new ArrayList(Control.getClassCssMetaData());
            arrayList.add(MAX_PAGE_INDICATOR_COUNT);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }
}

