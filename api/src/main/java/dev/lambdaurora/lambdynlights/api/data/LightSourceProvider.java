/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.api.data;

import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Provides the means to generate light source JSON files via datagen.
 *
 * @param <L> The type of the light source.
 * @author Thomas Glasser
 * @version 4.1.0
 * @since 4.1.0
 */
public abstract class LightSourceProvider<L> implements DataProvider {
    private final PackOutput.PathProvider pathProvider;
    private final CompletableFuture<HolderLookup.Provider> registryProvider;
    private final String modId;
    private final String subPath;
    private final Codec<L> codec;

    protected final Map<String, L> sources;

    public LightSourceProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registryProvider, String modId, String subPath, Codec<L> codec) {
        this.registryProvider = registryProvider;
        this.modId = modId;
        this.pathProvider = packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "dynamiclights/" + subPath);
        this.subPath = subPath;
        this.codec = codec;
        this.sources = new HashMap<>();
    }

    /**
     * Adds a new light source to the provider.
     *
     * @param id The name of the JSON file. Supports subfolders.
     * @param source The light source
     */
    protected void add(String id, L source) {
        sources.put(id, source);
    }

    /**
     * Generates the light sources and adds them to the list.
     * @param provider The registry provider
     */
    protected abstract void generate(HolderLookup.Provider provider);

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        return registryProvider.thenCompose(provider -> {
            generate(provider);
            List<CompletableFuture<?>> futures = new ArrayList<>();
            sources.forEach((id, source) -> {
                futures.add(DataProvider.saveStable(cachedOutput, provider, codec, source, pathProvider.json(Identifier.of(modId, id))));
            });
            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        });
    }

    @Override
    public String getName() {
        return subPath + " Light Sources Provider";
    }
}
