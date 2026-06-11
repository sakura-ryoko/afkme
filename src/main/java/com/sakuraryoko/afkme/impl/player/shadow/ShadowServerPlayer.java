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

package com.sakuraryoko.afkme.impl.player.shadow;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.mojang.authlib.GameProfile;
//#if MC >= 1.20.6
//$$ import net.minecraft.world.entity.ai.attributes.Attributes;
//#endif
//#if MC >= 1.20.1
//$$ import net.minecraft.core.BlockPos;
//$$ import net.minecraft.world.level.block.state.BlockState;
//#endif
//#if MC >= 1.21.2
//$$ import net.minecraft.network.DisconnectionDetails;
//$$ import net.minecraft.network.chat.contents.TranslatableContents;
//#endif
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
//#if MC >= 1.21.2
//$$ import net.minecraft.world.level.portal.TeleportTransition;
//#elseif MC >= 1.21.0
//$$ import net.minecraft.world.level.portal.DimensionTransition;
//#endif
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
//#if MC >= 1.20.2
//$$ import net.minecraft.server.network.CommonListenerCookie;
//$$ import net.minecraft.server.level.ClientInformation;
//#else
import net.minecraft.world.entity.player.ProfilePublicKey;
//#endif
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;

import com.sakuraryoko.afkme.impl.config.ConfigWrap;
import com.sakuraryoko.afkme.impl.player.PlayerManager;
import com.sakuraryoko.afkme.impl.player.ShadowEntry;
import com.sakuraryoko.afkme.impl.player.ShadowEntryList;
import com.sakuraryoko.afkme.impl.player.ShadowState;
import com.sakuraryoko.corelib.impl.text.BuiltinTextHandler;

public class ShadowServerPlayer extends ServerPlayer
{
	private boolean freshPlayer = true;
	private long freshHoldTime;
	private long timeout = -1L;
	private int time;
	private String reason;
	private long lastTick = -1L;

	//#if MC >= 1.20.2
	//$$ public ShadowServerPlayer(MinecraftServer server, ServerLevel level, GameProfile profile, ClientInformation ci)
	//$$ {
		//$$ super(server, level, profile, ci);
	//$$ }
	//#elseif MC >= 1.19.3
	//$$ public ShadowServerPlayer(MinecraftServer server, ServerLevel level, GameProfile profile)
	//$$ {
		//$$ super(server, level, profile);
	//$$ }
	//#else
	public ShadowServerPlayer(MinecraftServer server, ServerLevel level, GameProfile profile, @Nullable ProfilePublicKey profilePublicKey)
	{
		super(server, level, profile, profilePublicKey);
	}
	//#endif

