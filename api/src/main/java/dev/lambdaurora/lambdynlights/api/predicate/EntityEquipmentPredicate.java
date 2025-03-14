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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Represents a predicate to match entity equipment with.
 * <p>
 * <b>Important Note:</b> In Minecraft 1.21 this is replaced by {@link net.minecraft.advancements.critereon.EntityEquipmentPredicate}.
 *
 * @param head the item to match the head slot with
 * @param chest the item to match the chest slot with
 * @param legs the item to match the legs slot with
 * @param feet the item to match the feet slot with
 * @param mainhand the item to match the main-hand slot with
 * @param offhand the item to match the off-hand slot with
 */
public record EntityEquipmentPredicate(
		Optional<ItemPredicate> head,
		Optional<ItemPredicate> chest,
		Optional<ItemPredicate> legs,
		Optional<ItemPredicate> feet,
		Optional<ItemPredicate> mainhand,
		Optional<ItemPredicate> offhand
) {
	public static final Codec<EntityEquipmentPredicate> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
							ItemPredicate.CODEC.optionalFieldOf("head").forGetter(EntityEquipmentPredicate::head),
							ItemPredicate.CODEC.optionalFieldOf("chest").forGetter(EntityEquipmentPredicate::chest),
							ItemPredicate.CODEC.optionalFieldOf("legs").forGetter(EntityEquipmentPredicate::legs),
							ItemPredicate.CODEC.optionalFieldOf("feet").forGetter(EntityEquipmentPredicate::feet),
							ItemPredicate.CODEC.optionalFieldOf("mainhand").forGetter(EntityEquipmentPredicate::mainhand),
							ItemPredicate.CODEC.optionalFieldOf("offhand").forGetter(EntityEquipmentPredicate::offhand)
					)
					.apply(instance, EntityEquipmentPredicate::new)
	);

	public boolean matches(@Nullable Entity entity) {
		if (entity instanceof LivingEntity living) {
			if (this.head.isPresent() && !this.head.get().test(living.getItemBySlot(EquipmentSlot.HEAD))) {
				return false;
			} else if (this.chest.isPresent() && !this.chest.get().test(living.getItemBySlot(EquipmentSlot.CHEST))) {
				return false;
			} else if (this.legs.isPresent() && !this.legs.get().test(living.getItemBySlot(EquipmentSlot.LEGS))) {
				return false;
			} else if (this.feet.isPresent() && !this.feet.get().test(living.getItemBySlot(EquipmentSlot.FEET))) {
				return false;
			} else {
				return (this.mainhand.isEmpty() || this.mainhand.get().test(living.getItemBySlot(EquipmentSlot.MAINHAND))) && (!this.offhand.isPresent() || this.offhand.get().test(living.getItemBySlot(EquipmentSlot.OFFHAND)));
			}
		} else {
			return false;
		}
	}
}