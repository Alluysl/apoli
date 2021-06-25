package io.github.apace100.apoli.util;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

public class SimpleMatrix3f {

    private float[][] factors = new float[3][3]; // [column][line]

    // Builds a basis matrix from its Z vector. The X vector shall be normal to the world's Y axis. All three vectors are normalized.
    public static SimpleMatrix3f fromOrientationVector(Vec3f vec){
        float[][] factors = new float[3][3];
        // Z
        factors[1][2] = vec.getY();
        if (Math.abs(factors[1][2]) != 1.0F) {
            // If the orientation vector points straight up or straight down, avoid a catastrophe (division by 0, can't know yaw, impossible to determine stuff by nullifying X and Y
            // The default value of floats is 0.0F so we can just return already if the vector points straight up or down
            // Otherwise continue on:
            factors[0][2] = vec.getX();
            factors[2][2] = vec.getZ();
            // X
            factors[0][0] = vec.getZ();
            factors[1][0] = 0; // X vector is horizontal
            factors[2][0] = -vec.getX();
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
        return new SimpleMatrix3f(factors);
    }
    public static SimpleMatrix3f fromOrientationVector(Vec3d vec){
        return fromOrientationVector(new Vec3f((float)vec.getX(), (float)vec.getY(), (float)vec.getZ()));
    }

//    public SimpleMatrix3f(Vec3f diagonalVector){
//        factors[0][0] = diagonalVector.getX();
//        factors[1][1] = diagonalVector.getY();
//        factors[2][2] = diagonalVector.getZ();
//    }

    public SimpleMatrix3f(float[][] factors){
        set(factors);
    }

//    @Override
//    public SimpleMatrix3f clone(){
//        return new SimpleMatrix3f(factors);
//    }

    public void set(float[][] factors){
        for (int i = 0; i < 9; ++i)
            this.factors[i / 3][i % 3] = factors[i / 3][i % 3];
    }

//    public float get(int x, int y){
//        return factors[MathHelper.clamp(x, 0, 2)][MathHelper.clamp(y, 0, 2)];
//    }

    // ( r )   (      ) ( v )
    // ( e ) = ( this ) ( e )
    // ( s )   (      ) ( c )
    public Vec3f multiply(Vec3f vec){
        return  new Vec3f(
                factors[0][0] * vec.getX() + factors[0][1] * vec.getY() + factors[0][2] * vec.getZ(),
                factors[1][0] * vec.getX() + factors[1][1] * vec.getY() + factors[1][2] * vec.getZ(),
                factors[2][0] * vec.getX() + factors[2][1] * vec.getY() + factors[2][2] * vec.getZ()
        );
    }

//    public void transposeSelf(){
//        float[][] transposedFactors = new float[3][3];
//        for (int i = 0; i < 9; ++i)
//            transposedFactors[i / 3][i % 3] = factors[i % 3][i / 3];
//        factors = transposedFactors;
//    }
//
//    public void scaleSelf(float scalar){
//        for (int i = 0; i < 9; ++i)
//            factors[i / 3][i % 3] *= scalar;
//    }
//
//    // I have a feeling there's a better implementation out there, I'm pretty sure creating all these arrays is a bad idea (and the time complexity is meh)
//    private float[][] cofactors(){
//        float[][] cofactors = new float[3][3];
//        float[][] minor = new float[2][2];
//        for (int i = 0; i < 3; ++i)
//            for (int j = 0; j < 3; ++j){
//                for (int ii = 0; ii < 3; ++ii)
//                    for (int jj = 0; jj < 3; ++jj)
//                        if (ii != i && jj != j)
//                            minor[ii - (ii > i ? 1 : 0)][jj - (jj > j ? 1 : 0)] = factors[ii][jj];
//                cofactors[i][j] = ((i + j) % 2 == 0 ? 1 : -1) * (minor[0][0] * minor[1][1] - minor[0][1] * minor[1][0]);
//            }
//        return cofactors;
//    }
//
//    public boolean invertSelf(){
//        float[][] cofactors = cofactors();
//        float determinant = factors[0][0] * cofactors[0][0] + factors[1][0] * cofactors[1][0] + factors[2][0] * cofactors[2][0];
//        if (determinant == 0)
//            return false;
//        set(cofactors);
//        transposeSelf();
//        scaleSelf(1 / determinant);
//        return true;
//    }
//
//    // Solves "this * solution = vec"
//    public boolean solve(Vec3f vec, Vec3f solution){
//        SimpleMatrix3f mat = clone();
//        if (!mat.invertSelf())
//            return false;
//        solution.set(mat.multiply(vec));
//        return true;
//    }
//
//    @Override
//    public String toString(){
//        String[][] factorStrings = new String[3][3];
//        int maxLen = 0;
//        for (int i = 0; i < 3; ++i)
//            for (int j = 0; j < 3; ++j){
//                factorStrings[i][j] = Float.toString(factors[i][j]);
//                maxLen = Math.max(maxLen, factorStrings[i][j].length());
//            }
//        String res = "";
//        for (int i = 0; i < 3; ++i) {
//            res += "\n( ";
//            for (int j = 0; j < 3; ++j){
//                int diff = maxLen - factorStrings[i][j].length();
//                res += " ".repeat(diff / 2) + factorStrings[i][j] + " ".repeat(diff / 2 + diff % 2) + " ";
//            }
//            res += ")";
//        }
//        return res;
//    }
}
