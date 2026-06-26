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

package com.sakuraryoko.unplugged_afk.impl.player.shadow;

import io.netty.channel.embedded.EmbeddedChannel;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.network.Connection;
//#if MC >= 1.21.8
//$$ import io.netty.channel.ChannelFutureListener;
//$$ import net.minecraft.network.PacketListener;
//#endif
//#if MC >= 1.20.6
//$$ import net.minecraft.network.ProtocolInfo;
//#endif
//#if MC >= 1.20.2
//$$ import javax.annotation.Nullable;
//$$ import org.jspecify.annotations.NonNull;
//$$ import net.minecraft.network.PacketListener;
//$$ import net.minecraft.network.PacketSendListener;
//$$ import net.minecraft.network.protocol.Packet;
//#endif
import net.minecraft.network.protocol.PacketFlow;

@ApiStatus.Internal
public class ShadowConnection extends Connection
{
	public ShadowConnection(PacketFlow receiving)
	{
		super(receiving);
		((IShadowConnection) this).setChannel(new EmbeddedChannel());
	}

	//#if MC >= 1.21.8
	//$$ @Override
	//$$ public void send(@NonNull Packet<?> packet, @Nullable ChannelFutureListener futureListener, boolean bl)
	//$$ {
	//$$ }
	//#elseif MC >= 1.20.2
	//$$ @Override
	//$$ public void send(@NonNull Packet<?> packet, @Nullable PacketSendListener sendListener)
	//$$ {
	//$$ }
	//#else
	//#endif

	@Override
	public void setReadOnly()
	{
	}

	@Override
	public void handleDisconnection()
	{
	}

	//#if MC >= 1.20.6
	//$$ @Override
	//$$ public void setListenerForServerboundHandshake(@NonNull PacketListener packetListener)
	//$$ {
	//$$ }

	//$$ @Override
	//$$ public <T extends PacketListener> void setupInboundProtocol(@NonNull ProtocolInfo<T> protocolInfo, @NonNull T packetListener)
	//$$ {
	//$$ }
	//#elseif MC >= 1.20.2
	//$$ @Override
	//$$ public void setListener(@NonNull PacketListener packetListener)
	//$$ {
	//$$ }
	//#else
	//#endif
}
