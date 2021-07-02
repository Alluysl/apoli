package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PreventBlockUsePower;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {

    @Inject(method = "interactBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;shouldCancelInteraction()Z"), cancellable = true)
    private void preventBlockInteraction(ClientPlayerEntity player, ClientWorld world, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        PowerHolderComponent.getPowers(player, PreventBlockUsePower.class).stream().filter(p -> p.doesPrevent(world, hitResult.getBlockPos())).findFirst().ifPresent(p -> {
            player.sendMessage(new TranslatableText(p.getMessage()), true);
            cir.setReturnValue(ActionResult.PASS);
        });
    }
}
