/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.gui;

import dev.lambdaurora.lambdynlights.LambDynLightsConstants;
import dev.lambdaurora.spruceui.event.ScreenEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Predicate;

/**
 * Represents the development-mode GUI overlay.
 *
 * @author LambdAurora
 * @version 4.3.0
 * @since 4.0.0
 */
public final class DevModeGui {
	private static final DevModeGui INSTANCE = new DevModeGui();
	private final Minecraft client = Minecraft.getInstance();

	private DevModeGui() {}

	public static void init() {
		if (!LambDynLightsConstants.isDevMode()) {
			return;
		}

		ScreenEvents.AFTER_RENDER.register(
				(screen, graphics, mouseX, mouseY, tickDelta) -> INSTANCE.render(graphics.vanilla()),
				Predicate.not(SettingsScreen.class::isInstance)
		);
		HudElementRegistry.addLast(
				LambDynLightsConstants.id("dev_mode"),
				(graphics, deltaTracker) -> INSTANCE.render(graphics)
		);
	}

	public void render(GuiGraphics graphics) {
		if (this.client.isGameLoadFinished() && !this.client.getDebugOverlay().showDebugScreen()) {
			int bottom = this.client.getWindow().getGuiScaledHeight();
			int y = bottom - this.client.font.lineHeight;

			graphics.fill(
					0, y - 4,
					this.client.font.width(LambDynLightsConstants.DEV_MODE_OVERLAY_TEXT) + 4, bottom,
					0xaa000000
			);
			graphics.drawShadowedText(this.client.font, LambDynLightsConstants.DEV_MODE_OVERLAY_TEXT, 2, y - 2, 0xffff0000);
		}
	}
}
