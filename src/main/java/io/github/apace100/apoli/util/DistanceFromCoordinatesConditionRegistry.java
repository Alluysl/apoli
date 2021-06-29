package io.github.apace100.apoli.util;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

// Dummy class to register the distance_from_spawn conditions and avoid duplicating code

public class DistanceFromCoordinatesConditionRegistry {

    public static String[] getAliases(){
        return new String[]{"distance_from_spawn", "distance_from_coordinates"};
    }

    public static SerializableData getSerializableData(){
        // Using doubles and not ints because the player position is a vector of doubles and the sqrt function (for the distance) returns a double so we might as well use that precision
        return new SerializableData()
                .add("reference", SerializableDataTypes.STRING, "world_spawn")
                .add("consider_modify_player_spawn_powers", SerializableDataTypes.BOOLEAN, true)
                .add("x_offset", SerializableDataTypes.DOUBLE, 0.0)
                .add("y_offset", SerializableDataTypes.DOUBLE, 0.0)
                .add("z_offset", SerializableDataTypes.DOUBLE, 0.0)
                .add("ignore_x", SerializableDataTypes.BOOLEAN, false)
                .add("ignore_y", SerializableDataTypes.BOOLEAN, false)
                .add("ignore_z", SerializableDataTypes.BOOLEAN, false)
                .add("shape", SerializableDataTypes.STRING, "euclidean")
                .add("scale_reference_to_dimension", SerializableDataTypes.BOOLEAN, true)
                .add("scale_distance_to_dimension", SerializableDataTypes.BOOLEAN, false)
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.DOUBLE);
    }

    private static boolean firstCouldNotGetWarning = true;
    private static void warnCouldNotGetObject(String object, String from, boolean assumption){
        if (firstCouldNotGetWarning){
            firstCouldNotGetWarning = false;
            System.out.println("Could not retrieve " + object + " from " + from + " for distance_from_spawn condition, assuming " + assumption + ".");
        }
    }

    public static boolean testCondition(SerializableData.Instance data, CachedBlockPosition block){
        return testCondition(data, block, null);
    }

    public static boolean testCondition(SerializableData.Instance data, LivingEntity entity){
        return testCondition(data, null, entity);
    }

    // Assumes that either block or entity is non-null
    public static boolean testCondition(SerializableData.Instance data, CachedBlockPosition block, LivingEntity entity){
        Vec3d pos;
        WorldView worldView;
        if (block != null){
            BlockPos blockPos = block.getBlockPos();
            pos = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            worldView = block.getWorld();
        } else {
            pos = entity.getPos();
            worldView = entity.getEntityWorld();
        }
        double x = 0, y = 0, z = 0;
        double currentDimensionCoordinateScale = worldView.getDimension().getCoordinateScale();
        if (!(worldView instanceof World)){
            warnCouldNotGetObject("world", block != null ? "block" : "entity", false);
            return false;
        }
        switch (data.getString("reference")){
            case "origin":
                break;
            case "world_spawn":
                BlockPos spawnPos;
                if (worldView instanceof ClientWorld)
                    spawnPos = ((ClientWorld)worldView).getSpawnPos();
                else if (worldView instanceof ServerWorld)
                    spawnPos = ((ServerWorld)worldView).getSpawnPos();
                else {
                    warnCouldNotGetObject("world", block != null ? "block" : "entity", false);
                    return false;
                }
//                        System.out.println("ww: " + worldView.hashCode());
                x = spawnPos.getX();
                y = spawnPos.getY();
                z = spawnPos.getZ();
//                        if (((World) worldView).getServer() != null)
//                            for (ServerWorld world : ((World) worldView).getServer().getWorlds())
//                                System.out.println(world.hashCode() + " " + (world == worldView));
                break;
            case "player_set_spawn":
                // TODO nest that inside an if that tests if the spawn was set?
                // No break on purpose
            case "player_natural_spawn":
                if (!(entity instanceof PlayerEntity)) // null instance of AnyClass is always false so the block case is covered
                    return false; // cannot infer the spawn of a block or non-player entity
                // TODO using consider_modify_player_spawn_powers
                break;
        }
        x += data.getDouble("x_offset");
        y += data.getDouble("y_offset");
        z += data.getDouble("z_offset");
        if (data.getBoolean("scale_reference_to_dimension") && (x != 0 || z != 0)){
            if (currentDimensionCoordinateScale == 0) // pocket dimensions?
                return false; // coordinate scale 0 means it takes 0 blocks to travel in the OW to travel 1 block in the dimension, so the dimension is folded on 0 0, so unless the OW reference is at 0 0, it gets scaled to infinity
            x /= currentDimensionCoordinateScale;
            z /= currentDimensionCoordinateScale;
        }
        double distance,
                xDistance = data.getBoolean("ignore_x") ? 0 : Math.abs(pos.getX() - x),
                yDistance = data.getBoolean("ignore_y") ? 0 : Math.abs(pos.getY() - y),
                zDistance = data.getBoolean("ignore_z") ? 0 : Math.abs(pos.getZ() - z);
        if (data.getBoolean("scale_distance_to_dimension") && (xDistance != 0 || zDistance != 0)){
            if (currentDimensionCoordinateScale == 0){
                Comparison comparison = (Comparison)data.get("comparison");
                return comparison == Comparison.NOT_EQUAL || comparison == Comparison.GREATER_THAN || comparison == Comparison.GREATER_THAN_OR_EQUAL; // nonzero would get scaled to infinity
            }
            xDistance /= currentDimensionCoordinateScale;
            zDistance /= currentDimensionCoordinateScale;
        }
        switch (data.getString("shape")) {
            case "euclidean", "circle", "sphere" -> distance = Math.sqrt(xDistance * xDistance + yDistance * yDistance + zDistance * zDistance);
            case "manhattan", "star" -> distance = xDistance + yDistance + zDistance;
            case "chebyshev", "cube" -> distance = Math.max(Math.max(xDistance, yDistance), zDistance);
            default -> { // unrecognized
                warnCouldNotGetObject("recognized shape name", "data", false);
                return false;
            }
        }
        return ((Comparison)data.get("comparison")).compare(distance, data.getDouble("compare_to"));
    }
}
