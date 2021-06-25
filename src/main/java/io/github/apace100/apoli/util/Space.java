package io.github.apace100.apoli.util;

import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

public enum Space {
    WORLD, LOCAL, LOCAL_HORIZONTAL, VELOCITY, VELOCITY_NORMALIZED, VELOCITY_HORIZONTAL, VELOCITY_HORIZONTAL_NORMALIZED;

    public static void rotateVectorToBase(Vec3d newBase, Vec3f vector) {

        vector.set(SimpleMatrix3f.fromOrientationVector(newBase).multiply(vector));
    }
}
