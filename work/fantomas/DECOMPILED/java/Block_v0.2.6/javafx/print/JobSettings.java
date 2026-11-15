/*
 * Decompiled with CFR 0.152.
 */
package javafx.print;

import java.util.Set;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.print.Collation;
import javafx.print.PageLayout;
import javafx.print.PageRange;
import javafx.print.PaperSource;
import javafx.print.PrintColor;
import javafx.print.PrintQuality;
import javafx.print.PrintResolution;
import javafx.print.PrintSides;
import javafx.print.Printer;
import javafx.print.PrinterAttributes;
import javafx.print.PrinterJob;

public final class JobSettings {
    private PrinterJob job;
    private Printer printer;
    private PrinterAttributes printerCaps;
    private boolean defaultCopies = true;
    private boolean hasOldCopies = false;
    private int oldCopies;
    private boolean defaultSides = true;
    private boolean hasOldSides = false;
    private PrintSides oldSides;
    private boolean defaultCollation = true;
    private boolean hasOldCollation = false;
    private Collation oldCollation;
    private boolean defaultPrintColor = true;
    private boolean hasOldPrintColor = false;
    private PrintColor oldPrintColor;
    private boolean defaultPrintQuality = true;
    private boolean hasOldPrintQuality = false;
    private PrintQuality oldPrintQuality;
    private boolean defaultPrintResolution = true;
    private boolean hasOldPrintResolution = false;
    private PrintResolution oldPrintResolution;
    private boolean defaultPaperSource = true;
    private boolean hasOldPaperSource = false;
    private PaperSource oldPaperSource;
    private boolean defaultPageLayout = true;
    private boolean hasOldPageLayout = false;
    private PageLayout oldPageLayout;
    private static final String DEFAULT_JOBNAME = "JavaFX Print Job";
    private SimpleStringProperty jobName;
    private IntegerProperty copies;
    private ObjectProperty<PageRange[]> pageRanges = null;
    private ObjectProperty<PrintSides> sides = null;
    private ObjectProperty<Collation> collation = null;
    private ObjectProperty<PrintColor> color = null;
    private ObjectProperty<PrintQuality> quality = null;
    private ObjectProperty<PrintResolution> resolution = null;
    private ObjectProperty<PaperSource> paperSource = null;
    private ObjectProperty<PageLayout> layout = null;

    JobSettings(Printer printer) {
        this.printer = printer;
        this.printerCaps = printer.getPrinterAttributes();
    }

    void setPrinterJob(PrinterJob printerJob) {
        this.job = printerJob;
    }

    private boolean isJobNew() {
        return this.job == null || this.job.isJobNew();
    }

