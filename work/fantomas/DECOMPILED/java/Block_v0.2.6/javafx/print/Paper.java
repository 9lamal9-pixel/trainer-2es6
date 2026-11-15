/*
 * Decompiled with CFR 0.152.
 */
package javafx.print;

import com.sun.javafx.print.Units;

public final class Paper {
    private String name;
    private double width;
    private double height;
    private Units units;
    public static final Paper A0 = new Paper("A0", 841.0, 1189.0, Units.MM);
    public static final Paper A1 = new Paper("A1", 594.0, 841.0, Units.MM);
    public static final Paper A2 = new Paper("A2", 420.0, 594.0, Units.MM);
    public static final Paper A3 = new Paper("A3", 297.0, 420.0, Units.MM);
    public static final Paper A4 = new Paper("A4", 210.0, 297.0, Units.MM);
    public static final Paper A5 = new Paper("A5", 148.0, 210.0, Units.MM);
    public static final Paper A6 = new Paper("A6", 105.0, 148.0, Units.MM);
    public static final Paper DESIGNATED_LONG = new Paper("Designated Long", 110.0, 220.0, Units.MM);
    public static final Paper NA_LETTER = new Paper("Letter", 8.5, 11.0, Units.INCH);
    public static final Paper LEGAL = new Paper("Legal", 8.4, 14.0, Units.INCH);
    public static final Paper TABLOID = new Paper("Tabloid", 11.0, 17.0, Units.INCH);
    public static final Paper EXECUTIVE = new Paper("Executive", 7.25, 10.5, Units.INCH);
    public static final Paper NA_8X10 = new Paper("8x10", 8.0, 10.0, Units.INCH);
    public static final Paper MONARCH_ENVELOPE = new Paper("Monarch Envelope", 3.87, 7.5, Units.INCH);
    public static final Paper NA_NUMBER_10_ENVELOPE = new Paper("Number 10 Envelope", 4.125, 9.5, Units.INCH);
    public static final Paper C = new Paper("C", 17.0, 22.0, Units.INCH);
    public static final Paper JIS_B4 = new Paper("B4", 257.0, 364.0, Units.MM);
    public static final Paper JIS_B5 = new Paper("B5", 182.0, 257.0, Units.MM);
    public static final Paper JIS_B6 = new Paper("B6", 128.0, 182.0, Units.MM);
    public static final Paper JAPANESE_POSTCARD = new Paper("Japanese Postcard", 100.0, 148.0, Units.MM);

    Paper(String string, double d, double d2, Units units) throws IllegalArgumentException {
        if (d <= 0.0 || d2 <= 0.0) {
            throw new IllegalArgumentException("Illegal dimension");
        }
        if (string == null) {
            throw new IllegalArgumentException("Null name");
        }
        this.name = string;
        this.width = d;
        this.height = d2;
        this.units = units;
    }

    public final String getName() {
        return this.name;
    }

    private double getSizeInPoints(double d) {
        switch (this.units) {
            case POINT: {
                return (int)(d + 0.5);
            }
            case INCH: {
                return (int)(d * 72.0 + 0.5);
            }
            case MM: {
                return (int)(d * 72.0 / 25.4 + 0.5);
            }
        }
        return d;
    }

    public final double getWidth() {
        return this.getSizeInPoints(this.width);
    }

    public final double getHeight() {
        return this.getSizeInPoints(this.height);
    }

    public final int hashCode() {
        return (int)this.width + ((int)this.height << 16) + this.units.hashCode();
    }

    public final boolean equals(Object object) {
        return object != null && object instanceof Paper && this.name.equals(((Paper)object).name) && this.width == ((Paper)object).width && this.height == ((Paper)object).height && this.units == ((Paper)object).units;
    }

    public final String toString() {
        return "Paper: " + this.name + " size=" + this.width + "x" + this.height + " " + this.units;
    }
}

