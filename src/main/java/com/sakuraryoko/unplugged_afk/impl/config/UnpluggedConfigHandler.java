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

import java.util.ArrayList;
import java.util.List;

import com.sakuraryoko.unplugged_afk.impl.config.data.options.UnpluggedOptions;
import org.jetbrains.annotations.ApiStatus;

import com.sakuraryoko.unplugged_afk.impl.UnpluggedAfk;
import com.sakuraryoko.unplugged_afk.impl.Reference;
import com.sakuraryoko.unplugged_afk.impl.config.data.UnpluggedConfigData;
import com.sakuraryoko.unplugged_afk.impl.config.data.options.MainOptions;
import com.sakuraryoko.unplugged_afk.impl.config.data.options.MessageOptions;
import com.sakuraryoko.unplugged_afk.impl.config.data.options.PlayerOptions;
import com.sakuraryoko.unplugged_afk.impl.modinit.UnpluggedInit;
import com.sakuraryoko.unplugged_afk.impl.player.PlayerManager;
import com.sakuraryoko.corelib.api.config.IConfigData;
import com.sakuraryoko.corelib.api.config.IConfigDispatch;
import com.sakuraryoko.corelib.api.time.TimeFormat;

@ApiStatus.Internal
public class UnpluggedConfigHandler implements IConfigDispatch
{
    private static final UnpluggedConfigHandler INSTANCE = new UnpluggedConfigHandler();
    public static UnpluggedConfigHandler getInstance() { return INSTANCE; }
    private UnpluggedConfigData CONFIG = newConfig();
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
    public UnpluggedConfigData newConfig()
    {
        return new UnpluggedConfigData();
    }

    @Override
    public UnpluggedConfigData getConfig()
    {
        return CONFIG;
    }

    public MainOptions getMainOptions()
    {
        return CONFIG.MAIN;
    }

    public UnpluggedOptions getUnpluggedOptions()
    {
        return CONFIG.UNPLUGGED;
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
    public UnpluggedConfigData defaults()
    {
        UnpluggedConfigData config = this.newConfig();
        UnpluggedAfk.debugLog("UnpluggedConfigHandler#defaults(): Setting default config.");

        // Set default values
        config.config_date = TimeFormat.RFC1123.formatNow(null);
        config.MAIN = new MainOptions();
        config.UNPLUGGED = new UnpluggedOptions();
        config.MESS = new MessageOptions();
        config.PLAYERS = new ArrayList<>();

        return config;
    }

    @Override
    public UnpluggedConfigData update(IConfigData newConfig)
    {
        UnpluggedConfigData newConf = (UnpluggedConfigData) newConfig;
        UnpluggedAfk.debugLog("UnpluggedConfigHandler#update(): Refresh config.");

        // Refresh
        CONFIG.comment = UnpluggedInit.getInstance().getModVersionString() + " Config";
        CONFIG.config_date = TimeFormat.RFC1123.formatNow(null);
        UnpluggedAfk.debugLog("UnpluggedConfigHandler#update(): save_date: {} --> {}", newConf.config_date, CONFIG.config_date);

        // Copy Incoming Config
        CONFIG.MAIN.copy(newConf.MAIN);
        CONFIG.UNPLUGGED.copy(newConf.UNPLUGGED);
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
        UnpluggedAfk.debugLog("UnpluggedConfigHandler#execute(): Execute config.");

        // Load data into Player Manager.
        CONFIG.PLAYERS.forEach(
                player ->
                        PlayerManager.getInstance().syncFromConfig(player)
        );

        // Do this when the Config gets finalized.
        UnpluggedAfk.debugLog("UnpluggedConfigHandler#execute(): new config_date: {}", CONFIG.config_date);
    }
}
