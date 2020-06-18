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

package com.a6raywa1cher.mucminigamesspring.stomp.response;

import com.a6raywa1cher.mucminigamesspring.model.redis.Lobby;
import com.a6raywa1cher.mucminigamesspring.model.redis.LobbyStatus;
import lombok.Data;
import org.springframework.data.util.Pair;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class CurrentLobbyResponse {
	private String id;

	private long hostUID;

	private boolean visible;

	private boolean passwordProtected;

	private List<Long> players;

	private LobbyStatus lobbyStatus;

	public CurrentLobbyResponse(Lobby lobby) {
		this.id = lobby.getId();
		this.hostUID = lobby.getHostUID();
		this.visible = lobby.isVisible();
		this.passwordProtected = StringUtils.hasLength(lobby.getPassword());
		this.players = lobby.getPlayers().stream()
				.map(Pair::getFirst)
				.collect(Collectors.toList());
		this.lobbyStatus = lobby.getLobbyStatus();
	}
}
