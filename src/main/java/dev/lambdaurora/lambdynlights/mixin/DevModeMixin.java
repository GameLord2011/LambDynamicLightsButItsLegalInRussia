/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.LambDynLightsConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public class DevModeMixin {
	@Shadow
	@Final
	Minecraft minecraft;

	@WrapOperation(
			method = "render(FJZ)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;flush()V")
	)
	private void handler(GuiGraphics graphics, Operation<Void> original) {
		int bottom = this.minecraft.getWindow().getGuiScaledHeight();
		int y = bottom - this.minecraft.font.lineHeight;

		if (LambDynLights.get().gameLoadFinished && !this.minecraft.options.renderDebug) {
			graphics.fill(
					0, y - 4,
					this.minecraft.font.width(LambDynLightsConstants.DEV_MODE_OVERLAY_TEXT) + 4, bottom,
					0xaa000000
			);
			graphics.drawShadowedText(this.minecraft.font, LambDynLightsConstants.DEV_MODE_OVERLAY_TEXT, 2, y - 2, 0xff0000);
		}
		original.call(graphics);
	}
}
