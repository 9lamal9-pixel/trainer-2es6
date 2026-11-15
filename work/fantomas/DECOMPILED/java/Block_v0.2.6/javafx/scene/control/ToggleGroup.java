/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import com.sun.javafx.collections.TrackableObservableList;
import com.sun.javafx.collections.VetoableListDecorator;
import java.util.HashMap;
import java.util.List;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.Toggle;

public class ToggleGroup {
    private final ObservableList<Toggle> toggles = new VetoableListDecorator<Toggle>((ObservableList)new TrackableObservableList<Toggle>(){

        @Override
        protected void onChanged(ListChangeListener.Change<Toggle> change) {
            block0: while (change.next()) {
                for (Toggle toggle : change.getRemoved()) {
                    if (!toggle.isSelected()) continue;
                    ToggleGroup.this.selectToggle(null);
                }
                for (Toggle toggle : change.getAddedSubList()) {
                    if (ToggleGroup.this.equals(toggle.getToggleGroup())) continue;
                    if (toggle.getToggleGroup() != null) {
                        toggle.getToggleGroup().getToggles().remove(toggle);
                    }
                    toggle.setToggleGroup(ToggleGroup.this);
                }
                for (Toggle toggle : change.getAddedSubList()) {
                    if (!toggle.isSelected()) continue;
                    ToggleGroup.this.selectToggle(toggle);
                    continue block0;
                }
            }
        }
    }){

        @Override
        protected void onProposedChange(List<Toggle> list, int ... nArray) {
            for (Toggle toggle : list) {
                if (nArray[0] == 0 && nArray[1] == this.size()) break;
                if (!ToggleGroup.this.toggles.contains(toggle)) continue;
                throw new IllegalArgumentException("Duplicate toggles are not allow in a ToggleGroup.");
            }
        }
    };
    private final ReadOnlyObjectWrapper<Toggle> selectedToggle = new ReadOnlyObjectWrapper<Toggle>(){

        @Override
        public void set(Toggle toggle) {
            if (this.isBound()) {
                throw new RuntimeException("A bound value cannot be set.");
            }
            Toggle toggle2 = (Toggle)this.get();
            if (toggle2 == toggle) {
                return;
            }
            if (ToggleGroup.this.setSelected(toggle, true) || toggle != null && toggle.getToggleGroup() == ToggleGroup.this || toggle == null) {
                if (toggle2 == null || toggle2.getToggleGroup() == ToggleGroup.this || !toggle2.isSelected()) {
                    ToggleGroup.this.setSelected(toggle2, false);
                }
                super.set(toggle);
            }
        }
    };
    private static final Object USER_DATA_KEY = new Object();
    private ObservableMap<Object, Object> properties;

    public final ObservableList<Toggle> getToggles() {
        return this.toggles;
    }

    public final void selectToggle(Toggle toggle) {
        this.selectedToggle.set(toggle);
    }

    public final Toggle getSelectedToggle() {
        return (Toggle)this.selectedToggle.get();
    }

    public final ReadOnlyObjectProperty<Toggle> selectedToggleProperty() {
        return this.selectedToggle.getReadOnlyProperty();
    }

    private boolean setSelected(Toggle toggle, boolean bl) {
        if (toggle != null && toggle.getToggleGroup() == this && !toggle.selectedProperty().isBound()) {
            toggle.setSelected(bl);
            return true;
        }
        return false;
    }

    final void clearSelectedToggle() {
        if (!((Toggle)this.selectedToggle.getValue()).isSelected()) {
            for (Toggle toggle : this.getToggles()) {
                if (!toggle.isSelected()) continue;
                return;
            }
        }
        this.selectedToggle.set(null);
    }

    public final ObservableMap<Object, Object> getProperties() {
        if (this.properties == null) {
            this.properties = FXCollections.observableMap(new HashMap());
        }
        return this.properties;
    }

    public boolean hasProperties() {
        return this.properties != null && !this.properties.isEmpty();
    }

    public void setUserData(Object object) {
        this.getProperties().put(USER_DATA_KEY, object);
    }

    public Object getUserData() {
        return this.getProperties().get(USER_DATA_KEY);
    }
}

