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

import com.a6raywa1cher.mucminigamesspring.config.AppConfigProperties;
import com.a6raywa1cher.mucminigamesspring.security.jwt.JwtAuthenticationFilter;
import com.a6raywa1cher.mucminigamesspring.security.jwt.service.JwtTokenService;
import com.a6raywa1cher.mucminigamesspring.security.providers.JwtAuthenticationProvider;
import com.a6raywa1cher.mucminigamesspring.security.providers.UsernamePasswordAuthenticationProvider;
import com.a6raywa1cher.mucminigamesspring.service.UserService;
import com.a6raywa1cher.mucminigamesspring.utils.AuthenticationResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationCodeGrantFilter;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	private final UserService userService;

	private final AppConfigProperties appConfigProperties;

	private final JwtTokenService jwtTokenService;

	private final AuthenticationResolver authenticationResolver;

	private final OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService;

	private final OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService;

	private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

	@Autowired
	public SecurityConfig(UserService userService, AppConfigProperties appConfigProperties,
						  JwtTokenService jwtTokenService, AuthenticationResolver authenticationResolver,
						  @Qualifier("oidc-user-service") OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService,
						  @Qualifier("oauth2-user-service") OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService,
						  CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler) {
		this.userService = userService;
		this.appConfigProperties = appConfigProperties;
		this.jwtTokenService = jwtTokenService;
		this.authenticationResolver = authenticationResolver;
		this.oidcUserService = oidcUserService;
		this.oAuth2UserService = oAuth2UserService;
		this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) {
		auth
				.authenticationProvider(new JwtAuthenticationProvider(userService))
				.authenticationProvider(new UsernamePasswordAuthenticationProvider(userService, passwordEncoder()));
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
				.csrf().disable()
				.authorizeRequests()
				.antMatchers("/").permitAll()
				.antMatchers("/user/reg").permitAll()
				.antMatchers("/v2/api-docs", "/webjars/**", "/swagger-resources", "/swagger-resources/**",
						"/swagger-ui.html").permitAll()
				.antMatchers("/csrf").permitAll()
				.antMatchers("/ws-entry").permitAll()
				.antMatchers("/auth/convert").hasAuthority("CONVERTIBLE")
				.antMatchers("/logout").authenticated()
				.anyRequest().hasRole("USER")
				.and()
				.cors()
				.configurationSource(corsConfigurationSource(appConfigProperties));
		http.oauth2Login()
				.successHandler(customAuthenticationSuccessHandler)
				.userInfoEndpoint()
				.oidcUserService(oidcUserService)
				.userService(oAuth2UserService)
				.and()
				.tokenEndpoint()
				.accessTokenResponseClient(accessTokenResponseClient());
		http.oauth2Client();
		http
				.httpBasic()
				.and()
				.formLogin();
		http.addFilterBefore(new JwtAuthenticationFilter(jwtTokenService, authenticationManagerBean()), OAuth2AuthorizationCodeGrantFilter.class);
		http.addFilterAfter(new LastVisitFilter(userService, authenticationResolver), SecurityContextHolderAwareRequestFilter.class);
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Bean
	public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
		DefaultAuthorizationCodeTokenResponseClient accessTokenResponseClient =
				new DefaultAuthorizationCodeTokenResponseClient();
		accessTokenResponseClient.setRequestEntityConverter(new OAuth2AuthorizationCodeGrantRequestEntityConverter());

		OAuth2AccessTokenResponseHttpMessageConverter tokenResponseHttpMessageConverter =
				new OAuth2AccessTokenResponseHttpMessageConverter();
		tokenResponseHttpMessageConverter.setTokenResponseConverter(map -> {
			String accessToken = map.get(OAuth2ParameterNames.ACCESS_TOKEN);
			long expiresIn = Long.parseLong(map.get(OAuth2ParameterNames.EXPIRES_IN));

			OAuth2AccessToken.TokenType accessTokenType = OAuth2AccessToken.TokenType.BEARER; // vk issue

			Map<String, Object> additionalParameters = new HashMap<>();

			map.forEach(additionalParameters::put);

			return OAuth2AccessTokenResponse.withToken(accessToken)
					.tokenType(accessTokenType)
					.expiresIn(expiresIn)
					.additionalParameters(additionalParameters)
					.build();
		});
		RestTemplate restTemplate = new RestTemplate(Arrays.asList(
				new FormHttpMessageConverter(), tokenResponseHttpMessageConverter));
		restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());

		accessTokenResponseClient.setRestOperations(restTemplate);
		return accessTokenResponseClient;
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource(AppConfigProperties appConfigProperties) {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList(appConfigProperties.getCorsAllowedOrigins()));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "DELETE", "PATCH", "PUT", "HEAD", "OPTIONS"));
		configuration.setAllowedHeaders(Collections.singletonList("*"));
		configuration.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
