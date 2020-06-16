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

package com.a6raywa1cher.mucminigamesspring.model.jpa;

import com.a6raywa1cher.mucminigamesspring.utils.Views;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Data
public class User {
	@Id
	@GeneratedValue
	@JsonView(Views.Public.class)
	private Long id;

	@Column(unique = true)
	@JsonView(Views.Internal.class)
	private String googleId;

	@Column(unique = true)
	@JsonView(Views.Internal.class)
	private String vkId;

	@Column(unique = true, nullable = false)
	@JsonView(Views.Internal.class)
	private String email;

	@Column(unique = true)
	@JsonView(Views.Public.class)
	private String name;

	@Column(length = 1024)
	@JsonView(Views.Public.class)
	private String picture;

	@Column(length = 1024)
	@JsonIgnore
	private String password;

	@Column
	@JsonView(Views.Internal.class)
	private boolean emailVerified;

	@Column
	@JsonView(Views.Public.class)
	private boolean locked;

	@Column
	@JsonView(Views.Public.class)
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private ZonedDateTime createdAt;

	@Column
	@JsonView(Views.Public.class)
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private ZonedDateTime lastVisit;

	@Transient
	@JsonView(Views.Internal.class)
	@JsonProperty("passwordLoginEnabled")
	public boolean passwordLoginEnabled() {
		return password != null && !password.isBlank();
	}
}
