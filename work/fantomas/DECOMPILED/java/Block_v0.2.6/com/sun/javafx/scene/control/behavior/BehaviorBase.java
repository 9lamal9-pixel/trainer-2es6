/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.behavior;

import com.sun.javafx.scene.control.inputmap.InputMap;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javafx.scene.Node;

public abstract class BehaviorBase<N extends Node> {
    private final N node;
    private final List<InputMap.Mapping<?>> installedDefaultMappings;
    private final List<Runnable> childInputMapDisposalHandlers;

    public BehaviorBase(N n) {
        this.node = n;
        this.installedDefaultMappings = new ArrayList();
        this.childInputMapDisposalHandlers = new ArrayList<Runnable>();
    }

    public abstract InputMap<N> getInputMap();

    public final N getNode() {
        return this.node;
    }

    public void dispose() {
        for (InputMap.Mapping<?> object : this.installedDefaultMappings) {
            this.getInputMap().getMappings().remove(object);
        }
        for (Runnable runnable : this.childInputMapDisposalHandlers) {
            runnable.run();
        }
    }

    protected void addDefaultMapping(List<InputMap.Mapping<?>> list) {
        this.addDefaultMapping(this.getInputMap(), list.toArray(new InputMap.Mapping[list.size()]));
    }

    protected void addDefaultMapping(InputMap.Mapping<?> ... mappingArray) {
        this.addDefaultMapping(this.getInputMap(), mappingArray);
    }

    protected void addDefaultMapping(InputMap<N> inputMap, InputMap.Mapping<?> ... mappingArray) {
        ArrayList arrayList = new ArrayList(inputMap.getMappings());
        for (InputMap.Mapping<?> mapping : mappingArray) {
            if (arrayList.contains(mapping)) continue;
            inputMap.getMappings().add(mapping);
            this.installedDefaultMappings.add(mapping);
        }
    }

    protected <T extends Node> void addDefaultChildMap(InputMap<T> inputMap, InputMap<T> inputMap2) {
        inputMap.getChildInputMaps().add(inputMap2);
        this.childInputMapDisposalHandlers.add(() -> inputMap.getChildInputMaps().remove(inputMap2));
    }

    protected InputMap<N> createInputMap() {
        return new InputMap<N>(this.node);
    }

    protected void removeMapping(Object object) {
        InputMap<N> inputMap = this.getInputMap();
        inputMap.lookupMapping(object).ifPresent(mapping -> {
            inputMap.getMappings().remove(mapping);
            this.installedDefaultMappings.remove(mapping);
        });
    }

    void rtl(Node node, Runnable runnable, Runnable runnable2) {
        switch (node.getEffectiveNodeOrientation()) {
            case RIGHT_TO_LEFT: {
                runnable.run();
                break;
            }
            default: {
                runnable2.run();
            }
        }
    }

    <T> void rtl(Node node, T t, Consumer<T> consumer, Consumer<T> consumer2) {
        switch (node.getEffectiveNodeOrientation()) {
            case RIGHT_TO_LEFT: {
                consumer.accept(t);
                break;
            }
            default: {
                consumer2.accept(t);
            }
        }
    }

    boolean isRTL(Node node) {
        switch (node.getEffectiveNodeOrientation()) {
            case RIGHT_TO_LEFT: {
                return true;
            }
        }
        return false;
    }
}

