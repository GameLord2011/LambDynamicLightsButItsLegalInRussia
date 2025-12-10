/*
 * Copyright © 2020 LambdAurora <email@lambdaurora.dev>
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
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Adds a debug string for dynamic light sources tracking and updates.
 *
 * @author LambdAurora
 * @version 4.6.0
 * @since 1.3.2
 */
@Mixin(DebugScreenOverlay.class)
public class DebugScreenOverlayMixin {
	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;"))
	private Collection<String> lambdynlights$onRender(
			Map<Identifier, Collection<String>> instance,
			Operation<Collection<String>> original
	) {
		instance.computeIfAbsent(LambDynLights.id("debug"), idx -> new ArrayList<>())
				.add(ChatFormatting.RED + LambDynLightsConstants.DEV_MODE_OVERLAY_TEXT);
		return original.call(instance);
	}
}
