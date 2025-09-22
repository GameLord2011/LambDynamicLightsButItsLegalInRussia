/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.engine.source;

import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.yumi.mc.core.api.YumiMods;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;

/**
 * Represents the ticking behavior of particle light sources.
 *
 * @author LambdAurora
 * @version 4.4.2
 * @since 4.4.2
 */
public final class ParticleLightSourceBehavior {
	private static final boolean HAS_ASYNC_PARTICLES = YumiMods.get().isModLoaded("asyncparticles");

	public static void tickParticle(Particle particle) {
		if (HAS_ASYNC_PARTICLES) {
			Minecraft.getInstance().execute(() -> doTickParticle(particle));
		} else {
			doTickParticle(particle);
		}
	}

	private static void doTickParticle(Particle particle) {
		var lightSource = (EntityDynamicLightSourceBehavior) particle;

		if (!particle.isAlive()) {
			lightSource.setDynamicLightEnabled(false);
		} else {
			if (LambDynLights.get().canLightParticle(particle)) {
				lightSource.dynamicLightTick();
			} else {
				lightSource.setLuminance(0);
			}
			LambDynLights.updateTracking(lightSource);
		}
	}
}
