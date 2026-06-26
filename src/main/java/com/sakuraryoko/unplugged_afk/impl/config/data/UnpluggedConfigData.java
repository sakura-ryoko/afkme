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

package com.sakuraryoko.unplugged_afk.impl.config.data;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.SerializedName;
import com.sakuraryoko.unplugged_afk.impl.config.data.options.UnpluggedOptions;
import org.jetbrains.annotations.ApiStatus;

import com.sakuraryoko.unplugged_afk.impl.config.data.options.MainOptions;
import com.sakuraryoko.unplugged_afk.impl.config.data.options.MessageOptions;
import com.sakuraryoko.unplugged_afk.impl.config.data.options.PlayerOptions;
import com.sakuraryoko.corelib.api.config.IConfigData;

@ApiStatus.Internal
public class UnpluggedConfigData implements IConfigData
{
    @SerializedName("___comment")
    public String comment = "Unplugged AFK Config";

    @SerializedName("config_date")
    public String config_date;

    @SerializedName("main")
    public MainOptions MAIN = new MainOptions();

    @SerializedName("unplugged")
    public UnpluggedOptions UNPLUGGED = new UnpluggedOptions();

    @SerializedName("messages")
    public MessageOptions MESS = new MessageOptions();

    @SerializedName("players")
    public List<PlayerOptions> PLAYERS = new ArrayList<>();

}
