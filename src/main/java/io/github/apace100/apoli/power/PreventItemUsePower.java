package io.github.apace100.apoli.power;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class PreventItemUsePower extends Power {

    private final Predicate<ItemStack> predicate;
    private final String message;
    private final Consumer<Entity> entityAction;
    private final Consumer<ItemStack> itemAction;

    public PreventItemUsePower(PowerType<?> type, LivingEntity entity, Predicate<ItemStack> predicate, String message, Consumer<Entity> entityAction, Consumer<ItemStack> itemAction) {
        super(type, entity);
        this.predicate = predicate;
        this.message = message;
        this.entityAction = entityAction;
        this.itemAction = itemAction;
        /*UseItemCallback.EVENT.register(((playerEntity, world, hand) -> {
            if(getType().isActive(playerEntity)) {
                ItemStack stackInHand = playerEntity.getStackInHand(hand);
                if(doesPrevent(stackInHand)) {
                    return TypedActionResult.fail(stackInHand);
                }
            }
            return TypedActionResult.pass(ItemStack.EMPTY);
        }));*/
    }

    public boolean doesPrevent(ItemStack stack) {
        return predicate.test(stack);
    }

    public void executeActions(ItemStack stack) {
        if (entity instanceof PlayerEntity)
            ((PlayerEntity)entity).sendMessage(new TranslatableText(message), true);
        if(entityAction != null)
            entityAction.accept(entity);
        if (itemAction != null)
            itemAction.accept(stack);
    }
}
