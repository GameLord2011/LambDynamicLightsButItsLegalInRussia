/*
 * Copyright © 2020 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.api.item;

import dev.lambdaurora.lambdynlights.LambDynLights;
import net.minecraft.resources.io.ResourceManager;
import net.minecraft.world.item.ItemStack;

/**
 * Represents an item light sources manager.
 *
 * @author LambdAurora
 * @version 2.3.2
 * @since 1.3.0
 * @deprecated Please update your usage of
 * the {@linkplain dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager item lighting API} instead, and read
 * <a href="https://lambdaurora.dev/projects/lambdynamiclights/docs/v4/item.html">the official item lighting documentation</a>.
 * <p>
 * This is fully removed in LambDynamicLights releases targeting Minecraft 1.21 and newer.
 */
@Deprecated(forRemoval = true)
public final class ItemLightSources {
	/**
	 * Loads the item light source data from resource pack.
	 *
	 * @param resourceManager The resource manager.
	 */
	public static void load(ResourceManager resourceManager) {
		// NO-OP
	}

	/**
	 * Registers an item light source data.
	 *
	 * @param data the item light source data
	 */
	public static void registerItemLightSource(ItemLightSource data) {
		LambDynLights.get().itemLightSourceManager().onRegisterEvent().register(ctx -> ctx.register(data));
	}

	/**
	 * Returns the luminance of the item in the stack.
	 *
	 * @param stack the item stack
	 * @param submergedInWater {@code true} if the stack is submerged in water, else {@code false}
	 * @return a luminance value
	 */
	public static int getLuminance(ItemStack stack, boolean submergedInWater) {
		return LambDynLights.get().itemLightSourceManager().getLuminance(stack, submergedInWater);
	}
}