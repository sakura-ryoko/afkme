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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.sakuraryoko.unplugged_afk.impl.player.interfaces.IPlayerListInvoker;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
//#if MC >= 1.20.2
//$$ import net.minecraft.server.level.ClientInformation;
//$$ import net.minecraft.server.network.CommonListenerCookie;
//#else
import net.minecraft.world.entity.player.ProfilePublicKey;
//#endif
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
//#if MC >= 1.21.10
//#elseif MC >= 1.21.6
//$$ import net.minecraft.util.ProblemReporter;
//#endif
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
//#if MC >= 1.21.10
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//#else
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.nbt.CompoundTag;
//#endif

import com.sakuraryoko.unplugged_afk.impl.player.PlayerManager;
import com.sakuraryoko.unplugged_afk.impl.player.ShadowEntry;
import com.sakuraryoko.unplugged_afk.impl.player.ShadowEntryList;
import com.sakuraryoko.unplugged_afk.impl.player.state.ShadowState;
import com.sakuraryoko.unplugged_afk.impl.player.shadow.ShadowGamePacketListener;
import com.sakuraryoko.unplugged_afk.impl.player.shadow.ShadowServerPlayer;

@Mixin(PlayerList.class)
@ApiStatus.Internal
public abstract class MixinPlayerList implements IPlayerListInvoker
{
	@Shadow @Final private MinecraftServer server;
	@Unique private boolean hideSystemMessages = false;

	//#if MC >= 1.21.10
	//$$ @Inject(method = "placeNewPlayer",
		//$$ at = @At(value = "INVOKE",
			//$$ target = "Lnet/minecraft/server/level/ServerPlayer;level()Lnet/minecraft/server/level/ServerLevel;"))
	//$$ private void unplugged$onLoad(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci)
	//#elseif MC >= 1.21.6
	//$$ @Inject(method = "load", at = @At(value = "RETURN", shift = At.Shift.BEFORE))
	//$$ private void unplugged$onLoad(ServerPlayer player, ProblemReporter problemReporter, CallbackInfoReturnable<CompoundTag> cir)
	//#else
	@Inject(method = "load", at = @At(value = "RETURN", shift = At.Shift.BEFORE))
	private void unplugged$onLoad(ServerPlayer player, CallbackInfoReturnable<CompoundTag> cir)
	//#endif
	{
		if (player instanceof ShadowServerPlayer sp)
		{
			sp.startingPosition.run();
		}
	}

	@WrapOperation(method = "placeNewPlayer",
	          at = @At(value = "NEW",
						//#if MC >= 1.20.2
	                        //$$ target = "(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/network/Connection;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/network/CommonListenerCookie;)Lnet/minecraft/server/network/ServerGamePacketListenerImpl;"
                        //#else
	                        target = "net/minecraft/server/network/ServerGamePacketListenerImpl"
                        //#endif
	               )
	)
	//#if MC >= 1.20.2
	//$$ private ServerGamePacketListenerImpl unplugged$spawnShadowPlayer(MinecraftServer server, Connection connection, ServerPlayer player, CommonListenerCookie cookie, Operation<ServerGamePacketListenerImpl> original)
	//#else
	private ServerGamePacketListenerImpl unplugged$spawnShadowPlayer(MinecraftServer server, Connection connection, ServerPlayer player, Operation<ServerGamePacketListenerImpl> original)
	//#endif
	{
		//#if MC >= 1.20.2
		//$$ if (player instanceof ShadowServerPlayer shadow)
		//$$ {
			//$$ return new ShadowGamePacketListener(this.server, connection, shadow, cookie);
		//$$ }

		//$$ return original.call(server, connection, player, cookie);
		//#else
		if (player instanceof ShadowServerPlayer shadow)
		{
			return new ShadowGamePacketListener(this.server, connection, shadow);
		}
//		else
//		{
//			return new ServerGamePacketListenerImpl(server, connection, player);
//		}

		return original.call(server, connection, player);
		//#endif
	}

