/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.util;

import com.mojang.blaze3d.vertex.MatrixStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehavior;
import dev.lambdaurora.lambdynlights.engine.source.DeferredDynamicLightSource;
import dev.lambdaurora.lambdynlights.engine.source.DynamicLightSource;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.debug.DebugValueAccess;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Represents a debug renderer for the bounding boxes of {@link DynamicLightBehavior}.
 *
 * @author Akarys
 * @version 4.5.2
 * @since 4.0.0
 */
@Environment(EnvType.CLIENT)
public class DynamicLightBehaviorDebugRenderer extends DynamicLightDebugRenderer {
	private final Set<DynamicLightSource> lightSourceSetRef;

	public DynamicLightBehaviorDebugRenderer(LambDynLights mod, Set<DynamicLightSource> lightSourceSetRef) {
		super(mod);
		this.lightSourceSetRef = lightSourceSetRef;
	}

	private boolean isEnabled() {
		return this.config.getDebugDisplayHandlerBoundingBox().get();
	}

	@Override
	public void render(
			@NotNull MatrixStack matrices, @NotNull MultiBufferSource bufferSource, double x, double y, double z,
			@NotNull DebugValueAccess debugValueAccess, @NotNull Frustum frustum
	) {
		if (!this.isEnabled()) {
			return;
		}

		matrices.push();
		matrices.translate(-x, -y, -z);
		this.lightSourceSetRef.forEach(lightSource -> {
			if (lightSource instanceof DeferredDynamicLightSource deferredLightSource) {
				VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.lines());

				DynamicLightBehavior.BoundingBox boundingBox = deferredLightSource.behavior().getBoundingBox();

				ShapeRenderer.renderLineBox(
						matrices.peek(), vertexConsumer,
						boundingBox.startX(), boundingBox.startY(), boundingBox.startZ(),
						boundingBox.endX(), boundingBox.endY(), boundingBox.endZ(),
						1.f, 0.f, 0.f, 0.8f
				);
			}
		});
		matrices.pop();
	}
}
