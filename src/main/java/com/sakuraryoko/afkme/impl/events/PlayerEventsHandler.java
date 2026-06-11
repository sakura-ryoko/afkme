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

package com.sakuraryoko.afkme.impl.events;

import java.net.SocketAddress;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import com.sakuraryoko.afkme.impl.player.PlayerManager;
import com.sakuraryoko.afkme.impl.player.ShadowEntryList;
import com.sakuraryoko.afkme.impl.player.shadow.ShadowServerPlayer;
import com.sakuraryoko.corelib.api.events.IPlayerEventsDispatch;

@ApiStatus.Internal
public class PlayerEventsHandler implements IPlayerEventsDispatch
{
	private static final PlayerEventsHandler INSTANCE = new PlayerEventsHandler();
	public static PlayerEventsHandler getInstance() { return INSTANCE; }

	@Override
	public void onConnection(SocketAddress addr, GameProfile profile, @Nullable Component result)
	{
		// TODO
	}

	@Override
	public void onCreatePlayer(ServerPlayer player, @Nullable GameProfile profile)
	{
		if (player instanceof ShadowServerPlayer) { return; }
		PlayerManager.getInstance().syncProfile(profile);

		if (!ShadowEntryList.getInstance().exists(player))
		{
			ShadowEntryList.getInstance().add(player);
		}
	}

	@Override
	public void onPlayerJoinPre(ServerPlayer player, Connection connection)
	{
		// TODO
	}

	@Override
	public void onPlayerJoinPost(ServerPlayer player, Connection connection)
	{
		if (player instanceof ShadowServerPlayer) { return; }

		if (!ShadowEntryList.getInstance().exists(player))
		{
			ShadowEntryList.getInstance().add(player);
		}
	}

	@Override
	public void onPlayerRespawn(ServerPlayer newPlayer)
	{
		if (newPlayer instanceof ShadowServerPlayer) { return; }
		PlayerManager.getInstance().syncProfile(newPlayer.getGameProfile());

		if (!ShadowEntryList.getInstance().exists(newPlayer))
		{
			ShadowEntryList.getInstance().add(newPlayer);
		}
	}

	@Override
	public void onPlayerLeave(ServerPlayer player)
	{
		if (player instanceof ShadowServerPlayer) { return; }
		PlayerManager.getInstance().syncProfile(player.getGameProfile());
		ShadowEntryList.getInstance().remove(player);
	}

	@Override
	public void onDisconnectAll()
	{
		// TODO
	}

	@Override
	public void onSetViewDistance(int distance)
	{
		// TODO
	}

	@Override
	public void onSetSimulationDistance(int distance)
	{
		// TODO
	}
}
