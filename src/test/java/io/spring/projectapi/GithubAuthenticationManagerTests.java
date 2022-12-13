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
//
package io.spring.projectapi;

import io.spring.projectapi.ApplicationProperties.Contentful;
import io.spring.projectapi.ApplicationProperties.Github;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.Base64Utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Tests for {@link GithubAuthenticationManager}.
 *
 * @author Madhura Bhave
 */
class GithubAuthenticationManagerTests {

	private ApplicationProperties properties;

	private RestTemplateBuilder restTemplateBuilder;

	private GithubAuthenticationManager authenticationManager;

	private MockRestServiceServer server;

	private static final String MEMBER_PATH_TEMPLATE = "https://api.github.com/orgs/test-org/teams/test-team/memberships/user";

	@BeforeEach
	void setup() {
		this.properties = new ApplicationProperties(
				new Contentful("access-token", "management-token", "space-id", "environment-id"),
				new Github("test-org", "test-team"));
		MockServerRestTemplateCustomizer mockServerCustomizer = new MockServerRestTemplateCustomizer();
		this.restTemplateBuilder = new RestTemplateBuilder(mockServerCustomizer);
		this.authenticationManager = new GithubAuthenticationManager(this.restTemplateBuilder, this.properties);
		this.server = mockServerCustomizer.getServer();
	}

	@Test
	void authenticateWhenUserIsActiveInTeam() {
		this.server.expect(requestTo(MEMBER_PATH_TEMPLATE))
				.andExpect(header("Authorization", "Basic " + Base64Utils.encodeToString("user:password".getBytes())))
				.andRespond(withSuccess(getResponse("active"), MediaType.APPLICATION_JSON));
		Authentication authentication = new TestingAuthenticationToken("user", "password");
		Authentication adminAuthentication = this.authenticationManager.authenticate(authentication);
		assertThat(adminAuthentication.getAuthorities()).extracting(GrantedAuthority::getAuthority)
				.containsExactly("ROLE_ADMIN");
		this.server.verify();
	}

	private static String getResponse(String state) {
		return "{\n" + "  \"url\": \"https://api.github.com/organizations/1/team/2/memberships/user\",\n"
				+ "  \"role\": \"member\",\n" + "  \"state\": \"" + state + "\"\n" + "}";
	}

	@Test
	void authenticateWhenUserIsNotActiveInTeam() {
		this.server.expect(requestTo(MEMBER_PATH_TEMPLATE))
				.andExpect(header("Authorization", "Basic " + Base64Utils.encodeToString("user:password".getBytes())))
				.andRespond(withSuccess(getResponse("pending"), MediaType.APPLICATION_JSON));
		Authentication authentication = new TestingAuthenticationToken("user", "password");
		Authentication adminAuthentication = this.authenticationManager.authenticate(authentication);
		assertThat(adminAuthentication.getAuthorities()).extracting(GrantedAuthority::getAuthority).isEmpty();
		this.server.verify();
	}

	@Test
	void authenticateWhenNotFound() {
		this.server.expect(requestTo(MEMBER_PATH_TEMPLATE))
				.andExpect(header("Authorization", "Basic " + Base64Utils.encodeToString("user:password".getBytes())))
				.andRespond(withStatus(HttpStatus.NOT_FOUND));
		Authentication authentication = new TestingAuthenticationToken("user", "password");
		Authentication adminAuthentication = this.authenticationManager.authenticate(authentication);
		assertThat(adminAuthentication.getAuthorities()).extracting(GrantedAuthority::getAuthority).isEmpty();
		this.server.verify();
	}

}