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

import java.util.Objects;
import org.jspecify.annotations.NonNull;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import com.sakuraryoko.afkme.impl.modinit.InitWrap;

public record GameState(String gameMode, boolean flying)
{
	@Override
	public @NonNull String toString()
	{
		return "gameType="+this.gameMode+",flying="+this.flying;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }
		GameState gameState = (GameState) o;
		return Objects.equals(this.gameMode, gameState.gameMode) && this.flying == gameState.flying;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(this.gameMode, this.flying);
	}

	public Component getDebugFormatted()
	{
		MutableComponent text = Component.literal("");

		text.append(
				InitWrap.text().formatText("§rGM: ")
		).append(
				InitWrap.text().formatText(this.gameMode())
		).append(
				InitWrap.text().formatText("§r / F: ")
		).append(
				InitWrap.text().formatText(
						String.format("§e%s§r", this.flying())
				)
		).append(
				InitWrap.text().formatText("§r")
		);

		return text;
	}

}
