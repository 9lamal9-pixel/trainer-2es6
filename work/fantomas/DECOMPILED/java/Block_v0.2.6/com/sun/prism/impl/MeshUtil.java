/*
 * Decompiled with CFR 0.152.
 */
package com.sun.prism.impl;

import com.sun.javafx.geom.Quat4f;
import com.sun.javafx.geom.Vec2f;
import com.sun.javafx.geom.Vec3f;
import com.sun.prism.impl.MeshTempState;

class MeshUtil {
    static final float NORMAL_WELD_COS = 0.9952f;
    static final float TANGENT_WELD_COS = 0.866f;
    static final float G_UV_PARALLEL = 0.9988f;
    static final float COS_1_DEGREE = 0.9998477f;
    static final float BIG_ENOUGH_NORMA2 = 0.0625f;
    static final double PI = Math.PI;
    static final float INV_SQRT2 = 0.70710677f;
    static final float DEAD_FACE = 9.094947E-13f;
    static final float MAGIC_SMALL = 1.0E-10f;
    static final float COS110 = -0.33333334f;

    private MeshUtil() {
    }

    static boolean isDeadFace(float f) {
        return f < 9.094947E-13f;
    }

    static boolean isDeadFace(int[] nArray) {
        return nArray[0] == nArray[1] || nArray[1] == nArray[2] || nArray[2] == nArray[0];
    }

    static boolean isNormalAlmostEqual(Vec3f vec3f, Vec3f vec3f2) {
        return vec3f.dot(vec3f2) >= 0.9998477f;
    }

    static boolean isTangentOk(Vec3f[] vec3fArray, Vec3f[] vec3fArray2) {
        return vec3fArray[0].dot(vec3fArray2[0]) >= 0.9952f && vec3fArray[1].dot(vec3fArray2[1]) >= 0.866f && vec3fArray[2].dot(vec3fArray2[2]) >= 0.866f;
    }

    static boolean isNormalOkAfterWeld(Vec3f vec3f) {
        return vec3f.dot(vec3f) > 0.0625f;
    }

    static boolean isTangentOK(Vec3f[] vec3fArray) {
        return MeshUtil.isTangentOk(vec3fArray, vec3fArray);
    }

    static boolean isOppositeLookingNormals(Vec3f[] vec3fArray, Vec3f[] vec3fArray2) {
        float f = vec3fArray[0].dot(vec3fArray2[0]);
        return f < -0.33333334f;
    }

    static float fabs(float f) {
        return f < 0.0f ? -f : f;
    }

    static void getOrt(Vec3f vec3f, Vec3f vec3f2) {
        vec3f2.cross(vec3f, vec3f2);
        vec3f2.cross(vec3f2, vec3f);
    }

    static void orthogonalizeTB(Vec3f[] vec3fArray) {
        MeshUtil.getOrt(vec3fArray[0], vec3fArray[1]);
        MeshUtil.getOrt(vec3fArray[0], vec3fArray[2]);
        vec3fArray[1].normalize();
        vec3fArray[2].normalize();
    }

