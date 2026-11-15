/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.fxml.builder;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.util.Builder;

public final class JavaFXFontBuilder
extends AbstractMap<String, Object>
implements Builder<Font> {
    private String name = null;
    private double size = 12.0;
    private FontWeight weight = null;
    private FontPosture posture = null;
    private URL url = null;

    @Override
    public Font build() {
        Font font;
        if (this.url != null) {
            InputStream inputStream = null;
            try {
                inputStream = this.url.openStream();
                font = Font.loadFont(inputStream, this.size);
            }
            catch (Exception exception) {
                throw new RuntimeException("Load of font file failed from " + this.url, exception);
            }
            finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
                catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }
        if (this.weight == null && this.posture == null) {
            font = new Font(this.name, this.size);
        } else {
            if (this.weight == null) {
                this.weight = FontWeight.NORMAL;
            }
            if (this.posture == null) {
                this.posture = FontPosture.REGULAR;
            }
            font = Font.font(this.name, this.weight, this.posture, this.size);
        }
        return font;
    }

    @Override
    public Object put(String string, Object object) {
        if ("name".equals(string)) {
            if (object instanceof URL) {
                this.url = (URL)object;
            } else {
                this.name = (String)object;
            }
        } else if ("size".equals(string)) {
            this.size = Double.parseDouble((String)object);
        } else if ("style".equals(string)) {
            String string2 = (String)object;
            if (string2 != null && string2.length() > 0) {
                boolean bl = false;
                StringTokenizer stringTokenizer = new StringTokenizer(string2, " ");
                while (stringTokenizer.hasMoreTokens()) {
                    FontWeight fontWeight;
                    String string3 = stringTokenizer.nextToken();
                    if (!bl && (fontWeight = FontWeight.findByName(string3)) != null) {
                        this.weight = fontWeight;
                        bl = true;
                        continue;
                    }
                    FontPosture fontPosture = FontPosture.findByName(string3);
                    if (fontPosture == null) continue;
                    this.posture = fontPosture;
                }
            }
        } else if ("url".equals(string)) {
            if (object instanceof URL) {
                this.url = (URL)object;
            } else {
                try {
                    this.url = new URL(object.toString());
                }
                catch (MalformedURLException malformedURLException) {
                    throw new IllegalArgumentException("Invalid url " + object.toString(), malformedURLException);
                }
            }
        } else {
            throw new IllegalArgumentException("Unknown Font property: " + string);
        }
        return null;
    }

    @Override
    public boolean containsKey(Object object) {
        return false;
    }

    @Override
    public Object get(Object object) {
        return null;
    }

    @Override
    public Set<Map.Entry<String, Object>> entrySet() {
        throw new UnsupportedOperationException();
    }
}

