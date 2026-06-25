/*
 * This file is part of the AfkPlus project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2024  Sakura Ryoko and contributors
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

package com.sakuraryoko.afkme.impl.config.data;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.SerializedName;
import com.sakuraryoko.afkme.impl.config.data.options.AfkMeOptions;
import org.jetbrains.annotations.ApiStatus;

import com.sakuraryoko.afkme.impl.config.data.options.MainOptions;
import com.sakuraryoko.afkme.impl.config.data.options.MessageOptions;
import com.sakuraryoko.afkme.impl.config.data.options.PlayerOptions;
import com.sakuraryoko.corelib.api.config.IConfigData;

@ApiStatus.Internal
public class AfkMeConfigData implements IConfigData
{
    @SerializedName("___comment")
    public String comment = "AFK Me Config";

    @SerializedName("config_date")
    public String config_date;

    @SerializedName("main")
    public MainOptions MAIN = new MainOptions();

    @SerializedName("afk_me")
    public AfkMeOptions AFK_ME = new AfkMeOptions();

    @SerializedName("messages")
    public MessageOptions MESS = new MessageOptions();

    @SerializedName("players")
    public List<PlayerOptions> PLAYERS = new ArrayList<>();

}
