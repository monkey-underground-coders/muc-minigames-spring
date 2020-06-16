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

package com.a6raywa1cher.mucminigamesspring.utils;

import com.a6raywa1cher.mucminigamesspring.model.VendorId;
import com.a6raywa1cher.mucminigamesspring.model.jpa.User;
import com.a6raywa1cher.mucminigamesspring.security.authentication.CustomAuthentication;
import com.a6raywa1cher.mucminigamesspring.service.UserService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationResolverImpl implements AuthenticationResolver {
	private final UserService userService;

	public AuthenticationResolverImpl(UserService userService) {
		this.userService = userService;
	}

	@Override
	public User getUser() throws AuthenticationException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return getUser(authentication);
	}

	@Override
	public User getUser(Authentication authentication) throws AuthenticationException {
		if (authentication == null) {
			throw new BadCredentialsException("No credentials presented");
		}
		if (authentication instanceof CustomAuthentication) {
			CustomAuthentication customAuthentication = (CustomAuthentication) authentication;
			return userService.getById(customAuthentication.getPrincipal()).orElseThrow();
		} else if (authentication instanceof OAuth2AuthenticationToken) {
			OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
			VendorId vendorId = VendorId.valueOf(token.getAuthorizedClientRegistrationId().toUpperCase());
			String id = token.getPrincipal().getAttribute("sub");
			String email = token.getPrincipal().getAttribute("email");
			return userService.getByVendorIdOrEmail(vendorId, id, email).orElseThrow();
		} else if (authentication instanceof UsernamePasswordAuthenticationToken) {
			UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
			return userService.getById((Long) token.getPrincipal()).orElseThrow();
		}
		throw new AuthenticationResolveException("Unknown Authentication " + authentication.getClass().getCanonicalName());
	}
}
