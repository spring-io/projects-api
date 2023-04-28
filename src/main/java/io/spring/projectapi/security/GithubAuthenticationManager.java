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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * {@link AuthenticationManager} that reads OAuth2 tokens from basic Auth requests and
 * checks if the user is an active member if the configured team.
 * <p>
 * This authentication method is used for API endpoints other than HTTP GET. This
 * {@link AuthenticationManager} expects requests that are similar to
 * {@code curl -u username:token https://spring.io/api}.
 *
 * @author Madhura Bhave
 */
class GithubAuthenticationManager implements AuthenticationManager {

	private static final Logger logger = LoggerFactory.getLogger(GithubAuthenticationManager.class);

	private static final String MEMBER_PATH_TEMPLATE = "https://api.github.com/orgs/{org}/teams/{team}/memberships/{username}";

	private static final ParameterizedTypeReference<Map<String, String>> STRING_MAP = new ParameterizedTypeReference<>() {
	};

	private final RestTemplate restTemplate;

	private final String org;

	private final String team;

	GithubAuthenticationManager(RestTemplateBuilder restTemplateBuilder, String org, String team) {
		Assert.hasText(org, "Org must not be empty");
		Assert.hasText(team, "Team must not be empty");
		this.restTemplate = restTemplateBuilder.build();
		this.org = org;
		this.team = team;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String username = (String) authentication.getPrincipal();
		String token = (String) authentication.getCredentials();
		if (!StringUtils.hasText(token)) {
			logger.debug("Missing OAuth2 token as basic auth credentials");
			return null;
		}
		List<GrantedAuthority> authorities = new ArrayList<>();
		User user = new User(username, token, authorities);
		try {
			if (isAdmin(username, token)) {
				authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
			}
		}
		catch (HttpClientErrorException.Unauthorized unauthorized) {
			throw new BadCredentialsException("Invalid credentials", unauthorized);
		}

		return new UsernamePasswordAuthenticationToken(user, null, authorities);
	}

	private boolean isAdmin(String userName, String accessToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBasicAuth(userName, accessToken);
		try {
			logger.debug("Checking {}/{} membership for user {}", this.org, this.team, userName);
			ResponseEntity<Map<String, String>> response = this.restTemplate.exchange(MEMBER_PATH_TEMPLATE,
					HttpMethod.GET, new HttpEntity<>(headers), STRING_MAP, this.org, this.team, userName);
			if (response.getStatusCode().is2xxSuccessful()) {
				logger.debug("Membership state is {}", response.getBody().get("state"));
				return response.getBody().get("state").equals("active");
			}
			return false;
		}
		catch (HttpClientErrorException.NotFound notFound) {
			logger.debug("Membership not found, maybe privacy restrictions are in place");
			return false;
		}
	}

}
