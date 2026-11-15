/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.geom;

import com.sun.javafx.geom.Vec3f;

public class Matrix3f {
    public float m00;
    public float m01;
    public float m02;
    public float m10;
    public float m11;
    public float m12;
    public float m20;
    public float m21;
    public float m22;

    public Matrix3f(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9) {
        this.m00 = f;
        this.m01 = f2;
        this.m02 = f3;
        this.m10 = f4;
        this.m11 = f5;
        this.m12 = f6;
        this.m20 = f7;
        this.m21 = f8;
        this.m22 = f9;
    }

    public Matrix3f(float[] fArray) {
        this.m00 = fArray[0];
        this.m01 = fArray[1];
        this.m02 = fArray[2];
        this.m10 = fArray[3];
        this.m11 = fArray[4];
        this.m12 = fArray[5];
        this.m20 = fArray[6];
        this.m21 = fArray[7];
        this.m22 = fArray[8];
    }

    public Matrix3f(Vec3f[] vec3fArray) {
        this.m00 = vec3fArray[0].x;
        this.m01 = vec3fArray[0].y;
        this.m02 = vec3fArray[0].z;
        this.m10 = vec3fArray[1].x;
        this.m11 = vec3fArray[1].x;
        this.m12 = vec3fArray[1].x;
        this.m20 = vec3fArray[2].x;
        this.m21 = vec3fArray[2].x;
        this.m22 = vec3fArray[2].x;
    }

    public Matrix3f(Matrix3f matrix3f) {
        this.m00 = matrix3f.m00;
        this.m01 = matrix3f.m01;
        this.m02 = matrix3f.m02;
        this.m10 = matrix3f.m10;
        this.m11 = matrix3f.m11;
        this.m12 = matrix3f.m12;
        this.m20 = matrix3f.m20;
        this.m21 = matrix3f.m21;
        this.m22 = matrix3f.m22;
    }

    public Matrix3f() {
        this.m00 = 1.0f;
        this.m01 = 0.0f;
        this.m02 = 0.0f;
        this.m10 = 0.0f;
        this.m11 = 1.0f;
        this.m12 = 0.0f;
        this.m20 = 0.0f;
        this.m21 = 0.0f;
        this.m22 = 1.0f;
    }

    public String toString() {
        return this.m00 + ", " + this.m01 + ", " + this.m02 + "\n" + this.m10 + ", " + this.m11 + ", " + this.m12 + "\n" + this.m20 + ", " + this.m21 + ", " + this.m22 + "\n";
    }

    public final void setIdentity() {
        this.m00 = 1.0f;
        this.m01 = 0.0f;
        this.m02 = 0.0f;
        this.m10 = 0.0f;
        this.m11 = 1.0f;
        this.m12 = 0.0f;
        this.m20 = 0.0f;
        this.m21 = 0.0f;
        this.m22 = 1.0f;
    }

    public final void setRow(int n, float[] fArray) {
        switch (n) {
            case 0: {
                this.m00 = fArray[0];
                this.m01 = fArray[1];
                this.m02 = fArray[2];
                break;
            }
            case 1: {
                this.m10 = fArray[0];
                this.m11 = fArray[1];
                this.m12 = fArray[2];
                break;
            }
            case 2: {
                this.m20 = fArray[0];
                this.m21 = fArray[1];
                this.m22 = fArray[2];
                break;
            }
            default: {
                throw new ArrayIndexOutOfBoundsException("Matrix3f");
            }
        }
    }

    public final void setRow(int n, Vec3f vec3f) {
        switch (n) {
            case 0: {
                this.m00 = vec3f.x;
                this.m01 = vec3f.y;
                this.m02 = vec3f.z;
                break;
            }
            case 1: {
                this.m10 = vec3f.x;
                this.m11 = vec3f.y;
                this.m12 = vec3f.z;
                break;
            }
            case 2: {
                this.m20 = vec3f.x;
                this.m21 = vec3f.y;
                this.m22 = vec3f.z;
                break;
            }
            default: {
                throw new ArrayIndexOutOfBoundsException("Matrix3f");
            }
        }
    }

    public final void getRow(int n, Vec3f vec3f) {
        if (n == 0) {
            vec3f.x = this.m00;
            vec3f.y = this.m01;
            vec3f.z = this.m02;
        } else if (n == 1) {
            vec3f.x = this.m10;
            vec3f.y = this.m11;
            vec3f.z = this.m12;
        } else if (n == 2) {
            vec3f.x = this.m20;
            vec3f.y = this.m21;
            vec3f.z = this.m22;
        } else {
            throw new ArrayIndexOutOfBoundsException("Matrix3f");
        }
    }

    public final void getRow(int n, float[] fArray) {
        if (n == 0) {
            fArray[0] = this.m00;
            fArray[1] = this.m01;
            fArray[2] = this.m02;
        } else if (n == 1) {
            fArray[0] = this.m10;
            fArray[1] = this.m11;
            fArray[2] = this.m12;
        } else if (n == 2) {
            fArray[0] = this.m20;
            fArray[1] = this.m21;
            fArray[2] = this.m22;
        } else {
            throw new ArrayIndexOutOfBoundsException("Matrix3f");
        }
    }
}

