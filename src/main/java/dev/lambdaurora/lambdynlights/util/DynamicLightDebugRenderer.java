/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.util;

import dev.lambdaurora.lambdynlights.DynamicLightsConfig;
import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.engine.scheduler.ChunkRebuildStatus;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.ChunkSectionPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Represents a debug renderer for dynamic lighting.
 *
 * @author LambdAurora
 * @version 4.9.0
 * @since 4.0.0
 */
@Environment(EnvType.CLIENT)
public abstract class DynamicLightDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
	final Minecraft client = Minecraft.getInstance();
	final DynamicLightsConfig config;

	public DynamicLightDebugRenderer(LambDynLights mod) {
		this.config = mod.config;
	}

	static void renderFaces(
			DiscreteVoxelShape shape,
			Vec3i origin,
			int cellSize,
			int color
	) {
		shape.forAllFaces((direction, cellX, cellY, cellZ) -> {
			int realCellX = cellX + origin.getX();
			int realCellY = cellY + origin.getY();
			int realCellZ = cellZ + origin.getZ();
			renderFace(direction, cellSize, realCellX, realCellY, realCellZ, color);
		});
	}

	static void renderEdges(
			DiscreteVoxelShape shape,
			Vec3i origin,
			int cellSize,
			int color
	) {
		shape.forAllEdges((startCellX, startCellY, startCellZ, endCellX, endCellY, endCellZ) -> {
			int realStartCellX = startCellX + origin.getX();
			int realStartCellY = startCellY + origin.getY();
			int realStartCellZ = startCellZ + origin.getZ();
			int realEndCellX = endCellX + origin.getX();
			int realEndCellY = endCellY + origin.getY();
			int realEndCellZ = endCellZ + origin.getZ();
			renderEdge(
					cellSize,
					realStartCellX, realStartCellY, realStartCellZ,
					realEndCellX, realEndCellY, realEndCellZ,
					color
			);
		}, true);
	}

	static void renderFace(
			Direction direction,
			int cellSize, int cellX, int cellY, int cellZ,
			int color
	) {
		var facePos = new Vec3(
				cellX * cellSize,
				cellY * cellSize,
				cellZ * cellSize
		);
		Gizmos.rect(facePos, facePos.add(cellSize), direction, GizmoStyle.fill(color));
	}

	static void renderEdge(
			int cellSize,
			int startCellX, int startCellY, int startCellZ,
			int endCellX, int endCellY, int endCellZ,
			int color
	) {
		float startX = (float) (startCellX * cellSize);
		float startY = (float) (startCellY * cellSize);
		float startZ = (float) (startCellZ * cellSize);
		float endX = (float) (endCellX * cellSize);
		float endY = (float) (endCellY * cellSize);
		float endZ = (float) (endCellZ * cellSize);
		renderLine(startX, startY, startZ, endX, endY, endZ, color);
	}

	static void renderLine(
			float startX, float startY, float startZ,
			float endX, float endY, float endZ,
			int color
	) {
		Gizmos.line(new Vec3(startX, startY, startZ), new Vec3(endX, endY, endZ), color);
	}

	public static class SectionRebuild extends DynamicLightDebugRenderer {
		private static final int SCHEDULED_COLOR = 0x3f0099ff;
		private static final int REQUESTED_COLOR = 0x3f9b00a6;
		private final Long2IntMap scheduledChunks = new Long2IntOpenHashMap();
		private Long2ObjectMap<int[]> requestedChunks;

		public SectionRebuild(LambDynLights mod) {
			super(mod);
		}

		public boolean isEnabled() {
			return this.config.getDebugDisplayDynamicLightingChunkRebuilds().get();
		}

		@Override
		public void emitGizmos(
				double x, double y, double z,
				@NotNull DebugValueAccess debugValueAccess, @NotNull Frustum frustum, float tickDelta
		) {
			if (!this.isEnabled()) return;

			for (var entry : this.scheduledChunks.long2IntEntrySet()) {
				this.addBox(SCHEDULED_COLOR, (int) (entry.getIntValue() / 4.f * 255), ChunkSectionPos.of(entry.getLongKey()));
			}

			if (this.requestedChunks != null) {
				for (var chunk : this.requestedChunks.long2ObjectEntrySet()) {
					var chunkPos = ChunkSectionPos.of(chunk.getLongKey());
					var statuses = chunk.getValue();
					boolean canRenderBox = false;

					int statusY = chunkPos.maxBlockY() - 4;
					for (int i = 0; i < statuses.length; i++) {
						if (statuses[i] > 0) {
							var status = ChunkRebuildStatus.VALUES.get(i);

							Gizmos.billboardText(
									statuses[i] + "x " + status,
									new Vec3(
											chunkPos.minBlockX() + 8,
											statusY,
											chunkPos.minBlockZ() + 8
									),
									TextGizmo.Style.forColorAndCentered(status.color())
											.withScale(1f)
							);

							if (i != ChunkRebuildStatus.AFFECTED.ordinal()) {
								canRenderBox = true;
							}
						}

						statusY -= 2;
					}

					if (canRenderBox) {
						this.addBox(REQUESTED_COLOR, 0xff, chunkPos);
					}
				}
			}
		}

		private void addBox(int color, int alpha, ChunkSectionPos chunk) {
			var box = new AABB(
					chunk.minBlockX(), chunk.minBlockY(), chunk.minBlockZ(),
					ChunkSectionPos.sectionToBlockCoord(chunk.x(), 16),
					ChunkSectionPos.sectionToBlockCoord(chunk.y(), 16),
					ChunkSectionPos.sectionToBlockCoord(chunk.z(), 16)
			);

			Gizmos.cuboid(box, GizmoStyle.stroke((alpha << 24) | (color & 0x00ffffff)));
		}

		public void scheduleChunkRebuild(long chunkPos) {
			if (!this.isEnabled()) return;

			this.scheduledChunks.put(chunkPos, 4);
		}

		public void setRequestedChunks(Supplier<Long2ObjectMap<int[]>> chunks) {
			if (!this.isEnabled()) return;

			this.requestedChunks = chunks.get();
		}

		public void clearRequestedChunks() {
			this.requestedChunks = Long2ObjectMaps.emptyMap();
		}

		public void tick() {
			if (!this.isEnabled()) return;

			var iterator = this.scheduledChunks.long2IntEntrySet().iterator();
			while (iterator.hasNext()) {
				var entry = iterator.next();

				if (entry.getIntValue() == 0) {
					iterator.remove();
				} else {
					entry.setValue(entry.getIntValue() - 1);
				}
			}
		}
	}
}
