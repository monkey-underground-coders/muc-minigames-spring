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

package com.a6raywa1cher.mucminigamesspring.security.providers;

import com.a6raywa1cher.mucminigamesspring.security.authentication.CustomAuthentication;
import com.a6raywa1cher.mucminigamesspring.security.authentication.JwtAuthentication;
import com.a6raywa1cher.mucminigamesspring.security.jwt.JwtToken;
import com.a6raywa1cher.mucminigamesspring.service.UserService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class JwtAuthenticationProvider implements AuthenticationProvider {
	private final UserService userService;

	public JwtAuthenticationProvider(UserService userService) {
		this.userService = userService;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		if (!supports(authentication.getClass())) {
			return null;
		}
		JwtAuthentication customAuthentication = (JwtAuthentication) authentication;
		JwtToken jwtToken = customAuthentication.getCredentials();
		if (jwtToken == null) {
			customAuthentication.setAuthenticated(false);
			throw new BadCredentialsException("JwtToken not provided");
		}
		Long userId = jwtToken.getUid();
		if (userService.getById(userId).isEmpty()) {
			customAuthentication.setAuthenticated(false);
			throw new UsernameNotFoundException(String.format("User %d doesn't exists", userId));
		}
		return new CustomAuthentication(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
				jwtToken);
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return JwtAuthentication.class.isAssignableFrom(authentication);
	}
}
