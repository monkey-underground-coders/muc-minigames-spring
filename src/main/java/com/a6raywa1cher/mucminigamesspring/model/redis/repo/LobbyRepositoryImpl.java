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

package com.a6raywa1cher.mucminigamesspring.model.redis.repo;

import com.a6raywa1cher.mucminigamesspring.model.redis.Lobby;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Component
public class LobbyRepositoryImpl implements LobbyRepository {
	private final InnerLobbyRepository innerLobbyRepository;

	@Autowired
	public LobbyRepositoryImpl(InnerLobbyRepository innerLobbyRepository) {
		this.innerLobbyRepository = innerLobbyRepository;
	}

	@Override
	public Optional<Lobby> findByHostUID(long hostUID) {
		return innerLobbyRepository.findByHostUID(hostUID);
	}

	@Override
	public List<Lobby> findAllByPlayerContaining(Long playerId, String playerSimpSessionId) {
		List<Lobby> lobbies = new LinkedList<>();
		lobbies.addAll(innerLobbyRepository.findAllByPlayer1IdAndPlayer1SimpSessionId(playerId, playerSimpSessionId));
		lobbies.addAll(innerLobbyRepository.findAllByPlayer2IdAndPlayer2SimpSessionId(playerId, playerSimpSessionId));
		return lobbies;
	}

	@Override
	public List<Lobby> findAllByPlayerContaining(Long playerId) {
		return innerLobbyRepository.findAllByPlayer1IdOrPlayer2Id(playerId, playerId);
	}

	@Override
	public Iterable<Lobby> findAll(Sort sort) {
		return innerLobbyRepository.findAll(sort);
	}

	@Override
	public Page<Lobby> findAll(Pageable pageable) {
		return innerLobbyRepository.findAll(pageable);
	}

	@Override
	public <S extends Lobby> S save(S entity) {
		return innerLobbyRepository.save(entity);
	}

	@Override
	public <S extends Lobby> Iterable<S> saveAll(Iterable<S> entities) {
		return innerLobbyRepository.saveAll(entities);
	}

	@Override
	public Optional<Lobby> findById(String s) {
		return innerLobbyRepository.findById(s);
	}

	@Override
	public boolean existsById(String s) {
		return innerLobbyRepository.existsById(s);
	}

	@Override
	public Iterable<Lobby> findAll() {
		return innerLobbyRepository.findAll();
	}

	@Override
	public Iterable<Lobby> findAllById(Iterable<String> strings) {
		return innerLobbyRepository.findAllById(strings);
	}

	@Override
	public long count() {
		return innerLobbyRepository.count();
	}

	@Override
	public void deleteById(String s) {
		innerLobbyRepository.deleteById(s);
	}

	@Override
	public void delete(Lobby entity) {
		innerLobbyRepository.delete(entity);
	}

	@Override
	public void deleteAll(Iterable<? extends Lobby> entities) {
		innerLobbyRepository.deleteAll(entities);
	}

	@Override
	public void deleteAll() {
		innerLobbyRepository.deleteAll();
	}
}
