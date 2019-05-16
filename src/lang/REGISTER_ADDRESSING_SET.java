/*
 * Copyright (C) 2018-2019  Dinu Blanovschi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package lang;

public class REGISTER_ADDRESSING_SET {
	public REGISTER x64;
	public REGISTER x32;
	public REGISTER x16;
	public REGISTER x8;

	public REGISTER_ADDRESSING_SET(REGISTER x64, REGISTER x32, REGISTER x16, REGISTER x8) {
		this.x64 = x64;
		this.x32 = x32;
		this.x16 = x16;
		this.x8 = x8;
		x64.setAddressing(this);
		x32.setAddressing(this);
		x16.setAddressing(this);
		x8.setAddressing(this);
	}
}
