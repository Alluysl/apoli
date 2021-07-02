package io.github.apace100.apoli.power;

import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.apache.commons.lang3.tuple.Triple;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class PreventSleepPower extends Power {

    private final Predicate<CachedBlockPosition> predicate;
    private final String message;
    private final boolean allowSpawnPoint;
    private final Consumer<Entity> entityAction;
    private final Consumer<Triple<World, BlockPos, Direction>> blockAction;

    public PreventSleepPower(PowerType<?> type, LivingEntity entity, Predicate<CachedBlockPosition> predicate, String message, boolean allowSpawnPoint, Consumer<Entity> entityAction, Consumer<Triple<World, BlockPos, Direction>> blockAction) {
        super(type, entity);
        this.predicate = predicate;
        this.message = message;
        this.allowSpawnPoint = allowSpawnPoint;
        this.entityAction = entityAction;
        this.blockAction = blockAction;
    }

    public boolean doesPrevent(WorldView world, BlockPos pos) {
        CachedBlockPosition cbp = new CachedBlockPosition(world, pos, true);
        return predicate.test(cbp);
    }

    public boolean doesAllowSpawnPoint() {
        return allowSpawnPoint;
    }

    public void executeActions(WorldView world, BlockPos pos, Direction direction){
        if (entity instanceof PlayerEntity)
            ((PlayerEntity)entity).sendMessage(new TranslatableText(message), true);
        if (entityAction != null)
            entityAction.accept(entity);
        if (blockAction != null && world instanceof World)
            blockAction.accept(Triple.of((World)world, pos, direction));
    }
}
