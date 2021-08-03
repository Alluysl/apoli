package io.github.apace100.apoli.power;

import io.github.apace100.apoli.util.HudRender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class PreventDeathPower extends CooldownPower {

    private final Consumer<Entity> entityAction;
    private final Consumer<Entity> attackerAction;
    private final Predicate<Pair<DamageSource, Float>> condition;

    public PreventDeathPower(PowerType<?> type, LivingEntity entity, int cooldownDuration, HudRender hudRender, Consumer<Entity> entityAction, Consumer<Entity> attackerAction, Predicate<Pair<DamageSource, Float>> condition) {
        super(type, entity, cooldownDuration, hudRender);
        this.entityAction = entityAction;
        this.attackerAction = attackerAction;
        this.condition = condition;
    }

    public boolean doesApply(DamageSource source, float amount) {
        return canUse() && (condition == null || condition.test(new Pair<>(source, amount)));
    }

    public void executeActions(Entity attacker) {
        if (entityAction != null)
            entityAction.accept(entity);
        if (attackerAction != null && attacker != null && attacker != entity)
            attackerAction.accept(attacker);
        use();
    }
}
