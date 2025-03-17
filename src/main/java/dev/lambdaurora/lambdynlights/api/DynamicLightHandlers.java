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
import dev.lambdaurora.lambdynlights.LambDynLightsConstants;
import dev.lambdaurora.lambdynlights.api.entity.luminance.EntityLuminance;
import dev.lambdaurora.lambdynlights.api.entity.luminance.WaterSensitiveEntityLuminance;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import dev.lambdaurora.lambdynlights.engine.DynamicLightingEngine;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LambdAurora
 * @version 3.2.2
 * @since 1.1.0
 *
 * @deprecated Please use the {@linkplain dev.lambdaurora.lambdynlights.api.entity.EntityLightSourceManager entity light source API}
 * instead, and read
 * <a href="https://lambdaurora.dev/projects/lambdynamiclights/docs/v4/entity.html">the official entity lighting documentation</a>.
 * <p>
 * This is fully removed in LambDynamicLights releases targeting Minecraft 1.21.4 and newer.
 */
@SuppressWarnings({"removal", "DeprecatedIsStillUsed"})
@Deprecated(forRemoval = true)
@ApiStatus.ScheduledForRemoval(inVersion = "4.0.0+1.21.4")
public final class DynamicLightHandlers {
	private static final Map<EntityType<?>, DynamicLightHandler<?>> HANDLERS = new HashMap<>();
	private static final EntityLuminance.Type DUMMY_RETROFIT_TYPE = new EntityLuminance.Type(
			LambDynLightsConstants.id("dummy/old_dynamic_light_handlers"), null
	);

	private DynamicLightHandlers() {
		throw new UnsupportedOperationException("DynamicLightHandlers only contains static definitions.");
	}

	/**
	 * Registers the default handlers.
	 */
	@SuppressWarnings("unchecked")
	@ApiStatus.Internal
	public static void registerDefaultHandlers() {
		LambDynLights.get().entityLightSourceManager().onRegisterEvent().register(context -> {
			HANDLERS.forEach((type, handler) -> {
				var actualHandler = (DynamicLightHandler<Entity>) handler;

				context.register(type, new WaterSensitiveEntityLuminance(
						List.of(retrofitHandler(actualHandler, false)),
						List.of(retrofitHandler(actualHandler, true))
				));
			});
		});
	}

	/**
	 * Registers an entity dynamic light handler.
	 *
	 * @param type the entity type
	 * @param handler the dynamic light handler
	 * @param <T> the type of the entity
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Entity> void registerDynamicLightHandler(EntityType<T> type, DynamicLightHandler<T> handler) {
		HANDLERS.compute(type, (ignored, existing) -> {
			if (existing != null) {
				return DynamicLightHandler.<T>makeHandler(
						entity -> Math.max(
								((DynamicLightHandler<T>) existing).getLuminance(entity),
								handler.getLuminance(entity)
						),
						entity -> ((DynamicLightHandler<T>) existing).isWaterSensitive(entity) || handler.isWaterSensitive(entity)
				);
			} else {
				return handler;
			}
		});
	}

	/**
	 * Returns the registered dynamic light handler of the specified entity.
	 *
	 * @param type the entity type
	 * @param <T> the type of the entity
	 * @return the registered dynamic light handler
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Entity> @Nullable DynamicLightHandler<T> getDynamicLightHandler(EntityType<T> type) {
		return (DynamicLightHandler<T>) HANDLERS.get(type);
	}

	/**
	 * Returns whether the given entity can light up.
	 *
	 * @param entity the entity
	 * @param <T> the type of the entity
	 * @return {@code true} if the entity can light up, otherwise {@code false}
	 */
	public static <T extends Entity> boolean canLightUp(T entity) {
		return DynamicLightingEngine.canLightUp(entity);
	}

	/**
	 * Returns the luminance from an entity.
	 *
	 * @param entity the entity
	 * @param <T> the type of the entity
	 * @return the luminance
	 */
	public static <T extends Entity> int getLuminanceFrom(T entity) {
		return LambDynLights.getLuminanceFrom(entity);
	}

	private static EntityLuminance retrofitHandler(DynamicLightHandler<Entity> handler, boolean inWater) {
		return new EntityLuminance() {
			@Override
			public @NotNull Type type() {
				return DUMMY_RETROFIT_TYPE;
			}

			@Override
			public @Range(from = 0, to = 15) int getLuminance(
					@NotNull ItemLightSourceManager itemLightSourceManager,
					@NotNull Entity entity
			) {
				if (handler.isWaterSensitive(entity) && inWater) {
					return 0;
				} else {
					return handler.getLuminance(entity);
				}
			}
		};
	}
}
