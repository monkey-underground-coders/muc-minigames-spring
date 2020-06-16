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

package com.a6raywa1cher.mucminigamesspring.config;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

@Configuration
@EnableRedisRepositories(basePackages = {"com.a6raywa1cher.mucminigamesspring.model.redis"})
public class RedisConfig {
	@Bean
	JedisConnectionFactory jedisConnectionFactory(RedisProperties redisProperties) {
		RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
		redisStandaloneConfiguration.setHostName(redisProperties.getHost());
		redisStandaloneConfiguration.setPort(redisProperties.getPort());
		redisStandaloneConfiguration.setDatabase(redisProperties.getDatabase());
		if (StringUtils.hasLength(redisProperties.getPassword())) {
			redisStandaloneConfiguration.setPassword(redisProperties.getPassword());
		}
		return new JedisConnectionFactory(redisStandaloneConfiguration);
	}

	@Bean
	StringRedisSerializer stringRedisSerializer() {
		return new StringRedisSerializer();
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisProperties redisProperties) {
		final RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(jedisConnectionFactory(redisProperties));
		template.setValueSerializer(new GenericToStringSerializer<>(Object.class));
		template.setKeySerializer(stringRedisSerializer());
		template.setHashKeySerializer(stringRedisSerializer());
		return template;
	}
}
