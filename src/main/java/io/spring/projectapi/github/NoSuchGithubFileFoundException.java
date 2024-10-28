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

import org.springframework.http.ResponseEntity;

/**
 * {@link GithubException} thrown when a project documentation with the specified version
 * does not exist.
 *
 * @author Madhura Bhave
 */
public final class NoSuchGithubFileFoundException extends GithubException {

	private final String projectSlug;

	private final String fileName;

	NoSuchGithubFileFoundException(String projectSlug, String fileName) {
		super("%s not found for project '%s'".formatted(fileName, projectSlug));
		this.projectSlug = projectSlug;
		this.fileName = fileName;
	}

	static void throwWhenFileNotFound(ResponseEntity<?> response, String projectSlug, String fileName) {
		if (response == null) {
			throw new NoSuchGithubFileFoundException(projectSlug, fileName);
		}

	}

	public String getProjectSlug() {
		return this.projectSlug;
	}

	public String getFileName() {
		return this.fileName;
	}

}
