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

package com.a6raywa1cher.mucminigamesspring.service;

import com.a6raywa1cher.mucminigamesspring.model.VendorId;
import com.a6raywa1cher.mucminigamesspring.model.jpa.User;

import java.time.ZonedDateTime;
import java.util.Optional;

public interface UserService {
	Optional<User> getById(Long id);

	Optional<User> getByVendorIdOrEmail(VendorId vendorId, String id, String email);

	Optional<User> getByEmail(String email);

	boolean isNameAvailable(String name);

	User setVendorSub(User user, VendorId vendorId, String id);

	User setLastVisit(User user, ZonedDateTime now);

	void delete(User user);
}
