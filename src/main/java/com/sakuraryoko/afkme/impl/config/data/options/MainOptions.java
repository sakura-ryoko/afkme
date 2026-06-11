/*
 * This file is part of the AfkPlus project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2026  Sakura Ryoko and contributors
 *
 * AfkPlus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AfkPlus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with AfkPlus.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sakuraryoko.afkme.impl.config.data.options;

import org.jetbrains.annotations.ApiStatus;

import com.sakuraryoko.corelib.api.config.IConfigOption;

@ApiStatus.Internal
public class MainOptions implements IConfigOption
{
    public boolean afkMeEnabled;
    public boolean debugMode;
    public int defaultShadowTimeout;
    public int afkMeCommandPermissions;


    public MainOptions()
    {
        this.defaults();
    }

    public void defaults()
    {
        this.afkMeEnabled = true;
        this.debugMode = false;
        this.defaultShadowTimeout = 129600;
        this.afkMeCommandPermissions = 0;
    }

    @Override
    public MainOptions copy(IConfigOption opt)
    {
        MainOptions opts = (MainOptions) opt;

        this.afkMeEnabled = opts.afkMeEnabled;
        this.debugMode = opts.debugMode;
        this.defaultShadowTimeout = opts.defaultShadowTimeout;
        this.afkMeCommandPermissions = opts.afkMeCommandPermissions;

        return this;
    }
}
