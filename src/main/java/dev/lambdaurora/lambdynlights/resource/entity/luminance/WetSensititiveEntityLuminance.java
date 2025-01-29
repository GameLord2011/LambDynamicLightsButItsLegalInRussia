/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.resource.entity.luminance;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.api.entity.luminance.EntityLuminance;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import dev.lambdaurora.lambdynlights.resource.entity.EntityLightSources;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.List;

/**
 * Provides a conditional luminance value depending on whether the entity is wet or dry.
 *
 * @param dry the luminance values if the entity is dry
 * @param wet the luminance values if the entity is wet
 * @author LambdAurora
 * @version 4.0.0
 * @since 4.0.0
 */
public record WetSensititiveEntityLuminance(
		List<EntityLuminance> dry,
		List<EntityLuminance> wet
) implements EntityLuminance {
	public static final MapCodec<WetSensititiveEntityLuminance> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
					EntityLuminance.LIST_CODEC
							.optionalFieldOf("dry", List.of())
							.forGetter(WetSensititiveEntityLuminance::dry),
					EntityLuminance.LIST_CODEC
							.optionalFieldOf("wet", List.of())
							.forGetter(WetSensititiveEntityLuminance::wet)
			).apply(instance, WetSensititiveEntityLuminance::new)
	);

	@Override
	public @NotNull Type type() {
		return EntityLightSources.WET_SENSITIVE;
	}

	@Override
	public @Range(from = 0, to = 15) int getLuminance(@NotNull ItemLightSourceManager itemLightSourceManager, @NotNull Entity entity) {
		boolean submergedInWater = entity.isInWaterOrRain();
		boolean shouldCareAboutWater = LambDynLights.get().config.getWaterSensitiveCheck().get();

		if (submergedInWater && (shouldCareAboutWater || this.dry.isEmpty())) {
			return EntityLuminance.getLuminance(itemLightSourceManager, entity, this.wet);
		} else {
			return EntityLuminance.getLuminance(itemLightSourceManager, entity, this.dry);
		}
	}
}
