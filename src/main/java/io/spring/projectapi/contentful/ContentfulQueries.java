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

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.springframework.graphql.client.ClientGraphQlResponse;
import org.springframework.graphql.client.ClientResponseField;
import org.springframework.graphql.client.GraphQlClient;
import org.springframework.graphql.client.GraphQlClient.RequestSpec;
import org.springframework.graphql.client.GraphQlClientException;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Contentful queries performed via the GraphQL API.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
class ContentfulQueries {

	private static final Duration TIMEOUT = Duration.ofSeconds(1);

	private final GraphQlClient client;

	ContentfulQueries(WebClient webClient, String accessToken) {
		this.client = HttpGraphQlClient.builder(webClient)
			.headers((headers) -> headers.setBearerAuth(accessToken))
			.build();
	}

	List<Project> getProjects() {
		ClientGraphQlResponse response = execute("projects");
		return fieldToEntityList(response, "projectCollection.items", Project.class);
	}

	Project getProject(String projectSlug) {
		ClientGraphQlResponse response = executeForSingleProject("project", projectSlug);
		return fieldToEntity(response, "projectCollection.items[0]", Project.class);
	}

	List<ProjectDocumentation> getProjectDocumentations(String projectSlug) {
		ClientGraphQlResponse response = executeForSingleProject("project-documentations", projectSlug);
		return fieldToEntityList(response, "projectCollection.items[0].documentation", ProjectDocumentation.class);
	}

	List<ProjectSupport> getProjectSupports(String projectSlug) {
		ClientGraphQlResponse response = executeForSingleProject("project-supports", projectSlug);
		return fieldToEntityList(response, "projectCollection.items[0].support", ProjectSupport.class);
	}

	private ClientGraphQlResponse execute(String documentName) {
		return execute(this.client.documentName(documentName));
	}

	private ClientGraphQlResponse executeForSingleProject(String documentName, String projectSlug) {
		ClientGraphQlResponse response = execute(this.client.documentName(documentName).variable("slug", projectSlug));
		NoSuchContentfulProjectException.throwIfHasNoValue(response.field("projectCollection.items[0]"), projectSlug);
		return response;
	}

	private <T> List<T> fieldToEntityList(ClientGraphQlResponse response, String path, Class<T> type) {
		return field(response, path, (field) -> getList(type, response, path));
	}

	private static <T> List<T> getList(Class<T> type, ClientGraphQlResponse response, String path) {
		ClientResponseField field = response.field(path);
		return (!field.hasValue() && response.getErrors().isEmpty()) ? Collections.emptyList()
				: field.toEntityList(type);
	}

	private <T> T fieldToEntity(ClientGraphQlResponse response, String path, Class<T> type) {
		return field(response, path, (field) -> field.toEntity(type));
	}

	private <T> T field(ClientGraphQlResponse response, String path, Function<ClientResponseField, T> adapter) {
		try {
			return adapter.apply(response.field(path));
		}
		catch (GraphQlClientException ex) {
			throw new ContentfulException(ex);
		}
	}

	private ClientGraphQlResponse execute(RequestSpec request) {
		ClientGraphQlResponse response = request.execute().block(TIMEOUT);
		InvalidContentfulQueryResponseException.throwIfInvalid(response);
		return response;
	}

}
