/*
 * Copyright 2012-2022 the original author or authors.
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
 *
 */

package io.spring.projectapi;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration. Allows public access to all GET endpoints. All other endpoints
 * require basic authentication with a Github token. The configured
 * {@link AuthenticationManager} expects requests that are similar to
 * {@code curl -u username:token https://api.spring.io/}.
 *
 * @see {@link GithubAuthenticationManager}.
 * @author Madhura Bhave
 */
@Configuration(proxyBeanMethods = false)
public class SecurityConfiguration {

	@Bean
	public SecurityFilterChain configure(HttpSecurity http, RestTemplateBuilder builder,
			ApplicationProperties properties) throws Exception {
		http.csrf().disable();
		http.requiresChannel(channel -> channel
				.requestMatchers(request -> request.getHeader("x-forwarded-port") != null).requiresSecure());
		http.authorizeHttpRequests((req) -> {
			req.mvcMatchers(HttpMethod.GET, "/**").permitAll();
			req.anyRequest().hasRole("ADMIN");
		});
		http.authenticationManager(new GithubAuthenticationManager(builder, properties));
		http.httpBasic();
		return http.build();
	}

}
