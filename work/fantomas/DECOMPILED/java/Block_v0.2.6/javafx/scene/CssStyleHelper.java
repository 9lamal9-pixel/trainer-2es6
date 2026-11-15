/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene;

import com.sun.javafx.css.CalculatedValue;
import com.sun.javafx.css.CascadingStyle;
import com.sun.javafx.css.ParsedValueImpl;
import com.sun.javafx.css.PseudoClassState;
import com.sun.javafx.css.StyleCache;
import com.sun.javafx.css.StyleCacheEntry;
import com.sun.javafx.css.StyleManager;
import com.sun.javafx.css.StyleMap;
import com.sun.javafx.logging.PlatformLogger;
import com.sun.javafx.util.Logging;
import com.sun.javafx.util.Utils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.WritableValue;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.css.CssMetaData;
import javafx.css.CssParser;
import javafx.css.FontCssMetaData;
import javafx.css.ParsedValue;
import javafx.css.PseudoClass;
import javafx.css.Rule;
import javafx.css.Selector;
import javafx.css.Style;
import javafx.css.StyleConverter;
import javafx.css.StyleOrigin;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.css.Stylesheet;
import javafx.css.converter.FontConverter;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.SubScene;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

final class CssStyleHelper {
    private static final PlatformLogger LOGGER = Logging.getCSSLogger();
    private Node firstStyleableAncestor;
    private CacheContainer cacheContainer;
    private PseudoClassState triggerStates = new PseudoClassState();
    private static final Set<PseudoClass> NULL_PSEUDO_CLASS_STATE = null;
    private static final CssMetaData dummyFontProperty = new FontCssMetaData<Node>("-fx-font", Font.getDefault()){

        @Override
        public boolean isSettable(Node node) {
            return true;
        }

        @Override
        public StyleableProperty<Font> getStyleableProperty(Node node) {
            return null;
        }
    };

    private CssStyleHelper() {
    }

    static CssStyleHelper createStyleHelper(Node node) {
        int n = 0;
        for (Styleable styleable = node; styleable != null; styleable = styleable.getStyleableParent()) {
            ++n;
        }
        PseudoClassState[] pseudoClassStateArray = new PseudoClassState[n];
        StyleMap styleMap = StyleManager.getInstance().findMatchingStyles(node, node.getSubScene(), pseudoClassStateArray);
        if (CssStyleHelper.canReuseStyleHelper(node, styleMap)) {
            if (node.styleHelper.cacheContainer != null && node.styleHelper.isUserSetFont(node)) {
                node.styleHelper.cacheContainer.fontSizeCache.clear();
            }
            node.styleHelper.cacheContainer.forceSlowpath = true;
            node.styleHelper.triggerStates.addAll((Collection)pseudoClassStateArray[0]);
            node.styleHelper.firstStyleableAncestor = CssStyleHelper.findFirstStyleableAncestor(node);
            CssStyleHelper.updateParentTriggerStates(node, n, pseudoClassStateArray);
            return node.styleHelper;
        }
        if (styleMap == null || styleMap.isEmpty()) {
            boolean bl = false;
            List<CssMetaData<Styleable, ?>> list = node.getCssMetaData();
            int n2 = list != null ? list.size() : 0;
            for (int i = 0; i < n2; ++i) {
                CssMetaData<Styleable, ?> cssMetaData = list.get(i);
                if (!cssMetaData.isInherits()) continue;
                bl = true;
                break;
            }
            if (!bl) {
                if (node.styleHelper != null) {
                    node.styleHelper.resetToInitialValues(node);
                }
                return null;
            }
        }
        CssStyleHelper cssStyleHelper = new CssStyleHelper();
        cssStyleHelper.triggerStates.addAll((Collection)pseudoClassStateArray[0]);
        CssStyleHelper.updateParentTriggerStates(node, n, pseudoClassStateArray);
        cssStyleHelper.cacheContainer = new CacheContainer(node, styleMap, n);
        cssStyleHelper.firstStyleableAncestor = CssStyleHelper.findFirstStyleableAncestor(node);
        if (node.styleHelper != null) {
            node.styleHelper.resetToInitialValues(node);
        }
        return cssStyleHelper;
    }

    private static void updateParentTriggerStates(Styleable styleable, int n, PseudoClassState[] pseudoClassStateArray) {
        Styleable styleable2 = styleable.getStyleableParent();
        for (int i = 1; i < n; ++i) {
            if (!(styleable2 instanceof Node)) {
                styleable2 = styleable2.getStyleableParent();
                continue;
            }
            Node node = (Node)styleable2;
            PseudoClassState pseudoClassState = pseudoClassStateArray[i];
            if (pseudoClassState != null && pseudoClassState.size() > 0) {
                if (node.styleHelper == null) {
                    node.styleHelper = new CssStyleHelper();
                    node.styleHelper.firstStyleableAncestor = CssStyleHelper.findFirstStyleableAncestor(node);
                }
                node.styleHelper.triggerStates.addAll((Collection)pseudoClassState);
            }
            styleable2 = styleable2.getStyleableParent();
        }
    }

    private boolean isUserSetFont(Styleable styleable) {
        Node node;
        CssMetaData cssMetaData;
        if (styleable == null) {
            return false;
        }
        CssMetaData cssMetaData2 = cssMetaData = this.cacheContainer != null ? this.cacheContainer.fontProp : null;
        if (cssMetaData != null) {
            Node node2 = node = cssMetaData != null ? cssMetaData.getStyleableProperty(styleable) : null;
            if (node != null && node.getStyleOrigin() == StyleOrigin.USER) {
                return true;
            }
        }
        node = this.firstStyleableAncestor;
        CssStyleHelper cssStyleHelper = CssStyleHelper.getStyleHelper(this.firstStyleableAncestor);
        if (cssStyleHelper != null) {
            return cssStyleHelper.isUserSetFont(node);
        }
        return false;
    }

    private static CssStyleHelper getStyleHelper(Node node) {
        return node != null ? node.styleHelper : null;
    }

    private static Node findFirstStyleableAncestor(Styleable styleable) {
        Node node = null;
        for (Styleable styleable2 = styleable.getStyleableParent(); styleable2 != null; styleable2 = styleable2.getStyleableParent()) {
            if (!(styleable2 instanceof Node) || ((Node)styleable2).styleHelper == null) continue;
            node = (Node)styleable2;
            break;
        }
        return node;
    }

    private static boolean isTrue(WritableValue<Boolean> writableValue) {
        return writableValue != null && writableValue.getValue() != false;
    }

    private static void setTrue(WritableValue<Boolean> writableValue) {
        if (writableValue != null) {
            writableValue.setValue(true);
        }
    }

    private static boolean canReuseStyleHelper(Node node, StyleMap styleMap) {
        int[] nArray;
        int[] nArray2;
        if (node == null || node.styleHelper == null) {
            return false;
        }
        if (styleMap == null) {
            return false;
        }
        StyleMap styleMap2 = node.styleHelper.getStyleMap(node);
        if (styleMap2 != styleMap) {
            return false;
        }
        if (node.styleHelper.cacheContainer == null) {
            return true;
        }
        Styleable styleable = node.getStyleableParent();
        if (styleable == null) {
            return true;
        }
        CssStyleHelper cssStyleHelper = CssStyleHelper.getStyleHelper(node.styleHelper.firstStyleableAncestor);
        if (cssStyleHelper != null && cssStyleHelper.cacheContainer != null && (nArray2 = cssStyleHelper.cacheContainer.styleCacheKey.getStyleMapIds()).length == (nArray = node.styleHelper.cacheContainer.styleCacheKey.getStyleMapIds()).length - 1) {
            boolean bl = true;
            for (int i = 0; i < nArray2.length; ++i) {
                if (nArray[i + 1] == nArray2[i]) continue;
                bl = false;
                break;
            }
            return bl;
        }
        return false;
    }

