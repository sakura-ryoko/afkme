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

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import com.sakuraryoko.afkme.impl.player.interfaces.IPlayerListInvoker;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
//#if MC >= 1.21.10
//$$ import org.slf4j.Logger;
//$$ import org.slf4j.LoggerFactory;
//#endif
//#if MC >= 1.20.2
//$$ import java.util.Optional;
//$$ import java.util.concurrent.CompletableFuture;
//#endif

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
//#if MC >= 1.21.10
//$$ import net.minecraft.core.UUIDUtil;
//$$ import net.minecraft.server.players.NameAndId;
//$$ import net.minecraft.server.players.OldUsersConverter;
//$$ import net.minecraft.world.item.component.ResolvableProfile;
//$$ import net.minecraft.world.level.storage.TagValueInput;
//$$ import net.minecraft.world.level.storage.ValueInput;
//$$ import net.minecraft.util.ProblemReporter;
//#endif
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
//#if MC >= 1.21.2
//$$ import net.minecraft.world.level.portal.TeleportTransition;
//#elseif MC >= 1.21.0
//$$ import net.minecraft.world.level.portal.DimensionTransition;
//#endif
import net.minecraft.server.players.GameProfileCache;
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
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

//#if MC >= 1.21.10
//$$ import com.sakuraryoko.afkme.impl.Reference;
//#endif
import com.sakuraryoko.afkme.impl.AfkMe;
import com.sakuraryoko.afkme.impl.config.ConfigWrap;
import com.sakuraryoko.afkme.impl.config.data.options.PlayerOptions;
import com.sakuraryoko.afkme.impl.player.*;
import com.sakuraryoko.afkme.impl.player.state.GameState;
import com.sakuraryoko.afkme.impl.player.state.PosState;
import com.sakuraryoko.afkme.impl.player.state.ShadowState;
import com.sakuraryoko.corelib.impl.text.BuiltinTextHandler;

@ApiStatus.Internal
@SuppressWarnings("EntityConstructor")
public class ShadowServerPlayer extends ServerPlayer
{
	public Runnable startingPosition = () -> {};
	private boolean freshPlayer;
	private long freshHoldTime;
	private long timeout = -1L;
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

	//#if MC >= 1.21.10
	//$$ private static CompletableFuture<GameProfile> fetchGameProfile(MinecraftServer server, final UUID uuid)
	//$$ {
		//$$ final ResolvableProfile resolver = ResolvableProfile.createUnresolved(uuid);
		//$$ return resolver.resolveProfile(server.services().profileResolver());
	//$$ }
	//#elseif MC >= 1.20.2
	//$$ private static CompletableFuture<Optional<GameProfile>> fetchGameProfile(final String name)
	//$$ {
		//$$ return SkullBlockEntity.fetchGameProfile(name);
	//$$ }
	//#endif

