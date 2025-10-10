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
import dev.lambdaurora.lambdynlights.engine.DynamicLightingEngine;
import dev.lambdaurora.spruceui.util.ColorUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a debug renderer for dynamic light levels.
 *
 * @author Akarys
 * @version 4.9.0
 * @since 4.0.0
 */
@Environment(EnvType.CLIENT)
public class DynamicLightLevelDebugRenderer extends DynamicLightDebugRenderer {
	private final DynamicLightingEngine lightingEngine;

	public DynamicLightLevelDebugRenderer(LambDynLights mod) {
		super(mod);
		this.lightingEngine = mod.engine;
	}

	@Override
	public void emitGizmos(
			double x, double y, double z,
			@NotNull DebugValueAccess debugValueAccess, @NotNull Frustum frustum, float tickDelta
	) {
		int lightDisplayRadius = this.config.getDebugLightLevelRadius();

		if (lightDisplayRadius == 0) {
			// Don't render debugging stuff if debug rendering is disabled.
			return;
		}

		int startX = this.client.player.getBlockPos().getX();
		int startY = this.client.player.getBlockPos().getY();
		int startZ = this.client.player.getBlockPos().getZ();
		var pos = new BlockPos.Mutable();

		if (lightDisplayRadius > 0) {
			for (int offsetX = 0; offsetX < lightDisplayRadius * 2 + 1; offsetX++) {
				for (int offsetY = 0; offsetY < lightDisplayRadius * 2 + 1; offsetY++) {
					for (int offsetZ = 0; offsetZ < lightDisplayRadius * 2 + 1; offsetZ++) {
						int currentX = startX + offsetX - lightDisplayRadius;
						int currentY = startY + offsetY - lightDisplayRadius;
						int currentZ = startZ + offsetZ - lightDisplayRadius;
						pos.set(currentX, currentY, currentZ);

						double light = this.lightingEngine.getDynamicLightLevel(pos);

						if (light <= 0.05) {
							continue;
						}

						int red;
						int green;

						if (light < 7.5) {
							red = 255;
							green = (int) MathHelper.lerp(light / 7.5, 0x00, 0xFF);
						} else {
							red = (int) MathHelper.lerp((light - 7.5) / 7.5, 0xFF, 0x00);
							green = 255;
						}

						Gizmos.billboardText(
								"%.1f".formatted(light),
								new Vec3(
										currentX + 0.5,
										currentY + 0.5,
										currentZ + 0.5
								),
								TextGizmo.Style.forColorAndCentered(ColorUtil.packARGBColor(red, green, 0, 255))
						);
					}
				}
			}
		}
	}
}
