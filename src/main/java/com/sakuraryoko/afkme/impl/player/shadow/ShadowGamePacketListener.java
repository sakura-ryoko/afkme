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

package com.sakuraryoko.afkme.impl.player.shadow;

import java.util.Set;
import org.jspecify.annotations.NonNull;

import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.Packet;
//#if MC >= 1.20.2
//$$ import net.minecraft.server.network.CommonListenerCookie;
//#endif
//#if MC >= 1.19.4
//$$ import net.minecraft.world.entity.RelativeMovement;
//#else
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
//#endif
//#if MC >= 1.21.2
//$$ import net.minecraft.world.entity.PositionMoveRotation;
//$$ import net.minecraft.world.entity.Relative;
//#endif
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class ShadowGamePacketListener extends ServerGamePacketListenerImpl
{
	//#if MC >= 1.20.2
	//$$ public ShadowGamePacketListener(MinecraftServer server, Connection connection, ServerPlayer player, CommonListenerCookie cookie)
	//$$ {
		//$$ super(server, connection, player, cookie);
	//$$ }
	//#else
	public ShadowGamePacketListener(MinecraftServer server, Connection connection, ServerPlayer player)
	{
		super(server, connection, player);
	}
	//#endif

	@Override
	public void send(@NonNull Packet<?> packet)
	{
	}

	@Override
	public void disconnect(@NonNull Component message)
	{
		if (message.getContents() instanceof TranslatableContents text &&
			(text.getKey().equals("multiplayer.disconnect.idling") ||
			 text.getKey().equals("multiplayer.disconnect.duplicate_login")))
		{
			((ShadowServerPlayer) this.player).kill(message);
		}
	}

	//#if MC >= 1.21.2
	//$$ @Override
	//$$ public void teleport(@NonNull PositionMoveRotation position, @NonNull Set<Relative> relativeSet)
	//$$ {
		//$$ super.teleport(position, relativeSet);
	//#elseif MC >= 1.19.4
	//$$ @Override
	//$$ public void teleport(double x, double y, double z, float yaw, float pitch, @NonNull Set<RelativeMovement> relativeSet)
	//$$ {
		//$$ super.teleport(x, y, z, yaw, pitch, relativeSet);
	//#else
	@Override
	public void teleport(double x, double y, double z, float yaw, float pitch, @NonNull Set<ClientboundPlayerPositionPacket.RelativeArgument> relativeSet, boolean dismountVehicle)
	{
		super.teleport(x, y, z, yaw, pitch, relativeSet, dismountVehicle);
	//#endif

		//#if MC >= 1.21.8
		//$$ if (this.player.level().getPlayerByUUID(this.player.getUUID()) != null)
		//$$ {
			//$$ this.resetPosition();
			//$$ this.player.level().getChunkSource().move(this.player);
		//$$ }
		//#elseif MC >= 1.20.1
		//$$ if (this.player.serverLevel().getPlayerByUUID(this.player.getUUID()) != null)
		//$$ {
			//$$ this.resetPosition();
			//$$ this.player.serverLevel().getChunkSource().move(this.player);
		//$$ }
		//#else
		if (this.player.getLevel().getPlayerByUUID(this.player.getUUID()) != null)
		{
			this.resetPosition();
			this.player.getLevel().getChunkSource().move(this.player);
		}
		//#endif
	}
}
