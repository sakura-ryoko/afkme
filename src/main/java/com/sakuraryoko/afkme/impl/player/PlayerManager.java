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
import javax.annotation.Nonnull;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import com.mojang.authlib.GameProfile;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.GameType;

import com.sakuraryoko.afkme.impl.AfkMe;
import com.sakuraryoko.afkme.impl.config.ConfigWrap;
import com.sakuraryoko.afkme.impl.config.data.options.PlayerOptions;
import com.sakuraryoko.afkme.impl.modinit.InitWrap;
import com.sakuraryoko.afkme.impl.player.shadow.ShadowServerPlayer;

public class PlayerManager
{
	private static final PlayerManager INSTANCE = new PlayerManager();
	public static PlayerManager getInstance() { return INSTANCE; }

	private final HashMap<UUID, ShadowState> playerMap;
	private final HashMap<UUID, PosState> posMap;
	private final HashMap<UUID, GameState> gameMap;

	private PlayerManager()
	{
		this.playerMap = new HashMap<>();
		this.posMap = new HashMap<>();
		this.gameMap = new HashMap<>();
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
		this.addOrUpdateProfile(profile, ShadowState.DEFAULT);
	}

	public void syncFromConfig(PlayerOptions opt)
	{
		this.addOrUpdateProfile(ProfileWrap.profile(opt.uuid, opt.name), opt.state);
	}

	private void addOrUpdateProfile(GameProfile profile, ShadowState state)
	{
		UUID uuid = ProfileWrap.id(profile);

		this.posMap.putIfAbsent(uuid, PosWrap.defaultPos());
		this.gameMap.putIfAbsent(uuid, GameWrap.defMode());

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
		UUID uuid = ProfileWrap.id(profile);
		boolean exists = false;

		for (PlayerOptions entry : config)
		{
			if (entry.uuid.equals(uuid))
			{
				exists = true;
			}
		}

		if (!exists)
		{
			PlayerOptions opt = PlayerOptions.fromProfile(profile, ShadowState.DEFAULT);

			if (this.posMap.containsKey(uuid))
			{
				opt.pos = this.posMap.get(uuid);
			}

			if (this.gameMap.containsKey(uuid))
			{
				opt.game = this.gameMap.get(uuid);
			}

			ConfigWrap.players().add(opt);
		}

		if (ConfigWrap.mainOpt().debugMode)
		{
			AfkMe.LOGGER.warn("addConfig: player: ['{}'/{}]", ProfileWrap.name(profile), ProfileWrap.id(profile));
		}
	}

	private void setConfig(GameProfile profile, ShadowState state)
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

				if (this.posMap.containsKey(uuid))
				{
					entry.pos = this.posMap.get(uuid);
				}

				if (this.gameMap.containsKey(uuid))
				{
					entry.game = this.gameMap.get(uuid);
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

				if (this.posMap.containsKey(uuid))
				{
					opt.pos = this.posMap.get(uuid);
				}

				if (this.gameMap.containsKey(uuid))
				{
					opt.game = this.gameMap.get(uuid);
				}

				ConfigWrap.players().add(opt);
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

	public PosState getPosState(@NotNull UUID uuid)
	{
		if (this.posMap.containsKey(uuid))
		{
			return this.posMap.get(uuid);
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

	public GameState getGameMode(@NotNull UUID uuid)
	{
		if (this.gameMap.containsKey(uuid))
		{
			return this.gameMap.get(uuid);
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

	public void updatePlayerPosition(@Nonnull ServerPlayer player)
	{
		PosState pos = PosWrap.of(player);
		GameState game = GameWrap.of(player);
		UUID uuid = player.getUUID();
		this.posMap.put(uuid, pos);
		this.gameMap.put(uuid, game);
	}

	@VisibleForTesting
	public ImmutableMap<UUID, ShadowState> getPlayerMap()
	{
		return ImmutableMap.copyOf(this.playerMap);
	}

	@VisibleForTesting
	public Component getDebugFormatted(UUID uuid)
	{
		ShadowState state = this.playerMap.getOrDefault(uuid, ShadowState.DEFAULT);
		PosState pos = this.posMap.getOrDefault(uuid, PosWrap.defaultPos());
		GameState game = this.gameMap.getOrDefault(uuid, GameWrap.defMode());
		MutableComponent text = Component.literal("");

		text.append(
				InitWrap.text().formatText("\n - §7UUID: ")
		).append(
				InitWrap.text().formatText(uuid.toString())
		).append(
				InitWrap.text().formatText("\n - §7Shadow: ")
		).append(
				state.getDebugFormatted()
		).append(
				InitWrap.text().formatText("\n - §7Game: ")
		).append(
				game.getDebugFormatted()
		).append(
				InitWrap.text().formatText("\n - §7Position: ")
		).append(
				InitWrap.text().formatText(
						String.format("§b%s §f[%d, %d, %d]§r", pos.location(), pos.x(), pos.y(), pos.z())
				)
		);

		return text;
	}

	public void onServerStop(@Nonnull MinecraftServer server)
	{
		PlayerList playerList = server.getPlayerList();
		List<ServerPlayer> players = playerList.getPlayers();

		for (ServerPlayer player : players)
		{
			String name = player.getName().getString();
			UUID uuid = player.getUUID();
			ShadowState state = this.playerMap.getOrDefault(uuid, ShadowState.DEFAULT);
			PosState pos = PosWrap.of(player);
			GameState game = GameWrap.of(player);

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

			List<PlayerOptions> config = new ArrayList<>(ConfigWrap.players());
			boolean found = false;
			boolean dirty = false;

			for (PlayerOptions entry : config)
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
			}

			if (!found)
			{
				PlayerOptions opt = PlayerOptions.fromProfile(player.getGameProfile(), state);
				opt.pos = pos;
				opt.game = game;
				config.add(opt);
				dirty = true;
			}

			if (dirty)
			{
				// FIXME
				ConfigWrap.players().clear();

				for (PlayerOptions entry : config)
				{
					PlayerOptions opt = new PlayerOptions(entry);
					ConfigWrap.players().add(opt);
				}
			}
		}
	}

	public void onServerStarted(@Nonnull MinecraftServer server)
	{
		List<PlayerOptions> config = new ArrayList<>(ConfigWrap.players());
		PlayerList playerList = server.getPlayerList();
		List<ServerPlayer> players = playerList.getPlayers();

		for (PlayerOptions entry : config)
		{
			if (entry != null)
			{
				UUID uuid = entry.uuid;
				ShadowState state = entry.state;

				if (state.enabled())
				{
					boolean exists = false;

					for (ServerPlayer player : players)
					{
						if (player.getUUID().equals(uuid))
						{
							exists = true;
							break;
						}
					}

					if (!exists)
					{
						ShadowServerPlayer.createShadowFromConfig(server, entry);
					}
				}
			}
		}
	}
}
