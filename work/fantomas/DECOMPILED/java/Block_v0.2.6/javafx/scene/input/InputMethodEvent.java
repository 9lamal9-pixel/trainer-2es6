/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.input;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.NamedArg;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.input.InputEvent;
import javafx.scene.input.InputMethodTextRun;

public final class InputMethodEvent
extends InputEvent {
    private static final long serialVersionUID = 20121107L;
    public static final EventType<InputMethodEvent> INPUT_METHOD_TEXT_CHANGED = new EventType<InputEvent>(InputEvent.ANY, "INPUT_METHOD_TEXT_CHANGED");
    public static final EventType<InputMethodEvent> ANY = INPUT_METHOD_TEXT_CHANGED;
    private transient ObservableList<InputMethodTextRun> composed;
    private final String committed;
    private final int caretPosition;

    public InputMethodEvent(@NamedArg(value="source") Object object, @NamedArg(value="target") EventTarget eventTarget, @NamedArg(value="eventType") EventType<InputMethodEvent> eventType, @NamedArg(value="composed") List<InputMethodTextRun> list, @NamedArg(value="committed") String string, @NamedArg(value="caretPosition") int n) {
        super(object, eventTarget, (EventType<? extends InputEvent>)eventType);
        this.composed = FXCollections.unmodifiableObservableList(FXCollections.observableArrayList(list));
        this.committed = string;
        this.caretPosition = n;
    }

    public InputMethodEvent(@NamedArg(value="eventType") EventType<InputMethodEvent> eventType, @NamedArg(value="composed") List<InputMethodTextRun> list, @NamedArg(value="committed") String string, @NamedArg(value="caretPosition") int n) {
        this(null, null, eventType, list, string, n);
    }

    public final ObservableList<InputMethodTextRun> getComposed() {
        return this.composed;
    }

    public final String getCommitted() {
        return this.committed;
    }

    public final int getCaretPosition() {
        return this.caretPosition;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("InputMethodEvent [");
        stringBuilder.append("source = ").append(this.getSource());
        stringBuilder.append(", target = ").append(this.getTarget());
        stringBuilder.append(", eventType = ").append(this.getEventType());
        stringBuilder.append(", consumed = ").append(this.isConsumed());
        stringBuilder.append(", composed = ").append(this.getComposed());
        stringBuilder.append(", committed = ").append(this.getCommitted());
        stringBuilder.append(", caretPosition = ").append(this.getCaretPosition());
        return stringBuilder.append("]").toString();
    }

    @Override
    public InputMethodEvent copyFor(Object object, EventTarget eventTarget) {
        return (InputMethodEvent)super.copyFor(object, eventTarget);
    }

    public EventType<InputMethodEvent> getEventType() {
        return super.getEventType();
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.defaultWriteObject();
        objectOutputStream.writeObject(new ArrayList<InputMethodTextRun>(this.composed));
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        ArrayList arrayList = (ArrayList)objectInputStream.readObject();
        this.composed = FXCollections.unmodifiableObservableList(FXCollections.observableArrayList(arrayList));
    }
}

