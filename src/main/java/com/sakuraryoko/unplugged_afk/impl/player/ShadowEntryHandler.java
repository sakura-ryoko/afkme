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

package com.sakuraryoko.unplugged_afk.impl.player;

import javax.annotation.Nonnull;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import com.sakuraryoko.unplugged_afk.impl.config.ConfigWrap;
import com.sakuraryoko.unplugged_afk.impl.modinit.InitWrap;
import com.sakuraryoko.unplugged_afk.impl.player.interfaces.IPlayerInvoker;
import com.sakuraryoko.unplugged_afk.impl.player.shadow.ShadowServerPlayer;
import com.sakuraryoko.unplugged_afk.impl.player.state.ShadowState;

@ApiStatus.Internal
public record ShadowEntryHandler(ShadowEntry entry)
{
    public ShadowEntryHandler(@Nonnull ShadowEntry entry)
    {
        this.entry = entry;
    }

    @ApiStatus.Internal
    public void registerShadowAfk(@Nonnull ShadowServerPlayer player, ShadowState state)
    {
        if (this.entry().shadowEnabled()) { return; }

        int time = state.time();
        String reason = state.reason();
        long shadowTimeout = -1L;

        if (time > 0)
        {
            // Time is represented in Minutes
            shadowTimeout = (time * 60L) * 1000L;
        }

        if (reason == null && ConfigWrap.mess().defaultShadowReason == null)
        {
            this.entry().setReason("§cnone");
            String mess1 = this.entry().name().getString()
                    + ConfigWrap.mess().shadowStarted;
            Component mess2 = InitWrap.text().formatTextSafe(mess1);
            this.sendMessage(mess2);
        }
        else if (reason == null || reason.isEmpty())
        {
            this.entry().setReason("§cnone");
            String mess1 = this.entry().name().getString()
                    + ConfigWrap.mess().shadowStarted;
            Component mess2 = InitWrap.text().formatTextSafe(mess1);
            this.sendMessage(mess2);
        }
        else
        {
            this.entry().setReason(reason);
            String mess1 = this.entry().name().getString()
                    + ConfigWrap.mess().shadowStarted
                    + ConfigWrap.mess().shadowPunctuation
                    + reason;
            Component mess2 = InitWrap.text().formatTextSafe(mess1);
            this.sendMessage(mess2);
        }

        ShadowState newState = new ShadowState(true, time, shadowTimeout, reason);

        if (!newState.equals(state))
        {
            this.entry().updateShadowState(newState);
            PlayerManager.getInstance().setShadowState(player.getGameProfile(), newState);
        }
//        this.updatePlayerList();
    }

    @ApiStatus.Internal
    public void unregisterShadowAfk()
    {
        if (ConfigWrap.mess().displayDuration)
        {
            String ret = this.entry().name().getString()
                    + ConfigWrap.mess().shadowReturned
                    + ConfigWrap.mess().whenReturnDurationPrefix
                    + this.entry().getShadowDurationString()
                    + ConfigWrap.mess().whenReturnDurationSuffix + "§r";

            Component mess = InitWrap.text().formatTextSafe(ret);
            this.sendMessage(mess);
        }
        else
        {
            String ret = this.entry().name().getString() + ConfigWrap.mess().shadowReturned + "§r";
            Component mess = InitWrap.text().formatTextSafe(ret);
            this.sendMessage(mess);
        }

        this.entry().clearShadow();
        this.entry().reset();
    }

    @ApiStatus.Internal
    private void sendMessage(Component message)
    {
        if (!ConfigWrap.mess().broadcastMessages || message.getString().trim().isEmpty())
        {
            return;
        }

        this.invoker().unplugged$server().sendSystemMessage(message);     // Server Log

        for (ServerPlayer player : this.invoker().unplugged$server().getPlayerList().getPlayers())
        {
            player.sendSystemMessage(message);                          // Broadcast
        }
    }

    @ApiStatus.Internal
    private IPlayerInvoker invoker()
    {
        return (IPlayerInvoker) this.entry().shadowPlayer();
    }
}
