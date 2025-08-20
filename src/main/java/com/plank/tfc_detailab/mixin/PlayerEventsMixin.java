package com.plank.tfc_detailab.mixin;

import com.plank.tfc_detailab.ArmorBarRenderer;
import com.redlimerl.detailab.DetailArmorBar;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = com.redlimerl.detailab.events.PlayerEvents.class)
public abstract class PlayerEventsMixin {

    @Inject(method = "onArmorRender", remap = false, at = @At("HEAD"), cancellable = true)
    private static void onArmorRender(RenderGuiOverlayEvent.Pre event, CallbackInfo ci) {
        if (event.getOverlay().id() == VanillaGuiOverlay.ARMOR_LEVEL.id()) {
            event.setCanceled(true);
            var instance = Minecraft.getInstance();
            if (instance.player != null && instance.gameMode != null && instance.gameMode.getPlayerMode().isSurvival()) {
                ArmorBarRenderer.INSTANCE.render(event.getGuiGraphics().pose(), instance.player);
            }
        }
        ci.cancel();
    }

    @Inject(method = "onExpPick", remap = false, at = @At("HEAD"), cancellable = true)
    private static void onExpPick(PlayerXpEvent.PickupXp event, CallbackInfo ci) {
        Map.Entry<EquipmentSlot, ItemStack> entry = EnchantmentHelper.getRandomItemWith(Enchantments.MENDING, event.getEntity(), ItemStack::isDamaged);
        if (entry != null) ArmorBarRenderer.LAST_MENDING = DetailArmorBar.getTicks();
        ci.cancel();
    }
}