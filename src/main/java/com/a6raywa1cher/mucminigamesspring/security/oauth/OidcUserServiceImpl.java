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

package com.a6raywa1cher.mucminigamesspring.security.oauth;

import com.a6raywa1cher.mucminigamesspring.security.SecurityConstants;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component("oidc-user-service")
public class OidcUserServiceImpl implements OAuth2UserService<OidcUserRequest, OidcUser> {
	private final OidcUserService oidcUserService = new OidcUserService();

	@Override
	public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
		OidcUser oidcUser = oidcUserService.loadUser(userRequest);
		if (!oidcUser.getEmailVerified()) {
			throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT));
		}
		Collection<? extends GrantedAuthority> oldAuthorities = oidcUser.getAuthorities();
		Optional<? extends GrantedAuthority> grantedAuthority = oldAuthorities.stream().filter(ga -> ga instanceof OidcUserAuthority).findAny();
		if (grantedAuthority.isEmpty()) throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR));
		OidcUserAuthority oidcUserAuthority = (OidcUserAuthority) grantedAuthority.get();
		Set<GrantedAuthority> newAuthorities = new HashSet<>();
		newAuthorities.add(new OidcUserAuthority(SecurityConstants.CONVERTIBLE, oidcUserAuthority.getIdToken(), oidcUserAuthority.getUserInfo()));
		return new DefaultOidcUser(newAuthorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
	}
}
