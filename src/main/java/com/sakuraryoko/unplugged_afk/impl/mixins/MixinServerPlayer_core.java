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

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;
//#if MC >= 1.20.2
//$$ import net.minecraft.server.level.ClientInformation;
//#else
import net.minecraft.world.entity.player.ProfilePublicKey;
//#endif
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.sakuraryoko.unplugged_afk.impl.events.PlayerEventsHandler;
import com.sakuraryoko.unplugged_afk.impl.player.interfaces.IPlayerInvoker;

@Mixin(ServerPlayer.class)
@ApiStatus.Internal
public abstract class MixinServerPlayer_core extends Player implements IPlayerInvoker
{
	//#if MC >= 1.21.10
	//$$ @Shadow @Final private MinecraftServer server;
	//#else
	@Shadow @Final public MinecraftServer server;
	//#endif
	@Shadow public ServerGamePacketListenerImpl connection;
	@Unique public ServerPlayer player = (ServerPlayer) (Object) this;

	//#if MC >= 1.21.8
	//$$ public MixinServerPlayer_core(MinecraftServer server, Level level, GameProfile gameProfile, ClientInformation ci)
	//$$ {
		//$$ super(level, gameProfile);
	//$$ }
	//#elseif MC >= 1.20.2
	//$$ public MixinServerPlayer_core(Level level, BlockPos pos, float yRot, GameProfile gameProfile, ClientInformation ci)
	//$$ {
		//$$ super(level, pos, yRot, gameProfile);
	//$$ }
	//#elseif MC >= 1.19.3
	//$$ public MixinServerPlayer_core(Level level, BlockPos pos, float yRot, GameProfile gameProfile)
	//$$ {
		//$$ super(level, pos, yRot, gameProfile);
	//$$ }
	//#else
	public MixinServerPlayer_core(Level level, BlockPos pos, float yRot, GameProfile gameProfile, @Nullable ProfilePublicKey profilePublicKey)
	{
		super(level, pos, yRot, gameProfile, profilePublicKey);
	}
	//#endif

	@Override
	public ServerPlayer unplugged$player()
	{
		return this.player;
	}

	@Override
	public MinecraftServer unplugged$server()
	{
		return this.server;
	}

	@Override
	public ServerGamePacketListenerImpl unplugged$connection()
	{
		return this.connection;
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void unplugged$onPlayerTick(CallbackInfo ci)
	{
		PlayerEventsHandler.getInstance().onTick((ServerPlayer) (Object) this);
	}
}