	public static ShadowServerPlayer createShadow(MinecraftServer server, ServerPlayer player, int time, String reason)
	{
		Component kickMsg = BuiltinTextHandler.getInstance().formatText(ConfigWrap.mess().shadowKickMessage);

		if (kickMsg == null || kickMsg.toString().isEmpty())
		{
			kickMsg = Component.translatable("multiplayer.disconnect.duplicate_login");
		}

		ShadowEntry entry = ShadowEntryList.getInstance().get(player);

		if (entry == null)
		{
			entry = ShadowEntryList.getInstance().add(player);
		}

		if (entry == null)
		{
			throw new RuntimeException("Error creating ShadowServerPlayer");
		}

		server.getPlayerList().remove(player);
		player.connection.disconnect(kickMsg);

		//#if MC >= 1.20.1
		//$$ ServerLevel level = player.serverLevel();
		//#else
		ServerLevel level = player.getLevel();
		//#endif
		GameProfile profile = player.getGameProfile();
		//#if MC >= 1.20.2
		//$$ ShadowServerPlayer shadow = new ShadowServerPlayer(server, level, profile, player.clientInformation());
		//#elseif MC >= 1.19.3
		//$$ ShadowServerPlayer shadow = new ShadowServerPlayer(server, level, profile);
		//#else
		ShadowServerPlayer shadow = new ShadowServerPlayer(server, level, profile, player.getProfilePublicKey());
		//#endif

		//#if MC >= 1.19.3
		//$$ shadow.setChatSession(player.getChatSession());
		//#endif

		//#if MC >= 1.20.6
		//$$ server.getPlayerList().placeNewPlayer(new ShadowConnection(PacketFlow.SERVERBOUND), shadow, new CommonListenerCookie(profile, 0, player.clientInformation(), true));
		//#elseif MC >= 1.20.2
		//$$ server.getPlayerList().placeNewPlayer(new ShadowConnection(PacketFlow.SERVERBOUND), shadow, new CommonListenerCookie(profile, 0, player.clientInformation()));
		//#else
		server.getPlayerList().placeNewPlayer(new ShadowConnection(PacketFlow.SERVERBOUND), shadow);
		//#endif

		shadow.setHealth(player.getHealth());
		shadow.connection.teleport(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
		shadow.gameMode.changeGameModeForPlayer(player.gameMode.getGameModeForPlayer());
		//#if MC >= 1.20.6
		//$$ shadow.getAttribute(Attributes.STEP_HEIGHT).setBaseValue(0.6F);
		//#elseif MC >= 1.19.4
		//$$ shadow.setMaxUpStep(0.6F);
		//#else
		shadow.maxUpStep = 0.6f;
		//#endif
		shadow.entityData.set(DATA_PLAYER_MODE_CUSTOMISATION, player.getEntityData().get(DATA_PLAYER_MODE_CUSTOMISATION));
		shadow.getAbilities().flying = player.getAbilities().flying;

		if (time <= 0)
		{
			time = 129600;      // Hard coded in case of stupid
		}

		shadow.timeout = (long) (time * 60L) * 1000L;
		shadow.time = time;
		shadow.reason = reason;
		shadow.freshHoldTime = System.currentTimeMillis();
		entry.setShadowPlayer(shadow);
		ShadowState state = new ShadowState(true, time, reason);
		PlayerManager.getInstance().setShadowState(profile, state);

		return shadow;
	}

	//#if MC >= 1.20.2
	//$$ public static ShadowServerPlayer respawnShadow(MinecraftServer server, ServerLevel level, GameProfile profile, ClientInformation ci)
	//$$ {
	//$$ return new ShadowServerPlayer(server, level, profile, ci);
	//$$ }
	//#elseif MC >= 1.19.3
	//$$ public static ShadowServerPlayer respawnShadow(MinecraftServer server, ServerLevel level, GameProfile profile)
	//$$ {
		//$$ return new ShadowServerPlayer(server, level, profile);
	//$$ }
	//#else
	public static ShadowServerPlayer respawnShadow(MinecraftServer server, ServerLevel level, GameProfile profile, @Nullable ProfilePublicKey profilePublicKey)
	{
		return new ShadowServerPlayer(server, level, profile, profilePublicKey);
	}
	//#endif

	private void createShadowPost(MinecraftServer server)
	{
		server.getPlayerList().broadcastAll(new ClientboundRotateHeadPacket(this, (byte) (this.yHeadRot * 256 / 360)),
				//#if MC >= 1.20.1
				//$$ this.serverLevel().dimension());
				//#else
                this.level.dimension());
				//#endif
		server.getPlayerList().broadcastAll(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, this));
//		server.getPlayerList().broadcastAll(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.UPDATE_GAME_MODE, this));
//		server.getPlayerList().broadcastAll(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.UPDATE_LATENCY, this));
		//#if MC >= 1.19.3
		//$$ server.getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED, this));
		//#endif
	}

	public long getTimeout()
	{
		return this.timeout;
	}

	public int getTime()
	{
		return this.time;
	}

	public String getReason()
	{
		return this.reason;
	}

	public void updateTimeAndReason(long timeout, int time, String reason)
	{
		this.timeout = timeout;
		this.time = time;
		this.reason = reason;
	}

	@Override
	public void onEquipItem(final @NonNull EquipmentSlot slot, final @NonNull ItemStack previous, final @NonNull ItemStack stack)
	{
		if (!this.isUsingItem())
		{
			super.onEquipItem(slot, previous, stack);
		}
	}

//	@Override
//	//#if MC >= 1.21.2
//	//$$ public boolean hurtServer(@NonNull ServerLevel level, @NonNull DamageSource damageSource, float amount)
//	//#else
//	public boolean hurt(@NonNull DamageSource damageSource, float amount)
//	//#endif
//	{
//		ShadowEntry entry = ShadowEntryList.getInstance().get(this);
//
//		if (entry != null && entry.shadowEnabled() &&
//			ConfigWrap.mess().shadowInvulnerable)
//		{
//			// Works just like disable damage; so let's just
//			// stop the default behavior from glitching things.
//			// If you want to be able to kill shadow bots;
//			// then just disable this config.
//			return false;
//		}
//
//		//#if MC >= 1.21.2
//		//$$ return super.hurtServer(level, damageSource, amount);
//		//#else
//		return super.hurt(damageSource, amount);
//		//#endif
//	}

	@Override
	//#if MC >= 1.21.2
	//$$ public void kill(@NonNull ServerLevel level)
	//#else
	public void kill()
	//#endif
	{
		this.kill(BuiltinTextHandler.getInstance().formatTextSafe("Killed"));
	}

