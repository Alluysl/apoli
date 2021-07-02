package io.github.apace100.apoli.power;

import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.apache.commons.lang3.tuple.Triple;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class PreventBlockUsePower extends Power {

    private final Predicate<CachedBlockPosition> predicate;
    private final String message;

    public PreventBlockUsePower(PowerType<?> type, LivingEntity entity, Predicate<CachedBlockPosition> predicate, String message) {
        super(type, entity);
        this.predicate = predicate;
        this.message = message;
    }

    public boolean doesPrevent(WorldView world, BlockPos pos) {
        CachedBlockPosition cbp = new CachedBlockPosition(world, pos, true);
        return predicate.test(cbp);
    }

    public String getMessage() {
        return message;
    }
}
