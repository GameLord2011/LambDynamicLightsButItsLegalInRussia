/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.mixin.lightsource;

import dev.lambdaurora.lambdynlights.api.behavior.LineLightBehavior;
import dev.lambdaurora.lambdynlights.echo.GuardianEntityLightSource;
import net.minecraft.world.entity.monster.Guardian;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Guardian.class)
public class GuardianEntityMixin implements GuardianEntityLightSource {
	@Unique
	private LineLightBehavior dynamicLightBeam;

	@Override
	public LineLightBehavior lambdynlights$getDynamicLightBeam() {
		return this.dynamicLightBeam;
	}

	@Override
	public void lambdynlights$setDynamicLightBeam(LineLightBehavior beam) {
		this.dynamicLightBeam = beam;
	}
}
