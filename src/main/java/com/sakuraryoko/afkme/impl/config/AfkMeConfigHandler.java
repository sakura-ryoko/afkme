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

package com.sakuraryoko.afkme.impl.config;

import java.util.ArrayList;
import java.util.List;

import com.sakuraryoko.afkme.impl.config.data.options.AfkMeOptions;
import org.jetbrains.annotations.ApiStatus;

import com.sakuraryoko.afkme.impl.AfkMe;
import com.sakuraryoko.afkme.impl.Reference;
import com.sakuraryoko.afkme.impl.config.data.AfkMeConfigData;
import com.sakuraryoko.afkme.impl.config.data.options.MainOptions;
import com.sakuraryoko.afkme.impl.config.data.options.MessageOptions;
import com.sakuraryoko.afkme.impl.config.data.options.PlayerOptions;
import com.sakuraryoko.afkme.impl.modinit.AfkMeInit;
import com.sakuraryoko.afkme.impl.player.PlayerManager;
import com.sakuraryoko.corelib.api.config.IConfigData;
import com.sakuraryoko.corelib.api.config.IConfigDispatch;
import com.sakuraryoko.corelib.api.time.TimeFormat;

@ApiStatus.Internal
public class AfkMeConfigHandler implements IConfigDispatch
{
    private static final AfkMeConfigHandler INSTANCE = new AfkMeConfigHandler();
    public static AfkMeConfigHandler getInstance() { return INSTANCE; }
    private AfkMeConfigData CONFIG = newConfig();
    private final String CONFIG_ROOT = ".";
    private final String CONFIG_NAME = Reference.MOD_ID;
    private boolean loaded = false;

    @Override
    public String getConfigRoot()
    {
        return this.CONFIG_ROOT;
    }

    @Override
    public boolean useRootDir()
    {
        return true;
    }

    @Override
    public String getConfigName()
    {
        return this.CONFIG_NAME;
    }

    @Override
    public AfkMeConfigData newConfig()
    {
        return new AfkMeConfigData();
    }

    @Override
    public AfkMeConfigData getConfig()
    {
        return CONFIG;
    }

    public MainOptions getMainOptions()
    {
        return CONFIG.MAIN;
    }

    public AfkMeOptions getAfkMeOptions()
    {
        return CONFIG.AFK_ME;
    }

    public MessageOptions getMessageOptions()
    {
        return CONFIG.MESS;
    }

    public List<PlayerOptions> getPlayerOptions()
    {
        return CONFIG.PLAYERS;
    }

    @Override
    public boolean isLoaded()
    {
        return this.loaded;
    }

    @Override
    public void initConfig()
    {
        // NO-OP
    }

    @Override
    public void onPreLoadConfig()
    {
        this.loaded = false;
    }

    @Override
    public void onPostLoadConfig()
    {
        this.loaded = true;
    }

    @Override
    public void onPreSaveConfig()
    {
        this.loaded = false;
    }

    @Override
    public void onPostSaveConfig()
    {
        this.loaded = true;
    }

    @Override
    public AfkMeConfigData defaults()
    {
        AfkMeConfigData config = this.newConfig();
        AfkMe.debugLog("AfkMeConfigHandler#defaults(): Setting default config.");

        // Set default values
        config.config_date = TimeFormat.RFC1123.formatNow(null);
        config.MAIN = new MainOptions();
        config.AFK_ME = new AfkMeOptions();
        config.MESS = new MessageOptions();
        config.PLAYERS = new ArrayList<>();

        return config;
    }

    @Override
    public AfkMeConfigData update(IConfigData newConfig)
    {
        AfkMeConfigData newConf = (AfkMeConfigData) newConfig;
        AfkMe.debugLog("AfkMeConfigHandler#update(): Refresh config.");

        // Refresh
        CONFIG.comment = AfkMeInit.getInstance().getModVersionString() + " Config";
        CONFIG.config_date = TimeFormat.RFC1123.formatNow(null);
        AfkMe.debugLog("AfkMeConfigHandler#update(): save_date: {} --> {}", newConf.config_date, CONFIG.config_date);

        // Copy Incoming Config
        CONFIG.MAIN.copy(newConf.MAIN);
        CONFIG.AFK_ME.copy(newConf.AFK_ME);
        CONFIG.MESS.copy(newConf.MESS);

        // Copy Players Config
        CONFIG.PLAYERS.clear();
        newConf.PLAYERS.forEach(
                player ->
                        CONFIG.PLAYERS.add(new PlayerOptions(player))
        );      // Deep copy

        return CONFIG;
    }

    @Override
    public void execute(boolean fromInit)
    {
        AfkMe.debugLog("AfkMeConfigHandler#execute(): Execute config.");

        // Load data into Player Manager.
        CONFIG.PLAYERS.forEach(
                player ->
                        PlayerManager.getInstance().syncFromConfig(player)
        );

        // Do this when the Config gets finalized.
        AfkMe.debugLog("AfkMeConfigHandler#execute(): new config_date: {}", CONFIG.config_date);
    }
}
