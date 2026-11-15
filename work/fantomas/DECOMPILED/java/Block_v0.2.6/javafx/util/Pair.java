/*
 * Decompiled with CFR 0.152.
 */
package javafx.util;

import java.io.Serializable;
import javafx.beans.NamedArg;

public class Pair<K, V>
implements Serializable {
    private K key;
    private V value;

    public K getKey() {
        return this.key;
    }

    public V getValue() {
        return this.value;
    }

    public Pair(@NamedArg(value="key") K k, @NamedArg(value="value") V v) {
        this.key = k;
        this.value = v;
    }

    public String toString() {
        return this.key + "=" + this.value;
    }

    public int hashCode() {
        int n = 7;
        n = 31 * n + (this.key != null ? this.key.hashCode() : 0);
        n = 31 * n + (this.value != null ? this.value.hashCode() : 0);
        return n;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof Pair) {
            Pair pair = (Pair)object;
            if (this.key != null ? !this.key.equals(pair.key) : pair.key != null) {
                return false;
            }
            return !(this.value != null ? !this.value.equals(pair.value) : pair.value != null);
        }
        return false;
    }
}

