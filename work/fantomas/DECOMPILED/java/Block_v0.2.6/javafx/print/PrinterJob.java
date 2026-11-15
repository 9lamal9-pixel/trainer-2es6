/*
 * Decompiled with CFR 0.152.
 */
package javafx.print;

import com.sun.javafx.print.PrinterJobImpl;
import com.sun.javafx.tk.PrintPipeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.print.JobSettings;
import javafx.print.PageLayout;
import javafx.print.Printer;
import javafx.scene.Node;
import javafx.stage.Window;

public final class PrinterJob {
    private PrinterJobImpl jobImpl;
    private ObjectProperty<Printer> printer;
    private JobSettings settings;
    private ReadOnlyObjectWrapper<JobStatus> jobStatus = new ReadOnlyObjectWrapper<JobStatus>(JobStatus.NOT_STARTED);

    public static final PrinterJob createPrinterJob() {
        Printer printer;
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            securityManager.checkPrintJobAccess();
        }
        if ((printer = Printer.getDefaultPrinter()) == null) {
            return null;
        }
        return new PrinterJob(printer);
    }

    public static final PrinterJob createPrinterJob(Printer printer) {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            securityManager.checkPrintJobAccess();
        }
        return new PrinterJob(printer);
    }

    private PrinterJob(Printer printer) {
        this.printer = this.createPrinterProperty(printer);
        this.settings = printer.getDefaultJobSettings();
        this.settings.setPrinterJob(this);
        this.createImplJob(printer, this.settings);
    }

    private synchronized PrinterJobImpl createImplJob(Printer printer, JobSettings jobSettings) {
        if (this.jobImpl == null) {
            this.jobImpl = PrintPipeline.getPrintPipeline().createPrinterJob(this);
        }
        return this.jobImpl;
    }

    boolean isJobNew() {
        return this.getJobStatus() == JobStatus.NOT_STARTED;
    }

    private ObjectProperty<Printer> createPrinterProperty(Printer printer) {
        return new SimpleObjectProperty<Printer>(printer){

            @Override
            public void set(Printer printer) {
                if (printer == this.get() || !PrinterJob.this.isJobNew()) {
                    return;
                }
                if (printer == null) {
                    printer = Printer.getDefaultPrinter();
                }
                super.set(printer);
                PrinterJob.this.jobImpl.setPrinterImpl(printer.getPrinterImpl());
                PrinterJob.this.settings.updateForPrinter(printer);
            }

            @Override
            public void bind(ObservableValue<? extends Printer> observableValue) {
                throw new RuntimeException("Printer property cannot be bound");
            }

            @Override
            public void bindBidirectional(Property<Printer> property) {
                throw new RuntimeException("Printer property cannot be bound");
            }

            @Override
            public Object getBean() {
                return PrinterJob.this;
            }

            @Override
            public String getName() {
                return "printer";
            }
        };
    }

    public final ObjectProperty<Printer> printerProperty() {
        return this.printer;
    }

    public synchronized Printer getPrinter() {
        return (Printer)this.printerProperty().get();
    }

    public synchronized void setPrinter(Printer printer) {
        this.printerProperty().set(printer);
    }

    public synchronized JobSettings getJobSettings() {
        return this.settings;
    }

    public synchronized boolean showPrintDialog(Window window) {
        if (!this.isJobNew()) {
            return false;
        }
        return this.jobImpl.showPrintDialog(window);
    }

    public synchronized boolean showPageSetupDialog(Window window) {
        if (!this.isJobNew()) {
            return false;
        }
        return this.jobImpl.showPageDialog(window);
    }

    synchronized PageLayout validatePageLayout(PageLayout pageLayout) {
        if (pageLayout == null) {
            throw new NullPointerException("pageLayout cannot be null");
        }
        return this.jobImpl.validatePageLayout(pageLayout);
    }

    public synchronized boolean printPage(PageLayout pageLayout, Node node) {
        if (((JobStatus)((Object)this.jobStatus.get())).ordinal() > JobStatus.PRINTING.ordinal()) {
            return false;
        }
        if (this.jobStatus.get() == JobStatus.NOT_STARTED) {
            this.jobStatus.set(JobStatus.PRINTING);
        }
        if (pageLayout == null || node == null) {
            this.jobStatus.set(JobStatus.ERROR);
            throw new NullPointerException("Parameters cannot be null");
        }
        boolean bl = this.jobImpl.print(pageLayout, node);
        if (!bl) {
            this.jobStatus.set(JobStatus.ERROR);
        }
        return bl;
    }

    public synchronized boolean printPage(Node node) {
        return this.printPage(this.settings.getPageLayout(), node);
    }

    public ReadOnlyObjectProperty<JobStatus> jobStatusProperty() {
        return this.jobStatus.getReadOnlyProperty();
    }

    public JobStatus getJobStatus() {
        return (JobStatus)((Object)this.jobStatus.get());
    }

    public void cancelJob() {
        if (((JobStatus)((Object)this.jobStatus.get())).ordinal() <= JobStatus.PRINTING.ordinal()) {
            this.jobStatus.set(JobStatus.CANCELED);
            this.jobImpl.cancelJob();
        }
    }

    public synchronized boolean endJob() {
        if (this.jobStatus.get() == JobStatus.NOT_STARTED) {
            this.cancelJob();
            return false;
        }
        if (this.jobStatus.get() == JobStatus.PRINTING) {
            boolean bl = this.jobImpl.endJob();
            this.jobStatus.set(bl ? JobStatus.DONE : JobStatus.ERROR);
            return bl;
        }
        return false;
    }

    public String toString() {
        return "JavaFX PrinterJob " + this.getPrinter() + "\n" + this.getJobSettings() + "\nJob Status = " + this.getJobStatus();
    }

    public static enum JobStatus {
        NOT_STARTED,
        PRINTING,
        CANCELED,
        ERROR,
        DONE;

    }
}

