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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.contentful.java.cma.CMAClient;
import com.contentful.java.cma.model.CMAArray;
import com.contentful.java.cma.model.CMAEntry;
import io.spring.projectapi.generation.GenerationMetadata;
import io.spring.projectapi.project.ProjectMetadata;
import io.spring.projectapi.release.ReleaseMetadata;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.client.ClientGraphQlResponse;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;

/**
 * Central class for interacting with Contentful's REST and GraphQL API.
 *
 * @author Madhura Bhave
 */
@Component
public class ContentfulService {

	private final HttpGraphQlClient graphQlClient;

	private final ApplicationProperties properties;

	private final CMAClient client;

	private static final String LOCALE = "en-US";

	public ContentfulService(Builder webClientBuilder, ApplicationProperties properties) {
		this.properties = properties;
		this.client = getClient();
		WebClient webClient = webClientBuilder.baseUrl(getBaseUrl()).build();
		this.graphQlClient = HttpGraphQlClient.builder(webClient)
				.headers(headers -> headers.setBearerAuth(this.properties.getContentful().getAccessToken())).build();
	}

	public CMAClient getClient() {
		return new CMAClient.Builder().setAccessToken(this.properties.getContentful().getAccessToken())
				.setSpaceId(this.properties.getContentful().getSpaceId())
				.setEnvironmentId(this.properties.getContentful().getEnvironmentId()).build();
	}

	private String getBaseUrl() {
		return "https://graphql.contentful.com/content/v1/spaces/" + this.properties.getContentful().getSpaceId()
				+ "/environments/" + this.properties.getContentful().getEnvironmentId();
	}

	/**
	 * Get the list of projects
	 * @return the list of projects
	 */
	public List<ProjectMetadata> getProjects() {
		String query = "query {\n" + "  projectCollection {\n" + "    items {\n" + "      title, slug, github, status\n"
				+ "    }\n" + "  }\n" + "}";
		ClientGraphQlResponse block = this.graphQlClient.document(query).execute().block();
		return block.field("projectCollection.items").toEntity(new ParameterizedTypeReference<>() {
		});
	}

	/**
	 * Get the list of releases for the given project
	 * @param projectId the project id
	 * @return the list of releases
	 */
	public List<ReleaseMetadata> getReleases(String projectId) {
		ClientGraphQlResponse block = getClientGraphQlResponse(projectId, "documentation");
		return block.field("projectCollection.items[0].documentation").toEntity(new ParameterizedTypeReference<>() {
		});
	}

	/**
	 * Get the list of generations for the given project
	 * @param projectId the project id
	 * @return the list of generations
	 */
	public List<GenerationMetadata> getGenerations(String projectId) {
		ClientGraphQlResponse block = getClientGraphQlResponse(projectId, "support");
		return block.field("projectCollection.items[0].support").toEntity(new ParameterizedTypeReference<>() {
		});
	}

	private ClientGraphQlResponse getClientGraphQlResponse(String projectId, String field) {
		String query = "query {\n" + "  projectCollection(where: {slug:\"" + projectId + "\"}) {\n" + "    items {\n"
				+ field + "    }\n" + "  }\n" + "}";
		return this.graphQlClient.document(query).execute().block();
	}

	private CMAEntry getCmaEntry(String projectId) {
		Map<String, String> query = new LinkedHashMap<>();
		query.put("content_type", "project");
		query.put("fields.slug", projectId);
		CMAArray<CMAEntry> cmaArray = this.client.entries().fetchAll(query);
		List<CMAEntry> items = cmaArray.getItems();
		return items.get(0);
	}

	/**
	 * Add a new release for the given project
	 * @param projectId the project id
	 */
	public void addRelease(String projectId, ReleaseMetadata release) {
		CMAEntry cmaEntry = getCmaEntry(projectId);
		List<Map<String, Object>> releases = getField(cmaEntry, "documentation");
		Map<String, Object> map = new HashMap<>();
		map.put("version", release.getVersion());
		map.put("api", release.getApiDocUrl());
		map.put("ref", release.getReferenceDocUrl());
		map.put("status", release.getStatus());
		map.put("current", release.isCurrent());
		releases.add(map);
		this.client.entries().update(cmaEntry);
	}

	private List<Map<String, Object>> getField(CMAEntry cmaEntry, String field) {
		return cmaEntry.getField(field, LOCALE);
	}

	/**
	 * Delete the given release for the project
	 * @param projectId the project id
	 */
	public void deleteRelease(String projectId, String version) {
		CMAEntry cmaEntry = getCmaEntry(projectId);
		List<Map<String, Object>> releases = getField(cmaEntry, "documentation");
		releases.removeIf((release) -> release.get("version").equals(version));
		this.client.entries().update(cmaEntry);
	}

}