    void updateForPrinter(Printer printer) {
        this.printer = printer;
        this.printerCaps = printer.getPrinterAttributes();
        if (this.defaultCopies) {
            if (this.getCopies() != this.printerCaps.getDefaultCopies()) {
                this.setCopies(this.printerCaps.getDefaultCopies());
                this.defaultCopies = true;
            }
        } else {
            int n = this.getCopies();
            if (this.hasOldCopies && this.oldCopies > n) {
                n = this.oldCopies;
            }
            int n2 = this.printerCaps.getMaxCopies();
            if (!this.hasOldCopies && this.getCopies() > n2) {
                this.hasOldCopies = true;
                this.oldCopies = this.getCopies();
            }
            if (n > n2) {
                n = n2;
            }
            this.setCopies(n);
        }
        PrintSides printSides = this.getPrintSides();
        PrintSides printSides2 = this.printerCaps.getDefaultPrintSides();
        Set<PrintSides> set = this.printerCaps.getSupportedPrintSides();
        if (this.defaultSides) {
            if (printSides != printSides2) {
                this.setPrintSides(printSides2);
                this.defaultSides = true;
            }
        } else if (this.hasOldSides) {
            if (set.contains((Object)this.oldSides)) {
                this.setPrintSides(this.oldSides);
                this.hasOldSides = false;
            } else {
                this.setPrintSides(printSides2);
            }
        } else if (!set.contains((Object)printSides)) {
            this.hasOldSides = true;
            this.oldSides = printSides;
            this.setPrintSides(printSides2);
        }
        Collation collation = this.getCollation();
        Collation collation2 = this.printerCaps.getDefaultCollation();
        Set<Collation> set2 = this.printerCaps.getSupportedCollations();
        if (this.defaultCollation) {
            if (collation != collation2) {
                this.setCollation(collation2);
                this.defaultCollation = true;
            }
        } else if (this.hasOldCollation) {
            if (set2.contains((Object)this.oldCollation)) {
                this.setCollation(this.oldCollation);
                this.hasOldCollation = false;
            } else {
                this.setCollation(collation2);
            }
        } else if (!set2.contains((Object)collation)) {
            this.hasOldCollation = true;
            this.oldCollation = collation;
            this.setCollation(collation2);
        }
        PrintColor printColor = this.getPrintColor();
        PrintColor printColor2 = this.printerCaps.getDefaultPrintColor();
        Set<PrintColor> set3 = this.printerCaps.getSupportedPrintColors();
        if (this.defaultPrintColor) {
            if (printColor != printColor2) {
                this.setPrintColor(printColor2);
                this.defaultPrintColor = true;
            }
        } else if (this.hasOldPrintColor) {
            if (set3.contains((Object)this.oldPrintColor)) {
                this.setPrintColor(this.oldPrintColor);
                this.hasOldPrintColor = false;
            } else {
                this.setPrintColor(printColor2);
            }
        } else if (!set3.contains((Object)printColor)) {
            this.hasOldPrintColor = true;
            this.oldPrintColor = printColor;
            this.setPrintColor(printColor2);
        }
        PrintQuality printQuality = this.getPrintQuality();
        PrintQuality printQuality2 = this.printerCaps.getDefaultPrintQuality();
        Set<PrintQuality> set4 = this.printerCaps.getSupportedPrintQuality();
        if (this.defaultPrintQuality) {
            if (printQuality != printQuality2) {
                this.setPrintQuality(printQuality2);
                this.defaultPrintQuality = true;
            }
        } else if (this.hasOldPrintQuality) {
            if (set4.contains((Object)this.oldPrintQuality)) {
                this.setPrintQuality(this.oldPrintQuality);
                this.hasOldPrintQuality = false;
            } else {
                this.setPrintQuality(printQuality2);
            }
        } else if (!set4.contains((Object)printQuality)) {
            this.hasOldPrintQuality = true;
            this.oldPrintQuality = printQuality;
            this.setPrintQuality(printQuality2);
        }
        PrintResolution printResolution = this.getPrintResolution();
        PrintResolution printResolution2 = this.printerCaps.getDefaultPrintResolution();
        Set<PrintResolution> set5 = this.printerCaps.getSupportedPrintResolutions();
        if (this.defaultPrintResolution) {
            if (printResolution != printResolution2) {
                this.setPrintResolution(printResolution2);
                this.defaultPrintResolution = true;
            }
        } else if (this.hasOldPrintResolution) {
            if (set5.contains(this.oldPrintResolution)) {
                this.setPrintResolution(this.oldPrintResolution);
                this.hasOldPrintResolution = false;
            } else {
                this.setPrintResolution(printResolution2);
            }
        } else if (!set5.contains(printResolution)) {
            this.hasOldPrintResolution = true;
            this.oldPrintResolution = printResolution;
            this.setPrintResolution(printResolution2);
        }
        PaperSource paperSource = this.getPaperSource();
        PaperSource paperSource2 = this.printerCaps.getDefaultPaperSource();
        Set<PaperSource> set6 = this.printerCaps.getSupportedPaperSources();
        if (this.defaultPaperSource) {
            if (paperSource != paperSource2) {
                this.setPaperSource(paperSource2);
                this.defaultPaperSource = true;
            }
        } else if (this.hasOldPaperSource) {
            if (set6.contains(this.oldPaperSource)) {
                this.setPaperSource(this.oldPaperSource);
                this.hasOldPaperSource = false;
            } else {
                this.setPaperSource(paperSource2);
            }
        } else if (!set6.contains(paperSource)) {
            this.hasOldPaperSource = true;
            this.oldPaperSource = paperSource;
            this.setPaperSource(paperSource2);
        }
        PageLayout pageLayout = this.getPageLayout();
        PageLayout pageLayout2 = printer.getDefaultPageLayout();
        if (this.defaultPageLayout) {
            if (!pageLayout.equals(pageLayout2)) {
                this.setPageLayout(pageLayout2);
                this.defaultPageLayout = true;
            }
        } else if (this.hasOldPageLayout) {
            PageLayout pageLayout3 = this.job.validatePageLayout(this.oldPageLayout);
            if (pageLayout3.equals(this.oldPageLayout)) {
                this.setPageLayout(this.oldPageLayout);
                this.hasOldPageLayout = false;
            } else {
                this.setPageLayout(pageLayout2);
            }
        } else {
            PageLayout pageLayout4 = this.job.validatePageLayout(pageLayout);
            if (!pageLayout4.equals(pageLayout)) {
                this.hasOldPageLayout = true;
                this.oldPageLayout = pageLayout;
                this.setPageLayout(pageLayout2);
            }
        }
    }

