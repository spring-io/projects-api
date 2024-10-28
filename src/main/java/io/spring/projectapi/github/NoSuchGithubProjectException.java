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

import org.springframework.web.client.HttpClientErrorException;

/**
 * {@link GithubException} thrown when a project with the specified slug does not exist.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
public final class NoSuchGithubProjectException extends GithubException {

	private final String projectSlug;

	NoSuchGithubProjectException(String projectSlug) {
		super("No github project found for slug '%s'".formatted(projectSlug));
		this.projectSlug = projectSlug;
	}

	static void throwIfNotFound(HttpClientErrorException ex, String projectSlug) {
		if (ex.getStatusCode().value() == 404) {
			throw new NoSuchGithubProjectException(projectSlug);
		}
	}

	public String getProjectSlug() {
		return this.projectSlug;
	}

}
