package org.mcteampotato.mixin;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class HudMixin {
    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
    public void CancelRender(GuiGraphics guiGraphics, Player player, int y, int x, CallbackInfo ci) {
        ci.cancel();
    }
}
