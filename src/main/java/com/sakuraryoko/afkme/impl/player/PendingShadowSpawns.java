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

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.server.MinecraftServer;

import com.sakuraryoko.afkme.impl.config.data.options.PlayerOptions;
import com.sakuraryoko.afkme.impl.player.shadow.ShadowServerPlayer;

@ApiStatus.Internal
public class PendingShadowSpawns
{
	public static final PendingShadowSpawns INSTANCE = new PendingShadowSpawns();
	private final List<PlayerOptions> pendingSpawnsList;
	private long lastTick;
	private boolean locked;

	private PendingShadowSpawns()
	{
		this.pendingSpawnsList = new ArrayList<>();
		this.lastTick = System.currentTimeMillis();
		this.locked = false;
	}

	protected void scheduleSpawn(PlayerOptions opts)
	{
		this.pendingSpawnsList.add(opts);
	}

	private boolean shouldTick()
	{
		return !this.locked && !this.pendingSpawnsList.isEmpty();
	}

	private void executeOneSpawn(@Nonnull MinecraftServer server)
	{
		if (!this.pendingSpawnsList.isEmpty())
		{
			PlayerOptions opts = this.pendingSpawnsList.removeFirst();
			opts.state = opts.state.ensureValid();
			ShadowServerPlayer.createShadowFromConfig(server, opts);

			if (this.pendingSpawnsList.isEmpty())
			{
				this.locked = true;
			}
		}
	}

	public void tick(@Nonnull MinecraftServer server)
	{
		if (this.shouldTick())
		{
			final long now = System.currentTimeMillis();

			if ((now - this.lastTick) > 250L)
			{
				this.executeOneSpawn(server);
				this.lastTick = now;
			}
		}
	}
}
