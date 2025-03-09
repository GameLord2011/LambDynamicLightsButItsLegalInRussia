/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.compat;

import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import io.wispforest.accessories.api.AccessoriesCapability;
import net.minecraft.world.entity.LivingEntity;

/**
 * Represents the Accessories compatibility layer.
 *
 * @author LambdAurora
 * @version 3.1.4
 * @since 3.1.4
 */
final class AccessoriesCompat implements CompatLayer {
	@Override
	public int getLivingEntityLuminanceFromItems(ItemLightSourceManager itemLightSources, LivingEntity entity, boolean submergedInWater) {
		int luminance = 0;
		var component = AccessoriesCapability.get(entity);

		if (component != null) {
			for (var equipped : component.getAllEquipped()) {
				if (!equipped.stack().isEmpty()) {
					luminance = Math.max(luminance, itemLightSources.getLuminance(equipped.stack(), submergedInWater));

					if (luminance >= 15) {
						break;
					}
				}
			}
		}

		return luminance;
	}
}
