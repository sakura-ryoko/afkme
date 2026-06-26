/*
 * This file is part of the Unplugged-AFK project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2026  Sakura-Ryoko and contributors
 *
 * Unplugged-AFK is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Unplugged-AFK is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Unplugged-AFK.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sakuraryoko.unplugged_afk.impl.mixins;

import java.util.stream.Stream;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.sakuraryoko.unplugged_afk.impl.player.shadow.ShadowServerPlayer;

@Mixin(TickRateManager.class)
@ApiStatus.Internal
public abstract class MixinTickRateManager
{
	@Shadow public abstract boolean runsNormally();

	@ModifyReturnValue(method = "isEntityFrozen", at = @At("TAIL"))
	private boolean unplugged$checkIsShadowFrozen(boolean original,
												  @Local(argsOnly = true) Entity entity)
	{
		if (original) { return true; }
		if (this.runsNormally()) { return false; }

		Stream<Entity> passengers = entity.getPassengers().stream().flatMap(Entity::getSelfAndPassengers);

		return isNotFake(entity) && passengers
				.noneMatch(MixinTickRateManager::isNotFake);
	}

	@Unique
	private static boolean isNotFake(Entity e)
	{
		return e instanceof Player && !(e instanceof ShadowServerPlayer);
	}
}