    static void computeTBNNormalized(Vec3f vec3f, Vec3f vec3f2, Vec3f vec3f3, Vec2f vec2f, Vec2f vec2f2, Vec2f vec2f3, Vec3f[] vec3fArray) {
        MeshTempState meshTempState = MeshTempState.getInstance();
        Vec3f vec3f4 = meshTempState.vec3f1;
        Vec3f vec3f5 = meshTempState.vec3f2;
        Vec3f vec3f6 = meshTempState.vec3f3;
        vec3f5.sub(vec3f2, vec3f);
        vec3f6.sub(vec3f3, vec3f);
        vec3f4.cross(vec3f5, vec3f6);
        vec3fArray[0].set(vec3f4);
        vec3fArray[0].normalize();
        vec3f5.set(0.0f, vec2f2.x - vec2f.x, vec2f2.y - vec2f.y);
        vec3f6.set(0.0f, vec2f3.x - vec2f.x, vec2f3.y - vec2f.y);
        if (vec3f5.y * vec3f6.z == vec3f5.z * vec3f6.y) {
            MeshUtil.generateTB(vec3f, vec3f2, vec3f3, vec3fArray);
            return;
        }
        vec3f5.x = vec3f2.x - vec3f.x;
        vec3f6.x = vec3f3.x - vec3f.x;
        vec3f4.cross(vec3f5, vec3f6);
        vec3fArray[1].x = -vec3f4.y / vec3f4.x;
        vec3fArray[2].x = -vec3f4.z / vec3f4.x;
        vec3f5.x = vec3f2.y - vec3f.y;
        vec3f6.x = vec3f3.y - vec3f.y;
        vec3f4.cross(vec3f5, vec3f6);
        vec3fArray[1].y = -vec3f4.y / vec3f4.x;
        vec3fArray[2].y = -vec3f4.z / vec3f4.x;
        vec3f5.x = vec3f2.z - vec3f.z;
        vec3f6.x = vec3f3.z - vec3f.z;
        vec3f4.cross(vec3f5, vec3f6);
        vec3fArray[1].z = -vec3f4.y / vec3f4.x;
        vec3fArray[2].z = -vec3f4.z / vec3f4.x;
        vec3fArray[1].normalize();
        vec3fArray[2].normalize();
    }

    static void fixParallelTB(Vec3f[] vec3fArray) {
        MeshTempState meshTempState = MeshTempState.getInstance();
        Vec3f vec3f = meshTempState.vec3f1;
        vec3f.add(vec3fArray[1], vec3fArray[2]);
        Vec3f vec3f2 = meshTempState.vec3f2;
        vec3f2.cross(vec3fArray[0], vec3f);
        vec3f.normalize();
        vec3f2.normalize();
        vec3fArray[1].add(vec3f, vec3f2);
        vec3fArray[1].mul(0.70710677f);
        vec3fArray[2].sub(vec3f, vec3f2);
        vec3fArray[2].mul(0.70710677f);
    }

    static void generateTB(Vec3f vec3f, Vec3f vec3f2, Vec3f vec3f3, Vec3f[] vec3fArray) {
        MeshTempState meshTempState = MeshTempState.getInstance();
        Vec3f vec3f4 = meshTempState.vec3f1;
        vec3f4.sub(vec3f2, vec3f);
        Vec3f vec3f5 = meshTempState.vec3f2;
        vec3f5.sub(vec3f3, vec3f);
        if (vec3f4.dot(vec3f4) > vec3f5.dot(vec3f5)) {
            vec3fArray[1].set(vec3f4);
            vec3fArray[1].normalize();
            vec3fArray[2].cross(vec3fArray[0], vec3fArray[1]);
        } else {
            vec3fArray[2].set(vec3f5);
            vec3fArray[2].normalize();
            vec3fArray[1].cross(vec3fArray[2], vec3fArray[0]);
        }
    }

    static double clamp(double d, double d2, double d3) {
        return d < d3 ? (d > d2 ? d : d2) : d3;
    }

