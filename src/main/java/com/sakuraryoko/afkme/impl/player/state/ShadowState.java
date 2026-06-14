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

package com.sakuraryoko.afkme.impl.player.state;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import com.sakuraryoko.afkme.impl.modinit.InitWrap;

@ApiStatus.Internal
public record ShadowState(boolean enabled, int time, long timeout, String reason)
{
	public static final ShadowState DEFAULT = new ShadowState(false, 129600, -1L, "");

	@Override
	public boolean equals(Object o)
	{
		if (o == this)
		{
			return true;
		}
		if (o == null || !(o instanceof ShadowState))
		{
			return false;
		}

		ShadowState s = (ShadowState) o;
		return this.enabled == s.enabled && this.timeout == s.timeout;
	}

	@Override
	public int hashCode()
	{
		int hash = 7;
		hash = 97 * hash + (this.enabled ? 1 : 0);
		hash = 97 * hash + Long.hashCode(this.time);
		hash = 97 * hash + Long.hashCode(this.timeout);
		hash = 97 * hash + (this.reason != null ? this.reason.hashCode() : 0);
		return hash;
	}

	@Override
	public @NonNull String toString()
	{
		return "ShadowState{" + "enabled=" + this.enabled + ", time=" + this.time + ", timeout=" + this.timeout + ", reason=" + this.reason + '}';
	}

	public Component getDebugFormatted()
	{
		MutableComponent text = Component.literal("");

		text.append(
				InitWrap.text().formatText("§rEN: ")
		).append(
				InitWrap.text().formatText(this.enabled ? "§6Y§r" : "§aN§r")
		).append(
				InitWrap.text().formatText("§r / HT: ")
		).append(
				InitWrap.text().formatText(String.format("§e%d§r", this.time))
		).append(
				InitWrap.text().formatText("§r / TO: ")
		).append(
				InitWrap.text().formatText(String.format("§e%d§r", this.timeout))
		).append(
				InitWrap.text().formatText("§r / R: §e")
		).append(
				InitWrap.text().formatText(this.reason.isEmpty() ? "<EMPTY>" : this.reason)
		).append(
				InitWrap.text().formatText("§r")
		);

		return text;
	}

	// Fix stupid crashes from people editing the file
	public ShadowState ensureValid()
	{
		if (this.enabled)
		{
			int time = this.time;
			long timeout = this.timeout;

			if (time <= 0)
			{
				time = 5;
			}
			if (timeout <= 0)
			{
				timeout = (time * 60L) * 1000L;
			}

			return new ShadowState(true, time, timeout, this.reason);
		}

		return this;
	}
}