	public static void createShadowFromConfig(MinecraftServer server, PlayerOptions opts)
	{
		UUID uuid = opts.uuid;
		String name = opts.name;
		ShadowState state = opts.state;
		PosState pos = opts.pos;
		GameState game = opts.game;
		ResourceLocation id = ResourceLocation.tryParse(pos.location());
		AtomicReference<ResourceKey<Level>> ref = new AtomicReference<>(Level.OVERWORLD);

		if (id != null)
		{
			server.levelKeys().forEach(levelKey ->
			                           {
										   if (levelKey.location().equals(id))
										   {
											   ref.set(levelKey);
										   }
			                           });
		}

		ServerLevel level = server.getLevel(ref.get());
		//#if MC >= 1.21.10
		//$$ server.services().nameToIdCache().resolveOfflineUsers(false);
		//#else
		GameProfileCache.setUsesAuthentication(false);
		//#endif
		GameProfile profile;

		//#if MC >= 1.21.10
		//$$ UUID tempUUID = OldUsersConverter.convertMobOwnerIfNecessary(server, name);
		//$$ if (tempUUID != null && !tempUUID.equals(uuid))
		//$$ {
			//$$ uuid = tempUUID;
			//$$ opts.uuid = uuid;
		//$$ }
		//$$ if (uuid == null)
		//$$ {
			//$$ uuid = UUIDUtil.createOfflinePlayerUUID(name);
		//$$ }
		//$$ server.services().nameToIdCache().resolveOfflineUsers(server.isDedicatedServer() && server.usesAuthentication());
		//$$ profile = new GameProfile(uuid, name);
		//#else
		try
		{
			profile = server.getProfileCache().get(name).orElse(
					  server.getProfileCache().get(uuid).orElse(null)
			);
		}
		finally
		{
			GameProfileCache.setUsesAuthentication(server.isDedicatedServer() && server.usesAuthentication());
		}

		if (profile == null)
		{
			profile = new GameProfile(uuid, name);
		}
		//#endif

		//#if MC >= 1.21.10
		//$$ if (server.getPlayerList().getBans().isBanned(new NameAndId(profile)))
		//#else
		if (server.getPlayerList().getBans().isBanned(profile))
		//#endif
		{
			if (ConfigWrap.mainOpt().debugMode)
			{
				AfkMe.LOGGER.warn("createShadowFromConfig: Blocking banned player: ['{}'/{}]", name, uuid.toString());
			}

			PlayerManager.getInstance().remove(uuid);
			return;
		}

		//#if MC >= 1.21.10
		//$$ server.services().nameToIdCache().resolveOfflineUsers(server.isDedicatedServer() && server.usesAuthentication());
		//$$ fetchGameProfile(server, profile.id()).whenCompleteAsync((p, throwable) ->
		//$$ {
			//$$ if (throwable != null) { return; }
			//$$ GameProfile temp;
			//$$ if (p.name().isEmpty())
			//$$ {
				//$$ temp = profile;
			//$$ }
			//$$ else
			//$$ {
				//$$ temp = p;
			//$$ }
			//$$ createShadowFromConfigPhase2(server, level, temp, state, pos, game);
		//$$ });
		//#elseif MC >= 1.20.2
		//$$ GameProfile tempProfile = profile;
		//$$ fetchGameProfile(profile.getName()).thenAccept(opt ->
		//$$ {
			//$$ GameProfile temp = tempProfile;
			//$$ if (opt.isPresent())
			//$$ {
				//$$ temp = opt.get();
			//$$ }
			//$$ createShadowFromConfigPhase2(server, level, temp, state, pos, game);
		//$$ });
		//#else
		if (profile.getProperties().containsKey("textures"))
		{
			AtomicReference<GameProfile> result = new AtomicReference<>();
			SkullBlockEntity.updateGameprofile(profile, result::set);
			profile = result.get();
		}

		createShadowFromConfigPhase2(server, level, profile, state, pos, game);
		//#endif
	}

