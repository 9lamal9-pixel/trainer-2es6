/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.skin;

import com.sun.javafx.PlatformUtil;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.control.ContextMenuContent;
import com.sun.javafx.scene.control.behavior.TextBinding;
import com.sun.javafx.scene.text.FontHelper;
import com.sun.javafx.scene.text.TextLayout;
import com.sun.javafx.tk.Toolkit;
import java.net.URL;
import java.text.Bidi;
import java.util.Locale;
import java.util.function.Consumer;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.OverrunStyle;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.Mnemonic;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

public class Utils {
    static final Text helper = new Text();
    static final double DEFAULT_WRAPPING_WIDTH = helper.getWrappingWidth();
    static final double DEFAULT_LINE_SPACING = helper.getLineSpacing();
    static final String DEFAULT_TEXT = helper.getText();
    static final TextBoundsType DEFAULT_BOUNDS_TYPE = helper.getBoundsType();
    static final TextLayout layout = Toolkit.getToolkit().getTextLayoutFactory().createLayout();

    public static double getAscent(Font font, TextBoundsType textBoundsType) {
        layout.setContent("", FontHelper.getNativeFont(font));
        layout.setWrapWidth(0.0f);
        layout.setLineSpacing(0.0f);
        if (textBoundsType == TextBoundsType.LOGICAL_VERTICAL_CENTER) {
            layout.setBoundsType(16384);
        } else {
            layout.setBoundsType(0);
        }
        return -layout.getBounds().getMinY();
    }

    public static double getLineHeight(Font font, TextBoundsType textBoundsType) {
        layout.setContent("", FontHelper.getNativeFont(font));
        layout.setWrapWidth(0.0f);
        layout.setLineSpacing(0.0f);
        if (textBoundsType == TextBoundsType.LOGICAL_VERTICAL_CENTER) {
            layout.setBoundsType(16384);
        } else {
            layout.setBoundsType(0);
        }
        return layout.getLines()[0].getBounds().getHeight();
    }

    public static double computeTextWidth(Font font, String string, double d) {
        layout.setContent(string != null ? string : "", FontHelper.getNativeFont(font));
        layout.setWrapWidth((float)d);
        return layout.getBounds().getWidth();
    }

    public static double computeTextHeight(Font font, String string, double d, TextBoundsType textBoundsType) {
        return Utils.computeTextHeight(font, string, d, 0.0, textBoundsType);
    }

    public static double computeTextHeight(Font font, String string, double d, double d2, TextBoundsType textBoundsType) {
        layout.setContent(string != null ? string : "", FontHelper.getNativeFont(font));
        layout.setWrapWidth((float)d);
        layout.setLineSpacing((float)d2);
        if (textBoundsType == TextBoundsType.LOGICAL_VERTICAL_CENTER) {
            layout.setBoundsType(16384);
        } else {
            layout.setBoundsType(0);
        }
        return layout.getBounds().getHeight();
    }

    public static Point2D computeMnemonicPosition(Font font, String string, int n, double d, double d2) {
        int n2;
        if (font == null || string == null || n < 0 || n > string.length()) {
            return null;
        }
        layout.setContent(string, FontHelper.getNativeFont(font));
        layout.setWrapWidth((float)d);
        layout.setLineSpacing((float)d2);
        int n3 = 0;
        int n4 = layout.getLines().length;
        for (n2 = 0; n2 < n4; ++n2) {
            int n5 = layout.getLines()[n2].getLength();
            if (n >= n3 && n < n3 + n5) break;
            n3 += n5;
        }
        double d3 = layout.getBounds().getHeight() / (float)n4;
        double d4 = Utils.computeTextWidth(font, string.substring(n3, n), 0.0);
        double d5 = d3 * (double)(n2 + 1);
        if (n2 + 1 != n4) {
            d5 -= d2 / 2.0;
        }
        return new Point2D(d4, d5);
    }

