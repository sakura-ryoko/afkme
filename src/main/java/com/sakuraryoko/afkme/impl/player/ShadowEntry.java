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

package com.sakuraryoko.afkme.impl.player;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.VisibleForTesting;

import com.mojang.authlib.GameProfile;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import com.sakuraryoko.afkme.impl.config.ConfigWrap;
import com.sakuraryoko.afkme.impl.modinit.InitWrap;
import com.sakuraryoko.afkme.impl.player.shadow.ShadowServerPlayer;
import com.sakuraryoko.corelib.api.time.DurationFormat;
import com.sakuraryoko.corelib.api.time.TimeFormat;

public class ShadowEntry
{
	private @Nullable ShadowServerPlayer shadowPlayer;
	private final ShadowEntryHandler handler;
	private boolean shadowEnabled;
	private long shadowStartTimeMs;
	private long shadowStartTimeEpoch;
	private long shadowTimeout;
	private String reason;

	private ShadowEntry()
	{
		this.shadowPlayer = null;
		this.shadowEnabled = false;
		this.shadowStartTimeMs = -1L;
		this.shadowStartTimeEpoch = -1L;
		this.shadowTimeout = -1L;
		this.reason = "";
		this.handler = new ShadowEntryHandler(this);
	}

	public static ShadowEntry create(@Nonnull ShadowServerPlayer player)
	{
		ShadowEntry newEntry = new ShadowEntry();
		newEntry.setShadowPlayer(player);
		return newEntry;
	}

	public ShadowEntryHandler handler()
	{
		return this.handler;
	}

	@Nullable
	public ShadowServerPlayer shadowPlayer()
	{
		return this.shadowPlayer;
	}

	public Component name()
	{
		return this.shadowPlayer != null
		       ? this.shadowPlayer.getName()
		       : Component.empty();
	}

	public @Nullable GameProfile profile()
	{
		return this.shadowPlayer != null
		       ? this.shadowPlayer.getGameProfile()
		       : null;
	}

	public boolean shadowEnabled()
	{
		return this.shadowEnabled;
	}

	public long shadowStartTimeEpoch()
	{
		return this.shadowStartTimeEpoch;
	}

	public long shadowStartTimeMs()
	{
		return this.shadowStartTimeMs;
	}

	public long shadowTimeout()
	{
		return this.shadowTimeout;
	}

	public String reason()
	{
		return this.reason;
	}

	public DurationFormat getDurationType()
	{
		DurationFormat format = DurationFormat.fromStringStatic(ConfigWrap.mess().duration.option.getName());

		if (format != null)
		{
			return format;
		}

		ConfigWrap.mess().duration.option = DurationFormat.PRETTY;
		return DurationFormat.REGULAR;
	}

	public TimeFormat getTimeDateType()
	{
		TimeFormat format = TimeFormat.fromStringStatic(ConfigWrap.mess().timeDate.option.getName());

		if (format != null)
		{
			return format;
		}

		ConfigWrap.mess().timeDate.option = TimeFormat.REGULAR;
		return TimeFormat.REGULAR;
	}

	public String getShadowStartTimeEpochString()
	{
		return this.getTimeDateType().formatTo(this.shadowStartTimeEpoch(), ConfigWrap.mess().duration.customFormat);
	}

	public String getShadowDurationString()
	{
		return this.getDurationType().format((Util.getMillis() - this.shadowStartTimeMs()), ConfigWrap.mess().duration.customFormat);
	}

    public String getShadowTimeoutString()
    {
        return this.getDurationType().format((this.shadowTimeout), ConfigWrap.mess().duration.customFormat);
    }

	public void setShadowPlayer(@Nonnull ShadowServerPlayer player)
	{
		this.shadowPlayer = player;
	}

	public void updateShadowState(ShadowState state)
	{
		this.shadowEnabled = state.enabled();
		this.setShadowTimeout(state.timeout());
		this.setReason(state.reason());

		if (this.shadowStartTimeMs() <= 1L)
		{
			this.setShadowStartTimeMs(Util.getMillis());
		}
		if (this.shadowStartTimeEpoch() <= 1L)
		{
			this.setShadowStartTimeEpoch(ZonedDateTime.now().toInstant().toEpochMilli());
		}
	}

	public void clearShadow()
	{
		this.shadowPlayer = null;
	}

	public void setShadowStartTimeMs(long time)
	{
		this.shadowStartTimeMs = time;
	}

	public void setShadowStartTimeEpoch(long time)
	{
		this.shadowStartTimeEpoch = time;
	}

	public void setShadowTimeout(long timeout)
    {
        this.shadowTimeout = Math.min(Math.max(timeout, 0L), Long.MAX_VALUE);
    }

	public void setReason(String reason)
	{
		this.reason = reason;
	}

    public boolean tickShadowTimeout(final long tickDelta)
    {
        this.shadowTimeout -= tickDelta;
        return this.shadowTimeout > 0L;
    }

	public boolean matches(@Nonnull ShadowServerPlayer player)
	{
		if (this.shadowPlayer == null) { return false; }
		return  this.shadowPlayer.getUUID().equals(player.getUUID()) ||
				this.shadowPlayer.equals(player);
	}

	public boolean matches(UUID uuid)
	{
		if (this.shadowPlayer == null) { return false; }
		return  this.shadowPlayer.getUUID().equals(uuid);
	}

	@VisibleForTesting
	public Component getDebugFormatted()
	{
		MutableComponent text = Component.literal("");

		text.append(
				InitWrap.text().formatText("§r\n - §7UUID: ")
		).append(
				InitWrap.text().formatText(
					this.profile() != null
					? ProfileWrap.id(Objects.requireNonNull(this.profile())).toString()
					: "§c<NULL>"
				)
		);

		if (this.shadowPlayer != null)
		{
			text.append(
					InitWrap.text().formatText("§r\n - §7Shadow Player Name: ")
			).append(this.name());
		}
		else
		{
			text.append(
					InitWrap.text().formatText("§r\n - §cNO PLAYER")
			);
		}

		text.append(
				InitWrap.text().formatText("§r\n - §7Shadow: ")
		).append(
				InitWrap.text().formatText(this.shadowEnabled ? "§cEnabled" : "§aDisabled")
		);
		text.append(
				InitWrap.text().formatText("§r\n - §7Timeout: ")
		).append(
				InitWrap.text().formatText(this.shadowTimeout > 1L ? this.getShadowTimeoutString() : "§eN/A")
		);
		text.append(
				InitWrap.text().formatText("§r\n - §7Duration: ")
		).append(
				InitWrap.text().formatText(this.shadowStartTimeMs > 1L ? this.getShadowDurationString() : "§eN/A")
		);
		text.append(
				InitWrap.text().formatText("§r\n - §7Since: ")
		).append(
				InitWrap.text().formatText(this.shadowStartTimeEpoch > 1L ? this.getShadowTimeoutString() : "§eN/A")
		);
		text.append(
				InitWrap.text().formatText("§r\n - §7Reason: ")
		).append(
				InitWrap.text().formatText(this.reason.isEmpty() ? "§e<empty>" : this.reason())
		);

		return text;
	}
	public void reset()
	{
		this.handler.reset();
		this.shadowEnabled = false;
		this.shadowStartTimeMs = -1L;
		this.shadowStartTimeEpoch = -1L;
		this.shadowTimeout = -1L;
		this.reason = "";
	}
}
