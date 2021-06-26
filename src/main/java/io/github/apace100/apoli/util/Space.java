package io.github.apace100.apoli.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

public enum Space {
    WORLD, LOCAL, LOCAL_HORIZONTAL, LOCAL_HORIZONTAL_NORMALIZED, VELOCITY, VELOCITY_NORMALIZED, VELOCITY_HORIZONTAL, VELOCITY_HORIZONTAL_NORMALIZED;

    private final double VECTOR_MAGNITUDE_ZERO_THRESHOLD = 0.00005;

    public static void rotateVectorToBase(Vec3d newBase, Vec3f vector) {
        vector.set(SimpleMatrix3f.fromOrientationVector(newBase).multiply(vector));
    }

    public boolean toGlobal(Vec3f vector, Entity entity){
        Vec3d base;

        switch (this){

            case WORLD:
                return true;

            case LOCAL:
            case LOCAL_HORIZONTAL:
            case LOCAL_HORIZONTAL_NORMALIZED:
                base = entity.getRotationVector();
                if (this != LOCAL){ // horizontal
                    base = new Vec3d(base.getX(), 0, base.getZ());
                    if (this == LOCAL_HORIZONTAL_NORMALIZED){
                        if (base.lengthSquared() <= VECTOR_MAGNITUDE_ZERO_THRESHOLD)
                            return false; // can't normalize null vector
                        base = base.normalize();
                    }
                }
                rotateVectorToBase(base, vector);
                return true;

            case VELOCITY:
            case VELOCITY_NORMALIZED:
            case VELOCITY_HORIZONTAL:
            case VELOCITY_HORIZONTAL_NORMALIZED:
                base = entity.getVelocity();
                if (this == VELOCITY_HORIZONTAL || this == VELOCITY_HORIZONTAL_NORMALIZED)
                    base = new Vec3d(base.getX(), 0, base.getZ());
                if (this == VELOCITY_NORMALIZED || this == VELOCITY_HORIZONTAL_NORMALIZED){
                    if (base.lengthSquared() <= VECTOR_MAGNITUDE_ZERO_THRESHOLD)
                        return false; // can't normalize null vector
                    base = base.normalize();
                }
                rotateVectorToBase(base, vector);
                return true;
        }
        return false;
    }
}
