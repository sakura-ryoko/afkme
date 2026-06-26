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

package com.sakuraryoko.unplugged_afk.impl.config;

import java.util.List;

import com.sakuraryoko.unplugged_afk.impl.config.data.options.UnpluggedOptions;
import org.jetbrains.annotations.ApiStatus;

import com.sakuraryoko.unplugged_afk.impl.config.data.options.MainOptions;
import com.sakuraryoko.unplugged_afk.impl.config.data.options.MessageOptions;
import com.sakuraryoko.unplugged_afk.impl.config.data.options.PlayerOptions;

@ApiStatus.Internal
public class ConfigWrap
{
    public static MainOptions mainOpt()
    {
        return UnpluggedConfigHandler.getInstance().getMainOptions();
    }

    public static UnpluggedOptions unplugged()
    {
        return UnpluggedConfigHandler.getInstance().getUnpluggedOptions();
    }

    public static MessageOptions mess()
    {
        return UnpluggedConfigHandler.getInstance().getMessageOptions();
    }

    public static List<PlayerOptions> players()
    {
        return UnpluggedConfigHandler.getInstance().getPlayerOptions();
    }
}
