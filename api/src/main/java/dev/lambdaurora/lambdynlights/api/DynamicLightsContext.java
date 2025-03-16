/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.api;

import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehaviorManager;
import dev.lambdaurora.lambdynlights.api.entity.EntityLightSourceManager;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;

/**
 * Represents the dynamic lights context, containing references to managers for each source type provided by this API.
 *
 * @author LambdAurora
 * @version 4.0.0
 * @since 4.0.0
 */
public interface DynamicLightsContext {
	/**
	 * {@return the manager for item light sources}
	 */
	ItemLightSourceManager itemLightSourceManager();

	/**
	 * {@return the manager for entity light sources}
	 */
	EntityLightSourceManager entityLightSourceManager();

	/**
	 * {@return the manager for dynamic light behaviors and associated light sources}
	 */
	DynamicLightBehaviorManager dynamicLightBehaviorManager();
}