    private void resetToInitialValues(Styleable styleable) {
        if (this.cacheContainer == null || this.cacheContainer.cssSetProperties == null || this.cacheContainer.cssSetProperties.isEmpty()) {
            return;
        }
        HashSet hashSet = new HashSet(this.cacheContainer.cssSetProperties.entrySet());
        this.cacheContainer.cssSetProperties.clear();
        for (Map.Entry entry : hashSet) {
            CssMetaData cssMetaData = (CssMetaData)entry.getKey();
            StyleableProperty styleableProperty = cssMetaData.getStyleableProperty(styleable);
            StyleOrigin styleOrigin = styleableProperty.getStyleOrigin();
            if (styleOrigin == null || styleOrigin == StyleOrigin.USER) continue;
            CalculatedValue calculatedValue = (CalculatedValue)entry.getValue();
            styleableProperty.applyStyle(calculatedValue.getOrigin(), calculatedValue.getValue());
        }
    }

    private StyleMap getStyleMap(Styleable styleable) {
        if (this.cacheContainer == null || styleable == null) {
            return null;
        }
        return this.cacheContainer.getStyleMap(styleable);
    }

    boolean pseudoClassStateChanged(PseudoClass pseudoClass) {
        return this.triggerStates.contains(pseudoClass);
    }

    private Set<PseudoClass>[] getTransitionStates(Node node) {
        Object object;
        Node node2;
        if (this.cacheContainer == null) {
            return null;
        }
        int n = 0;
        for (node2 = node; node2 != null; node2 = node2.getParent()) {
            ++n;
        }
        PseudoClassState[] pseudoClassStateArray = new PseudoClassState[n];
        int n2 = 0;
        for (node2 = node; node2 != null; node2 = node2.getParent()) {
            Object object2 = object = node2 instanceof Node ? node2.styleHelper : null;
            if (object == null) continue;
            ObservableSet<PseudoClass> observableSet = node2.pseudoClassStates;
            pseudoClassStateArray[n2] = new PseudoClassState();
            pseudoClassStateArray[n2].addAll(observableSet);
            pseudoClassStateArray[n2].retainAll(object.triggerStates);
            ++n2;
        }
        object = new PseudoClassState[n2];
        System.arraycopy(pseudoClassStateArray, 0, object, 0, n2);
        return object;
    }

    void transitionToState(Node node) {
        Font font;
        StyleCacheEntry.Key key;
        StyleCacheEntry styleCacheEntry;
        boolean bl;
        if (this.cacheContainer == null) {
            return;
        }
        StyleMap styleMap = this.getStyleMap(node);
        if (styleMap == null) {
            this.cacheContainer = null;
            node.reapplyCSS();
            return;
        }
        boolean bl2 = styleMap.isEmpty();
        StyleCache styleCache = StyleManager.getInstance().getSharedCache(node, node.getSubScene(), this.cacheContainer.styleCacheKey);
        if (styleCache == null) {
            this.cacheContainer = null;
            node.reapplyCSS();
            return;
        }
        Set<PseudoClass>[] setArray = this.getTransitionStates(node);
        StyleCacheEntry.Key key2 = new StyleCacheEntry.Key(setArray, Font.getDefault());
        CalculatedValue calculatedValue = (CalculatedValue)this.cacheContainer.fontSizeCache.get(key2);
        if (calculatedValue == null) {
            if ((calculatedValue = this.lookupFont(node, "-fx-font", styleMap, calculatedValue)) == CalculatedValue.SKIP) {
                calculatedValue = this.getCachedFont(node.getStyleableParent());
            }
            if (calculatedValue == null) {
                calculatedValue = new CalculatedValue(Font.getDefault(), null, false);
            }
            this.cacheContainer.fontSizeCache.put(key2, calculatedValue);
        }
        boolean bl3 = bl = (styleCacheEntry = styleCache.getStyleCacheEntry(key = new StyleCacheEntry.Key(setArray, font = (Font)calculatedValue.getValue()))) != null;
        if (styleCacheEntry == null) {
            styleCacheEntry = new StyleCacheEntry();
            styleCache.addStyleCacheEntry(key, styleCacheEntry);
        }
        List<CssMetaData<Styleable, ?>> list = node.getCssMetaData();
        int n = list.size();
        boolean bl4 = this.cacheContainer.forceSlowpath;
        this.cacheContainer.forceSlowpath = false;
        for (int i = 0; i < n; ++i) {
            CalculatedValue calculatedValue2;
            Object object;
            ObservableList<CssParser.ParseError> observableList;
            Object object2;
            Object object3;
            boolean bl5;
            CssMetaData<Styleable, ?> cssMetaData = list.get(i);
            if (bl2 && !cssMetaData.isInherits() || !cssMetaData.isSettable(node)) continue;
            String string = cssMetaData.getProperty();
            CalculatedValue calculatedValue3 = styleCacheEntry.get(string);
            boolean bl6 = bl && calculatedValue3 == null && bl4;
            boolean bl7 = bl5 = !bl && calculatedValue3 == null || bl6;
            if (bl && !bl6) {
                if (calculatedValue3 == CalculatedValue.SKIP) {
                    continue;
                }
            } else if (calculatedValue3 == null && (calculatedValue3 = this.lookup(node, cssMetaData, styleMap, setArray[0], node, calculatedValue)) == null) {
                assert (false) : "lookup returned null for " + string;
                continue;
            }
            try {
                Object object4;
                if (calculatedValue3 == null || calculatedValue3 == CalculatedValue.SKIP) {
                    object4 = (CalculatedValue)this.cacheContainer.cssSetProperties.get(cssMetaData);
                    if (object4 == null || (object3 = cssMetaData.getStyleableProperty(node)).getStyleOrigin() == StyleOrigin.USER) continue;
                    object3.applyStyle(((CalculatedValue)object4).getOrigin(), ((CalculatedValue)object4).getValue());
                    continue;
                }
                if (bl5) {
                    styleCacheEntry.put(string, calculatedValue3);
                }
                object4 = cssMetaData.getStyleableProperty(node);
                object3 = object4.getStyleOrigin();
                object2 = calculatedValue3.getOrigin();
                if (object2 == null) {
                    assert (false) : object4.toString();
                    continue;
                }
                if (object3 == StyleOrigin.USER && object2 == StyleOrigin.USER_AGENT) continue;
                observableList = calculatedValue3.getValue();
                object = object4.getValue();
                if (object3 == object2 && !(object != null ? !object.equals(observableList) : observableList != null)) continue;
                if (LOGGER.isLoggable(PlatformLogger.Level.FINER)) {
                    LOGGER.finer(string + ", call applyStyle: " + (StyleableProperty)object4 + ", value =" + String.valueOf(observableList) + ", originOfCalculatedValue=" + object2);
                }
                object4.applyStyle((StyleOrigin)((Object)object2), observableList);
                if (this.cacheContainer.cssSetProperties.containsKey(cssMetaData)) continue;
                calculatedValue2 = new CalculatedValue(object, (StyleOrigin)((Object)object3), false);
                this.cacheContainer.cssSetProperties.put(cssMetaData, calculatedValue2);
                continue;
            }
            catch (Exception exception) {
                object3 = cssMetaData.getStyleableProperty(node);
                object2 = String.format("Failed to set css [%s] on [%s] due to '%s'\n", cssMetaData.getProperty(), object3, exception.getMessage());
                observableList = null;
                observableList = StyleManager.getErrors();
                if (observableList != null) {
                    object = new CssParser.ParseError.PropertySetError(cssMetaData, node, (String)object2);
                    observableList.add((CssParser.ParseError)object);
                }
                if (((PlatformLogger)(object = Logging.getCSSLogger())).isLoggable(PlatformLogger.Level.WARNING)) {
                    ((PlatformLogger)object).warning((String)object2);
                }
                styleCacheEntry.put(string, CalculatedValue.SKIP);
                calculatedValue2 = null;
                if (this.cacheContainer != null && this.cacheContainer.cssSetProperties != null) {
                    calculatedValue2 = (CalculatedValue)this.cacheContainer.cssSetProperties.get(cssMetaData);
                }
                Object object5 = calculatedValue2 != null ? calculatedValue2.getValue() : cssMetaData.getInitialValue(node);
                StyleOrigin styleOrigin = calculatedValue2 != null ? calculatedValue2.getOrigin() : null;
                try {
                    object3.applyStyle(styleOrigin, object5);
                    continue;
                }
                catch (Exception exception2) {
                    if (!((PlatformLogger)object).isLoggable(PlatformLogger.Level.SEVERE)) continue;
                    ((PlatformLogger)object).severe(String.format("Could not reset [%s] on [%s] due to %s\n", cssMetaData.getProperty(), object3, exception.getMessage()));
                }
            }
        }
    }