    public final StringProperty jobNameProperty() {
        if (this.jobName == null) {
            this.jobName = new SimpleStringProperty(this, "jobName", DEFAULT_JOBNAME){

                @Override
                public void set(String string) {
                    if (!JobSettings.this.isJobNew()) {
                        return;
                    }
                    if (string == null) {
                        string = JobSettings.DEFAULT_JOBNAME;
                    }
                    super.set(string);
                }

                @Override
                public void bind(ObservableValue<? extends String> observableValue) {
                    throw new RuntimeException("Jobname property cannot be bound");
                }

                @Override
                public void bindBidirectional(Property<String> property) {
                    throw new RuntimeException("Jobname property cannot be bound");
                }
            };
        }
        return this.jobName;
    }

    public String getJobName() {
        return (String)this.jobNameProperty().get();
    }

    public void setJobName(String string) {
        this.jobNameProperty().set(string);
    }

    public final IntegerProperty copiesProperty() {
        if (this.copies == null) {
            this.copies = new SimpleIntegerProperty(this, "copies", this.printerCaps.getDefaultCopies()){

                @Override
                public void set(int n) {
                    if (!JobSettings.this.isJobNew()) {
                        return;
                    }
                    if (n <= 0) {
                        if (JobSettings.this.defaultCopies) {
                            return;
                        }
                        super.set(JobSettings.this.printerCaps.getDefaultCopies());
                        JobSettings.this.defaultCopies = true;
                        return;
                    }
                    super.set(n);
                    JobSettings.this.defaultCopies = false;
                }

                @Override
                public void bind(ObservableValue<? extends Number> observableValue) {
                    throw new RuntimeException("Copies property cannot be bound");
                }

                @Override
                public void bindBidirectional(Property<Number> property) {
                    throw new RuntimeException("Copies property cannot be bound");
                }
            };
        }
        return this.copies;
    }

    public int getCopies() {
        return this.copiesProperty().get();
    }

    public final void setCopies(int n) {
        this.copiesProperty().set(n);
    }

