package io.github.apace100.apoli.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

public enum Space {
    WORLD, LOCAL, LOCAL_HORIZONTAL, LOCAL_HORIZONTAL_NORMALIZED, VELOCITY, VELOCITY_NORMALIZED, VELOCITY_HORIZONTAL, VELOCITY_HORIZONTAL_NORMALIZED;

    private static Matrix3f getBaseTransformMatrixFromNormalizedDirectionVector(Vec3d vector){
        double[][] factors = new double[3][3];
        // Z
        factors[1][2] = vector.getY();
        if (Math.abs(factors[1][2]) != 1.0F) {
            // If the orientation vector points straight up or straight down, avoid a catastrophe (division by 0, can't know yaw, impossible to determine stuff by nullifying X and Y
            // The default value of floats is 0.0F so we can just return already if the vector points straight up or down
            // Otherwise continue on:
            factors[0][2] = vector.getX();
            factors[2][2] = vector.getZ();
            // X
            factors[0][0] = vector.getZ();
            factors[1][0] = 0; // X vector is horizontal
            factors[2][0] = -vector.getX();
            // Normalize X
            float xFactor = (float)(1 / Math.sqrt(factors[0][0] * factors[0][0] + factors[2][0] * factors[2][0]));
            factors[0][0] *= xFactor;
            factors[2][0] *= xFactor;
            // Y (cross product of Z and X)
            factors[0][1] = factors[1][2] * factors[2][0];
            factors[1][1] = factors[2][2] * factors[0][0] - factors[0][2] * factors[2][0];
            factors[2][1] = -factors[1][2] * factors[0][0];
        } else
            System.err.println("[Warning] Vertical orientation vector, couldn't assess X and Y local vectors, set to null vector.");
        Matrix3f res = new Matrix3f();
        for (int i = 0; i < 3; ++i)
            for (int j = 0; j < 3; ++j)
                res.set(i, j, (float)factors[i][j]);
        return res;
    }

    private static void rotateVectorToBase(Vec3d base, Vec3f vector, boolean normalizeBase) {

        double baseScaleD = base.length();
        if (baseScaleD <= 0.007){ // tweak value if too high, may be a bit too aggressive
            vector.set(Vec3f.ZERO);
        } else {
            float baseScale = (float)baseScaleD;

            Vec3d normalizedBase = base.normalize(); // the function called below assumes the base is normalized to simplify calculations (Y calculated as cross product of Z and X guaranteed to be normalized if X and Z are normalized)

            Matrix3f transformMatrix = getBaseTransformMatrixFromNormalizedDirectionVector(normalizedBase);
            if (!normalizeBase) // if the base wasn't supposed to get normalized, re-scale to compensate for the prior normalization
                transformMatrix.multiply(Matrix3f.scale(baseScale, baseScale, baseScale));
            vector.transform(transformMatrix); // matrix multiplication, vector is now in the new base :D
        }
    }

    public void toGlobal(Vec3f vector, Entity entity){
        Vec3d base;

        switch (this){

            case WORLD:
                break;

            case LOCAL:
            case LOCAL_HORIZONTAL:
            case LOCAL_HORIZONTAL_NORMALIZED:
                base = entity.getRotationVector();
                if (this != LOCAL) // horizontal
                    base = new Vec3d(base.getX(), 0, base.getZ());
                rotateVectorToBase(base, vector, this == LOCAL_HORIZONTAL_NORMALIZED);
                break;

            case VELOCITY:
            case VELOCITY_NORMALIZED:
            case VELOCITY_HORIZONTAL:
            case VELOCITY_HORIZONTAL_NORMALIZED:
                base = entity.getVelocity();
                if (this == VELOCITY_HORIZONTAL || this == VELOCITY_HORIZONTAL_NORMALIZED)
                    base = new Vec3d(base.getX(), 0, base.getZ());
                rotateVectorToBase(base, vector, this == VELOCITY_NORMALIZED || this == VELOCITY_HORIZONTAL_NORMALIZED);
                break;
        }
    }
}
