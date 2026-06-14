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

package com.sakuraryoko.afkme.impl.config.data.options;

import org.jetbrains.annotations.ApiStatus;

import com.sakuraryoko.corelib.api.config.IConfigOption;
import com.sakuraryoko.corelib.api.time.DurationFormat;
import com.sakuraryoko.corelib.api.time.DurationOption;
import com.sakuraryoko.corelib.api.time.TimeDateOption;
import com.sakuraryoko.corelib.api.time.TimeFormat;

@ApiStatus.Internal
public class MessageOptions implements IConfigOption
{
	public boolean broadcastMessages;
	public boolean displayDuration;
	public String defaultShadowReason;
	public String shadowKickMessage;
	public String shadowExpiredReason;
	public String shadowStarted;
	public String shadowPunctuation;
	public String shadowReturned;
	public String whenReturnDurationPrefix;
	public String whenReturnDurationSuffix;
	public DurationOption duration;
	public TimeDateOption timeDate;

	public MessageOptions()
	{
		this.defaults();
	}

	@Override
	public void defaults()
	{
		this.broadcastMessages = false;
		this.displayDuration = true;
		this.defaultShadowReason = "§7shadowing§r";
		this.shadowKickMessage = "§6Shadow activation§r";
		this.shadowExpiredReason = "§eYou didn't come back in time§r";
		this.shadowStarted = "§r §eis now shadowed§r";
		this.shadowPunctuation = "§e,§r ";
		this.shadowReturned = "§r §eis no longer shadowed§r";
		this.whenReturnDurationPrefix = " §7(Gone for: §a";
		this.whenReturnDurationSuffix = "§7)";
		this.duration = new DurationOption();
		this.duration.option = DurationFormat.PRETTY;
		this.timeDate = new TimeDateOption();
		this.timeDate.option = TimeFormat.RFC1123;
	}

	@Override
	public MessageOptions copy(IConfigOption opt)
	{
		MessageOptions opts = (MessageOptions) opt;

		this.broadcastMessages = opts.broadcastMessages;
		this.displayDuration = opts.displayDuration;
		this.defaultShadowReason = opts.defaultShadowReason;
		this.shadowKickMessage = opts.shadowKickMessage;
		this.shadowExpiredReason = opts.shadowExpiredReason;
		this.shadowStarted = opts.shadowStarted;
		this.shadowPunctuation = opts.shadowPunctuation;
		this.shadowReturned = opts.shadowReturned;
		this.whenReturnDurationPrefix = !opts.whenReturnDurationPrefix.isEmpty() ? opts.whenReturnDurationPrefix : " §7(Gone for: §a";
		this.whenReturnDurationSuffix = !opts.whenReturnDurationSuffix.isEmpty() ? opts.whenReturnDurationSuffix : "§7)";
		this.duration.copy(opts.duration);
		this.timeDate.copy(opts.timeDate);

		return this;
	}
}