	private static ShadowServerPlayer createShadowFromConfigPhase2(MinecraftServer server, ServerLevel level, GameProfile profile,
	                                                               ShadowState state, PosState pos, GameState game)
	{
		GameType gameType = GameType.byName(game.gameMode(), GameType.DEFAULT_MODE);

		//#if MC >= 1.20.2
		//$$ ShadowServerPlayer shadow = new ShadowServerPlayer(server, level, profile, ClientInformation.createDefault());
		//#elseif MC >= 1.19.3
		//$$ ShadowServerPlayer shadow = new ShadowServerPlayer(server, level, profile);
		//#else
		ShadowServerPlayer shadow = new ShadowServerPlayer(server, level, profile, null);
		//#endif

		//#if MC >= 1.21.10
		//$$ shadow.startingPosition = () -> shadow.snapTo(pos.x(), pos.y(), pos.z(), pos.yaw(), pos.pitch());
		//#else
		shadow.startingPosition = () -> shadow.moveTo(pos.x(), pos.y(), pos.z(), pos.yaw(), pos.pitch());
		//#endif

		//#if MC >= 1.20.6
		//$$ server.getPlayerList().placeNewPlayer(new ShadowConnection(PacketFlow.SERVERBOUND), shadow, new CommonListenerCookie(profile, 0, ClientInformation.createDefault(), true));
		//#elseif MC >= 1.20.2
		//$$ server.getPlayerList().placeNewPlayer(new ShadowConnection(PacketFlow.SERVERBOUND), shadow, new CommonListenerCookie(profile, 0, ClientInformation.createDefault()));
		//#else
		server.getPlayerList().placeNewPlayer(new ShadowConnection(PacketFlow.SERVERBOUND), shadow);
		//#endif

		//#if MC >= 1.21.10
		//$$ loadPlayerNbt(shadow);
		//#endif
		shadow.setHealth(20.0f);
		shadow.connection.teleport(pos.x(), pos.y(), pos.z(), pos.yaw(), pos.pitch());
		shadow.gameMode.changeGameModeForPlayer(gameType);
		shadow.unsetRemoved();
		//#if MC >= 1.20.6
		//$$ shadow.getAttribute(Attributes.STEP_HEIGHT).setBaseValue(0.6F);
		//#elseif MC >= 1.19.4
		//$$ shadow.setMaxUpStep(0.6F);
		//#else
		shadow.maxUpStep = 0.6f;
		//#endif
		shadow.entityData.set(DATA_PLAYER_MODE_CUSTOMISATION, (byte) 0x7f);

		if (gameType.isSurvival())
		{
			// Survival players shouldn't be able to fly, or be invulnerable.
			shadow.getAbilities().flying = false;
			shadow.setInvulnerable(false);
		}
		else
		{
			shadow.getAbilities().flying = game.flying();
		}

		shadow.timeout = state.timeout();
		shadow.freshPlayer = true;
		shadow.freshHoldTime = System.currentTimeMillis();

		PlayerManager.getInstance().setShadowState(profile, state);
		ShadowEntry entry = ShadowEntryList.getInstance().add(shadow, state);

		if (entry != null)
		{
			entry.handler().registerShadowAfk(shadow, state);
			entry.setShadowPlayer(shadow);
		}

		return shadow;
	}

