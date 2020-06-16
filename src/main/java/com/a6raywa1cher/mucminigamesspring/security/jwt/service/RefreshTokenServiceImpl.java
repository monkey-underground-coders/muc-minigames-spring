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

import com.a6raywa1cher.mucminigamesspring.model.jpa.User;
import com.a6raywa1cher.mucminigamesspring.security.model.jpa.RefreshToken;
import com.a6raywa1cher.mucminigamesspring.security.model.jpa.repo.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {
	private final RefreshTokenRepository repository;

	public RefreshTokenServiceImpl(RefreshTokenRepository repository) {
		this.repository = repository;
	}

	@Override
	public RefreshToken issue(User user) {
		List<RefreshToken> tokenList = repository.findAllByUser(user);
		if (tokenList.size() > 5) {
			repository.deleteAll(tokenList.stream()
					.sorted(Comparator.comparing(RefreshToken::getExpiringAt))
					.limit(tokenList.size() - 5)
					.collect(Collectors.toUnmodifiableList()));
		}
		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setToken(UUID.randomUUID().toString());
		refreshToken.setExpiringAt(LocalDateTime.now().plus(3, ChronoUnit.MONTHS));
		refreshToken.setUser(user);
		return repository.save(refreshToken);
	}

	@Override
	public Optional<RefreshToken> getByToken(String token) {
		return repository.findById(token);
	}

	@Override
	public void invalidate(RefreshToken refreshToken) {
		repository.delete(refreshToken);
	}

	@Override
	public void invalidateAll(User user) {
		repository.deleteAll(repository.findAllByUser(user));
	}
}