    public static int computeTruncationIndex(Font font, String string, double d) {
        helper.setText(string);
        helper.setFont(font);
        helper.setWrappingWidth(0.0);
        helper.setLineSpacing(0.0);
        Bounds bounds = helper.getLayoutBounds();
        Point2D point2D = new Point2D(d - 2.0, bounds.getMinY() + bounds.getHeight() / 2.0);
        int n = helper.hitTest(point2D).getCharIndex();
        helper.setWrappingWidth(DEFAULT_WRAPPING_WIDTH);
        helper.setLineSpacing(DEFAULT_LINE_SPACING);
        helper.setText(DEFAULT_TEXT);
        return n;
    }

    public static String computeClippedText(Font font, String string, double d, OverrunStyle overrunStyle, String string2) {
        char c;
        String string3;
        if (font == null) {
            throw new IllegalArgumentException("Must specify a font");
        }
        OverrunStyle overrunStyle2 = overrunStyle == null || overrunStyle == OverrunStyle.CLIP ? OverrunStyle.ELLIPSIS : overrunStyle;
        String string4 = string3 = overrunStyle == OverrunStyle.CLIP ? "" : string2;
        if (string == null || "".equals(string)) {
            return string;
        }
        double d2 = Utils.computeTextWidth(font, string, 0.0);
        if (d2 - d < (double)0.001f) {
            return string;
        }
        double d3 = Utils.computeTextWidth(font, string3, 0.0);
        double d4 = d - d3;
        if (d < d3) {
            return "";
        }
        if (overrunStyle2 == OverrunStyle.ELLIPSIS || overrunStyle2 == OverrunStyle.WORD_ELLIPSIS || overrunStyle2 == OverrunStyle.LEADING_ELLIPSIS || overrunStyle2 == OverrunStyle.LEADING_WORD_ELLIPSIS) {
            String string5;
            int n;
            boolean bl;
            boolean bl2 = bl = overrunStyle2 == OverrunStyle.WORD_ELLIPSIS || overrunStyle2 == OverrunStyle.LEADING_WORD_ELLIPSIS;
            if (overrunStyle2 == OverrunStyle.ELLIPSIS && !new Bidi(string, 0).isMixed()) {
                int n2 = Utils.computeTruncationIndex(font, string, d - d3);
                if (n2 < 0 || n2 >= string.length()) {
                    return string;
                }
                return string.substring(0, n2) + string3;
            }
            double d5 = 0.0;
            int n3 = -1;
            int n4 = 0;
            int n5 = overrunStyle2 == OverrunStyle.LEADING_ELLIPSIS || overrunStyle2 == OverrunStyle.LEADING_WORD_ELLIPSIS ? string.length() - 1 : 0;
            int n6 = n5 == 0 ? string.length() - 1 : 0;
            int n7 = n = n5 == 0 ? 1 : -1;
            boolean bl3 = n5 == 0 ? n5 > n6 : n5 < n6;
            int n8 = n5;
            while (!bl3) {
                n4 = n8;
                char c2 = string.charAt(n4);
                d5 = Utils.computeTextWidth(font, n5 == 0 ? string.substring(0, n8 + 1) : string.substring(n8, n5 + 1), 0.0);
                if (Character.isWhitespace(c2)) {
                    n3 = n4;
                }
                if (d5 > d4) break;
                bl3 = n5 == 0 ? n8 >= n6 : n8 <= n6;
                n8 += n;
            }
            int n9 = n8 = !bl || n3 == -1 ? 1 : 0;
            String string6 = n5 == 0 ? string.substring(0, n8 != 0 ? n4 : n3) : (string5 = string.substring((n8 != 0 ? n4 : n3) + 1));
            assert (!string.equals(string5));
            if (overrunStyle2 == OverrunStyle.ELLIPSIS || overrunStyle2 == OverrunStyle.WORD_ELLIPSIS) {
                return string5 + string3;
            }
            return string3 + string5;
        }
        int n = 0;
        int n10 = 0;
        int n11 = -1;
        int n12 = -1;
        n = -1;
        n10 = -1;
        double d6 = 0.0;
        for (int i = 0; i <= string.length() - 1 && !((d6 += Utils.computeTextWidth(font, "" + (c = string.charAt(i)), 0.0)) > d4); ++i) {
            int n13;
            n = i;
            if (Character.isWhitespace(c)) {
                n11 = n;
            }
            if ((d6 += Utils.computeTextWidth(font, "" + (c = string.charAt(n13 = string.length() - 1 - i)), 0.0)) > d4) break;
            n10 = n13;
            if (!Character.isWhitespace(c)) continue;
            n12 = n10;
        }
        if (n < 0) {
            return string3;
        }
        if (overrunStyle2 == OverrunStyle.CENTER_ELLIPSIS) {
            if (n10 < 0) {
                return string.substring(0, n + 1) + string3;
            }
            return string.substring(0, n + 1) + string3 + string.substring(n10);
        }
        boolean bl = Character.isWhitespace(string.charAt(n + 1));
        int n14 = n11 == -1 || bl ? n + 1 : n11;
        String string7 = string.substring(0, n14);
        if (n10 < 0) {
            return string7 + string3;
        }
        c = (char)(Character.isWhitespace(string.charAt(n10 - 1)) ? 1 : 0);
        n14 = n12 == -1 || c != '\u0000' ? n10 : n12 + 1;
        String string8 = string.substring(n14);
        return string7 + string3 + string8;
    }

