/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.layout;

import com.sun.javafx.collections.TrackableObservableList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.BooleanConverter;
import javafx.css.converter.EnumConverter;
import javafx.css.converter.SizeConverter;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.util.Callback;

public class GridPane
extends Pane {
    public static final int REMAINING = Integer.MAX_VALUE;
    private static final String MARGIN_CONSTRAINT = "gridpane-margin";
    private static final String HALIGNMENT_CONSTRAINT = "gridpane-halignment";
    private static final String VALIGNMENT_CONSTRAINT = "gridpane-valignment";
    private static final String HGROW_CONSTRAINT = "gridpane-hgrow";
    private static final String VGROW_CONSTRAINT = "gridpane-vgrow";
    private static final String ROW_INDEX_CONSTRAINT = "gridpane-row";
    private static final String COLUMN_INDEX_CONSTRAINT = "gridpane-column";
    private static final String ROW_SPAN_CONSTRAINT = "gridpane-row-span";
    private static final String COLUMN_SPAN_CONSTRAINT = "gridpane-column-span";
    private static final String FILL_WIDTH_CONSTRAINT = "gridpane-fill-width";
    private static final String FILL_HEIGHT_CONSTRAINT = "gridpane-fill-height";
    private static final Callback<Node, Insets> marginAccessor = node -> GridPane.getMargin(node);
    private static final Color GRID_LINE_COLOR = Color.rgb(30, 30, 30);
    private static final double GRID_LINE_DASH = 3.0;
    private DoubleProperty hgap;
    private DoubleProperty vgap;
    private ObjectProperty<Pos> alignment;
    private BooleanProperty gridLinesVisible;
    private final ObservableList<RowConstraints> rowConstraints = new TrackableObservableList<RowConstraints>(){

        @Override
        protected void onChanged(ListChangeListener.Change<RowConstraints> change) {
            while (change.next()) {
                for (RowConstraints rowConstraints : change.getRemoved()) {
                    if (rowConstraints == null || GridPane.this.rowConstraints.contains(rowConstraints)) continue;
                    rowConstraints.remove(GridPane.this);
                }
                for (RowConstraints rowConstraints : change.getAddedSubList()) {
                    if (rowConstraints == null) continue;
                    rowConstraints.add(GridPane.this);
                }
            }
            GridPane.this.requestLayout();
        }
    };
    private final ObservableList<ColumnConstraints> columnConstraints = new TrackableObservableList<ColumnConstraints>(){

        @Override
        protected void onChanged(ListChangeListener.Change<ColumnConstraints> change) {
            while (change.next()) {
                for (ColumnConstraints columnConstraints : change.getRemoved()) {
                    if (columnConstraints == null || GridPane.this.columnConstraints.contains(columnConstraints)) continue;
                    columnConstraints.remove(GridPane.this);
                }
                for (ColumnConstraints columnConstraints : change.getAddedSubList()) {
                    if (columnConstraints == null) continue;
                    columnConstraints.add(GridPane.this);
                }
            }
            GridPane.this.requestLayout();
        }
    };
    private Group gridLines;
    private Orientation bias;
    private double[] rowPercentHeight;
    private double rowPercentTotal = 0.0;
    private CompositeSize rowMinHeight;
    private CompositeSize rowPrefHeight;
    private CompositeSize rowMaxHeight;
    private List<Node>[] rowBaseline;
    private double[] rowMinBaselineComplement;
    private double[] rowPrefBaselineComplement;
    private double[] rowMaxBaselineComplement;
    private Priority[] rowGrow;
    private double[] columnPercentWidth;
    private double columnPercentTotal = 0.0;
    private CompositeSize columnMinWidth;
    private CompositeSize columnPrefWidth;
    private CompositeSize columnMaxWidth;
    private Priority[] columnGrow;
    private boolean metricsDirty = true;
    private boolean performingLayout = false;
    private int numRows;
    private int numColumns;
    private CompositeSize currentHeights;
    private CompositeSize currentWidths;

    public static void setRowIndex(Node node, Integer n) {
        if (n != null && n < 0) {
            throw new IllegalArgumentException("rowIndex must be greater or equal to 0, but was " + n);
        }
        GridPane.setConstraint(node, ROW_INDEX_CONSTRAINT, n);
    }

    public static Integer getRowIndex(Node node) {
        return (Integer)GridPane.getConstraint(node, ROW_INDEX_CONSTRAINT);
    }

    public static void setColumnIndex(Node node, Integer n) {
        if (n != null && n < 0) {
            throw new IllegalArgumentException("columnIndex must be greater or equal to 0, but was " + n);
        }
        GridPane.setConstraint(node, COLUMN_INDEX_CONSTRAINT, n);
    }

    public static Integer getColumnIndex(Node node) {
        return (Integer)GridPane.getConstraint(node, COLUMN_INDEX_CONSTRAINT);
    }

    public static void setRowSpan(Node node, Integer n) {
        if (n != null && n < 1) {
            throw new IllegalArgumentException("rowSpan must be greater or equal to 1, but was " + n);
        }
        GridPane.setConstraint(node, ROW_SPAN_CONSTRAINT, n);
    }

    public static Integer getRowSpan(Node node) {
        return (Integer)GridPane.getConstraint(node, ROW_SPAN_CONSTRAINT);
    }

    public static void setColumnSpan(Node node, Integer n) {
        if (n != null && n < 1) {
            throw new IllegalArgumentException("columnSpan must be greater or equal to 1, but was " + n);
        }
        GridPane.setConstraint(node, COLUMN_SPAN_CONSTRAINT, n);
    }

    public static Integer getColumnSpan(Node node) {
        return (Integer)GridPane.getConstraint(node, COLUMN_SPAN_CONSTRAINT);
    }

    public static void setMargin(Node node, Insets insets) {
        GridPane.setConstraint(node, MARGIN_CONSTRAINT, insets);
    }

    public static Insets getMargin(Node node) {
        return (Insets)GridPane.getConstraint(node, MARGIN_CONSTRAINT);
    }

    private double getBaselineComplementForChild(Node node) {
        if (this.isNodePositionedByBaseline(node)) {
            return this.rowMinBaselineComplement[GridPane.getNodeRowIndex(node)];
        }
        return -1.0;
    }

    public static void setHalignment(Node node, HPos hPos) {
        GridPane.setConstraint(node, HALIGNMENT_CONSTRAINT, (Object)hPos);
    }

    public static HPos getHalignment(Node node) {
        return (HPos)((Object)GridPane.getConstraint(node, HALIGNMENT_CONSTRAINT));
    }

    public static void setValignment(Node node, VPos vPos) {
        GridPane.setConstraint(node, VALIGNMENT_CONSTRAINT, (Object)vPos);
    }

    public static VPos getValignment(Node node) {
        return (VPos)((Object)GridPane.getConstraint(node, VALIGNMENT_CONSTRAINT));
    }

    public static void setHgrow(Node node, Priority priority) {
        GridPane.setConstraint(node, HGROW_CONSTRAINT, (Object)priority);
    }

    public static Priority getHgrow(Node node) {
        return (Priority)((Object)GridPane.getConstraint(node, HGROW_CONSTRAINT));
    }

    public static void setVgrow(Node node, Priority priority) {
        GridPane.setConstraint(node, VGROW_CONSTRAINT, (Object)priority);
    }

    public static Priority getVgrow(Node node) {
        return (Priority)((Object)GridPane.getConstraint(node, VGROW_CONSTRAINT));
    }

    public static void setFillWidth(Node node, Boolean bl) {
        GridPane.setConstraint(node, FILL_WIDTH_CONSTRAINT, bl);
    }

    public static Boolean isFillWidth(Node node) {
        return (Boolean)GridPane.getConstraint(node, FILL_WIDTH_CONSTRAINT);
    }

    public static void setFillHeight(Node node, Boolean bl) {
        GridPane.setConstraint(node, FILL_HEIGHT_CONSTRAINT, bl);
    }

    public static Boolean isFillHeight(Node node) {
        return (Boolean)GridPane.getConstraint(node, FILL_HEIGHT_CONSTRAINT);
    }

    public static void setConstraints(Node node, int n, int n2) {
        GridPane.setRowIndex(node, n2);
        GridPane.setColumnIndex(node, n);
    }

    public static void setConstraints(Node node, int n, int n2, int n3, int n4) {
        GridPane.setRowIndex(node, n2);
        GridPane.setColumnIndex(node, n);
        GridPane.setRowSpan(node, n4);
        GridPane.setColumnSpan(node, n3);
    }

    public static void setConstraints(Node node, int n, int n2, int n3, int n4, HPos hPos, VPos vPos) {
        GridPane.setRowIndex(node, n2);
        GridPane.setColumnIndex(node, n);
        GridPane.setRowSpan(node, n4);
        GridPane.setColumnSpan(node, n3);
        GridPane.setHalignment(node, hPos);
        GridPane.setValignment(node, vPos);
    }

    public static void setConstraints(Node node, int n, int n2, int n3, int n4, HPos hPos, VPos vPos, Priority priority, Priority priority2) {
        GridPane.setRowIndex(node, n2);
        GridPane.setColumnIndex(node, n);
        GridPane.setRowSpan(node, n4);
        GridPane.setColumnSpan(node, n3);
        GridPane.setHalignment(node, hPos);
        GridPane.setValignment(node, vPos);
        GridPane.setHgrow(node, priority);
        GridPane.setVgrow(node, priority2);
    }

    public static void setConstraints(Node node, int n, int n2, int n3, int n4, HPos hPos, VPos vPos, Priority priority, Priority priority2, Insets insets) {
        GridPane.setRowIndex(node, n2);
        GridPane.setColumnIndex(node, n);
        GridPane.setRowSpan(node, n4);
        GridPane.setColumnSpan(node, n3);
        GridPane.setHalignment(node, hPos);
        GridPane.setValignment(node, vPos);
        GridPane.setHgrow(node, priority);
        GridPane.setVgrow(node, priority2);
        GridPane.setMargin(node, insets);
    }

    public static void clearConstraints(Node node) {
        GridPane.setRowIndex(node, null);
        GridPane.setColumnIndex(node, null);
        GridPane.setRowSpan(node, null);
        GridPane.setColumnSpan(node, null);
        GridPane.setHalignment(node, null);
        GridPane.setValignment(node, null);
        GridPane.setHgrow(node, null);
        GridPane.setVgrow(node, null);
        GridPane.setMargin(node, null);
    }

    static void createRow(int n, int n2, Node ... nodeArray) {
        for (int i = 0; i < nodeArray.length; ++i) {
            GridPane.setConstraints(nodeArray[i], n2 + i, n);
        }
    }

    static void createColumn(int n, int n2, Node ... nodeArray) {
        for (int i = 0; i < nodeArray.length; ++i) {
            GridPane.setConstraints(nodeArray[i], n, n2 + i);
        }
    }

    static int getNodeRowIndex(Node node) {
        Integer n = GridPane.getRowIndex(node);
        return n != null ? n : 0;
    }

    private static int getNodeRowSpan(Node node) {
        Integer n = GridPane.getRowSpan(node);
        return n != null ? n : 1;
    }

    static int getNodeRowEnd(Node node) {
        int n = GridPane.getNodeRowSpan(node);
        return n != Integer.MAX_VALUE ? GridPane.getNodeRowIndex(node) + n - 1 : Integer.MAX_VALUE;
    }

    static int getNodeColumnIndex(Node node) {
        Integer n = GridPane.getColumnIndex(node);
        return n != null ? n : 0;
    }

    private static int getNodeColumnSpan(Node node) {
        Integer n = GridPane.getColumnSpan(node);
        return n != null ? n : 1;
    }

    static int getNodeColumnEnd(Node node) {
        int n = GridPane.getNodeColumnSpan(node);
        return n != Integer.MAX_VALUE ? GridPane.getNodeColumnIndex(node) + n - 1 : Integer.MAX_VALUE;
    }

    private static Priority getNodeHgrow(Node node) {
        Priority priority = GridPane.getHgrow(node);
        return priority != null ? priority : Priority.NEVER;
    }

    private static Priority getNodeVgrow(Node node) {
        Priority priority = GridPane.getVgrow(node);
        return priority != null ? priority : Priority.NEVER;
    }

    private static Priority[] createPriorityArray(int n, Priority priority) {
        Priority[] priorityArray = new Priority[n];
        Arrays.fill((Object[])priorityArray, (Object)priority);
        return priorityArray;
    }

    public GridPane() {
        this.getChildren().addListener(observable -> this.requestLayout());
    }

    public final DoubleProperty hgapProperty() {
        if (this.hgap == null) {
            this.hgap = new StyleableDoubleProperty(0.0){

                @Override
                public void invalidated() {
                    GridPane.this.requestLayout();
                }

                @Override
                public CssMetaData<GridPane, Number> getCssMetaData() {
                    return StyleableProperties.HGAP;
                }

                @Override
                public Object getBean() {
                    return GridPane.this;
                }

                @Override
                public String getName() {
                    return "hgap";
                }
            };
        }
        return this.hgap;
    }

    public final void setHgap(double d) {
        this.hgapProperty().set(d);
    }

    public final double getHgap() {
        return this.hgap == null ? 0.0 : this.hgap.get();
    }

    public final DoubleProperty vgapProperty() {
        if (this.vgap == null) {
            this.vgap = new StyleableDoubleProperty(0.0){

                @Override
                public void invalidated() {
                    GridPane.this.requestLayout();
                }

                @Override
                public CssMetaData<GridPane, Number> getCssMetaData() {
                    return StyleableProperties.VGAP;
                }

                @Override
                public Object getBean() {
                    return GridPane.this;
                }

                @Override
                public String getName() {
                    return "vgap";
                }
            };
        }
        return this.vgap;
    }

    public final void setVgap(double d) {
        this.vgapProperty().set(d);
    }

    public final double getVgap() {
        return this.vgap == null ? 0.0 : this.vgap.get();
    }

    public final ObjectProperty<Pos> alignmentProperty() {
        if (this.alignment == null) {
            this.alignment = new StyleableObjectProperty<Pos>(Pos.TOP_LEFT){

                @Override
                public void invalidated() {
                    GridPane.this.requestLayout();
                }

                @Override
                public CssMetaData<GridPane, Pos> getCssMetaData() {
                    return StyleableProperties.ALIGNMENT;
                }

                @Override
                public Object getBean() {
                    return GridPane.this;
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
        return this.alignment == null ? Pos.TOP_LEFT : (Pos)((Object)this.alignment.get());
    }

    private Pos getAlignmentInternal() {
        Pos pos = this.getAlignment();
        return pos == null ? Pos.TOP_LEFT : pos;
    }

    public final BooleanProperty gridLinesVisibleProperty() {
        if (this.gridLinesVisible == null) {
            this.gridLinesVisible = new StyleableBooleanProperty(){

                @Override
                protected void invalidated() {
                    if (this.get()) {
                        GridPane.this.gridLines = new Group();
                        GridPane.this.gridLines.setManaged(false);
                        GridPane.this.getChildren().add(GridPane.this.gridLines);
                    } else {
                        GridPane.this.getChildren().remove(GridPane.this.gridLines);
                        GridPane.this.gridLines = null;
                    }
                    GridPane.this.requestLayout();
                }

                @Override
                public CssMetaData<GridPane, Boolean> getCssMetaData() {
                    return StyleableProperties.GRID_LINES_VISIBLE;
                }

                @Override
                public Object getBean() {
                    return GridPane.this;
                }

                @Override
                public String getName() {
                    return "gridLinesVisible";
                }
            };
        }
        return this.gridLinesVisible;
    }

    public final void setGridLinesVisible(boolean bl) {
        this.gridLinesVisibleProperty().set(bl);
    }

    public final boolean isGridLinesVisible() {
        return this.gridLinesVisible == null ? false : this.gridLinesVisible.get();
    }

    public final ObservableList<RowConstraints> getRowConstraints() {
        return this.rowConstraints;
    }

    public final ObservableList<ColumnConstraints> getColumnConstraints() {
        return this.columnConstraints;
    }

    public void add(Node node, int n, int n2) {
        GridPane.setConstraints(node, n, n2);
        this.getChildren().add(node);
    }

    public void add(Node node, int n, int n2, int n3, int n4) {
        GridPane.setConstraints(node, n, n2, n3, n4);
        this.getChildren().add(node);
    }

    public void addRow(int n, Node ... nodeArray) {
        int n2 = 0;
        List list = this.getManagedChildren();
        int n3 = list.size();
        for (int i = 0; i < n3; ++i) {
            Node node = (Node)list.get(i);
            int n4 = GridPane.getNodeRowIndex(node);
            int n5 = GridPane.getNodeRowEnd(node);
            if (n < n4 || n > n5 && n5 != Integer.MAX_VALUE) continue;
            int n6 = GridPane.getNodeColumnIndex(node);
            int n7 = GridPane.getNodeColumnEnd(node);
            n2 = Math.max(n2, (n7 != Integer.MAX_VALUE ? n7 : n6) + 1);
        }
        GridPane.createRow(n, n2, nodeArray);
        this.getChildren().addAll((Node[])nodeArray);
    }

    public void addColumn(int n, Node ... nodeArray) {
        int n2 = 0;
        List list = this.getManagedChildren();
        int n3 = list.size();
        for (int i = 0; i < n3; ++i) {
            Node node = (Node)list.get(i);
            int n4 = GridPane.getNodeColumnIndex(node);
            int n5 = GridPane.getNodeColumnEnd(node);
            if (n < n4 || n > n5 && n5 != Integer.MAX_VALUE) continue;
            int n6 = GridPane.getNodeRowIndex(node);
            int n7 = GridPane.getNodeRowEnd(node);
            n2 = Math.max(n2, (n7 != Integer.MAX_VALUE ? n7 : n6) + 1);
        }
        GridPane.createColumn(n, n2, nodeArray);
        this.getChildren().addAll((Node[])nodeArray);
    }

    private int getNumberOfRows() {
        this.computeGridMetrics();
        return this.numRows;
    }

    private int getNumberOfColumns() {
        this.computeGridMetrics();
        return this.numColumns;
    }

    private boolean isNodePositionedByBaseline(Node node) {
        return this.getRowValignment(GridPane.getNodeRowIndex(node)) == VPos.BASELINE && GridPane.getValignment(node) == null || GridPane.getValignment(node) == VPos.BASELINE;
    }

    private void computeGridMetrics() {
        if (this.metricsDirty) {
            int n;
            int n2;
            Object object;
            int n3;
            this.numRows = this.rowConstraints.size();
            this.numColumns = this.columnConstraints.size();
            List list = this.getManagedChildren();
            int n4 = list.size();
            for (n3 = 0; n3 < n4; ++n3) {
                object = (Node)list.get(n3);
                n2 = GridPane.getNodeRowIndex((Node)object);
                n = GridPane.getNodeColumnIndex((Node)object);
                int n5 = GridPane.getNodeRowEnd((Node)object);
                int n6 = GridPane.getNodeColumnEnd((Node)object);
                this.numRows = Math.max(this.numRows, (n5 != Integer.MAX_VALUE ? n5 : n2) + 1);
                this.numColumns = Math.max(this.numColumns, (n6 != Integer.MAX_VALUE ? n6 : n) + 1);
            }
            this.rowPercentHeight = GridPane.createDoubleArray(this.numRows, -1.0);
            this.rowPercentTotal = 0.0;
            this.columnPercentWidth = GridPane.createDoubleArray(this.numColumns, -1.0);
            this.columnPercentTotal = 0.0;
            this.columnGrow = GridPane.createPriorityArray(this.numColumns, Priority.NEVER);
            this.rowGrow = GridPane.createPriorityArray(this.numRows, Priority.NEVER);
            this.rowMinBaselineComplement = GridPane.createDoubleArray(this.numRows, -1.0);
            this.rowPrefBaselineComplement = GridPane.createDoubleArray(this.numRows, -1.0);
            this.rowMaxBaselineComplement = GridPane.createDoubleArray(this.numRows, -1.0);
            this.rowBaseline = new List[this.numRows];
            n4 = this.numRows;
            for (n3 = 0; n3 < n4; ++n3) {
                Object object2;
                if (n3 < this.rowConstraints.size()) {
                    object = (RowConstraints)this.rowConstraints.get(n3);
                    double d = ((RowConstraints)object).getPercentHeight();
                    object2 = ((RowConstraints)object).getVgrow();
                    if (d >= 0.0) {
                        this.rowPercentHeight[n3] = d;
                    }
                    if (object2 != null) {
                        this.rowGrow[n3] = object2;
                    }
                }
                object = new ArrayList(this.numColumns);
                n = list.size();
                for (n2 = 0; n2 < n; ++n2) {
                    object2 = (Node)list.get(n2);
                    if (GridPane.getNodeRowIndex((Node)object2) != n3 || !this.isNodePositionedByBaseline((Node)object2)) continue;
                    object.add(object2);
                }
                this.rowMinBaselineComplement[n3] = GridPane.getMinBaselineComplement((List<Node>)object);
                this.rowPrefBaselineComplement[n3] = GridPane.getPrefBaselineComplement((List<Node>)object);
                this.rowMaxBaselineComplement[n3] = GridPane.getMaxBaselineComplement((List<Node>)object);
                this.rowBaseline[n3] = object;
            }
            n4 = Math.min(this.numColumns, this.columnConstraints.size());
            for (n3 = 0; n3 < n4; ++n3) {
                object = (ColumnConstraints)this.columnConstraints.get(n3);
                double d = ((ColumnConstraints)object).getPercentWidth();
                Priority priority = ((ColumnConstraints)object).getHgrow();
                if (d >= 0.0) {
                    this.columnPercentWidth[n3] = d;
                }
                if (priority == null) continue;
                this.columnGrow[n3] = priority;
            }
            n4 = list.size();
            for (n3 = 0; n3 < n4; ++n3) {
                Priority priority;
                object = (Node)list.get(n3);
                if (GridPane.getNodeColumnSpan((Node)object) == 1) {
                    priority = GridPane.getNodeHgrow((Node)object);
                    n = GridPane.getNodeColumnIndex((Node)object);
                    this.columnGrow[n] = Priority.max(this.columnGrow[n], priority);
                }
                if (GridPane.getNodeRowSpan((Node)object) != 1) continue;
                priority = GridPane.getNodeVgrow((Node)object);
                n = GridPane.getNodeRowIndex((Node)object);
                this.rowGrow[n] = Priority.max(this.rowGrow[n], priority);
            }
            for (n3 = 0; n3 < this.rowPercentHeight.length; ++n3) {
                if (!(this.rowPercentHeight[n3] > 0.0)) continue;
                this.rowPercentTotal += this.rowPercentHeight[n3];
            }
            if (this.rowPercentTotal > 100.0) {
                double d = 100.0 / this.rowPercentTotal;
                for (int i = 0; i < this.rowPercentHeight.length; ++i) {
                    if (!(this.rowPercentHeight[i] > 0.0)) continue;
                    int n7 = i;
                    this.rowPercentHeight[n7] = this.rowPercentHeight[n7] * d;
                }
                this.rowPercentTotal = 100.0;
            }
            for (int i = 0; i < this.columnPercentWidth.length; ++i) {
                if (!(this.columnPercentWidth[i] > 0.0)) continue;
                this.columnPercentTotal += this.columnPercentWidth[i];
            }
            if (this.columnPercentTotal > 100.0) {
                double d = 100.0 / this.columnPercentTotal;
                for (int i = 0; i < this.columnPercentWidth.length; ++i) {
                    if (!(this.columnPercentWidth[i] > 0.0)) continue;
                    int n8 = i;
                    this.columnPercentWidth[n8] = this.columnPercentWidth[n8] * d;
                }
                this.columnPercentTotal = 100.0;
            }
            this.bias = null;
            for (int i = 0; i < list.size(); ++i) {
                Orientation orientation = ((Node)list.get(i)).getContentBias();
                if (orientation == null) continue;
                this.bias = orientation;
                if (orientation == Orientation.HORIZONTAL) break;
            }
            this.metricsDirty = false;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected double computeMinWidth(double d) {
        this.computeGridMetrics();
        this.performingLayout = true;
        try {
            double[] dArray = d == -1.0 ? null : this.computeHeightsToFit(d).asArray();
            double d2 = this.snapSpaceX(this.getInsets().getLeft()) + this.computeMinWidths(dArray).computeTotalWithMultiSize() + this.snapSpaceX(this.getInsets().getRight());
            return d2;
        }
        finally {
            this.performingLayout = false;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected double computeMinHeight(double d) {
        this.computeGridMetrics();
        this.performingLayout = true;
        try {
            double[] dArray = d == -1.0 ? null : this.computeWidthsToFit(d).asArray();
            double d2 = this.snapSpaceY(this.getInsets().getTop()) + this.computeMinHeights(dArray).computeTotalWithMultiSize() + this.snapSpaceY(this.getInsets().getBottom());
            return d2;
        }
        finally {
            this.performingLayout = false;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected double computePrefWidth(double d) {
        this.computeGridMetrics();
        this.performingLayout = true;
        try {
            double[] dArray = d == -1.0 ? null : this.computeHeightsToFit(d).asArray();
            double d2 = this.snapSpaceX(this.getInsets().getLeft()) + this.computePrefWidths(dArray).computeTotalWithMultiSize() + this.snapSpaceX(this.getInsets().getRight());
            return d2;
        }
        finally {
            this.performingLayout = false;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected double computePrefHeight(double d) {
        this.computeGridMetrics();
        this.performingLayout = true;
        try {
            double[] dArray = d == -1.0 ? null : this.computeWidthsToFit(d).asArray();
            double d2 = this.snapSpaceY(this.getInsets().getTop()) + this.computePrefHeights(dArray).computeTotalWithMultiSize() + this.snapSpaceY(this.getInsets().getBottom());
            return d2;
        }
        finally {
            this.performingLayout = false;
        }
    }

    private VPos getRowValignment(int n) {
        RowConstraints rowConstraints;
        if (n < this.getRowConstraints().size() && (rowConstraints = (RowConstraints)this.getRowConstraints().get(n)).getValignment() != null) {
            return rowConstraints.getValignment();
        }
        return VPos.CENTER;
    }

    private HPos getColumnHalignment(int n) {
        ColumnConstraints columnConstraints;
        if (n < this.getColumnConstraints().size() && (columnConstraints = (ColumnConstraints)this.getColumnConstraints().get(n)).getHalignment() != null) {
            return columnConstraints.getHalignment();
        }
        return HPos.LEFT;
    }

    private double getColumnMinWidth(int n) {
        if (n < this.getColumnConstraints().size()) {
            ColumnConstraints columnConstraints = (ColumnConstraints)this.getColumnConstraints().get(n);
            return columnConstraints.getMinWidth();
        }
        return -1.0;
    }

    private double getRowMinHeight(int n) {
        if (n < this.getRowConstraints().size()) {
            RowConstraints rowConstraints = (RowConstraints)this.getRowConstraints().get(n);
            return rowConstraints.getMinHeight();
        }
        return -1.0;
    }

    private double getColumnMaxWidth(int n) {
        if (n < this.getColumnConstraints().size()) {
            ColumnConstraints columnConstraints = (ColumnConstraints)this.getColumnConstraints().get(n);
            return columnConstraints.getMaxWidth();
        }
        return -1.0;
    }

    private double getColumnPrefWidth(int n) {
        if (n < this.getColumnConstraints().size()) {
            ColumnConstraints columnConstraints = (ColumnConstraints)this.getColumnConstraints().get(n);
            return columnConstraints.getPrefWidth();
        }
        return -1.0;
    }

    private double getRowPrefHeight(int n) {
        if (n < this.getRowConstraints().size()) {
            RowConstraints rowConstraints = (RowConstraints)this.getRowConstraints().get(n);
            return rowConstraints.getPrefHeight();
        }
        return -1.0;
    }

    private double getRowMaxHeight(int n) {
        if (n < this.getRowConstraints().size()) {
            RowConstraints rowConstraints = (RowConstraints)this.getRowConstraints().get(n);
            return rowConstraints.getMaxHeight();
        }
        return -1.0;
    }

    private boolean shouldRowFillHeight(int n) {
        if (n < this.getRowConstraints().size()) {
            return ((RowConstraints)this.getRowConstraints().get(n)).isFillHeight();
        }
        return true;
    }

    private boolean shouldColumnFillWidth(int n) {
        if (n < this.getColumnConstraints().size()) {
            return ((ColumnConstraints)this.getColumnConstraints().get(n)).isFillWidth();
        }
        return true;
    }

    private double getTotalWidthOfNodeColumns(Node node, double[] dArray) {
        if (GridPane.getNodeColumnSpan(node) == 1) {
            return dArray[GridPane.getNodeColumnIndex(node)];
        }
        double d = 0.0;
        int n = this.getNodeColumnEndConvertRemaining(node);
        for (int i = GridPane.getNodeColumnIndex(node); i <= n; ++i) {
            d += dArray[i];
        }
        return d;
    }

    private CompositeSize computeMaxHeights() {
        if (this.rowMaxHeight == null) {
            this.rowMaxHeight = this.createCompositeRows(Double.MAX_VALUE);
            ObservableList<RowConstraints> observableList = this.getRowConstraints();
            CompositeSize compositeSize = null;
            for (int i = 0; i < observableList.size(); ++i) {
                RowConstraints rowConstraints = (RowConstraints)observableList.get(i);
                double d = rowConstraints.getMaxHeight();
                if (d == Double.NEGATIVE_INFINITY) {
                    if (compositeSize == null) {
                        compositeSize = this.computePrefHeights(null);
                    }
                    this.rowMaxHeight.setPresetSize(i, compositeSize.getSize(i));
                    continue;
                }
                if (d == -1.0) continue;
                double d2 = this.snapSizeY(d);
                double d3 = rowConstraints.getMinHeight();
                if (d3 >= 0.0) {
                    double d4 = this.snapSizeY(rowConstraints.getMinHeight());
                    this.rowMaxHeight.setPresetSize(i, GridPane.boundedSize(d4, d2, d2));
                    continue;
                }
                this.rowMaxHeight.setPresetSize(i, d2);
            }
        }
        return this.rowMaxHeight;
    }

    private CompositeSize computePrefHeights(double[] dArray) {
        double d;
        double d2;
        double d3;
        CompositeSize compositeSize;
        if (dArray == null) {
            if (this.rowPrefHeight != null) {
                return this.rowPrefHeight;
            }
            compositeSize = this.rowPrefHeight = this.createCompositeRows(0.0);
        } else {
            compositeSize = this.createCompositeRows(0.0);
        }
        ObservableList<RowConstraints> observableList = this.getRowConstraints();
        for (int i = 0; i < observableList.size(); ++i) {
            RowConstraints rowConstraints = (RowConstraints)observableList.get(i);
            double d4 = rowConstraints.getMinHeight();
            double d5 = rowConstraints.getPrefHeight();
            if (d5 != -1.0) {
                d3 = this.snapSizeY(d5);
                d2 = rowConstraints.getMaxHeight();
                if (d4 >= 0.0 || d2 >= 0.0) {
                    d = d4 < 0.0 ? 0.0 : this.snapSizeY(d4);
                    double d6 = d2 < 0.0 ? Double.POSITIVE_INFINITY : this.snapSizeY(d2);
                    compositeSize.setPresetSize(i, GridPane.boundedSize(d, d3, d6));
                    continue;
                }
                compositeSize.setPresetSize(i, d3);
                continue;
            }
            if (!(d4 > 0.0)) continue;
            compositeSize.setSize(i, this.snapSizeY(d4));
        }
        List list = this.getManagedChildren();
        int n = list.size();
        for (int i = 0; i < n; ++i) {
            Node node = (Node)list.get(i);
            int n2 = GridPane.getNodeRowIndex(node);
            int n3 = this.getNodeRowEndConvertRemaining(node);
            d3 = this.computeChildPrefAreaHeight(node, this.isNodePositionedByBaseline(node) ? this.rowPrefBaselineComplement[n2] : -1.0, GridPane.getMargin(node), dArray == null ? -1.0 : this.getTotalWidthOfNodeColumns(node, dArray));
            if (n2 == n3 && !compositeSize.isPreset(n2)) {
                d2 = this.getRowMinHeight(n2);
                d = this.getRowMaxHeight(n2);
                compositeSize.setMaxSize(n2, GridPane.boundedSize(d2 < 0.0 ? 0.0 : d2, d3, d < 0.0 ? Double.MAX_VALUE : d));
                continue;
            }
            if (n2 == n3) continue;
            compositeSize.setMaxMultiSize(n2, n3 + 1, d3);
        }
        return compositeSize;
    }

    private CompositeSize computeMinHeights(double[] dArray) {
        CompositeSize compositeSize;
        if (dArray == null) {
            if (this.rowMinHeight != null) {
                return this.rowMinHeight;
            }
            compositeSize = this.rowMinHeight = this.createCompositeRows(0.0);
        } else {
            compositeSize = this.createCompositeRows(0.0);
        }
        ObservableList<RowConstraints> observableList = this.getRowConstraints();
        CompositeSize compositeSize2 = null;
        for (int i = 0; i < observableList.size(); ++i) {
            double d = ((RowConstraints)observableList.get(i)).getMinHeight();
            if (d == Double.NEGATIVE_INFINITY) {
                if (compositeSize2 == null) {
                    compositeSize2 = this.computePrefHeights(dArray);
                }
                compositeSize.setPresetSize(i, compositeSize2.getSize(i));
                continue;
            }
            if (d == -1.0) continue;
            compositeSize.setPresetSize(i, this.snapSizeY(d));
        }
        List list = this.getManagedChildren();
        int n = list.size();
        for (int i = 0; i < n; ++i) {
            Node node = (Node)list.get(i);
            int n2 = GridPane.getNodeRowIndex(node);
            int n3 = this.getNodeRowEndConvertRemaining(node);
            double d = this.computeChildMinAreaHeight(node, this.isNodePositionedByBaseline(node) ? this.rowMinBaselineComplement[n2] : -1.0, GridPane.getMargin(node), dArray == null ? -1.0 : this.getTotalWidthOfNodeColumns(node, dArray));
            if (n2 == n3 && !compositeSize.isPreset(n2)) {
                compositeSize.setMaxSize(n2, d);
                continue;
            }
            if (n2 == n3) continue;
            compositeSize.setMaxMultiSize(n2, n3 + 1, d);
        }
        return compositeSize;
    }

    private double getTotalHeightOfNodeRows(Node node, double[] dArray) {
        if (GridPane.getNodeRowSpan(node) == 1) {
            return dArray[GridPane.getNodeRowIndex(node)];
        }
        double d = 0.0;
        int n = this.getNodeRowEndConvertRemaining(node);
        for (int i = GridPane.getNodeRowIndex(node); i <= n; ++i) {
            d += dArray[i];
        }
        return d;
    }

    private CompositeSize computeMaxWidths() {
        if (this.columnMaxWidth == null) {
            this.columnMaxWidth = this.createCompositeColumns(Double.MAX_VALUE);
            ObservableList<ColumnConstraints> observableList = this.getColumnConstraints();
            CompositeSize compositeSize = null;
            for (int i = 0; i < observableList.size(); ++i) {
                ColumnConstraints columnConstraints = (ColumnConstraints)observableList.get(i);
                double d = columnConstraints.getMaxWidth();
                if (d == Double.NEGATIVE_INFINITY) {
                    if (compositeSize == null) {
                        compositeSize = this.computePrefWidths(null);
                    }
                    this.columnMaxWidth.setPresetSize(i, compositeSize.getSize(i));
                    continue;
                }
                if (d == -1.0) continue;
                double d2 = this.snapSizeX(d);
                double d3 = columnConstraints.getMinWidth();
                if (d3 >= 0.0) {
                    double d4 = this.snapSizeX(d3);
                    this.columnMaxWidth.setPresetSize(i, GridPane.boundedSize(d4, d2, d2));
                    continue;
                }
                this.columnMaxWidth.setPresetSize(i, d2);
            }
        }
        return this.columnMaxWidth;
    }

    private CompositeSize computePrefWidths(double[] dArray) {
        double d;
        double d2;
        CompositeSize compositeSize;
        if (dArray == null) {
            if (this.columnPrefWidth != null) {
                return this.columnPrefWidth;
            }
            compositeSize = this.columnPrefWidth = this.createCompositeColumns(0.0);
        } else {
            compositeSize = this.createCompositeColumns(0.0);
        }
        ObservableList<ColumnConstraints> observableList = this.getColumnConstraints();
        for (int i = 0; i < observableList.size(); ++i) {
            ColumnConstraints columnConstraints = (ColumnConstraints)observableList.get(i);
            double d3 = columnConstraints.getPrefWidth();
            double d4 = columnConstraints.getMinWidth();
            if (d3 != -1.0) {
                d2 = this.snapSizeX(d3);
                d = columnConstraints.getMaxWidth();
                if (d4 >= 0.0 || d >= 0.0) {
                    double d5 = d4 < 0.0 ? 0.0 : this.snapSizeX(d4);
                    double d6 = d < 0.0 ? Double.POSITIVE_INFINITY : this.snapSizeX(d);
                    compositeSize.setPresetSize(i, GridPane.boundedSize(d5 < 0.0 ? 0.0 : d5, d2, d6 < 0.0 ? Double.POSITIVE_INFINITY : d6));
                    continue;
                }
                compositeSize.setPresetSize(i, d2);
                continue;
            }
            if (!(d4 > 0.0)) continue;
            compositeSize.setSize(i, this.snapSizeX(d4));
        }
        List list = this.getManagedChildren();
        int n = list.size();
        for (int i = 0; i < n; ++i) {
            int n2;
            Node node = (Node)list.get(i);
            int n3 = GridPane.getNodeColumnIndex(node);
            if (n3 == (n2 = this.getNodeColumnEndConvertRemaining(node)) && !compositeSize.isPreset(n3)) {
                d2 = this.getColumnMinWidth(n3);
                d = this.getColumnMaxWidth(n3);
                compositeSize.setMaxSize(n3, GridPane.boundedSize(d2 < 0.0 ? 0.0 : d2, this.computeChildPrefAreaWidth(node, this.getBaselineComplementForChild(node), GridPane.getMargin(node), dArray == null ? -1.0 : this.getTotalHeightOfNodeRows(node, dArray), false), d < 0.0 ? Double.MAX_VALUE : d));
                continue;
            }
            if (n3 == n2) continue;
            compositeSize.setMaxMultiSize(n3, n2 + 1, this.computeChildPrefAreaWidth(node, this.getBaselineComplementForChild(node), GridPane.getMargin(node), dArray == null ? -1.0 : this.getTotalHeightOfNodeRows(node, dArray), false));
        }
        return compositeSize;
    }

    private CompositeSize computeMinWidths(double[] dArray) {
        CompositeSize compositeSize;
        if (dArray == null) {
            if (this.columnMinWidth != null) {
                return this.columnMinWidth;
            }
            compositeSize = this.columnMinWidth = this.createCompositeColumns(0.0);
        } else {
            compositeSize = this.createCompositeColumns(0.0);
        }
        ObservableList<ColumnConstraints> observableList = this.getColumnConstraints();
        CompositeSize compositeSize2 = null;
        for (int i = 0; i < observableList.size(); ++i) {
            double d = ((ColumnConstraints)observableList.get(i)).getMinWidth();
            if (d == Double.NEGATIVE_INFINITY) {
                if (compositeSize2 == null) {
                    compositeSize2 = this.computePrefWidths(dArray);
                }
                compositeSize.setPresetSize(i, compositeSize2.getSize(i));
                continue;
            }
            if (d == -1.0) continue;
            compositeSize.setPresetSize(i, this.snapSizeX(d));
        }
        List list = this.getManagedChildren();
        int n = list.size();
        for (int i = 0; i < n; ++i) {
            int n2;
            Node node = (Node)list.get(i);
            int n3 = GridPane.getNodeColumnIndex(node);
            if (n3 == (n2 = this.getNodeColumnEndConvertRemaining(node)) && !compositeSize.isPreset(n3)) {
                compositeSize.setMaxSize(n3, this.computeChildMinAreaWidth(node, this.getBaselineComplementForChild(node), GridPane.getMargin(node), dArray == null ? -1.0 : this.getTotalHeightOfNodeRows(node, dArray), false));
                continue;
            }
            if (n3 == n2) continue;
            compositeSize.setMaxMultiSize(n3, n2 + 1, this.computeChildMinAreaWidth(node, this.getBaselineComplementForChild(node), GridPane.getMargin(node), dArray == null ? -1.0 : this.getTotalHeightOfNodeRows(node, dArray), false));
        }
        return compositeSize;
    }

    private CompositeSize computeHeightsToFit(double d) {
        assert (d != -1.0);
        CompositeSize compositeSize = this.rowPercentTotal == 100.0 ? this.createCompositeRows(0.0) : (CompositeSize)this.computePrefHeights(null).clone();
        this.adjustRowHeights(compositeSize, d);
        return compositeSize;
    }

    private CompositeSize computeWidthsToFit(double d) {
        assert (d != -1.0);
        CompositeSize compositeSize = this.columnPercentTotal == 100.0 ? this.createCompositeColumns(0.0) : (CompositeSize)this.computePrefWidths(null).clone();
        this.adjustColumnWidths(compositeSize, d);
        return compositeSize;
    }

    @Override
    public Orientation getContentBias() {
        this.computeGridMetrics();
        return this.bias;
    }

    @Override
    public void requestLayout() {
        if (this.performingLayout) {
            return;
        }
        if (this.metricsDirty) {
            super.requestLayout();
            return;
        }
        this.metricsDirty = true;
        this.bias = null;
        this.rowGrow = null;
        this.rowMaxHeight = null;
        this.rowPrefHeight = null;
        this.rowMinHeight = null;
        this.columnGrow = null;
        this.columnMaxWidth = null;
        this.columnPrefWidth = null;
        this.columnMinWidth = null;
        this.rowMaxBaselineComplement = null;
        this.rowPrefBaselineComplement = null;
        this.rowMinBaselineComplement = null;
        super.requestLayout();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void layoutChildren() {
        this.performingLayout = true;
        try {
            double d;
            double d2;
            CompositeSize compositeSize;
            CompositeSize compositeSize2;
            double d3 = this.snapSpaceX(this.getHgap());
            double d4 = this.snapSpaceY(this.getVgap());
            double d5 = this.snapSpaceY(this.getInsets().getTop());
            double d6 = this.snapSpaceY(this.getInsets().getBottom());
            double d7 = this.snapSpaceX(this.getInsets().getLeft());
            double d8 = this.snapSpaceX(this.getInsets().getRight());
            double d9 = this.getWidth();
            double d10 = this.getHeight();
            double d11 = d10 - d5 - d6;
            double d12 = d9 - d7 - d8;
            this.computeGridMetrics();
            Orientation orientation = this.getContentBias();
            if (orientation == null) {
                compositeSize2 = (CompositeSize)this.computePrefHeights(null).clone();
                compositeSize = (CompositeSize)this.computePrefWidths(null).clone();
                d2 = this.adjustRowHeights(compositeSize2, d10);
                d = this.adjustColumnWidths(compositeSize, d9);
            } else if (orientation == Orientation.HORIZONTAL) {
                compositeSize = (CompositeSize)this.computePrefWidths(null).clone();
                d = this.adjustColumnWidths(compositeSize, d9);
                compositeSize2 = this.computePrefHeights(compositeSize.asArray());
                d2 = this.adjustRowHeights(compositeSize2, d10);
            } else {
                compositeSize2 = (CompositeSize)this.computePrefHeights(null).clone();
                d2 = this.adjustRowHeights(compositeSize2, d10);
                compositeSize = this.computePrefWidths(compositeSize2.asArray());
                d = this.adjustColumnWidths(compositeSize, d9);
            }
            double d13 = d7 + GridPane.computeXOffset(d12, d, this.getAlignmentInternal().getHpos());
            double d14 = d5 + GridPane.computeYOffset(d11, d2, this.getAlignmentInternal().getVpos());
            List list = this.getManagedChildren();
            double[] dArray = GridPane.createDoubleArray(this.numRows, -1.0);
            int n3 = list.size();
            for (int i = 0; i < n3; ++i) {
                int n4;
                Node node = (Node)list.get(i);
                int n5 = GridPane.getNodeRowIndex(node);
                int n6 = GridPane.getNodeColumnIndex(node);
                int n7 = GridPane.getNodeColumnSpan(node);
                if (n7 == Integer.MAX_VALUE) {
                    n7 = compositeSize.getLength() - n6;
                }
                if ((n4 = GridPane.getNodeRowSpan(node)) == Integer.MAX_VALUE) {
                    n4 = compositeSize2.getLength() - n5;
                }
                double d15 = d13;
                for (int j = 0; j < n6; ++j) {
                    d15 += compositeSize.getSize(j) + d3;
                }
                double d16 = d14;
                for (int j = 0; j < n5; ++j) {
                    d16 += compositeSize2.getSize(j) + d4;
                }
                double d17 = compositeSize.getSize(n6);
                for (int j = 2; j <= n7; ++j) {
                    d17 += compositeSize.getSize(n6 + j - 1) + d3;
                }
                double d18 = compositeSize2.getSize(n5);
                for (int j = 2; j <= n4; ++j) {
                    d18 += compositeSize2.getSize(n5 + j - 1) + d4;
                }
                HPos hPos = GridPane.getHalignment(node);
                VPos vPos = GridPane.getValignment(node);
                Boolean bl = GridPane.isFillWidth(node);
                Boolean bl2 = GridPane.isFillHeight(node);
                if (hPos == null) {
                    hPos = this.getColumnHalignment(n6);
                }
                if (vPos == null) {
                    vPos = this.getRowValignment(n5);
                }
                if (bl == null) {
                    bl = this.shouldColumnFillWidth(n6);
                }
                if (bl2 == null) {
                    bl2 = this.shouldRowFillHeight(n5);
                }
                double d19 = 0.0;
                if (vPos == VPos.BASELINE) {
                    if (dArray[n5] == -1.0) {
                        dArray[n5] = this.getAreaBaselineOffset(this.rowBaseline[n5], marginAccessor, (Integer n2) -> {
                            Node node = this.rowBaseline[n5].get((int)n2);
                            int n3 = GridPane.getNodeColumnIndex(node);
                            int n4 = GridPane.getNodeColumnSpan(node);
                            if (n4 == Integer.MAX_VALUE) {
                                n4 = compositeSize.getLength() - n3;
                            }
                            double d2 = compositeSize.getSize(n3);
                            for (int i = 2; i <= n4; ++i) {
                                d2 += compositeSize.getSize(n3 + i - 1) + d3;
                            }
                            return d2;
                        }, d18, n -> {
                            Boolean bl = GridPane.isFillHeight(node);
                            if (bl != null) {
                                return bl;
                            }
                            return this.shouldRowFillHeight(GridPane.getNodeRowIndex(node));
                        }, this.rowMinBaselineComplement[n5]);
                    }
                    d19 = dArray[n5];
                }
                Insets insets = GridPane.getMargin(node);
                this.layoutInArea(node, d15, d16, d17, d18, d19, insets, bl, bl2, hPos, vPos);
            }
            this.layoutGridLines(compositeSize, compositeSize2, d13, d14, d2, d);
            this.currentHeights = compositeSize2;
            this.currentWidths = compositeSize;
        }
        finally {
            this.performingLayout = false;
        }
    }

    private double adjustRowHeights(CompositeSize compositeSize, double d) {
        double d2;
        double d3;
        assert (d != -1.0);
        double d4 = this.snapSpaceY(this.getVgap());
        double d5 = this.snapSpaceY(this.getInsets().getTop());
        double d6 = this.snapSpaceY(this.getInsets().getBottom());
        double d7 = d4 * (double)(this.getNumberOfRows() - 1);
        double d8 = d - d5 - d6;
        if (this.rowPercentTotal > 0.0) {
            d3 = 0.0;
            for (int i = 0; i < this.rowPercentHeight.length; ++i) {
                if (!(this.rowPercentHeight[i] >= 0.0)) continue;
                double d9 = (d8 - d7) * (this.rowPercentHeight[i] / 100.0);
                double d10 = Math.floor(d9);
                d3 += d9 - d10;
                d9 = d10;
                if (d3 >= 0.5) {
                    d9 += 1.0;
                    d3 = -1.0 + d3;
                }
                compositeSize.setSize(i, d9);
            }
        }
        d3 = compositeSize.computeTotal();
        if (this.rowPercentTotal < 100.0 && (d2 = d - d5 - d6 - d3) != 0.0) {
            double d11 = this.growToMultiSpanPreferredHeights(compositeSize, d2);
            d11 = this.growOrShrinkRowHeights(compositeSize, Priority.ALWAYS, d11);
            d11 = this.growOrShrinkRowHeights(compositeSize, Priority.SOMETIMES, d11);
            d3 += d2 - d11;
        }
        return d3;
    }

    private double growToMultiSpanPreferredHeights(CompositeSize compositeSize, double d) {
        double d2;
        double d3;
        double d4;
        int n;
        double d5;
        double d6;
        double d7;
        int n2;
        Iterator iterator;
        if (d <= 0.0) {
            return d;
        }
        TreeSet<Integer> treeSet = new TreeSet<Integer>();
        TreeSet<Integer> treeSet2 = new TreeSet<Integer>();
        TreeSet<Integer> treeSet3 = new TreeSet<Integer>();
        for (Map.Entry entry : compositeSize.multiSizes()) {
            Interval interval = (Interval)entry.getKey();
            block5: for (int i = interval.begin; i < interval.end; ++i) {
                if (!(this.rowPercentHeight[i] < 0.0)) continue;
                switch (this.rowGrow[i]) {
                    case ALWAYS: {
                        treeSet.add(i);
                        continue block5;
                    }
                    case SOMETIMES: {
                        treeSet2.add(i);
                    }
                }
            }
            if (!(this.rowPercentHeight[interval.end - 1] < 0.0)) continue;
            treeSet3.add(interval.end - 1);
        }
        double d8 = d;
        while (treeSet.size() > 0 && d8 > (double)treeSet.size()) {
            double d9 = Math.floor(d8 / (double)treeSet.size());
            iterator = treeSet.iterator();
            while (iterator.hasNext()) {
                n2 = (Integer)iterator.next();
                d7 = this.getRowMaxHeight(n2);
                d6 = this.getRowPrefHeight(n2);
                d5 = d9;
                for (Map.Entry entry : compositeSize.multiSizes()) {
                    Interval interval = (Interval)entry.getKey();
                    if (!interval.contains(n2)) continue;
                    n = 0;
                    for (int i = interval.begin; i < interval.end; ++i) {
                        if (!treeSet.contains(i)) continue;
                        ++n;
                    }
                    d4 = compositeSize.computeTotal(interval.begin, interval.end);
                    d5 = Math.min(Math.floor(Math.max(0.0, ((Double)entry.getValue() - d4) / (double)n)), d5);
                }
                d3 = compositeSize.getSize(n2);
                d2 = d7 >= 0.0 ? GridPane.boundedSize(0.0, d3 + d5, d7) : (d7 == Double.NEGATIVE_INFINITY && d6 > 0.0 ? GridPane.boundedSize(0.0, d3 + d5, d6) : d3 + d5);
                d4 = d2 - d3;
                d8 -= d4;
                if (d4 != d5 || d4 == 0.0) {
                    iterator.remove();
                }
                compositeSize.setSize(n2, d2);
            }
        }
        while (treeSet2.size() > 0 && d8 > (double)treeSet2.size()) {
            double d10 = Math.floor(d8 / (double)treeSet2.size());
            iterator = treeSet2.iterator();
            while (iterator.hasNext()) {
                n2 = (Integer)iterator.next();
                d7 = this.getRowMaxHeight(n2);
                d6 = this.getRowPrefHeight(n2);
                d5 = d10;
                for (Map.Entry entry : compositeSize.multiSizes()) {
                    Interval interval = (Interval)entry.getKey();
                    if (!interval.contains(n2)) continue;
                    n = 0;
                    for (int i = interval.begin; i < interval.end; ++i) {
                        if (!treeSet2.contains(i)) continue;
                        ++n;
                    }
                    d4 = compositeSize.computeTotal(interval.begin, interval.end);
                    d5 = Math.min(Math.floor(Math.max(0.0, ((Double)entry.getValue() - d4) / (double)n)), d5);
                }
                d3 = compositeSize.getSize(n2);
                d2 = d7 >= 0.0 ? GridPane.boundedSize(0.0, d3 + d5, d7) : (d7 == Double.NEGATIVE_INFINITY && d6 > 0.0 ? GridPane.boundedSize(0.0, d3 + d5, d6) : d3 + d5);
                d4 = d2 - d3;
                d8 -= d4;
                if (d4 != d5 || d4 == 0.0) {
                    iterator.remove();
                }
                compositeSize.setSize(n2, d2);
            }
        }
        while (treeSet3.size() > 0 && d8 > (double)treeSet3.size()) {
            double d11 = Math.floor(d8 / (double)treeSet3.size());
            iterator = treeSet3.iterator();
            while (iterator.hasNext()) {
                n2 = (Integer)iterator.next();
                d7 = this.getRowMaxHeight(n2);
                d6 = this.getRowPrefHeight(n2);
                d5 = d11;
                for (Map.Entry entry : compositeSize.multiSizes()) {
                    Interval interval = (Interval)entry.getKey();
                    if (interval.end - 1 != n2) continue;
                    double d12 = compositeSize.computeTotal(interval.begin, interval.end);
                    d5 = Math.min(Math.max(0.0, (Double)entry.getValue() - d12), d5);
                }
                d3 = compositeSize.getSize(n2);
                d2 = d7 >= 0.0 ? GridPane.boundedSize(0.0, d3 + d5, d7) : (d7 == Double.NEGATIVE_INFINITY && d6 > 0.0 ? GridPane.boundedSize(0.0, d3 + d5, d6) : d3 + d5);
                d4 = d2 - d3;
                d8 -= d4;
                if (d4 != d5 || d4 == 0.0) {
                    iterator.remove();
                }
                compositeSize.setSize(n2, d2);
            }
        }
        return d8;
    }

    private double growOrShrinkRowHeights(CompositeSize compositeSize, Priority priority, double d) {
        CompositeSize compositeSize2;
        boolean bl;
        boolean bl2 = d < 0.0;
        ArrayList<Integer> arrayList = new ArrayList<Integer>();
        for (int i = 0; i < this.rowGrow.length; ++i) {
            if (!(this.rowPercentHeight[i] < 0.0) || !bl2 && this.rowGrow[i] != priority) continue;
            arrayList.add(i);
        }
        double d2 = d;
        boolean bl3 = false;
        double d3 = 0.0;
        boolean bl4 = bl = d2 >= 0.0;
        CompositeSize compositeSize3 = compositeSize2 = bl2 ? this.computeMinHeights(null) : this.computeMaxHeights();
        block1: while (d2 != 0.0 && bl == bl4 && arrayList.size() > 0) {
            if (!bl3) {
                double d4 = d3 = d2 > 0.0 ? Math.floor(d2 / (double)arrayList.size()) : Math.ceil(d2 / (double)arrayList.size());
            }
            if (d3 != 0.0) {
                Iterator iterator = arrayList.iterator();
                while (iterator.hasNext()) {
                    int n = (Integer)iterator.next();
                    double d5 = this.snapSpaceY(compositeSize2.getProportionalMinOrMaxSize(n, bl2)) - compositeSize.getSize(n);
                    if (bl2 && d5 > 0.0 || !bl2 && d5 < 0.0) {
                        d5 = 0.0;
                    }
                    double d6 = Math.abs(d5) <= Math.abs(d3) ? d5 : d3;
                    compositeSize.addSize(n, d6);
                    boolean bl5 = bl4 = (d2 -= d6) >= 0.0;
                    if (Math.abs(d6) < Math.abs(d3)) {
                        iterator.remove();
                    }
                    if (d2 != 0.0) continue;
                    continue block1;
                }
                continue;
            }
            d3 = (int)d2 % arrayList.size();
            if (d3 == 0.0) break;
            d3 = bl2 ? -1.0 : 1.0;
            bl3 = true;
        }
        return d2;
    }

    private double adjustColumnWidths(CompositeSize compositeSize, double d) {
        double d2;
        double d3;
        assert (d != -1.0);
        double d4 = this.snapSpaceX(this.getHgap());
        double d5 = this.snapSpaceX(this.getInsets().getLeft());
        double d6 = this.snapSpaceX(this.getInsets().getRight());
        double d7 = d4 * (double)(this.getNumberOfColumns() - 1);
        double d8 = d - d5 - d6;
        if (this.columnPercentTotal > 0.0) {
            d3 = 0.0;
            for (int i = 0; i < this.columnPercentWidth.length; ++i) {
                if (!(this.columnPercentWidth[i] >= 0.0)) continue;
                double d9 = (d8 - d7) * (this.columnPercentWidth[i] / 100.0);
                double d10 = Math.floor(d9);
                d3 += d9 - d10;
                d9 = d10;
                if (d3 >= 0.5) {
                    d9 += 1.0;
                    d3 = -1.0 + d3;
                }
                compositeSize.setSize(i, d9);
            }
        }
        d3 = compositeSize.computeTotal();
        if (this.columnPercentTotal < 100.0 && (d2 = d - d5 - d6 - d3) != 0.0) {
            double d11 = this.growToMultiSpanPreferredWidths(compositeSize, d2);
            d11 = this.growOrShrinkColumnWidths(compositeSize, Priority.ALWAYS, d11);
            d11 = this.growOrShrinkColumnWidths(compositeSize, Priority.SOMETIMES, d11);
            d3 += d2 - d11;
        }
        return d3;
    }

    private double growToMultiSpanPreferredWidths(CompositeSize compositeSize, double d) {
        double d2;
        double d3;
        double d4;
        int n;
        double d5;
        double d6;
        double d7;
        int n2;
        Iterator iterator;
        if (d <= 0.0) {
            return d;
        }
        TreeSet<Integer> treeSet = new TreeSet<Integer>();
        TreeSet<Integer> treeSet2 = new TreeSet<Integer>();
        TreeSet<Integer> treeSet3 = new TreeSet<Integer>();
        for (Map.Entry entry : compositeSize.multiSizes()) {
            Interval interval = (Interval)entry.getKey();
            block5: for (int i = interval.begin; i < interval.end; ++i) {
                if (!(this.columnPercentWidth[i] < 0.0)) continue;
                switch (this.columnGrow[i]) {
                    case ALWAYS: {
                        treeSet.add(i);
                        continue block5;
                    }
                    case SOMETIMES: {
                        treeSet2.add(i);
                    }
                }
            }
            if (!(this.columnPercentWidth[interval.end - 1] < 0.0)) continue;
            treeSet3.add(interval.end - 1);
        }
        double d8 = d;
        while (treeSet.size() > 0 && d8 > (double)treeSet.size()) {
            double d9 = Math.floor(d8 / (double)treeSet.size());
            iterator = treeSet.iterator();
            while (iterator.hasNext()) {
                n2 = (Integer)iterator.next();
                d7 = this.getColumnMaxWidth(n2);
                d6 = this.getColumnPrefWidth(n2);
                d5 = d9;
                for (Map.Entry entry : compositeSize.multiSizes()) {
                    Interval interval = (Interval)entry.getKey();
                    if (!interval.contains(n2)) continue;
                    n = 0;
                    for (int i = interval.begin; i < interval.end; ++i) {
                        if (!treeSet.contains(i)) continue;
                        ++n;
                    }
                    d4 = compositeSize.computeTotal(interval.begin, interval.end);
                    d5 = Math.min(Math.floor(Math.max(0.0, ((Double)entry.getValue() - d4) / (double)n)), d5);
                }
                d3 = compositeSize.getSize(n2);
                d2 = d7 >= 0.0 ? GridPane.boundedSize(0.0, d3 + d5, d7) : (d7 == Double.NEGATIVE_INFINITY && d6 > 0.0 ? GridPane.boundedSize(0.0, d3 + d5, d6) : d3 + d5);
                d4 = d2 - d3;
                d8 -= d4;
                if (d4 != d5 || d4 == 0.0) {
                    iterator.remove();
                }
                compositeSize.setSize(n2, d2);
            }
        }
        while (treeSet2.size() > 0 && d8 > (double)treeSet2.size()) {
            double d10 = Math.floor(d8 / (double)treeSet2.size());
            iterator = treeSet2.iterator();
            while (iterator.hasNext()) {
                n2 = (Integer)iterator.next();
                d7 = this.getColumnMaxWidth(n2);
                d6 = this.getColumnPrefWidth(n2);
                d5 = d10;
                for (Map.Entry entry : compositeSize.multiSizes()) {
                    Interval interval = (Interval)entry.getKey();
                    if (!interval.contains(n2)) continue;
                    n = 0;
                    for (int i = interval.begin; i < interval.end; ++i) {
                        if (!treeSet2.contains(i)) continue;
                        ++n;
                    }
                    d4 = compositeSize.computeTotal(interval.begin, interval.end);
                    d5 = Math.min(Math.floor(Math.max(0.0, ((Double)entry.getValue() - d4) / (double)n)), d5);
                }
                d3 = compositeSize.getSize(n2);
                d2 = d7 >= 0.0 ? GridPane.boundedSize(0.0, d3 + d5, d7) : (d7 == Double.NEGATIVE_INFINITY && d6 > 0.0 ? GridPane.boundedSize(0.0, d3 + d5, d6) : d3 + d5);
                d4 = d2 - d3;
                d8 -= d4;
                if (d4 != d5 || d4 == 0.0) {
                    iterator.remove();
                }
                compositeSize.setSize(n2, d2);
            }
        }
        while (treeSet3.size() > 0 && d8 > (double)treeSet3.size()) {
            double d11 = Math.floor(d8 / (double)treeSet3.size());
            iterator = treeSet3.iterator();
            while (iterator.hasNext()) {
                n2 = (Integer)iterator.next();
                d7 = this.getColumnMaxWidth(n2);
                d6 = this.getColumnPrefWidth(n2);
                d5 = d11;
                for (Map.Entry entry : compositeSize.multiSizes()) {
                    Interval interval = (Interval)entry.getKey();
                    if (interval.end - 1 != n2) continue;
                    double d12 = compositeSize.computeTotal(interval.begin, interval.end);
                    d5 = Math.min(Math.max(0.0, (Double)entry.getValue() - d12), d5);
                }
                d3 = compositeSize.getSize(n2);
                d2 = d7 >= 0.0 ? GridPane.boundedSize(0.0, d3 + d5, d7) : (d7 == Double.NEGATIVE_INFINITY && d6 > 0.0 ? GridPane.boundedSize(0.0, d3 + d5, d6) : d3 + d5);
                d4 = d2 - d3;
                d8 -= d4;
                if (d4 != d5 || d4 == 0.0) {
                    iterator.remove();
                }
                compositeSize.setSize(n2, d2);
            }
        }
        return d8;
    }

    private double growOrShrinkColumnWidths(CompositeSize compositeSize, Priority priority, double d) {
        CompositeSize compositeSize2;
        boolean bl;
        if (d == 0.0) {
            return 0.0;
        }
        boolean bl2 = d < 0.0;
        ArrayList<Integer> arrayList = new ArrayList<Integer>();
        for (int i = 0; i < this.columnGrow.length; ++i) {
            if (!(this.columnPercentWidth[i] < 0.0) || !bl2 && this.columnGrow[i] != priority) continue;
            arrayList.add(i);
        }
        double d2 = d;
        boolean bl3 = false;
        double d3 = 0.0;
        boolean bl4 = bl = d2 >= 0.0;
        CompositeSize compositeSize3 = compositeSize2 = bl2 ? this.computeMinWidths(null) : this.computeMaxWidths();
        block1: while (d2 != 0.0 && bl == bl4 && arrayList.size() > 0) {
            if (!bl3) {
                double d4 = d3 = d2 > 0.0 ? Math.floor(d2 / (double)arrayList.size()) : Math.ceil(d2 / (double)arrayList.size());
            }
            if (d3 != 0.0) {
                Iterator iterator = arrayList.iterator();
                while (iterator.hasNext()) {
                    int n = (Integer)iterator.next();
                    double d5 = this.snapSpaceX(compositeSize2.getProportionalMinOrMaxSize(n, bl2)) - compositeSize.getSize(n);
                    if (bl2 && d5 > 0.0 || !bl2 && d5 < 0.0) {
                        d5 = 0.0;
                    }
                    double d6 = Math.abs(d5) <= Math.abs(d3) ? d5 : d3;
                    compositeSize.addSize(n, d6);
                    boolean bl5 = bl4 = (d2 -= d6) >= 0.0;
                    if (Math.abs(d6) < Math.abs(d3)) {
                        iterator.remove();
                    }
                    if (d2 != 0.0) continue;
                    continue block1;
                }
                continue;
            }
            d3 = (int)d2 % arrayList.size();
            if (d3 == 0.0) break;
            d3 = bl2 ? -1.0 : 1.0;
            bl3 = true;
        }
        return d2;
    }

    private void layoutGridLines(CompositeSize compositeSize, CompositeSize compositeSize2, double d, double d2, double d3, double d4) {
        int n;
        if (!this.isGridLinesVisible()) {
            return;
        }
        if (!this.gridLines.getChildren().isEmpty()) {
            this.gridLines.getChildren().clear();
        }
        double d5 = this.snapSpaceX(this.getHgap());
        double d6 = this.snapSpaceY(this.getVgap());
        double d7 = d;
        double d8 = d2;
        for (n = 0; n <= compositeSize.getLength(); ++n) {
            this.gridLines.getChildren().add(this.createGridLine(d7, d8, d7, d8 + d3));
            if (n > 0 && n < compositeSize.getLength() && d5 != 0.0) {
                this.gridLines.getChildren().add(this.createGridLine(d7 += d5, d8, d7, d8 + d3));
            }
            if (n >= compositeSize.getLength()) continue;
            d7 += compositeSize.getSize(n);
        }
        d7 = d;
        for (n = 0; n <= compositeSize2.getLength(); ++n) {
            this.gridLines.getChildren().add(this.createGridLine(d7, d8, d7 + d4, d8));
            if (n > 0 && n < compositeSize2.getLength() && d6 != 0.0) {
                this.gridLines.getChildren().add(this.createGridLine(d7, d8 += d6, d7 + d4, d8));
            }
            if (n >= compositeSize2.getLength()) continue;
            d8 += compositeSize2.getSize(n);
        }
    }

    private Line createGridLine(double d, double d2, double d3, double d4) {
        Line line = new Line();
        line.setStartX(d);
        line.setStartY(d2);
        line.setEndX(d3);
        line.setEndY(d4);
        line.setStroke(GRID_LINE_COLOR);
        line.setStrokeDashOffset(3.0);
        return line;
    }

    @Override
    public String toString() {
        return "Grid hgap=" + this.getHgap() + ", vgap=" + this.getVgap() + ", alignment=" + this.getAlignment();
    }

    private CompositeSize createCompositeRows(double d) {
        return new CompositeSize(this.getNumberOfRows(), this.rowPercentHeight, this.rowPercentTotal, this.snapSpaceY(this.getVgap()), d);
    }

    private CompositeSize createCompositeColumns(double d) {
        return new CompositeSize(this.getNumberOfColumns(), this.columnPercentWidth, this.columnPercentTotal, this.snapSpaceX(this.getHgap()), d);
    }

    private int getNodeRowEndConvertRemaining(Node node) {
        int n = GridPane.getNodeRowSpan(node);
        return n != Integer.MAX_VALUE ? GridPane.getNodeRowIndex(node) + n - 1 : this.getNumberOfRows() - 1;
    }

    private int getNodeColumnEndConvertRemaining(Node node) {
        int n = GridPane.getNodeColumnSpan(node);
        return n != Integer.MAX_VALUE ? GridPane.getNodeColumnIndex(node) + n - 1 : this.getNumberOfColumns() - 1;
    }

    double[][] getGrid() {
        if (this.currentHeights == null || this.currentWidths == null) {
            return null;
        }
        return new double[][]{this.currentWidths.asArray(), this.currentHeights.asArray()};
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return GridPane.getClassCssMetaData();
    }

    public final int getRowCount() {
        int n = this.getRowConstraints().size();
        for (int i = 0; i < this.getChildren().size(); ++i) {
            Node node = (Node)this.getChildren().get(i);
            if (!node.isManaged()) continue;
            int n2 = GridPane.getNodeRowIndex(node);
            int n3 = GridPane.getNodeRowEnd(node);
            n = Math.max(n, (n3 != Integer.MAX_VALUE ? n3 : n2) + 1);
        }
        return n;
    }

    public final int getColumnCount() {
        int n = this.getColumnConstraints().size();
        for (int i = 0; i < this.getChildren().size(); ++i) {
            Node node = (Node)this.getChildren().get(i);
            if (!node.isManaged()) continue;
            int n2 = GridPane.getNodeColumnIndex(node);
            int n3 = GridPane.getNodeColumnEnd(node);
            n = Math.max(n, (n3 != Integer.MAX_VALUE ? n3 : n2) + 1);
        }
        return n;
    }

    public final Bounds getCellBounds(int n, int n2) {
        double[] dArray;
        double[] dArray2;
        double d = this.snapSpaceX(this.getHgap());
        double d2 = this.snapSpaceY(this.getVgap());
        double d3 = this.snapSpaceY(this.getInsets().getTop());
        double d4 = this.snapSpaceX(this.getInsets().getRight());
        double d5 = this.snapSpaceY(this.getInsets().getBottom());
        double d6 = this.snapSpaceX(this.getInsets().getLeft());
        double d7 = this.snapSizeY(this.getHeight()) - (d3 + d5);
        double d8 = this.snapSizeX(this.getWidth()) - (d6 + d4);
        double[][] dArray3 = this.getGrid();
        if (dArray3 == null) {
            dArray2 = new double[]{0.0};
            n2 = 0;
            dArray = new double[]{0.0};
            n = 0;
        } else {
            dArray = dArray3[0];
            dArray2 = dArray3[1];
        }
        double d9 = 0.0;
        for (int i = 0; i < dArray2.length; ++i) {
            d9 += dArray2[i];
        }
        double d10 = d3 + Region.computeYOffset(d7, d9 += (double)(dArray2.length - 1) * d2, this.getAlignment().getVpos());
        double d11 = dArray2[n2];
        for (int i = 0; i < n2; ++i) {
            d10 += dArray2[i] + d2;
        }
        double d12 = 0.0;
        for (int i = 0; i < dArray.length; ++i) {
            d12 += dArray[i];
        }
        double d13 = d6 + Region.computeXOffset(d8, d12 += (double)(dArray.length - 1) * d, this.getAlignment().getHpos());
        double d14 = dArray[n];
        for (int i = 0; i < n; ++i) {
            d13 += dArray[i] + d;
        }
        return new BoundingBox(d13, d10, d14, d11);
    }

    private static final class CompositeSize
    implements Cloneable {
        double[] singleSizes;
        private SortedMap<Interval, Double> multiSizes;
        private BitSet preset;
        private final double[] fixedPercent;
        private final double totalFixedPercent;
        private final double gap;

        public CompositeSize(int n, double[] dArray, double d, double d2, double d3) {
            this.singleSizes = new double[n];
            Arrays.fill(this.singleSizes, d3);
            this.fixedPercent = dArray;
            this.totalFixedPercent = d;
            this.gap = d2;
        }

        private void setSize(int n, double d) {
            this.singleSizes[n] = d;
        }

        private void setPresetSize(int n, double d) {
            this.setSize(n, d);
            if (this.preset == null) {
                this.preset = new BitSet(this.singleSizes.length);
            }
            this.preset.set(n);
        }

        private boolean isPreset(int n) {
            if (this.preset == null) {
                return false;
            }
            return this.preset.get(n);
        }

        private void addSize(int n, double d) {
            this.singleSizes[n] = this.singleSizes[n] + d;
        }

        private double getSize(int n) {
            return this.singleSizes[n];
        }

        private void setMaxSize(int n, double d) {
            this.singleSizes[n] = Math.max(this.singleSizes[n], d);
        }

        private void setMultiSize(int n, int n2, double d) {
            if (this.multiSizes == null) {
                this.multiSizes = new TreeMap<Interval, Double>();
            }
            Interval interval = new Interval(n, n2);
            this.multiSizes.put(interval, d);
        }

        private Iterable<Map.Entry<Interval, Double>> multiSizes() {
            if (this.multiSizes == null) {
                return Collections.EMPTY_LIST;
            }
            return this.multiSizes.entrySet();
        }

        private void setMaxMultiSize(int n, int n2, double d) {
            Interval interval;
            Double d2;
            if (this.multiSizes == null) {
                this.multiSizes = new TreeMap<Interval, Double>();
            }
            if ((d2 = (Double)this.multiSizes.get(interval = new Interval(n, n2))) == null) {
                this.multiSizes.put(interval, d);
            } else {
                this.multiSizes.put(interval, Math.max(d, d2));
            }
        }

        private double getProportionalMinOrMaxSize(int n, boolean bl) {
            double d = this.singleSizes[n];
            if (!this.isPreset(n) && this.multiSizes != null) {
                for (Interval interval : this.multiSizes.keySet()) {
                    double d2;
                    if (!interval.contains(n)) continue;
                    double d3 = d2 = (Double)this.multiSizes.get(interval) / (double)interval.size();
                    for (int i = interval.begin; i < interval.end; ++i) {
                        if (i == n || !(bl ? this.singleSizes[i] > d2 : this.singleSizes[i] < d2)) continue;
                        d3 += d2 - this.singleSizes[i];
                    }
                    d = bl ? Math.max(d, d3) : Math.min(d, d3);
                }
            }
            return d;
        }

        private double computeTotal(int n, int n2) {
            double d = this.gap * (double)(n2 - n - 1);
            for (int i = n; i < n2; ++i) {
                d += this.singleSizes[i];
            }
            return d;
        }

        private double computeTotal() {
            return this.computeTotal(0, this.singleSizes.length);
        }

        private boolean allPreset(int n, int n2) {
            if (this.preset == null) {
                return false;
            }
            for (int i = n; i < n2; ++i) {
                if (this.preset.get(i)) continue;
                return false;
            }
            return true;
        }

        private double computeTotalWithMultiSize() {
            double d = this.computeTotal();
            if (this.multiSizes != null) {
                for (Map.Entry<Interval, Double> entry : this.multiSizes.entrySet()) {
                    Interval interval = entry.getKey();
                    if (this.allPreset(interval.begin, interval.end)) continue;
                    double d2 = this.computeTotal(interval.begin, interval.end);
                    if (!(entry.getValue() > d2)) continue;
                    d += entry.getValue() - d2;
                }
            }
            if (this.totalFixedPercent > 0.0) {
                int n;
                double d3 = 0.0;
                for (n = 0; n < this.fixedPercent.length; ++n) {
                    if (this.fixedPercent[n] != 0.0) continue;
                    d -= this.singleSizes[n];
                }
                for (n = 0; n < this.fixedPercent.length; ++n) {
                    if (this.fixedPercent[n] > 0.0) {
                        d = Math.max(d, this.singleSizes[n] * (100.0 / this.fixedPercent[n]));
                        continue;
                    }
                    if (!(this.fixedPercent[n] < 0.0)) continue;
                    d3 += this.singleSizes[n];
                }
                if (this.totalFixedPercent < 100.0) {
                    d = Math.max(d, d3 * 100.0 / (100.0 - this.totalFixedPercent));
                }
            }
            return d;
        }

        private int getLength() {
            return this.singleSizes.length;
        }

        protected Object clone() {
            try {
                CompositeSize compositeSize = (CompositeSize)super.clone();
                compositeSize.singleSizes = (double[])compositeSize.singleSizes.clone();
                if (this.multiSizes != null) {
                    compositeSize.multiSizes = new TreeMap<Interval, Double>(compositeSize.multiSizes);
                }
                return compositeSize;
            }
            catch (CloneNotSupportedException cloneNotSupportedException) {
                throw new RuntimeException(cloneNotSupportedException);
            }
        }

        private double[] asArray() {
            return this.singleSizes;
        }
    }

    private static final class Interval
    implements Comparable<Interval> {
        public final int begin;
        public final int end;

        public Interval(int n, int n2) {
            this.begin = n;
            this.end = n2;
        }

        @Override
        public int compareTo(Interval interval) {
            return this.begin != interval.begin ? this.begin - interval.begin : this.end - interval.end;
        }

        private boolean contains(int n) {
            return this.begin <= n && n < this.end;
        }

        private int size() {
            return this.end - this.begin;
        }
    }

    private static class StyleableProperties {
        private static final CssMetaData<GridPane, Boolean> GRID_LINES_VISIBLE = new CssMetaData<GridPane, Boolean>("-fx-grid-lines-visible", BooleanConverter.getInstance(), Boolean.FALSE){

            @Override
            public boolean isSettable(GridPane gridPane) {
                return gridPane.gridLinesVisible == null || !gridPane.gridLinesVisible.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(GridPane gridPane) {
                return (StyleableProperty)((Object)gridPane.gridLinesVisibleProperty());
            }
        };
        private static final CssMetaData<GridPane, Number> HGAP = new CssMetaData<GridPane, Number>("-fx-hgap", SizeConverter.getInstance(), (Number)0.0){

            @Override
            public boolean isSettable(GridPane gridPane) {
                return gridPane.hgap == null || !gridPane.hgap.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(GridPane gridPane) {
                return (StyleableProperty)((Object)gridPane.hgapProperty());
            }
        };
        private static final CssMetaData<GridPane, Pos> ALIGNMENT = new CssMetaData<GridPane, Pos>("-fx-alignment", new EnumConverter<Pos>(Pos.class), Pos.TOP_LEFT){

            @Override
            public boolean isSettable(GridPane gridPane) {
                return gridPane.alignment == null || !gridPane.alignment.isBound();
            }

            @Override
            public StyleableProperty<Pos> getStyleableProperty(GridPane gridPane) {
                return (StyleableProperty)((Object)gridPane.alignmentProperty());
            }
        };
        private static final CssMetaData<GridPane, Number> VGAP = new CssMetaData<GridPane, Number>("-fx-vgap", SizeConverter.getInstance(), (Number)0.0){

            @Override
            public boolean isSettable(GridPane gridPane) {
                return gridPane.vgap == null || !gridPane.vgap.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(GridPane gridPane) {
                return (StyleableProperty)((Object)gridPane.vgapProperty());
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList arrayList = new ArrayList(Region.getClassCssMetaData());
            arrayList.add(GRID_LINES_VISIBLE);
            arrayList.add(HGAP);
            arrayList.add(ALIGNMENT);
            arrayList.add(VGAP);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }
}

