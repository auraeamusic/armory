package com.aura.armory.client;

import com.aura.armory.BackSlotAttachments;
import com.aura.armory.DrawSheathPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;

@SuppressWarnings ("all")
public class ArmoryClient implements ClientModInitializer {
	private static final boolean ENABLE_TRANSFORM_EDITOR = false;
	private static final boolean ENABLE_SCYTHE_POSE_EDITOR = false;

	@Override
	public void onInitializeClient() {
		BackSlotTransform.load();

		KeyMapping drawSheathKey = KeyMappingHelper.registerKeyMapping(
				new KeyMapping("key.lamplight.draw_sheath", InputConstants.Type.KEYSYM, InputConstants.KEY_G, new KeyMapping.Category(Identifier.fromNamespaceAndPath("armory", "keybinds"))));

		ClientTickEvents.END_CLIENT_TICK.register(new SelectionDisarmTracker(drawSheathKey));

		LivingEntityRenderLayerRegistrationCallback.EVENT.register((entityType, renderer, helper, context) -> {
			if (renderer instanceof AvatarRenderer<?> avatarRenderer) {
				helper.register(new BackSlotLayer(avatarRenderer));
			}
		});
	}

	private static final class SelectionDisarmTracker implements ClientTickEvents.EndTick {
		private final KeyMapping drawSheathKey;
		private int lastSelected = -1;

		private SelectionDisarmTracker(KeyMapping drawSheathKey) {
			this.drawSheathKey = drawSheathKey;
		}

		@Override
		public void onEndTick(net.minecraft.client.Minecraft client) {
			if (client.player == null) {
				lastSelected = -1;
				return;
			}
			if (!ClientPlayNetworking.canSend(DrawSheathPayload.TYPE)) {
				return;
			}
			int selected = client.player.getInventory().getSelectedSlot();
			boolean holding = BackSlotAttachments.isHolding(client.player);
			if (holding && lastSelected != -1 && selected != lastSelected) {
				BackSlotAttachments.setHolding(client.player, false);
				ClientPlayNetworking.send(new DrawSheathPayload(false));
				holding = false;
			}
			lastSelected = selected;
			if (!drawSheathKey.consumeClick() || client.gui.screen() != null) {
				return;
			}
			
			if (!holding && BackSlotAttachments.getBackStack(client.player).isEmpty()) {
				return;
			}
			BackSlotAttachments.setHolding(client.player, !holding);
			ClientPlayNetworking.send(new DrawSheathPayload(!holding));
		}
	}
}
