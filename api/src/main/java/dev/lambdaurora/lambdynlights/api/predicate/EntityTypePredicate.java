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
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

/**
 * Represents an equivalent to 1.21's EntityTypePredicate.
 * <p>
 * <b>Important Note:</b> In Minecraft 1.21 this is replaced by {@link net.minecraft.advancements.critereon.EntityTypePredicate}.
 *
 * @param types the types to match
 */
@SuppressWarnings("deprecation")
public record EntityTypePredicate(HolderSet<EntityType<?>> types) {
	public static final Codec<EntityTypePredicate> CODEC = RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE)
			.xmap(EntityTypePredicate::new, EntityTypePredicate::types);

	public static EntityTypePredicate of(EntityType<?> type) {
		return new EntityTypePredicate(HolderSet.direct(type.builtInRegistryHolder()));
	}

	public static EntityTypePredicate of(TagKey<EntityType<?>> tag) {
		return new EntityTypePredicate(BuiltInRegistries.ENTITY_TYPE.getOrCreateTag(tag));
	}

	public boolean matches(EntityType<?> type) {
		return this.types.contains(type.builtInRegistryHolder());
	}
}
