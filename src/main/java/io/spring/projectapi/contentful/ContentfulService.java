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

package io.spring.projectapi.contentful;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.web.reactive.function.client.WebClient;

/**
 * Central class for interacting with Contentful's REST and GraphQL API.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
public class ContentfulService {

	private final ContentfulQueries queries;

	private final ContentfulOperations operations;

	public ContentfulService(ObjectMapper objectMapper, WebClient webClient, String accessToken, String spaceId,
			String environmentId) {
		this.queries = new ContentfulQueries(webClient, accessToken);
		this.operations = new ContentfulOperations(objectMapper, accessToken, spaceId, environmentId);
	}

	ContentfulService(ContentfulQueries queries, ContentfulOperations operations) {
		this.queries = queries;
		this.operations = operations;
	}

	public List<Project> getProjects() {
		return this.queries.getProjects();
	}

	public Project getProject(String projectSlug) {
		return this.queries.getProject(projectSlug);
	}

	public List<ProjectDocumentation> getProjectDocumentations(String projectSlug) {
		return this.queries.getProjectDocumentations(projectSlug);
	}

	public List<ProjectSupport> getProjectSupports(String projectSlug) {
		return this.queries.getProjectSupports(projectSlug);
	}

	public void addProjectDocumentation(String projectSlug, ProjectDocumentation documentation) {
		this.operations.addProjectDocumentation(projectSlug, documentation);
	}

	public void deleteDocumentation(String projectSlug, String version) {
		this.operations.deleteDocumentation(projectSlug, version);
	}

}
