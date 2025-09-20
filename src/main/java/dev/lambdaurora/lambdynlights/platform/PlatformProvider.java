/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.platform;

import dev.yumi.mc.core.api.ModContainer;

public interface PlatformProvider {
	Platform getPlatform(ModContainer mod);
}
