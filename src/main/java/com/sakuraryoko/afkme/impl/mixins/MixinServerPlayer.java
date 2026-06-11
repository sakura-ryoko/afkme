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

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.sakuraryoko.afkme.impl.player.interfaces.IPlayerInvoker;

@Mixin(ServerPlayer.class)
@ApiStatus.Internal
public abstract class MixinServerPlayer extends Entity implements IPlayerInvoker
{
	//#if MC >= 26.1
	//$$ @Shadow @Final private MinecraftServer server;
	//#else
	@Shadow
	@Final
	public MinecraftServer server;
	//#endif
	@Shadow public ServerGamePacketListenerImpl connection;
	@Unique
	public ServerPlayer player = (ServerPlayer) (Object) this;

	public MixinServerPlayer(EntityType<?> entityType, Level level)
	{
		super(entityType, level);
	}

	@Override
	public ServerPlayer afkme$player()
	{
		return this.player;
	}

	@Override
	public MinecraftServer afkme$server()
	{
		return this.server;
	}

	@Override
	public ServerGamePacketListenerImpl afkme$connection()
	{
		return this.connection;
	}
}
