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

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.projectapi.github.ProjectDocumentation.Status;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Handles fetching and putting data to Github.
 *
 * @author Madhura Bhave
 */
public class GithubOperations {

	private static final TypeReference<@NotNull List<ProjectDocumentation>> DOCUMENTATION_LIST = new TypeReference<>() {
	};

	private static final String GITHUB_URI = "https://api.github.com/repos/spring-io/spring-website-content/contents";

	private static final Comparator<ProjectDocumentation> VERSION_COMPARATOR = GithubOperations::compare;

	private static final Logger logger = LoggerFactory.getLogger(GithubOperations.class);

	private final RestTemplate restTemplate;

	private final ObjectMapper objectMapper;

	private static final String DOCUMENTATION_COMMIT_MESSAGE = "Update documentation";

	private static final String INDEX_COMMIT_MESSAGE = "Update project index";

	private static final String CONFIG_COMMIT_MESSAGE = "Update Spring Boot Config";

	private static final ParameterizedTypeReference<Map<String, Object>> STRING_OBJECT_MAP = new ParameterizedTypeReference<>() {
	};

	private static final ParameterizedTypeReference<List<Map<String, Object>>> STRING_OBJECT_MAP_LIST = new ParameterizedTypeReference<>() {
	};

	private final DefaultPrettyPrinter prettyPrinter;

	private final String branch;

	private final RetryTemplate retryTemplate;

	public GithubOperations(RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper, String token,
			String branch, RetryTemplate retryTemplate) {
		this.retryTemplate = retryTemplate;
		this.restTemplate = restTemplateBuilder.rootUri(GITHUB_URI)
			.defaultHeader("Authorization", "Bearer " + token)
			.build();
		this.branch = branch;
		this.objectMapper = objectMapper;
		this.prettyPrinter = new DefaultPrettyPrinter();
		this.prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
	}

	private static int compare(ProjectDocumentation o1, ProjectDocumentation o2) {
		ComparableVersion version1 = new ComparableVersion(o1.getVersion());
		ComparableVersion version2 = new ComparableVersion(o2.getVersion());
		return -version1.compareTo(version2);
	}

	public void addProjectDocumentation(String projectSlug, ProjectDocumentation documentation) {
		try {
			this.retryTemplate.execute((context) -> {
				ResponseEntity<Map<String, Object>> response = getFile(projectSlug, "documentation.json");
				List<ProjectDocumentation> documentations = new ArrayList<>();
				String sha = null;
				if (response != null) {
					String content = getFileContents(response);
					sha = getFileSha(response);
					documentations.addAll(convertToProjectDocumentation(content));
				}
				documentations.add(documentation);
				List<ProjectDocumentation> updatedDocumentation = computeCurrentRelease(documentations);
				updateProjectDocumentation(projectSlug, updatedDocumentation, sha);
				return null;
			});
		}
		catch (HttpClientErrorException ex) {
			ConflictingGithubContentException.throwIfConflict(ex, projectSlug, "documentation.json");
		}

	}

	private List<ProjectDocumentation> convertToProjectDocumentation(String content) {
		return readValue(content, DOCUMENTATION_LIST);
	}

	private <T> T readValue(String contents, TypeReference<T> type) {
		try {
			return this.objectMapper.readValue(contents, type);
		}
		catch (JsonProcessingException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void updateProjectDocumentation(String projectSlug, List<ProjectDocumentation> documentations, String sha) {
		try {
			byte[] content = this.objectMapper.writer(this.prettyPrinter).writeValueAsBytes(documentations);
			updateContents(content, sha, projectSlug, "documentation.json", DOCUMENTATION_COMMIT_MESSAGE);
		}
		catch (JsonProcessingException ex) {
			throw new RuntimeException(ex);
		}
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

	private List<ProjectDocumentation> computeCurrentRelease(List<ProjectDocumentation> documentations) {
		Map<Boolean, List<ProjectDocumentation>> partitioned = documentations.stream()
			.collect(Collectors
				.partitioningBy((documentation) -> Status.GENERAL_AVAILABILITY.equals(documentation.getStatus())));
		List<ProjectDocumentation> gaList = partitioned.get(true);
		List<ProjectDocumentation> sortedGaList = gaList.stream().sorted(VERSION_COMPARATOR).toList();
		List<ProjectDocumentation> preReleaseList = partitioned.get(false);
		List<ProjectDocumentation> updatedGaList = new ArrayList<>(getListWithUpdatedCurrentRelease(sortedGaList));
		Collections.reverse(updatedGaList);
		preReleaseList.addAll(updatedGaList);
		return List.copyOf(preReleaseList);
	}

	@NotNull
	private static List<ProjectDocumentation> getListWithUpdatedCurrentRelease(
			List<ProjectDocumentation> sortedGaList) {
		return IntStream.range(0, sortedGaList.size())
			.mapToObj((i) -> updateCurrent(sortedGaList.get(i), i == 0))
			.toList();
	}

	private static ProjectDocumentation updateCurrent(ProjectDocumentation documentation, boolean current) {
		return new ProjectDocumentation(documentation.getVersion(), documentation.isAntora(), documentation.getApi(),
				documentation.getRef(), documentation.getStatus(), current);
	}

	public void deleteDocumentation(String projectSlug, String version) {
		try {
			this.retryTemplate.execute((context) -> {
				ResponseEntity<Map<String, Object>> response = getFile(projectSlug, "documentation.json");
				NoSuchGithubFileFoundException.throwWhenFileNotFound(response, projectSlug, "documentation.json");
				String content = getFileContents(response);
				String sha = getFileSha(response);
				List<ProjectDocumentation> documentation = convertToProjectDocumentation(content);
				NoSuchGithubProjectDocumentationFoundException.throwIfHasNotPresent(documentation, projectSlug,
						version);
				documentation.removeIf((y) -> y.getVersion().equals(version));
				List<ProjectDocumentation> documentations1 = computeCurrentRelease(documentation);
				updateProjectDocumentation(projectSlug, documentations1, sha);
				return null;
			});
		}
		catch (HttpClientErrorException ex) {
			ConflictingGithubContentException.throwIfConflict(ex, projectSlug, "documentation.json");
		}

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
