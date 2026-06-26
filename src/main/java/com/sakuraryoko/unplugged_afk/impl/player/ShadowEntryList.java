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

package com.sakuraryoko.unplugged_afk.impl.player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.VisibleForTesting;

import net.minecraft.network.chat.Component;

import com.sakuraryoko.unplugged_afk.impl.UnpluggedAfk;
import com.sakuraryoko.unplugged_afk.impl.player.shadow.ShadowServerPlayer;
import com.sakuraryoko.unplugged_afk.impl.player.state.ShadowState;

@ApiStatus.Internal
public class ShadowEntryList
{
	private static final ShadowEntryList INSTANCE = new ShadowEntryList();
	public static ShadowEntryList getInstance() { return INSTANCE; }
	private final List<ShadowEntry> list;

	private ShadowEntryList()
	{
		this.list = new ArrayList<>();
	}

	public @Nullable ShadowEntry get(@Nonnull ShadowServerPlayer player)
	{
		for (ShadowEntry entry : this.list)
		{
			if (entry.matches(player))
			{
				return entry;
			}
		}

		return null;
	}

	public @Nullable ShadowEntry add(@Nonnull ShadowServerPlayer player, ShadowState state)
	{
		if (this.get(player) == null)
		{
			ShadowEntry entry = ShadowEntry.create(player);

			if (state.enabled())
			{
				entry.updateShadowState(state);
			}

			this.list.add(entry);
			UnpluggedAfk.debugLog("ShadowEntryList(): add({}) --> ADD", entry.name().getString());
			return entry;
		}

		return this.get(player);
	}

	public void updateShadow(@Nonnull ShadowServerPlayer player)
	{
		for (ShadowEntry entry : this.list)
		{
			if (entry.matches(player))
			{
				entry.setShadowPlayer(player);
				break;
			}
		}
	}

	public void remove(@Nonnull UUID uuid)
	{
		for (ShadowEntry entry : this.list)
		{
			if (entry.matches(uuid))
			{
				this.list.remove(entry);
				entry.handler().unregisterShadowAfk();
				break;
			}
		}
	}

	public void remove(@Nonnull ShadowServerPlayer player)
	{
		for (ShadowEntry entry : this.list)
		{
			if (entry.matches(player))
			{
				UnpluggedAfk.debugLog("ShadowEntryList(): remove({}) --> REMOVE", entry.name().getString());
				this.list.remove(entry);
				entry.handler().unregisterShadowAfk();
				break;
			}
		}
	}

	@VisibleForTesting
	public ImmutableList<ShadowEntry> listCopy()
	{
		return ImmutableList.copyOf(this.list);
	}

	@VisibleForTesting
	public Component getDebugFormatted(UUID uuid)
	{
		for (ShadowEntry entry : this.list)
		{
			if (entry.matches(uuid))
			{
				return entry.getDebugFormatted();
			}
		}

		return Component.literal("§cShadow Player not found§r");
	}
}
