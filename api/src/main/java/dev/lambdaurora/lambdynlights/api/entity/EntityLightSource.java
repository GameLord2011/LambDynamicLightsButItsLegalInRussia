/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.api.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lambdaurora.lambdynlights.api.entity.luminance.EntityLuminance;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import dev.lambdaurora.lambdynlights.api.predicate.LightSourceLocationPredicate;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Range;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Represents an entity light source.
 *
 * @param predicate the predicate to select which entities emit the given luminance
 * @param luminances the luminance sources
 * @author LambdAurora
 * @version 4.1.0
 * @since 4.0.0
 */
public record EntityLightSource(EntityPredicate predicate, List<EntityLuminance> luminances) {
	public static final Codec<EntityLightSource> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
							EntityPredicate.CODEC.fieldOf("match").forGetter(EntityLightSource::predicate),
							EntityLuminance.LIST_CODEC.fieldOf("luminance").forGetter(EntityLightSource::luminances)
					)
					.apply(instance, EntityLightSource::new)
	);

	/**
	 * Gets the luminance of the entity.
	 *
	 * @param itemLightSourceManager the item light source manager
	 * @param entity the entity
	 * @return the luminance value between {@code 0} and {@code 15}
	 */
	public @Range(from = 0, to = 15) int getLuminance(ItemLightSourceManager itemLightSourceManager, Entity entity) {
		if (this.predicate.test(entity)) {
			return EntityLuminance.getLuminance(itemLightSourceManager, entity, this.luminances);
		}

		return 0;
	}

	/**
	 * Represents a predicate to match entities with.
	 * <p>
	 * This is inspired from the {@linkplain net.minecraft.advancements.critereon.EntityPredicate entity predicate}
	 * found in advancements but with fewer features since this one needs to work on the client.
	 *
	 * @param entityType the entity type predicate to match if present
	 * @param located the location predicate to match if present
	 * @param effects the effects predicate to match if present
	 * @param flags the entity flags predicate to match if present
	 * @param equipment the equipment predicate to match if present
	 * @param vehicle the vehicle predicate to match if present
	 * @param passenger the passenger predicate to match if present
	 * @param slots the slots predicate to match if present
	 */
	public record EntityPredicate(
			Optional<EntityTypePredicate> entityType,
			Optional<LightSourceLocationPredicate> located,
			Optional<MobEffectsPredicate> effects,
			Optional<EntityFlagsPredicate> flags,
			Optional<EntityEquipmentPredicate> equipment,
			Optional<EntityPredicate> vehicle,
			Optional<EntityPredicate> passenger,
			Optional<SlotsPredicate> slots
	) {
		public static final Codec<EntityPredicate> CODEC = Codec.recursive(
				"EntityPredicate",
				codec -> RecordCodecBuilder.create(
						instance -> instance.group(
										EntityTypePredicate.CODEC.optionalFieldOf("type").forGetter(EntityPredicate::entityType),
										LightSourceLocationPredicate.CODEC.optionalFieldOf("location").forGetter(EntityPredicate::located),
										MobEffectsPredicate.CODEC.optionalFieldOf("effects").forGetter(EntityPredicate::effects),
										EntityFlagsPredicate.CODEC.optionalFieldOf("flags").forGetter(EntityPredicate::flags),
										EntityEquipmentPredicate.CODEC.optionalFieldOf("equipment").forGetter(EntityPredicate::equipment),
										codec.optionalFieldOf("vehicle").forGetter(EntityPredicate::vehicle),
										codec.optionalFieldOf("passenger").forGetter(EntityPredicate::passenger),
										SlotsPredicate.CODEC.optionalFieldOf("slots").forGetter(EntityPredicate::slots)
								)
								.apply(instance, EntityPredicate::new)
				)
		);

		/**
		 * Tests the predicate with the given entity.
		 *
		 * @param entity the entity to test
		 * @return {@code true} if the entity matches this predicate, or {@code false} otherwise
		 */
		public boolean test(Entity entity) {
			if (entity == null) {
				return false;
			} else if (this.entityType.isPresent() && !this.entityType.get().matches(entity.getType())) {
				return false;
			} else if (this.located.isPresent() && !this.located.get().matches(entity.level(), entity.getX(), entity.getY(), entity.getZ())) {
				return false;
			} else if (this.effects.isPresent() && !this.effects.get().matches(entity)) {
				return false;
			} else if (this.flags.isPresent() && !this.flags.get().matches(entity)) {
				return false;
			} else if (this.equipment.isPresent() && !this.equipment.get().matches(entity)) {
				return false;
			} else if (this.vehicle.isPresent() && !this.vehicle.get().test(entity.getVehicle())) {
				return false;
			} else if (this.passenger.isPresent()
					&& entity.getPassengers().stream().noneMatch(passenger -> this.passenger.get().test(passenger))) {
				return false;
			} else {
				return this.slots.isEmpty() || this.slots.get().matches(entity);
			}
		}

		/**
		 * Creates a new builder instance.
		 *
		 * @return The builder instance.
		 * @since 4.1.0
		 */
		public static Builder builder() {
			return new Builder();
		}

		/**
		 * A builder for creating a new {@link EntityLightSource} instance.
		 *
		 * @since 4.1.0
		 */
		public static class Builder {
			Optional<EntityTypePredicate> entityType = Optional.empty();
			Optional<LightSourceLocationPredicate> located = Optional.empty();
			Optional<MobEffectsPredicate> effects = Optional.empty();
			Optional<EntityFlagsPredicate> flags = Optional.empty();
			Optional<EntityEquipmentPredicate> equipment = Optional.empty();
			Optional<EntityPredicate> vehicle = Optional.empty();
			Optional<EntityPredicate> passenger = Optional.empty();
			Optional<SlotsPredicate> slots = Optional.empty();

			public Builder of(EntityType<?> entityType) {
				this.entityType = Optional.of(EntityTypePredicate.of(entityType));
				return this;
			}

			public Builder of(EntityType<?>... entityTypes) {
				this.entityType = Optional.of(new EntityTypePredicate(HolderSet.direct(Arrays.stream(entityTypes).map(EntityType::builtInRegistryHolder).toList())));
				return this;
			}

			public Builder of(TagKey<EntityType<?>> tagKey) {
				this.entityType = Optional.of(EntityTypePredicate.of(tagKey));
				return this;
			}

			public Builder entityType(EntityTypePredicate entityTypePredicate) {
				this.entityType = Optional.of(entityTypePredicate);
				return this;
			}

			public Builder located(LightSourceLocationPredicate.Builder builder) {
				this.located = Optional.of(builder.build());
				return this;
			}

			public Builder effects(MobEffectsPredicate.Builder builder) {
				this.effects = builder.build();
				return this;
			}

			public Builder flags(EntityFlagsPredicate.Builder builder) {
				this.flags = Optional.of(builder.build());
				return this;
			}

			public Builder equipment(EntityEquipmentPredicate.Builder builder) {
				this.equipment = Optional.of(builder.build());
				return this;
			}

			public Builder equipment(EntityEquipmentPredicate entityEquipmentPredicate) {
				this.equipment = Optional.of(entityEquipmentPredicate);
				return this;
			}

			public Builder vehicle(Builder builder) {
				this.vehicle = Optional.of(builder.build());
				return this;
			}

			public Builder passenger(Builder builder) {
				this.passenger = Optional.of(builder.build());
				return this;
			}

			public Builder slots(SlotsPredicate slotsPredicate) {
				this.slots = Optional.of(slotsPredicate);
				return this;
			}

			public EntityPredicate build() {
				return new EntityPredicate(
						this.entityType,
						this.located,
						this.effects,
						this.flags,
						this.equipment,
						this.vehicle,
						this.passenger,
						this.slots
				);
			}
		}
	}
}
