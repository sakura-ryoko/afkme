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

package com.sakuraryoko.unplugged_afk.impl.config.data.options;

import com.sakuraryoko.corelib.api.config.IConfigOption;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class UnpluggedOptions implements IConfigOption
{
	public boolean resetHealthUponDeath;
	public int defaultShadowTimeout;
	public int unpluggedAfkCommandPermissions;
	public boolean unpluggedAfkDisableDamage;

	public UnpluggedOptions()
	{
		this.defaults();
	}

	@Override
	public void defaults()
	{
		this.resetHealthUponDeath = false;
		this.defaultShadowTimeout = 129600;
		this.unpluggedAfkCommandPermissions = 0;
		this.unpluggedAfkDisableDamage = false;
	}

	@Override
	public UnpluggedOptions copy(IConfigOption opt)
	{
		UnpluggedOptions opts = (UnpluggedOptions) opt;

		this.resetHealthUponDeath = opts.resetHealthUponDeath;
		this.defaultShadowTimeout = opts.defaultShadowTimeout;
		this.unpluggedAfkCommandPermissions = opts.unpluggedAfkCommandPermissions;
		this.unpluggedAfkDisableDamage = opts.unpluggedAfkDisableDamage;

		return this;
	}
}
