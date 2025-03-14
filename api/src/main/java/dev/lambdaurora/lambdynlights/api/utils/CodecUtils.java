/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.api.utils;

import com.google.common.base.Suppliers;
import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Some codec utilities to overcome the limitations of DFU 6.0.8.
 * <p>
 * <b>Important Note:</b> In Minecraft 1.21 this is replaced by direct implementations within DFU and the game.
 */
public final class CodecUtils {
	public static final Codec<MinMaxBounds.Ints> MIN_MAX_INT_CODEC = createMinMaxCodec(Codec.INT,
			BoundsFactory.fromOld(
					MinMaxBounds.Ints::between,
					MinMaxBounds.Ints::atLeast,
					MinMaxBounds.Ints::atMost,
					MinMaxBounds.Ints.ANY
			)
	);
	public static final Codec<MinMaxBounds.Doubles> MIN_MAX_DOUBLE_CODEC = createMinMaxCodec(Codec.DOUBLE,
			BoundsFactory.fromOld(
					MinMaxBounds.Doubles::between,
					MinMaxBounds.Doubles::atLeast,
					MinMaxBounds.Doubles::atMost,
					MinMaxBounds.Doubles.ANY
			)
	);

	public static final Codec<EnchantmentPredicate[]> ENCHANTMENTS_PREDICATE_CODEC = ExtraCodecs.JSON.flatXmap(
			json -> {
				try {
					return DataResult.success(EnchantmentPredicate.fromJsonArray(json));
				} catch (JsonParseException exception) {
					return DataResult.error(exception::getMessage);
				}
			},
			predicate -> {
				var jsonArray = new JsonArray();

				for (var enchantmentPredicate : predicate) {
					jsonArray.add(enchantmentPredicate.serializeToJson());
				}

				return DataResult.success(jsonArray);
			}
	);
	public static final Codec<NbtPredicate> NBT_PREDICATE_CODEC = ExtraCodecs.JSON.flatXmap(
			json -> {
				try {
					return DataResult.success(NbtPredicate.fromJson(json));
				} catch (JsonParseException exception) {
					return DataResult.error(exception::getMessage);
				}
			},
			predicate -> {
				return DataResult.success(predicate.serializeToJson());
			}
	);

	public static final Codec<MobEffectsPredicate> MOB_EFFECTS_PREDICATE_CODEC = ExtraCodecs.JSON.flatXmap(
			json -> {
				try {
					return DataResult.success(MobEffectsPredicate.fromJson(json));
				} catch (JsonParseException exception) {
					return DataResult.error(exception::getMessage);
				}
			},
			predicate -> {
				return DataResult.success(predicate.serializeToJson());
			}
	);
	public static final Codec<EntityFlagsPredicate> ENTITY_FLAGS_PREDICATE_CODEC = ExtraCodecs.JSON.flatXmap(
			json -> {
				try {
					return DataResult.success(EntityFlagsPredicate.fromJson(json));
				} catch (JsonParseException exception) {
					return DataResult.error(exception::getMessage);
				}
			},
			predicate -> {
				return DataResult.success(predicate.serializeToJson());
			}
	);

	public static <T> Codec<T> withAlternative(final Codec<T> primary, final Codec<? extends T> alternative) {
		return Codec.either(
				primary,
				alternative
		).xmap(
				either -> either.map(Function.identity(), Function.identity()),
				Either::left
		);
	}

	public static <T> Codec<T> recursive(String name, Function<Codec<T>, Codec<T>> wrapped) {
		return new RecursiveCodec<T>(name, wrapped);
	}

	public static <E> Codec<E> identifierResolver(final Function<E, Identifier> toId, final Function<Identifier, E> fromId) {
		return Identifier.CODEC.flatXmap(
				name -> Optional.ofNullable(fromId.apply(name))
						.map(DataResult::success)
						.orElseGet(() -> DataResult.error(() -> "Unknown element name:" + name)),
				e -> Optional.ofNullable(toId.apply(e))
						.map(DataResult::success)
						.orElseGet(() -> DataResult.error(() -> "Element with unknown name: " + e))
		);
	}

	@SuppressWarnings("unchecked")
	private static <T extends Number, R extends MinMaxBounds<T>> Codec<R> createMinMaxCodec(Codec<T> codec, BoundsFactory<T, R> boundsFactory) {
		Codec<R> fullCodec = RecordCodecBuilder.create(
				instance -> instance.group(
						codec.optionalFieldOf("min").forGetter(bounds -> Optional.ofNullable(bounds.getMin())),
						codec.optionalFieldOf("max").forGetter(bounds -> Optional.ofNullable(bounds.getMax()))
				).apply(instance, boundsFactory::create)
		);
		return Codec.either(fullCodec, codec)
				.xmap(either -> either.map(
						Function.identity(),
						number -> boundsFactory.create(Optional.of(number), Optional.of(number))
				), bounds -> {
					var min = Optional.ofNullable(bounds.getMin());
					var max = Optional.ofNullable(bounds.getMax());
					var point = min.equals(max) ? min : Optional.empty();
					return point.<Either<R, T>>map(o -> Either.right((T) o)).orElseGet(() -> Either.left(bounds));
				});
	}

	@FunctionalInterface
	private interface BoundsFactory<T extends Number, R extends MinMaxBounds<T>> {
		R create(Optional<T> min, Optional<T> max);

		static <T extends Number, R extends MinMaxBounds<T>> BoundsFactory<T, R> fromOld(
				BiFunction<T, T, R> betweenFunc,
				Function<T, R> atLeast,
				Function<T, R> atMost,
				R any
		) {
			return (min, max) -> {
				if (min.isPresent() && max.isPresent()) {
					return betweenFunc.apply(min.get(), max.get());
				} else if (min.isPresent()) {
					return atLeast.apply(min.get());
				} else if (max.isPresent()) {
					return atMost.apply(max.get());
				} else {
					return any;
				}
			};
		}
	}

	public static class RecursiveCodec<T> implements Codec<T> {
		private final String name;
		private final Supplier<Codec<T>> wrapped;

		private RecursiveCodec(String name, Function<Codec<T>, Codec<T>> wrapped) {
			this.name = name;
			this.wrapped = Suppliers.memoize(() -> wrapped.apply(this));
		}

		public <S> DataResult<Pair<T, S>> decode(DynamicOps<S> ops, S input) {
			return this.wrapped.get().decode(ops, input);
		}

		public <S> DataResult<S> encode(T input, DynamicOps<S> ops, S prefix) {
			return this.wrapped.get().encode(input, ops, prefix);
		}

		public String toString() {
			return "RecursiveCodec[" + this.name + "]";
		}
	}
}
