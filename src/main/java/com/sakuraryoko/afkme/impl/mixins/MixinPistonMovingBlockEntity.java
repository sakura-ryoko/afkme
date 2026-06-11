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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.sakuraryoko.afkme.impl.player.shadow.ShadowServerPlayer;

@Mixin(PistonMovingBlockEntity.class)
@ApiStatus.Internal
public abstract class MixinPistonMovingBlockEntity
{
	@WrapOperation(method = "moveCollidedEntities",
	               at = @At(value = "INVOKE",
	                        target = "Lnet/minecraft/world/entity/Entity;getPistonPushReaction()Lnet/minecraft/world/level/material/PushReaction;"))
	private static PushReaction afkme$moveShadowPlayer(Entity instance, Operation<PushReaction> original,
	                                                     @Local(argsOnly = true) PistonMovingBlockEntity piston)
	{
		if (instance instanceof ShadowServerPlayer && piston.getMovedState().is(Blocks.SLIME_BLOCK))
		{
			final Vec3 vec3 = instance.getDeltaMovement();
			double x = vec3.x();
			double y = vec3.y();
			double z = vec3.z();
			Direction direction = piston.getMovementDirection();

			switch (direction.getAxis())
			{
				case X -> x = direction.getStepX();
				case Y -> y = direction.getStepY();
				case Z -> z = direction.getStepZ();
			}

			instance.setDeltaMovement(x, y, z);
		}

		return original.call(instance);
	}
}
