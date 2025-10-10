/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.util;

import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehavior;
import dev.lambdaurora.lambdynlights.engine.source.DeferredDynamicLightSource;
import dev.lambdaurora.lambdynlights.engine.source.DynamicLightSource;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Represents a debug renderer for the bounding boxes of {@link DynamicLightBehavior}.
 *
 * @author Akarys
 * @version 4.9.0
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
	public void emitGizmos(
			double x, double y, double z,
			@NotNull DebugValueAccess debugValueAccess, @NotNull Frustum frustum, float tickDelta
	) {
		if (!this.isEnabled()) {
			return;
		}

		this.lightSourceSetRef.forEach(lightSource -> {
			if (lightSource instanceof DeferredDynamicLightSource deferredLightSource) {
				DynamicLightBehavior.BoundingBox boundingBox = deferredLightSource.behavior().getBoundingBox();
				var box = new AABB(
						boundingBox.startX(), boundingBox.startY(), boundingBox.startZ(),
						boundingBox.endX(), boundingBox.endY(), boundingBox.endZ()
				);

				Gizmos.cuboid(box, GizmoStyle.stroke(0xccff0000));
			}
		});
	}
}
