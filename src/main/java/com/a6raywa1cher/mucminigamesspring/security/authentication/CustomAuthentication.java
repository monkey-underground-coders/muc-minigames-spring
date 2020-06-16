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

package com.a6raywa1cher.mucminigamesspring.security.authentication;

import com.a6raywa1cher.mucminigamesspring.security.jwt.JwtToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class CustomAuthentication implements Authentication {
	private final Collection<? extends GrantedAuthority> authorities;
	private final Long userId;
	private final Object credentials;
	private boolean authenticated = true;

	public CustomAuthentication(Collection<? extends GrantedAuthority> authorities, JwtToken jwtToken) {
		this.authorities = authorities;
		this.credentials = jwtToken;
		this.userId = jwtToken.getUid();
	}

	public CustomAuthentication(Collection<? extends GrantedAuthority> authorities, Long userId, UsernamePasswordAuthenticationToken token) {
		this.authorities = authorities;
		this.credentials = token;
		this.userId = userId;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public Object getCredentials() {
		return credentials;
	}

	@Override
	public Object getDetails() {
		return null;
	}

	@Override
	public Long getPrincipal() {
		return userId;
	}

	@Override
	public boolean isAuthenticated() {
		return authenticated;
	}

	@Override
	public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
		if (isAuthenticated) {
			throw new IllegalArgumentException();
		}
		authenticated = false;
	}

	@Override
	public String getName() {
		return Long.toString(userId);
	}
}
