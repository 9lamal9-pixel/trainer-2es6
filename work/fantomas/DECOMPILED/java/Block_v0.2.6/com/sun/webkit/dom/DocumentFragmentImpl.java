/*
 * Decompiled with CFR 0.152.
 */
package com.sun.webkit.dom;

import com.sun.webkit.dom.ElementImpl;
import com.sun.webkit.dom.HTMLCollectionImpl;
import com.sun.webkit.dom.NodeImpl;
import com.sun.webkit.dom.NodeListImpl;
import org.w3c.dom.DOMException;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLCollection;

public class DocumentFragmentImpl
extends NodeImpl
implements DocumentFragment {
    DocumentFragmentImpl(long l) {
        super(l);
    }

    static DocumentFragment getImpl(long l) {
        return (DocumentFragment)DocumentFragmentImpl.create(l);
    }

    public HTMLCollection getChildren() {
        return HTMLCollectionImpl.getImpl(DocumentFragmentImpl.getChildrenImpl(this.getPeer()));
    }

    static native long getChildrenImpl(long var0);

    public Element getFirstElementChild() {
        return ElementImpl.getImpl(DocumentFragmentImpl.getFirstElementChildImpl(this.getPeer()));
    }

    static native long getFirstElementChildImpl(long var0);

    public Element getLastElementChild() {
        return ElementImpl.getImpl(DocumentFragmentImpl.getLastElementChildImpl(this.getPeer()));
    }

    static native long getLastElementChildImpl(long var0);

    public int getChildElementCount() {
        return DocumentFragmentImpl.getChildElementCountImpl(this.getPeer());
    }

    static native int getChildElementCountImpl(long var0);

    public Element getElementById(String string) {
        return ElementImpl.getImpl(DocumentFragmentImpl.getElementByIdImpl(this.getPeer(), string));
    }

    static native long getElementByIdImpl(long var0, String var2);

    public Element querySelector(String string) throws DOMException {
        return ElementImpl.getImpl(DocumentFragmentImpl.querySelectorImpl(this.getPeer(), string));
    }

    static native long querySelectorImpl(long var0, String var2);

    public NodeList querySelectorAll(String string) throws DOMException {
        return NodeListImpl.getImpl(DocumentFragmentImpl.querySelectorAllImpl(this.getPeer(), string));
    }

    static native long querySelectorAllImpl(long var0, String var2);
}

