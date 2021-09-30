package io.github.apace100.apoli.util;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

import java.util.function.Predicate;

/**
 * @author apace100
 * @author Alluysl
 * Handles the registry of the block_in_radius condition in both block and entity conditions to avoid duplicating code.
 * */
public class BlockInRadiusConditionRegistry {

    public static SerializableData getSerializableData(){
        return new SerializableData()
            .add("block_condition", ApoliDataTypes.BLOCK_CONDITION)
            .add("radius", SerializableDataTypes.INT)
            .add("shape", SerializableDataType.enumValue(Shape.class), Shape.CUBE)
            .add("compare_to", SerializableDataTypes.INT, 1)
            .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN_OR_EQUAL);
    }

    public static boolean testCondition(SerializableData.Instance data, CachedBlockPosition cbp){
        WorldView worldView = cbp.getWorld();
        return worldView instanceof World && testCondition(data, cbp.getBlockPos(), (World)worldView);
    }

    public static boolean testCondition(SerializableData.Instance data, Entity entity){
        return testCondition(data, entity.getBlockPos(), entity.world);
    }

    @SuppressWarnings("unchecked")
    public static boolean testCondition(SerializableData.Instance data, BlockPos blockPos, World world){
        Predicate<CachedBlockPosition> blockCondition = ((ConditionFactory<CachedBlockPosition>.Instance)data.get("block_condition"));
        int stopAt = -1;
        Comparison comparison = ((Comparison)data.get("comparison"));
        int compareTo = data.getInt("compare_to");
        switch (comparison){
            case EQUAL, LESS_THAN_OR_EQUAL, GREATER_THAN -> stopAt = compareTo + 1;
            case LESS_THAN, GREATER_THAN_OR_EQUAL -> stopAt = compareTo;
        }
        int count = 0;
        for(BlockPos pos : Shape.getPositions(blockPos, (Shape) data.get("shape"), data.getInt("radius"))) {
            if(blockCondition.test(new CachedBlockPosition(world, pos, true))) {
                count++;
                if(count == stopAt) {
                    break;
                }
            }
        }
        return comparison.compare(count, compareTo);
    }
}
