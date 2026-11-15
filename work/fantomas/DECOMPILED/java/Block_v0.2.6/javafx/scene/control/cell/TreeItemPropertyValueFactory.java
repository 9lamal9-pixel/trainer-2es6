/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.cell;

import com.sun.javafx.logging.PlatformLogger;
import com.sun.javafx.property.PropertyReference;
import com.sun.javafx.scene.control.Logging;
import javafx.beans.NamedArg;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;

public class TreeItemPropertyValueFactory<S, T>
implements Callback<TreeTableColumn.CellDataFeatures<S, T>, ObservableValue<T>> {
    private final String property;
    private Class<?> columnClass;
    private String previousProperty;
    private PropertyReference<T> propertyRef;

    public TreeItemPropertyValueFactory(@NamedArg(value="property") String string) {
        this.property = string;
    }

    @Override
    public ObservableValue<T> call(TreeTableColumn.CellDataFeatures<S, T> cellDataFeatures) {
        TreeItem<S> treeItem = cellDataFeatures.getValue();
        return this.getCellDataReflectively(treeItem.getValue());
    }

    public final String getProperty() {
        return this.property;
    }

    private ObservableValue<T> getCellDataReflectively(S s) {
        if (this.getProperty() == null || this.getProperty().isEmpty() || s == null) {
            return null;
        }
        try {
            if (this.columnClass == null || this.previousProperty == null || !this.columnClass.equals(s.getClass()) || !this.previousProperty.equals(this.getProperty())) {
                this.columnClass = s.getClass();
                this.previousProperty = this.getProperty();
                this.propertyRef = new PropertyReference(s.getClass(), this.getProperty());
            }
            if (this.propertyRef != null) {
                return this.propertyRef.getProperty(s);
            }
        }
        catch (RuntimeException runtimeException) {
            try {
                T t = this.propertyRef.get(s);
                return new ReadOnlyObjectWrapper<T>(t);
            }
            catch (RuntimeException runtimeException2) {
                PlatformLogger platformLogger = Logging.getControlsLogger();
                if (platformLogger.isLoggable(PlatformLogger.Level.WARNING)) {
                    platformLogger.warning("Can not retrieve property '" + this.getProperty() + "' in TreeItemPropertyValueFactory: " + this + " with provided class type: " + s.getClass(), runtimeException);
                }
                this.propertyRef = null;
            }
        }
        return null;
    }
}

