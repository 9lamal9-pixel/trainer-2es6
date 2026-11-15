/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.input;

import com.sun.javafx.scene.CameraHelper;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.SceneHelper;
import com.sun.javafx.scene.SceneUtils;
import com.sun.javafx.scene.SubSceneHelper;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.input.PickResult;
import javafx.scene.input.TransferMode;

public class InputEventUtils {
    private static final List<TransferMode> TM_ANY = Collections.unmodifiableList(Arrays.asList(TransferMode.COPY, TransferMode.MOVE, TransferMode.LINK));
    private static final List<TransferMode> TM_COPY_OR_MOVE = Collections.unmodifiableList(Arrays.asList(TransferMode.COPY, TransferMode.MOVE));

    public static Point3D recomputeCoordinates(PickResult pickResult, Object object) {
        boolean bl;
        Point3D point3D = pickResult.getIntersectedPoint();
        if (point3D == null) {
            return new Point3D(Double.NaN, Double.NaN, Double.NaN);
        }
        Node node = pickResult.getIntersectedNode();
        Node node2 = object instanceof Node ? (Node)object : null;
        SubScene subScene = node == null ? null : NodeHelper.getSubScene(node);
        SubScene subScene2 = node2 == null ? null : NodeHelper.getSubScene(node2);
        boolean bl2 = bl = subScene != subScene2;
        if (node != null) {
            point3D = node.localToScene(point3D);
            if (bl && subScene != null) {
                point3D = SceneUtils.subSceneToScene(subScene, point3D);
            }
        }
        if (node2 != null) {
            if (bl && subScene2 != null) {
                Point2D point2D = CameraHelper.project(SceneHelper.getEffectiveCamera(node2.getScene()), point3D);
                point3D = (point2D = SceneUtils.sceneToSubScenePlane(subScene2, point2D)) == null ? null : CameraHelper.pickProjectPlane(SubSceneHelper.getEffectiveCamera(subScene2), point2D.getX(), point2D.getY());
            }
            if (point3D != null) {
                point3D = node2.sceneToLocal(point3D);
            }
            if (point3D == null) {
                point3D = new Point3D(Double.NaN, Double.NaN, Double.NaN);
            }
        }
        return point3D;
    }

    public static List<TransferMode> safeTransferModes(TransferMode[] transferModeArray) {
        if (transferModeArray == TransferMode.ANY) {
            return TM_ANY;
        }
        if (transferModeArray == TransferMode.COPY_OR_MOVE) {
            return TM_COPY_OR_MOVE;
        }
        return Arrays.asList(transferModeArray);
    }
}

