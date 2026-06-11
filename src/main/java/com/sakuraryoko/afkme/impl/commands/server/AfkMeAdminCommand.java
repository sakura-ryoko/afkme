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

package com.sakuraryoko.afkme.impl.commands.server;

import java.util.List;
import org.jetbrains.annotations.ApiStatus;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import com.sakuraryoko.afkme.impl.AfkMe;
import com.sakuraryoko.afkme.impl.Reference;
import com.sakuraryoko.afkme.impl.commands.PermsWrap;
import com.sakuraryoko.afkme.impl.config.AfkMeConfigHandler;
import com.sakuraryoko.afkme.impl.modinit.AfkMeInit;
import com.sakuraryoko.afkme.impl.modinit.InitWrap;
import com.sakuraryoko.corelib.api.commands.IServerCommand;
import com.sakuraryoko.corelib.api.modinit.ModInitData;
import com.sakuraryoko.corelib.impl.config.ConfigManager;

import static net.minecraft.commands.Commands.literal;

@ApiStatus.Internal
public class AfkMeAdminCommand implements IServerCommand
{
    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment)
    {
        dispatcher.register(
                literal(this.getName())
                        .requires(PermsWrap.check(this.getNode(), 4))
                        .executes(this::about)
                        .then(literal("save")
                                      .requires(PermsWrap.check(this.getNode()+".save", 4))
                                      .executes(this::save)
                        )
                        .then(literal("reload")
                                      .requires(PermsWrap.check(this.getNode()+".reload", 4))
                                      .executes(this::reload)
                        )
        );
    }

    @Override
    public String getName()
    {
        return "afkme-admin";
    }

    @Override
    public String getModId()
    {
        return Reference.MOD_ID;
    }

    private int about(CommandContext<CommandSourceStack> ctx)
    {
        List<Component> info = AfkMeInit.getInstance().getVanillaFormatted(ModInitData.ALL_INFO);
        MutableComponent text = Component.literal("");

        for (Component entry : info)
        {
            text.append(entry).append("\n");
        }

        //#if MC >= 1.20.1
        //$$ ctx.getSource().sendSuccess(() -> text, false);
        //#else
        ctx.getSource().sendSuccess(text, false);
        //#endif

        return 1;
    }

    private int save(CommandContext<CommandSourceStack> ctx)
    {
        //#if MC >= 1.20.1
        //$$ ctx.getSource().sendSuccess(() -> InitWrap.text().formatText("Saving config!"), false);
        //#else
        ctx.getSource().sendSuccess(InitWrap.text().formatText("Saving config!"), false);
        //#endif

        ConfigManager.getInstance().saveEach(AfkMeConfigHandler.getInstance());
        String user = ctx.getSource().getTextName();
        AfkMe.LOGGER.info("{} has saved the configuration.", user);

        return 1;
    }

    private int reload(CommandContext<CommandSourceStack> ctx)
    {
        //#if MC >= 1.20.1
        //$$ ctx.getSource().sendSuccess(() -> InitWrap.text().formatText("Reloaded config!"), false);
        //#else
        ctx.getSource().sendSuccess(InitWrap.text().formatText("Reloaded config!"), false);
        //#endif

        ConfigManager.getInstance().reloadEach(AfkMeConfigHandler.getInstance());
        String user = ctx.getSource().getTextName();
        AfkMe.LOGGER.info("{} has reloaded the configuration.", user);

        return 1;
    }
}