	public static ShadowServerPlayer createShadow(MinecraftServer server, ServerPlayer player, int time, String reason)
	{
		Component kickMsg = BuiltinTextHandler.getInstance().formatText(ConfigWrap.mess().shadowKickMessage);

		if (kickMsg == null || kickMsg.toString().isEmpty())
		{
			kickMsg = Component.translatable("multiplayer.disconnect.duplicate_login");
		}

		if (ConfigWrap.mess().hideShadowJoin)
		{
			((IPlayerListInvoker) server.getPlayerList()).afkme$toggleBroadcastSystemMessage(true);
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

		((IPlayerListInvoker) server.getPlayerList()).afkme$toggleBroadcastSystemMessage(false);

		//#if MC >= 1.21.10
		//$$ loadPlayerNbt(shadow);
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

		if (shadow.gameMode.isSurvival())
		{
			// Survival players shouldn't be able to fly, or be invulnerable.
			shadow.getAbilities().flying = false;
			shadow.setInvulnerable(false);
		}
		else
		{
			shadow.getAbilities().flying = player.getAbilities().flying;
		}

		if (time <= 0)
		{
			time = 129600;      // Hard coded in case of stupid
		}

		shadow.timeout = (time * 60L) * 1000L;
		shadow.freshPlayer = true;
		shadow.freshHoldTime = System.currentTimeMillis();

		ShadowState state = new ShadowState(true, time, shadow.timeout, reason);
		PlayerManager.getInstance().setShadowState(profile, state);
		ShadowEntry entry = ShadowEntryList.getInstance().add(shadow, state);

		if (entry != null)
		{
			entry.handler().registerShadowAfk(shadow, state);
			entry.setShadowPlayer(shadow);
		}

		return shadow;
	}

	//#if MC >= 1.21.10
	//$$ private final static Logger DUMB_LOGGER = LoggerFactory.getLogger(Reference.MOD_ID);
	//$$ private static void loadPlayerNbt(ShadowServerPlayer player)
	//$$ {
		//$$ try (ProblemReporter.ScopedCollector logger = new ProblemReporter.ScopedCollector(player.problemPath(), DUMB_LOGGER))
		//$$ {
			//$$ Optional<ValueInput> opt = player.level()
				//$$ .getServer().getPlayerList()
				//$$ .loadPlayerData(player.nameAndId())
				//$$ .map((nbt) ->
					//$$ TagValueInput.create(logger, player.registryAccess(), nbt)
				//$$ );
				//$$ opt.ifPresent((data) ->
				//$$ {
					//$$ player.load(data);
					//$$ player.loadAndSpawnEnderPearls(data);
					//$$ player.loadAndSpawnParentVehicle(data);
				//$$ });
			//$$ }
		//$$ }
	//#endif

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

	public void updateTimeOut(long timeout)
	{
		this.timeout = timeout;
	}

	@Override
	public void onEquipItem(final @NonNull EquipmentSlot slot, final @NonNull ItemStack previous, final @NonNull ItemStack stack)
	{
		if (!this.isUsingItem())
		{
			super.onEquipItem(slot, previous, stack);
		}
	}

	@Override
	//#if MC >= 1.21.2
	//$$ public boolean hurtServer(@NonNull ServerLevel level, @NonNull DamageSource damageSource, float amount)
	//#else
	public boolean hurt(@NonNull DamageSource damageSource, float amount)
	//#endif
	{
		ShadowEntry entry = ShadowEntryList.getInstance().get(this);

		if (entry != null && entry.shadowEnabled() &&
			ConfigWrap.afkMe().afkMeDisableDamage)
		{
			// If you want to be able to kill shadow bots;
			// then just disable this config.
			return false;
		}

		//#if MC >= 1.21.2
		//$$ return super.hurtServer(level, damageSource, amount);
		//#else
		return super.hurt(damageSource, amount);
		//#endif
	}

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
		this.killShadow();
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
				ShadowState state = PlayerManager.getInstance().getShadowState(this.getGameProfile());
				entry.updateShadowState(state);
				entry.setShadowTimeout(this.timeout);
			}
			else
			{
				this.timeout = entry.shadowTimeout();
			}

			PosState pos = PlayerManager.getInstance().getPosState(this.uuid);
			BlockPos blockPos = this.blockPosition();

			if (blockPos.getX() != pos.x() || blockPos.getY() != pos.y() || blockPos.getZ() != pos.z())
			{
				PlayerManager.getInstance().updatePlayerData(this);
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
				this.killShadow();
			}
		}
	}

	@Override
	public void die(@NonNull DamageSource damageSource)
	{
		this.dismount();
		super.die(damageSource);

		if (ConfigWrap.afkMe().resetHealthUponDeath)
		{
			this.setHealth(20.0F);
			this.foodData = new FoodData();
		}
		else
		{
			this.killShadow();
		}

		this.kill(this.getCombatTracker().getDeathMessage());
	}

	public void killShadow()
	{
		ShadowEntryList.getInstance().remove(this);
		PlayerManager.getInstance().updatePlayerData(this);
		PlayerManager.getInstance().resetShadowState(this);
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

//	//#if MC >= 1.20.1
//	//$$ @Override
//	//$$ protected void checkFallDamage(double y, boolean onGround, @NonNull BlockState state, @NonNull BlockPos pos)
//	//$$ {
//		//$$ this.doCheckFallDamage(0.0, y, 0.0, onGround);
//	//$$ }
//	//#endif

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
