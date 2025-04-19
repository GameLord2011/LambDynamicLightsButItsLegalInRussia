/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.mixin.retrofit;

import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.api.DynamicLightHandler;
import dev.lambdaurora.lambdynlights.resource.entity.luminance.CreeperLuminance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@SuppressWarnings("removal")
@Mixin(value = DynamicLightHandler.class, remap = false)
public interface DynamicLightHandlerMixin {
	/**
	 * @author LambdAurora
	 * @reason Implementation of the deprecated DynamicLightHandler#makeLivingEntityHandler API.
	 */
	@Overwrite(remap = false)
	static <T extends LivingEntity> @NotNull DynamicLightHandler<T> makeLivingEntityHandler(@NotNull DynamicLightHandler<T> handler) {
		return entity -> {
			int luminance = LambDynLights.getLivingEntityLuminanceFromItems(entity);
			return Math.max(luminance, handler.getLuminance(entity));
		};
	}

	/**
	 * @author LambdAurora
	 * @reason Implementation of the deprecated DynamicLightHandler#makeCreeperEntityHandler API.
	 */
	@Overwrite(remap = false)
	static <T extends Creeper> @NotNull DynamicLightHandler<T> makeCreeperEntityHandler(@Nullable DynamicLightHandler<T> handler) {
		return entity -> {
			int luminance = CreeperLuminance.INSTANCE.getLuminance(LambDynLights.get().itemLightSourceManager(), entity);

			if (handler != null)
				luminance = Math.max(luminance, handler.getLuminance(entity));

			return luminance;
		};
	}
}
