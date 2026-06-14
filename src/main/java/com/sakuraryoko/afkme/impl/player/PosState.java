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

import org.jspecify.annotations.NonNull;

public record PosState(String location, int x, int y, int z, float yaw, float pitch)
{
	@Override
	public @NonNull String toString()
	{
		return "dim="+this.location()+", [x="+this.x()+",y="+this.y()+",z="+this.z()+",yaw="+this.yaw()+",pitch="+this.pitch()+"]";
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }
		PosState posState = (PosState) o;

		if (this.location().equals(posState.location()))
		{
			return  this.x() == posState.x() && this.y() == posState.y() && this.z() == posState.z() &&
					this.yaw() == posState.yaw() && this.pitch() == posState.pitch();
		}

		return false;
	}

	@Override
	public int hashCode()
	{
		int result = this.location().hashCode();
		result = 31 * result + this.x();
		result = 31 * result + this.y();
		result = 31 * result + this.z();
		result = 31 * result + Float.floatToIntBits(this.yaw());
		result = 31 * result + Float.floatToIntBits(this.pitch());
		return result;
	}
}
