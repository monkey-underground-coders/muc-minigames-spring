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

package com.a6raywa1cher.mucminigamesspring.security;

import com.a6raywa1cher.mucminigamesspring.model.jpa.User;
import com.a6raywa1cher.mucminigamesspring.model.redis.Lobby;
import com.a6raywa1cher.mucminigamesspring.service.LobbyService;
import com.a6raywa1cher.mucminigamesspring.utils.AuthenticationResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MvcAccessChecker {
	private final LobbyService lobbyService;
	private final AuthenticationResolver authenticationResolver;

	@Autowired
	public MvcAccessChecker(LobbyService lobbyService, AuthenticationResolver authenticationResolver) {
		this.lobbyService = lobbyService;
		this.authenticationResolver = authenticationResolver;
	}

	public boolean checkLid(Authentication authentication, String lid) {
		User user = authenticationResolver.getUser(authentication);
		Optional<Lobby> byId = lobbyService.getById(lid);
		return byId.map(lobby -> lobby.getPlayers().contains(user.getId())).orElse(false);
	}
}
