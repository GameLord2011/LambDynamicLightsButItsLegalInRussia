/*
 * Copyright © 2020 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.api;

import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.resource.entity.luminance.CreeperLuminance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Represents a dynamic light handler.
 *
 * @param <T> The type of the light source.
 * @author LambdAurora
 * @version 3.1.2
 * @since 1.1.0
 * @deprecated Please use the {@linkplain dev.lambdaurora.lambdynlights.api.entity.EntityLightSourceManager entity light source API}
 * instead, and read
 * <a href="https://lambdaurora.dev/projects/lambdynamiclights/docs/v4/entity.html">the official entity lighting documentation</a>.
 * <p>
 * This is fully removed in LambDynamicLights releases targeting Minecraft 1.21.4 and newer.
 */
@Deprecated(forRemoval = true)
public interface DynamicLightHandler<T> {
	/**
	 * Returns the luminance of the light source.
	 *
	 * @param lightSource The light source.
	 * @return The luminance.
	 */
	int getLuminance(T lightSource);

	/**
	 * Returns whether the light source is water-sensitive or not.
	 *
	 * @param lightSource The light source.
	 * @return True if the light source is water-sensitive, else false.
	 */
	default boolean isWaterSensitive(T lightSource) {
		return false;
	}

	/**
	 * Returns a dynamic light handler.
	 *
	 * @param luminance The luminance function.
	 * @param waterSensitive The water sensitive function.
	 * @param <T> The type of the entity.
	 * @return The completed handler.
	 */
	static <T extends Entity> @NotNull DynamicLightHandler<T> makeHandler(
			Function<T, Integer> luminance, Function<T, Boolean> waterSensitive
	) {
		return new DynamicLightHandler<>() {
			@Override
			public int getLuminance(T lightSource) {
				return luminance.apply(lightSource);
			}

			@Override
			public boolean isWaterSensitive(T lightSource) {
				return waterSensitive.apply(lightSource);
			}
		};
	}

	/**
	 * Returns a living entity dynamic light handler.
	 *
	 * @param handler The handler.
	 * @param <T> The type of the entity.
	 * @return The completed handler.
	 */
	static <T extends LivingEntity> @NotNull DynamicLightHandler<T> makeLivingEntityHandler(@NotNull DynamicLightHandler<T> handler) {
		return entity -> {
			int luminance = LambDynLights.getLivingEntityLuminanceFromItems(entity);
			return Math.max(luminance, handler.getLuminance(entity));
		};
	}

	/**
	 * Returns a Creeper dynamic light handler.
	 *
	 * @param handler Extra handler.
	 * @param <T> The type of Creeper entity.
	 * @return The completed handler.
	 */
	static <T extends Creeper> @NotNull DynamicLightHandler<T> makeCreeperEntityHandler(@Nullable DynamicLightHandler<T> handler) {
		return entity -> {
			int luminance = CreeperLuminance.INSTANCE.getLuminance(LambDynLights.get().itemLightSourceManager(), entity);

			if (handler != null)
				luminance = Math.max(luminance, handler.getLuminance(entity));

			return luminance;
		};
	}
}
