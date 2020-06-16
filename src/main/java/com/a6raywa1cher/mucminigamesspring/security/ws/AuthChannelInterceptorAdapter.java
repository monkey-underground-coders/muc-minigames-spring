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

package com.a6raywa1cher.mucminigamesspring.security.ws;

import com.a6raywa1cher.mucminigamesspring.security.authentication.JwtAuthentication;
import com.a6raywa1cher.mucminigamesspring.security.jwt.JwtToken;
import com.a6raywa1cher.mucminigamesspring.security.jwt.service.JwtTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;

// https://stackoverflow.com/questions/45405332/websocket-authentication-and-authorization-in-spring
@Component
public class AuthChannelInterceptorAdapter implements ChannelInterceptor {
	private final AuthenticationManager authenticationManager;
	private final JwtTokenService jwtTokenService;

	@Autowired
	public AuthChannelInterceptorAdapter(AuthenticationManager authenticationManager, JwtTokenService jwtTokenService) {
		this.authenticationManager = authenticationManager;
		this.jwtTokenService = jwtTokenService;
	}

	@Override
	public Message<?> preSend(final Message<?> message, final MessageChannel channel) throws AuthenticationException {
		final StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

		if (accessor != null && StompCommand.CONNECT == accessor.getCommand()) {
			final String password = accessor.getPasscode();
			Optional<JwtToken> jwtToken = jwtTokenService.decode(password);
			if (jwtToken.isEmpty()) {
				throw new BadCredentialsException("Broken jwt");
			}
			final JwtAuthentication jwtAuthentication = new JwtAuthentication(Collections.emptyList(), jwtToken.get());
			final Authentication user = authenticationManager.authenticate(jwtAuthentication);

			accessor.setUser(user);
		}
		return message;
	}
}