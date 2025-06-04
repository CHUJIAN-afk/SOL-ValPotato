package org.mcteampotato.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.mcteampotato.config.ValpotatoConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public class FoodDataMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void cancelTick(Player player, CallbackInfo ci){
        if (ValpotatoConfig.DISABLE_VANILLA_FOOD.get()){
            ci.cancel();
        }
    }
}
