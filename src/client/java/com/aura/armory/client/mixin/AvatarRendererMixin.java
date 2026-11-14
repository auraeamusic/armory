package com.aura.armory.client.mixin;

import com.aura.armory.client.BackSlotHandItems;
import com.aura.armory.client.BackSlotRenderState;
import com.aura.armory.BackSlotAttachments;

import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


// resolves backslot itemstack into itemstackrenderstate
// (mirrors vanilla baking)
@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin {
	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
			at = @At("TAIL"))
	private void armory$populateBackSlot(Avatar entity, AvatarRenderState state, float tick, CallbackInfo ci) {
		if (!(entity instanceof Player player)) {
			return;
		}
		if (!(entity instanceof LivingEntity living)) {
			return;
		}
		FabricRenderState fabricState = (FabricRenderState) (Object) state;
		ItemStackRenderState renderState = fabricState.getData(BackSlotRenderState.BACK_ITEM_KEY);
		if (renderState == null) {
			renderState = new ItemStackRenderState();
			fabricState.setData(BackSlotRenderState.BACK_ITEM_KEY, renderState);
		} else {
			renderState.clear();
		}
		ItemStack back = BackSlotAttachments.getBackStack(player);
		if (back.isEmpty()) {
			return;
		}
		// item is in hand so back is bare
		// should i put original mainhand item into back rendering? maybe....
		// TODO: mainhand item into back slot render
		if (BackSlotAttachments.isHolding(player)) {
			return;
		}
		boolean handModel = BackSlotHandItems.usesHandModel(back);
		fabricState.setData(BackSlotRenderState.HAND_MODEL_KEY, handModel);
		fabricState.setData(BackSlotRenderState.HAND_ADJUST_KEY,
				handModel ? BackSlotHandItems.adjustFor(back) : BackSlotHandItems.HandAdjust.NONE);
		ItemDisplayContext context = handModel
				? ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
				: ItemDisplayContext.FIXED;
		Minecraft.getInstance().getItemModelResolver().updateForLiving(renderState, back, context, living);
	}
}