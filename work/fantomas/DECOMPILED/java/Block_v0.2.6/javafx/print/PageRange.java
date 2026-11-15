/*
 * Decompiled with CFR 0.152.
 */
package javafx.print;

import javafx.beans.NamedArg;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;

public final class PageRange {
    private ReadOnlyIntegerWrapper startPage;
    private ReadOnlyIntegerWrapper endPage;

    public PageRange(@NamedArg(value="startPage") int n, @NamedArg(value="endPage") int n2) {
        if (n <= 0 || n > n2) {
            throw new IllegalArgumentException("Invalid range : " + n + " -> " + n2);
        }
        this.startPageImplProperty().set(n);
        this.endPageImplProperty().set(n2);
    }

    private ReadOnlyIntegerWrapper startPageImplProperty() {
        if (this.startPage == null) {
            this.startPage = new ReadOnlyIntegerWrapper(this, "startPage", 1){

                @Override
                public void set(int n) {
                    if (n <= 0 || PageRange.this.endPage != null && n < PageRange.this.endPage.get()) {
                        return;
                    }
                    super.set(n);
                }
            };
        }
        return this.startPage;
    }

    public ReadOnlyIntegerProperty startPageProperty() {
        return this.startPageImplProperty().getReadOnlyProperty();
    }

    public int getStartPage() {
        return this.startPageProperty().get();
    }

    private ReadOnlyIntegerWrapper endPageImplProperty() {
        if (this.endPage == null) {
            this.endPage = new ReadOnlyIntegerWrapper(this, "endPage", 9999){

                @Override
                public void set(int n) {
                    if (n <= 0 || PageRange.this.startPage != null && n < PageRange.this.startPage.get()) {
                        return;
                    }
                    super.set(n);
                }
            };
        }
        return this.endPage;
    }

    public ReadOnlyIntegerProperty endPageProperty() {
        return this.endPageImplProperty().getReadOnlyProperty();
    }

    public int getEndPage() {
        return this.endPageProperty().get();
    }
}

