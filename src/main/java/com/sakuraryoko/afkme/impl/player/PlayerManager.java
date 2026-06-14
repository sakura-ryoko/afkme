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
import java.util.UUID;
import javax.annotation.Nonnull;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.VisibleForTesting;

import com.mojang.authlib.GameProfile;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

import com.sakuraryoko.afkme.impl.AfkMe;
import com.sakuraryoko.afkme.impl.config.AfkMeConfigHandler;
import com.sakuraryoko.afkme.impl.config.ConfigWrap;
import com.sakuraryoko.afkme.impl.config.data.options.PlayerOptions;
import com.sakuraryoko.afkme.impl.player.shadow.ShadowServerPlayer;
import com.sakuraryoko.afkme.impl.player.state.*;
import com.sakuraryoko.corelib.impl.config.ConfigManager;

@ApiStatus.Internal
public class PlayerManager
{
	private static final PlayerManager INSTANCE = new PlayerManager();
	public static PlayerManager getInstance() { return INSTANCE; }

	private final List<PlayerEntry> players;

	@ApiStatus.Internal
	private PlayerManager()
	{
		this.players = new ArrayList<>();
	}

	@ApiStatus.Internal
	public void syncProfile(GameProfile profile)
	{
		if (profile == null) { return; }
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
		this.addOrUpdateProfile(profile, ShadowState.DEFAULT);
	}

	@ApiStatus.Internal
	public void syncFromConfig(@Nonnull PlayerOptions opt)
	{
		this.addOrUpdateProfile(ProfileWrap.profile(opt.uuid, opt.name), opt.state);
	}

	@ApiStatus.Internal
	private void addOrUpdateProfile(@Nonnull GameProfile profile, ShadowState state)
	{
		UUID uuid = ProfileWrap.id(profile);
		String name = ProfileWrap.name(profile);
		PosState pos = PosWrap.defaultPos();
		GameState game = GameWrap.defMode();
		boolean found = false;

		for (PlayerEntry entry : this.players)
		{
			if (entry.uuid().equals(uuid))
			{
				if (!entry.state().equals(state))
				{
					PlayerEntry newEntry = entry.updateState(state);
					this.players.remove(entry);
					this.players.add(newEntry);
				}

				found = true;
				break;
			}
		}

		if (!found)
		{
			this.players.add(
					new PlayerEntry(uuid, name, state, pos, game)
			);
		}

		if (ConfigWrap.mainOpt().debugMode)
		{
			AfkMe.LOGGER.warn("addOrUpdateProfile: player: ['{}'/{}] state: {}", ProfileWrap.name(profile), ProfileWrap.id(profile), state.toString());
		}
	}

	@ApiStatus.Internal
	private void checkOrUpdateFromConfig(@Nonnull GameProfile profile)
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

	@ApiStatus.Internal
	private void addConfig(@Nonnull GameProfile profile)
	{
		List<PlayerOptions> config = new ArrayList<>(ConfigWrap.players());
		UUID uuid = ProfileWrap.id(profile);
		boolean exists = false;

		for (PlayerOptions entry : config)
		{
			if (entry.uuid.equals(uuid))
			{
				exists = true;
				break;
			}
		}

		if (!exists)
		{
			PlayerOptions opt = PlayerOptions.fromProfile(profile, ShadowState.DEFAULT);

			for (PlayerEntry entry : this.players)
			{
				if (entry.matches(uuid))
				{
					opt.pos = entry.pos();
					opt.game = entry.game();
					break;
				}
			}

			ConfigWrap.players().add(opt);
		}

		if (ConfigWrap.mainOpt().debugMode)
		{
			AfkMe.LOGGER.warn("addConfig: player: ['{}'/{}]", ProfileWrap.name(profile), ProfileWrap.id(profile));
		}
	}

