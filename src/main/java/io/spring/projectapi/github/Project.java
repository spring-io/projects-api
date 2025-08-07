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

package io.spring.projectapi.github;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.springframework.util.Assert;

/**
 * Java representation of the {@code project} type as defined in
 * <a href= "https://github.com/spring-io/spring-website/">spring-io/spring-website</a>.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Project {

	private final String title;

	private final String slug;

	private final String github;

	private final Status status;

	@JsonCreator(mode = Mode.PROPERTIES)
	public Project(String title, String slug, String github, Status status) {
		Assert.notNull(title, "'title' must not be null");
		Assert.notNull(slug, "'slug' must not be null");
		this.title = title;
		this.slug = slug;
		this.github = github;
		this.status = status;
	}

	public String getTitle() {
		return this.title;
	}

	public String getSlug() {
		return this.slug;
	}

	public String getGithub() {
		return this.github;
	}

	public Status getStatus() {
		return this.status;
	}

	/**
	 * Project status.
	 */
	public enum Status {

		/**
		 * Incubating.
		 */
		INCUBATING,

		/**
		 * Active.
		 */
		ACTIVE,

		/**
		 * Active Unsupported.
		 */
		ACTIVE_UNSUPPORTED,

		/**
		 * Community Maintained.
		 */
		COMMUNITY,

		/**
		 * End of Life.
		 */
		END_OF_LIFE

	}

}
