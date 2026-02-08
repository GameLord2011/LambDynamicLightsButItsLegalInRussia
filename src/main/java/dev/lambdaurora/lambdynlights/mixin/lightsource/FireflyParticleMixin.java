/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.mixin.lightsource;

import dev.lambdaurora.lambdynlights.engine.source.EntityDynamicLightSourceBehavior;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FireflyParticle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.LightCoordsUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FireflyParticle.class)
public abstract class FireflyParticleMixin extends SingleQuadParticle implements EntityDynamicLightSourceBehavior {
	@Shadow
	public abstract int getLightCoords(float tickDelta);

	protected FireflyParticleMixin(ClientLevel clientLevel, double d, double e, double oZ, TextureAtlasSprite sprite) {
		super(clientLevel, d, e, oZ, sprite);
	}

	@Override
	public void dynamicLightTick() {
		this.setLuminance(LightCoordsUtil.block(this.getLightCoords(0)) / 2);
	}
}
