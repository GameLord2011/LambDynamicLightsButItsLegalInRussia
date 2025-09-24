/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.engine;

import dev.lambdaurora.lambdynlights.engine.source.DynamicLightSource;
import dev.lambdaurora.lambdynlights.mixin.LevelRendererAccessor;
import dev.lambdaurora.lambdynlights.util.DynamicLightDebugRenderer;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.ChunkSectionPos;
import org.jetbrains.annotations.NotNull;
import org.joml.FrustumIntersection;

public final class DynamicLightingChunkRebuildScheduler {
	private final DynamicLightDebugRenderer.SectionRebuild sectionRebuildDebugRenderer;
	private final LongSet chunksToRebuild = new LongOpenHashSet();

	private int lastUpdateCount = 0;
	private int lastQueuedCount = 0;

	public DynamicLightingChunkRebuildScheduler(
			DynamicLightDebugRenderer.SectionRebuild sectionRebuildDebugRenderer
	) {
		this.sectionRebuildDebugRenderer = sectionRebuildDebugRenderer;
	}

	/**
	 * {@return the last number of dynamic light source updates}
	 */
	public int getLastUpdateCount() {
		return this.lastUpdateCount;
	}

	public int getLastQueuedCount() {
		return this.lastQueuedCount;
	}

	/**
	 * {@return the number of currently queued rebuilds}
	 */
	public int getCurrentlyQueued() {
		return this.chunksToRebuild.size();
	}

	public void accept(DynamicLightSource lightSource, LongSet chunks) {
		if (!chunks.isEmpty()) {
			this.chunksToRebuild.addAll(chunks);
			this.lastUpdateCount++;
		}
	}

	public void startTick() {
		this.lastUpdateCount = 0;
		this.lastQueuedCount = 0;
	}

	public void endTick() {
		final var renderer = Minecraft.getInstance().levelRenderer;
		final var frustum = this.getFrustum(renderer);

		var it = this.chunksToRebuild.iterator();
		while (it.hasNext()) {
			long chunkPos = it.nextLong();
			int x = ChunkSectionPos.x(chunkPos);
			int y = ChunkSectionPos.y(chunkPos);
			int z = ChunkSectionPos.z(chunkPos);

			int hitResult = frustum.cubeInFrustum(
					ChunkSectionPos.sectionToBlockCoord(x),
					ChunkSectionPos.sectionToBlockCoord(y),
					ChunkSectionPos.sectionToBlockCoord(z),
					ChunkSectionPos.sectionToBlockCoord(x, 15),
					ChunkSectionPos.sectionToBlockCoord(y, 15),
					ChunkSectionPos.sectionToBlockCoord(z, 15)
			);
			if (hitResult == FrustumIntersection.INTERSECT || hitResult == FrustumIntersection.INSIDE) {
				this.scheduleChunkRebuild(renderer, chunkPos);
				it.remove();
				this.lastQueuedCount++;
			}
		}
	}

	public void clear() {
		this.chunksToRebuild.clear();
	}

	private @NotNull Frustum getFrustum(LevelRenderer renderer) {
		final var capturedFrustum = renderer.getCapturedFrustum();

		return capturedFrustum != null ? capturedFrustum : ((LevelRendererAccessor) renderer).getCullingFrustum();
	}

	/**
	 * Schedules a chunk rebuild at the specified chunk position.
	 *
	 * @param renderer the renderer
	 * @param chunkPos the packed chunk position
	 */
	private void scheduleChunkRebuild(@NotNull LevelRenderer renderer, long chunkPos) {
		scheduleChunkRebuild(
				renderer,
				ChunkSectionPos.x(chunkPos), ChunkSectionPos.y(chunkPos), ChunkSectionPos.z(chunkPos)
		);
		this.sectionRebuildDebugRenderer.scheduleChunkRebuild(chunkPos);
	}

	static void scheduleChunkRebuild(@NotNull LevelRenderer renderer, int x, int y, int z) {
		if (Minecraft.getInstance().level != null)
			((LevelRendererAccessor) renderer).lambdynlights$scheduleChunkRebuild(x, y, z, false);
	}
}
