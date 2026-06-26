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

package com.sakuraryoko.unplugged_afk.impl.player;

import java.util.UUID;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import com.sakuraryoko.unplugged_afk.impl.modinit.InitWrap;
import com.sakuraryoko.unplugged_afk.impl.player.state.GameState;
import com.sakuraryoko.unplugged_afk.impl.player.state.PosState;
import com.sakuraryoko.unplugged_afk.impl.player.state.ShadowState;

@ApiStatus.Internal
public record PlayerEntry(UUID uuid, String name, ShadowState state, PosState pos, GameState game)
{
	public PlayerEntry updateState(ShadowState state)
	{
		return new PlayerEntry(this.uuid(), this.name(), state, this.pos(), this.game());
	}

	public PlayerEntry updatePosAndGame(PosState pos, GameState game)
	{
		return new PlayerEntry(this.uuid(), this.name(), this.state(), pos, game);
	}

	public PlayerEntry updatePlayerData(String name, PosState pos, GameState game)
	{
		return new PlayerEntry(this.uuid(), name, this.state(), pos, game);
	}

	public PlayerEntry updateAll(ShadowState state, PosState pos, GameState game)
	{
		return new PlayerEntry(this.uuid(), this.name(), state, pos, game);
	}

	@Override
	public @NonNull String toString()
	{
		return "[name="+this.name()+",uuid="+this.uuid().toString()+"],[state="+this.state().toString()+"],[pos=]"+this.pos().toString()+"[game="+this.game().toString()+"]";
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }
		PlayerEntry playerEntry = (PlayerEntry) o;

		if (this.uuid.equals(playerEntry.uuid))
		{
			return this.name().equals(playerEntry.name()) && this.game().equals(playerEntry.game()) && this.state().equals(playerEntry.state()) && this.pos().equals(playerEntry.pos());
		}

		return false;
	}

	@Override
	public int hashCode()
	{
		int result = this.uuid.hashCode();
		result = 31 * result + this.name().hashCode();
		result = 31 * result + this.state().hashCode();
		result = 31 * result + this.pos().hashCode();
		result = 31 * result + this.game().hashCode();
		return result;
	}

	public boolean matches(UUID uuid)
	{
		return this.uuid.equals(uuid);
	}

	public Component getDebugFormatted()
	{
		MutableComponent text = Component.literal("");

		text.append(
				InitWrap.text().formatText("\n - §7Name: ")
		).append(
				InitWrap.text().formatText(this.name())
		).append(
				InitWrap.text().formatText("\n - §7UUID: ")
		).append(
				InitWrap.text().formatText(this.uuid().toString())
		).append(
				InitWrap.text().formatText("\n - §7Game: ")
		).append(
				this.game().getDebugFormatted()
		).append(
				InitWrap.text().formatText("\n - §7Position: ")
		).append(
				this.pos().getDebugFormatted()
		).append(
				InitWrap.text().formatText("\n - §7Shadow: ")
		).append(
				this.state().getDebugFormatted()
		);

		return text;
	}
}
