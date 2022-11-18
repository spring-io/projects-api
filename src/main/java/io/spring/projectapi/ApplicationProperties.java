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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Application configuration properties.
 *
 * @author Madhura Bhave
 */
@ConfigurationProperties(prefix = "app")
public class ApplicationProperties {

	private final Contentful contentful;

	private final Github github;

	@ConstructorBinding
	ApplicationProperties(@DefaultValue Contentful contentful, @DefaultValue Github github) {
		this.contentful = contentful;
		this.github = github;
	}

	public Contentful getContentful() {
		return this.contentful;
	}

	public Github getGithub() {
		return this.github;
	}

	public static class Contentful {

		private String accessToken;

		private String spaceId;

		private String environmentId;

		@ConstructorBinding
		Contentful(String accessToken, String spaceId, String environmentId) {
			this.accessToken = accessToken;
			this.spaceId = spaceId;
			this.environmentId = environmentId;
		}

		public String getAccessToken() {
			return this.accessToken;
		}

		public String getSpaceId() {
			return this.spaceId;
		}

		public String getEnvironmentId() {
			return this.environmentId;
		}

	}

	public static class Github {

		/**
		 * GitHub org that holds the team admin users should belong to.
		 */
		private String org;

		/**
		 * GitHub team admin users should belong to.
		 * @see <a href=
		 * "https://developer.github.com/v3/teams/members/#get-team-membership-for-a-user">GitHub
		 * API team membership</a>
		 */
		private String team;

		@ConstructorBinding
		Github(String org, String team) {
			this.org = org;
			this.team = team;
		}

		public String getOrg() {
			return this.org;
		}

		public String getTeam() {
			return this.team;
		}

	}

}
