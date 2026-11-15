/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.shape;

public final class VertexFormat {
    public static final VertexFormat POINT_TEXCOORD = new VertexFormat("POINT_TEXCOORD", 2, 0, -1, 1);
    public static final VertexFormat POINT_NORMAL_TEXCOORD = new VertexFormat("POINT_NORMAL_TEXCOORD", 3, 0, 1, 2);
    private static final int POINT_ELEMENT_SIZE = 3;
    private static final int NORMAL_ELEMENT_SIZE = 3;
    private static final int TEXCOORD_ELEMENT_SIZE = 2;
    private final String name;
    private final int vertexIndexSize;
    private final int pointIndexOffset;
    private final int normalIndexOffset;
    private final int texCoordIndexOffset;

    private VertexFormat(String string, int n, int n2, int n3, int n4) {
        this.name = string;
        this.vertexIndexSize = n;
        this.pointIndexOffset = n2;
        this.normalIndexOffset = n3;
        this.texCoordIndexOffset = n4;
    }

    int getPointElementSize() {
        return 3;
    }

    int getNormalElementSize() {
        return 3;
    }

    int getTexCoordElementSize() {
        return 2;
    }

    public int getVertexIndexSize() {
        return this.vertexIndexSize;
    }

    public int getPointIndexOffset() {
        return this.pointIndexOffset;
    }

    public int getNormalIndexOffset() {
        return this.normalIndexOffset;
    }

    public int getTexCoordIndexOffset() {
        return this.texCoordIndexOffset;
    }

    public String toString() {
        return this.name;
    }
}

