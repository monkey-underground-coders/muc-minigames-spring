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

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

// https://stackoverflow.com/questions/45405332/websocket-authentication-and-authorization-in-spring
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketAuthenticationSecurityConfig implements WebSocketMessageBrokerConfigurer {
	private final AuthChannelInterceptorAdapter authChannelInterceptorAdapter;

	public WebSocketAuthenticationSecurityConfig(AuthChannelInterceptorAdapter authChannelInterceptorAdapter) {
		this.authChannelInterceptorAdapter = authChannelInterceptorAdapter;
	}

	@Override
	public void registerStompEndpoints(final StompEndpointRegistry registry) {
		// Endpoints are already registered on WebSocketConfig, no need to add more.
	}

	@Override
	public void configureClientInboundChannel(final ChannelRegistration registration) {
		registration.interceptors(authChannelInterceptorAdapter);
	}

}