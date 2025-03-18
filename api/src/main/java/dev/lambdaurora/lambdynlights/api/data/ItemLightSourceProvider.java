/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.api.data;

import dev.lambdaurora.lambdynlights.api.item.ItemLightSource;
import dev.lambdaurora.lambdynlights.api.item.ItemLuminance;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import java.util.concurrent.CompletableFuture;

/**
 * Provides the means to generate item light source JSON files via datagen.
 *
 * @author Thomas Glasser
 * @version 4.1.0
 * @since 4.1.0
 */
public abstract class ItemLightSourceProvider extends LightSourceProvider<ItemLightSource> {
    public ItemLightSourceProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registryProvider, String modId) {
        super(packOutput, registryProvider, modId, "item", ItemLightSource.CODEC);
    }

    /**
     * Adds a new item light source to the provider.
     *
     * @param key The name of the JSON file. Supports subfolders.
     * @param predicate the predicate to select which items emit the given luminance
     * @param luminance the luminance to emit
     * @param waterSensitive {@code true} if this light source is sensitive to water, or {@code false} otherwise
     */
    protected void add(String key, ItemPredicate predicate, ItemLuminance luminance, boolean waterSensitive) {
        add(key, new ItemLightSource(predicate, luminance, waterSensitive));
    }

    /**
     * Adds a new item light source to the provider.
     *
     * @param key The name of the JSON file. Supports subfolders.
     * @param luminance the luminance to emit
     * @param waterSensitive {@code true} if this light source is sensitive to water, or {@code false} otherwise
     * @param items the items that emit the given luminance
     */
    protected void add(String key, ItemLuminance luminance, boolean waterSensitive, ItemLike... items) {
        add(key, ItemPredicate.Builder.item().of(items).build(), luminance, waterSensitive);
    }

    /**
     * Adds a new item light source to the provider.
     *
     * @param key The name of the JSON file. Supports subfolders.
     * @param tag The tag that selects the items that emit the given luminance.
     * @param luminance the luminance to emit
     * @param waterSensitive {@code true} if this light source is sensitive to water, or {@code false} otherwise
     */
    protected void add(String key, TagKey<Item> tag, ItemLuminance luminance, boolean waterSensitive) {
        add(key, ItemPredicate.Builder.item().of(tag).build(), luminance, waterSensitive);
    }
}