	@WrapOperation(method = "respawn",
	               at = @At(value = "NEW",
                        //#if MC >= 1.20.2
	                        //$$ target = "(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/server/level/ServerLevel;Lcom/mojang/authlib/GameProfile;Lnet/minecraft/server/level/ClientInformation;)Lnet/minecraft/server/level/ServerPlayer;"
                        //#else
	                        target = "net/minecraft/server/level/ServerPlayer"
                        //#endif
	               )
	)
//#if MC >= 1.20.2
	//$$ private ServerPlayer unplugged$respawnShadow(MinecraftServer server, ServerLevel level, GameProfile profile,
													//$$ ClientInformation ci,
													//$$ Operation<ServerPlayer> original,
													//$$ @Local(argsOnly = true) ServerPlayer player)
//#elseif MC >= 1.19.3
	//$$ private ServerPlayer unplugged$respawnShadow(MinecraftServer server, ServerLevel level, GameProfile profile,
													//$$ Operation<ServerPlayer> original,
													//$$ @Local(argsOnly = true) ServerPlayer player)
//#else
	private ServerPlayer unplugged$respawnShadow(MinecraftServer server, ServerLevel level, GameProfile profile,
												 ProfilePublicKey profilePublicKey,
												 Operation<ServerPlayer> original,
												 @Local(argsOnly = true) ServerPlayer player)
//#endif
	{
		//#if MC >= 1.20.2
		//$$ if (player instanceof ShadowServerPlayer sp)
		//$$ {
			//$$ ShadowServerPlayer newSp = ShadowServerPlayer.respawnShadow(server, level, profile, ci);
			//$$ newSp.updateTimeOut(sp.getTimeout());
			//$$ ShadowEntryList.getInstance().updateShadow(newSp);
			//$$ ShadowEntry entry = ShadowEntryList.getInstance().get(newSp);
			//$$ ShadowState state = PlayerManager.getInstance().getShadowState(profile);
			//$$ if (!state.enabled())
			//$$ {
				//$$ state = new ShadowState(true, state.time(), newSp.getTimeout(), state.reason());
				//$$ PlayerManager.getInstance().setShadowState(profile, state);
				//$$ if (entry != null)
				//$$ {
					//$$ entry.updateShadowState(state);
				//$$ }
			//$$ }
			//$$ return newSp;
		//$$ }

		//$$ return original.call(server, level, profile, ci);
		//#elseif MC >= 1.19.3
		//$$ if (player instanceof ShadowServerPlayer sp)
		//$$ {
			//$$ ShadowServerPlayer newSp = ShadowServerPlayer.respawnShadow(server, level, profile);
			//$$ newSp.updateTimeOut(sp.getTimeout());
			//$$ ShadowEntryList.getInstance().updateShadow(newSp);
			//$$ ShadowEntry entry = ShadowEntryList.getInstance().get(newSp);
			//$$ ShadowState state = PlayerManager.getInstance().getShadowState(profile);
			//$$ if (!state.enabled())
			//$$ {
				//$$ state = new ShadowState(true, state.time(), newSp.getTimeout(), state.reason());
				//$$ PlayerManager.getInstance().setShadowState(profile, state);
				//$$ if (entry != null)
				//$$ {
					//$$ entry.updateShadowState(state);
				//$$ }
			//$$ }
			//$$ return newSp;
		//$$ }

		//$$ return original.call(server, level, profile);
		//#else
		if (player instanceof ShadowServerPlayer sp)
		{
			ShadowServerPlayer newSp = ShadowServerPlayer.respawnShadow(server, level, profile, profilePublicKey);
			newSp.updateTimeOut(sp.getTimeout());
			ShadowEntryList.getInstance().updateShadow(newSp);
			ShadowEntry entry = ShadowEntryList.getInstance().get(newSp);
			ShadowState state = PlayerManager.getInstance().getShadowState(profile);
			if (!state.enabled())
			{
				state = new ShadowState(true, state.time(), newSp.getTimeout(), state.reason());
				PlayerManager.getInstance().setShadowState(profile, state);
				if (entry != null)
				{
					entry.updateShadowState(state);
				}
			}

			return newSp;
		}

		return original.call(server, level, profile, profilePublicKey);
		//#endif
	}

	@Inject(method = "broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V", at = @At("HEAD"),
	        cancellable = true)
	private void unplugged$hideSystemBroadcasts(Component message, boolean bypassHiddenChat, CallbackInfo ci)
	{
		if (this.hideSystemMessages && !bypassHiddenChat)
		{
			ci.cancel();
		}
	}

	@Override
	public void unplugged$toggleBroadcastSystemMessage(boolean toggle)
	{
		this.hideSystemMessages = toggle;
	}
}
