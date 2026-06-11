/*
 * This file is part of the AfkMe project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2026  Sakura-Ryoko and contributors
 *
 * AfkMe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AfkMe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with AfkMe.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sakuraryoko.afkme.impl.mixins;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.Entity;
//#if MC >= 1.19.4
//$$ import net.minecraft.world.entity.LivingEntity;
//#endif
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.sakuraryoko.afkme.impl.player.shadow.ShadowServerPlayer;

@Mixin(Entity.class)
@ApiStatus.Internal
public abstract class MixinEntity
{
	//#if MC >= 1.19.4
	//$$ @Shadow public abstract @Nullable LivingEntity getControllingPassenger();
	//#else
	@Shadow public abstract @Nullable Entity getControllingPassenger();
	//#endif
	//#if MC >= 1.20.1
	//$$ @Shadow private Level level;
	//#else
	@Shadow public Level level;
	//#endif

	//#if MC >= 1.21.5
	//$$ @Inject(method = "isLocalInstanceAuthoritative", at = @At("HEAD"), cancellable = true)
	//#else
	@Inject(method = "isControlledByLocalInstance", at = @At("HEAD"), cancellable = true)
	//#endif
	private void afkme$isControlledByLocalInstance(CallbackInfoReturnable<Boolean> cir)
	{
		if (this.getControllingPassenger() instanceof ShadowServerPlayer)
		{
			cir.setReturnValue(!this.level.isClientSide());
		}
	}
}
