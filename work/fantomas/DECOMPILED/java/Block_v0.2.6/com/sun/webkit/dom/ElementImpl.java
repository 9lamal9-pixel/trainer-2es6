/*
 * Decompiled with CFR 0.152.
 */
package com.sun.webkit.dom;

import com.sun.webkit.dom.AttrImpl;
import com.sun.webkit.dom.CSSStyleDeclarationImpl;
import com.sun.webkit.dom.EventListenerImpl;
import com.sun.webkit.dom.HTMLCollectionImpl;
import com.sun.webkit.dom.NamedNodeMapImpl;
import com.sun.webkit.dom.NodeImpl;
import com.sun.webkit.dom.NodeListImpl;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.html.HTMLCollection;

public class ElementImpl
extends NodeImpl
implements Element {
    public static final int ALLOW_KEYBOARD_INPUT = 1;

    ElementImpl(long l) {
        super(l);
    }

    static Element getImpl(long l) {
        return (Element)ElementImpl.create(l);
    }

    static native boolean isHTMLElementImpl(long var0);

    @Override
    public String getTagName() {
        return ElementImpl.getTagNameImpl(this.getPeer());
    }

    static native String getTagNameImpl(long var0);

    @Override
    public NamedNodeMap getAttributes() {
        return NamedNodeMapImpl.getImpl(ElementImpl.getAttributesImpl(this.getPeer()));
    }

    static native long getAttributesImpl(long var0);

    public CSSStyleDeclaration getStyle() {
        return CSSStyleDeclarationImpl.getImpl(ElementImpl.getStyleImpl(this.getPeer()));
    }

    static native long getStyleImpl(long var0);

    public String getId() {
        return ElementImpl.getIdImpl(this.getPeer());
    }

    static native String getIdImpl(long var0);

    public void setId(String string) {
        ElementImpl.setIdImpl(this.getPeer(), string);
    }

    static native void setIdImpl(long var0, String var2);

    public double getOffsetLeft() {
        return ElementImpl.getOffsetLeftImpl(this.getPeer());
    }

    static native double getOffsetLeftImpl(long var0);

    public double getOffsetTop() {
        return ElementImpl.getOffsetTopImpl(this.getPeer());
    }

    static native double getOffsetTopImpl(long var0);

    public double getOffsetWidth() {
        return ElementImpl.getOffsetWidthImpl(this.getPeer());
    }

    static native double getOffsetWidthImpl(long var0);

    public double getOffsetHeight() {
        return ElementImpl.getOffsetHeightImpl(this.getPeer());
    }

    static native double getOffsetHeightImpl(long var0);

    public double getClientLeft() {
        return ElementImpl.getClientLeftImpl(this.getPeer());
    }

    static native double getClientLeftImpl(long var0);

    public double getClientTop() {
        return ElementImpl.getClientTopImpl(this.getPeer());
    }

    static native double getClientTopImpl(long var0);

    public double getClientWidth() {
        return ElementImpl.getClientWidthImpl(this.getPeer());
    }

    static native double getClientWidthImpl(long var0);

    public double getClientHeight() {
        return ElementImpl.getClientHeightImpl(this.getPeer());
    }

    static native double getClientHeightImpl(long var0);

    public int getScrollLeft() {
        return ElementImpl.getScrollLeftImpl(this.getPeer());
    }

    static native int getScrollLeftImpl(long var0);

    public void setScrollLeft(int n) {
        ElementImpl.setScrollLeftImpl(this.getPeer(), n);
    }

    static native void setScrollLeftImpl(long var0, int var2);

    public int getScrollTop() {
        return ElementImpl.getScrollTopImpl(this.getPeer());
    }

    static native int getScrollTopImpl(long var0);

    public void setScrollTop(int n) {
        ElementImpl.setScrollTopImpl(this.getPeer(), n);
    }

    static native void setScrollTopImpl(long var0, int var2);

    public int getScrollWidth() {
        return ElementImpl.getScrollWidthImpl(this.getPeer());
    }

    static native int getScrollWidthImpl(long var0);

    public int getScrollHeight() {
        return ElementImpl.getScrollHeightImpl(this.getPeer());
    }

    static native int getScrollHeightImpl(long var0);

    public Element getOffsetParent() {
        return ElementImpl.getImpl(ElementImpl.getOffsetParentImpl(this.getPeer()));
    }

    static native long getOffsetParentImpl(long var0);

    public String getInnerHTML() {
        return ElementImpl.getInnerHTMLImpl(this.getPeer());
    }

    static native String getInnerHTMLImpl(long var0);

    public void setInnerHTML(String string) throws DOMException {
        ElementImpl.setInnerHTMLImpl(this.getPeer(), string);
    }

    static native void setInnerHTMLImpl(long var0, String var2);

    public String getOuterHTML() {
        return ElementImpl.getOuterHTMLImpl(this.getPeer());
    }

    static native String getOuterHTMLImpl(long var0);

    public void setOuterHTML(String string) throws DOMException {
        ElementImpl.setOuterHTMLImpl(this.getPeer(), string);
    }

    static native void setOuterHTMLImpl(long var0, String var2);

    public String getClassName() {
        return ElementImpl.getClassNameImpl(this.getPeer());
    }

    static native String getClassNameImpl(long var0);

    public void setClassName(String string) {
        ElementImpl.setClassNameImpl(this.getPeer(), string);
    }

    static native void setClassNameImpl(long var0, String var2);

    public EventListener getOnbeforecopy() {
        return EventListenerImpl.getImpl(ElementImpl.getOnbeforecopyImpl(this.getPeer()));
    }

    static native long getOnbeforecopyImpl(long var0);

    public void setOnbeforecopy(EventListener eventListener) {
        ElementImpl.setOnbeforecopyImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnbeforecopyImpl(long var0, long var2);

    public EventListener getOnbeforecut() {
        return EventListenerImpl.getImpl(ElementImpl.getOnbeforecutImpl(this.getPeer()));
    }

    static native long getOnbeforecutImpl(long var0);

    public void setOnbeforecut(EventListener eventListener) {
        ElementImpl.setOnbeforecutImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnbeforecutImpl(long var0, long var2);

    public EventListener getOnbeforepaste() {
        return EventListenerImpl.getImpl(ElementImpl.getOnbeforepasteImpl(this.getPeer()));
    }

    static native long getOnbeforepasteImpl(long var0);

    public void setOnbeforepaste(EventListener eventListener) {
        ElementImpl.setOnbeforepasteImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnbeforepasteImpl(long var0, long var2);

    public EventListener getOncopy() {
        return EventListenerImpl.getImpl(ElementImpl.getOncopyImpl(this.getPeer()));
    }

    static native long getOncopyImpl(long var0);

    public void setOncopy(EventListener eventListener) {
        ElementImpl.setOncopyImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOncopyImpl(long var0, long var2);

    public EventListener getOncut() {
        return EventListenerImpl.getImpl(ElementImpl.getOncutImpl(this.getPeer()));
    }

    static native long getOncutImpl(long var0);

    public void setOncut(EventListener eventListener) {
        ElementImpl.setOncutImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOncutImpl(long var0, long var2);

    public EventListener getOnpaste() {
        return EventListenerImpl.getImpl(ElementImpl.getOnpasteImpl(this.getPeer()));
    }

    static native long getOnpasteImpl(long var0);

    public void setOnpaste(EventListener eventListener) {
        ElementImpl.setOnpasteImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnpasteImpl(long var0, long var2);

    public EventListener getOnselectstart() {
        return EventListenerImpl.getImpl(ElementImpl.getOnselectstartImpl(this.getPeer()));
    }

    static native long getOnselectstartImpl(long var0);

    public void setOnselectstart(EventListener eventListener) {
        ElementImpl.setOnselectstartImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnselectstartImpl(long var0, long var2);

    public EventListener getOnanimationend() {
        return EventListenerImpl.getImpl(ElementImpl.getOnanimationendImpl(this.getPeer()));
    }

    static native long getOnanimationendImpl(long var0);

    public void setOnanimationend(EventListener eventListener) {
        ElementImpl.setOnanimationendImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnanimationendImpl(long var0, long var2);

    public EventListener getOnanimationiteration() {
        return EventListenerImpl.getImpl(ElementImpl.getOnanimationiterationImpl(this.getPeer()));
    }

    static native long getOnanimationiterationImpl(long var0);

    public void setOnanimationiteration(EventListener eventListener) {
        ElementImpl.setOnanimationiterationImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnanimationiterationImpl(long var0, long var2);

    public EventListener getOnanimationstart() {
        return EventListenerImpl.getImpl(ElementImpl.getOnanimationstartImpl(this.getPeer()));
    }

    static native long getOnanimationstartImpl(long var0);

    public void setOnanimationstart(EventListener eventListener) {
        ElementImpl.setOnanimationstartImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnanimationstartImpl(long var0, long var2);

    public EventListener getOntransitionend() {
        return EventListenerImpl.getImpl(ElementImpl.getOntransitionendImpl(this.getPeer()));
    }

    static native long getOntransitionendImpl(long var0);

    public void setOntransitionend(EventListener eventListener) {
        ElementImpl.setOntransitionendImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOntransitionendImpl(long var0, long var2);

    public EventListener getOnwebkitanimationend() {
        return EventListenerImpl.getImpl(ElementImpl.getOnwebkitanimationendImpl(this.getPeer()));
    }

    static native long getOnwebkitanimationendImpl(long var0);

    public void setOnwebkitanimationend(EventListener eventListener) {
        ElementImpl.setOnwebkitanimationendImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnwebkitanimationendImpl(long var0, long var2);

    public EventListener getOnwebkitanimationiteration() {
        return EventListenerImpl.getImpl(ElementImpl.getOnwebkitanimationiterationImpl(this.getPeer()));
    }

    static native long getOnwebkitanimationiterationImpl(long var0);

    public void setOnwebkitanimationiteration(EventListener eventListener) {
        ElementImpl.setOnwebkitanimationiterationImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnwebkitanimationiterationImpl(long var0, long var2);

    public EventListener getOnwebkitanimationstart() {
        return EventListenerImpl.getImpl(ElementImpl.getOnwebkitanimationstartImpl(this.getPeer()));
    }

    static native long getOnwebkitanimationstartImpl(long var0);

    public void setOnwebkitanimationstart(EventListener eventListener) {
        ElementImpl.setOnwebkitanimationstartImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnwebkitanimationstartImpl(long var0, long var2);

    public EventListener getOnwebkittransitionend() {
        return EventListenerImpl.getImpl(ElementImpl.getOnwebkittransitionendImpl(this.getPeer()));
    }

    static native long getOnwebkittransitionendImpl(long var0);

    public void setOnwebkittransitionend(EventListener eventListener) {
        ElementImpl.setOnwebkittransitionendImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnwebkittransitionendImpl(long var0, long var2);

    public EventListener getOnfocusin() {
        return EventListenerImpl.getImpl(ElementImpl.getOnfocusinImpl(this.getPeer()));
    }

    static native long getOnfocusinImpl(long var0);

    public void setOnfocusin(EventListener eventListener) {
        ElementImpl.setOnfocusinImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnfocusinImpl(long var0, long var2);

    public EventListener getOnfocusout() {
        return EventListenerImpl.getImpl(ElementImpl.getOnfocusoutImpl(this.getPeer()));
    }

    static native long getOnfocusoutImpl(long var0);

    public void setOnfocusout(EventListener eventListener) {
        ElementImpl.setOnfocusoutImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnfocusoutImpl(long var0, long var2);

    public EventListener getOnbeforeload() {
        return EventListenerImpl.getImpl(ElementImpl.getOnbeforeloadImpl(this.getPeer()));
    }

    static native long getOnbeforeloadImpl(long var0);

    public void setOnbeforeload(EventListener eventListener) {
        ElementImpl.setOnbeforeloadImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnbeforeloadImpl(long var0, long var2);

    public EventListener getOnabort() {
        return EventListenerImpl.getImpl(ElementImpl.getOnabortImpl(this.getPeer()));
    }

    static native long getOnabortImpl(long var0);

    public void setOnabort(EventListener eventListener) {
        ElementImpl.setOnabortImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnabortImpl(long var0, long var2);

    public EventListener getOnblur() {
        return EventListenerImpl.getImpl(ElementImpl.getOnblurImpl(this.getPeer()));
    }

    static native long getOnblurImpl(long var0);

    public void setOnblur(EventListener eventListener) {
        ElementImpl.setOnblurImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnblurImpl(long var0, long var2);

    public EventListener getOncanplay() {
        return EventListenerImpl.getImpl(ElementImpl.getOncanplayImpl(this.getPeer()));
    }

    static native long getOncanplayImpl(long var0);

    public void setOncanplay(EventListener eventListener) {
        ElementImpl.setOncanplayImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOncanplayImpl(long var0, long var2);

    public EventListener getOncanplaythrough() {
        return EventListenerImpl.getImpl(ElementImpl.getOncanplaythroughImpl(this.getPeer()));
    }

    static native long getOncanplaythroughImpl(long var0);

    public void setOncanplaythrough(EventListener eventListener) {
        ElementImpl.setOncanplaythroughImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOncanplaythroughImpl(long var0, long var2);

    public EventListener getOnchange() {
        return EventListenerImpl.getImpl(ElementImpl.getOnchangeImpl(this.getPeer()));
    }

    static native long getOnchangeImpl(long var0);

    public void setOnchange(EventListener eventListener) {
        ElementImpl.setOnchangeImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnchangeImpl(long var0, long var2);

    public EventListener getOnclick() {
        return EventListenerImpl.getImpl(ElementImpl.getOnclickImpl(this.getPeer()));
    }

    static native long getOnclickImpl(long var0);

    public void setOnclick(EventListener eventListener) {
        ElementImpl.setOnclickImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnclickImpl(long var0, long var2);

    public EventListener getOncontextmenu() {
        return EventListenerImpl.getImpl(ElementImpl.getOncontextmenuImpl(this.getPeer()));
    }

    static native long getOncontextmenuImpl(long var0);

    public void setOncontextmenu(EventListener eventListener) {
        ElementImpl.setOncontextmenuImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOncontextmenuImpl(long var0, long var2);

    public EventListener getOndblclick() {
        return EventListenerImpl.getImpl(ElementImpl.getOndblclickImpl(this.getPeer()));
    }

    static native long getOndblclickImpl(long var0);

    public void setOndblclick(EventListener eventListener) {
        ElementImpl.setOndblclickImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOndblclickImpl(long var0, long var2);

    public EventListener getOndrag() {
        return EventListenerImpl.getImpl(ElementImpl.getOndragImpl(this.getPeer()));
    }

    static native long getOndragImpl(long var0);

    public void setOndrag(EventListener eventListener) {
        ElementImpl.setOndragImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOndragImpl(long var0, long var2);

    public EventListener getOndragend() {
        return EventListenerImpl.getImpl(ElementImpl.getOndragendImpl(this.getPeer()));
    }

    static native long getOndragendImpl(long var0);

    public void setOndragend(EventListener eventListener) {
        ElementImpl.setOndragendImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOndragendImpl(long var0, long var2);

    public EventListener getOndragenter() {
        return EventListenerImpl.getImpl(ElementImpl.getOndragenterImpl(this.getPeer()));
    }

    static native long getOndragenterImpl(long var0);

    public void setOndragenter(EventListener eventListener) {
        ElementImpl.setOndragenterImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOndragenterImpl(long var0, long var2);

    public EventListener getOndragleave() {
        return EventListenerImpl.getImpl(ElementImpl.getOndragleaveImpl(this.getPeer()));
    }

    static native long getOndragleaveImpl(long var0);

    public void setOndragleave(EventListener eventListener) {
        ElementImpl.setOndragleaveImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOndragleaveImpl(long var0, long var2);

    public EventListener getOndragover() {
        return EventListenerImpl.getImpl(ElementImpl.getOndragoverImpl(this.getPeer()));
    }

    static native long getOndragoverImpl(long var0);

    public void setOndragover(EventListener eventListener) {
        ElementImpl.setOndragoverImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOndragoverImpl(long var0, long var2);

    public EventListener getOndragstart() {
        return EventListenerImpl.getImpl(ElementImpl.getOndragstartImpl(this.getPeer()));
    }

    static native long getOndragstartImpl(long var0);

    public void setOndragstart(EventListener eventListener) {
        ElementImpl.setOndragstartImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOndragstartImpl(long var0, long var2);

    public EventListener getOndrop() {
        return EventListenerImpl.getImpl(ElementImpl.getOndropImpl(this.getPeer()));
    }

    static native long getOndropImpl(long var0);

    public void setOndrop(EventListener eventListener) {
        ElementImpl.setOndropImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOndropImpl(long var0, long var2);

    public EventListener getOndurationchange() {
        return EventListenerImpl.getImpl(ElementImpl.getOndurationchangeImpl(this.getPeer()));
    }

    static native long getOndurationchangeImpl(long var0);

    public void setOndurationchange(EventListener eventListener) {
        ElementImpl.setOndurationchangeImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOndurationchangeImpl(long var0, long var2);

    public EventListener getOnemptied() {
        return EventListenerImpl.getImpl(ElementImpl.getOnemptiedImpl(this.getPeer()));
    }

    static native long getOnemptiedImpl(long var0);

    public void setOnemptied(EventListener eventListener) {
        ElementImpl.setOnemptiedImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnemptiedImpl(long var0, long var2);

    public EventListener getOnended() {
        return EventListenerImpl.getImpl(ElementImpl.getOnendedImpl(this.getPeer()));
    }

    static native long getOnendedImpl(long var0);

    public void setOnended(EventListener eventListener) {
        ElementImpl.setOnendedImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnendedImpl(long var0, long var2);

    public EventListener getOnerror() {
        return EventListenerImpl.getImpl(ElementImpl.getOnerrorImpl(this.getPeer()));
    }

    static native long getOnerrorImpl(long var0);

    public void setOnerror(EventListener eventListener) {
        ElementImpl.setOnerrorImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnerrorImpl(long var0, long var2);

    public EventListener getOnfocus() {
        return EventListenerImpl.getImpl(ElementImpl.getOnfocusImpl(this.getPeer()));
    }

    static native long getOnfocusImpl(long var0);

    public void setOnfocus(EventListener eventListener) {
        ElementImpl.setOnfocusImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnfocusImpl(long var0, long var2);

    public EventListener getOninput() {
        return EventListenerImpl.getImpl(ElementImpl.getOninputImpl(this.getPeer()));
    }

    static native long getOninputImpl(long var0);

    public void setOninput(EventListener eventListener) {
        ElementImpl.setOninputImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOninputImpl(long var0, long var2);

    public EventListener getOninvalid() {
        return EventListenerImpl.getImpl(ElementImpl.getOninvalidImpl(this.getPeer()));
    }

    static native long getOninvalidImpl(long var0);

    public void setOninvalid(EventListener eventListener) {
        ElementImpl.setOninvalidImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOninvalidImpl(long var0, long var2);

    public EventListener getOnkeydown() {
        return EventListenerImpl.getImpl(ElementImpl.getOnkeydownImpl(this.getPeer()));
    }

    static native long getOnkeydownImpl(long var0);

    public void setOnkeydown(EventListener eventListener) {
        ElementImpl.setOnkeydownImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnkeydownImpl(long var0, long var2);

    public EventListener getOnkeypress() {
        return EventListenerImpl.getImpl(ElementImpl.getOnkeypressImpl(this.getPeer()));
    }

    static native long getOnkeypressImpl(long var0);

    public void setOnkeypress(EventListener eventListener) {
        ElementImpl.setOnkeypressImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnkeypressImpl(long var0, long var2);

    public EventListener getOnkeyup() {
        return EventListenerImpl.getImpl(ElementImpl.getOnkeyupImpl(this.getPeer()));
    }

    static native long getOnkeyupImpl(long var0);

    public void setOnkeyup(EventListener eventListener) {
        ElementImpl.setOnkeyupImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnkeyupImpl(long var0, long var2);

    public EventListener getOnload() {
        return EventListenerImpl.getImpl(ElementImpl.getOnloadImpl(this.getPeer()));
    }

    static native long getOnloadImpl(long var0);

    public void setOnload(EventListener eventListener) {
        ElementImpl.setOnloadImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnloadImpl(long var0, long var2);

    public EventListener getOnloadeddata() {
        return EventListenerImpl.getImpl(ElementImpl.getOnloadeddataImpl(this.getPeer()));
    }

    static native long getOnloadeddataImpl(long var0);

    public void setOnloadeddata(EventListener eventListener) {
        ElementImpl.setOnloadeddataImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnloadeddataImpl(long var0, long var2);

    public EventListener getOnloadedmetadata() {
        return EventListenerImpl.getImpl(ElementImpl.getOnloadedmetadataImpl(this.getPeer()));
    }

    static native long getOnloadedmetadataImpl(long var0);

    public void setOnloadedmetadata(EventListener eventListener) {
        ElementImpl.setOnloadedmetadataImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnloadedmetadataImpl(long var0, long var2);

    public EventListener getOnloadstart() {
        return EventListenerImpl.getImpl(ElementImpl.getOnloadstartImpl(this.getPeer()));
    }

    static native long getOnloadstartImpl(long var0);

    public void setOnloadstart(EventListener eventListener) {
        ElementImpl.setOnloadstartImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnloadstartImpl(long var0, long var2);

    public EventListener getOnmousedown() {
        return EventListenerImpl.getImpl(ElementImpl.getOnmousedownImpl(this.getPeer()));
    }

    static native long getOnmousedownImpl(long var0);

    public void setOnmousedown(EventListener eventListener) {
        ElementImpl.setOnmousedownImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnmousedownImpl(long var0, long var2);

    public EventListener getOnmouseenter() {
        return EventListenerImpl.getImpl(ElementImpl.getOnmouseenterImpl(this.getPeer()));
    }

    static native long getOnmouseenterImpl(long var0);

    public void setOnmouseenter(EventListener eventListener) {
        ElementImpl.setOnmouseenterImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnmouseenterImpl(long var0, long var2);

    public EventListener getOnmouseleave() {
        return EventListenerImpl.getImpl(ElementImpl.getOnmouseleaveImpl(this.getPeer()));
    }

    static native long getOnmouseleaveImpl(long var0);

    public void setOnmouseleave(EventListener eventListener) {
        ElementImpl.setOnmouseleaveImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnmouseleaveImpl(long var0, long var2);

    public EventListener getOnmousemove() {
        return EventListenerImpl.getImpl(ElementImpl.getOnmousemoveImpl(this.getPeer()));
    }

    static native long getOnmousemoveImpl(long var0);

    public void setOnmousemove(EventListener eventListener) {
        ElementImpl.setOnmousemoveImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnmousemoveImpl(long var0, long var2);

    public EventListener getOnmouseout() {
        return EventListenerImpl.getImpl(ElementImpl.getOnmouseoutImpl(this.getPeer()));
    }

    static native long getOnmouseoutImpl(long var0);

    public void setOnmouseout(EventListener eventListener) {
        ElementImpl.setOnmouseoutImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnmouseoutImpl(long var0, long var2);

    public EventListener getOnmouseover() {
        return EventListenerImpl.getImpl(ElementImpl.getOnmouseoverImpl(this.getPeer()));
    }

    static native long getOnmouseoverImpl(long var0);

    public void setOnmouseover(EventListener eventListener) {
        ElementImpl.setOnmouseoverImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnmouseoverImpl(long var0, long var2);

    public EventListener getOnmouseup() {
        return EventListenerImpl.getImpl(ElementImpl.getOnmouseupImpl(this.getPeer()));
    }

    static native long getOnmouseupImpl(long var0);

    public void setOnmouseup(EventListener eventListener) {
        ElementImpl.setOnmouseupImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnmouseupImpl(long var0, long var2);

    public EventListener getOnmousewheel() {
        return EventListenerImpl.getImpl(ElementImpl.getOnmousewheelImpl(this.getPeer()));
    }

    static native long getOnmousewheelImpl(long var0);

    public void setOnmousewheel(EventListener eventListener) {
        ElementImpl.setOnmousewheelImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnmousewheelImpl(long var0, long var2);

    public EventListener getOnpause() {
        return EventListenerImpl.getImpl(ElementImpl.getOnpauseImpl(this.getPeer()));
    }

    static native long getOnpauseImpl(long var0);

    public void setOnpause(EventListener eventListener) {
        ElementImpl.setOnpauseImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnpauseImpl(long var0, long var2);

    public EventListener getOnplay() {
        return EventListenerImpl.getImpl(ElementImpl.getOnplayImpl(this.getPeer()));
    }

    static native long getOnplayImpl(long var0);

    public void setOnplay(EventListener eventListener) {
        ElementImpl.setOnplayImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnplayImpl(long var0, long var2);

    public EventListener getOnplaying() {
        return EventListenerImpl.getImpl(ElementImpl.getOnplayingImpl(this.getPeer()));
    }

    static native long getOnplayingImpl(long var0);

    public void setOnplaying(EventListener eventListener) {
        ElementImpl.setOnplayingImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnplayingImpl(long var0, long var2);

    public EventListener getOnprogress() {
        return EventListenerImpl.getImpl(ElementImpl.getOnprogressImpl(this.getPeer()));
    }

    static native long getOnprogressImpl(long var0);

    public void setOnprogress(EventListener eventListener) {
        ElementImpl.setOnprogressImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnprogressImpl(long var0, long var2);

    public EventListener getOnratechange() {
        return EventListenerImpl.getImpl(ElementImpl.getOnratechangeImpl(this.getPeer()));
    }

    static native long getOnratechangeImpl(long var0);

    public void setOnratechange(EventListener eventListener) {
        ElementImpl.setOnratechangeImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnratechangeImpl(long var0, long var2);

    public EventListener getOnreset() {
        return EventListenerImpl.getImpl(ElementImpl.getOnresetImpl(this.getPeer()));
    }

    static native long getOnresetImpl(long var0);

    public void setOnreset(EventListener eventListener) {
        ElementImpl.setOnresetImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnresetImpl(long var0, long var2);

    public EventListener getOnresize() {
        return EventListenerImpl.getImpl(ElementImpl.getOnresizeImpl(this.getPeer()));
    }

    static native long getOnresizeImpl(long var0);

    public void setOnresize(EventListener eventListener) {
        ElementImpl.setOnresizeImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnresizeImpl(long var0, long var2);

    public EventListener getOnscroll() {
        return EventListenerImpl.getImpl(ElementImpl.getOnscrollImpl(this.getPeer()));
    }

    static native long getOnscrollImpl(long var0);

    public void setOnscroll(EventListener eventListener) {
        ElementImpl.setOnscrollImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnscrollImpl(long var0, long var2);

    public EventListener getOnseeked() {
        return EventListenerImpl.getImpl(ElementImpl.getOnseekedImpl(this.getPeer()));
    }

    static native long getOnseekedImpl(long var0);

    public void setOnseeked(EventListener eventListener) {
        ElementImpl.setOnseekedImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnseekedImpl(long var0, long var2);

    public EventListener getOnseeking() {
        return EventListenerImpl.getImpl(ElementImpl.getOnseekingImpl(this.getPeer()));
    }

    static native long getOnseekingImpl(long var0);

    public void setOnseeking(EventListener eventListener) {
        ElementImpl.setOnseekingImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnseekingImpl(long var0, long var2);

    public EventListener getOnselect() {
        return EventListenerImpl.getImpl(ElementImpl.getOnselectImpl(this.getPeer()));
    }

    static native long getOnselectImpl(long var0);

    public void setOnselect(EventListener eventListener) {
        ElementImpl.setOnselectImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnselectImpl(long var0, long var2);

    public EventListener getOnstalled() {
        return EventListenerImpl.getImpl(ElementImpl.getOnstalledImpl(this.getPeer()));
    }

    static native long getOnstalledImpl(long var0);

    public void setOnstalled(EventListener eventListener) {
        ElementImpl.setOnstalledImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnstalledImpl(long var0, long var2);

    public EventListener getOnsubmit() {
        return EventListenerImpl.getImpl(ElementImpl.getOnsubmitImpl(this.getPeer()));
    }

    static native long getOnsubmitImpl(long var0);

    public void setOnsubmit(EventListener eventListener) {
        ElementImpl.setOnsubmitImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnsubmitImpl(long var0, long var2);

    public EventListener getOnsuspend() {
        return EventListenerImpl.getImpl(ElementImpl.getOnsuspendImpl(this.getPeer()));
    }

    static native long getOnsuspendImpl(long var0);

    public void setOnsuspend(EventListener eventListener) {
        ElementImpl.setOnsuspendImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnsuspendImpl(long var0, long var2);

    public EventListener getOntimeupdate() {
        return EventListenerImpl.getImpl(ElementImpl.getOntimeupdateImpl(this.getPeer()));
    }

    static native long getOntimeupdateImpl(long var0);

    public void setOntimeupdate(EventListener eventListener) {
        ElementImpl.setOntimeupdateImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOntimeupdateImpl(long var0, long var2);

    public EventListener getOnvolumechange() {
        return EventListenerImpl.getImpl(ElementImpl.getOnvolumechangeImpl(this.getPeer()));
    }

    static native long getOnvolumechangeImpl(long var0);

    public void setOnvolumechange(EventListener eventListener) {
        ElementImpl.setOnvolumechangeImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnvolumechangeImpl(long var0, long var2);

    public EventListener getOnwaiting() {
        return EventListenerImpl.getImpl(ElementImpl.getOnwaitingImpl(this.getPeer()));
    }

    static native long getOnwaitingImpl(long var0);

    public void setOnwaiting(EventListener eventListener) {
        ElementImpl.setOnwaitingImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnwaitingImpl(long var0, long var2);

    public EventListener getOnsearch() {
        return EventListenerImpl.getImpl(ElementImpl.getOnsearchImpl(this.getPeer()));
    }

    static native long getOnsearchImpl(long var0);

    public void setOnsearch(EventListener eventListener) {
        ElementImpl.setOnsearchImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnsearchImpl(long var0, long var2);

    public EventListener getOnwheel() {
        return EventListenerImpl.getImpl(ElementImpl.getOnwheelImpl(this.getPeer()));
    }

    static native long getOnwheelImpl(long var0);

    public void setOnwheel(EventListener eventListener) {
        ElementImpl.setOnwheelImpl(this.getPeer(), EventListenerImpl.getPeer(eventListener));
    }

    static native void setOnwheelImpl(long var0, long var2);

    public Element getPreviousElementSibling() {
        return ElementImpl.getImpl(ElementImpl.getPreviousElementSiblingImpl(this.getPeer()));
    }

    static native long getPreviousElementSiblingImpl(long var0);

    public Element getNextElementSibling() {
        return ElementImpl.getImpl(ElementImpl.getNextElementSiblingImpl(this.getPeer()));
    }

    static native long getNextElementSiblingImpl(long var0);

    public HTMLCollection getChildren() {
        return HTMLCollectionImpl.getImpl(ElementImpl.getChildrenImpl(this.getPeer()));
    }

    static native long getChildrenImpl(long var0);

    public Element getFirstElementChild() {
        return ElementImpl.getImpl(ElementImpl.getFirstElementChildImpl(this.getPeer()));
    }

    static native long getFirstElementChildImpl(long var0);

    public Element getLastElementChild() {
        return ElementImpl.getImpl(ElementImpl.getLastElementChildImpl(this.getPeer()));
    }

    static native long getLastElementChildImpl(long var0);

    public int getChildElementCount() {
        return ElementImpl.getChildElementCountImpl(this.getPeer());
    }

    static native int getChildElementCountImpl(long var0);

    @Override
    public String getAttribute(String string) {
        return ElementImpl.getAttributeImpl(this.getPeer(), string);
    }

    static native String getAttributeImpl(long var0, String var2);

    @Override
    public void setAttribute(String string, String string2) throws DOMException {
        ElementImpl.setAttributeImpl(this.getPeer(), string, string2);
    }

    static native void setAttributeImpl(long var0, String var2, String var3);

    @Override
    public void removeAttribute(String string) {
        ElementImpl.removeAttributeImpl(this.getPeer(), string);
    }

    static native void removeAttributeImpl(long var0, String var2);

    @Override
    public Attr getAttributeNode(String string) {
        return AttrImpl.getImpl(ElementImpl.getAttributeNodeImpl(this.getPeer(), string));
    }

    static native long getAttributeNodeImpl(long var0, String var2);

    @Override
    public Attr setAttributeNode(Attr attr) throws DOMException {
        return AttrImpl.getImpl(ElementImpl.setAttributeNodeImpl(this.getPeer(), AttrImpl.getPeer(attr)));
    }

    static native long setAttributeNodeImpl(long var0, long var2);

    @Override
    public Attr removeAttributeNode(Attr attr) throws DOMException {
        return AttrImpl.getImpl(ElementImpl.removeAttributeNodeImpl(this.getPeer(), AttrImpl.getPeer(attr)));
    }

    static native long removeAttributeNodeImpl(long var0, long var2);

    @Override
    public NodeList getElementsByTagName(String string) {
        return NodeListImpl.getImpl(ElementImpl.getElementsByTagNameImpl(this.getPeer(), string));
    }

    static native long getElementsByTagNameImpl(long var0, String var2);

    @Override
    public boolean hasAttributes() {
        return ElementImpl.hasAttributesImpl(this.getPeer());
    }

    static native boolean hasAttributesImpl(long var0);

    @Override
    public String getAttributeNS(String string, String string2) {
        return ElementImpl.getAttributeNSImpl(this.getPeer(), string, string2);
    }

    static native String getAttributeNSImpl(long var0, String var2, String var3);

    @Override
    public void setAttributeNS(String string, String string2, String string3) throws DOMException {
        ElementImpl.setAttributeNSImpl(this.getPeer(), string, string2, string3);
    }

    static native void setAttributeNSImpl(long var0, String var2, String var3, String var4);

    @Override
    public void removeAttributeNS(String string, String string2) {
        ElementImpl.removeAttributeNSImpl(this.getPeer(), string, string2);
    }

    static native void removeAttributeNSImpl(long var0, String var2, String var3);

    @Override
    public NodeList getElementsByTagNameNS(String string, String string2) {
        return NodeListImpl.getImpl(ElementImpl.getElementsByTagNameNSImpl(this.getPeer(), string, string2));
    }

    static native long getElementsByTagNameNSImpl(long var0, String var2, String var3);

    @Override
    public Attr getAttributeNodeNS(String string, String string2) {
        return AttrImpl.getImpl(ElementImpl.getAttributeNodeNSImpl(this.getPeer(), string, string2));
    }

    static native long getAttributeNodeNSImpl(long var0, String var2, String var3);

    @Override
    public Attr setAttributeNodeNS(Attr attr) throws DOMException {
        return AttrImpl.getImpl(ElementImpl.setAttributeNodeNSImpl(this.getPeer(), AttrImpl.getPeer(attr)));
    }

    static native long setAttributeNodeNSImpl(long var0, long var2);

    @Override
    public boolean hasAttribute(String string) {
        return ElementImpl.hasAttributeImpl(this.getPeer(), string);
    }

    static native boolean hasAttributeImpl(long var0, String var2);

    @Override
    public boolean hasAttributeNS(String string, String string2) {
        return ElementImpl.hasAttributeNSImpl(this.getPeer(), string, string2);
    }

    static native boolean hasAttributeNSImpl(long var0, String var2, String var3);

    public void focus() {
        ElementImpl.focusImpl(this.getPeer());
    }

    static native void focusImpl(long var0);

    public void blur() {
        ElementImpl.blurImpl(this.getPeer());
    }

    static native void blurImpl(long var0);

    public void scrollIntoView(boolean bl) {
        ElementImpl.scrollIntoViewImpl(this.getPeer(), bl);
    }

    static native void scrollIntoViewImpl(long var0, boolean var2);

    public void scrollIntoViewIfNeeded(boolean bl) {
        ElementImpl.scrollIntoViewIfNeededImpl(this.getPeer(), bl);
    }

    static native void scrollIntoViewIfNeededImpl(long var0, boolean var2);

    public void scrollByLines(int n) {
        ElementImpl.scrollByLinesImpl(this.getPeer(), n);
    }

    static native void scrollByLinesImpl(long var0, int var2);

    public void scrollByPages(int n) {
        ElementImpl.scrollByPagesImpl(this.getPeer(), n);
    }

    static native void scrollByPagesImpl(long var0, int var2);

    public HTMLCollection getElementsByClassName(String string) {
        return HTMLCollectionImpl.getImpl(ElementImpl.getElementsByClassNameImpl(this.getPeer(), string));
    }

    static native long getElementsByClassNameImpl(long var0, String var2);

    public boolean matches(String string) throws DOMException {
        return ElementImpl.matchesImpl(this.getPeer(), string);
    }

    static native boolean matchesImpl(long var0, String var2);

    public Element closest(String string) throws DOMException {
        return ElementImpl.getImpl(ElementImpl.closestImpl(this.getPeer(), string));
    }

    static native long closestImpl(long var0, String var2);

    public boolean webkitMatchesSelector(String string) throws DOMException {
        return ElementImpl.webkitMatchesSelectorImpl(this.getPeer(), string);
    }

    static native boolean webkitMatchesSelectorImpl(long var0, String var2);

    public void webkitRequestFullScreen(short s) {
        ElementImpl.webkitRequestFullScreenImpl(this.getPeer(), s);
    }

    static native void webkitRequestFullScreenImpl(long var0, short var2);

    public void webkitRequestFullscreen() {
        ElementImpl.webkitRequestFullscreenImpl(this.getPeer());
    }

    static native void webkitRequestFullscreenImpl(long var0);

    public void remove() throws DOMException {
        ElementImpl.removeImpl(this.getPeer());
    }

    static native void removeImpl(long var0);

    public Element querySelector(String string) throws DOMException {
        return ElementImpl.getImpl(ElementImpl.querySelectorImpl(this.getPeer(), string));
    }

    static native long querySelectorImpl(long var0, String var2);

    public NodeList querySelectorAll(String string) throws DOMException {
        return NodeListImpl.getImpl(ElementImpl.querySelectorAllImpl(this.getPeer(), string));
    }

    static native long querySelectorAllImpl(long var0, String var2);

    @Override
    public void setIdAttribute(String string, boolean bl) throws DOMException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setIdAttributeNode(Attr attr, boolean bl) throws DOMException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TypeInfo getSchemaTypeInfo() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setIdAttributeNS(String string, String string2, boolean bl) throws DOMException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

