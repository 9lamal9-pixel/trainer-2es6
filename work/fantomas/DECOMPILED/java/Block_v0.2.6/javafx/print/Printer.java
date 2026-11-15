/*
 * Decompiled with CFR 0.152.
 */
package javafx.print;

import com.sun.javafx.print.PrintHelper;
import com.sun.javafx.print.PrinterImpl;
import com.sun.javafx.print.Units;
import com.sun.javafx.tk.PrintPipeline;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableSet;
import javafx.geometry.Rectangle2D;
import javafx.print.JobSettings;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.PaperSource;
import javafx.print.PrintResolution;
import javafx.print.PrinterAttributes;

public final class Printer {
    private static ReadOnlyObjectWrapper<Printer> defaultPrinter;
    private PrinterImpl impl;
    private PrinterAttributes attributes;
    private PageLayout defPageLayout;

    public static ObservableSet<Printer> getAllPrinters() {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            securityManager.checkPrintJobAccess();
        }
        return PrintPipeline.getPrintPipeline().getAllPrinters();
    }

    private static ReadOnlyObjectWrapper<Printer> defaultPrinterImpl() {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            securityManager.checkPrintJobAccess();
        }
        if (defaultPrinter == null) {
            Printer printer = PrintPipeline.getPrintPipeline().getDefaultPrinter();
            defaultPrinter = new ReadOnlyObjectWrapper<Printer>(null, "defaultPrinter", printer);
        }
        return defaultPrinter;
    }

    public static ReadOnlyObjectProperty<Printer> defaultPrinterProperty() {
        return Printer.defaultPrinterImpl().getReadOnlyProperty();
    }

    public static Printer getDefaultPrinter() {
        return (Printer)Printer.defaultPrinterProperty().get();
    }

    Printer(PrinterImpl printerImpl) {
        this.impl = printerImpl;
        printerImpl.setPrinter(this);
    }

    PrinterImpl getPrinterImpl() {
        return this.impl;
    }

    public String getName() {
        return this.impl.getName();
    }

    public PrinterAttributes getPrinterAttributes() {
        if (this.attributes == null) {
            this.attributes = new PrinterAttributes(this.impl);
        }
        return this.attributes;
    }

    JobSettings getDefaultJobSettings() {
        return this.impl.getDefaultJobSettings();
    }

    public PageLayout getDefaultPageLayout() {
        if (this.defPageLayout == null) {
            PrinterAttributes printerAttributes = this.getPrinterAttributes();
            this.defPageLayout = this.createPageLayout(printerAttributes.getDefaultPaper(), printerAttributes.getDefaultPageOrientation(), MarginType.DEFAULT);
        }
        return this.defPageLayout;
    }

    public PageLayout createPageLayout(Paper paper, PageOrientation pageOrientation, MarginType marginType) {
        double d;
        double d2;
        double d3;
        if (paper == null || pageOrientation == null || marginType == null) {
            throw new NullPointerException("Parameters cannot be null");
        }
        Rectangle2D rectangle2D = this.impl.printableArea(paper);
        double d4 = paper.getWidth() / 72.0;
        double d5 = paper.getHeight() / 72.0;
        double d6 = rectangle2D.getMinX();
        double d7 = rectangle2D.getMinY();
        double d8 = d4 - rectangle2D.getMaxX();
        double d9 = d5 - rectangle2D.getMaxY();
        if (d6 < 0.01) {
            d6 = 0.0;
        }
        if (d8 < 0.01) {
            d8 = 0.0;
        }
        if (d7 < 0.01) {
            d7 = 0.0;
        }
        if (d9 < 0.01) {
            d9 = 0.0;
        }
        switch (marginType) {
            case DEFAULT: {
                d6 = d6 <= 0.75 ? 0.75 : d6;
                d8 = d8 <= 0.75 ? 0.75 : d8;
                d7 = d7 <= 0.75 ? 0.75 : d7;
                d9 = d9 <= 0.75 ? 0.75 : d9;
                break;
            }
            case EQUAL: {
                d3 = Math.max(d6, d8);
                d2 = Math.max(d7, d9);
                d7 = d9 = (d = Math.max(d3, d2));
                d8 = d9;
                d6 = d9;
                break;
            }
            case EQUAL_OPPOSITES: {
                d3 = Math.max(d6, d8);
                d2 = Math.max(d7, d9);
                d6 = d8 = d3;
                d7 = d9 = d2;
                break;
            }
        }
        while (d6 + d8 > d4) {
            d6 /= 2.0;
            d8 /= 2.0;
        }
        while (d7 + d9 > d5) {
            d7 /= 2.0;
            d9 /= 2.0;
        }
        switch (pageOrientation) {
            case LANDSCAPE: {
                d3 = d9;
                d2 = d7;
                d = d6;
                double d10 = d8;
                break;
            }
            case REVERSE_LANDSCAPE: {
                d3 = d7;
                d2 = d9;
                d = d8;
                double d10 = d6;
                break;
            }
            case REVERSE_PORTRAIT: {
                d3 = d8;
                d2 = d6;
                d = d9;
                double d10 = d7;
                break;
            }
            default: {
                d3 = d6;
                d2 = d8;
                d = d7;
                double d10 = d9;
            }
        }
        return new PageLayout(paper, pageOrientation, d3 *= 72.0, d2 *= 72.0, d *= 72.0, d10 *= 72.0);
    }

    public PageLayout createPageLayout(Paper paper, PageOrientation pageOrientation, double d, double d2, double d3, double d4) {
        double d5;
        double d6;
        double d7;
        double d8;
        if (paper == null || pageOrientation == null) {
            throw new NullPointerException("Parameters cannot be null");
        }
        if (d < 0.0 || d2 < 0.0 || d3 < 0.0 || d4 < 0.0) {
            throw new IllegalArgumentException("Margins must be >= 0");
        }
        Rectangle2D rectangle2D = this.impl.printableArea(paper);
        double d9 = paper.getWidth() / 72.0;
        double d10 = paper.getHeight() / 72.0;
        double d11 = rectangle2D.getMinX();
        double d12 = rectangle2D.getMinY();
        double d13 = d9 - rectangle2D.getMaxX();
        double d14 = d10 - rectangle2D.getMaxY();
        d /= 72.0;
        d2 /= 72.0;
        d3 /= 72.0;
        d4 /= 72.0;
        boolean bl = false;
        if (pageOrientation == PageOrientation.PORTRAIT || pageOrientation == PageOrientation.REVERSE_PORTRAIT) {
            if (d + d2 > d9 || d3 + d4 > d10) {
                bl = true;
            }
        } else if (d + d2 > d10 || d3 + d4 > d9) {
            bl = true;
        }
        if (bl) {
            return this.createPageLayout(paper, pageOrientation, MarginType.DEFAULT);
        }
        switch (pageOrientation) {
            case LANDSCAPE: {
                d8 = d14;
                d7 = d12;
                d6 = d11;
                d5 = d13;
                break;
            }
            case REVERSE_LANDSCAPE: {
                d8 = d12;
                d7 = d14;
                d6 = d13;
                d5 = d11;
                break;
            }
            case REVERSE_PORTRAIT: {
                d8 = d13;
                d7 = d11;
                d6 = d14;
                d5 = d12;
                break;
            }
            default: {
                d8 = d11;
                d7 = d13;
                d6 = d12;
                d5 = d14;
            }
        }
        d8 = d >= d8 ? d : d8;
        d7 = d2 >= d7 ? d2 : d7;
        d6 = d3 >= d6 ? d3 : d6;
        d5 = d4 >= d5 ? d4 : d5;
        return new PageLayout(paper, pageOrientation, d8 *= 72.0, d7 *= 72.0, d6 *= 72.0, d5 *= 72.0);
    }

    public String toString() {
        return "Printer " + this.getName();
    }

    static {
        PrintHelper.setPrintAccessor(new PrintHelper.PrintAccessor(){

            @Override
            public PrintResolution createPrintResolution(int n, int n2) {
                return new PrintResolution(n, n2);
            }

            @Override
            public Paper createPaper(String string, double d, double d2, Units units) {
                return new Paper(string, d, d2, units);
            }

            @Override
            public PaperSource createPaperSource(String string) {
                return new PaperSource(string);
            }

            @Override
            public JobSettings createJobSettings(Printer printer) {
                return new JobSettings(printer);
            }

            @Override
            public Printer createPrinter(PrinterImpl printerImpl) {
                return new Printer(printerImpl);
            }

            @Override
            public PrinterImpl getPrinterImpl(Printer printer) {
                return printer.getPrinterImpl();
            }
        });
    }

    public static enum MarginType {
        DEFAULT,
        HARDWARE_MINIMUM,
        EQUAL,
        EQUAL_OPPOSITES;

    }
}

