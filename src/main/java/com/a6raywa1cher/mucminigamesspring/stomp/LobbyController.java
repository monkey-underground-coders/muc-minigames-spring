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

package com.a6raywa1cher.mucminigamesspring.stomp;

import com.a6raywa1cher.mucminigamesspring.model.jpa.User;
import com.a6raywa1cher.mucminigamesspring.model.redis.Lobby;
import com.a6raywa1cher.mucminigamesspring.service.LobbyService;
import com.a6raywa1cher.mucminigamesspring.service.UserService;
import com.a6raywa1cher.mucminigamesspring.stomp.exceptions.DoubleLobbyEntryException;
import com.a6raywa1cher.mucminigamesspring.stomp.exceptions.ForbiddenException;
import com.a6raywa1cher.mucminigamesspring.stomp.exceptions.NotFoundException;
import com.a6raywa1cher.mucminigamesspring.stomp.request.LoginToLobbyRequest;
import com.a6raywa1cher.mucminigamesspring.stomp.request.OpenLobbyRequest;
import com.a6raywa1cher.mucminigamesspring.stomp.response.CurrentLobbyResponse;
import com.a6raywa1cher.mucminigamesspring.stomp.response.OpenLobbyResponse;
import com.a6raywa1cher.mucminigamesspring.utils.AuthenticationResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.security.Principal;

@Controller
@Slf4j
public class LobbyController {
	private final UserService userService;
	private final LobbyService lobbyService;
	private final AuthenticationResolver authenticationResolver;
	private final SimpMessagingTemplate template;

	@Autowired
	public LobbyController(UserService userService, LobbyService lobbyService, AuthenticationResolver authenticationResolver, SimpMessagingTemplate template) {
		this.userService = userService;
		this.lobbyService = lobbyService;
		this.authenticationResolver = authenticationResolver;
		this.template = template;
	}

	@MessageMapping("/lobby_control/open_lobby")
	@SendToUser("/queue/reply")
	@Transactional
	public OpenLobbyResponse openLobby(@Payload @Valid OpenLobbyRequest request, @Header("simpSessionId") String sessionId, Principal principal) {
		User user = authenticationResolver.getUser((Authentication) principal);
		lobbyService.getByHostUID(user.getId()).ifPresent(lobby -> lobbyService.closeLobby(lobby.getId()));
		Lobby lobby;
		if (StringUtils.hasLength(request.getPassword())) {
			lobby = lobbyService.create(user.getId(), sessionId, request.isVisible());
		} else {
			lobby = lobbyService.create(user.getId(), sessionId, request.isVisible(), request.getPassword());
		}
		return new OpenLobbyResponse(lobby.getId());
	}

	@MessageMapping("/lobby/{lid}/login")
	@SendToUser("/queue/reply")
	@SendTo("/lobby/{lid}")
	@Transactional
	public CurrentLobbyResponse loginToLobby(@DestinationVariable("lid") String lid,
											 @Payload @Valid LoginToLobbyRequest request,
											 @Header("simpSessionId") String sessionId,
											 Principal principal) {
		User user = authenticationResolver.getUser((Authentication) principal);
		Lobby lobby = lobbyService.getById(lid).orElseThrow(NotFoundException::new);
		if (lobby.isPasswordEnforced() && !lobby.getPassword().equals(request.getPassword())) {
			throw new ForbiddenException();
		}
		if (lobbyService.isConnectedToAnyLobby(user.getId())) {
			throw new DoubleLobbyEntryException();
		}
		Lobby out = lobbyService.appendUser(lobby, user, sessionId);
		return new CurrentLobbyResponse(out);
	}

	@MessageMapping("/lobby/{lid}/info")
	@SendTo("/lobby/{lid}")
	@Transactional
	public CurrentLobbyResponse getLobbyInfo(@DestinationVariable("lid") String lid,
											 Principal principal) {
		User user = authenticationResolver.getUser((Authentication) principal);
		Lobby lobby = lobbyService.getById(lid).orElseThrow(NotFoundException::new);
		return new CurrentLobbyResponse(lobby);
	}
}