	@ApiStatus.Internal
	private void setConfig(@Nonnull GameProfile profile, ShadowState state)
	{
		List<PlayerOptions> config = new ArrayList<>(ConfigWrap.players());
		UUID uuid = ProfileWrap.id(profile);
		boolean dirty = false;

		for (PlayerOptions entry : config)
		{
			if (entry.uuid.equals(uuid) &&
				!entry.state.equals(state))
			{
				entry.state = state;
				entry.name = ProfileWrap.name(profile);

				for (PlayerEntry playerEntry : this.players)
				{
					if (playerEntry.matches(uuid))
					{
						entry.pos = playerEntry.pos();
						entry.game = playerEntry.game();
						break;
					}
				}

				dirty = true;
			}
		}

		if (dirty)
		{
			ConfigWrap.players().clear();

			for (PlayerOptions entry : config)
			{
				PlayerOptions opt = new PlayerOptions(entry);

				for (PlayerEntry playerEntry : this.players)
				{
					if (playerEntry.matches(uuid))
					{
						entry.pos = playerEntry.pos();
						entry.game = playerEntry.game();
						break;
					}
				}

				ConfigWrap.players().add(opt);
			}
		}
	}

	public ShadowState getShadowState(@Nonnull GameProfile profile)
	{
		UUID uuid = ProfileWrap.id(profile);

		for (PlayerEntry entry : this.players)
		{
			if (entry.matches(uuid))
			{
				return entry.state();
			}
		}

		this.addOrUpdateProfile(profile, ShadowState.DEFAULT);
		this.addConfig(profile);

		return ShadowState.DEFAULT;
	}

	@ApiStatus.Internal
	public ShadowState getShadowState(@Nonnull UUID uuid)
	{
		for (PlayerEntry entry : this.players)
		{
			if (entry.matches(uuid))
			{
				return entry.state();
			}
		}

		return ShadowState.DEFAULT;
	}

	@ApiStatus.Internal
	public void setShadowState(@Nonnull GameProfile profile, ShadowState state)
	{
		this.addOrUpdateProfile(profile, state);
		this.setConfig(profile, state);

		//  || this.getDebugStatus(profile)
		if (ConfigWrap.mainOpt().debugMode)
		{
			AfkMe.LOGGER.warn("setShadowState: player: ['{}'/{}] state: {}", ProfileWrap.name(profile), ProfileWrap.id(profile), state.toString());
		}
	}

	public void resetShadowState(@Nonnull ServerPlayer player)
	{
		this.setShadowState(player.getGameProfile(), ShadowState.DEFAULT);
	}

	public void remove(@Nonnull UUID uuid)
	{
		ShadowEntryList.getInstance().remove(uuid);
		this.players.removeIf(entry -> entry.matches(uuid));
		ConfigWrap.players().removeIf(opt -> opt.uuid.equals(uuid));
	}

	public PosState getPosState(@Nonnull UUID uuid)
	{
		for (PlayerEntry entry : this.players)
		{
			if (entry.matches(uuid))
			{
				return entry.pos();
			}
		}

		List<PlayerOptions> config = new ArrayList<>(ConfigWrap.players());

		for (PlayerOptions entry : config)
		{
			if (entry.uuid.equals(uuid))
			{
				return entry.pos;
			}
		}

		return PosWrap.defaultPos();
	}

	public GameState getGameMode(@Nonnull UUID uuid)
	{
		for (PlayerEntry entry : this.players)
		{
			if (entry.matches(uuid))
			{
				return entry.game();
			}
		}

		List<PlayerOptions> config = new ArrayList<>(ConfigWrap.players());

		for (PlayerOptions entry : config)
		{
			if (entry.uuid.equals(uuid))
			{
				return entry.game;
			}
		}

		return GameWrap.defMode();
	}

	@ApiStatus.Internal
	public void updatePlayerData(@Nonnull ServerPlayer player)
	{
		PosState pos = PosWrap.of(player);
		GameState game = GameWrap.of(player);
		UUID uuid = player.getUUID();

		for (PlayerEntry entry : this.players)
		{
			if (entry.matches(uuid))
			{
				PlayerEntry newEntry = entry.updatePlayerData(player.getName().getString(), pos, game);
				this.players.remove(entry);
				this.players.add(newEntry);
				break;
			}
		}
	}

	@VisibleForTesting
	public ImmutableMap<UUID, PlayerEntry> getPlayerMap()
	{
		ImmutableMap.Builder<UUID, PlayerEntry> map = ImmutableMap.builder();

		for (PlayerEntry entry : this.players)
		{
			map.put(entry.uuid(), entry);
		}

		return map.build();
	}

	@VisibleForTesting
	public Component getDebugFormatted(UUID uuid)
	{
		for (PlayerEntry entry : this.players)
		{
			if (entry.matches(uuid))
			{
				return entry.getDebugFormatted();
			}
		}

		return Component.literal("§cPlayer not found§r");
	}

