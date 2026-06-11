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

import java.util.Collection;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.level.GameType;

import com.sakuraryoko.corelib.api.events.IServerEventsDispatch;

@ApiStatus.Internal
public class ServerEventsHandler implements IServerEventsDispatch
{
	private static final ServerEventsHandler INSTANCE = new ServerEventsHandler();
	public static ServerEventsHandler getInstance() { return INSTANCE; }

	@Override
	public void onStarting(MinecraftServer server)
	{
		// TODO
	}

	@Override
	public void onStarted(MinecraftServer server)
	{
		// TODO
	}

	@Override
	public void onReloadComplete(MinecraftServer server, Collection<String> resources)
	{
		// TODO
	}

	@Override
	public void onDedicatedStarted(DedicatedServer server)
	{
		// TODO
	}

	@Override
	public void onIntegratedStarted(IntegratedServer server)
	{
		// TODO
	}

	@Override
	public void onOpenToLan(IntegratedServer server, GameType mode)
	{
		// TODO
	}

	@Override
	public void onDedicatedStopping(DedicatedServer server)
	{
		// TODO
	}

	@Override
	public void onStopping(MinecraftServer server)
	{
		// TODO
	}

	@Override
	public void onStopped(MinecraftServer server)
	{
		// TODO
	}
}
