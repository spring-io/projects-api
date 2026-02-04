/*
 * Copyright 2022-present the original author or authors.
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

package io.spring.projectapi;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Application configuration properties.
 *
 * @author Madhura Bhave
 */
@ConfigurationProperties(prefix = "projects")
public class ApplicationProperties {

	private final Github github;

	@ConstructorBinding
	ApplicationProperties(@DefaultValue Github github) {
		this.github = github;
	}

	public Github getGithub() {
		return this.github;
	}

	/**
	 * Properties to decide Github team membership.
	 */
	public static class Github {

		/**
		 * GitHub org that holds the team admin users should belong to.
		 */
		private String org;

		/**
		 * GitHub team admin users should belong to.
		 *
		 * @see <a href=
		 * "https://developer.github.com/v3/teams/members/#get-team-membership-for-a-user">GitHub
		 * API team membership</a>
		 */
		private String team;

		/**
		 * Github access token for accessing the API.
		 */
		private String accesstoken;

		/**
		 * Github branch to use for fetching and updating content.
		 */
		private String branch;

		/**
		 * Secret for triggering the webhook that refreshes the cache.
		 */
		private String webhookSecret;

		private final Enterprise enterprise;

		@ConstructorBinding
		Github(String org, String team, String accesstoken, @DefaultValue("main") String branch, String webhookSecret,
				@DefaultValue Enterprise enterprise) {
			this.org = org;
			this.team = team;
			this.accesstoken = accesstoken;
			this.branch = branch;
			this.webhookSecret = webhookSecret;
			this.enterprise = enterprise;
		}

		public String getOrg() {
			return this.org;
		}

		public String getTeam() {
			return this.team;
		}

		public String getBranch() {
			return this.branch;
		}

		public String getAccesstoken() {
			return this.accesstoken;
		}

		public String getWebhookSecret() {
			return this.webhookSecret;
		}

		public Enterprise getEnterprise() {
			return this.enterprise;
		}

	}

	/**
	 * Properties for enterprise content repository.
	 */
	public static class Enterprise {

		/**
		 * Github access token for accessing the commercial content API.
		 */
		private String accesstoken;

		/**
		 * Github branch to use for fetching commercial content.
		 */
		private String branch;

		@ConstructorBinding
		Enterprise(String accesstoken, @DefaultValue("main") String branch) {
			this.accesstoken = accesstoken;
			this.branch = branch;
		}

		public String getAccesstoken() {
			return this.accesstoken;
		}

		public String getBranch() {
			return this.branch;
		}

	}

}
