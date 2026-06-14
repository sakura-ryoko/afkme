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

package com.sakuraryoko.afkme.impl.mixins;

import java.util.function.BooleanSupplier;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.sakuraryoko.afkme.impl.events.ServerEventsHandler;

@ApiStatus.Internal
@Mixin(value = MinecraftServer.class)
public class MixinMinecraftServer
{
	@Inject(method = "tickServer", at = @At(value = "TAIL"))
	private void afkme$onServerTick(BooleanSupplier hasTimeLeft, CallbackInfo ci)
	{
		ServerEventsHandler.getInstance().onTick((MinecraftServer) (Object) this);
	}
}
