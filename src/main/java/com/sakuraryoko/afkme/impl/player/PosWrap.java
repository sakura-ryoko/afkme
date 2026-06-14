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

package com.sakuraryoko.afkme.impl.player;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class PosWrap
{
	public static PosState defaultPos()
	{
		return new PosState(Level.OVERWORLD.location().toString(), 0, 0, 0, 0f, 0f);
	}

	public static PosState of(@Nonnull ServerPlayer player)
	{
		//#if MC >= 1.20.1
		//$$ ResourceKey<Level> key = player.level().dimension();
		//#else
		ResourceKey<Level> key = player.level.dimension();
		//#endif
		BlockPos pos = player.blockPosition();

		return new PosState(key.location().toString(), pos.getX(), pos.getY(), pos.getZ(), player.getYRot(), player.getXRot());
	}
}
