/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.inputmap;

import com.sun.javafx.scene.control.inputmap.KeyBinding;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Pair;

public class InputMap<N extends Node>
implements EventHandler<Event> {
    private final N node;
    private final ObservableList<InputMap<N>> childInputMaps;
    private final ObservableList<Mapping<?>> mappings;
    private final Map<EventType<?>, List<EventHandler<? super Event>>> installedEventHandlers;
    private final Map<EventType, List<Mapping>> eventTypeMappings;
    private ReadOnlyObjectWrapper<InputMap<N>> parentInputMap = new ReadOnlyObjectWrapper<InputMap<N>>(this, "parentInputMap"){

        @Override
        protected void invalidated() {
            InputMap.this.reprocessAllMappings();
        }
    };
    private ObjectProperty<Predicate<? extends Event>> interceptor = new SimpleObjectProperty<Predicate<? extends Event>>(this, "interceptor");

    public InputMap(N n) {
        if (n == null) {
            throw new IllegalArgumentException("Node can not be null");
        }
        this.node = n;
        this.eventTypeMappings = new HashMap<EventType, List<Mapping>>();
        this.installedEventHandlers = new HashMap();
        this.mappings = FXCollections.observableArrayList();
        this.mappings.addListener(change -> {
            while (change.next()) {
                Object object2;
                if (change.wasRemoved()) {
                    for (Object object2 : change.getRemoved()) {
                        this.removeMapping((Mapping<?>)object2);
                    }
                }
                if (!change.wasAdded()) continue;
                ArrayList arrayList = new ArrayList();
                object2 = change.getAddedSubList().iterator();
                while (object2.hasNext()) {
                    Mapping mapping = (Mapping)object2.next();
                    if (mapping == null) {
                        arrayList.add(null);
                        continue;
                    }
                    this.addMapping(mapping);
                }
                if (arrayList.isEmpty()) continue;
                this.getMappings().removeAll(arrayList);
                throw new IllegalArgumentException("Null mappings not permitted");
            }
        });
        this.childInputMaps = FXCollections.observableArrayList();
        this.childInputMaps.addListener(change -> {
            while (change.next()) {
                Object object2;
                if (change.wasRemoved()) {
                    for (Object object2 : change.getRemoved()) {
                        super.setParentInputMap(null);
                    }
                }
                if (!change.wasAdded()) continue;
                ArrayList arrayList = new ArrayList();
                object2 = change.getAddedSubList().iterator();
                while (object2.hasNext()) {
                    InputMap inputMap = (InputMap)object2.next();
                    if (inputMap.getNode() != this.getNode()) {
                        arrayList.add(inputMap);
                        continue;
                    }
                    inputMap.setParentInputMap(this);
                }
                if (arrayList.isEmpty()) continue;
                this.getChildInputMaps().removeAll(arrayList);
                throw new IllegalArgumentException("Child InputMap intances need to share a common Node object");
            }
        });
    }

    private final void setParentInputMap(InputMap<N> inputMap) {
        this.parentInputMap.set(inputMap);
    }

    private final InputMap<N> getParentInputMap() {
        return (InputMap)this.parentInputMap.get();
    }

    private final ReadOnlyObjectProperty<InputMap<N>> parentInputMapProperty() {
        return this.parentInputMap.getReadOnlyProperty();
    }

    public final Predicate<? extends Event> getInterceptor() {
        return (Predicate)this.interceptor.get();
    }

    public final void setInterceptor(Predicate<? extends Event> predicate) {
        this.interceptor.set(predicate);
    }

    public final ObjectProperty<Predicate<? extends Event>> interceptorProperty() {
        return this.interceptor;
    }

    public final N getNode() {
        return this.node;
    }

    public ObservableList<Mapping<?>> getMappings() {
        return this.mappings;
    }

    public ObservableList<InputMap<N>> getChildInputMaps() {
        return this.childInputMaps;
    }

    public void dispose() {
        for (InputMap inputMap : this.getChildInputMaps()) {
            inputMap.dispose();
        }
        this.removeAllEventHandlers();
        this.getMappings().clear();
    }

    @Override
    public void handle(Event event) {
        if (event == null || event.isConsumed()) {
            return;
        }
        List<Mapping<?>> list = this.lookup(event, true);
        for (Mapping<?> mapping : list) {
            EventHandler<?> eventHandler = mapping.getEventHandler();
            if (eventHandler != null) {
                eventHandler.handle(event);
            }
            if (mapping.isAutoConsume()) {
                event.consume();
            }
            if (!event.isConsumed()) continue;
            break;
        }
    }

    public Optional<Mapping<?>> lookupMapping(Object object) {
        if (object == null) {
            return Optional.empty();
        }
        List<Mapping<?>> list = this.lookupMappingKey(object);
        for (int i = 0; i < this.getChildInputMaps().size(); ++i) {
            InputMap inputMap = (InputMap)this.getChildInputMaps().get(i);
            List<Mapping<?>> list2 = inputMap.lookupMappingKey(object);
            list.addAll(0, list2);
        }
        return list.size() > 0 ? Optional.of(list.get(0)) : Optional.empty();
    }

    private List<Mapping<?>> lookupMappingKey(Object object) {
        return this.getMappings().stream().filter(mapping -> !mapping.isDisabled()).filter(mapping -> object.equals(mapping.getMappingKey())).collect(Collectors.toList());
    }

    private List<Mapping<?>> lookup(Event event, boolean bl) {
        boolean bl2;
        if (bl && (bl2 = this.testInterceptor(event, this.getInterceptor()))) {
            return Collections.emptyList();
        }
        ArrayList arrayList = new ArrayList();
        int n = 0;
        List<Pair<Integer, Mapping<?>>> list = this.lookupMappingAndSpecificity(event, n);
        if (!list.isEmpty()) {
            n = list.get(0).getKey();
            arrayList.addAll(list.stream().map(pair -> (Mapping)pair.getValue()).collect(Collectors.toList()));
        }
        for (int i = 0; i < this.getChildInputMaps().size(); ++i) {
            InputMap inputMap = (InputMap)this.getChildInputMaps().get(i);
            n = this.scanRecursively(inputMap, event, bl, n, arrayList);
        }
        return arrayList;
    }

    private int scanRecursively(InputMap<?> inputMap, Event event, boolean bl, int n, List<Mapping<?>> list) {
        int n2;
        boolean bl2;
        if (bl && (bl2 = this.testInterceptor(event, inputMap.getInterceptor()))) {
            return n;
        }
        List<Pair<Integer, Mapping<?>>> list2 = super.lookupMappingAndSpecificity(event, n);
        if (!list2.isEmpty()) {
            n2 = list2.get(0).getKey();
            List list3 = list2.stream().map(pair -> (Mapping)pair.getValue()).collect(Collectors.toList());
            if (n2 == n) {
                list.addAll(0, list3);
            } else if (n2 > n) {
                list.clear();
                n = n2;
                list.addAll(list3);
            }
        }
        for (n2 = 0; n2 < inputMap.getChildInputMaps().size(); ++n2) {
            n = this.scanRecursively((InputMap)inputMap.getChildInputMaps().get(n2), event, bl, n, list);
        }
        return n;
    }

    private InputMap<N> getRootInputMap() {
        InputMap<N> inputMap;
        InputMap<N> inputMap2 = this;
        while (inputMap2 != null && (inputMap = inputMap2.getParentInputMap()) != null) {
            inputMap2 = inputMap;
        }
        return inputMap2;
    }

    private void addMapping(Mapping<?> mapping) {
        InputMap<N> inputMap = this.getRootInputMap();
        super.addEventHandler(((Mapping)mapping).eventType);
        EventType<?> eventType2 = mapping.getEventType();
        List list = this.eventTypeMappings.computeIfAbsent(eventType2, eventType -> new ArrayList());
        list.add(mapping);
    }

    private void removeMapping(Mapping<?> mapping) {
        EventType<?> eventType = mapping.getEventType();
        if (this.eventTypeMappings.containsKey(eventType)) {
            List<Mapping> list = this.eventTypeMappings.get(eventType);
            list.remove(mapping);
        }
    }

    private void addEventHandler(EventType eventType2) {
        List list = this.installedEventHandlers.computeIfAbsent(eventType2, eventType -> new ArrayList());
        EventHandler<Event> eventHandler = this::handle;
        if (list.isEmpty()) {
            ((Node)this.node).addEventHandler(eventType2, eventHandler);
        }
        list.add(eventHandler);
    }

    private void removeAllEventHandlers() {
        for (EventType<?> eventType : this.installedEventHandlers.keySet()) {
            List<EventHandler<? super Event>> list = this.installedEventHandlers.get(eventType);
            for (EventHandler<? super Event> eventHandler : list) {
                ((Node)this.node).removeEventHandler(eventType, eventHandler);
            }
        }
    }

    private void reprocessAllMappings() {
        this.removeAllEventHandlers();
        this.mappings.stream().forEach(this::addMapping);
        for (InputMap inputMap : this.getChildInputMaps()) {
            inputMap.reprocessAllMappings();
        }
    }

    private List<Pair<Integer, Mapping<?>>> lookupMappingAndSpecificity(Event event, int n) {
        int n2 = n;
        List list = this.eventTypeMappings.getOrDefault(event.getEventType(), Collections.emptyList());
        ArrayList arrayList = new ArrayList();
        for (Mapping mapping : list) {
            boolean bl;
            if (mapping.isDisabled() || (bl = this.testInterceptor(event, mapping.getInterceptor()))) continue;
            int n3 = mapping.getSpecificity(event);
            if (n3 > 0 && n3 == n2) {
                arrayList.add(new Pair<Integer, Mapping>(n3, mapping));
                continue;
            }
            if (n3 <= n2) continue;
            arrayList.clear();
            arrayList.add(new Pair<Integer, Mapping>(n3, mapping));
            n2 = n3;
        }
        return arrayList;
    }

    private boolean testInterceptor(Event event, Predicate predicate) {
        return predicate != null && predicate.test(event);
    }

    public static class MouseMappingInterceptor
    implements Predicate<Event> {
        private final EventType<MouseEvent> eventType;

        public MouseMappingInterceptor(EventType<MouseEvent> eventType) {
            this.eventType = eventType;
        }

        @Override
        public boolean test(Event event) {
            if (!(event instanceof MouseEvent)) {
                return false;
            }
            return event.getEventType() == this.eventType;
        }
    }

    public static class KeyMappingInterceptor
    implements Predicate<Event> {
        private final KeyBinding keyBinding;

        public KeyMappingInterceptor(KeyBinding keyBinding) {
            this.keyBinding = keyBinding;
        }

        @Override
        public boolean test(Event event) {
            if (!(event instanceof KeyEvent)) {
                return false;
            }
            return KeyBinding.toKeyBinding((KeyEvent)event).equals(this.keyBinding);
        }
    }

    public static class MouseMapping
    extends Mapping<MouseEvent> {
        public MouseMapping(EventType<MouseEvent> eventType, EventHandler<MouseEvent> eventHandler) {
            super(eventType, eventHandler);
            if (eventType == null) {
                throw new IllegalArgumentException("MouseMapping eventType constructor argument can not be null");
            }
        }

        @Override
        public int getSpecificity(Event event) {
            if (this.isDisabled()) {
                return 0;
            }
            if (!(event instanceof MouseEvent)) {
                return 0;
            }
            EventType eventType = this.getEventType();
            int n = 0;
            if (event.getEventType() == MouseEvent.MOUSE_CLICKED && eventType != MouseEvent.MOUSE_CLICKED) {
                return 0;
            }
            ++n;
            if (event.getEventType() == MouseEvent.MOUSE_DRAGGED && eventType != MouseEvent.MOUSE_DRAGGED) {
                return 0;
            }
            ++n;
            if (event.getEventType() == MouseEvent.MOUSE_ENTERED && eventType != MouseEvent.MOUSE_ENTERED) {
                return 0;
            }
            ++n;
            if (event.getEventType() == MouseEvent.MOUSE_ENTERED_TARGET && eventType != MouseEvent.MOUSE_ENTERED_TARGET) {
                return 0;
            }
            ++n;
            if (event.getEventType() == MouseEvent.MOUSE_EXITED && eventType != MouseEvent.MOUSE_EXITED) {
                return 0;
            }
            ++n;
            if (event.getEventType() == MouseEvent.MOUSE_EXITED_TARGET && eventType != MouseEvent.MOUSE_EXITED_TARGET) {
                return 0;
            }
            ++n;
            if (event.getEventType() == MouseEvent.MOUSE_MOVED && eventType != MouseEvent.MOUSE_MOVED) {
                return 0;
            }
            ++n;
            if (event.getEventType() == MouseEvent.MOUSE_PRESSED && eventType != MouseEvent.MOUSE_PRESSED) {
                return 0;
            }
            ++n;
            if (event.getEventType() == MouseEvent.MOUSE_RELEASED && eventType != MouseEvent.MOUSE_RELEASED) {
                return 0;
            }
            return ++n;
        }
    }

    public static class KeyMapping
    extends Mapping<KeyEvent> {
        private final KeyBinding keyBinding;

        public KeyMapping(KeyCode keyCode, EventHandler<KeyEvent> eventHandler) {
            this(new KeyBinding(keyCode), eventHandler);
        }

        public KeyMapping(KeyCode keyCode, EventType<KeyEvent> eventType, EventHandler<KeyEvent> eventHandler) {
            this(new KeyBinding(keyCode, eventType), eventHandler);
        }

        public KeyMapping(KeyBinding keyBinding, EventHandler<KeyEvent> eventHandler) {
            this(keyBinding, eventHandler, null);
        }

        public KeyMapping(KeyBinding keyBinding, EventHandler<KeyEvent> eventHandler, Predicate<KeyEvent> predicate) {
            super(keyBinding == null ? null : keyBinding.getType(), eventHandler);
            if (keyBinding == null) {
                throw new IllegalArgumentException("KeyMapping keyBinding constructor argument can not be null");
            }
            this.keyBinding = keyBinding;
            this.setInterceptor(predicate);
        }

        @Override
        public Object getMappingKey() {
            return this.keyBinding;
        }

        @Override
        public int getSpecificity(Event event) {
            if (this.isDisabled()) {
                return 0;
            }
            if (!(event instanceof KeyEvent)) {
                return 0;
            }
            return this.keyBinding.getSpecificity((KeyEvent)event);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof KeyMapping)) {
                return false;
            }
            if (!super.equals(object)) {
                return false;
            }
            KeyMapping keyMapping = (KeyMapping)object;
            return this.keyBinding.equals(keyMapping.keyBinding);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.keyBinding);
        }
    }

    public static abstract class Mapping<T extends Event> {
        private final EventType<T> eventType;
        private final EventHandler<T> eventHandler;
        private BooleanProperty disabled = new SimpleBooleanProperty(this, "disabled", false);
        private BooleanProperty autoConsume = new SimpleBooleanProperty(this, "autoConsume", true);
        private ObjectProperty<Predicate<? extends Event>> interceptor = new SimpleObjectProperty<Predicate<? extends Event>>(this, "interceptor");

        public Mapping(EventType<T> eventType, EventHandler<T> eventHandler) {
            this.eventType = eventType;
            this.eventHandler = eventHandler;
        }

        public abstract int getSpecificity(Event var1);

        public final void setDisabled(boolean bl) {
            this.disabled.set(bl);
        }

        public final boolean isDisabled() {
            return this.disabled.get();
        }

        public final BooleanProperty disabledProperty() {
            return this.disabled;
        }

        public final void setAutoConsume(boolean bl) {
            this.autoConsume.set(bl);
        }

        public final boolean isAutoConsume() {
            return this.autoConsume.get();
        }

        public final BooleanProperty autoConsumeProperty() {
            return this.autoConsume;
        }

        public final EventType<T> getEventType() {
            return this.eventType;
        }

        public final EventHandler<T> getEventHandler() {
            return this.eventHandler;
        }

        public final Predicate<? extends Event> getInterceptor() {
            return (Predicate)this.interceptor.get();
        }

        public final void setInterceptor(Predicate<? extends Event> predicate) {
            this.interceptor.set(predicate);
        }

        public final ObjectProperty<Predicate<? extends Event>> interceptorProperty() {
            return this.interceptor;
        }

        public Object getMappingKey() {
            return this.eventType;
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof Mapping)) {
                return false;
            }
            Mapping mapping = (Mapping)object;
            return !(this.eventType != null ? !this.eventType.equals(mapping.getEventType()) : mapping.getEventType() != null);
        }

        public int hashCode() {
            return this.eventType != null ? this.eventType.hashCode() : 0;
        }
    }
}

