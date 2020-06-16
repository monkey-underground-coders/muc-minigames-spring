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

package com.a6raywa1cher.mucminigamesspring.service.impl;

import com.a6raywa1cher.mucminigamesspring.model.VendorId;
import com.a6raywa1cher.mucminigamesspring.model.jpa.User;
import com.a6raywa1cher.mucminigamesspring.model.jpa.repo.UserRepository;
import com.a6raywa1cher.mucminigamesspring.security.jwt.service.RefreshTokenService;
import com.a6raywa1cher.mucminigamesspring.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
	private final UserRepository repository;
	private final RefreshTokenService refreshTokenService;

	public UserServiceImpl(UserRepository repository, RefreshTokenService refreshTokenService) {
		this.repository = repository;
		this.refreshTokenService = refreshTokenService;
	}

	@Override
	public Optional<User> getById(Long id) {
		return repository.findById(id);
	}

	@Override
	public Optional<User> getByVendorIdOrEmail(VendorId vendorId, String id, String email) {
		switch (vendorId) {
			case VK:
				return repository.findByVkIdOrEmail(id, email);
			case GOOGLE:
				return repository.findByGoogleIdOrEmail(id, email);
			default:
				throw new RuntimeException();
		}
	}

	@Override
	public Optional<User> getByEmail(String email) {
		return repository.findByEmail(email);
	}

	@Override
	public boolean isNameAvailable(String name) {
		return !repository.existsByName(name);
	}

	@Override
	public User setVendorSub(User user, VendorId vendorId, String id) {
		if (this.getByVendorIdOrEmail(vendorId, id, "--not email!--").isPresent()) {
			throw new IllegalArgumentException();
		}
		switch (vendorId) {
			case VK:
				user.setVkId(id);
				break;
			case GOOGLE:
				user.setGoogleId(id);
				break;
			default:
				throw new RuntimeException();
		}
		return repository.save(user);
	}

	@Override
	@Transactional
	public User setLastVisit(User user, ZonedDateTime now) {
		user.setLastVisit(now);
		return repository.save(user);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void delete(User user) {
		refreshTokenService.invalidateAll(user);
		repository.delete(user);
	}
}
