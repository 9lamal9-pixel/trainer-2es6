/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.binding;

import com.sun.javafx.binding.BindingHelperObserver;
import com.sun.javafx.binding.MapExpressionHelper;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Binding;
import javafx.beans.binding.MapExpression;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

public abstract class MapBinding<K, V>
extends MapExpression<K, V>
implements Binding<ObservableMap<K, V>> {
    private final MapChangeListener<K, V> mapChangeListener = new MapChangeListener<K, V>(){

        @Override
        public void onChanged(MapChangeListener.Change<? extends K, ? extends V> change) {
            MapBinding.this.invalidateProperties();
            MapBinding.this.onInvalidating();
            MapExpressionHelper.fireValueChangedEvent(MapBinding.this.helper, change);
        }
    };
    private ObservableMap<K, V> value;
    private boolean valid = false;
    private BindingHelperObserver observer;
    private MapExpressionHelper<K, V> helper = null;
    private SizeProperty size0;
    private EmptyProperty empty0;

    @Override
    public ReadOnlyIntegerProperty sizeProperty() {
        if (this.size0 == null) {
            this.size0 = new SizeProperty();
        }
        return this.size0;
    }

    @Override
    public ReadOnlyBooleanProperty emptyProperty() {
        if (this.empty0 == null) {
            this.empty0 = new EmptyProperty();
        }
        return this.empty0;
    }

    @Override
    public void addListener(InvalidationListener invalidationListener) {
        this.helper = MapExpressionHelper.addListener(this.helper, this, invalidationListener);
    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {
        this.helper = MapExpressionHelper.removeListener(this.helper, invalidationListener);
    }

    @Override
    public void addListener(ChangeListener<? super ObservableMap<K, V>> changeListener) {
        this.helper = MapExpressionHelper.addListener(this.helper, this, changeListener);
    }

    @Override
    public void removeListener(ChangeListener<? super ObservableMap<K, V>> changeListener) {
        this.helper = MapExpressionHelper.removeListener(this.helper, changeListener);
    }

    @Override
    public void addListener(MapChangeListener<? super K, ? super V> mapChangeListener) {
        this.helper = MapExpressionHelper.addListener(this.helper, this, mapChangeListener);
    }

    @Override
    public void removeListener(MapChangeListener<? super K, ? super V> mapChangeListener) {
        this.helper = MapExpressionHelper.removeListener(this.helper, mapChangeListener);
    }

    protected final void bind(Observable ... observableArray) {
        if (observableArray != null && observableArray.length > 0) {
            if (this.observer == null) {
                this.observer = new BindingHelperObserver(this);
            }
            for (Observable observable : observableArray) {
                if (observable == null) continue;
                observable.addListener(this.observer);
            }
        }
    }

    protected final void unbind(Observable ... observableArray) {
        if (this.observer != null) {
            for (Observable observable : observableArray) {
                if (observable == null) continue;
                observable.removeListener(this.observer);
            }
            this.observer = null;
        }
    }

    @Override
    public void dispose() {
    }

    @Override
    public ObservableList<?> getDependencies() {
        return FXCollections.emptyObservableList();
    }

    @Override
    public final ObservableMap<K, V> get() {
        if (!this.valid) {
            this.value = this.computeValue();
            this.valid = true;
            if (this.value != null) {
                this.value.addListener(this.mapChangeListener);
            }
        }
        return this.value;
    }

    protected void onInvalidating() {
    }

    private void invalidateProperties() {
        if (this.size0 != null) {
            this.size0.fireValueChangedEvent();
        }
        if (this.empty0 != null) {
            this.empty0.fireValueChangedEvent();
        }
    }

    @Override
    public final void invalidate() {
        if (this.valid) {
            if (this.value != null) {
                this.value.removeListener(this.mapChangeListener);
            }
            this.valid = false;
            this.invalidateProperties();
            this.onInvalidating();
            MapExpressionHelper.fireValueChangedEvent(this.helper);
        }
    }

    @Override
    public final boolean isValid() {
        return this.valid;
    }

    protected abstract ObservableMap<K, V> computeValue();

    public String toString() {
        return this.valid ? "MapBinding [value: " + (ObservableMap)this.get() + "]" : "MapBinding [invalid]";
    }

    private class EmptyProperty
    extends ReadOnlyBooleanPropertyBase {
        private EmptyProperty() {
        }

        @Override
        public boolean get() {
            return MapBinding.this.isEmpty();
        }

        @Override
        public Object getBean() {
            return MapBinding.this;
        }

        @Override
        public String getName() {
            return "empty";
        }

        @Override
        protected void fireValueChangedEvent() {
            super.fireValueChangedEvent();
        }
    }

    private class SizeProperty
    extends ReadOnlyIntegerPropertyBase {
        private SizeProperty() {
        }

        @Override
        public int get() {
            return MapBinding.this.size();
        }

        @Override
        public Object getBean() {
            return MapBinding.this;
        }

        @Override
        public String getName() {
            return "size";
        }

        @Override
        protected void fireValueChangedEvent() {
            super.fireValueChangedEvent();
        }
    }
}

