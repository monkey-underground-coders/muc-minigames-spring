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

package com.a6raywa1cher.mucminigamesspring.security.rest;

import com.a6raywa1cher.mucminigamesspring.model.VendorId;
import com.a6raywa1cher.mucminigamesspring.model.jpa.User;
import com.a6raywa1cher.mucminigamesspring.security.jwt.JwtRefreshPair;
import com.a6raywa1cher.mucminigamesspring.security.jwt.JwtToken;
import com.a6raywa1cher.mucminigamesspring.security.jwt.service.JwtRefreshPairService;
import com.a6raywa1cher.mucminigamesspring.security.jwt.service.JwtTokenService;
import com.a6raywa1cher.mucminigamesspring.security.jwt.service.RefreshTokenService;
import com.a6raywa1cher.mucminigamesspring.security.model.jpa.RefreshToken;
import com.a6raywa1cher.mucminigamesspring.security.rest.req.GetNewJwtTokenRequest;
import com.a6raywa1cher.mucminigamesspring.security.rest.req.InvalidateTokenRequest;
import com.a6raywa1cher.mucminigamesspring.security.rest.req.LinkSocialAccountsRequest;
import com.a6raywa1cher.mucminigamesspring.service.UserService;
import com.a6raywa1cher.mucminigamesspring.utils.AuthenticationResolver;
import com.a6raywa1cher.mucminigamesspring.utils.Views;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/auth")
public class AuthController {
	private final RefreshTokenService refreshTokenService;

	private final JwtTokenService jwtTokenService;

	private final AuthenticationResolver authenticationResolver;

	private final JwtRefreshPairService jwtRefreshPairService;

	private final UserService userService;

	public AuthController(AuthenticationResolver authenticationResolver, RefreshTokenService refreshTokenService,
						  JwtTokenService jwtTokenService, JwtRefreshPairService jwtRefreshPairService, UserService userService) {
		this.authenticationResolver = authenticationResolver;
		this.refreshTokenService = refreshTokenService;
		this.jwtTokenService = jwtTokenService;
		this.jwtRefreshPairService = jwtRefreshPairService;
		this.userService = userService;
	}

	@GetMapping("/user")
	@JsonView(Views.Internal.class)
	@Operation(security = @SecurityRequirement(name = "jwt"))
	public ResponseEntity<User> getCurrentUser(@Parameter(hidden = true) User user) {
		return ResponseEntity.ok(user);
	}

	@GetMapping("/convert")
	@Operation(security = @SecurityRequirement(name = "jwt"))
	public ResponseEntity<JwtRefreshPair> convertToJwt(HttpServletRequest request, Authentication authentication) {
		if (authentication instanceof OAuth2AuthenticationToken) {
			OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
			User user = authenticationResolver.getUser();
			JwtRefreshPair pair = jwtRefreshPairService.issue(user,
					VendorId.valueOf(token.getAuthorizedClientRegistrationId().toUpperCase()),
					token.getPrincipal().getAttribute("sub"));
			SecurityContextHolder.clearContext();
			request.getSession().invalidate();
			return ResponseEntity.ok(pair);
		} else if (authentication instanceof UsernamePasswordAuthenticationToken) {
			User user = authenticationResolver.getUser();
			JwtRefreshPair pair = jwtRefreshPairService.issue(user);
			SecurityContextHolder.clearContext();
			request.getSession().invalidate();
			return ResponseEntity.ok(pair);
		} else {
			return ResponseEntity.badRequest().build();
		}
	}

	@PostMapping("/get_access")
	public ResponseEntity<JwtRefreshPair> getNewJwtToken(@RequestBody @Valid GetNewJwtTokenRequest request) {
		Optional<RefreshToken> optional = refreshTokenService.getByToken(request.getRefreshToken());
		if (optional.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		refreshTokenService.invalidate(optional.get());
		User user = optional.get().getUser();
		return ResponseEntity.ok(jwtRefreshPairService.issue(user));
	}

	@DeleteMapping("/invalidate")
	@Operation(security = @SecurityRequirement(name = "jwt"))
	public ResponseEntity<Void> invalidateToken(@RequestBody @Valid InvalidateTokenRequest request) {
		User user = authenticationResolver.getUser();
		Optional<RefreshToken> optional = refreshTokenService.getByToken(request.getRefreshToken());
		if (optional.isPresent()) {
			RefreshToken refreshToken = optional.get();
			if (user.equals(refreshToken.getUser())) {
				refreshTokenService.invalidate(refreshToken);
			}
		}
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/invalidate_all")
	@Operation(security = @SecurityRequirement(name = "jwt"))
	public ResponseEntity<Void> invalidateAllTokens() {
		User user = authenticationResolver.getUser();
		refreshTokenService.invalidateAll(user);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/link_social")
	public ResponseEntity<JwtRefreshPair> linkSocialAccounts(@RequestBody LinkSocialAccountsRequest request) {
		Optional<User> primaryUser = refreshTokenService.getByToken(request.getPrimaryRefreshToken())
				.flatMap(rt -> Optional.of(rt.getUser()));
		Optional<JwtToken> optionalJwtToken = jwtTokenService.decode(request.getSecondaryAccessToken());
		Optional<User> secondaryUser = optionalJwtToken.flatMap(jt -> userService.getById(jt.getUid()));
		if (primaryUser.isEmpty() || secondaryUser.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		JwtToken jwtToken = optionalJwtToken.get();
		if (jwtToken.getVendorId() == null) {
			return ResponseEntity.badRequest().build();
		}
		userService.delete(secondaryUser.get());
		userService.setVendorSub(primaryUser.get(), jwtToken.getVendorId(), jwtToken.getVendorSub());
		return ResponseEntity.ok(jwtRefreshPairService.issue(primaryUser.get(), jwtToken.getVendorId(), jwtToken.getVendorSub()));
	}
}
