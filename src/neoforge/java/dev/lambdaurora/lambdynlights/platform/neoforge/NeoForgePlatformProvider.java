/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.platform.neoforge;

import dev.lambdaurora.lambdynlights.platform.Platform;
import dev.lambdaurora.lambdynlights.platform.PlatformProvider;
import dev.yumi.mc.core.api.ModContainer;

public final class NeoForgePlatformProvider implements PlatformProvider {
	@Override
	public Platform getPlatform(ModContainer mod) {
		return new NeoForgePlatform(mod);
	}
}
