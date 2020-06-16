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

package com.a6raywa1cher.mucminigamesspring.security.jwt.service;

import com.a6raywa1cher.mucminigamesspring.model.VendorId;
import com.a6raywa1cher.mucminigamesspring.model.jpa.User;
import com.a6raywa1cher.mucminigamesspring.security.jwt.JwtRefreshPair;
import com.a6raywa1cher.mucminigamesspring.security.jwt.JwtToken;
import com.a6raywa1cher.mucminigamesspring.security.model.jpa.RefreshToken;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class JwtRefreshPairServiceImpl implements JwtRefreshPairService {
	private final RefreshTokenService refreshTokenService;

	private final JwtTokenService jwtTokenService;

	public JwtRefreshPairServiceImpl(RefreshTokenService refreshTokenService, JwtTokenService jwtTokenService) {
		this.refreshTokenService = refreshTokenService;
		this.jwtTokenService = jwtTokenService;
	}

	@Override
	public JwtRefreshPair issue(User user) {
		JwtToken accessToken = jwtTokenService.issue(user.getId());
		RefreshToken refreshToken = refreshTokenService.issue(user);
		return new JwtRefreshPair(
				refreshToken.getToken(),
				OffsetDateTime.of(refreshToken.getExpiringAt(), OffsetDateTime.now().getOffset()),
				accessToken.getToken(),
				OffsetDateTime.of(accessToken.getExpiringAt(), OffsetDateTime.now().getOffset())
		);
	}

	@Override
	public JwtRefreshPair issue(User user, VendorId vendorId, String sub) {
		JwtToken accessToken = jwtTokenService.issue(vendorId, sub, user.getId());
		RefreshToken refreshToken = refreshTokenService.issue(user);
		return new JwtRefreshPair(
				refreshToken.getToken(),
				OffsetDateTime.of(refreshToken.getExpiringAt(), OffsetDateTime.now().getOffset()),
				accessToken.getToken(),
				OffsetDateTime.of(accessToken.getExpiringAt(), OffsetDateTime.now().getOffset())
		);
	}
}
