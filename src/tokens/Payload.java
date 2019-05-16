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

import java.util.Collection;
import java.util.HashMap;

public class Payload {
	private static final HashMap<String, Payload> payloads = new HashMap<>();
	public byte[] payload;
	public String identifier;

	public Payload(String identifier, byte[] payload) {
		this.identifier = identifier;
		this.payload = payload;
		payloads.put(identifier, this);
	}

	public static Payload payload(String name) {
		return payloads.get(name);
	}

	public static Collection<Payload> allPayloads() {
		return payloads.values();
	}
}