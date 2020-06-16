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

package com.a6raywa1cher.mucminigamesspring.security.jwt;

import com.a6raywa1cher.mucminigamesspring.security.authentication.JwtAuthentication;
import com.a6raywa1cher.mucminigamesspring.security.jwt.service.JwtTokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtTokenService jwtTokenService;
	private final AuthenticationManager authenticationManager;
	private static final String AUTHORIZATION_HEADER = "Authorization";

	public JwtAuthenticationFilter(JwtTokenService jwtTokenService, AuthenticationManager authenticationManager) {
		this.jwtTokenService = jwtTokenService;
		this.authenticationManager = authenticationManager;
	}

	private Authentication check(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
		if (request.getHeader(AUTHORIZATION_HEADER) == null) {
			return null;
		}
		String lowerCase = request.getHeader(AUTHORIZATION_HEADER).toLowerCase();
		String token;
		if (lowerCase.startsWith("jwt ") || lowerCase.startsWith("bearer ")) {
			token = request.getHeader(AUTHORIZATION_HEADER).split(" ")[1];
		} else {
			return null;
		}
		Optional<JwtToken> jwtBody = jwtTokenService.decode(token);
		if (jwtBody.isEmpty()) {
			throw new BadCredentialsException("Broken JWT or an unknown sign key");
		} else {
			return new JwtAuthentication(
					Collections.emptyList(),
					jwtBody.get()
			);
		}
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		try {
			Authentication authentication = check(request, response);
			if (authentication != null) {
				Authentication auth = authenticationManager.authenticate(authentication);
				SecurityContextHolder.createEmptyContext();
				SecurityContextHolder.getContext().setAuthentication(auth);
			}
		} catch (AuthenticationException e) {
			SecurityContextHolder.clearContext();
			logger.debug("AuthenticationException on jwt", e);
			response.setStatus(403);
			return;
		}
		filterChain.doFilter(request, response);
	}
}