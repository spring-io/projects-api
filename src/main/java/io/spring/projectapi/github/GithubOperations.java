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

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.util.DefaultIndenter;
import tools.jackson.core.util.DefaultPrettyPrinter;
import tools.jackson.core.util.Separators;
import tools.jackson.core.util.Separators.Spacing;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Handles fetching and putting data to Github.
 *
 * @author Madhura Bhave
 */
public class GithubOperations {

	private static final String GITHUB_URI = "https://api.github.com/repos/spring-io/spring-website-content/contents";

	private static final Logger logger = LoggerFactory.getLogger(GithubOperations.class);

	private final RestTemplate restTemplate;

	private final JsonMapper jsonMapper;

	private static final String INDEX_COMMIT_MESSAGE = "Update project index";

	private static final String PROJECT_FILE = "project.json";

	private static final String LATEST_VERSION_COMMIT_MESSAGE = "Update latest OSS version to %s";

	private static final String CONFIG_COMMIT_MESSAGE = "Update Spring Boot Config";

	private static final ParameterizedTypeReference<Map<String, Object>> STRING_OBJECT_MAP = new ParameterizedTypeReference<>() {
	};

	private static final ParameterizedTypeReference<List<Map<String, Object>>> STRING_OBJECT_MAP_LIST = new ParameterizedTypeReference<>() {
	};

	private final DefaultPrettyPrinter prettyPrinter;

	private final ObjectWriter projectPrettyWriter;

	private final String branch;

	private final RetryTemplate retryTemplate;

	public GithubOperations(RestTemplateBuilder restTemplateBuilder, JsonMapper jsonMapper, String token, String branch,
			RetryTemplate retryTemplate) {
		this.retryTemplate = retryTemplate;
		this.restTemplate = restTemplateBuilder.baseUri(GITHUB_URI)
			.defaultHeader("Authorization", "Bearer " + token)
			.build();
		this.branch = branch;
		this.jsonMapper = jsonMapper;
		this.prettyPrinter = new DefaultPrettyPrinter();
		this.prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
		DefaultPrettyPrinter projectPrinter = new DefaultPrettyPrinter(
				Separators.createDefaultInstance().withObjectNameValueSpacing(Spacing.AFTER));
		projectPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
		this.projectPrettyWriter = jsonMapper.writer().with(projectPrinter);
	}

	/**
	 * Update the latest open source version of a generation in the project's
	 * {@code project.json}.
	 * @param projectSlug the project slug
	 * @param generation the generation name (for example {@code 4.1.x})
	 * @param version the new latest open source version
	 */
	public void updateLatestOssVersion(String projectSlug, String generation, String version) {
		try {
			this.retryTemplate.invoke(() -> {
				ResponseEntity<Map<String, Object>> response = getFile(projectSlug, "project.json");
				NoSuchGithubFileFoundException.throwWhenFileNotFound(response, projectSlug, PROJECT_FILE);
				JsonNode root = this.jsonMapper.readTree(getFileContents(response));
				ObjectNode generationNode = findGeneration(root, generation);
				NoSuchGithubGenerationFoundException.throwIfNull(generationNode, projectSlug, generation);
				JsonNode latestVersions = generationNode.get("latestVersions");
				if (!(latestVersions instanceof ObjectNode)) {
					latestVersions = generationNode.putObject("latestVersions");
				}
				((ObjectNode) latestVersions).put("oss", version);
				byte[] content = (this.projectPrettyWriter.writeValueAsString(root) + "\n").getBytes();
				updateContents(content, getFileSha(response), projectSlug, PROJECT_FILE,
						LATEST_VERSION_COMMIT_MESSAGE.formatted(version));
			});
		}
		catch (HttpClientErrorException ex) {
			ConflictingGithubContentException.throwIfConflict(ex, projectSlug, PROJECT_FILE);
		}
	}