    public static String computeClippedWrappedText(Font font, String string, double d, double d2, OverrunStyle overrunStyle, String string2, TextBoundsType textBoundsType) {
        Point2D point2D;
        int n;
        if (font == null) {
            throw new IllegalArgumentException("Must specify a font");
        }
        String string3 = overrunStyle == OverrunStyle.CLIP ? "" : string2;
        int n2 = string3.length();
        double d3 = Utils.computeTextWidth(font, string3, 0.0);
        double d4 = Utils.computeTextHeight(font, string3, 0.0, textBoundsType);
        if (d < d3 || d2 < d4) {
            return string;
        }
        helper.setText(string);
        helper.setFont(font);
        helper.setWrappingWidth((int)Math.ceil(d));
        helper.setBoundsType(textBoundsType);
        helper.setLineSpacing(0.0);
        boolean bl = overrunStyle == OverrunStyle.LEADING_ELLIPSIS || overrunStyle == OverrunStyle.LEADING_WORD_ELLIPSIS;
        boolean bl2 = overrunStyle == OverrunStyle.CENTER_ELLIPSIS || overrunStyle == OverrunStyle.CENTER_WORD_ELLIPSIS;
        boolean bl3 = !bl && !bl2;
        boolean bl4 = overrunStyle == OverrunStyle.WORD_ELLIPSIS || overrunStyle == OverrunStyle.LEADING_WORD_ELLIPSIS || overrunStyle == OverrunStyle.CENTER_WORD_ELLIPSIS;
        Object object = string;
        int n3 = object != null ? ((String)object).length() : 0;
        int n4 = -1;
        Point2D point2D2 = null;
        if (bl2) {
            point2D2 = new Point2D((d - d3) / 2.0, d2 / 2.0 - helper.getBaselineOffset());
        }
        if ((n = helper.hitTest(point2D = new Point2D(0.0, d2 - helper.getBaselineOffset())).getCharIndex()) >= n3) {
            helper.setBoundsType(TextBoundsType.LOGICAL);
            return string;
        }
        if (bl2) {
            n = helper.hitTest(point2D2).getCharIndex();
        }
        if (n > 0 && n < n3) {
            int n5;
            int n6;
            if (bl2 || bl3) {
                n6 = n;
                if (bl2) {
                    if (bl4) {
                        n5 = Utils.lastBreakCharIndex(string, n6 + 1);
                        if (n5 >= 0) {
                            n6 = n5 + 1;
                        } else {
                            n5 = Utils.firstBreakCharIndex(string, n6);
                            if (n5 >= 0) {
                                n6 = n5 + 1;
                            }
                        }
                    }
                    n4 = n6 + n2;
                }
                object = ((String)object).substring(0, n6) + string3;
            }
            if (bl || bl2) {
                n6 = Math.max(0, n3 - n - 10);
                if (n6 > 0 && bl4) {
                    n5 = Utils.lastBreakCharIndex(string, n6 + 1);
                    if (n5 >= 0) {
                        n6 = n5 + 1;
                    } else {
                        n5 = Utils.firstBreakCharIndex(string, n6);
                        if (n5 >= 0) {
                            n6 = n5 + 1;
                        }
                    }
                }
                object = bl2 ? (String)object + string.substring(n6) : string3 + string.substring(n6);
            }
            while (true) {
                int n7;
                helper.setText((String)object);
                n6 = helper.hitTest(point2D).getCharIndex();
                if (bl2 && n6 < n4) {
                    if (n6 > 0 && ((String)object).charAt(n6 - 1) == '\n') {
                        --n6;
                    }
                    object = string.substring(0, n6) + string3;
                    break;
                }
                if (n6 <= 0 || n6 >= ((String)object).length()) break;
                if (bl) {
                    n5 = n2 + 1;
                    if (bl4 && (n7 = Utils.firstBreakCharIndex((String)object, n5)) >= 0) {
                        n5 = n7 + 1;
                    }
                    object = string3 + ((String)object).substring(n5);
                    continue;
                }
                if (bl2) {
                    n5 = n4 + 1;
                    if (bl4 && (n7 = Utils.firstBreakCharIndex((String)object, n5)) >= 0) {
                        n5 = n7 + 1;
                    }
                    object = ((String)object).substring(0, n4) + ((String)object).substring(n5);
                    continue;
                }
                n5 = ((String)object).length() - n2 - 1;
                if (bl4 && (n7 = Utils.lastBreakCharIndex((String)object, n5)) >= 0) {
                    n5 = n7;
                }
                object = ((String)object).substring(0, n5) + string3;
            }
        }
        helper.setWrappingWidth(DEFAULT_WRAPPING_WIDTH);
        helper.setLineSpacing(DEFAULT_LINE_SPACING);
        helper.setText(DEFAULT_TEXT);
        helper.setBoundsType(DEFAULT_BOUNDS_TYPE);
        return object;
    }

