/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene;

import com.sun.javafx.scene.CameraHelper;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.SceneHelper;
import com.sun.javafx.scene.SubSceneHelper;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.SubScene;

public class SceneUtils {
    public static Point3D subSceneToScene(SubScene subScene, Point3D point3D) {
        SubScene subScene2 = subScene;
        while (subScene2 != null) {
            Point2D point2D = CameraHelper.project(SubSceneHelper.getEffectiveCamera(subScene), point3D);
            point3D = subScene2.localToScene(point2D.getX(), point2D.getY(), 0.0);
            subScene2 = NodeHelper.getSubScene(subScene2);
        }
        return point3D;
    }

    public static Point2D sceneToSubScenePlane(SubScene subScene, Point2D point2D) {
        point2D = SceneUtils.computeSubSceneCoordinates(point2D.getX(), point2D.getY(), subScene);
        return point2D;
    }

    private static Point2D computeSubSceneCoordinates(double d, double d2, SubScene subScene) {
        SubScene subScene2 = NodeHelper.getSubScene(subScene);
        if (subScene2 == null) {
            return CameraHelper.pickNodeXYPlane(SceneHelper.getEffectiveCamera(subScene.getScene()), subScene, d, d2);
        }
        Point2D point2D = SceneUtils.computeSubSceneCoordinates(d, d2, subScene2);
        if (point2D != null) {
            point2D = CameraHelper.pickNodeXYPlane(SubSceneHelper.getEffectiveCamera(subScene2), subScene, point2D.getX(), point2D.getY());
        }
        return point2D;
    }
}