	private ObjectNode findGeneration(JsonNode root, String generation) {
		for (JsonNode candidate : root.path("generations")) {
			if (candidate instanceof ObjectNode node && generation.equals(node.path("generation").asString())) {
				return node;
			}
		}
		return null;
	}

	public void patchProjectDetails(String projectSlug, ProjectDetails projectDetails) {
		throwIfProjectDoesNotExist(projectSlug);
		if (projectDetails.getSpringBootConfig() != null) {
			ResponseEntity<Map<String, Object>> response = getFile(projectSlug, "springBootConfig.md");
			NoSuchGithubFileFoundException.throwWhenFileNotFound(response, projectSlug, "springBootConfig.md");
			String sha = getFileSha(response);
			updateContents(projectDetails.getSpringBootConfig().getBytes(), sha, projectSlug, "springBootConfig.md",
					CONFIG_COMMIT_MESSAGE);
		}
		if (projectDetails.getBody() != null) {
			ResponseEntity<Map<String, Object>> response = getFile(projectSlug, "index.md");
			NoSuchGithubFileFoundException.throwWhenFileNotFound(response, projectSlug, "index.md");
			String contents = getFileContents(response);
			String sha = getFileSha(response);
			String updatedContent = MarkdownUtils.getUpdatedContent(contents, projectDetails.getBody());
			InvalidGithubProjectIndexException.throwIfInvalid(Objects::nonNull, updatedContent, projectSlug);
			updateContents(updatedContent.getBytes(), sha, projectSlug, "index.md", INDEX_COMMIT_MESSAGE);
		}
	}

	private void updateContents(byte[] content, String sha, String projectSlug, String fileName, String commitMessage) {
		String encodedContent = Base64.getEncoder().encodeToString(content);
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("content", encodedContent);
		body.put("message", commitMessage);
		body.put("branch", this.branch);
		if (sha != null) {
			body.put("sha", sha);
		}
		RequestEntity<Map<String, Object>> request = RequestEntity
			.put("/project/{projectSlug}/{fileName}", projectSlug, fileName)
			.contentType(MediaType.APPLICATION_JSON)
			.body(body);
		this.restTemplate.exchange(request, Object.class);
	}

	private ResponseEntity<Map<String, Object>> getFile(String projectSlug, String fileName) {
		RequestEntity<Void> request = RequestEntity
			.get("/project/{projectSlug}/{fileName}?ref=" + this.branch, projectSlug, fileName)
			.build();
		try {
			return this.restTemplate.exchange(request, STRING_OBJECT_MAP);
		}
		catch (HttpClientErrorException ex) {
			HttpStatusCode statusCode = ex.getStatusCode();
			logger.debug("Failed to get file " + fileName + " for project " + projectSlug + " due to " + ex.getMessage()
					+ " with status " + statusCode);
			if (statusCode.value() == 404) {
				throwIfProjectDoesNotExist(projectSlug);
				return null;
			}
			throw new GithubException(ex);
		}
	}

	private void throwIfProjectDoesNotExist(String projectSlug) {
		RequestEntity<Void> request = RequestEntity.get("/project/{projectSlug}?ref=" + this.branch, projectSlug)
			.build();
		try {
			this.restTemplate.exchange(request, STRING_OBJECT_MAP_LIST);
		}
		catch (HttpClientErrorException ex) {
			NoSuchGithubProjectException.throwIfNotFound(ex, projectSlug);
		}
	}

	private String getFileContents(ResponseEntity<Map<String, Object>> exchange) {
		InvalidGithubResponseException.throwIfInvalid(exchange);
		String encodedContent = (String) exchange.getBody().get("content");
		String cleanedContent = StringUtils.replace(encodedContent, "\n", "");
		byte[] contents = Base64.getDecoder().decode(cleanedContent);
		return new String(contents);
	}

	private String getFileSha(ResponseEntity<Map<String, Object>> exchange) {
		InvalidGithubResponseException.throwIfInvalid(exchange);
		return (String) exchange.getBody().get("sha");
	}

}
