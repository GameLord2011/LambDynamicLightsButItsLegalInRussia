/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.engine.scheduler;

/**
 * Represents the status of a chunk section rebuild in the context of dynamic lighting.
 *
 * @author LambdAurora
 * @version 4.8.0
 * @since 4.8.0
 */
public enum ChunkRebuildStatus {
	REQUESTED,
	AFFECTED,
	REQUESTED_AGAIN,
	REMOVE_REQUESTED;

	public boolean needsRebuild() {
		return this != AFFECTED;
	}

	public boolean needsCleanup() {
		return this == AFFECTED || this == REQUESTED_AGAIN;
	}
}
