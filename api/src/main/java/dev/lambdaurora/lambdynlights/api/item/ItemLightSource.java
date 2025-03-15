/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.api.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lambdaurora.lambdynlights.api.predicate.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Range;

/**
 * Represents an item light source.
 *
 * @author LambdAurora
 * @version 3.0.0
 * @since 3.0.0
 */
public class ItemLightSource {
	public static final Codec<ItemLightSource> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					ItemPredicate.CODEC.fieldOf("match").forGetter(ItemLightSource::predicate),
					ItemLuminance.CODEC.fieldOf("luminance").forGetter(ItemLightSource::luminance),
					Codec.BOOL.optionalFieldOf("water_sensitive", false).forGetter(ItemLightSource::waterSensitive)
			).apply(instance, ItemLightSource::new)
	);

	private final ItemPredicate predicate;
	private final ItemLuminance luminance;
	private final boolean waterSensitive;

	public ItemLightSource(ItemPredicate predicate, ItemLuminance luminance, boolean waterSensitive) {
		this.predicate = predicate;
		this.luminance = luminance;
		this.waterSensitive = waterSensitive;
	}

	public ItemLightSource(ItemPredicate predicate, @Range(from = 0, to = 15) int luminance) {
		this(predicate, new ItemLuminance.Value(luminance));
	}

	public ItemLightSource(ItemPredicate predicate, ItemLuminance luminance) {
		this(predicate, luminance, false);
	}

	/**
	 * {@return the predicate to select which items emit the given luminance}
	 */
	public ItemPredicate predicate() {
		return this.predicate;
	}

	/**
	 * {@return the luminance to emit}
	 */
	public ItemLuminance luminance() {
		return this.luminance;
	}

	/**
	 * {@return {@code true} if this light source is sensitive to water, or {@code false} otherwise}
	 */
	public boolean waterSensitive() {
		return this.waterSensitive;
	}

	/**
	 * Gets the identifier of this item light source.
	 *
	 * @return the identifier of this item light source
	 * @deprecated Please update your usage of
	 * the {@linkplain dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager item lighting API} instead, and read
	 * <a href="https://lambdaurora.dev/projects/lambdynamiclights/docs/v4/item.html">the official item lighting documentation</a>.
	 * <p>
	 * This is fully removed in LambDynamicLights releases targeting Minecraft 1.21 and newer.
	 */
	@Deprecated(forRemoval = true)
	@ApiStatus.ScheduledForRemoval(inVersion = "3.0.0+1.21.1")
	public Identifier id() {
		return null;
	}

	/**
	 * Gets the item associated with this light source.
	 *
	 * @return the item
	 * @deprecated Please update your usage of
	 * the {@linkplain dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager item lighting API} instead, and read
	 * <a href="https://lambdaurora.dev/projects/lambdynamiclights/docs/v4/item.html">the official item lighting documentation</a>.
	 * <p>
	 * This is fully removed in LambDynamicLights releases targeting Minecraft 1.21 and newer.
	 */
	@Deprecated(forRemoval = true)
	@ApiStatus.ScheduledForRemoval(inVersion = "3.0.0+1.21.1")
	public Item item() {
		return this.predicate.items()
				.flatMap(items -> items.stream().findFirst())
				.map(Holder::value)
				.orElse(null);
	}

	/**
	 * Gets the luminance of the item.
	 *
	 * @param stack the item stack
	 * @return the luminance value between {@code 0} and {@code 15}
	 */
	public @Range(from = 0, to = 15) int getLuminance(ItemStack stack) {
		if (this.predicate.test(stack)) {
			return this.luminance.getLuminance(stack);
		}

		return 0;
	}

	/**
	 * Gets the luminance of the item.
	 *
	 * @param stack the item stack
	 * @param submergedInWater {@code true} if submerged in water, else {@code false}.
	 * @return the luminance value between {@code 0} and {@code 15}
	 * @deprecated Please update your usage of
	 * the {@linkplain dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager item lighting API} instead, and read
	 * <a href="https://lambdaurora.dev/projects/lambdynamiclights/docs/v4/item.html">the official item lighting documentation</a>.
	 * <p>
	 * This is fully removed in LambDynamicLights releases targeting Minecraft 1.21 and newer.
	 */
	@Deprecated(forRemoval = true)
	@ApiStatus.ScheduledForRemoval(inVersion = "3.0.0+1.21.1")
	public int getLuminance(ItemStack stack, boolean submergedInWater) {
		return this.getLuminance(stack);
	}

	/**
	 * @deprecated Please update your usage of
	 * the {@linkplain dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager item lighting API} instead, and read
	 * <a href="https://lambdaurora.dev/projects/lambdynamiclights/docs/v4/item.html">the official item lighting documentation</a>.
	 * <p>
	 * This is fully removed in LambDynamicLights releases targeting Minecraft 1.21 and newer.
	 */
	@Deprecated(forRemoval = true)
	@ApiStatus.ScheduledForRemoval(inVersion = "3.0.0+1.21.1")
	public static class StaticItemLightSource extends ItemLightSource {
		public StaticItemLightSource(Identifier id, Item item, int luminance, boolean waterSensitive) {
			super(ItemPredicate.Builder.item().of(item).build(), new ItemLuminance.Value(luminance), waterSensitive);
		}

		public StaticItemLightSource(Identifier id, Item item, int luminance) {
			this(id, item, luminance, false);
		}
	}

	/**
	 * @deprecated Please update your usage of
	 * the {@linkplain dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager item lighting API} instead, and read
	 * <a href="https://lambdaurora.dev/projects/lambdynamiclights/docs/v4/item.html">the official item lighting documentation</a>.
	 * <p>
	 * This is fully removed in LambDynamicLights releases targeting Minecraft 1.21 and newer.
	 */
	@Deprecated(forRemoval = true)
	@ApiStatus.ScheduledForRemoval(inVersion = "3.0.0+1.21.1")
	public static class BlockItemLightSource extends ItemLightSource {
		public BlockItemLightSource(Identifier id, Item item, BlockState block, boolean waterSensitive) {
			super(ItemPredicate.Builder.item().of(item).build(), new ItemLuminance.BlockReference(block.getBlock()), waterSensitive);
		}
	}
}
