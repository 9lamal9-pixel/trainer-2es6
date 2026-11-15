/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.control.SplitPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

public class SplitPaneSkin
extends SkinBase<SplitPane> {
    private ObservableList<Content> contentRegions;
    private ObservableList<ContentDivider> contentDividers;
    private boolean horizontal = ((SplitPane)this.getSkinnable()).getOrientation() == Orientation.HORIZONTAL;
    private double previousSize = -1.0;
    private int lastDividerUpdate = 0;
    private boolean resize = false;
    private boolean checkDividerPos = true;

    public SplitPaneSkin(SplitPane splitPane) {
        super(splitPane);
        this.contentRegions = FXCollections.observableArrayList();
        this.contentDividers = FXCollections.observableArrayList();
        int n = 0;
        for (Node object : ((SplitPane)this.getSkinnable()).getItems()) {
            this.addContent(n++, object);
        }
        this.initializeContentListener();
        for (SplitPane.Divider divider : ((SplitPane)this.getSkinnable()).getDividers()) {
            this.addDivider(divider);
        }
        this.registerChangeListener(splitPane.orientationProperty(), observableValue -> {
            this.horizontal = ((SplitPane)this.getSkinnable()).getOrientation() == Orientation.HORIZONTAL;
            this.previousSize = -1.0;
            for (ContentDivider contentDivider : this.contentDividers) {
                contentDivider.setGrabberStyle(this.horizontal);
            }
            ((SplitPane)this.getSkinnable()).requestLayout();
        });
        this.registerChangeListener(splitPane.widthProperty(), observableValue -> ((SplitPane)this.getSkinnable()).requestLayout());
        this.registerChangeListener(splitPane.heightProperty(), observableValue -> ((SplitPane)this.getSkinnable()).requestLayout());
    }

    /*
     * Enabled aggressive block sorting
     */
    @Override
    protected void layoutChildren(double d, double d2, double d3, double d4) {
        int n;
        int n2;
        double d5;
        block61: {
            double d6;
            double d7;
            block60: {
                int n3;
                boolean bl;
                ArrayList<Content> arrayList;
                block56: {
                    block59: {
                        SplitPane splitPane = (SplitPane)this.getSkinnable();
                        d7 = splitPane.getWidth();
                        d6 = splitPane.getHeight();
                        if (this.horizontal) {
                            if (d7 == 0.0) {
                                return;
                            }
                        } else if (d6 == 0.0) return;
                        if (this.contentRegions.isEmpty()) {
                            return;
                        }
                        double d8 = d5 = this.contentDividers.isEmpty() ? 0.0 : ((ContentDivider)this.contentDividers.get(0)).prefWidth(-1.0);
                        if (this.contentDividers.size() <= 0 || this.previousSize == -1.0 || this.previousSize == (this.horizontal ? d7 : d6)) break block59;
                        arrayList = new ArrayList<Content>();
                        for (Content content : this.contentRegions) {
                            if (!content.isResizableWithParent()) continue;
                            arrayList.add(content);
                        }
                        double d9 = (this.horizontal ? splitPane.getWidth() : splitPane.getHeight()) - this.previousSize;
                        bl = d9 > 0.0;
                        if ((d9 = Math.abs(d9)) == 0.0 || arrayList.isEmpty()) break block60;
                        n2 = (int)d9 / arrayList.size();
                        n = (int)d9 % arrayList.size();
                        n3 = 0;
                        if (n2 == 0) {
                            n2 = n;
                            n3 = n;
                            n = 0;
                            break block56;
                        } else {
                            n3 = n2 * arrayList.size();
                        }
                        break block56;
                    }
                    this.previousSize = this.horizontal ? d7 : d6;
                    break block61;
                }
                while (n3 > 0 && !arrayList.isEmpty()) {
                    double d10;
                    Content content;
                    block57: {
                        double d11;
                        int n4;
                        if (bl) {
                            ++this.lastDividerUpdate;
                        } else {
                            --this.lastDividerUpdate;
                            if (this.lastDividerUpdate < 0) {
                                this.lastDividerUpdate = this.contentRegions.size() - 1;
                            }
                        }
                        if (!(content = (Content)this.contentRegions.get(n4 = this.lastDividerUpdate % this.contentRegions.size())).isResizableWithParent() || !arrayList.contains(content)) continue;
                        d10 = content.getArea();
                        if (bl) {
                            double d12 = d11 = this.horizontal ? content.maxWidth(-1.0) : content.maxHeight(-1.0);
                            if (d10 + (double)n2 <= d11) {
                                d10 += (double)n2;
                                break block57;
                            } else {
                                arrayList.remove(content);
                                continue;
                            }
                        }
                        double d13 = d11 = this.horizontal ? content.minWidth(-1.0) : content.minHeight(-1.0);
                        if (d10 - (double)n2 >= d11) {
                            d10 -= (double)n2;
                        } else {
                            arrayList.remove(content);
                            continue;
                        }
                    }
                    content.setArea(d10);
                    if ((n3 -= n2) == 0 && n != 0) {
                        n2 = n;
                        n3 = n;
                        n = 0;
                        continue;
                    }
                    if (n3 != 0) continue;
                }
                for (Content content : this.contentRegions) {
                    content.setResizableWithParentArea(content.getArea());
                    content.setAvailable(0.0);
                }
                this.resize = true;
            }
            this.previousSize = this.horizontal ? d7 : d6;
        }
        double d14 = this.totalMinSize();
        if (!(d14 > (this.horizontal ? d3 : d4))) {
        } else {
            double d15 = 0.0;
            n2 = 0;
            while (true) {
                if (n2 >= this.contentRegions.size()) {
                    this.setupContentAndDividerForLayout();
                    this.layoutDividersAndContent(d3, d4);
                    this.resize = false;
                    return;
                }
                Content content = (Content)this.contentRegions.get(n2);
                double d16 = this.horizontal ? content.minWidth(-1.0) : content.minHeight(-1.0);
                d15 = d16 / d14;
                if (this.horizontal) {
                    content.setArea(this.snapSpaceX(d15 * d3));
                } else {
                    content.setArea(this.snapSpaceY(d15 * d4));
                }
                content.setAvailable(0.0);
                ++n2;
            }
        }
        for (int i = 0; i < 10; ++i) {
            ContentDivider contentDivider = null;
            ContentDivider contentDivider2 = null;
            for (n = 0; n < this.contentRegions.size(); ++n) {
                double d17 = 0.0;
                if (n < this.contentDividers.size()) {
                    contentDivider2 = (ContentDivider)this.contentDividers.get(n);
                    if (contentDivider2.posExplicit) {
                        this.checkDividerPosition(contentDivider2, this.posToDividerPos(contentDivider2, contentDivider2.d.getPosition()), contentDivider2.getDividerPos());
                    }
                    if (n == 0) {
                        d17 = this.getAbsoluteDividerPos(contentDivider2);
                    } else {
                        double d18 = this.getAbsoluteDividerPos(contentDivider) + d5;
                        if (this.getAbsoluteDividerPos(contentDivider2) <= this.getAbsoluteDividerPos(contentDivider)) {
                            this.setAndCheckAbsoluteDividerPos(contentDivider2, d18);
                        }
                        d17 = this.getAbsoluteDividerPos(contentDivider2) - d18;
                    }
                } else if (n == this.contentDividers.size()) {
                    d17 = (this.horizontal ? d3 : d4) - (contentDivider != null ? this.getAbsoluteDividerPos(contentDivider) + d5 : 0.0);
                }
                if (!this.resize || contentDivider2.posExplicit) {
                    ((Content)this.contentRegions.get(n)).setArea(d17);
                }
                contentDivider = contentDivider2;
            }
            double d19 = 0.0;
            double d20 = 0.0;
            for (Content content : this.contentRegions) {
                double d21;
                if (content == null) continue;
                double d22 = this.horizontal ? content.maxWidth(-1.0) : content.maxHeight(-1.0);
                double d23 = d21 = this.horizontal ? content.minWidth(-1.0) : content.minHeight(-1.0);
                if (content.getArea() >= d22) {
                    d20 += content.getArea() - d22;
                    content.setArea(d22);
                }
                content.setAvailable(content.getArea() - d21);
                if (!(content.getAvailable() < 0.0)) continue;
                d19 += content.getAvailable();
            }
            d19 = Math.abs(d19);
            ArrayList arrayList = new ArrayList();
            ArrayList<Content> arrayList2 = new ArrayList<Content>();
            ArrayList<Content> arrayList3 = new ArrayList<Content>();
            double d24 = 0.0;
            for (Content content : this.contentRegions) {
                if (content.getAvailable() >= 0.0) {
                    d24 += content.getAvailable();
                    arrayList.add(content);
                }
                if (this.resize && !content.isResizableWithParent()) {
                    if (content.getArea() >= content.getResizableWithParentArea()) {
                        d20 += content.getArea() - content.getResizableWithParentArea();
                    } else {
                        d19 += content.getResizableWithParentArea() - content.getArea();
                    }
                    content.setAvailable(0.0);
                }
                if (this.resize) {
                    if (content.isResizableWithParent()) {
                        arrayList2.add(content);
                    }
                } else {
                    arrayList2.add(content);
                }
                if (!(content.getAvailable() < 0.0)) continue;
                arrayList3.add(content);
            }
            if (d20 > 0.0) {
                d20 = this.distributeTo(arrayList2, d20);
                d19 = 0.0;
                arrayList3.clear();
                d24 = 0.0;
                arrayList.clear();
                for (Content content : this.contentRegions) {
                    if (content.getAvailable() < 0.0) {
                        d19 += content.getAvailable();
                        arrayList3.add(content);
                        continue;
                    }
                    d24 += content.getAvailable();
                    arrayList.add(content);
                }
                d19 = Math.abs(d19);
            }
            if (d24 >= d19) {
                for (Content content : arrayList3) {
                    double d25 = this.horizontal ? content.minWidth(-1.0) : content.minHeight(-1.0);
                    content.setArea(d25);
                    content.setAvailable(0.0);
                }
                if (d19 > 0.0 && !arrayList3.isEmpty()) {
                    this.distributeFrom(d19, arrayList);
                }
                if (this.resize) {
                    double d26 = 0.0;
                    for (Content content : this.contentRegions) {
                        if (content.isResizableWithParent()) {
                            d26 += content.getArea();
                            continue;
                        }
                        d26 += content.getResizableWithParentArea();
                    }
                    d26 += d5 * (double)this.contentDividers.size();
                    double d27 = this.horizontal ? d3 : d4;
                    if (d26 < d27) {
                        this.distributeTo(arrayList2, d20 += (this.horizontal ? d3 : d4) - d26);
                    } else {
                        this.distributeFrom(d19 += d26 - (this.horizontal ? d3 : d4), arrayList2);
                    }
                }
            }
            this.setupContentAndDividerForLayout();
            boolean bl = true;
            for (Content content : this.contentRegions) {
                double d28;
                double d29 = this.horizontal ? content.maxWidth(-1.0) : content.maxHeight(-1.0);
                double d30 = d28 = this.horizontal ? content.minWidth(-1.0) : content.minHeight(-1.0);
                if (!(content.getArea() < d28) && !(content.getArea() > d29)) continue;
                bl = false;
                break;
            }
            if (bl) break;
        }
        this.layoutDividersAndContent(d3, d4);
        this.resize = false;
    }

    @Override
    protected double computeMinWidth(double d, double d2, double d3, double d4, double d5) {
        double d6 = 0.0;
        double d7 = 0.0;
        for (Content stackPane : this.contentRegions) {
            d6 += stackPane.minWidth(-1.0);
            d7 = Math.max(d7, stackPane.minWidth(-1.0));
        }
        for (ContentDivider contentDivider : this.contentDividers) {
            d6 += contentDivider.prefWidth(-1.0);
        }
        if (this.horizontal) {
            return d6 + d5 + d3;
        }
        return d7 + d5 + d3;
    }

    @Override
    protected double computeMinHeight(double d, double d2, double d3, double d4, double d5) {
        double d6 = 0.0;
        double d7 = 0.0;
        for (Content stackPane : this.contentRegions) {
            d6 += stackPane.minHeight(-1.0);
            d7 = Math.max(d7, stackPane.minHeight(-1.0));
        }
        for (ContentDivider contentDivider : this.contentDividers) {
            d6 += contentDivider.prefWidth(-1.0);
        }
        if (this.horizontal) {
            return d7 + d2 + d4;
        }
        return d6 + d2 + d4;
    }

    @Override
    protected double computePrefWidth(double d, double d2, double d3, double d4, double d5) {
        double d6 = 0.0;
        double d7 = 0.0;
        for (Content stackPane : this.contentRegions) {
            d6 += stackPane.prefWidth(-1.0);
            d7 = Math.max(d7, stackPane.prefWidth(-1.0));
        }
        for (ContentDivider contentDivider : this.contentDividers) {
            d6 += contentDivider.prefWidth(-1.0);
        }
        if (this.horizontal) {
            return d6 + d5 + d3;
        }
        return d7 + d5 + d3;
    }

    @Override
    protected double computePrefHeight(double d, double d2, double d3, double d4, double d5) {
        double d6 = 0.0;
        double d7 = 0.0;
        for (Content stackPane : this.contentRegions) {
            d6 += stackPane.prefHeight(-1.0);
            d7 = Math.max(d7, stackPane.prefHeight(-1.0));
        }
        for (ContentDivider contentDivider : this.contentDividers) {
            d6 += contentDivider.prefWidth(-1.0);
        }
        if (this.horizontal) {
            return d7 + d2 + d4;
        }
        return d6 + d2 + d4;
    }

    private void addContent(int n, Node node) {
        Content content = new Content(node);
        this.contentRegions.add(n, content);
        this.getChildren().add(n, content);
    }

    private void removeContent(Node node) {
        for (Content content : this.contentRegions) {
            if (!content.getContent().equals(node)) continue;
            content.dispose();
            this.getChildren().remove(content);
            this.contentRegions.remove(content);
            break;
        }
    }

    private void initializeContentListener() {
        ((SplitPane)this.getSkinnable()).getItems().addListener(change -> {
            while (change.next()) {
                int n;
                if (change.wasPermutated() || change.wasUpdated()) {
                    this.getChildren().clear();
                    this.contentRegions.clear();
                    n = 0;
                    for (Node node : change.getList()) {
                        this.addContent(n++, node);
                    }
                    continue;
                }
                for (Node node : change.getRemoved()) {
                    this.removeContent(node);
                }
                n = change.getFrom();
                for (Node node : change.getAddedSubList()) {
                    this.addContent(n++, node);
                }
            }
            this.removeAllDividers();
            for (SplitPane.Divider divider : ((SplitPane)this.getSkinnable()).getDividers()) {
                this.addDivider(divider);
            }
        });
    }

    private void checkDividerPosition(ContentDivider contentDivider, double d, double d2) {
        double d3;
        double d4;
        double d5;
        double d6 = contentDivider.prefWidth(-1.0);
        Content content = this.getLeft(contentDivider);
        Content content2 = this.getRight(contentDivider);
        double d7 = content == null ? 0.0 : (d5 = this.horizontal ? content.minWidth(-1.0) : content.minHeight(-1.0));
        double d8 = content2 == null ? 0.0 : (d4 = this.horizontal ? content2.minWidth(-1.0) : content2.minHeight(-1.0));
        double d9 = content == null ? 0.0 : (content.getContent() != null ? (this.horizontal ? content.getContent().maxWidth(-1.0) : content.getContent().maxHeight(-1.0)) : (d3 = 0.0));
        double d10 = content2 == null ? 0.0 : (content2.getContent() != null ? (this.horizontal ? content2.getContent().maxWidth(-1.0) : content2.getContent().maxHeight(-1.0)) : 0.0);
        double d11 = 0.0;
        double d12 = this.getSize();
        int n = this.contentDividers.indexOf(contentDivider);
        if (n - 1 >= 0 && (d11 = ((ContentDivider)this.contentDividers.get(n - 1)).getDividerPos()) == -1.0) {
            d11 = this.getAbsoluteDividerPos((ContentDivider)this.contentDividers.get(n - 1));
        }
        if (n + 1 < this.contentDividers.size() && (d12 = ((ContentDivider)this.contentDividers.get(n + 1)).getDividerPos()) == -1.0) {
            d12 = this.getAbsoluteDividerPos((ContentDivider)this.contentDividers.get(n + 1));
        }
        this.checkDividerPos = false;
        if (d > d2) {
            double d13;
            double d14 = d11 == 0.0 ? d3 : d11 + d6 + d3;
            double d15 = Math.min(d14, d13 = d12 - d4 - d6);
            if (d >= d15) {
                this.setAbsoluteDividerPos(contentDivider, d15);
            } else {
                double d16 = d12 - d10 - d6;
                if (d <= d16) {
                    this.setAbsoluteDividerPos(contentDivider, d16);
                } else {
                    this.setAbsoluteDividerPos(contentDivider, d);
                }
            }
        } else {
            double d17 = d12 - d10 - d6;
            double d18 = d11 == 0.0 ? d5 : d11 + d5 + d6;
            double d19 = Math.max(d17, d18);
            if (d <= d19) {
                this.setAbsoluteDividerPos(contentDivider, d19);
            } else {
                double d20 = d11 + d3 + d6;
                if (d >= d20) {
                    this.setAbsoluteDividerPos(contentDivider, d20);
                } else {
                    this.setAbsoluteDividerPos(contentDivider, d);
                }
            }
        }
        this.checkDividerPos = true;
    }

    private void addDivider(SplitPane.Divider divider) {
        ContentDivider contentDivider = new ContentDivider(divider);
        contentDivider.setInitialPos(divider.getPosition());
        contentDivider.setDividerPos(-1.0);
        PosPropertyListener posPropertyListener = new PosPropertyListener(contentDivider);
        contentDivider.setPosPropertyListener(posPropertyListener);
        divider.positionProperty().addListener(posPropertyListener);
        this.initializeDivderEventHandlers(contentDivider);
        this.contentDividers.add(contentDivider);
        this.getChildren().add(contentDivider);
    }

    private void removeAllDividers() {
        ListIterator listIterator = this.contentDividers.listIterator();
        while (listIterator.hasNext()) {
            ContentDivider contentDivider = (ContentDivider)listIterator.next();
            this.getChildren().remove(contentDivider);
            contentDivider.getDivider().positionProperty().removeListener(contentDivider.getPosPropertyListener());
            listIterator.remove();
        }
        this.lastDividerUpdate = 0;
    }

    private void initializeDivderEventHandlers(ContentDivider contentDivider) {
        contentDivider.addEventHandler(MouseEvent.ANY, mouseEvent -> mouseEvent.consume());
        contentDivider.setOnMousePressed(mouseEvent -> {
            if (this.horizontal) {
                contentDivider.setInitialPos(contentDivider.getDividerPos());
                contentDivider.setPressPos(mouseEvent.getSceneX());
                contentDivider.setPressPos(((SplitPane)this.getSkinnable()).getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT ? ((SplitPane)this.getSkinnable()).getWidth() - mouseEvent.getSceneX() : mouseEvent.getSceneX());
            } else {
                contentDivider.setInitialPos(contentDivider.getDividerPos());
                contentDivider.setPressPos(mouseEvent.getSceneY());
            }
            mouseEvent.consume();
        });
        contentDivider.setOnMouseDragged(mouseEvent -> {
            double d = 0.0;
            d = this.horizontal ? (((SplitPane)this.getSkinnable()).getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT ? ((SplitPane)this.getSkinnable()).getWidth() - mouseEvent.getSceneX() : mouseEvent.getSceneX()) : mouseEvent.getSceneY();
            this.setAndCheckAbsoluteDividerPos(contentDivider, Math.ceil(contentDivider.getInitialPos() + (d -= contentDivider.getPressPos())));
            mouseEvent.consume();
        });
    }

    private Content getLeft(ContentDivider contentDivider) {
        int n = this.contentDividers.indexOf(contentDivider);
        if (n != -1) {
            return (Content)this.contentRegions.get(n);
        }
        return null;
    }

    private Content getRight(ContentDivider contentDivider) {
        int n = this.contentDividers.indexOf(contentDivider);
        if (n != -1) {
            return (Content)this.contentRegions.get(n + 1);
        }
        return null;
    }

    private void setAbsoluteDividerPos(ContentDivider contentDivider, double d) {
        if (((SplitPane)this.getSkinnable()).getWidth() > 0.0 && ((SplitPane)this.getSkinnable()).getHeight() > 0.0 && contentDivider != null) {
            SplitPane.Divider divider = contentDivider.getDivider();
            contentDivider.setDividerPos(d);
            double d2 = this.getSize();
            if (d2 != 0.0) {
                double d3 = d + contentDivider.prefWidth(-1.0) / 2.0;
                divider.setPosition(d3 / d2);
            } else {
                divider.setPosition(0.0);
            }
        }
    }

    private double getAbsoluteDividerPos(ContentDivider contentDivider) {
        if (((SplitPane)this.getSkinnable()).getWidth() > 0.0 && ((SplitPane)this.getSkinnable()).getHeight() > 0.0 && contentDivider != null) {
            SplitPane.Divider divider = contentDivider.getDivider();
            double d = this.posToDividerPos(contentDivider, divider.getPosition());
            contentDivider.setDividerPos(d);
            return d;
        }
        return 0.0;
    }

    private double posToDividerPos(ContentDivider contentDivider, double d) {
        double d2 = this.getSize() * d;
        d2 = d == 1.0 ? (d2 -= contentDivider.prefWidth(-1.0)) : (d2 -= contentDivider.prefWidth(-1.0) / 2.0);
        return Math.round(d2);
    }

    private double totalMinSize() {
        double d = !this.contentDividers.isEmpty() ? (double)this.contentDividers.size() * ((ContentDivider)this.contentDividers.get(0)).prefWidth(-1.0) : 0.0;
        double d2 = 0.0;
        for (Content content : this.contentRegions) {
            if (this.horizontal) {
                d2 += content.minWidth(-1.0);
                continue;
            }
            d2 += content.minHeight(-1.0);
        }
        return d2 + d;
    }

    private double getSize() {
        SplitPane splitPane = (SplitPane)this.getSkinnable();
        double d = this.totalMinSize();
        if (this.horizontal) {
            if (splitPane.getWidth() > d) {
                d = splitPane.getWidth() - this.snappedLeftInset() - this.snappedRightInset();
            }
        } else if (splitPane.getHeight() > d) {
            d = splitPane.getHeight() - this.snappedTopInset() - this.snappedBottomInset();
        }
        return d;
    }

    private double distributeTo(List<Content> list, double d) {
        if (list.isEmpty()) {
            return d;
        }
        d = this.horizontal ? this.snapSizeX(d) : this.snapSizeY(d);
        int n = (int)d / list.size();
        while (d > 0.0 && !list.isEmpty()) {
            Iterator<Content> iterator = list.iterator();
            while (iterator.hasNext()) {
                double d2;
                Content content = iterator.next();
                double d3 = Math.min(this.horizontal ? content.maxWidth(-1.0) : content.maxHeight(-1.0), Double.MAX_VALUE);
                double d4 = d2 = this.horizontal ? content.minWidth(-1.0) : content.minHeight(-1.0);
                if (content.getArea() >= d3) {
                    content.setAvailable(content.getArea() - d2);
                    iterator.remove();
                    continue;
                }
                if ((double)n >= d3 - content.getArea()) {
                    d -= d3 - content.getArea();
                    content.setArea(d3);
                    content.setAvailable(d3 - d2);
                    iterator.remove();
                } else {
                    content.setArea(content.getArea() + (double)n);
                    content.setAvailable(content.getArea() - d2);
                    d -= (double)n;
                }
                if ((int)d != 0) continue;
                return d;
            }
            if (list.isEmpty()) {
                return d;
            }
            n = (int)d / list.size();
            int n2 = (int)d % list.size();
            if (n != 0 || n2 == 0) continue;
            n = n2;
            n2 = 0;
        }
        return d;
    }

    private double distributeFrom(double d, List<Content> list) {
        if (list.isEmpty()) {
            return d;
        }
        d = this.horizontal ? this.snapSizeX(d) : this.snapSizeY(d);
        int n = (int)d / list.size();
        while (d > 0.0 && !list.isEmpty()) {
            Iterator<Content> iterator = list.iterator();
            while (iterator.hasNext()) {
                Content content = iterator.next();
                if ((double)n >= content.getAvailable()) {
                    content.setArea(content.getArea() - content.getAvailable());
                    d -= content.getAvailable();
                    content.setAvailable(0.0);
                    iterator.remove();
                } else {
                    content.setArea(content.getArea() - (double)n);
                    content.setAvailable(content.getAvailable() - (double)n);
                    d -= (double)n;
                }
                if ((int)d != 0) continue;
                return d;
            }
            if (list.isEmpty()) {
                return d;
            }
            n = (int)d / list.size();
            int n2 = (int)d % list.size();
            if (n != 0 || n2 == 0) continue;
            n = n2;
            n2 = 0;
        }
        return d;
    }

    private void setupContentAndDividerForLayout() {
        double d = this.contentDividers.isEmpty() ? 0.0 : ((ContentDivider)this.contentDividers.get(0)).prefWidth(-1.0);
        double d2 = 0.0;
        double d3 = 0.0;
        for (Content stackPane : this.contentRegions) {
            if (this.resize && !stackPane.isResizableWithParent()) {
                stackPane.setArea(stackPane.getResizableWithParentArea());
            }
            stackPane.setX(d2);
            stackPane.setY(d3);
            if (this.horizontal) {
                d2 += stackPane.getArea() + d;
                continue;
            }
            d3 += stackPane.getArea() + d;
        }
        d2 = 0.0;
        d3 = 0.0;
        this.checkDividerPos = false;
        for (int i = 0; i < this.contentDividers.size(); ++i) {
            ContentDivider contentDivider = (ContentDivider)this.contentDividers.get(i);
            if (this.horizontal) {
                d2 += this.getLeft(contentDivider).getArea() + (i == 0 ? 0.0 : d);
            } else {
                d3 += this.getLeft(contentDivider).getArea() + (i == 0 ? 0.0 : d);
            }
            contentDivider.setX(d2);
            contentDivider.setY(d3);
            this.setAbsoluteDividerPos(contentDivider, this.horizontal ? contentDivider.getX() : contentDivider.getY());
            contentDivider.posExplicit = false;
        }
        this.checkDividerPos = true;
    }

    private void layoutDividersAndContent(double d, double d2) {
        double d3 = this.snappedLeftInset();
        double d4 = this.snappedTopInset();
        double d5 = this.contentDividers.isEmpty() ? 0.0 : ((ContentDivider)this.contentDividers.get(0)).prefWidth(-1.0);
        for (Content stackPane : this.contentRegions) {
            if (this.horizontal) {
                stackPane.setClipSize(stackPane.getArea(), d2);
                this.layoutInArea(stackPane, stackPane.getX() + d3, stackPane.getY() + d4, stackPane.getArea(), d2, 0.0, HPos.CENTER, VPos.CENTER);
                continue;
            }
            stackPane.setClipSize(d, stackPane.getArea());
            this.layoutInArea(stackPane, stackPane.getX() + d3, stackPane.getY() + d4, d, stackPane.getArea(), 0.0, HPos.CENTER, VPos.CENTER);
        }
        for (ContentDivider contentDivider : this.contentDividers) {
            if (this.horizontal) {
                contentDivider.resize(d5, d2);
                this.positionInArea(contentDivider, contentDivider.getX() + d3, contentDivider.getY() + d4, d5, d2, 0.0, HPos.CENTER, VPos.CENTER);
                continue;
            }
            contentDivider.resize(d, d5);
            this.positionInArea(contentDivider, contentDivider.getX() + d3, contentDivider.getY() + d4, d, d5, 0.0, HPos.CENTER, VPos.CENTER);
        }
    }

    private void setAndCheckAbsoluteDividerPos(ContentDivider contentDivider, double d) {
        double d2 = contentDivider.getDividerPos();
        this.setAbsoluteDividerPos(contentDivider, d);
        this.checkDividerPosition(contentDivider, d, d2);
    }

    static class Content
    extends StackPane {
        private Node content;
        private Rectangle clipRect = new Rectangle();
        private double x;
        private double y;
        private double area;
        private double resizableWithParentArea;
        private double available;

        public Content(Node node) {
            this.setClip(this.clipRect);
            this.content = node;
            if (node != null) {
                this.getChildren().add(node);
            }
            this.x = 0.0;
            this.y = 0.0;
        }

        public Node getContent() {
            return this.content;
        }

        public double getX() {
            return this.x;
        }

        public void setX(double d) {
            this.x = d;
        }

        public double getY() {
            return this.y;
        }

        public void setY(double d) {
            this.y = d;
        }

        public double getArea() {
            return this.area;
        }

        public void setArea(double d) {
            this.area = d;
        }

        public double getAvailable() {
            return this.available;
        }

        public void setAvailable(double d) {
            this.available = d;
        }

        public boolean isResizableWithParent() {
            return SplitPane.isResizableWithParent(this.content);
        }

        public double getResizableWithParentArea() {
            return this.resizableWithParentArea;
        }

        public void setResizableWithParentArea(double d) {
            this.resizableWithParentArea = !this.isResizableWithParent() ? d : 0.0;
        }

        protected void setClipSize(double d, double d2) {
            this.clipRect.setWidth(d);
            this.clipRect.setHeight(d2);
        }

        private void dispose() {
            this.getChildren().remove(this.content);
        }

        @Override
        protected double computeMaxWidth(double d) {
            return this.snapSizeX(this.content.maxWidth(d));
        }

        @Override
        protected double computeMaxHeight(double d) {
            return this.snapSizeY(this.content.maxHeight(d));
        }
    }

    class ContentDivider
    extends StackPane {
        private double initialPos;
        private double dividerPos;
        private double pressPos;
        private SplitPane.Divider d;
        private StackPane grabber;
        private double x;
        private double y;
        private boolean posExplicit;
        private ChangeListener<Number> listener;

        public ContentDivider(SplitPane.Divider divider) {
            this.getStyleClass().setAll((String[])new String[]{"split-pane-divider"});
            this.d = divider;
            this.initialPos = 0.0;
            this.dividerPos = 0.0;
            this.pressPos = 0.0;
            this.grabber = new StackPane(){

                @Override
                protected double computeMinWidth(double d) {
                    return 0.0;
                }

                @Override
                protected double computeMinHeight(double d) {
                    return 0.0;
                }

                @Override
                protected double computePrefWidth(double d) {
                    return this.snappedLeftInset() + this.snappedRightInset();
                }

                @Override
                protected double computePrefHeight(double d) {
                    return this.snappedTopInset() + this.snappedBottomInset();
                }

                @Override
                protected double computeMaxWidth(double d) {
                    return this.computePrefWidth(-1.0);
                }

                @Override
                protected double computeMaxHeight(double d) {
                    return this.computePrefHeight(-1.0);
                }
            };
            this.setGrabberStyle(SplitPaneSkin.this.horizontal);
            this.getChildren().add(this.grabber);
        }

        public SplitPane.Divider getDivider() {
            return this.d;
        }

        public final void setGrabberStyle(boolean bl) {
            this.grabber.getStyleClass().clear();
            this.grabber.getStyleClass().setAll((String[])new String[]{"vertical-grabber"});
            this.setCursor(Cursor.V_RESIZE);
            if (bl) {
                this.grabber.getStyleClass().setAll((String[])new String[]{"horizontal-grabber"});
                this.setCursor(Cursor.H_RESIZE);
            }
        }

        public double getInitialPos() {
            return this.initialPos;
        }

        public void setInitialPos(double d) {
            this.initialPos = d;
        }

        public double getDividerPos() {
            return this.dividerPos;
        }

        public void setDividerPos(double d) {
            this.dividerPos = d;
        }

        public double getPressPos() {
            return this.pressPos;
        }

        public void setPressPos(double d) {
            this.pressPos = d;
        }

        public double getX() {
            return this.x;
        }

        public void setX(double d) {
            this.x = d;
        }

        public double getY() {
            return this.y;
        }

        public void setY(double d) {
            this.y = d;
        }

        public ChangeListener<Number> getPosPropertyListener() {
            return this.listener;
        }

        public void setPosPropertyListener(ChangeListener<Number> changeListener) {
            this.listener = changeListener;
        }

        @Override
        protected double computeMinWidth(double d) {
            return this.computePrefWidth(d);
        }

        @Override
        protected double computeMinHeight(double d) {
            return this.computePrefHeight(d);
        }

        @Override
        protected double computePrefWidth(double d) {
            return this.snappedLeftInset() + this.snappedRightInset();
        }

        @Override
        protected double computePrefHeight(double d) {
            return this.snappedTopInset() + this.snappedBottomInset();
        }

        @Override
        protected double computeMaxWidth(double d) {
            return this.computePrefWidth(d);
        }

        @Override
        protected double computeMaxHeight(double d) {
            return this.computePrefHeight(d);
        }

        @Override
        protected void layoutChildren() {
            double d = this.grabber.prefWidth(-1.0);
            double d2 = this.grabber.prefHeight(-1.0);
            double d3 = (this.getWidth() - d) / 2.0;
            double d4 = (this.getHeight() - d2) / 2.0;
            this.grabber.resize(d, d2);
            this.positionInArea(this.grabber, d3, d4, d, d2, 0.0, HPos.CENTER, VPos.CENTER);
        }
    }

    class PosPropertyListener
    implements ChangeListener<Number> {
        ContentDivider divider;

        public PosPropertyListener(ContentDivider contentDivider) {
            this.divider = contentDivider;
        }

        @Override
        public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
            if (SplitPaneSkin.this.checkDividerPos) {
                this.divider.posExplicit = true;
            }
            ((SplitPane)SplitPaneSkin.this.getSkinnable()).requestLayout();
        }
    }
}

