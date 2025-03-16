/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.compat;

import com.mojang.logging.LogUtils;
import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Map;
import java.util.Optional;

/**
 * Represents the Curios compatibility layer.
 *
 * @author LambdAurora
 * @version 4.0.2
 * @since 4.0.2
 */
final class CuriosCompat implements CompatLayer {
	private static final MethodHandles.Lookup LOOKUP = MethodHandles.publicLookup();
	private static final Logger LOGGER = LogUtils.getLogger();

	// Still undecided between this approach with method handles (which is reasonably fast)
	// or a potentially faster approach using ASM and a hidden class, but potentially less maintainable.
	// (Not that method handles are much better for maintainability)
	private final MethodHandle curios$getCuriosInventory;
	private final MethodHandle curios$ICuriosItemHandler$getCurios;
	private final MethodHandle curios$ICurioStacksHandler$getStacks;
	private final MethodHandle curios$IDynamicStackHandler$getSlots;
	private final MethodHandle curios$IDynamicStackHandler$getStackInSlot;
	private boolean firstError = true;

	CuriosCompat() {
		Class<?> class$CuriosApi = getCuriosClass("CuriosApi");
		Class<?> class$ICuriosItemHandler = getCuriosClass("type.capability.ICuriosItemHandler");
		Class<?> class$ICurioStacksHandler = getCuriosClass("type.inventory.ICurioStacksHandler");
		Class<?> class$IDynamicStackHandler = getCuriosClass("type.inventory.IDynamicStackHandler");

		try {
			this.curios$getCuriosInventory = LOOKUP.findStatic(class$CuriosApi, "getCuriosInventory",
					MethodType.methodType(Optional.class, LivingEntity.class)
			);
			this.curios$ICuriosItemHandler$getCurios = LOOKUP.findVirtual(class$ICuriosItemHandler, "getCurios",
					MethodType.methodType(Map.class)
			);
			this.curios$ICurioStacksHandler$getStacks = LOOKUP.findVirtual(class$ICurioStacksHandler, "getStacks",
					MethodType.methodType(class$IDynamicStackHandler)
			);
			this.curios$IDynamicStackHandler$getSlots = LOOKUP.findVirtual(class$IDynamicStackHandler, "getSlots",
					MethodType.methodType(int.class)
			);
			this.curios$IDynamicStackHandler$getStackInSlot = LOOKUP.findVirtual(class$IDynamicStackHandler, "getStackInSlot",
					MethodType.methodType(ItemStack.class, int.class)
			);
		} catch (NoSuchMethodException | IllegalAccessException e) {
			throw new LinkageError("Cannot access Curios methods.", e);
		}
	}

	@Override
	public int getLivingEntityLuminanceFromItems(
			ItemLightSourceManager itemLightSources, LivingEntity entity, boolean submergedInWater
	) {
		int luminance = 0;

		try {
			var component = (Optional<?>) this.curios$getCuriosInventory.invokeExact(entity);

			if (component.isPresent()) {
				for (var stacksHandler : ((Map<?, ?>) this.curios$ICuriosItemHandler$getCurios.invoke(component.get())).values()) {
					var stacks = this.curios$ICurioStacksHandler$getStacks.invoke(stacksHandler);
					int slots = (int) this.curios$IDynamicStackHandler$getSlots.invoke(stacks);

					for (int i = 0; i < slots; i++) {
						luminance = Math.max(luminance, itemLightSources.getLuminance(
								(ItemStack) this.curios$IDynamicStackHandler$getStackInSlot.invoke(stacks, i), submergedInWater
						));

						if (luminance >= 15) {
							break;
						}
					}
				}
			}
		} catch (Throwable e) {
			if (this.firstError) {
				LambDynLights.error(LOGGER,
						"Could not get luminance from curios."
								+ " This error may repeat but won't be logged again to avoid clogging the log output.",
						e
				);
				this.firstError = false;
			}
		}

		return luminance;
	}

	private static Class<?> getCuriosClass(String name) {
		try {
			return Class.forName("top.theillusivec4.curios.api." + name);
		} catch (ClassNotFoundException e) {
			throw new LinkageError("Could not find curios class " + name + ".", e);
		}
	}
}