    public final ObjectProperty pageRangesProperty() {
        if (this.pageRanges == null) {
            this.pageRanges = new SimpleObjectProperty(this, "pageRanges", null){

                @Override
                public void set(Object object) {
                    try {
                        this.set((PageRange[])object);
                    }
                    catch (ClassCastException classCastException) {
                        return;
                    }
                }

                @Override
                public void set(PageRange[] pageRangeArray) {
                    if (!JobSettings.this.isJobNew()) {
                        return;
                    }
                    if (pageRangeArray == null || pageRangeArray.length == 0 || pageRangeArray[0] == null) {
                        pageRangeArray = null;
                    } else {
                        int n = pageRangeArray.length;
                        PageRange[] pageRangeArray2 = new PageRange[n];
                        int n2 = 0;
                        for (int i = 0; i < n; ++i) {
                            PageRange pageRange = pageRangeArray[i];
                            if (pageRange == null || n2 >= pageRange.getStartPage()) {
                                return;
                            }
                            n2 = pageRange.getEndPage();
                            pageRangeArray2[i] = pageRange;
                        }
                        pageRangeArray = pageRangeArray2;
                    }
                    super.set(pageRangeArray);
                }

                @Override
                public void bind(ObservableValue observableValue) {
                    throw new RuntimeException("PageRanges property cannot be bound");
                }

                @Override
                public void bindBidirectional(Property property) {
                    throw new RuntimeException("PageRanges property cannot be bound");
                }
            };
        }
        return this.pageRanges;
    }

    public PageRange[] getPageRanges() {
        return (PageRange[])this.pageRangesProperty().get();
    }

    public void setPageRanges(PageRange ... pageRangeArray) {
        this.pageRangesProperty().set(pageRangeArray);
    }

    public final ObjectProperty<PrintSides> printSidesProperty() {
        if (this.sides == null) {
            this.sides = new SimpleObjectProperty<PrintSides>((Object)this, "printSides", this.printerCaps.getDefaultPrintSides()){

                @Override
                public void set(PrintSides printSides) {
                    if (!JobSettings.this.isJobNew()) {
                        return;
                    }
                    if (printSides == null) {
                        if (JobSettings.this.defaultSides) {
                            return;
                        }
                        super.set(JobSettings.this.printerCaps.getDefaultPrintSides());
                        JobSettings.this.defaultSides = true;
                    }
                    if (JobSettings.this.printerCaps.getSupportedPrintSides().contains((Object)printSides)) {
                        super.set(printSides);
                        JobSettings.this.defaultSides = false;
                    }
                }

                @Override
                public void bind(ObservableValue<? extends PrintSides> observableValue) {
                    throw new RuntimeException("PrintSides property cannot be bound");
                }

                @Override
                public void bindBidirectional(Property<PrintSides> property) {
                    throw new RuntimeException("PrintSides property cannot be bound");
                }
            };
        }
        return this.sides;
    }

    public PrintSides getPrintSides() {
        return (PrintSides)((Object)this.printSidesProperty().get());
    }

    public void setPrintSides(PrintSides printSides) {
        if (printSides == this.getPrintSides()) {
            return;
        }
        this.printSidesProperty().set(printSides);
    }

    public final ObjectProperty<Collation> collationProperty() {
        if (this.collation == null) {
            Collation collation = this.printerCaps.getDefaultCollation();
            this.collation = new SimpleObjectProperty<Collation>((Object)this, "collation", collation){

                @Override
                public void set(Collation collation) {
                    if (!JobSettings.this.isJobNew()) {
                        return;
                    }
                    if (collation == null) {
                        if (JobSettings.this.defaultCollation) {
                            return;
                        }
                        super.set(JobSettings.this.printerCaps.getDefaultCollation());
                        JobSettings.this.defaultCollation = true;
                        return;
                    }
                    if (JobSettings.this.printerCaps.getSupportedCollations().contains((Object)collation)) {
                        super.set(collation);
                        JobSettings.this.defaultCollation = false;
                    }
                }

                @Override
                public void bind(ObservableValue<? extends Collation> observableValue) {
                    throw new RuntimeException("Collation property cannot be bound");
                }

                @Override
                public void bindBidirectional(Property<Collation> property) {
                    throw new RuntimeException("Collation property cannot be bound");
                }
            };
        }
        return this.collation;
    }

