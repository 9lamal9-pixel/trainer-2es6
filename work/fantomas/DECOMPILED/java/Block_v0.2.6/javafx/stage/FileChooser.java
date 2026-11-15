/*
 * Decompiled with CFR 0.152.
 */
package javafx.stage;

import com.sun.glass.ui.CommonDialogs;
import com.sun.javafx.tk.FileChooserType;
import com.sun.javafx.tk.Toolkit;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Window;

public final class FileChooser {
    private StringProperty title;
    private ObjectProperty<File> initialDirectory;
    private ObjectProperty<String> initialFileName;
    private ObservableList<ExtensionFilter> extensionFilters = FXCollections.observableArrayList();
    private ObjectProperty<ExtensionFilter> selectedExtensionFilter;

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

    public final void setInitialFileName(String string) {
        this.initialFileNameProperty().set(string);
    }

    public final String getInitialFileName() {
        return this.initialFileName != null ? (String)this.initialFileName.get() : null;
    }

    public final ObjectProperty<String> initialFileNameProperty() {
        if (this.initialFileName == null) {
            this.initialFileName = new SimpleObjectProperty<String>(this, "initialFileName");
        }
        return this.initialFileName;
    }

    public ObservableList<ExtensionFilter> getExtensionFilters() {
        return this.extensionFilters;
    }

    public final ObjectProperty<ExtensionFilter> selectedExtensionFilterProperty() {
        if (this.selectedExtensionFilter == null) {
            this.selectedExtensionFilter = new SimpleObjectProperty<ExtensionFilter>(this, "selectedExtensionFilter");
        }
        return this.selectedExtensionFilter;
    }

    public final void setSelectedExtensionFilter(ExtensionFilter extensionFilter) {
        this.selectedExtensionFilterProperty().setValue(extensionFilter);
    }

    public final ExtensionFilter getSelectedExtensionFilter() {
        return this.selectedExtensionFilter != null ? (ExtensionFilter)this.selectedExtensionFilter.get() : null;
    }

    public File showOpenDialog(Window window) {
        List<File> list = this.showDialog(window, FileChooserType.OPEN);
        return list != null && list.size() > 0 ? list.get(0) : null;
    }

    public List<File> showOpenMultipleDialog(Window window) {
        List<File> list = this.showDialog(window, FileChooserType.OPEN_MULTIPLE);
        return list != null && list.size() > 0 ? Collections.unmodifiableList(list) : null;
    }

    public File showSaveDialog(Window window) {
        List<File> list = this.showDialog(window, FileChooserType.SAVE);
        return list != null && list.size() > 0 ? list.get(0) : null;
    }

    private ExtensionFilter findSelectedFilter(CommonDialogs.ExtensionFilter extensionFilter) {
        if (extensionFilter != null) {
            String string = extensionFilter.getDescription();
            List<String> list = extensionFilter.getExtensions();
            for (ExtensionFilter extensionFilter2 : this.extensionFilters) {
                if (!string.equals(extensionFilter2.getDescription()) || !list.equals(extensionFilter2.getExtensions())) continue;
                return extensionFilter2;
            }
        }
        return null;
    }

    private List<File> showDialog(Window window, FileChooserType fileChooserType) {
        CommonDialogs.FileChooserResult fileChooserResult = Toolkit.getToolkit().showFileChooser(window != null ? window.getPeer() : null, this.getTitle(), this.getInitialDirectory(), this.getInitialFileName(), fileChooserType, this.extensionFilters, this.getSelectedExtensionFilter());
        if (fileChooserResult == null) {
            return null;
        }
        List<File> list = fileChooserResult.getFiles();
        if (list != null && list.size() > 0) {
            this.selectedExtensionFilterProperty().set(this.findSelectedFilter(fileChooserResult.getExtensionFilter()));
        }
        return list;
    }

    public static final class ExtensionFilter {
        private final String description;
        private final List<String> extensions;

        public ExtensionFilter(String string, String ... stringArray) {
            ExtensionFilter.validateArgs(string, stringArray);
            this.description = string;
            this.extensions = Collections.unmodifiableList(Arrays.asList((String[])stringArray.clone()));
        }

        public ExtensionFilter(String string, List<String> list) {
            String[] stringArray = list != null ? list.toArray(new String[list.size()]) : null;
            ExtensionFilter.validateArgs(string, stringArray);
            this.description = string;
            this.extensions = Collections.unmodifiableList(Arrays.asList(stringArray));
        }

        public String getDescription() {
            return this.description;
        }

        public List<String> getExtensions() {
            return this.extensions;
        }

        private static void validateArgs(String string, String[] stringArray) {
            if (string == null) {
                throw new NullPointerException("Description must not be null");
            }
            if (string.isEmpty()) {
                throw new IllegalArgumentException("Description must not be empty");
            }
            if (stringArray == null) {
                throw new NullPointerException("Extensions must not be null");
            }
            if (stringArray.length == 0) {
                throw new IllegalArgumentException("At least one extension must be defined");
            }
            for (String string2 : stringArray) {
                if (string2 == null) {
                    throw new NullPointerException("Extension must not be null");
                }
                if (!string2.isEmpty()) continue;
                throw new IllegalArgumentException("Extension must not be empty");
            }
        }
    }
}

