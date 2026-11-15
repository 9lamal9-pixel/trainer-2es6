/*
 * Decompiled with CFR 0.152.
 */
package javafx.application;

import com.sun.javafx.application.HostServicesDelegate;
import java.net.URI;
import javafx.application.Application;

public final class HostServices {
    private final HostServicesDelegate delegate;

    HostServices(Application application) {
        this.delegate = HostServicesDelegate.getInstance(application);
    }

    public final String getCodeBase() {
        return this.delegate.getCodeBase();
    }

    public final String getDocumentBase() {
        return this.delegate.getDocumentBase();
    }

    public final String resolveURI(String string, String string2) {
        URI uRI = URI.create(string).resolve(string2);
        return uRI.toString();
    }

    public final void showDocument(String string) {
        this.delegate.showDocument(string);
    }
}