	@ApiStatus.Internal
	private boolean syncConfig(@Nonnull MinecraftServer server, boolean stop)
	{
		PlayerList playerList = server.getPlayerList();
		List<ServerPlayer> players = playerList.getPlayers();
		boolean dirty = false;

		for (ServerPlayer player : players)
		{
			if (this.syncConfigEach(player))
			{
				dirty = true;
			}
		}

		if (stop) { return dirty; }

		// Spawn shadow configured players
		List<PlayerOptions> config = new ArrayList<>(ConfigWrap.players());

		for (PlayerOptions entry : config)
		{
			boolean found = false;

			for (ServerPlayer player : players)
			{
				if (entry.uuid.equals(player.getUUID()))
				{
					found = true;
					break;
				}
			}

			if (!found && entry.state.enabled())
			{
				if (ConfigWrap.mainOpt().debugMode)
				{
					AfkMe.LOGGER.warn("syncConfig: Scheduling Shadow player: ['{}'/{}]", entry.name, entry.uuid.toString());
				}

				PendingShadowSpawns.INSTANCE.scheduleSpawn(entry);
			}
		}

		return dirty;
	}

	@ApiStatus.Internal
	private boolean syncConfigEach(ServerPlayer player)
	{
		List<PlayerOptions> oldConfig = new ArrayList<>(ConfigWrap.players());
		List<PlayerOptions> newConfig = new ArrayList<>();
		String name = player.getName().getString();
		UUID uuid = player.getUUID();
		PosState pos = PosWrap.of(player);
		GameState game = GameWrap.of(player);
		ShadowState state = this.getShadowState(uuid).ensureValid();

		if (player instanceof ShadowServerPlayer shadow)
		{
			ShadowEntry entry = ShadowEntryList.getInstance().get(shadow);

			if (entry == null)
			{
				entry = ShadowEntryList.getInstance().add(shadow, state);
			}

			if (entry != null)
			{
				state = new ShadowState(true, state.time(), entry.shadowTimeout(), state.reason());
			}
			else
			{
				state = new ShadowState(true, state.time(), shadow.getTimeout(), state.reason());
			}
		}

		boolean found = false;
		boolean dirty = false;

		for (PlayerOptions entry : oldConfig)
		{
			if (entry.uuid.equals(uuid))
			{
				if (!entry.state.equals(state))
				{
					entry.state = state;
					dirty = true;
				}
				if (!entry.pos.equals(pos))
				{
					entry.pos = pos;
					dirty = true;
				}
				if (!entry.game.equals(game))
				{
					entry.game = game;
					dirty = true;
				}
				if (!entry.name.equals(name))
				{
					entry.name = name;
					dirty = true;
				}

				found = true;
			}

			newConfig.add(entry);
		}

		if (!found)
		{
			PlayerOptions opt = PlayerOptions.fromProfile(player.getGameProfile(), state);
			opt.pos = pos;
			opt.game = game;
			newConfig.add(opt);
			dirty = true;
		}

		if (dirty)
		{
			ConfigWrap.players().clear();

			for (PlayerOptions entry : newConfig)
			{
				PlayerOptions opt = new PlayerOptions(entry);
				ConfigWrap.players().add(opt);
			}

			return true;
		}

		return false;
	}

	@ApiStatus.Internal
	public void onServerStop(@Nonnull MinecraftServer server)
	{
		if (ConfigWrap.mainOpt().debugMode)
		{
			AfkMe.LOGGER.warn("onServerStop --> syncConfig()");
		}

		if (this.syncConfig(server, true))
		{
			if (ConfigWrap.mainOpt().debugMode)
			{
				AfkMe.LOGGER.warn("onServerStop(): flushing changes ...");
			}

			ConfigManager.getInstance().saveEach(AfkMeConfigHandler.getInstance());
		}
	}

	@ApiStatus.Internal
	public void onServerStarted(@Nonnull MinecraftServer server)
	{
		if (ConfigWrap.mainOpt().debugMode)
		{
			AfkMe.LOGGER.warn("onServerStarted --> syncConfig()");
		}

		if (this.syncConfig(server, false))
		{
			if (ConfigWrap.mainOpt().debugMode)
			{
				AfkMe.LOGGER.warn("onServerStarted(): flushing changes ...");
			}

			ConfigManager.getInstance().saveEach(AfkMeConfigHandler.getInstance());
		}
	}
}