    private CascadingStyle getStyle(Styleable styleable, String string, StyleMap styleMap, Set<PseudoClass> set) {
        if (styleMap == null || styleMap.isEmpty()) {
            return null;
        }
        Map<String, List<CascadingStyle>> map = styleMap.getCascadingStyles();
        if (map == null || map.isEmpty()) {
            return null;
        }
        List<CascadingStyle> list = map.get(string);
        if (list == null || list.isEmpty()) {
            return null;
        }
        CascadingStyle cascadingStyle = null;
        int n = list == null ? 0 : list.size();
        for (int i = 0; i < n; ++i) {
            Selector selector;
            CascadingStyle cascadingStyle2 = list.get(i);
            Selector selector2 = selector = cascadingStyle2 == null ? null : cascadingStyle2.getSelector();
            if (selector == null || !selector.stateMatches(styleable, set)) continue;
            cascadingStyle = cascadingStyle2;
            break;
        }
        return cascadingStyle;
    }

    private CalculatedValue lookup(Styleable styleable, CssMetaData cssMetaData, StyleMap styleMap, Set<PseudoClass> set, Styleable styleable2, CalculatedValue calculatedValue) {
        Object object;
        int n;
        if (cssMetaData.getConverter() == FontConverter.getInstance()) {
            return this.lookupFont(styleable, cssMetaData.getProperty(), styleMap, calculatedValue);
        }
        String string = cssMetaData.getProperty();
        CascadingStyle cascadingStyle = this.getStyle(styleable, string, styleMap, set);
        List<CssMetaData<Styleable, ?>> list = cssMetaData.getSubProperties();
        int n2 = n = list != null ? list.size() : 0;
        if (cascadingStyle == null) {
            Object object2;
            CssMetaData<Styleable, ?> cssMetaData2;
            if (n == 0) {
                return this.handleNoStyleFound(styleable, cssMetaData, styleMap, set, styleable2, calculatedValue);
            }
            HashMap<Object, Object> hashMap = null;
            StyleOrigin styleOrigin = null;
            boolean bl = false;
            for (int i = 0; i < n; ++i) {
                cssMetaData2 = list.get(i);
                object2 = this.lookup(styleable, cssMetaData2, styleMap, set, styleable2, calculatedValue);
                if (object2 == CalculatedValue.SKIP) continue;
                if (hashMap == null) {
                    hashMap = new HashMap<Object, Object>();
                }
                hashMap.put(cssMetaData2, ((CalculatedValue)object2).getValue());
                if (styleOrigin != null && ((CalculatedValue)object2).getOrigin() != null ? styleOrigin.compareTo(((CalculatedValue)object2).getOrigin()) < 0 : ((CalculatedValue)object2).getOrigin() != null) {
                    styleOrigin = ((CalculatedValue)object2).getOrigin();
                }
                bl = bl || ((CalculatedValue)object2).isRelative();
            }
            if (hashMap == null || hashMap.isEmpty()) {
                return this.handleNoStyleFound(styleable, cssMetaData, styleMap, set, styleable2, calculatedValue);
            }
            try {
                StyleConverter styleConverter = cssMetaData.getConverter();
                cssMetaData2 = styleConverter.convert(hashMap);
                return new CalculatedValue(cssMetaData2, styleOrigin, bl);
            }
            catch (ClassCastException classCastException) {
                cssMetaData2 = this.formatExceptionMessage(styleable, cssMetaData, null, classCastException);
                object2 = null;
                object2 = StyleManager.getErrors();
                if (object2 != null) {
                    CssParser.ParseError.PropertySetError propertySetError = new CssParser.ParseError.PropertySetError(cssMetaData, styleable, (String)((Object)cssMetaData2));
                    object2.add(propertySetError);
                }
                if (LOGGER.isLoggable(PlatformLogger.Level.WARNING)) {
                    LOGGER.warning((String)((Object)cssMetaData2));
                    LOGGER.fine("caught: ", classCastException);
                    LOGGER.fine("styleable = " + cssMetaData);
                    LOGGER.fine("node = " + styleable.toString());
                }
                return CalculatedValue.SKIP;
            }
        }
        if (cascadingStyle.getOrigin() == StyleOrigin.USER_AGENT && (object = cssMetaData.getStyleableProperty(styleable2)) != null && object.getStyleOrigin() == StyleOrigin.USER) {
            return CalculatedValue.SKIP;
        }
        object = cascadingStyle.getParsedValue();
        if (object != null && "inherit".equals(((ParsedValue)object).getValue()) && (cascadingStyle = this.getInheritedStyle(styleable, string)) == null) {
            return CalculatedValue.SKIP;
        }
        return this.calculateValue(cascadingStyle, styleable, cssMetaData, styleMap, set, styleable2, calculatedValue);
    }

    private CalculatedValue handleNoStyleFound(Styleable styleable, CssMetaData cssMetaData, StyleMap styleMap, Set<PseudoClass> set, Styleable styleable2, CalculatedValue calculatedValue) {
        if (cssMetaData.isInherits()) {
            StyleOrigin styleOrigin;
            StyleableProperty styleableProperty = cssMetaData.getStyleableProperty(styleable);
            StyleOrigin styleOrigin2 = styleOrigin = styleableProperty != null ? styleableProperty.getStyleOrigin() : null;
            if (styleOrigin == StyleOrigin.USER) {
                return CalculatedValue.SKIP;
            }
            CascadingStyle cascadingStyle = this.getInheritedStyle(styleable, cssMetaData.getProperty());
            if (cascadingStyle == null) {
                return CalculatedValue.SKIP;
            }
            CalculatedValue calculatedValue2 = this.calculateValue(cascadingStyle, styleable, cssMetaData, styleMap, set, styleable2, calculatedValue);
            return calculatedValue2;
        }
        return CalculatedValue.SKIP;
    }

