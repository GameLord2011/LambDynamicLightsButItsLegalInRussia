/*
 * Copyright © 2020 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.api;

import org.jetbrains.annotations.ApiStatus;

/**
 * Represents the entrypoint for LambDynamicLights' API.
 *
 * @author LambdAurora
 * @version 4.0.0
 * @since 1.3.2
 */
public interface DynamicLightsInitializer {
	/**
	 * The entrypoint key for LambDynamicLights' API, whose value is {@value}.
	 *
	 * @since 4.0.0
	 */
	String ENTRYPOINT_KEY = "lambdynlights:initializer";

	/**
	 * Called when LambDynamicLights is initialized to register various objects related to dynamic lighting such as:
	 * <ul>
	 *     <li>entity luminance providers;</li>
	 *     <li>item and entity light sources;</li>
	 *     <li>custom dynamic lighting behavior.</li>
	 * </ul>
	 *
	 * @param context the dynamic lights context, containing references to managers for each source type provided by the API
	 */
	default void onInitializeDynamicLights(DynamicLightsContext context) {
		this.onInitializeDynamicLights();
	}

	/**
	 * Called when LambDynamicLights is initialized to register custom dynamic light handlers and item light sources.
	 *
	 * @deprecated Please use the {@link #onInitializeDynamicLights(DynamicLightsContext)} instead, and read
	 * <a href="https://lambdaurora.dev/projects/lambdynamiclights/docs/v4/java.html">the official documentation</a>.
	 * <p>
	 * This is fully removed in LambDynamicLights releases targeting Minecraft 1.21 and newer.
	 */
	@SuppressWarnings("DeprecatedIsStillUsed")
	@Deprecated(forRemoval = true)
	@ApiStatus.ScheduledForRemoval(inVersion = "3.0.0+1.21.1")
	void onInitializeDynamicLights();
}
