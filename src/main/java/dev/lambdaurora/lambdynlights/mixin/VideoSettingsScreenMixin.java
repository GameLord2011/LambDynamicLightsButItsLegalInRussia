/*
 * Copyright © 2020 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.lambdaurora.lambdynlights.gui.DynamicLightsOptionsOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VideoSettingsScreen.class)
public abstract class VideoSettingsScreenMixin extends OptionsSubScreen {
	@Unique
	private OptionInstance<?> lambdynlights$option;

	public VideoSettingsScreenMixin(Screen parent, Options gameOptions, Component title) {
		super(parent, gameOptions, title);
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void onConstruct(Screen parent, Minecraft client, Options gameOptions, CallbackInfo ci) {
		this.lambdynlights$option = DynamicLightsOptionsOption.getOption(this);
	}

	@WrapOperation(
			method = "addOptions",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/screens/options/VideoSettingsScreen;qualityOptions(Lnet/minecraft/client/Options;)[Lnet/minecraft/client/OptionInstance;"
			)
	)
	private OptionInstance<?>[] addOptionButton(Options options, Operation<OptionInstance<?>[]> original) {
		var old = original.call(options);
		var replaced = new OptionInstance<?>[old.length + 1];
		System.arraycopy(old, 0, replaced, 0, old.length);
		replaced[replaced.length - 1] = this.lambdynlights$option;
		return replaced;
	}
}
