/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.engine.scheduler;

import dev.lambdaurora.lambdynlights.accessor.FrustumStorage;
import dev.lambdaurora.lambdynlights.engine.source.DynamicLightSource;
import dev.lambdaurora.lambdynlights.util.DynamicLightDebugRenderer;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.ChunkSectionPos;
import org.jetbrains.annotations.NotNull;
import org.joml.FrustumIntersection;

import java.util.HashMap;
import java.util.function.Consumer;

public final class CullingChunkRebuildScheduler extends ChunkRebuildScheduler {
	private final HashMap<DynamicLightSource, LongSet> chunksToRebuild = new HashMap<>();

	private int rebuildQueuedLastTick = 0;

	public CullingChunkRebuildScheduler(
			DynamicLightDebugRenderer.SectionRebuild sectionRebuildDebugRenderer
	) {
		super(sectionRebuildDebugRenderer);
	}

	public int getRebuildQueuedLastTick() {
		return this.rebuildQueuedLastTick;
	}

	/**
	 * {@return the number of currently queued rebuilds}
	 */
	public long getCurrentlyQueued() {
		return this.chunksToRebuild.values().stream()
				.flatMapToLong(LongCollection::longStream)
				.distinct()
				.count();
	}

	@Override
	public void appendF3Debug(@NotNull Consumer<String> consumer) {
		consumer.accept("Scheduled Chunk Rebuilds: %d / %d"
				.formatted(this.getRebuildQueuedLastTick(), this.getCurrentlyQueued())
		);
	}

	@Override
	public void accept(@NotNull DynamicLightSource lightSource, @NotNull LongSet chunks) {
		if (!chunks.isEmpty()) {
			this.chunksToRebuild.computeIfAbsent(lightSource, k -> new LongOpenHashSet()).addAll(chunks);
		}
	}

	@Override
	public void remove(@NotNull DynamicLightSource lightSource, @NotNull LongSet chunks) {
		if (!chunks.isEmpty()) {
			this.chunksToRebuild.put(lightSource, chunks);
		} else {
			this.chunksToRebuild.remove(lightSource);
		}
	}

	@Override
	public void startTick() {
		super.startTick();
		this.rebuildQueuedLastTick = 0;
	}

	@Override
	public void endTick() {
		final var renderer = Minecraft.getInstance().levelRenderer;
		final var frustum = this.getFrustum(renderer);

		final var alreadyVisited = new Long2BooleanOpenHashMap();

		var lightSourceIt = this.chunksToRebuild.entrySet().iterator();
		while (lightSourceIt.hasNext()) {
			final var chunks = lightSourceIt.next().getValue();

			var chunkIt = chunks.iterator();
			while (chunkIt.hasNext()) {
				long chunkPos = chunkIt.nextLong();

				if (!alreadyVisited.containsKey(chunkPos)) {
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
					boolean isNotCulled = hitResult == FrustumIntersection.INTERSECT || hitResult == FrustumIntersection.INSIDE;
					if (isNotCulled) {
						this.scheduleChunkRebuild(chunkPos);
						this.rebuildQueuedLastTick++;
					}
					alreadyVisited.put(chunkPos, isNotCulled);
				}

				if (alreadyVisited.get(chunkPos)) {
					chunkIt.remove();
				}
			}

			if (chunks.isEmpty()) {
				lightSourceIt.remove();
			}
		}

		if (this.sectionRebuildDebugRenderer.isEnabled()) {
			this.sectionRebuildDebugRenderer.setRequestedChunks(
					this.chunksToRebuild.values().stream()
							.flatMapToLong(LongCollection::longStream)
							.distinct()
							.toArray()
			);
		}
	}

	@Override
	public void clear() {
		this.chunksToRebuild.clear();
	}

	private @NotNull Frustum getFrustum(LevelRenderer renderer) {
		return ((FrustumStorage) renderer).lambdynlights$getFrustum();
	}
}
