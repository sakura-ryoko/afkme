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

import org.jspecify.annotations.NonNull;

public record ShadowState(boolean enabled, long timeout, String reason)
{
	public static final ShadowState DEFAULT = new ShadowState(false, -1L, "");

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
		hash = 97 * hash + Long.hashCode(this.timeout);
		hash = 97 * hash + (this.reason != null ? this.reason.hashCode() : 0);
		return hash;
	}

	@Override
	public @NonNull String toString()
	{
		return "ShadowState{" + "enabled=" + this.enabled + ", timeout=" + this.timeout + ", reason=" + this.reason + '}';
	}
}
