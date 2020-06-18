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

import com.a6raywa1cher.mucminigamesspring.model.jpa.User;
import com.a6raywa1cher.mucminigamesspring.model.redis.Lobby;
import com.a6raywa1cher.mucminigamesspring.model.redis.LobbyStatus;
import com.a6raywa1cher.mucminigamesspring.model.redis.repo.LobbyRepository;
import com.a6raywa1cher.mucminigamesspring.service.LobbyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

	private Lobby $appendUser(Lobby lobby, long userId, String simpSessionId) {
		List<Pair<Long, String>> pairs = new ArrayList<>(lobby.getPlayers());
		pairs.add(Pair.of(userId, simpSessionId));
		lobby.setPlayers(pairs);
		return lobbyRepository.save(lobby);
	}

	private Lobby $removeUser(Lobby lobby, long userId, String simpSessionId) {
		List<Pair<Long, String>> pairs = new ArrayList<>(lobby.getPlayers());
		pairs.remove(Pair.of(userId, simpSessionId));
		lobby.setPlayers(pairs);
		return lobbyRepository.save(lobby);
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
		lobby.setPlayers(Collections.singletonList(Pair.of(hostUID, hostSimpSessionId)));
		lobby.setLobbyStatus(LobbyStatus.IN_LOBBY);
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
	public Lobby appendUser(Lobby lobby, User user, String simpSessionId) {
		return $appendUser(lobby, user.getId(), simpSessionId);
	}

	@Override
	public void closeLobby(String lobbyId) {
		lobbyRepository.deleteById(lobbyId);
	}

	@Override
	public Lobby driftHostTo(Lobby lobby, long userId) {
		if (lobby.getPlayers().stream().anyMatch(p -> p.getFirst().equals(userId))) {
			lobby.setHostUID(userId);
			return lobbyRepository.save(lobby);
		} else {
			throw new IllegalArgumentException("Transfer host to a non-related user denied");
		}
	}

	private Lobby disconnectFromLobby(Lobby lobby, long userId, String simpSessionId) {
		if (userId == lobby.getHostUID()) {
			// try to find successor, if any player present
			Optional<Pair<Long, String>> otherPlayer = lobby.getPlayers().stream()
					.filter(p -> !p.equals(Pair.of(userId, simpSessionId)))
					.findAny();
			if (otherPlayer.isPresent()) {
				Lobby lobby1 = driftHostTo(lobby, otherPlayer.get().getFirst());
				return $removeUser(lobby1, userId, simpSessionId);
			} else {
				closeLobby(lobby.getId());
				lobby.setLobbyStatus(LobbyStatus.CLOSED);
				return lobby;
			}
		} else {
			return $removeUser(lobby, userId, simpSessionId);
		}
	}

	@Override
	public Lobby disconnectFromLobby(Lobby lobby, String simpSessionId) {
		Pair<Long, String> player = lobby.getPlayers().stream()
				.filter(p -> p.getSecond().equals(simpSessionId))
				.findAny().orElseThrow(() -> new IllegalArgumentException("Can't disconnect from non-related lobby"));
		return disconnectFromLobby(lobby, player.getFirst(), player.getSecond());
	}

	@Override
	public List<Lobby> disconnectFromLobbies(long userId, String simpSessionId) {
		List<Lobby> lobbies = lobbyRepository.findAllByPlayerContaining(userId, simpSessionId);
		lobbies.forEach(l -> disconnectFromLobby(l, userId, simpSessionId));
		return lobbies;
	}

	@Override
	public boolean isConnectedToAnyLobby(long userId) {
		return lobbyRepository.findAllByPlayerContaining(userId).size() > 0;
	}

	@Override
	public Lobby disconnectFromLobby(Lobby lobby, long userId) {
		Pair<Long, String> player = lobby.getPlayers().stream()
				.filter(p -> p.getFirst().equals(userId))
				.findAny().orElseThrow(() -> new IllegalArgumentException("Can't disconnect from non-related lobby"));
		return disconnectFromLobby(lobby, player.getFirst(), player.getSecond());
	}
}
