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
import com.a6raywa1cher.mucminigamesspring.service.UserService;
import com.a6raywa1cher.mucminigamesspring.utils.AuthenticationResolveException;
import com.a6raywa1cher.mucminigamesspring.utils.AuthenticationResolver;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZonedDateTime;

public class LastVisitFilter extends OncePerRequestFilter {
	private final UserService service;
	private final AuthenticationResolver resolver;

	public LastVisitFilter(UserService service, AuthenticationResolver resolver) {
		this.service = service;
		this.resolver = resolver;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		try {
			filterChain.doFilter(request, response);
		} finally {
			try {
				if (SecurityContextHolder.getContext().getAuthentication() != null) {
					User user = resolver.getUser();
					if (user.getLastVisit().plusSeconds(30).isBefore(ZonedDateTime.now()))
						service.setLastVisit(user, ZonedDateTime.now());
				}
			} catch (AuthenticationResolveException ignored) {

			} catch (Exception e) {
				logger.error("Error while setting last visit", e);
			}
		}
	}
}
