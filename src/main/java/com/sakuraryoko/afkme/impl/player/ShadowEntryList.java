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
import javax.annotation.Nullable;

import net.minecraft.server.level.ServerPlayer;

import com.sakuraryoko.afkme.impl.AfkMe;
import com.sakuraryoko.afkme.impl.player.shadow.ShadowServerPlayer;

public class ShadowEntryList
{
	private static final ShadowEntryList INSTANCE = new ShadowEntryList();
	public static ShadowEntryList getInstance() { return INSTANCE; }
	private final List<ShadowEntry> list;

	private ShadowEntryList()
	{
		this.list = new ArrayList<>();
	}

	public @Nullable ShadowEntry get(@Nonnull ServerPlayer player)
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

	public @Nullable ShadowEntry add(@Nonnull ServerPlayer player)
	{
		if (this.get(player) == null)
		{
			ShadowEntry entry = ShadowEntry.create(player);
			this.list.add(entry);
			AfkMe.debugLog("ShadowEntryList(): add({}) --> ADD", entry.name().getString());
			return entry;
		}

		return this.get(player);
	}

	public boolean exists(@Nonnull ServerPlayer player)
	{
		for (ShadowEntry entry : this.list)
		{
			if (entry.matches(player))
			{
				return true;
			}
		}

		return false;
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

	public void remove(@Nonnull ServerPlayer player)
	{
		for (ShadowEntry entry : this.list)
		{
			if (entry.matches(player))
			{
				AfkMe.debugLog("ShadowEntryList(): remove({}) --> REMOVE", entry.name().getString());
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
				AfkMe.debugLog("ShadowEntryList(): remove({}) --> REMOVE", entry.name().getString());
				this.list.remove(entry);
				entry.handler().unregisterShadowAfk();
				break;
			}
		}
	}

	public List<ShadowEntry> listCopy()
	{
		return new ArrayList<>(this.list);
	}

	public void clear()
	{
		this.list.forEach(ShadowEntry::reset);
		this.list.clear();
	}
}