    private static int firstBreakCharIndex(String string, int n) {
        char[] cArray = string.toCharArray();
        for (int i = n; i < cArray.length; ++i) {
            if (!Utils.isPreferredBreakCharacter(cArray[i])) continue;
            return i;
        }
        return -1;
    }

    private static int lastBreakCharIndex(String string, int n) {
        char[] cArray = string.toCharArray();
        for (int i = n; i >= 0; --i) {
            if (!Utils.isPreferredBreakCharacter(cArray[i])) continue;
            return i;
        }
        return -1;
    }

    private static boolean isPreferredBreakCharacter(char c) {
        if (Character.isWhitespace(c)) {
            return true;
        }
        switch (c) {
            case '.': 
            case ':': 
            case ';': {
                return true;
            }
        }
        return false;
    }

    private static boolean requiresComplexLayout(Font font, String string) {
        return false;
    }

    static int computeStartOfWord(Font font, String string, int n) {
        if ("".equals(string) || n < 0) {
            return 0;
        }
        if (string.length() <= n) {
            return string.length();
        }
        if (Character.isWhitespace(string.charAt(n))) {
            return n;
        }
        boolean bl = Utils.requiresComplexLayout(font, string);
        if (bl) {
            return 0;
        }
        int n2 = n;
        while (--n2 >= 0) {
            if (!Character.isWhitespace(string.charAt(n2))) continue;
            return n2 + 1;
        }
        return 0;
    }

    static int computeEndOfWord(Font font, String string, int n) {
        if (string.equals("") || n < 0) {
            return 0;
        }
        if (string.length() <= n) {
            return string.length();
        }
        if (Character.isWhitespace(string.charAt(n))) {
            return n;
        }
        boolean bl = Utils.requiresComplexLayout(font, string);
        if (bl) {
            return string.length();
        }
        int n2 = n;
        while (++n2 < string.length()) {
            if (!Character.isWhitespace(string.charAt(n2))) continue;
            return n2;
        }
        return string.length();
    }

    public static double boundedSize(double d, double d2, double d3) {
        return Math.min(Math.max(d, d2), Math.max(d2, d3));
    }

