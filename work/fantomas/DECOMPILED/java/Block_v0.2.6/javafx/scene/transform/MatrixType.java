/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.transform;

public enum MatrixType {
    MT_2D_2x3(2, 3),
    MT_2D_3x3(3, 3),
    MT_3D_3x4(3, 4),
    MT_3D_4x4(4, 4);

    private int rows;
    private int cols;

    private MatrixType(int n2, int n3) {
        this.rows = n2;
        this.cols = n3;
    }

    public int elements() {
        return this.rows * this.cols;
    }

    public int rows() {
        return this.rows;
    }

    public int columns() {
        return this.cols;
    }

    public boolean is2D() {
        return this == MT_2D_2x3 || this == MT_2D_3x3;
    }
}