    private CascadingStyle getInheritedStyle(Styleable styleable, String string) {
        ObservableSet<PseudoClass> observableSet;
        StyleMap styleMap;
        CascadingStyle cascadingStyle;
        Node node = ((Node)styleable).styleHelper.firstStyleableAncestor;
        CssStyleHelper cssStyleHelper = CssStyleHelper.getStyleHelper(node);
        if (node != null && cssStyleHelper != null && (cascadingStyle = cssStyleHelper.getStyle(node, string, styleMap = cssStyleHelper.getStyleMap(node), observableSet = node.pseudoClassStates)) != null) {
            ParsedValue parsedValue = cascadingStyle.getParsedValue();
            if ("inherit".equals(parsedValue.getValue())) {
                return this.getInheritedStyle(node, string);
            }
            return cascadingStyle;
        }
        return null;
    }

    private CascadingStyle resolveRef(Styleable styleable, String string, StyleMap styleMap, Set<PseudoClass> set) {
        CascadingStyle cascadingStyle = this.getStyle(styleable, string, styleMap, set);
        if (cascadingStyle != null) {
            return cascadingStyle;
        }
        if (set != null && set.size() > 0) {
            return this.resolveRef(styleable, string, styleMap, NULL_PSEUDO_CLASS_STATE);
        }
        Node node = ((Node)styleable).styleHelper.firstStyleableAncestor;
        CssStyleHelper cssStyleHelper = CssStyleHelper.getStyleHelper(node);
        if (node == null || cssStyleHelper == null) {
            return null;
        }
        StyleMap styleMap2 = cssStyleHelper.getStyleMap(node);
        ObservableSet<PseudoClass> observableSet = node instanceof Node ? node.pseudoClassStates : styleable.getPseudoClassStates();
        return cssStyleHelper.resolveRef(node, string, styleMap2, observableSet);
    }

    private ParsedValue resolveLookups(Styleable styleable, ParsedValue parsedValue, StyleMap styleMap, Set<PseudoClass> set, ObjectProperty<StyleOrigin> objectProperty, Set<ParsedValue> set2) {
        ParsedValue[][] parsedValueArray;
        Object object;
        Object v;
        if (parsedValue.isLookup() && (v = parsedValue.getValue()) instanceof String && (object = this.resolveRef(styleable, (String)(parsedValueArray = ((String)v).toLowerCase(Locale.ROOT)), styleMap, set)) != null) {
            if (set2.contains(((CascadingStyle)object).getParsedValue())) {
                if (LOGGER.isLoggable(PlatformLogger.Level.WARNING)) {
                    LOGGER.warning("Loop detected in " + ((CascadingStyle)object).getRule().toString() + " while resolving '" + (String)parsedValueArray + "'");
                }
                throw new IllegalArgumentException("Loop detected in " + ((CascadingStyle)object).getRule().toString() + " while resolving '" + (String)parsedValueArray + "'");
            }
            set2.add(parsedValue);
            StyleOrigin styleOrigin = (StyleOrigin)((Object)objectProperty.get());
            StyleOrigin styleOrigin2 = ((CascadingStyle)object).getOrigin();
            if (styleOrigin2 != null && (styleOrigin == null || styleOrigin.compareTo(styleOrigin2) < 0)) {
                objectProperty.set(styleOrigin2);
            }
            ParsedValue parsedValue2 = this.resolveLookups(styleable, ((CascadingStyle)object).getParsedValue(), styleMap, set, objectProperty, set2);
            if (set2 != null) {
                set2.remove(parsedValue);
            }
            return parsedValue2;
        }
        if (!parsedValue.isContainsLookups()) {
            return parsedValue;
        }
        v = parsedValue.getValue();
        if (v instanceof ParsedValue[][]) {
            parsedValueArray = (ParsedValue[][])v;
            object = new ParsedValue[parsedValueArray.length][0];
            for (int i = 0; i < parsedValueArray.length; ++i) {
                object[i] = new ParsedValue[parsedValueArray[i].length];
                for (int j = 0; j < parsedValueArray[i].length; ++j) {
                    if (parsedValueArray[i][j] == null) continue;
                    object[i][j] = this.resolveLookups(styleable, parsedValueArray[i][j], styleMap, set, objectProperty, set2);
                }
            }
            set2.clear();
            return new ParsedValueImpl(object, parsedValue.getConverter(), false);
        }
        if (v instanceof ParsedValueImpl[]) {
            parsedValueArray = (ParsedValue[])v;
            object = new ParsedValue[parsedValueArray.length];
            for (int i = 0; i < parsedValueArray.length; ++i) {
                if (parsedValueArray[i] == null) continue;
                object[i] = this.resolveLookups(styleable, (ParsedValue)parsedValueArray[i], styleMap, set, objectProperty, set2);
            }
            set2.clear();
            return new ParsedValueImpl(object, parsedValue.getConverter(), false);
        }
        return parsedValue;
    }

    private String getUnresolvedLookup(ParsedValue parsedValue) {
        block5: {
            Object v;
            block4: {
                v = parsedValue.getValue();
                if (parsedValue.isLookup() && v instanceof String) {
                    return (String)v;
                }
                if (!(v instanceof ParsedValue[][])) break block4;
                ParsedValue[][] parsedValueArray = (ParsedValue[][])v;
                for (int i = 0; i < parsedValueArray.length; ++i) {
                    for (int j = 0; j < parsedValueArray[i].length; ++j) {
                        String string;
                        if (parsedValueArray[i][j] == null || (string = this.getUnresolvedLookup(parsedValueArray[i][j])) == null) continue;
                        return string;
                    }
                }
                break block5;
            }
            if (!(v instanceof ParsedValue[])) break block5;
            ParsedValue[] parsedValueArray = (ParsedValue[])v;
            for (int i = 0; i < parsedValueArray.length; ++i) {
                String string;
                if (parsedValueArray[i] == null || (string = this.getUnresolvedLookup(parsedValueArray[i])) == null) continue;
                return string;
            }
        }
        return null;
    }

    private String formatUnresolvedLookupMessage(Styleable styleable, CssMetaData cssMetaData, Style style, ParsedValue parsedValue, ClassCastException classCastException) {
        String string;
        String string2 = parsedValue != null && parsedValue.isContainsLookups() ? this.getUnresolvedLookup(parsedValue) : null;
        StringBuilder stringBuilder = new StringBuilder();
        if (string2 != null) {
            stringBuilder.append("Could not resolve '").append(string2).append("'").append(" while resolving lookups for '").append(cssMetaData.getProperty()).append("'");
        } else {
            stringBuilder.append("Caught '").append(classCastException).append("'").append(" while converting value for '").append(cssMetaData.getProperty()).append("'");
        }
        Rule rule = style != null ? style.getDeclaration().getRule() : null;
        Stylesheet stylesheet = rule != null ? rule.getStylesheet() : null;
        String string3 = string = stylesheet != null ? stylesheet.getUrl() : null;
        if (string != null) {
            stringBuilder.append(" from rule '").append(style.getSelector()).append("' in stylesheet ").append(string);
        } else if (stylesheet != null && StyleOrigin.INLINE == stylesheet.getOrigin()) {
            stringBuilder.append(" from inline style on ").append(styleable.toString());
        }
        return stringBuilder.toString();
    }

