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

import com.a6raywa1cher.mucminigamesspring.model.jpa.repo.UserRepository;
import com.a6raywa1cher.mucminigamesspring.model.VendorId;
import com.a6raywa1cher.mucminigamesspring.model.jpa.User;
import com.a6raywa1cher.mucminigamesspring.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
	private final UserService userService;
	private final UserRepository userRepository;

	public CustomAuthenticationSuccessHandler(UserService userService, UserRepository userRepository) {
		this.userService = userService;
		this.userRepository = userRepository;
	}

	private VendorId getVendorId(OAuth2AuthenticationToken authentication) {
		return VendorId.valueOf(authentication.getAuthorizedClientRegistrationId().toUpperCase());
	}

	private User getUserOrRegister(OAuth2AuthenticationToken authentication) {
		OAuth2User oAuth2User = authentication.getPrincipal();
		VendorId vendor = getVendorId(authentication);
		String email = oAuth2User.getAttribute("email");
		String id = oAuth2User.getAttribute("sub");
		Optional<User> optionalUser = userService.getByVendorIdOrEmail(vendor, id, email);
		User user;
		if (optionalUser.isEmpty()) { // if that's a new user, register him
			user = new User();
			switch (vendor) {
				case GOOGLE:
					user.setGoogleId(id);
					break;
				case VK:
					user.setVkId(id);
					break;
			}
			user.setLastVisit(ZonedDateTime.now());
			user.setPicture(oAuth2User.getAttribute("picture"));
			String inputName = oAuth2User.getAttribute("name");
			user.setName(userService.isNameAvailable(inputName) ? inputName : UUID.randomUUID().toString());
			user.setEmail(email);
			user.setCreatedAt(ZonedDateTime.now());
			userRepository.save(user);
		} else { // or else check email collisions
			user = optionalUser.get();
			switch (vendor) {
				case GOOGLE:
					if (user.getGoogleId() == null) {
						user.setGoogleId(id);
						userRepository.save(user);
					}
					break;
				case VK:
					if (user.getGoogleId() == null) {
						user.setVkId(id);
						userRepository.save(user);
					}
					break;
			}
		}
		return user;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
		if (authentication instanceof OAuth2AuthenticationToken) { // register user
			OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
			getUserOrRegister(token);
//			JwtToken jwt = jwtTokenService.issue(getVendorId(token), oAuth2User.getAttribute("sub"), user.getId());
//			CustomAuthentication customAuthentication = new CustomAuthentication(
//					authentication.getAuthorities(), jwt
//			);
//			SecurityContextHolder.getContext().setAuthentication(customAuthentication);
//			newAuthentication = customAuthentication;
//			response.setHeader("MHS-JWTToken", jwt.getToken());
//
//			RefreshToken refreshToken = refreshTokenService.issue(user);
//			response.setHeader("MHS-RefreshToken", refreshToken.getToken());
		}
		super.onAuthenticationSuccess(request, response, authentication);
	}
}
