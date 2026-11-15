/*
 * Decompiled with CFR 0.152.
 */
package javafx.css.converter;

import java.util.Map;
import javafx.css.ParsedValue;
import javafx.css.StyleConverter;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;

public class ShapeConverter
extends StyleConverter<String, Shape> {
    private static final ShapeConverter INSTANCE = new ShapeConverter();
    private static Map<ParsedValue<String, Shape>, Shape> cache;

    public static StyleConverter<String, Shape> getInstance() {
        return INSTANCE;
    }

    @Override
    public Shape convert(ParsedValue<String, Shape> parsedValue, Font font) {
        Shape shape = (Shape)super.getCachedValue(parsedValue);
        if (shape != null) {
            return shape;
        }
        String string = parsedValue.getValue();
        if (string == null || string.isEmpty()) {
            return null;
        }
        SVGPath sVGPath = new SVGPath();
        sVGPath.setContent(string);
        super.cacheValue(parsedValue, sVGPath);
        return sVGPath;
    }

    public static void clearCache() {
        if (cache != null) {
            cache.clear();
        }
    }
}

