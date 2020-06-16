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
import com.a6raywa1cher.mucminigamesspring.security.jwt.JwtToken;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtTokenServiceImpl implements JwtTokenService {
	private final static String ISSUER_NAME = "muc-minigames-spring";
	private final static String VENDOR_ID_CLAIM = "vid";
	private final static String VENDOR_SUB_CLAIM = "vsub";
	@Value("${jwt.secret}")
	private String secret;
	private Algorithm algorithm;
	private JWTVerifier jwtVerifier;
	@Value("${jwt.exchange-duration}")
	private Duration duration;

	@PostConstruct
	public void init() {
		algorithm = Algorithm.HMAC512(secret);
		jwtVerifier = JWT.require(algorithm)
				.withIssuer(ISSUER_NAME)
				.build();
	}

	@Override
	public JwtToken issue(Long userId) {
		ZonedDateTime expiringAt = nowPlusDuration();
		String token = JWT.create()
				.withIssuer(ISSUER_NAME)
				.withSubject(Long.toString(userId))
				.withExpiresAt(Date.from(expiringAt.toInstant()))
				.sign(algorithm);
		return JwtToken.builder()
				.token(token)
				.uid(userId)
				.expiringAt(expiringAt.toLocalDateTime())
				.build();
	}

	private ZonedDateTime nowPlusDuration() {
		return ZonedDateTime.now().plus(duration);
	}

	@Override
	public JwtToken issue(VendorId vendorId, String vendorSub, Long userId) {
		ZonedDateTime zonedDateTime = nowPlusDuration();
		String token = JWT.create()
				.withIssuer(ISSUER_NAME)
				.withSubject(Long.toString(userId))
				.withClaim(VENDOR_SUB_CLAIM, vendorSub)
				.withClaim(VENDOR_ID_CLAIM, vendorId.toString())
				.withExpiresAt(Date.from(zonedDateTime.toInstant()))
				.sign(algorithm);
		return JwtToken.builder()
				.token(token)
				.uid(userId)
				.expiringAt(zonedDateTime.toLocalDateTime())
				.vendorId(vendorId)
				.vendorSub(vendorSub)
				.build();
	}

	@Override
	public Optional<JwtToken> decode(String token) {
		try {
			DecodedJWT decodedJWT = jwtVerifier.verify(token);
			JwtToken jwtToken = JwtToken.builder()
					.token(token)
					.uid(Long.parseLong(decodedJWT.getSubject()))
					.expiringAt(decodedJWT.getExpiresAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
					.build();
			if (!decodedJWT.getClaim(VENDOR_SUB_CLAIM).isNull()) {
				jwtToken.setVendorId(VendorId.valueOf(decodedJWT.getClaim(VENDOR_ID_CLAIM).asString()));
				jwtToken.setVendorSub(decodedJWT.getClaim(VENDOR_SUB_CLAIM).asString());
			}
			return Optional.of(jwtToken);
		} catch (Exception e) {
			return Optional.empty();
		}
	}
}