    public Collation getCollation() {
        return (Collation)((Object)this.collationProperty().get());
    }

    public void setCollation(Collation collation) {
        if (collation == this.getCollation()) {
            return;
        }
        this.collationProperty().set(collation);
    }

    public final ObjectProperty<PrintColor> printColorProperty() {
        if (this.color == null) {
            this.color = new SimpleObjectProperty<PrintColor>((Object)this, "printColor", this.printerCaps.getDefaultPrintColor()){

                @Override
                public void set(PrintColor printColor) {
                    if (!JobSettings.this.isJobNew()) {
                        return;
                    }
                    if (printColor == null) {
                        if (JobSettings.this.defaultPrintColor) {
                            return;
                        }
                        super.set(JobSettings.this.printerCaps.getDefaultPrintColor());
                        JobSettings.this.defaultPrintColor = true;
                    }
                    if (JobSettings.this.printerCaps.getSupportedPrintColors().contains((Object)printColor)) {
                        super.set(printColor);
                        JobSettings.this.defaultPrintColor = false;
                    }
                }

                @Override
                public void bind(ObservableValue<? extends PrintColor> observableValue) {
                    throw new RuntimeException("PrintColor property cannot be bound");
                }

                @Override
                public void bindBidirectional(Property<PrintColor> property) {
                    throw new RuntimeException("PrintColor property cannot be bound");
                }
            };
        }
        return this.color;
    }

    public PrintColor getPrintColor() {
        return (PrintColor)((Object)this.printColorProperty().get());
    }

    public void setPrintColor(PrintColor printColor) {
        if (printColor == this.getPrintColor()) {
            return;
        }
        this.printColorProperty().set(printColor);
    }

    public final ObjectProperty<PrintQuality> printQualityProperty() {
        if (this.quality == null) {
            this.quality = new SimpleObjectProperty<PrintQuality>((Object)this, "printQuality", this.printerCaps.getDefaultPrintQuality()){

                @Override
                public void set(PrintQuality printQuality) {
                    if (!JobSettings.this.isJobNew()) {
                        return;
                    }
                    if (printQuality == null) {
                        if (JobSettings.this.defaultPrintQuality) {
                            return;
                        }
                        super.set(JobSettings.this.printerCaps.getDefaultPrintQuality());
                        JobSettings.this.defaultPrintQuality = true;
                    }
                    if (JobSettings.this.printerCaps.getSupportedPrintQuality().contains((Object)printQuality)) {
                        super.set(printQuality);
                        JobSettings.this.defaultPrintQuality = false;
                    }
                }

                @Override
                public void bind(ObservableValue<? extends PrintQuality> observableValue) {
                    throw new RuntimeException("PrintQuality property cannot be bound");
                }

                @Override
                public void bindBidirectional(Property<PrintQuality> property) {
                    throw new RuntimeException("PrintQuality property cannot be bound");
                }
            };
        }
        return this.quality;
    }

    public PrintQuality getPrintQuality() {
        return (PrintQuality)((Object)this.printQualityProperty().get());
    }

    public void setPrintQuality(PrintQuality printQuality) {
        if (printQuality == this.getPrintQuality()) {
            return;
        }
        this.printQualityProperty().set(printQuality);
    }

    public final ObjectProperty<PrintResolution> printResolutionProperty() {
        if (this.resolution == null) {
            this.resolution = new SimpleObjectProperty<PrintResolution>((Object)this, "printResolution", this.printerCaps.getDefaultPrintResolution()){

                @Override
                public void set(PrintResolution printResolution) {
                    if (!JobSettings.this.isJobNew()) {
                        return;
                    }
                    if (printResolution == null) {
                        if (JobSettings.this.defaultPrintResolution) {
                            return;
                        }
                        super.set(JobSettings.this.printerCaps.getDefaultPrintResolution());
                        JobSettings.this.defaultPrintResolution = true;
                    }
                    if (JobSettings.this.printerCaps.getSupportedPrintResolutions().contains(printResolution)) {
                        super.set(printResolution);
                        JobSettings.this.defaultPrintResolution = false;
                    }
                }

                @Override
                public void bind(ObservableValue<? extends PrintResolution> observableValue) {
                    throw new RuntimeException("PrintResolution property cannot be bound");
                }

                @Override
                public void bindBidirectional(Property<PrintResolution> property) {
                    throw new RuntimeException("PrintResolution property cannot be bound");
                }
            };
        }
        return this.resolution;
    }

