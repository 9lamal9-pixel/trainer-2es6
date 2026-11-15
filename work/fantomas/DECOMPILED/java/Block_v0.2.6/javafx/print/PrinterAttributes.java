/*
 * Decompiled with CFR 0.152.
 */
package javafx.print;

import com.sun.javafx.print.PrinterImpl;
import java.util.Set;
import javafx.print.Collation;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.PaperSource;
import javafx.print.PrintColor;
import javafx.print.PrintQuality;
import javafx.print.PrintResolution;
import javafx.print.PrintSides;

public final class PrinterAttributes {
    private PrinterImpl impl;

    PrinterAttributes(PrinterImpl printerImpl) {
        this.impl = printerImpl;
    }

    public int getDefaultCopies() {
        return this.impl.defaultCopies();
    }

    public int getMaxCopies() {
        return this.impl.maxCopies();
    }

    public boolean supportsPageRanges() {
        return this.impl.supportsPageRanges();
    }

    public Collation getDefaultCollation() {
        return this.impl.defaultCollation();
    }

    public Set<Collation> getSupportedCollations() {
        return this.impl.supportedCollations();
    }

    public PrintSides getDefaultPrintSides() {
        return this.impl.defaultSides();
    }

    public Set<PrintSides> getSupportedPrintSides() {
        return this.impl.supportedSides();
    }

    public PrintColor getDefaultPrintColor() {
        return this.impl.defaultPrintColor();
    }

    public Set<PrintColor> getSupportedPrintColors() {
        return this.impl.supportedPrintColor();
    }

    public PrintQuality getDefaultPrintQuality() {
        return this.impl.defaultPrintQuality();
    }

    public Set<PrintQuality> getSupportedPrintQuality() {
        return this.impl.supportedPrintQuality();
    }

    public PrintResolution getDefaultPrintResolution() {
        return this.impl.defaultPrintResolution();
    }

    public Set<PrintResolution> getSupportedPrintResolutions() {
        return this.impl.supportedPrintResolution();
    }

    public PageOrientation getDefaultPageOrientation() {
        return this.impl.defaultOrientation();
    }

    public Set<PageOrientation> getSupportedPageOrientations() {
        return this.impl.supportedOrientation();
    }

    public Paper getDefaultPaper() {
        return this.impl.defaultPaper();
    }

    public Set<Paper> getSupportedPapers() {
        return this.impl.supportedPapers();
    }

    public PaperSource getDefaultPaperSource() {
        return this.impl.defaultPaperSource();
    }

    public Set<PaperSource> getSupportedPaperSources() {
        return this.impl.supportedPaperSources();
    }
}

