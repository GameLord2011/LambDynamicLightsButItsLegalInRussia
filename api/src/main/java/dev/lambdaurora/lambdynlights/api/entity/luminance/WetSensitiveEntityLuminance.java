/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.api.entity.luminance;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a conditional luminance value depending on whether the entity is wet or dry.
 *
 * @param dry the luminance values if the entity is dry
 * @param wet the luminance values if the entity is wet
 * @author LambdAurora
 * @version 4.1.0
 * @since 4.1.0
 */
public record WetSensitiveEntityLuminance(
		List<EntityLuminance> dry,
		List<EntityLuminance> wet
) implements EntityLuminance {
	public static final MapCodec<WetSensitiveEntityLuminance> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
					EntityLuminance.LIST_CODEC
							.optionalFieldOf("dry", List.of())
							.forGetter(WetSensitiveEntityLuminance::dry),
					EntityLuminance.LIST_CODEC
							.optionalFieldOf("wet", List.of())
							.forGetter(WetSensitiveEntityLuminance::wet)
			).apply(instance, WetSensitiveEntityLuminance::new)
	);

	@Override
	public @NotNull Type type() {
		return EntityLuminance.Type.WET_SENSITIVE;
	}

	@Override
	public @Range(from = 0, to = 15) int getLuminance(@NotNull ItemLightSourceManager itemLightSourceManager, @NotNull Entity entity) {
		boolean submergedInWater = entity.isInWaterRainOrBubble();

		if (submergedInWater) {
			return EntityLuminance.getLuminance(itemLightSourceManager, entity, this.wet);
		} else {
			return EntityLuminance.getLuminance(itemLightSourceManager, entity, this.dry);
		}
	}
  
	/**
	 * Creates a new builder instance.
	 * @return The builder instance.
	 * @since 4.1.0
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * A builder for creating a new {@link WetSensitiveEntityLuminance} instance.
	 *
	 * @since 4.1.0
	 */
	public static class Builder {
		private final List<EntityLuminance> dry = new ArrayList<>();
		private final List<EntityLuminance> wet = new ArrayList<>();

		public Builder dry(EntityLuminance... luminances) {
			this.dry.addAll(List.of(luminances));
			return this;
		}

		public Builder dry(List<EntityLuminance> luminances) {
			this.dry.addAll(luminances);
			return this;
		}

		public Builder wet(EntityLuminance... luminances) {
			this.wet.addAll(List.of(luminances));
			return this;
		}

		public Builder wet(List<EntityLuminance> luminances) {
			this.wet.addAll(luminances);
			return this;
		}

		public WetSensitiveEntityLuminance build() {
			return new WetSensitiveEntityLuminance(List.copyOf(this.dry), List.copyOf(this.wet));
		}
	}
}
