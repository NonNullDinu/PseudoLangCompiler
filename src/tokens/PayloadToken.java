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

package tokens;

public class PayloadToken extends Token {
	public Payload payload;
	private String name;

	public PayloadToken(String name) {
		this.name = name;
	}

	public void bind() {
		if (this.payload == null)
			this.payload = Payload.payload(name);
	}

	@Override
	public String toString() {
		return "PT(" + name + ")";
	}
}
