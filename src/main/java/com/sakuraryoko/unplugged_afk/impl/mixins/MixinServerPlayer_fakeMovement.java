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

package com.sakuraryoko.unplugged_afk.impl.mixins;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
//#if MC >= 1.21.11
//$$ import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
//$$ import org.spongepowered.asm.mixin.injection.At;
//$$ import org.objectweb.asm.Opcodes;
//$$ import net.minecraft.world.phys.Vec3;
//$$ import com.sakuraryoko.unplugged_afk.impl.player.shadow.ShadowServerPlayer;
//#endif

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
//#if MC >= 1.21.8
//$$ import net.minecraft.server.MinecraftServer;
//#endif
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
//#if MC >= 1.20.2
//$$ import net.minecraft.server.level.ClientInformation;
//#else
import net.minecraft.world.entity.player.ProfilePublicKey;
//#endif
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayer.class)
@ApiStatus.Internal
public abstract class MixinServerPlayer_fakeMovement extends Player
{
	//#if MC >= 1.21.8
	//$$ public MixinServerPlayer_fakeMovement(MinecraftServer server, Level level, GameProfile gameProfile, ClientInformation ci)
	//$$ {
		//$$ super(level, gameProfile);
	//$$ }
	//#elseif MC >= 1.20.2
	//$$ public MixinServerPlayer_fakeMovement(Level level, BlockPos pos, float yRot, GameProfile gameProfile, ClientInformation ci)
	//$$ {
		//$$ super(level, pos, yRot, gameProfile);
		//$$ throw new AssertionError();
	//$$ }
	//#elseif MC >= 1.19.3
	//$$ public MixinServerPlayer_fakeMovement(Level level, BlockPos pos, float yRot, GameProfile gameProfile)
	//$$ {
		//$$ super(level, pos, yRot, gameProfile);
		//$$ throw new AssertionError();
	//$$ }
	//#else
	public MixinServerPlayer_fakeMovement(Level level, BlockPos pos, float yRot, GameProfile gameProfile, @Nullable ProfilePublicKey profilePublicKey)
	{
		super(level, pos, yRot, gameProfile, profilePublicKey);
		throw new AssertionError();
	}
	//#endif

	//#if MC >= 1.21.11
	//$$ @ModifyExpressionValue(method = {"getKnownMovement", "getKnownSpeed"},
		//$$ at = @At(value = "FIELD",
			//$$ target = "Lnet/minecraft/server/level/ServerPlayer;lastKnownClientMovement:Lnet/minecraft/world/phys/Vec3;",
			//$$ opcode = Opcodes.GETFIELD),
		//$$ require = 2)
	//$$ private Vec3 unplugged$bypassClientMovementInfo(Vec3 original)
	//$$ {
		//$$ return (((Player) this) instanceof ShadowServerPlayer) ? super.getKnownMovement() : original;
	//$$ }
	//#endif
}
