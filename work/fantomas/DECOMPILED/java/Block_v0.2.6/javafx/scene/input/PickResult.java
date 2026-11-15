/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.input;

import javafx.beans.NamedArg;
import javafx.event.EventTarget;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Node;

public class PickResult {
    public static final int FACE_UNDEFINED = -1;
    private Node node;
    private Point3D point;
    private double distance = Double.POSITIVE_INFINITY;
    private int face = -1;
    private Point3D normal;
    private Point2D texCoord;

    public PickResult(@NamedArg(value="node") Node node, @NamedArg(value="point") Point3D point3D, @NamedArg(value="distance") double d, @NamedArg(value="face") int n, @NamedArg(value="texCoord") Point2D point2D) {
        this.node = node;
        this.point = point3D;
        this.distance = d;
        this.face = n;
        this.normal = null;
        this.texCoord = point2D;
    }

    public PickResult(@NamedArg(value="node") Node node, @NamedArg(value="point") Point3D point3D, @NamedArg(value="distance") double d, @NamedArg(value="face") int n, @NamedArg(value="normal") Point3D point3D2, @NamedArg(value="texCoord") Point2D point2D) {
        this.node = node;
        this.point = point3D;
        this.distance = d;
        this.face = n;
        this.normal = point3D2;
        this.texCoord = point2D;
    }

    public PickResult(@NamedArg(value="node") Node node, @NamedArg(value="point") Point3D point3D, @NamedArg(value="distance") double d) {
        this.node = node;
        this.point = point3D;
        this.distance = d;
        this.face = -1;
        this.normal = null;
        this.texCoord = null;
    }

    public PickResult(@NamedArg(value="target") EventTarget eventTarget, @NamedArg(value="sceneX") double d, @NamedArg(value="sceneY") double d2) {
        this(eventTarget instanceof Node ? (Node)eventTarget : null, eventTarget instanceof Node ? ((Node)eventTarget).sceneToLocal(d, d2, 0.0) : new Point3D(d, d2, 0.0), 1.0);
    }

    public final Node getIntersectedNode() {
        return this.node;
    }

    public final Point3D getIntersectedPoint() {
        return this.point;
    }

    public final double getIntersectedDistance() {
        return this.distance;
    }

    public final int getIntersectedFace() {
        return this.face;
    }

    public final Point3D getIntersectedNormal() {
        return this.normal;
    }

    public final Point2D getIntersectedTexCoord() {
        return this.texCoord;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("PickResult [");
        stringBuilder.append("node = ").append(this.getIntersectedNode()).append(", point = ").append(this.getIntersectedPoint()).append(", distance = ").append(this.getIntersectedDistance());
        if (this.getIntersectedFace() != -1) {
            stringBuilder.append(", face = ").append(this.getIntersectedFace());
        }
        if (this.getIntersectedNormal() != null) {
            stringBuilder.append(", normal = ").append(this.getIntersectedNormal());
        }
        if (this.getIntersectedTexCoord() != null) {
            stringBuilder.append(", texCoord = ").append(this.getIntersectedTexCoord());
        }
        return stringBuilder.toString();
    }
}

