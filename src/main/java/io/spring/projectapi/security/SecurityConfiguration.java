/*
 * Copyright 2022-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.projectapi.security;

import io.spring.projectapi.ApplicationProperties;
import io.spring.projectapi.ApplicationProperties.Github;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration. Allows public access to all GET endpoints. All other endpoints
 * require basic authentication with a Github token. The configured
 * {@link AuthenticationManager} expects requests that are similar to
 * {@code curl -u username:token https://api.spring.io/}.
 *
 * @author Madhura Bhave
 * @see GithubAuthenticationManager
 */
@Configuration(proxyBeanMethods = false)
public class SecurityConfiguration {

	@Bean
	public SecurityFilterChain configure(HttpSecurity http, RestTemplateBuilder restTemplateBuilder,
			ApplicationProperties properties) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable);
		http.requiresChannel((channel) -> channel.requestMatchers(this::hasXForwardedPortHeader).requiresSecure());
		http.authorizeHttpRequests((requests) -> {
			requests.requestMatchers(HttpMethod.GET, "/**").permitAll();
			requests.requestMatchers("/refresh_cache").permitAll();
			requests.anyRequest().hasRole("ADMIN");
		});
		Github github = properties.getGithub();
		http.authenticationManager(
				new GithubAuthenticationManager(restTemplateBuilder, github.getOrg(), github.getTeam()));
		http.httpBasic(Customizer.withDefaults());
		return http.build();
	}

	private boolean hasXForwardedPortHeader(HttpServletRequest request) {
		return request.getHeader("x-forwarded-port") != null;
	}

}
