/*
 * Decompiled with CFR 0.152.
 */
package com.sun.webkit.dom;

import com.sun.webkit.dom.HTMLCollectionImpl;
import com.sun.webkit.dom.HTMLOptionElementImpl;
import com.sun.webkit.dom.NodeImpl;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLOptionElement;

public class HTMLOptionsCollectionImpl
extends HTMLCollectionImpl {
    HTMLOptionsCollectionImpl(long l) {
        super(l);
    }

    static HTMLOptionsCollectionImpl getImpl(long l) {
        return (HTMLOptionsCollectionImpl)HTMLOptionsCollectionImpl.create(l);
    }

    public int getSelectedIndex() {
        return HTMLOptionsCollectionImpl.getSelectedIndexImpl(this.getPeer());
    }

    static native int getSelectedIndexImpl(long var0);

    public void setSelectedIndex(int n) {
        HTMLOptionsCollectionImpl.setSelectedIndexImpl(this.getPeer(), n);
    }

    static native void setSelectedIndexImpl(long var0, int var2);

    @Override
    public int getLength() {
        return HTMLOptionsCollectionImpl.getLengthImpl(this.getPeer());
    }

    static native int getLengthImpl(long var0);

    public void setLength(int n) throws DOMException {
        HTMLOptionsCollectionImpl.setLengthImpl(this.getPeer(), n);
    }

    static native void setLengthImpl(long var0, int var2);

    @Override
    public Node namedItem(String string) {
        return NodeImpl.getImpl(HTMLOptionsCollectionImpl.namedItemImpl(this.getPeer(), string));
    }

    static native long namedItemImpl(long var0, String var2);

    public void add(HTMLOptionElement hTMLOptionElement, int n) throws DOMException {
        HTMLOptionsCollectionImpl.addImpl(this.getPeer(), HTMLOptionElementImpl.getPeer(hTMLOptionElement), n);
    }

    static native void addImpl(long var0, long var2, int var4);

    @Override
    public Node item(int n) {
        return NodeImpl.getImpl(HTMLOptionsCollectionImpl.itemImpl(this.getPeer(), n));
    }

    static native long itemImpl(long var0, int var2);
}

