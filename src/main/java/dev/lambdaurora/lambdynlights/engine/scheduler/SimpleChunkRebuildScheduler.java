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
import dev.lambdaurora.lambdynlights.util.DynamicLightDebugRenderer;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.jetbrains.annotations.NotNull;

public final class SimpleChunkRebuildScheduler extends ChunkRebuildScheduler {
	public SimpleChunkRebuildScheduler(DynamicLightDebugRenderer.SectionRebuild sectionRebuildDebugRenderer) {
		super(sectionRebuildDebugRenderer);
	}

	@Override
	protected void accept(@NotNull DynamicLightSource lightSource, @NotNull LongSet chunks) {
		chunks.forEach(this::scheduleChunkRebuild);
	}

	@Override
	protected void remove(@NotNull DynamicLightSource lightSource, @NotNull LongSet chunks) {
		chunks.forEach(this::scheduleChunkRebuild);
	}

	@Override
	public void clear() {}
}
