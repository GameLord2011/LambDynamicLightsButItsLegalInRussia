/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.api.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lambdaurora.lambdynlights.api.utils.CodecUtils;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ItemLike;

import java.util.Optional;

/**
 * Represents a predicate to match items with.
 * <p>
 * <b>Important Note:</b> In Minecraft 1.21 this is replaced by {@link net.minecraft.advancements.critereon.EntityTypePredicate}.
 *
 * @param items the items to match
 * @param count the item count to match
 * @param durability the durability to match
 * @param enchantments the enchantments to match
 * @param storedEnchantments the stored enchantments to match
 * @param potion the potion to match
 * @param nbt the NBT to match
 */
public record ItemPredicate(
		Optional<HolderSet<Item>> items,
		MinMaxBounds.Ints count,
		MinMaxBounds.Ints durability,
		EnchantmentPredicate[] enchantments,
		EnchantmentPredicate[] storedEnchantments,
		Optional<Potion> potion,
		NbtPredicate nbt
) {
	public static final Codec<ItemPredicate> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					RegistryCodecs.homogeneousList(Registries.ITEM).optionalFieldOf("items").forGetter(ItemPredicate::items),
					CodecUtils.MIN_MAX_INT_CODEC.optionalFieldOf("count", MinMaxBounds.Ints.ANY).forGetter(ItemPredicate::count),
					CodecUtils.MIN_MAX_INT_CODEC.optionalFieldOf("durability", MinMaxBounds.Ints.ANY).forGetter(ItemPredicate::durability),
					CodecUtils.ENCHANTMENTS_PREDICATE_CODEC.optionalFieldOf("enchantments", EnchantmentPredicate.NONE).forGetter(ItemPredicate::enchantments),
					CodecUtils.ENCHANTMENTS_PREDICATE_CODEC.optionalFieldOf("stored_enchantments", EnchantmentPredicate.NONE).forGetter(ItemPredicate::storedEnchantments),
					CodecUtils.identifierResolver(BuiltInRegistries.POTION::getId, BuiltInRegistries.POTION::get).optionalFieldOf("potion").forGetter(ItemPredicate::potion),
					CodecUtils.NBT_PREDICATE_CODEC.optionalFieldOf("nbt", NbtPredicate.ANY).forGetter(ItemPredicate::nbt)
			).apply(instance, ItemPredicate::new)
	);

	public boolean test(ItemStack stack) {
		if (this.items.isPresent() && !this.items.get().contains(stack.getItemHolder())) {
			return false;
		} else if (!this.count.matches(stack.getCount())) {
			return false;
		} else if (!this.durability.isAny() && !stack.isDamageableItem()) {
			return false;
		} else if (!this.durability.matches(stack.getMaxDamage() - stack.getDamageValue())) {
			return false;
		} else if (!this.nbt.matches(stack)) {
			return false;
		} else {
			if (this.enchantments.length > 0) {
				var enchantments = EnchantmentHelper.deserializeEnchantments(stack.getEnchantmentTags());

				for (EnchantmentPredicate enchantmentPredicate : this.enchantments) {
					if (!enchantmentPredicate.containedIn(enchantments)) {
						return false;
					}
				}
			}

			if (this.storedEnchantments.length > 0) {
				var enchantments = EnchantmentHelper.deserializeEnchantments(EnchantedBookItem.getEnchantments(stack));

				for (EnchantmentPredicate enchantmentPredicate : this.storedEnchantments) {
					if (!enchantmentPredicate.containedIn(enchantments)) {
						return false;
					}
				}
			}

			var potion = PotionUtils.getPotion(stack);
			return this.potion.isEmpty() || this.potion.get() == potion;
		}
	}

	public static class Builder {
		private Optional<HolderSet<Item>> items = Optional.empty();
		private MinMaxBounds.Ints count = MinMaxBounds.Ints.ANY;
		private MinMaxBounds.Ints durability = MinMaxBounds.Ints.ANY;

		private Builder() {
		}

		public static Builder item() {
			return new Builder();
		}

		@SuppressWarnings("deprecation")
		public Builder of(ItemLike... items) {
			this.items = Optional.of(HolderSet.direct(itemLike -> itemLike.asItem().builtInRegistryHolder(), items));
			return this;
		}

		public Builder of(TagKey<Item> tag) {
			this.items = Optional.of(BuiltInRegistries.ITEM.getOrCreateTag(tag));
			return this;
		}

		public Builder withCount(MinMaxBounds.Ints count) {
			this.count = count;
			return this;
		}

		public Builder withDurability(MinMaxBounds.Ints durability) {
			this.durability = durability;
			return this;
		}

		public ItemPredicate build() {
			return new ItemPredicate(
					this.items, this.count, this.durability,
					new EnchantmentPredicate[0],
					new EnchantmentPredicate[0],
					Optional.empty(),
					NbtPredicate.ANY
			);
		}
	}
}
