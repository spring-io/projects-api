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

/**
 * {@link GithubException} thrown when a project generation with the specified name does
 * not exist.
 *
 * @author Madhura Bhave
 */
public final class NoSuchGithubGenerationFoundException extends GithubException {

	private final String projectSlug;

	private final String generation;

	NoSuchGithubGenerationFoundException(String projectSlug, String generation) {
		super("No generation '%s' found for project '%s'".formatted(generation, projectSlug));
		this.projectSlug = projectSlug;
		this.generation = generation;
	}

	static void throwIfNull(Object generationNode, String projectSlug, String generation) {
		if (generationNode == null) {
			throw new NoSuchGithubGenerationFoundException(projectSlug, generation);
		}
	}

	public String getProjectSlug() {
		return this.projectSlug;
	}

	public String getGeneration() {
		return this.generation;
	}

}