    public static void addMnemonics(ContextMenu contextMenu, Scene scene) {
        Utils.addMnemonics(contextMenu, scene, false);
    }

    public static void addMnemonics(ContextMenu contextMenu, Scene scene, boolean bl) {
        if (!PlatformUtil.isMac()) {
            ContextMenuContent contextMenuContent = (ContextMenuContent)contextMenu.getSkin().getNode();
            for (int i = 0; i < contextMenu.getItems().size(); ++i) {
                TextBinding textBinding;
                int n;
                MenuItem menuItem = (MenuItem)contextMenu.getItems().get(i);
                if (!menuItem.isMnemonicParsing() || (n = (textBinding = new TextBinding(menuItem.getText())).getMnemonicIndex()) < 0) continue;
                KeyCombination keyCombination = textBinding.getMnemonicKeyCombination();
                Mnemonic mnemonic = new Mnemonic(contextMenuContent.getLabelAt(i), keyCombination);
                scene.addMnemonic(mnemonic);
                NodeHelper.setShowMnemonics(contextMenuContent.getLabelAt(i), bl);
            }
        }
    }

    public static void removeMnemonics(ContextMenu contextMenu, Scene scene) {
        if (!PlatformUtil.isMac()) {
            ContextMenuContent contextMenuContent = (ContextMenuContent)contextMenu.getSkin().getNode();
            for (int i = 0; i < contextMenu.getItems().size(); ++i) {
                TextBinding textBinding;
                int n;
                MenuItem menuItem = (MenuItem)contextMenu.getItems().get(i);
                if (!menuItem.isMnemonicParsing() || (n = (textBinding = new TextBinding(menuItem.getText())).getMnemonicIndex()) < 0) continue;
                KeyCombination keyCombination = textBinding.getMnemonicKeyCombination();
                ObservableList observableList = (ObservableList)scene.getMnemonics().get(keyCombination);
                if (observableList == null) continue;
                for (int j = 0; j < observableList.size(); ++j) {
                    if (((Mnemonic)observableList.get(j)).getNode() != contextMenuContent.getLabelAt(i)) continue;
                    observableList.remove(j);
                }
            }
        }
    }

    public static double computeXOffset(double d, double d2, HPos hPos) {
        if (hPos == null) {
            return 0.0;
        }
        switch (hPos) {
            case LEFT: {
                return 0.0;
            }
            case CENTER: {
                return (d - d2) / 2.0;
            }
            case RIGHT: {
                return d - d2;
            }
        }
        return 0.0;
    }

    public static double computeYOffset(double d, double d2, VPos vPos) {
        if (vPos == null) {
            return 0.0;
        }
        switch (vPos) {
            case TOP: {
                return 0.0;
            }
            case CENTER: {
                return (d - d2) / 2.0;
            }
            case BOTTOM: {
                return d - d2;
            }
        }
        return 0.0;
    }

    public static boolean isTwoLevelFocus() {
        return Platform.isSupported(ConditionalFeature.TWO_LEVEL_FOCUS);
    }

    public static <T> void executeOnceWhenPropertyIsNonNull(final ObservableValue<T> observableValue, final Consumer<T> consumer) {
        if (observableValue == null) {
            return;
        }
        T t = observableValue.getValue();
        if (t != null) {
            consumer.accept(t);
        } else {
            InvalidationListener invalidationListener = new InvalidationListener(){

                @Override
                public void invalidated(Observable observable) {
                    Object t = observableValue.getValue();
                    if (t != null) {
                        observableValue.removeListener(this);
                        consumer.accept(t);
                    }
                }
            };
            observableValue.addListener(invalidationListener);
        }
    }

    public static String formatHexString(Color color) {
        if (color != null) {
            return String.format((Locale)null, "#%02x%02x%02x", Math.round(color.getRed() * 255.0), Math.round(color.getGreen() * 255.0), Math.round(color.getBlue() * 255.0));
        }
        return null;
    }

    public static URL getResource(String string) {
        return Utils.class.getResource(string);
    }
}

