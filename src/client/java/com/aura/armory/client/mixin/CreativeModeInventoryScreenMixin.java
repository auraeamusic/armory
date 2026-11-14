package com.aura.armory.client.mixin;

import com.aura.armory.BackSlot;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin {
	@ModifyArgs(
			method = "selectTab",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen$SlotWrapper;<init>(Lnet/minecraft/world/inventory/Slot;III)V"
			)
	)
	private void armory$placeBackSlot(Args args) {
		if (args.get(0) instanceof BackSlot) {
			args.set(2, BackSlot.CREATIVE_X);
			args.set(3, BackSlot.CREATIVE_Y);
		}
	}
}