	public void kill(Component message)
	{
		this.dismount();
		//#if MC >= 1.21.2
		//$$ if (message.getContents() instanceof TranslatableContents text && text.getKey().equals("multiplayer.disconnect.duplicate_login"))
		//$$ {
			//$$ this.connection.onDisconnect(new DisconnectionDetails(message));
		//$$ }
		//$$ else
		//$$ {
		//#endif
		//#if MC >= 1.21.8
			//$$ this.level().getServer().schedule(
				//$$ new TickTask(this.level().getServer().getTickCount(),
						//$$ () -> this.connection.onDisconnect(new DisconnectionDetails(message))
			//$$ ));
		//$$ }
		//#elseif MC >= 1.21.2
			//$$ this.server.schedule(
				//$$ new TickTask(this.server.getTickCount(),
						//$$ () -> this.connection.onDisconnect(new DisconnectionDetails(message))
			//$$ ));
		//$$ }
		//#else
		this.server.tell(
				new TickTask(this.server.getTickCount(),
				             () -> this.connection.disconnect(message)
				));
		//#endif
	}

	@Override
	public void tick()
	{
		//#if MC >= 1.21.8
		//$$ MinecraftServer server = this.level().getServer();
		//#else
		MinecraftServer server = this.getServer();
		//#endif

		if (server.getTickCount() % 10 == 0)
		{
			if (this.freshPlayer)
			{
				final long now = System.currentTimeMillis();

				// Delay sending the ADD_PLAYER packets;
				// ... because Mojang.
				if ((now - this.freshHoldTime) >= 200L)
				{
					this.createShadowPost(server);
					this.freshPlayer = false;
				}
			}

			this.tickShadowAfk(server);
			this.connection.resetPosition();
			//#if MC >= 1.21.8
			//$$ this.level().getChunkSource().move(this);
			//#elseif MC >= 1.20.1
			//$$ this.serverLevel().getChunkSource().move(this);
			//#else
			this.getLevel().getChunkSource().move(this);
			//#endif
//			this.hasChangedDimension();
		}

		try
		{
			super.tick();
			this.doTick();
		}
		catch (NullPointerException ignored) {}
	}

	private void tickShadowAfk(MinecraftServer server)
	{
		final long now = System.currentTimeMillis();

		if (this.lastTick < 0L)
		{
			this.lastTick = now;
		}

		final long tickDelta = now - this.lastTick;
		this.lastTick = now;
		ShadowEntry entry = ShadowEntryList.getInstance().get(this);

		if (entry != null)
		{
			if (!entry.shadowEnabled())
			{
				entry.handler().registerShadowAfk(this, this.time, this.reason);
				entry.setShadowTimeout(this.timeout);
			}
			else
			{
				this.timeout = entry.shadowTimeout();
			}

			if (!entry.tickShadowTimeout(tickDelta))
			{
				String mess = ConfigWrap.mess().shadowExpiredReason;

				if (mess == null || mess.isEmpty())
				{
					mess = "Shadow Expired";
				}

				Component reason = BuiltinTextHandler.getInstance().formatTextSafe(mess);
				this.kill(reason);

				server.getPlayerList().remove(this);
				ShadowEntryList.getInstance().remove(this);
			}
		}
	}

	@Override
	public void die(@NonNull DamageSource damageSource)
	{
		this.dismount();
		super.die(damageSource);
		this.setHealth(20.0F);
		this.foodData = new FoodData();
		this.kill(this.getCombatTracker().getDeathMessage());
	}

	private void dismount()
	{
		if (this.getVehicle() != null)
		{
			if (this.getVehicle() instanceof Player)
			{
				this.stopRiding();
			}

			for (Entity entry : this.getVehicle().getPassengers())
			{
				if (entry instanceof Player)
				{
					entry.stopRiding();
				}
			}
		}
	}

	@Override
	public @NonNull String getIpAddress()
	{
		return "127.0.0.1";
	}

	//#if MC >= 1.20.1
	//$$ @Override
	//$$ protected void checkFallDamage(double y, boolean onGround, @NonNull BlockState state, @NonNull BlockPos pos)
	//$$ {
		//$$ this.doCheckFallDamage(0.0, y, 0.0, onGround);
	//$$ }
	//#endif

	@Override
	//#if MC >= 1.21.2
	//$$ public ServerPlayer teleport(@NonNull TeleportTransition transition)
	//$${
	//$$ super.teleport(transition);
	//#elseif MC >= 1.21.0
	//$$ public Entity changeDimension(@NonNull DimensionTransition transition)
	//$${
		//$$ super.changeDimension(transition);
	//#else
	public Entity changeDimension(@NonNull ServerLevel level)
	{
		super.changeDimension(level);
	//#endif

		// Handle freeing the End
		if (this.wonGame)
		{
			ServerboundClientCommandPacket packet = new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN);
			this.connection.handleClientCommand(packet);
		}

		if (this.connection.player.isChangingDimension())
		{
			this.connection.player.hasChangedDimension();
		}

		return this.connection.player;
	}
}
