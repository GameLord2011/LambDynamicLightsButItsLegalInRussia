/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.compat;

import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import net.minecraft.world.entity.LivingEntity;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Represents the Curios compatibility layer.
 *
 * @author LambdAurora
 * @version 4.0.2
 * @since 4.0.2
 */
final class CuriosCompat implements CompatLayer {
	@Override
	public int getLivingEntityLuminanceFromItems(ItemLightSourceManager itemLightSources, LivingEntity entity, boolean submergedInWater) {
		int luminance = 0;
		var component = CuriosApi.getCuriosInventory(entity);

		if (component.isPresent()) {
			for (var stacksHandler : component.get().getCurios().values()) {
				var stacks = stacksHandler.getStacks();

				for (int i = 0; i < stacks.getSlots(); i++) {
					luminance = Math.max(luminance, itemLightSources.getLuminance(stacks.getStackInSlot(i), submergedInWater));

					if (luminance >= 15) {
						break;
					}
				}
			}
		}

		return luminance;
	}
}
