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

package com.sakuraryoko.unplugged_afk.impl.modinit;

import org.jetbrains.annotations.ApiStatus;

import com.sakuraryoko.unplugged_afk.impl.UnpluggedAfk;
import com.sakuraryoko.unplugged_afk.impl.Reference;
import com.sakuraryoko.unplugged_afk.impl.commands.CommandRegister;
import com.sakuraryoko.unplugged_afk.impl.config.UnpluggedConfigHandler;
import com.sakuraryoko.unplugged_afk.impl.config.ConfigWrap;
import com.sakuraryoko.unplugged_afk.impl.events.PlayerEventsHandler;
import com.sakuraryoko.unplugged_afk.impl.events.ServerEventsHandler;
import com.sakuraryoko.corelib.api.modinit.IModInitDispatcher;
import com.sakuraryoko.corelib.api.modinit.ModInitData;
import com.sakuraryoko.corelib.api.text.ITextHandler;
import com.sakuraryoko.corelib.impl.config.ConfigManager;
import com.sakuraryoko.corelib.impl.events.players.PlayerEventsManager;
import com.sakuraryoko.corelib.impl.events.server.ServerEventsManager;
import com.sakuraryoko.corelib.impl.text.BuiltinTextHandler;

@ApiStatus.Internal
public class UnpluggedInit implements IModInitDispatcher
{
    private static final UnpluggedInit INSTANCE = new UnpluggedInit();
    public static UnpluggedInit getInstance() { return INSTANCE; }

    private final ModInitData MOD_DATA;
    private boolean INIT = false;

    public UnpluggedInit()
    {
        this.MOD_DATA = new ModInitData(Reference.MOD_ID);
        this.MOD_DATA.setTextHandler(this.getTextHandler());
    }

    @Override
    public ModInitData getModInit()
    {
        return this.MOD_DATA;
    }

    @Override
    public String getModId()
    {
        return Reference.MOD_ID;
    }

    @Override
    public ITextHandler getTextHandler()
    {
        return BuiltinTextHandler.getInstance();
    }

    @Override
    public boolean isDebug()
    {
        return Reference.DEBUG || ConfigWrap.mainOpt().debugMode;
    }

    @Override
    public boolean isInitComplete()
    {
        return this.INIT;
    }

    @Override
    public void reset()
    {
        // NO-OP
    }

    @Override
    public void onModInit()
    {
        UnpluggedAfk.debugLog("Initializing Mod.");
        for (String s : this.getBasic(ModInitData.BASIC_INFO))
        {
            UnpluggedAfk.LOGGER.info(s);
        }

        UnpluggedAfk.debugLog("Config Initializing.");
        ConfigManager.getInstance().registerConfigDispatcher(UnpluggedConfigHandler.getInstance());
        UnpluggedAfk.debugLog("Registering commands.");
        CommandRegister.register();
        UnpluggedAfk.debugLog("Registering Handlers.");

        ServerEventsManager.getInstance().registerEventDispatcher(ServerEventsHandler.getInstance());
        PlayerEventsManager.getInstance().registerPlayerEvents(PlayerEventsHandler.getInstance());

        UnpluggedAfk.debugLog("All Tasks Done.");
        this.INIT = true;
    }
}
