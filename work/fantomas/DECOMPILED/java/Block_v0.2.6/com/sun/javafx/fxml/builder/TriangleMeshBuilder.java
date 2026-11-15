/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.fxml.builder;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import javafx.util.Builder;

public class TriangleMeshBuilder
extends TreeMap<String, Object>
implements Builder<TriangleMesh> {
    private static final String VALUE_SEPARATOR_REGEX = "[,\\s]+";
    private float[] points;
    private float[] texCoords;
    private float[] normals;
    private int[] faces;
    private int[] faceSmoothingGroups;
    private VertexFormat vertexFormat;

    @Override
    public TriangleMesh build() {
        TriangleMesh triangleMesh = new TriangleMesh();
        if (this.points != null) {
            triangleMesh.getPoints().setAll(this.points);
        }
        if (this.texCoords != null) {
            triangleMesh.getTexCoords().setAll(this.texCoords);
        }
        if (this.faces != null) {
            triangleMesh.getFaces().setAll(this.faces);
        }
        if (this.faceSmoothingGroups != null) {
            triangleMesh.getFaceSmoothingGroups().setAll(this.faceSmoothingGroups);
        }
        if (this.normals != null) {
            triangleMesh.getNormals().setAll(this.normals);
        }
        if (this.vertexFormat != null) {
            triangleMesh.setVertexFormat(this.vertexFormat);
        }
        return triangleMesh;
    }

    @Override
    public Object put(String string, Object object) {
        if ("points".equalsIgnoreCase(string)) {
            String[] stringArray = ((String)object).split(VALUE_SEPARATOR_REGEX);
            this.points = new float[stringArray.length];
            for (int i = 0; i < stringArray.length; ++i) {
                this.points[i] = Float.parseFloat(stringArray[i]);
            }
        } else if ("texcoords".equalsIgnoreCase(string)) {
            String[] stringArray = ((String)object).split(VALUE_SEPARATOR_REGEX);
            this.texCoords = new float[stringArray.length];
            for (int i = 0; i < stringArray.length; ++i) {
                this.texCoords[i] = Float.parseFloat(stringArray[i]);
            }
        } else if ("faces".equalsIgnoreCase(string)) {
            String[] stringArray = ((String)object).split(VALUE_SEPARATOR_REGEX);
            this.faces = new int[stringArray.length];
            for (int i = 0; i < stringArray.length; ++i) {
                this.faces[i] = Integer.parseInt(stringArray[i]);
            }
        } else if ("facesmoothinggroups".equalsIgnoreCase(string)) {
            String[] stringArray = ((String)object).split(VALUE_SEPARATOR_REGEX);
            this.faceSmoothingGroups = new int[stringArray.length];
            for (int i = 0; i < stringArray.length; ++i) {
                this.faceSmoothingGroups[i] = Integer.parseInt(stringArray[i]);
            }
        } else if ("normals".equalsIgnoreCase(string)) {
            String[] stringArray = ((String)object).split(VALUE_SEPARATOR_REGEX);
            this.normals = new float[stringArray.length];
            for (int i = 0; i < stringArray.length; ++i) {
                this.normals[i] = Float.parseFloat(stringArray[i]);
            }
        } else if ("vertexformat".equalsIgnoreCase(string)) {
            if (object instanceof VertexFormat) {
                this.vertexFormat = (VertexFormat)object;
            } else if ("point_texcoord".equalsIgnoreCase((String)object)) {
                this.vertexFormat = VertexFormat.POINT_TEXCOORD;
            } else if ("point_normal_texcoord".equalsIgnoreCase((String)object)) {
                this.vertexFormat = VertexFormat.POINT_NORMAL_TEXCOORD;
            }
        }
        return super.put(string.toLowerCase(Locale.ROOT), object);
    }

    @Override
    public Set<Map.Entry<String, Object>> entrySet() {
        return super.entrySet();
    }
}

