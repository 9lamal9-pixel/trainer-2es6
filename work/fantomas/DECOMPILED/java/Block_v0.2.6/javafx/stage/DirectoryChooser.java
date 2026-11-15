/*
 * Decompiled with CFR 0.152.
 */
package javafx.stage;

import com.sun.javafx.tk.Toolkit;
import java.io.File;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.stage.Window;

public final class DirectoryChooser {
    private StringProperty title;
    private ObjectProperty<File> initialDirectory;

    public final void setTitle(String string) {
        this.titleProperty().set(string);
    }

    public final String getTitle() {
        return this.title != null ? (String)this.title.get() : null;
    }

    public final StringProperty titleProperty() {
        if (this.title == null) {
            this.title = new SimpleStringProperty(this, "title");
        }
        return this.title;
    }

    public final void setInitialDirectory(File file) {
        this.initialDirectoryProperty().set(file);
    }

    public final File getInitialDirectory() {
        return this.initialDirectory != null ? (File)this.initialDirectory.get() : null;
    }

    public final ObjectProperty<File> initialDirectoryProperty() {
        if (this.initialDirectory == null) {
            this.initialDirectory = new SimpleObjectProperty<File>(this, "initialDirectory");
        }
        return this.initialDirectory;
    }

    public File showDialog(Window window) {
        return Toolkit.getToolkit().showDirectoryChooser(window != null ? window.getPeer() : null, this.getTitle(), this.getInitialDirectory());
    }
}

