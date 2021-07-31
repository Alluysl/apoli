package io.github.apace100.apoli.power;

import io.github.apace100.apoli.util.HudRender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Pair;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class SelfActionWhenKilledPower extends CooldownPower {

    private final Predicate<Pair<DamageSource, Float>> damageCondition;
    private final Consumer<Entity> entityAction;

    public SelfActionWhenKilledPower(PowerType<?> type, LivingEntity entity, int cooldownDuration, HudRender hudRender, Predicate<Pair<DamageSource, Float>> damageCondition, Consumer<Entity> entityAction) {
        super(type, entity, cooldownDuration, hudRender);
        this.damageCondition = damageCondition;
        this.entityAction = entityAction;
    }

    public void whenKilled(DamageSource damageSource, float damageAmount) {
        if(damageCondition == null || damageCondition.test(new Pair<>(damageSource, damageAmount))) {
            if(canUse()) {
                this.entityAction.accept(this.entity);
                use();
            }
        }
    }
}
