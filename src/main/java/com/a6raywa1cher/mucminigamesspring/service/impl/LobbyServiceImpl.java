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

package com.a6raywa1cher.mucminigamesspring.service.impl;

import com.a6raywa1cher.mucminigamesspring.model.redis.Lobby;
import com.a6raywa1cher.mucminigamesspring.model.redis.LobbyStatus;
import com.a6raywa1cher.mucminigamesspring.model.redis.repo.LobbyRepository;
import com.a6raywa1cher.mucminigamesspring.service.LobbyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class LobbyServiceImpl implements LobbyService {
	private final LobbyRepository lobbyRepository;

	@Autowired
	public LobbyServiceImpl(LobbyRepository lobbyRepository) {
		this.lobbyRepository = lobbyRepository;
	}

	@Override
	public Lobby create(long hostUID, String hostSimpSessionId, boolean visible) {
		return create(hostUID, hostSimpSessionId, visible, "");
	}

	@Override
	public Lobby create(long hostUID, String hostSimpSessionId, boolean visible, String password) {
		Lobby lobby = new Lobby();
		lobby.setHostUID(hostUID);
		lobby.setVisible(visible);
		lobby.setPassword(password);
		lobby.setPlayers(Collections.singletonList(hostUID));
		lobby.setLobbyStatus(LobbyStatus.IN_LOBBY);
		lobby.setHostSimpSessionId(hostSimpSessionId);
		return lobbyRepository.save(lobby);
	}

	@Override
	public Optional<Lobby> getById(String id) {
		return lobbyRepository.findById(id);
	}

	@Override
	public Optional<Lobby> getByHostUID(long hostUID) {
		return lobbyRepository.findByHostUID(hostUID);
	}

	@Override
	public void closeLobby(String lobbyId) {
		lobbyRepository.deleteById(lobbyId);
	}

	@Override
	public List<Lobby> closeAllByHostSimpSessionId(String hostSimpSessionId) {
		List<Lobby> lobbies = lobbyRepository.findAllByHostSimpSessionId(hostSimpSessionId);
		lobbyRepository.deleteAll(lobbies);
		return lobbies;
	}
}
