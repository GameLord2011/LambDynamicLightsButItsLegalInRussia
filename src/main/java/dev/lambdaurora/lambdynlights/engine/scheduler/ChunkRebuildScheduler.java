/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.engine.scheduler;

import dev.lambdaurora.lambdynlights.engine.source.DynamicLightSource;
import dev.lambdaurora.lambdynlights.mixin.LevelRendererAccessor;
import dev.lambdaurora.lambdynlights.util.DynamicLightDebugRenderer;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.Minecraft;
import net.minecraft.core.ChunkSectionPos;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public abstract class ChunkRebuildScheduler {
	protected final Minecraft client = Minecraft.getInstance();
	protected final DynamicLightDebugRenderer.SectionRebuild sectionRebuildDebugRenderer;
	private int sourceUpdatedLastTick = 0;

	protected ChunkRebuildScheduler(DynamicLightDebugRenderer.SectionRebuild sectionRebuildDebugRenderer) {
		this.sectionRebuildDebugRenderer = sectionRebuildDebugRenderer;
	}

	/**
	 * {@return the last number of dynamic light source updates}
	 */
	public int getSourceUpdatedLastTick() {
		return this.sourceUpdatedLastTick;
	}

	public void appendF3Debug(@NotNull Consumer<String> consumer) {
	}

	public final void update(@NotNull DynamicLightSource lightSource, @NotNull LongSet chunks) {
		if (!chunks.isEmpty()) {
			this.sourceUpdatedLastTick++;
			this.accept(lightSource, chunks);
		}
	}

	protected abstract void accept(@NotNull DynamicLightSource lightSource, @NotNull LongSet chunks);

	public final void remove(@NotNull DynamicLightSource lightSource) {
		final var chunks = lightSource.getDynamicLightChunksToRebuild(true);

		if (!chunks.isEmpty()) {
			this.sourceUpdatedLastTick++;
		}

		this.remove(lightSource, chunks);
	}

	protected abstract void remove(@NotNull DynamicLightSource lightSource, @NotNull LongSet chunks);

	/**
	 * Clears this chunk rebuild scheduler.
	 */
	public abstract void clear();

	public void startTick() {
		this.sourceUpdatedLastTick = 0;
	}

	public void endTick() {}

	/**
	 * Schedules a chunk rebuild at the specified chunk position.
	 *
	 * @param chunkPos the packed chunk position
	 */
	protected final void scheduleChunkRebuild(long chunkPos) {
		this.scheduleChunkRebuild(ChunkSectionPos.x(chunkPos), ChunkSectionPos.y(chunkPos), ChunkSectionPos.z(chunkPos));
		this.sectionRebuildDebugRenderer.scheduleChunkRebuild(chunkPos);
	}

	private void scheduleChunkRebuild(int x, int y, int z) {
		if (this.client.level != null) {
			((LevelRendererAccessor) this.client.levelRenderer).lambdynlights$scheduleChunkRebuild(x, y, z, false);
		}
	}
}
