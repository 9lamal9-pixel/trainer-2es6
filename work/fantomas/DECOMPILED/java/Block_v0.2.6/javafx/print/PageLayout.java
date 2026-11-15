/*
 * Decompiled with CFR 0.152.
 */
package javafx.print;

import javafx.print.PageOrientation;
import javafx.print.Paper;

public final class PageLayout {
    private PageOrientation orient;
    private Paper paper;
    private double lMargin;
    private double rMargin;
    private double tMargin;
    private double bMargin;

    PageLayout(Paper paper, PageOrientation pageOrientation) {
        this(paper, pageOrientation, 56.0, 56.0, 56.0, 56.0);
    }

    PageLayout(Paper paper, PageOrientation pageOrientation, double d, double d2, double d3, double d4) {
        if (paper == null || pageOrientation == null || d < 0.0 || d2 < 0.0 || d3 < 0.0 || d4 < 0.0) {
            throw new IllegalArgumentException("Illegal parameters");
        }
        if (pageOrientation == PageOrientation.PORTRAIT || pageOrientation == PageOrientation.REVERSE_PORTRAIT ? d + d2 > paper.getWidth() || d3 + d4 > paper.getHeight() : d + d2 > paper.getHeight() || d3 + d4 > paper.getWidth()) {
            throw new IllegalArgumentException("Bad margins");
        }
        this.paper = paper;
        this.orient = pageOrientation;
        this.lMargin = d;
        this.rMargin = d2;
        this.tMargin = d3;
        this.bMargin = d4;
    }

    public PageOrientation getPageOrientation() {
        return this.orient;
    }

    public Paper getPaper() {
        return this.paper;
    }

    public double getPrintableWidth() {
        double d = 0.0;
        d = this.orient == PageOrientation.PORTRAIT || this.orient == PageOrientation.REVERSE_PORTRAIT ? this.paper.getWidth() : this.paper.getHeight();
        if ((d -= this.lMargin + this.rMargin) < 0.0) {
            d = 0.0;
        }
        return d;
    }

    public double getPrintableHeight() {
        double d = 0.0;
        d = this.orient == PageOrientation.PORTRAIT || this.orient == PageOrientation.REVERSE_PORTRAIT ? this.paper.getHeight() : this.paper.getWidth();
        if ((d -= this.tMargin + this.bMargin) < 0.0) {
            d = 0.0;
        }
        return d;
    }

    public double getLeftMargin() {
        return this.lMargin;
    }

    public double getRightMargin() {
        return this.rMargin;
    }

    public double getTopMargin() {
        return this.tMargin;
    }

    public double getBottomMargin() {
        return this.bMargin;
    }

    public boolean equals(Object object) {
        if (object instanceof PageLayout) {
            PageLayout pageLayout = (PageLayout)object;
            return this.paper.equals(pageLayout.paper) && this.orient.equals((Object)pageLayout.orient) && this.tMargin == pageLayout.tMargin && this.bMargin == pageLayout.bMargin && this.rMargin == pageLayout.rMargin && this.lMargin == pageLayout.lMargin;
        }
        return false;
    }

    public int hashCode() {
        return this.paper.hashCode() + this.orient.hashCode() + (int)(this.tMargin + this.bMargin + this.lMargin + this.rMargin);
    }

    public String toString() {
        return "Paper=" + this.paper + " Orient=" + this.orient + " leftMargin=" + this.lMargin + " rightMargin=" + this.rMargin + " topMargin=" + this.tMargin + " bottomMargin=" + this.bMargin;
    }
}