    private String formatExceptionMessage(Styleable styleable, CssMetaData cssMetaData, Style style, Exception exception) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Caught ").append(String.valueOf(exception));
        if (cssMetaData != null) {
            stringBuilder.append("'").append(" while calculating value for '").append(cssMetaData.getProperty()).append("'");
        }
        if (style != null) {
            String string;
            Rule rule = style.getDeclaration().getRule();
            Stylesheet stylesheet = rule != null ? rule.getStylesheet() : null;
            String string2 = string = stylesheet != null ? stylesheet.getUrl() : null;
            if (string != null) {
                stringBuilder.append(" from rule '").append(style.getSelector()).append("' in stylesheet ").append(string);
            } else if (styleable != null && stylesheet != null && StyleOrigin.INLINE == stylesheet.getOrigin()) {
                stringBuilder.append(" from inline style on ").append(styleable.toString());
            } else {
                stringBuilder.append(" from style '").append(String.valueOf(style)).append("'");
            }
        }
        return stringBuilder.toString();
    }

    private CalculatedValue calculateValue(CascadingStyle cascadingStyle, Styleable styleable, CssMetaData cssMetaData, StyleMap styleMap, Set<PseudoClass> set, Styleable styleable2, CalculatedValue calculatedValue) {
        ParsedValue parsedValue = cascadingStyle.getParsedValue();
        if (parsedValue != null && !"null".equals(parsedValue.getValue()) && !"none".equals(parsedValue.getValue())) {
            ParsedValueImpl parsedValueImpl = null;
            try {
                Object object;
                Object object2;
                SimpleObjectProperty<StyleOrigin> simpleObjectProperty = new SimpleObjectProperty<StyleOrigin>(cascadingStyle.getOrigin());
                parsedValueImpl = this.resolveLookups(styleable, parsedValue, styleMap, set, simpleObjectProperty, new HashSet<ParsedValue>());
                String string = cssMetaData.getProperty();
                Object var12_18 = null;
                boolean bl = "-fx-font".equals(string) || "-fx-font-size".equals(string);
                boolean bl2 = ParsedValueImpl.containsFontRelativeSize(parsedValueImpl, bl);
                Font font = null;
                if (bl2 && bl && (calculatedValue == null || calculatedValue.isRelative())) {
                    object2 = styleable;
                    object = calculatedValue;
                    do {
                        CalculatedValue calculatedValue2;
                        if ((calculatedValue2 = this.getCachedFont(object2.getStyleableParent())) == null) continue;
                        if (calculatedValue2.isRelative()) {
                            if (object == null || calculatedValue2.equals(object)) {
                                object = calculatedValue2;
                                continue;
                            }
                            font = (Font)calculatedValue2.getValue();
                            continue;
                        }
                        font = (Font)calculatedValue2.getValue();
                    } while (font == null && (object2 = object2.getStyleableParent()) != null);
                }
                if (font == null) {
                    font = calculatedValue != null && !calculatedValue.isRelative() ? (Font)calculatedValue.getValue() : Font.getDefault();
                }
                if ((object2 = cssMetaData.getConverter()) == StyleConverter.getInsetsConverter()) {
                    if (parsedValueImpl.getValue() instanceof ParsedValue) {
                        parsedValueImpl = new ParsedValueImpl(new ParsedValue[]{(ParsedValue)parsedValueImpl.getValue()}, null, false);
                    }
                    var12_18 = ((StyleConverter)object2).convert(parsedValueImpl, font);
                } else {
                    var12_18 = parsedValueImpl.getConverter() != null ? ((ParsedValue)parsedValueImpl).convert(font) : cssMetaData.getConverter().convert(parsedValueImpl, font);
                }
                object = (StyleOrigin)((Object)simpleObjectProperty.get());
                return new CalculatedValue(var12_18, (StyleOrigin)((Object)object), bl2);
            }
            catch (ClassCastException classCastException) {
                String string = this.formatUnresolvedLookupMessage(styleable, cssMetaData, cascadingStyle.getStyle(), parsedValueImpl, classCastException);
                ObservableList<CssParser.ParseError> observableList = null;
                observableList = StyleManager.getErrors();
                if (observableList != null) {
                    CssParser.ParseError.PropertySetError propertySetError = new CssParser.ParseError.PropertySetError(cssMetaData, styleable, string);
                    observableList.add(propertySetError);
                }
                if (LOGGER.isLoggable(PlatformLogger.Level.WARNING)) {
                    LOGGER.warning(string);
                    LOGGER.fine("node = " + styleable.toString());
                    LOGGER.fine("cssMetaData = " + cssMetaData);
                    LOGGER.fine("styles = " + CssStyleHelper.getMatchingStyles(styleable, cssMetaData));
                }
                return CalculatedValue.SKIP;
            }
            catch (IllegalArgumentException illegalArgumentException) {
                String string = this.formatExceptionMessage(styleable, cssMetaData, cascadingStyle.getStyle(), illegalArgumentException);
                ObservableList<CssParser.ParseError> observableList = null;
                observableList = StyleManager.getErrors();
                if (observableList != null) {
                    CssParser.ParseError.PropertySetError propertySetError = new CssParser.ParseError.PropertySetError(cssMetaData, styleable, string);
                    observableList.add(propertySetError);
                }
                if (LOGGER.isLoggable(PlatformLogger.Level.WARNING)) {
                    LOGGER.warning(string);
                    LOGGER.fine("caught: ", illegalArgumentException);
                    LOGGER.fine("styleable = " + cssMetaData);
                    LOGGER.fine("node = " + styleable.toString());
                }
                return CalculatedValue.SKIP;
            }
            catch (NullPointerException nullPointerException) {
                String string = this.formatExceptionMessage(styleable, cssMetaData, cascadingStyle.getStyle(), nullPointerException);
                ObservableList<CssParser.ParseError> observableList = null;
                observableList = StyleManager.getErrors();
                if (observableList != null) {
                    CssParser.ParseError.PropertySetError propertySetError = new CssParser.ParseError.PropertySetError(cssMetaData, styleable, string);
                    observableList.add(propertySetError);
                }
                if (LOGGER.isLoggable(PlatformLogger.Level.WARNING)) {
                    LOGGER.warning(string);
                    LOGGER.fine("caught: ", nullPointerException);
                    LOGGER.fine("styleable = " + cssMetaData);
                    LOGGER.fine("node = " + styleable.toString());
                }
                return CalculatedValue.SKIP;
            }
        }
        return new CalculatedValue(null, cascadingStyle.getOrigin(), false);
    }

    private CalculatedValue getCachedFont(Styleable styleable) {
        if (!(styleable instanceof Node)) {
            return null;
        }
        CalculatedValue calculatedValue = null;
        Node node = (Node)styleable;
        CssStyleHelper cssStyleHelper = node.styleHelper;
        if (cssStyleHelper == null || cssStyleHelper.cacheContainer == null) {
            calculatedValue = this.getCachedFont(node.getStyleableParent());
        } else {
            Set<PseudoClass>[] setArray;
            CacheContainer cacheContainer = cssStyleHelper.cacheContainer;
            if (cacheContainer != null && cacheContainer.fontSizeCache != null && !cacheContainer.fontSizeCache.isEmpty()) {
                setArray = cssStyleHelper.getTransitionStates(node);
                StyleCacheEntry.Key key = new StyleCacheEntry.Key(setArray, Font.getDefault());
                calculatedValue = (CalculatedValue)cacheContainer.fontSizeCache.get(key);
            }
            if (calculatedValue == null) {
                setArray = cssStyleHelper.getStyleMap(node);
                calculatedValue = cssStyleHelper.lookupFont(node, "-fx-font", (StyleMap)setArray, null);
            }
        }
        return calculatedValue != CalculatedValue.SKIP ? calculatedValue : null;
    }

    FontPosture getFontPosture(Font font) {
        if (font == null) {
            return FontPosture.REGULAR;
        }
        String string = font.getName().toLowerCase(Locale.ROOT);
        if (string.contains("italic")) {
            return FontPosture.ITALIC;
        }
        return FontPosture.REGULAR;
    }

    FontWeight getFontWeight(Font font) {
        if (font == null) {
            return FontWeight.NORMAL;
        }
        String string = font.getName().toLowerCase(Locale.ROOT);
        if (string.contains("bold")) {
            if (string.contains("extra")) {
                return FontWeight.EXTRA_BOLD;
            }
            if (string.contains("ultra")) {
                return FontWeight.EXTRA_BOLD;
            }
            if (string.contains("semi")) {
                return FontWeight.SEMI_BOLD;
            }
            if (string.contains("demi")) {
                return FontWeight.SEMI_BOLD;
            }
            return FontWeight.BOLD;
        }
        if (string.contains("light")) {
            if (string.contains("extra")) {
                return FontWeight.EXTRA_LIGHT;
            }
            if (string.contains("ultra")) {
                return FontWeight.EXTRA_LIGHT;
            }
            return FontWeight.LIGHT;
        }
        if (string.contains("black")) {
            return FontWeight.BLACK;
        }
        if (string.contains("heavy")) {
            return FontWeight.BLACK;
        }
        if (string.contains("medium")) {
            return FontWeight.MEDIUM;
        }
        return FontWeight.NORMAL;
    }

    String getFontFamily(Font font) {
        if (font == null) {
            return Font.getDefault().getFamily();
        }
        return font.getFamily();
    }

    Font deriveFont(Font font, String string, FontWeight fontWeight, FontPosture fontPosture, double d) {
        if (font != null && string == null) {
            string = this.getFontFamily(font);
        } else if (string != null) {
            string = Utils.stripQuotes(string);
        }
        if (font != null && fontWeight == null) {
            fontWeight = this.getFontWeight(font);
        }
        if (font != null && fontPosture == null) {
            fontPosture = this.getFontPosture(font);
        }
        if (font != null && d <= 0.0) {
            d = font.getSize();
        }
        return Font.font(string, fontWeight, fontPosture, d);
    }

    CalculatedValue lookupFont(Styleable styleable, String string, StyleMap styleMap, CalculatedValue calculatedValue) {
        Object object;
        Object object2;
        Object object3;
        Object object4;
        Object object5;
        Object object6;
        Object object7;
        Object object8;
        ObservableSet<PseudoClass> observableSet;
        Object object9 = null;
        int n = 0;
        boolean bl = false;
        String string2 = null;
        double d = -1.0;
        FontWeight fontWeight = null;
        FontPosture fontPosture = null;
        Object object10 = calculatedValue;
        ObservableSet<PseudoClass> observableSet2 = observableSet = styleable instanceof Node ? ((Node)styleable).pseudoClassStates : styleable.getPseudoClassStates();
        if (this.cacheContainer.fontProp != null) {
            object8 = this.cacheContainer.fontProp.getStyleableProperty(styleable);
            object7 = object8.getStyleOrigin();
            object6 = (Font)object8.getValue();
            if (object6 == null) {
                object6 = Font.getDefault();
            }
            if (object7 == StyleOrigin.USER) {
                object9 = object7;
                string2 = this.getFontFamily((Font)object6);
                d = ((Font)object6).getSize();
                fontWeight = this.getFontWeight((Font)object6);
                fontPosture = this.getFontPosture((Font)object6);
                object10 = new CalculatedValue(object6, (StyleOrigin)((Object)object7), false);
            }
        }
        if ((object8 = this.getCachedFont(styleable.getStyleableParent())) == null) {
            object8 = new CalculatedValue(Font.getDefault(), null, false);
        }
        if ((object7 = this.getStyle(styleable, string, styleMap, observableSet)) == null && object9 != StyleOrigin.USER) {
            Object object11 = object6 = styleable != null ? styleable.getStyleableParent() : null;
            while (object6 != null) {
                Object object12 = object5 = object6 instanceof Node ? ((Node)object6).styleHelper : null;
                if (object5 != null) {
                    ++n;
                    object4 = ((CssStyleHelper)object5).getStyleMap((Styleable)object6);
                    object2 = super.getStyle((Styleable)object6, string, (StyleMap)object4, (Set<PseudoClass>)(object3 = ((Node)object6).pseudoClassStates));
                    if (object2 != null && !"inherit".equals(((ParsedValue)(object = ((CascadingStyle)object2).getParsedValue())).getValue())) {
                        object7 = object2;
                        break;
                    }
                }
                object6 = object6.getStyleableParent();
            }
        }
        if (object7 != null && (object9 == null || object9.compareTo(((CascadingStyle)object7).getOrigin()) <= 0) && ((CalculatedValue)(object6 = this.calculateValue((CascadingStyle)object7, styleable, dummyFontProperty, styleMap, observableSet, styleable, (CalculatedValue)object8))).getValue() instanceof Font) {
            object9 = ((CalculatedValue)object6).getOrigin();
            object5 = (Font)((CalculatedValue)object6).getValue();
            string2 = this.getFontFamily((Font)object5);
            d = ((Font)object5).getSize();
            fontWeight = this.getFontWeight((Font)object5);
            fontPosture = this.getFontPosture((Font)object5);
            object10 = object6;
            bl = true;
        }
        if ((object6 = this.getStyle(styleable, string.concat("-size"), styleMap, observableSet)) != null) {
            if (object7 != null && ((CascadingStyle)object7).compareTo((CascadingStyle)object6) < 0) {
                object6 = null;
            } else if (object9 == StyleOrigin.USER && StyleOrigin.USER.compareTo(((CascadingStyle)object6).getOrigin()) > 0) {
                object6 = null;
            }
        } else if (object9 != StyleOrigin.USER) {
            object6 = this.lookupInheritedFontProperty(styleable, string.concat("-size"), styleMap, n, (CascadingStyle)object7);
        }
        if (object6 != null && ((CalculatedValue)(object5 = this.calculateValue((CascadingStyle)object6, styleable, dummyFontProperty, styleMap, observableSet, styleable, (CalculatedValue)object8))).getValue() instanceof Double) {
            boolean bl2;
            if (object9 == null || object9.compareTo(((CascadingStyle)object6).getOrigin()) <= 0) {
                object9 = ((CalculatedValue)object5).getOrigin();
            }
            d = (Double)((CalculatedValue)object5).getValue();
            if (object10 != null) {
                bl2 = ((CalculatedValue)object10).isRelative() || ((CalculatedValue)object5).isRelative();
                object3 = this.deriveFont((Font)((CalculatedValue)object10).getValue(), string2, fontWeight, fontPosture, d);
                object10 = new CalculatedValue(object3, (StyleOrigin)((Object)object9), bl2);
            } else {
                bl2 = ((CalculatedValue)object5).isRelative();
                object3 = this.deriveFont(Font.getDefault(), string2, fontWeight, fontPosture, d);
                object10 = new CalculatedValue(object3, (StyleOrigin)((Object)object9), bl2);
            }
            bl = true;
        }
        if (calculatedValue == null) {
            return object10 != null ? object10 : CalculatedValue.SKIP;
        }
        object5 = this.getStyle(styleable, string.concat("-weight"), styleMap, observableSet);
        if (object5 != null) {
            if (object7 != null && ((CascadingStyle)object7).compareTo((CascadingStyle)object5) < 0) {
                object5 = null;
            }
        } else if (object9 != StyleOrigin.USER) {
            object5 = this.lookupInheritedFontProperty(styleable, string.concat("-weight"), styleMap, n, (CascadingStyle)object7);
        }
        if (object5 != null && ((CalculatedValue)(object4 = this.calculateValue((CascadingStyle)object5, styleable, dummyFontProperty, styleMap, observableSet, styleable, null))).getValue() instanceof FontWeight) {
            if (object9 == null || object9.compareTo(((CascadingStyle)object5).getOrigin()) <= 0) {
                object9 = ((CalculatedValue)object4).getOrigin();
            }
            fontWeight = (FontWeight)((Object)((CalculatedValue)object4).getValue());
            bl = true;
        }
        if ((object4 = this.getStyle(styleable, string.concat("-style"), styleMap, observableSet)) != null) {
            if (object7 != null && ((CascadingStyle)object7).compareTo((CascadingStyle)object4) < 0) {
                object4 = null;
            }
        } else if (object9 != StyleOrigin.USER) {
            object4 = this.lookupInheritedFontProperty(styleable, string.concat("-style"), styleMap, n, (CascadingStyle)object7);
        }
        if (object4 != null && ((CalculatedValue)(object3 = this.calculateValue((CascadingStyle)object4, styleable, dummyFontProperty, styleMap, observableSet, styleable, null))).getValue() instanceof FontPosture) {
            if (object9 == null || object9.compareTo(((CascadingStyle)object4).getOrigin()) <= 0) {
                object9 = ((CalculatedValue)object3).getOrigin();
            }
            fontPosture = (FontPosture)((Object)((CalculatedValue)object3).getValue());
            bl = true;
        }
        if ((object3 = this.getStyle(styleable, string.concat("-family"), styleMap, observableSet)) != null) {
            if (object7 != null && ((CascadingStyle)object7).compareTo((CascadingStyle)object3) < 0) {
                object3 = null;
            }
        } else if (object9 != StyleOrigin.USER) {
            object3 = this.lookupInheritedFontProperty(styleable, string.concat("-family"), styleMap, n, (CascadingStyle)object7);
        }
        if (object3 != null && ((CalculatedValue)(object2 = this.calculateValue((CascadingStyle)object3, styleable, dummyFontProperty, styleMap, observableSet, styleable, null))).getValue() instanceof String) {
            if (object9 == null || object9.compareTo(((CascadingStyle)object3).getOrigin()) <= 0) {
                object9 = ((CalculatedValue)object2).getOrigin();
            }
            string2 = (String)((CalculatedValue)object2).getValue();
            bl = true;
        }
        if (bl) {
            object2 = object10 != null ? (Font)((CalculatedValue)object10).getValue() : Font.getDefault();
            object = this.deriveFont((Font)object2, string2, fontWeight, fontPosture, d);
            return new CalculatedValue(object, (StyleOrigin)((Object)object9), false);
        }
        return CalculatedValue.SKIP;
    }

    private CascadingStyle lookupInheritedFontProperty(Styleable styleable, String string, StyleMap styleMap, int n, CascadingStyle cascadingStyle) {
        int n2 = n;
        for (Styleable styleable2 = styleable != null ? styleable.getStyleableParent() : null; styleable2 != null && n2 > 0; styleable2 = styleable2.getStyleableParent()) {
            ObservableSet<PseudoClass> observableSet;
            CssStyleHelper cssStyleHelper;
            CssStyleHelper cssStyleHelper2 = cssStyleHelper = styleable2 instanceof Node ? ((Node)styleable2).styleHelper : null;
            if (cssStyleHelper == null) continue;
            --n2;
            StyleMap styleMap2 = cssStyleHelper.getStyleMap(styleable2);
            CascadingStyle cascadingStyle2 = cssStyleHelper.getStyle(styleable2, string, styleMap2, observableSet = ((Node)styleable2).pseudoClassStates);
            if (cascadingStyle2 == null) continue;
            if (cascadingStyle != null && n2 == 0 && cascadingStyle.compareTo(cascadingStyle2) < 0) {
                return null;
            }
            ParsedValue parsedValue = cascadingStyle2.getParsedValue();
            if ("inherit".equals(parsedValue.getValue())) continue;
            return cascadingStyle2;
        }
        return null;
    }

    static List<Style> getMatchingStyles(Styleable styleable, CssMetaData cssMetaData) {
        CssStyleHelper cssStyleHelper;
        if (!(styleable instanceof Node)) {
            return Collections.emptyList();
        }
        Node node = (Node)styleable;
        CssStyleHelper cssStyleHelper2 = cssStyleHelper = node.styleHelper != null ? node.styleHelper : CssStyleHelper.createStyleHelper(node);
        if (cssStyleHelper != null) {
            return cssStyleHelper.getMatchingStyles(node, cssMetaData, false);
        }
        return Collections.emptyList();
    }

    static Map<StyleableProperty<?>, List<Style>> getMatchingStyles(Map<StyleableProperty<?>, List<Style>> map, Node node) {
        CssStyleHelper cssStyleHelper;
        CssStyleHelper cssStyleHelper2 = cssStyleHelper = node.styleHelper != null ? node.styleHelper : CssStyleHelper.createStyleHelper(node);
        if (cssStyleHelper != null) {
            if (map == null) {
                map = new HashMap();
            }
            for (CssMetaData<Styleable, Object> cssMetaData : node.getCssMetaData()) {
                List<Style> list = cssStyleHelper.getMatchingStyles(node, cssMetaData, true);
                if (list == null || list.isEmpty()) continue;
                StyleableProperty<Object> styleableProperty = cssMetaData.getStyleableProperty(node);
                map.put(styleableProperty, list);
            }
        }
        if (node instanceof Parent) {
            for (Node node2 : ((Parent)node).getChildren()) {
                map = CssStyleHelper.getMatchingStyles(map, node2);
            }
        }
        return map;
    }

    private List<Style> getMatchingStyles(Styleable styleable, CssMetaData cssMetaData, boolean bl) {
        int n;
        ArrayList<CascadingStyle> arrayList = new ArrayList<CascadingStyle>();
        this.getMatchingStyles(styleable, cssMetaData, arrayList, bl);
        List<CssMetaData<Styleable, ?>> list = cssMetaData.getSubProperties();
        if (list != null) {
            n = list.size();
            for (int i = 0; i < n; ++i) {
                CssMetaData<Styleable, ?> cssMetaData2 = list.get(i);
                this.getMatchingStyles(styleable, cssMetaData2, arrayList, bl);
            }
        }
        Collections.sort(arrayList);
        ArrayList<Style> arrayList2 = new ArrayList<Style>(arrayList.size());
        int n2 = arrayList.size();
        for (n = 0; n < n2; ++n) {
            Style style = ((CascadingStyle)arrayList.get(n)).getStyle();
            if (arrayList2.contains(style)) continue;
            arrayList2.add(style);
        }
        return arrayList2;
    }

    private void getMatchingStyles(Styleable styleable, CssMetaData cssMetaData, List<CascadingStyle> list, boolean bl) {
        if (styleable != null) {
            List<CascadingStyle> list2;
            Map<String, List<CascadingStyle>> map;
            String string = cssMetaData.getProperty();
            Node node = styleable instanceof Node ? (Node)styleable : null;
            StyleMap styleMap = this.getStyleMap(node);
            if (styleMap == null) {
                return;
            }
            if (bl) {
                map = this.getStyle(styleable, cssMetaData.getProperty(), styleMap, node.pseudoClassStates);
                if (map != null) {
                    list.add((CascadingStyle)((Object)map));
                    list2 = ((CascadingStyle)((Object)map)).getParsedValue();
                    this.getMatchingLookupStyles(styleable, (ParsedValue)((Object)list2), list, bl);
                }
            } else {
                map = styleMap.getCascadingStyles();
                list2 = map.get(string);
                if (list2 != null) {
                    list.addAll(list2);
                    int n = list2.size();
                    for (int i = 0; i < n; ++i) {
                        CascadingStyle cascadingStyle = (CascadingStyle)list2.get(i);
                        ParsedValue parsedValue = cascadingStyle.getParsedValue();
                        this.getMatchingLookupStyles(styleable, parsedValue, list, bl);
                    }
                }
            }
            if (cssMetaData.isInherits()) {
                for (map = styleable.getStyleableParent(); map != null; map = map.getStyleableParent()) {
                    List<CascadingStyle> list3 = list2 = map instanceof Node ? ((Node)((Object)map)).styleHelper : null;
                    if (list2 == null) continue;
                    super.getMatchingStyles((Styleable)((Object)map), cssMetaData, list, bl);
                }
            }
        }
    }

    private void getMatchingLookupStyles(Styleable styleable, ParsedValue parsedValue, List<CascadingStyle> list, boolean bl) {
        block12: {
            ParsedValue[][] parsedValueArray;
            Object v;
            block11: {
                if (parsedValue.isLookup() && (v = parsedValue.getValue()) instanceof String) {
                    parsedValueArray = (ParsedValue[][])v;
                    Styleable styleable2 = styleable;
                    do {
                        Map<String, List<CascadingStyle>> map;
                        StyleMap styleMap;
                        CssStyleHelper cssStyleHelper;
                        Node node = styleable2 instanceof Node ? (Node)styleable2 : null;
                        CssStyleHelper cssStyleHelper2 = cssStyleHelper = node != null ? node.styleHelper : null;
                        if (cssStyleHelper == null || (styleMap = cssStyleHelper.getStyleMap(styleable2)) == null || styleMap.isEmpty()) continue;
                        int n = list.size();
                        if (bl) {
                            map = cssStyleHelper.resolveRef(node, (String)parsedValueArray, styleMap, node.pseudoClassStates);
                            if (map != null) {
                                list.add((CascadingStyle)((Object)map));
                            }
                        } else {
                            map = styleMap.getCascadingStyles();
                            List<CascadingStyle> list2 = map.get(parsedValueArray);
                            if (list2 != null) {
                                list.addAll(list2);
                            }
                        }
                        int n2 = list.size();
                        for (int i = n; i < n2; ++i) {
                            CascadingStyle cascadingStyle = list.get(i);
                            this.getMatchingLookupStyles(styleable2, cascadingStyle.getParsedValue(), list, bl);
                        }
                    } while ((styleable2 = styleable2.getStyleableParent()) != null);
                }
                if (!parsedValue.isContainsLookups()) {
                    return;
                }
                v = parsedValue.getValue();
                if (!(v instanceof ParsedValue[][])) break block11;
                parsedValueArray = (ParsedValue[][])v;
                for (int i = 0; i < parsedValueArray.length; ++i) {
                    for (int j = 0; j < parsedValueArray[i].length; ++j) {
                        if (parsedValueArray[i][j] == null) continue;
                        this.getMatchingLookupStyles(styleable, parsedValueArray[i][j], list, bl);
                    }
                }
                break block12;
            }
            if (!(v instanceof ParsedValue[])) break block12;
            parsedValueArray = (ParsedValue[])v;
            for (int i = 0; i < parsedValueArray.length; ++i) {
                if (parsedValueArray[i] == null) continue;
                this.getMatchingLookupStyles(styleable, (ParsedValue)parsedValueArray[i], list, bl);
            }
        }
    }

    private static final class CacheContainer {
        private final StyleCache.Key styleCacheKey;
        private final CssMetaData<Styleable, Font> fontProp;
        private final int smapId;
        private final Map<StyleCacheEntry.Key, CalculatedValue> fontSizeCache;
        private final Map<CssMetaData, CalculatedValue> cssSetProperties;
        private boolean forceSlowpath = false;

        private CacheContainer(Node node, StyleMap styleMap, int n) {
            List<CssMetaData<? extends Styleable, ?>> list;
            int n2 = 0;
            int[] nArray = new int[n];
            nArray[n2++] = this.smapId = styleMap.getId();
            Styleable styleable = node.getStyleableParent();
            for (int i = 1; i < n; ++i) {
                if (styleable instanceof Node) {
                    list = (Node)styleable;
                    CssStyleHelper cssStyleHelper = ((Node)((Object)list)).styleHelper;
                    if (cssStyleHelper != null && cssStyleHelper.cacheContainer != null) {
                        nArray[n2++] = ((CssStyleHelper)cssStyleHelper).cacheContainer.smapId;
                    }
                }
                styleable = styleable.getStyleableParent();
            }
            this.styleCacheKey = new StyleCache.Key(nArray, n2);
            CssMetaData cssMetaData = null;
            list = node.getCssMetaData();
            int n3 = list != null ? list.size() : 0;
            for (int i = 0; i < n3; ++i) {
                CssMetaData cssMetaData2 = (CssMetaData)list.get(i);
                if (!"-fx-font".equals(cssMetaData2.getProperty())) continue;
                cssMetaData = cssMetaData2;
                break;
            }
            this.fontProp = cssMetaData;
            this.fontSizeCache = new HashMap<StyleCacheEntry.Key, CalculatedValue>();
            this.cssSetProperties = new HashMap<CssMetaData, CalculatedValue>();
        }

        private StyleMap getStyleMap(Styleable styleable) {
            if (styleable != null) {
                SubScene subScene = styleable instanceof Node ? ((Node)styleable).getSubScene() : null;
                return StyleManager.getInstance().getStyleMap(styleable, subScene, this.smapId);
            }
            return StyleMap.EMPTY_MAP;
        }
    }
}

