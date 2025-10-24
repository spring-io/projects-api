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

package io.spring.projectapi.github;

import java.util.List;

/**
 * {@link GithubException} thrown when a project documentation with the specified version
 * does not exist.
 *
 * @author Madhura Bhave
 */
public final class NoSuchGithubProjectDocumentationFoundException extends GithubException {

	private final String projectSlug;

	private final String version;

	NoSuchGithubProjectDocumentationFoundException(String projectSlug, String version) {
		super("No github documentation found for slug '%s' and version '%s'".formatted(projectSlug, version));
		this.projectSlug = projectSlug;
		this.version = version;
	}

	static void throwIfHasNotPresent(List<ProjectDocumentation> documentations, String projectSlug, String version) {
		documentations.stream()
			.filter((m) -> m.getVersion().equals(version))
			.findFirst()
			.orElseThrow((() -> new NoSuchGithubProjectDocumentationFoundException(projectSlug, version)));
	}

	public String getProjectSlug() {
		return this.projectSlug;
	}

	public String getVersion() {
		return this.version;
	}

}
