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

import com.a6raywa1cher.mucminigamesspring.model.redis.Lobby;
import com.a6raywa1cher.mucminigamesspring.service.LobbyService;
import com.a6raywa1cher.mucminigamesspring.stomp.response.CurrentLobbyResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class DisconnectApplicationListener implements ApplicationListener<SessionDisconnectEvent> {
	private static final Logger log = LoggerFactory.getLogger(DisconnectApplicationListener.class);
	private final Map<String, Boolean> handling;
	private final LobbyService lobbyService;
	private final SimpMessagingTemplate template;

	@Autowired
	public DisconnectApplicationListener(LobbyService lobbyService, SimpMessagingTemplate template) {
		this.lobbyService = lobbyService;
		this.template = template;
		this.handling = new ConcurrentHashMap<>();
	}

	private synchronized boolean check(SessionDisconnectEvent event) {
		if (event.getUser() == null || event.getUser().getName() == null || handling.containsKey(event.getUser().getName())) {
			return false;
		}
		handling.put(event.getUser().getName(), true);
		return true;
	}

	@Override
	@Transactional
	public void onApplicationEvent(SessionDisconnectEvent event) {
		String name = (event.getUser() == null ? "unknown" : event.getUser().getName());
		String simpSessionId = event.getSessionId();
		log.debug("User disconnected, username:{}, simpSessionId:{}", name, simpSessionId);
		if (!check(event)) {
			return;
		}
		assert event.getUser() != null;
		log.info("Starting closing lobbies for username:{}, simpSessionId:{}", name, simpSessionId);
		try {
			List<Lobby> lobbies = lobbyService.closeAllByHostSimpSessionId(simpSessionId);
			log.info("simpSessionId:{} connected with these lobbies:{}", simpSessionId, lobbies.stream()
					.map(Lobby::getId)
					.collect(Collectors.joining(",")));
			for (Lobby lobby : lobbies) {
				template.convertAndSend(String.format("/lobby/%s", lobby.getId()),
						new CurrentLobbyResponse(lobby));
			}
		} finally {
			log.info("Completed closing lobbies for username " + name);
			handling.remove(name);
		}
	}
}
