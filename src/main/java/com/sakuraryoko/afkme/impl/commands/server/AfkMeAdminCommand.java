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
import java.util.UUID;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.ApiStatus;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import com.sakuraryoko.afkme.impl.AfkMe;
import com.sakuraryoko.afkme.impl.Reference;
import com.sakuraryoko.afkme.impl.commands.PermsWrap;
import com.sakuraryoko.afkme.impl.config.AfkMeConfigHandler;
import com.sakuraryoko.afkme.impl.modinit.AfkMeInit;
import com.sakuraryoko.afkme.impl.modinit.InitWrap;
import com.sakuraryoko.afkme.impl.player.PlayerEntry;
import com.sakuraryoko.afkme.impl.player.PlayerManager;
import com.sakuraryoko.afkme.impl.player.ShadowEntry;
import com.sakuraryoko.afkme.impl.player.ShadowEntryList;
import com.sakuraryoko.corelib.api.commands.IServerCommand;
import com.sakuraryoko.corelib.api.modinit.ModInitData;
import com.sakuraryoko.corelib.impl.config.ConfigManager;

import static net.minecraft.commands.Commands.argument;
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
                        .then(literal("list")
                                      .requires(PermsWrap.check(this.getNode()+".list", 4))
                                      .then(literal("players")
                                                    .requires(PermsWrap.check(this.getNode()+".list.players", 4))
                                                    .executes(this::listPlayerMap)
                                      )
                                      .then(literal("shadows")
                                                    .requires(PermsWrap.check(this.getNode()+".list.shadows", 4))
                                                    .executes(this::listShadowMap)
                                      )
                                      .then(literal("all")
                                                    .requires(PermsWrap.check(this.getNode()+".list.all", 4))
                                                    .executes(this::listAll)
                                      )
                        )
                        .then(literal("info")
                                      .requires(PermsWrap.check(this.getNode()+".info", 4))
                                      .executes(this::infoPlayer)
                                      .then(argument("player", EntityArgument.player())
                                                    .executes(ctx ->
                                                                      this.infoPlayer(ctx, EntityArgument.getPlayer(ctx, "player"))
                                                    )
                                      )
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

    private int listAll(CommandContext<CommandSourceStack> ctx)
    {
        this.listPlayerMap(ctx);
        this.listShadowMap(ctx);

        return 1;
    }

    private int listPlayerMap(CommandContext<CommandSourceStack> ctx)
    {
        ImmutableMap<UUID, PlayerEntry> playerMap = PlayerManager.getInstance().getPlayerMap();
        MutableComponent text = Component.literal("");
        int count = 0;

        text.append(
                InitWrap.text().formatText("§dPlayer Map:")
        );

        for (UUID key : playerMap.keySet())
        {
            PlayerEntry entry = playerMap.get(key);

            if (entry != null)
            {
                text.append(
                        InitWrap.text().formatText(
                                String.format("\n§9[Entry: %02d]", count)
                        )
                ).append(
                        entry.getDebugFormatted()
                );
            }

            count++;
        }

        text.append(
                String.format("\n§6(%d total)§r", count)
        );

        //#if MC >= 1.20.1
        //$$ ctx.getSource().sendSuccess(() -> text, false);
        //#else
        ctx.getSource().sendSuccess(text, false);
        //#endif

        return 1;
    }

    private int listShadowMap(CommandContext<CommandSourceStack> ctx)
    {
        ImmutableList<ShadowEntry> list = ShadowEntryList.getInstance().listCopy();
        MutableComponent text = Component.literal("");
        int count = 0;

        text.append(
                InitWrap.text().formatText("\n§dShadow List:")
        );

        for (ShadowEntry entry : list)
        {
            text.append(
                    InitWrap.text().formatText(
                            String.format("\n§9[Entry: %02d]", count)
                    )
            ).append(
                    entry.getDebugFormatted()
            );

            count++;
        }

        text.append(
                String.format("\n§6(%d total)§r", count)
        );

        //#if MC >= 1.20.1
        //$$ ctx.getSource().sendSuccess(() -> text, false);
        //#else
        ctx.getSource().sendSuccess(text, false);
        //#endif

        return 1;
    }

    private int infoPlayer(CommandContext<CommandSourceStack> ctx)
    {
        try
        {
            return this.infoPlayer(ctx, ctx.getSource().getPlayerOrException());
        }
        catch (CommandSyntaxException err)
        {
            AfkMe.LOGGER.warn("CMD:infoPlayer: Syntax Error; {}", err.getLocalizedMessage());
            return 0;
        }
    }

    private int infoPlayer(CommandContext<CommandSourceStack> ctx, ServerPlayer player)
    {
        MutableComponent text = Component.literal("");

        text.append(
                InitWrap.text().formatText("§7Player Info: ")
        ).append(
                PlayerManager.getInstance().getDebugFormatted(player.getUUID())
        ).append(
                InitWrap.text().formatText("\n§7Shadow Info: ")
        ).append(
                ShadowEntryList.getInstance().getDebugFormatted(player.getUUID())
        );

        //#if MC >= 1.20.1
        //$$ ctx.getSource().sendSuccess(() -> text, false);
        //#else
        ctx.getSource().sendSuccess(text, false);
        //#endif

        return 1;
    }
}
