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

package com.sakuraryoko.afkme.impl.config.data.options;

import java.util.UUID;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import com.mojang.authlib.GameProfile;

import com.sakuraryoko.afkme.impl.player.state.*;
import com.sakuraryoko.corelib.api.config.IConfigOption;

@ApiStatus.Internal
public class PlayerOptions implements IConfigOption
{
	public UUID uuid;
	public String name;
	public ShadowState state;
	public PosState pos;
	public GameState game;

	public PlayerOptions()
	{
		this.defaults();
	}

	public PlayerOptions(PlayerOptions other)
	{
		this.defaults();
		this.copy(other);
	}

	@Override
	public void defaults()
	{
		this.uuid = UUID.randomUUID();
		this.name = this.uuid.toString();
		this.state = ShadowState.DEFAULT;
		this.pos = PosWrap.defaultPos();
		this.game = GameWrap.defMode();
	}

	@Override
	public PlayerOptions copy(IConfigOption other)
	{
		PlayerOptions opts = (PlayerOptions) other;
		this.uuid = opts.uuid;
		this.name = opts.name;
		this.state = opts.state;
		this.pos = opts.pos;
		this.game = opts.game;

		// Fix stupid crashes from people editing the file
		if (this.state.enabled())
		{
			if (this.state.time() <= 1)
			{
				this.state = new ShadowState(true, 1, this.state.timeout(), this.state.reason());
			}
			if (this.state.timeout() <= 1L)
			{
				final long timeout = (this.state.time() * 60L) * 1000L;
				this.state = new ShadowState(true, this.state.time(), timeout, this.state.reason());
			}
		}

		return this;
	}

	@Override
	public boolean equals(Object o)
	{
		if (o instanceof PlayerOptions)
		{
			PlayerOptions opt = (PlayerOptions) o;

			// Only match the UUID
			return opt.uuid.equals(this.uuid);
		}

		return false;
	}

	public static PlayerOptions fromProfile(@NotNull GameProfile profile)
	{
		return fromProfile(profile, ShadowState.DEFAULT);
	}

	public static PlayerOptions fromProfile(@NotNull GameProfile profile, ShadowState state)
	{
		PlayerOptions opts = new PlayerOptions();
		opts.uuid = ProfileWrap.id(profile);
		opts.name = ProfileWrap.name(profile);
		opts.state = state;
		opts.pos = PosWrap.defaultPos();
		opts.game = GameWrap.defMode();
		return opts;
	}
}
