/*
 * muc-minigames-spring
 * Copyright (C) 2020 Monkey Underground Coders and
 * Konstantin "6rayWa1cher" Grigorev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.a6raywa1cher.mucminigamesspring.model.redis;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;
import org.springframework.data.util.Pair;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@RedisHash("Lobby")
@Data
public class Lobby {
	@Id
	private String id;

	@Indexed
	private long hostUID;

	private boolean visible;

	private String password;

	@Indexed
	private Long player1Id;

	@Indexed
	private String player1SimpSessionId;

	@Indexed
	private Long player2Id;

	@Indexed
	private String player2SimpSessionId;

	private LobbyStatus lobbyStatus;

	public boolean isPasswordEnforced() {
		return StringUtils.hasLength(password);
	}

	public List<Pair<Long, String>> getPlayers() {
		List<Pair<Long, String>> list = new ArrayList<>();
		if (player1Id != null) {
			list.add(Pair.of(player1Id, player1SimpSessionId));
		}
		if (player2Id != null) {
			list.add(Pair.of(player2Id, player2SimpSessionId));
		}
		return list;
	}

	public void setPlayers(List<Pair<Long, String>> players) {
		if (players.size() > 2) throw new IllegalArgumentException("Players must be less than 2");

		if (players.size() >= 1) {
			player1Id = players.get(0).getFirst();
			player1SimpSessionId = players.get(0).getSecond();
		} else {
			player1Id = null;
			player1SimpSessionId = null;
		}
		if (players.size() >= 2) {
			player2Id = players.get(1).getFirst();
			player2SimpSessionId = players.get(1).getSecond();
		} else {
			player2Id = null;
			player2SimpSessionId = null;
		}
	}
}