    public PrintResolution getPrintResolution() {
        return (PrintResolution)this.printResolutionProperty().get();
    }

    public void setPrintResolution(PrintResolution printResolution) {
        if (printResolution == null || printResolution == this.getPrintResolution()) {
            return;
        }
        this.printResolutionProperty().set(printResolution);
    }

    public final ObjectProperty<PaperSource> paperSourceProperty() {
        if (this.paperSource == null) {
            this.paperSource = new SimpleObjectProperty<PaperSource>((Object)this, "paperSource", this.printerCaps.getDefaultPaperSource()){

                @Override
                public void set(PaperSource paperSource) {
                    if (!JobSettings.this.isJobNew()) {
                        return;
                    }
                    if (paperSource == null) {
                        if (JobSettings.this.defaultPaperSource) {
                            return;
                        }
                        super.set(JobSettings.this.printerCaps.getDefaultPaperSource());
                        JobSettings.this.defaultPaperSource = true;
                    }
                    if (JobSettings.this.printerCaps.getSupportedPaperSources().contains(paperSource)) {
                        super.set(paperSource);
                        JobSettings.this.defaultPaperSource = false;
                    }
                }

                @Override
                public void bind(ObservableValue<? extends PaperSource> observableValue) {
                    throw new RuntimeException("PaperSource property cannot be bound");
                }

                @Override
                public void bindBidirectional(Property<PaperSource> property) {
                    throw new RuntimeException("PaperSource property cannot be bound");
                }
            };
        }
        return this.paperSource;
    }

    public PaperSource getPaperSource() {
        return (PaperSource)this.paperSourceProperty().get();
    }

    public void setPaperSource(PaperSource paperSource) {
        this.paperSourceProperty().set(paperSource);
    }

    public final ObjectProperty<PageLayout> pageLayoutProperty() {
        if (this.layout == null) {
            this.layout = new SimpleObjectProperty<PageLayout>((Object)this, "pageLayout", this.printer.getDefaultPageLayout()){

                @Override
                public void set(PageLayout pageLayout) {
                    if (!JobSettings.this.isJobNew()) {
                        return;
                    }
                    if (pageLayout == null) {
                        return;
                    }
                    JobSettings.this.defaultPageLayout = false;
                    super.set(pageLayout);
                }

                @Override
                public void bind(ObservableValue<? extends PageLayout> observableValue) {
                    throw new RuntimeException("PageLayout property cannot be bound");
                }

                @Override
                public void bindBidirectional(Property<PageLayout> property) {
                    throw new RuntimeException("PageLayout property cannot be bound");
                }
            };
        }
        return this.layout;
    }

    public PageLayout getPageLayout() {
        return (PageLayout)this.pageLayoutProperty().get();
    }

    public void setPageLayout(PageLayout pageLayout) {
        this.pageLayoutProperty().set(pageLayout);
    }

    public String toString() {
        String string = System.lineSeparator();
        return " Collation = " + this.getCollation() + string + " Copies = " + this.getCopies() + string + " Sides = " + this.getPrintSides() + string + " JobName = " + this.getJobName() + string + " Page ranges = " + this.getPageRanges() + string + " Print color = " + this.getPrintColor() + string + " Print quality = " + this.getPrintQuality() + string + " Print resolution = " + this.getPrintResolution() + string + " Paper source = " + this.getPaperSource() + string + " Page layout = " + this.getPageLayout();
    }
}

