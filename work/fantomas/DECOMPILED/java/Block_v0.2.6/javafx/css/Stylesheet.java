/*
 * Decompiled with CFR 0.152.
 */
package javafx.css;

import com.sun.javafx.collections.TrackableObservableList;
import com.sun.javafx.css.FontFaceImpl;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssParser;
import javafx.css.Declaration;
import javafx.css.FontFace;
import javafx.css.Rule;
import javafx.css.Selector;
import javafx.css.StyleConverter;
import javafx.css.StyleOrigin;

public class Stylesheet {
    static final int BINARY_CSS_VERSION = 6;
    private final String url;
    private StyleOrigin origin = StyleOrigin.AUTHOR;
    private final ObservableList<Rule> rules = new TrackableObservableList<Rule>(){

        @Override
        protected void onChanged(ListChangeListener.Change<Rule> change) {
            change.reset();
            while (change.next()) {
                if (change.wasAdded()) {
                    for (Rule rule : change.getAddedSubList()) {
                        rule.setStylesheet(Stylesheet.this);
                    }
                    continue;
                }
                if (!change.wasRemoved()) continue;
                for (Rule rule : change.getRemoved()) {
                    if (rule.getStylesheet() != Stylesheet.this) continue;
                    rule.setStylesheet(null);
                }
            }
        }
    };
    private final List<FontFace> fontFaces = new ArrayList<FontFace>();
    private String[] stringStore;

    public String getUrl() {
        return this.url;
    }

    public StyleOrigin getOrigin() {
        return this.origin;
    }

    public void setOrigin(StyleOrigin styleOrigin) {
        this.origin = styleOrigin;
    }

    Stylesheet() {
        this(null);
    }

    Stylesheet(String string) {
        this.url = string;
    }

    public List<Rule> getRules() {
        return this.rules;
    }

    public List<FontFace> getFontFaces() {
        return this.fontFaces;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof Stylesheet) {
            Stylesheet stylesheet = (Stylesheet)object;
            if (this.url == null && stylesheet.url == null) {
                return true;
            }
            if (this.url == null || stylesheet.url == null) {
                return false;
            }
            return this.url.equals(stylesheet.url);
        }
        return false;
    }

    public int hashCode() {
        int n = 7;
        n = 13 * n + (this.url != null ? this.url.hashCode() : 0);
        return n;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("/* ");
        if (this.url != null) {
            stringBuilder.append(this.url);
        }
        if (this.rules.isEmpty()) {
            stringBuilder.append(" */");
        } else {
            stringBuilder.append(" */\n");
            for (int i = 0; i < this.rules.size(); ++i) {
                stringBuilder.append(this.rules.get(i));
                stringBuilder.append('\n');
            }
        }
        return stringBuilder.toString();
    }

    final void writeBinary(DataOutputStream dataOutputStream, StyleConverter.StringStore stringStore) throws IOException {
        int n = stringStore.addString(this.origin.name());
        dataOutputStream.writeShort(n);
        dataOutputStream.writeShort(this.rules.size());
        for (Rule rule : this.rules) {
            rule.writeBinary(dataOutputStream, stringStore);
        }
        List<FontFace> list = this.getFontFaces();
        int n2 = list != null ? list.size() : 0;
        dataOutputStream.writeShort(n2);
        for (int i = 0; i < n2; ++i) {
            FontFace fontFace = (FontFace)list.get(i);
            if (!(fontFace instanceof FontFaceImpl)) continue;
            ((FontFaceImpl)fontFace).writeBinary(dataOutputStream, stringStore);
        }
    }

    final void readBinary(int n, DataInputStream dataInputStream, String[] stringArray) throws IOException {
        this.stringStore = stringArray;
        short s = dataInputStream.readShort();
        this.setOrigin(StyleOrigin.valueOf(stringArray[s]));
        int n2 = dataInputStream.readShort();
        ArrayList<Rule> arrayList = new ArrayList<Rule>(n2);
        for (int i = 0; i < n2; ++i) {
            arrayList.add(Rule.readBinary(n, dataInputStream, stringArray));
        }
        this.rules.addAll((Collection<Rule>)arrayList);
        if (n >= 5) {
            List<FontFace> list = this.getFontFaces();
            int n3 = dataInputStream.readShort();
            for (int i = 0; i < n3; ++i) {
                FontFaceImpl fontFaceImpl = FontFaceImpl.readBinary(n, dataInputStream, stringArray);
                list.add(fontFaceImpl);
            }
        }
    }

    final String[] getStringStore() {
        return this.stringStore;
    }

    public static Stylesheet loadBinary(URL uRL) throws IOException {
        Stylesheet stylesheet;
        block12: {
            if (uRL == null) {
                return null;
            }
            stylesheet = null;
            try (DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(uRL.openStream(), 40960));){
                short s = dataInputStream.readShort();
                if (s > 6) {
                    throw new IOException(uRL.toString() + " wrong binary CSS version: " + s + ". Expected version less than or equal to6");
                }
                String[] stringArray = StyleConverter.StringStore.readBinary(dataInputStream);
                stylesheet = new Stylesheet(uRL.toExternalForm());
                try {
                    dataInputStream.mark(Integer.MAX_VALUE);
                    stylesheet.readBinary(s, dataInputStream, stringArray);
                }
                catch (Exception exception) {
                    stylesheet = new Stylesheet(uRL.toExternalForm());
                    dataInputStream.reset();
                    if (s == 2) {
                        stylesheet.readBinary(3, dataInputStream, stringArray);
                        break block12;
                    }
                    stylesheet.readBinary(6, dataInputStream, stringArray);
                }
            }
            catch (FileNotFoundException fileNotFoundException) {
                // empty catch block
            }
        }
        return stylesheet;
    }

    public static void convertToBinary(File file, File file2) throws IOException {
        if (file == null || file2 == null) {
            throw new IllegalArgumentException("parameters may not be null");
        }
        if (file.getAbsolutePath().equals(file2.getAbsolutePath())) {
            throw new IllegalArgumentException("source and destination may not be the same");
        }
        if (!file.canRead()) {
            throw new IllegalArgumentException("cannot read source file");
        }
        if (file2.exists() ? !file2.canWrite() : !file2.createNewFile()) {
            throw new IllegalArgumentException("cannot write destination file");
        }
        URI uRI = file.toURI();
        Stylesheet stylesheet = new CssParser().parse(uRI.toURL());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        StyleConverter.StringStore stringStore = new StyleConverter.StringStore();
        stylesheet.writeBinary(dataOutputStream, stringStore);
        dataOutputStream.flush();
        dataOutputStream.close();
        FileOutputStream fileOutputStream = new FileOutputStream(file2);
        DataOutputStream dataOutputStream2 = new DataOutputStream(fileOutputStream);
        dataOutputStream2.writeShort(6);
        stringStore.writeBinary(dataOutputStream2);
        dataOutputStream2.write(byteArrayOutputStream.toByteArray());
        dataOutputStream2.flush();
        dataOutputStream2.close();
    }

    void importStylesheet(Stylesheet stylesheet) {
        if (stylesheet == null) {
            return;
        }
        List<Rule> list = stylesheet.getRules();
        if (list == null || list.isEmpty()) {
            return;
        }
        ArrayList<Rule> arrayList = new ArrayList<Rule>(list.size());
        for (Rule rule : list) {
            ObservableList<Selector> observableList = rule.getSelectors();
            List<Declaration> list2 = rule.getUnobservedDeclarationList();
            arrayList.add(new Rule(observableList, list2));
        }
        this.rules.addAll((Collection<Rule>)arrayList);
    }
}

