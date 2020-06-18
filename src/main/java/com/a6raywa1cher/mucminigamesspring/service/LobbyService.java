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

package com.a6raywa1cher.mucminigamesspring.service;

import com.a6raywa1cher.mucminigamesspring.model.jpa.User;
import com.a6raywa1cher.mucminigamesspring.model.redis.Lobby;

import java.util.List;
import java.util.Optional;

public interface LobbyService {
	Lobby create(long hostUID, String hostSimpSessionId, boolean visible);

	Lobby create(long hostUID, String hostSimpSessionId, boolean visible, String password);

	Optional<Lobby> getById(String id);

	Optional<Lobby> getByHostUID(long hostUID);

	Lobby appendUser(Lobby lobby, User user, String simpSessionId);

	void closeLobby(String lobbyId);

	Lobby driftHostTo(Lobby lobby, long userId);

	Lobby disconnectFromLobby(Lobby lobby, long userId);

	Lobby disconnectFromLobby(Lobby lobby, String simpSessionId);

	List<Lobby> disconnectFromLobbies(long userId, String simpSessionId);

	boolean isConnectedToAnyLobby(long userId);
}