    static void fixTSpace(Vec3f[] vec3fArray) {
        float f = vec3fArray[0].length();
        MeshTempState meshTempState = MeshTempState.getInstance();
        Vec3f vec3f = meshTempState.vec3f1;
        vec3f.set(vec3fArray[1]);
        Vec3f vec3f2 = meshTempState.vec3f2;
        vec3f2.set(vec3fArray[2]);
        MeshUtil.getOrt(vec3fArray[0], vec3f);
        MeshUtil.getOrt(vec3fArray[0], vec3f2);
        float f2 = vec3f.length();
        float f3 = vec3f2.length();
        double d = vec3f.dot(vec3f2) / (f2 * f3);
        Vec3f vec3f3 = meshTempState.vec3f3;
        Vec3f vec3f4 = meshTempState.vec3f4;
        if ((double)MeshUtil.fabs((float)d) > 0.998) {
            Vec3f vec3f5 = meshTempState.vec3f5;
            vec3f5.cross(vec3fArray[0], vec3f);
            vec3f5.normalize();
            vec3f4.set(vec3f5);
            if (vec3f5.dot(vec3f2) < 0.0f) {
                vec3f4.mul(-1.0f);
            }
            vec3f3.set(vec3f);
            vec3f3.mul(1.0f / f2);
        } else {
            double d2 = Math.acos(MeshUtil.clamp(d, -1.0, 1.0));
            double d3 = (1.5707963267948966 - d2) * 0.5;
            Vec2f vec2f = meshTempState.vec2f1;
            vec2f.set((float)Math.sin(d3), (float)Math.cos(d3));
            Vec2f vec2f2 = meshTempState.vec2f2;
            vec2f2.set((float)Math.sin(d3 + d2), (float)Math.cos(d3 + d2));
            Vec3f vec3f6 = meshTempState.vec3f5;
            vec3f6.set(vec3f2);
            MeshUtil.getOrt(vec3f, vec3f6);
            float f4 = vec3f6.length();
            vec3f3.set(vec3f);
            vec3f3.mul(vec2f.y / f2);
            Vec3f vec3f7 = meshTempState.vec3f6;
            vec3f7.set(vec3f6);
            vec3f7.mul(vec2f.x / f4);
            vec3f3.sub(vec3f7);
            vec3f4.set(vec3f);
            vec3f4.mul(vec2f2.y / f2);
            vec3f7.set(vec3f6);
            vec3f7.mul(vec2f2.x / f4);
            vec3f4.add(vec3f7);
            float f5 = vec3f3.dot(vec3f);
            float f6 = vec3f4.dot(vec3f2);
        }
        vec3fArray[1].set(vec3f3);
        vec3fArray[2].set(vec3f4);
        vec3fArray[0].mul(1.0f / f);
    }

    static void buildQuat(Vec3f[] vec3fArray, Quat4f quat4f) {
        MeshTempState meshTempState = MeshTempState.getInstance();
        float[][] fArray = meshTempState.matrix;
        float[] fArray2 = meshTempState.vector;
        for (int i = 0; i < 3; ++i) {
            fArray[i][0] = vec3fArray[i].x;
            fArray[i][1] = vec3fArray[i].y;
            fArray[i][2] = vec3fArray[i].z;
        }
        float f = fArray[0][0] + fArray[1][1] + fArray[2][2];
        if (f > 0.0f) {
            float f2 = (float)Math.sqrt(f + 1.0f);
            float f3 = 0.5f / f2;
            quat4f.w = 0.5f * f2;
            quat4f.x = (fArray[1][2] - fArray[2][1]) * f3;
            quat4f.y = (fArray[2][0] - fArray[0][2]) * f3;
            quat4f.z = (fArray[0][1] - fArray[1][0]) * f3;
        } else {
            int[] nArray = new int[]{1, 2, 0};
            int n = 0;
            if (fArray[1][1] > fArray[0][0]) {
                n = 1;
            }
            if (fArray[2][2] > fArray[n][n]) {
                n = 2;
            }
            int n2 = nArray[n];
            int n3 = nArray[n2];
            float f4 = (float)Math.sqrt(fArray[n][n] - fArray[n2][n2] - fArray[n3][n3] + 1.0f);
            if (fArray[n2][n3] < fArray[n3][n2]) {
                f4 = -f4;
            }
            float f5 = 0.5f / f4;
            fArray2[n] = 0.5f * f4;
            quat4f.w = (fArray[n2][n3] - fArray[n3][n2]) * f5;
            fArray2[n2] = (fArray[n][n2] + fArray[n2][n]) * f5;
            fArray2[n3] = (fArray[n][n3] + fArray[n3][n]) * f5;
            quat4f.x = fArray2[0];
            quat4f.y = fArray2[1];
            quat4f.z = fArray2[2];
        }
    }
}

