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
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

import com.mojang.authlib.GameProfile;

import com.sakuraryoko.afkme.impl.AfkMe;
import com.sakuraryoko.afkme.impl.config.ConfigWrap;
import com.sakuraryoko.afkme.impl.config.data.options.PlayerOptions;

public class PlayerManager
{
	private static final PlayerManager INSTANCE = new PlayerManager();
	public static PlayerManager getInstance() { return INSTANCE; }

	private final HashMap<UUID, ShadowState> playerMap;

	private PlayerManager()
	{
		this.playerMap = new HashMap<>();
	}

	public void syncProfile(GameProfile profile)
	{
		List<PlayerOptions> config = ConfigWrap.players();
		UUID uuid = ProfileWrap.id(profile);

		for (PlayerOptions opt : config)
		{
			if (opt.uuid.equals(uuid))
			{
				this.addOrUpdateProfile(profile, opt.state);
				return;
			}
		}

		// Doesn't exist in config --> Add
		this.addConfig(profile);
	}

	public void syncFromConfig(PlayerOptions opt)
	{
		this.addOrUpdateProfile(ProfileWrap.profile(opt.uuid, opt.name), opt.state);
	}

	private void addOrUpdateProfile(GameProfile profile, ShadowState state)
	{
		UUID uuid = ProfileWrap.id(profile);

		if (this.playerMap.containsKey(uuid) && this.playerMap.get(uuid) != state)
		{
			this.playerMap.remove(uuid);
			this.playerMap.put(uuid, state);
		}
		else if (!this.playerMap.containsKey(uuid))
		{
			this.playerMap.put(uuid, state);
			this.checkOrUpdateFromConfig(profile);
		}

//		this.debugMap.put(uuid, false);
//
		if (ConfigWrap.mainOpt().debugMode)
		{
			AfkMe.LOGGER.warn("addOrUpdateProfile: player: ['{}'/{}] state: {}", ProfileWrap.name(profile), ProfileWrap.id(profile), state.toString());
		}
	}

	private void checkOrUpdateFromConfig(GameProfile profile)
	{
		List<PlayerOptions> config = new ArrayList<>(ConfigWrap.players());
		UUID uuid = ProfileWrap.id(profile);
		ShadowState state = this.getShadowState(uuid);

		for (PlayerOptions opt : config)
		{
			if (opt.uuid.equals(uuid) && !opt.state.equals(state))
			{
				this.setShadowState(profile, state);
				return;
			}
		}
	}

	private void addConfig(GameProfile profile)
	{
		List<PlayerOptions> config = new ArrayList<>(ConfigWrap.players());
		boolean exists = false;

		for (PlayerOptions entry : config)
		{
			if (entry.uuid.equals(ProfileWrap.id(profile)))
			{
				exists = true;
			}
		}

		if (!exists)
		{
			ConfigWrap.players().add(PlayerOptions.fromProfile(profile, ShadowState.DEFAULT));
		}

		if (ConfigWrap.mainOpt().debugMode)
		{
			AfkMe.LOGGER.warn("addConfig: player: ['{}'/{}]", ProfileWrap.name(profile), ProfileWrap.id(profile));
		}
	}

	private void setConfig(GameProfile profile, ShadowState state)
	{
		List<PlayerOptions> config = new ArrayList<>(ConfigWrap.players());
		boolean dirty = false;

		for (PlayerOptions entry : config)
		{
			if (entry.uuid.equals(ProfileWrap.id(profile)) &&
				!entry.state.equals(state))
			{
				entry.state = state;
				dirty = true;
			}
		}

		if (dirty)
		{
			ConfigWrap.players().clear();

			for (PlayerOptions entry : config)
			{
				ConfigWrap.players().add(new PlayerOptions(entry));
			}
		}
	}

	public ShadowState getShadowState(@NotNull GameProfile profile)
	{
		UUID uuid = ProfileWrap.id(profile);

		if (this.playerMap.containsKey(uuid))
		{
			return this.playerMap.get(uuid);
		}

		this.addOrUpdateProfile(profile, ShadowState.DEFAULT);
		this.addConfig(profile);

		return ShadowState.DEFAULT;
	}

	public ShadowState getShadowState(@NotNull UUID uuid)
	{
		if (this.playerMap.containsKey(uuid))
		{
			return this.playerMap.get(uuid);
		}

		this.playerMap.put(uuid, ShadowState.DEFAULT);
		return ShadowState.DEFAULT;
	}

	public void setShadowState(@NotNull GameProfile profile, ShadowState state)
	{
		this.addOrUpdateProfile(profile, state);
		this.setConfig(profile, state);

		//  || this.getDebugStatus(profile)
		if (ConfigWrap.mainOpt().debugMode)
		{
			AfkMe.LOGGER.warn("setShadowState: player: ['{}'/{}] state: {}", ProfileWrap.name(profile), ProfileWrap.id(profile), state.toString());
		}
	}
}
