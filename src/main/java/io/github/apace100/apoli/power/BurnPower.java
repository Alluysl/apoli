package io.github.apace100.apoli.power;

import io.github.apace100.apoli.mixin.EntityAccessor;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.LivingEntity;

public class BurnPower extends Power {

    private final int refreshInterval;
    private final int burnDuration;

    public BurnPower(PowerType<?> type, LivingEntity entity, int refreshInterval, int burnDuration) {
        super(type, entity);
        this.refreshInterval = refreshInterval;
        this.burnDuration = burnDuration;
        this.setTicking();
    }

    public void tick() {
        if(entity.age % refreshInterval == 0) {
            int fireTicks = ProtectionEnchantment.transformFireDuration(entity, burnDuration);
            if (entity.getFireTicks() < fireTicks)
                ((EntityAccessor)entity).callSetFireTicks(fireTicks);
        }
    }
}