/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.webkit;

import com.sun.javafx.logging.PlatformLogger;
import com.sun.javafx.scene.SceneHelper;
import com.sun.javafx.scene.input.ExtendedInputMethodRequests;
import com.sun.webkit.InputMethodClient;
import com.sun.webkit.Invoker;
import com.sun.webkit.WebPage;
import com.sun.webkit.event.WCInputMethodEvent;
import com.sun.webkit.graphics.WCPoint;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javafx.geometry.Point2D;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.InputMethodHighlight;
import javafx.scene.input.InputMethodTextRun;
import javafx.scene.web.WebView;

public final class InputMethodClientImpl
implements InputMethodClient,
ExtendedInputMethodRequests {
    private static final PlatformLogger log = PlatformLogger.getLogger(InputMethodClientImpl.class.getName());
    private final WeakReference<WebView> wvRef;
    private final WebPage webPage;
    private boolean state;

    public InputMethodClientImpl(WebView webView, WebPage webPage) {
        this.wvRef = new WeakReference<WebView>(webView);
        this.webPage = webPage;
        if (webPage != null) {
            webPage.setInputMethodClient(this);
        }
    }

    @Override
    public void activateInputMethods(boolean bl) {
        WebView webView = (WebView)this.wvRef.get();
        if (webView != null && webView.getScene() != null) {
            SceneHelper.enableInputMethodEvents(webView.getScene(), bl);
        }
        this.state = bl;
    }

    public boolean getInputMethodState() {
        return this.state;
    }

    public static WCInputMethodEvent convertToWCInputMethodEvent(InputMethodEvent inputMethodEvent) {
        ArrayList<Integer> arrayList = new ArrayList<Integer>();
        StringBuilder stringBuilder = new StringBuilder();
        int n = 0;
        for (InputMethodTextRun object2 : inputMethodEvent.getComposed()) {
            String i = object2.getText();
            InputMethodHighlight inputMethodHighlight = object2.getHighlight();
            arrayList.add(n);
            arrayList.add(n + i.length());
            arrayList.add(inputMethodHighlight == InputMethodHighlight.SELECTED_CONVERTED || inputMethodHighlight == InputMethodHighlight.SELECTED_RAW ? 1 : 0);
            n += i.length();
            stringBuilder.append(i);
        }
        int n2 = arrayList.size();
        if (n2 == 0) {
            arrayList.add(0);
            arrayList.add(n);
            arrayList.add(0);
            n2 = arrayList.size();
        }
        int[] nArray = new int[n2];
        for (int i = 0; i < n2; ++i) {
            nArray[i] = (Integer)arrayList.get(i);
        }
        return new WCInputMethodEvent(inputMethodEvent.getCommitted(), stringBuilder.toString(), nArray, inputMethodEvent.getCaretPosition());
    }

    @Override
    public Point2D getTextLocation(int n) {
        FutureTask<Point2D> futureTask = new FutureTask<Point2D>(() -> {
            int[] nArray = this.webPage.getClientTextLocation(n);
            WCPoint wCPoint = this.webPage.getPageClient().windowToScreen(new WCPoint(nArray[0], nArray[1] + nArray[3]));
            return new Point2D(wCPoint.getIntX(), wCPoint.getIntY());
        });
        Invoker.getInvoker().invokeOnEventThread(futureTask);
        Point2D point2D = null;
        try {
            point2D = futureTask.get();
        }
        catch (ExecutionException executionException) {
            log.severe("InputMethodClientImpl.getTextLocation " + executionException);
        }
        catch (InterruptedException interruptedException) {
            log.severe("InputMethodClientImpl.getTextLocation InterruptedException" + interruptedException);
        }
        return point2D;
    }

    @Override
    public int getLocationOffset(int n, int n2) {
        FutureTask<Integer> futureTask = new FutureTask<Integer>(() -> {
            WCPoint wCPoint = this.webPage.getPageClient().windowToScreen(new WCPoint(0.0f, 0.0f));
            return this.webPage.getClientLocationOffset(n - wCPoint.getIntX(), n2 - wCPoint.getIntY());
        });
        Invoker.getInvoker().invokeOnEventThread(futureTask);
        int n3 = 0;
        try {
            n3 = futureTask.get();
        }
        catch (ExecutionException executionException) {
            log.severe("InputMethodClientImpl.getLocationOffset " + executionException);
        }
        catch (InterruptedException interruptedException) {
            log.severe("InputMethodClientImpl.getTextLocation InterruptedException" + interruptedException);
        }
        return n3;
    }

    @Override
    public void cancelLatestCommittedText() {
    }

    @Override
    public String getSelectedText() {
        return this.webPage.getClientSelectedText();
    }

    @Override
    public int getInsertPositionOffset() {
        return this.webPage.getClientInsertPositionOffset();
    }

    @Override
    public String getCommittedText(int n, int n2) {
        try {
            return this.webPage.getClientCommittedText().substring(n, n2);
        }
        catch (StringIndexOutOfBoundsException stringIndexOutOfBoundsException) {
            throw new IllegalArgumentException(stringIndexOutOfBoundsException);
        }
    }

    @Override
    public int getCommittedTextLength() {
        return this.webPage.getClientCommittedTextLength();
    }
}

