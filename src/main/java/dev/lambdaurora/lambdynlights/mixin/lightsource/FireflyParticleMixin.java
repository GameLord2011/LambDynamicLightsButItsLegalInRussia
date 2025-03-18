/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.mixin.lightsource;

import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.engine.source.EntityDynamicLightSourceBehavior;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FireflyParticle;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireflyParticle.class)
public abstract class FireflyParticleMixin extends TextureSheetParticle implements EntityDynamicLightSourceBehavior {
	@Shadow
	public abstract int getLightColor(float tickDelta);

	protected FireflyParticleMixin(ClientLevel level, double x, double y, double z) {
		super(level, x, y, z);
	}

	@Unique
	protected int lambdynlights$luminance = 0;
	@Unique
	private int lambdynlights$lastLuminance = 0;
	@Unique
	private long lambdynlights$lastUpdate = 0;
	@Unique
	private double lambdynlights$prevX;
	@Unique
	private double lambdynlights$prevY;
	@Unique
	private double lambdynlights$prevZ;
	@Unique
	private LongSet lambdynlights$trackedLitChunkPos = LongSet.of();

	@Override
	public double getDynamicLightX() {
		return this.x;
	}

	@Override
	public double getDynamicLightY() {
		return this.y;
	}

	@Override
	public double getDynamicLightZ() {
		return this.z;
	}

	@Override
	public Level dynamicLightWorld() {
		return this.level;
	}

	@Override
	public double getDynamicLightPrevX() {
		return this.lambdynlights$prevX;
	}

	@Override
	public double getDynamicLightPrevY() {
		return this.lambdynlights$prevY;
	}

	@Override
	public double getDynamicLightPrevZ() {
		return this.lambdynlights$prevZ;
	}

	@Override
	public void updateDynamicLightPreviousCoordinates() {
		this.lambdynlights$prevX = this.getDynamicLightX();
		this.lambdynlights$prevY = this.getDynamicLightY();
		this.lambdynlights$prevZ = this.getDynamicLightZ();
	}

	@Override
	public void resetDynamicLight() {
		this.lambdynlights$lastLuminance = 0;
	}

	@Override
	public boolean shouldUpdateDynamicLight() {
		var mode = LambDynLights.get().config.getDynamicLightsMode();
		if (!mode.isEnabled())
			return false;
		if (mode.hasDelay()) {
			long currentTime = System.currentTimeMillis();
			if (currentTime < this.lambdynlights$lastUpdate + mode.getDelay()) {
				return false;
			}

			this.lambdynlights$lastUpdate = currentTime;
		}
		return true;
	}

	@Override
	public int getLuminance() {
		return this.lambdynlights$luminance;
	}

	@Override
	public void setLuminance(int luminance) {
		this.lambdynlights$luminance = luminance;
	}

	@Override
	public int getLastDynamicLuminance() {
		return this.lambdynlights$lastLuminance;
	}

	@Override
	public void setLastDynamicLuminance(int luminance) {
		this.lambdynlights$lastLuminance = luminance;
	}

	@Override
	public LongSet lambdynlights$getTrackedLitChunkPos() {
		return this.lambdynlights$trackedLitChunkPos;
	}

	@Override
	public void lambdynlights$setTrackedLitChunkPos(LongSet trackedLitChunkPos) {
		this.lambdynlights$trackedLitChunkPos = trackedLitChunkPos;
	}

	@Inject(method = "tick", at = @At("RETURN"))
	private void lambdynlights$onTick(CallbackInfo ci) {
		if (this.removed) {
			this.setDynamicLightEnabled(false);
		} else {
			if (LambDynLights.get().config.getFireflyLighting().get()) {
				this.dynamicLightTick();
			} else {
				this.setLuminance(0);
			}
			LambDynLights.updateTracking(this);
		}
	}

	@Override
	public void dynamicLightTick() {
		this.setLuminance(LightTexture.block(this.getLightColor(0)) / 2);
	}
}
