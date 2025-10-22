/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.mixin;

import dev.lambdaurora.lambdynlights.accessor.FrustumStorage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin implements FrustumStorage {
	@Unique
	private Frustum lambdynlights$frustum;

	@Override
	public Frustum lambdynlights$getFrustum() {
		return this.lambdynlights$frustum;
	}

	@Inject(method = "prepareCullFrustum", at = @At("RETURN"))
	private void lambdynlights$onPrepareCullFrustum(
			Matrix4f matrix4f, Matrix4f matrix4f2, Vec3 vec3, CallbackInfoReturnable<Frustum> cir
	) {
		this.lambdynlights$frustum = cir.getReturnValue();
	}
}
