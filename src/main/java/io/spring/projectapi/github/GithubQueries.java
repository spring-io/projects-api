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

import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

/**
 * Central class for fetching data from Github.
 *
 * @author Madhura Bhave
 */
public class GithubQueries {

	private static final TypeReference<@NotNull List<ProjectDocumentation>> DOCUMENTATION_LIST = new TypeReference<>() {
	};

	private static final TypeReference<List<ProjectSupport>> SUPPORT_LIST = new TypeReference<>() {
	};

	private static final String GITHUB_URI = "https://api.github.com/repos/spring-io/spring-website-content/contents";

	private static final Logger logger = LoggerFactory.getLogger(GithubOperations.class);

	private final RestTemplate restTemplate;

	private final ObjectMapper objectMapper;

	private static final String DEFAULT_SUPPORT_POLICY = "SPRING_BOOT";

	private static final Pattern PROJECT_FILE = Pattern.compile("/project/(.*)/.");

	private static final ParameterizedTypeReference<Map<String, Object>> STRING_OBJECT_MAP = new ParameterizedTypeReference<>() {
	};

	private static final ParameterizedTypeReference<List<Map<String, Object>>> STRING_OBJECT_MAP_LIST = new ParameterizedTypeReference<>() {
	};

	private final String branch;

	public GithubQueries(RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper, String token,
			String branch) {
		this.restTemplate = restTemplateBuilder.rootUri(GITHUB_URI)
			.defaultHeader("Authorization", "Bearer " + token)
			.build();
		this.branch = branch;
		this.objectMapper = objectMapper;
	}

	ProjectData getData() {
		Map<String, Project> projects = new LinkedHashMap<>();
		Map<String, List<ProjectDocumentation>> documentation = new LinkedHashMap<>();
		Map<String, List<ProjectSupport>> support = new LinkedHashMap<>();
		Map<String, String> supportPolicy = new LinkedHashMap<>();
		try {
			RequestEntity<Void> request = RequestEntity.get("/project?ref=" + this.branch).build();
			ResponseEntity<List<Map<String, Object>>> exchange = this.restTemplate.exchange(request,
					STRING_OBJECT_MAP_LIST);
			InvalidGithubResponseException.throwIfInvalid(exchange);
			List<Map<String, Object>> body = exchange.getBody();
			body.forEach((project) -> populateData(project, projects, documentation, support, supportPolicy));
		}
		catch (Exception ex) {
			logger.debug("Could not get projects due to '%s'".formatted(ex.getMessage()));
			// Return empty list
		}
		return new ProjectData(projects, documentation, support, supportPolicy);
	}

	ProjectData updateData(ProjectData data, List<String> changes) {
		Assert.notNull(data, "Project data should not be null");
		Map<String, Project> projects = new LinkedHashMap<>(data.project());
		Map<String, List<ProjectDocumentation>> documentation = new LinkedHashMap<>(data.documentation());
		Map<String, List<ProjectSupport>> support = new LinkedHashMap<>(data.support());
		Map<String, String> supportPolicy = new LinkedHashMap<>(data.supportPolicy());
		try {
			changes.forEach((change) -> {
				ProjectFile file = ProjectFile.from(change);
				if (ProjectFile.OTHER.equals(file)) {
					return;
				}
				updateData(change, file, projects, supportPolicy, documentation, support);
			});
		}
		catch (Exception ex) {
			logger.debug("Could not update data due to '%s'".formatted(ex.getMessage()));
		}
		return new ProjectData(projects, documentation, support, supportPolicy);
	}

	private void updateData(String change, ProjectFile file, Map<String, Project> projects,
			Map<String, String> supportPolicy, Map<String, List<ProjectDocumentation>> documentation,
			Map<String, List<ProjectSupport>> support) {
		Matcher matcher = PROJECT_FILE.matcher(change);
		String slug = matcher.group();
		if (ProjectFile.INDEX.equals(file)) {
			ResponseEntity<Map<String, Object>> response = getFile(slug, "index.md");
			Project project = getProject(response, slug);
			if (project != null) {
				projects.put(slug, project);
			}
			String policy = getProjectSupportPolicy(response, slug);
			supportPolicy.put(slug, policy);
		}
		if (ProjectFile.DOCUMENTATION.equals(file)) {
			List<ProjectDocumentation> projectDocumentation = getProjectDocumentations(slug);
			documentation.put(slug, projectDocumentation);
		}
		if (ProjectFile.SUPPORT.equals(file)) {
			List<ProjectSupport> projectSupports = getProjectSupports(slug);
			support.put(slug, projectSupports);
		}
	}

	private void populateData(Map<String, Object> project, Map<String, Project> projects,
			Map<String, List<ProjectDocumentation>> documentation, Map<String, List<ProjectSupport>> support,
			Map<String, String> supportPolicy) {
		String projectSlug = (String) project.get("name");
		ResponseEntity<Map<String, Object>> response = getFile(projectSlug, "index.md");
		Project fetchedProject = getProject(response, projectSlug);
		if (fetchedProject != null) {
			projects.put(projectSlug, fetchedProject);
		}
		List<ProjectDocumentation> projectDocumentations = getProjectDocumentations(projectSlug);
		documentation.put(projectSlug, projectDocumentations);
		List<ProjectSupport> projectSupports = getProjectSupports(projectSlug);
		support.put(projectSlug, projectSupports);
		String policy = getProjectSupportPolicy(response, projectSlug);
		supportPolicy.put(projectSlug, policy);
	}

	private Project getProject(ResponseEntity<Map<String, Object>> response, String projectSlug) {
		try {
			String contents = getFileContent(response);
			Map<String, String> frontMatter = MarkdownUtils.getFrontMatter(contents);
			frontMatter.put("slug", projectSlug);
			return this.objectMapper.convertValue(frontMatter, Project.class);
		}
		catch (Exception ex) {
			logger.debug("Could not get project for '%s' due to '%s'".formatted(projectSlug, ex.getMessage()));
		}
		return null;
	}

	private List<ProjectDocumentation> getProjectDocumentations(String projectSlug) {
		try {
			ResponseEntity<Map<String, Object>> response = getFile(projectSlug, "documentation.json");
			String content = getFileContent(response);
			return List.copyOf(convertToProjectDocumentation(content));
		}
		catch (Exception ex) {
			logger.debug(
					"Could not get project documentation for '%s' due to '%s'".formatted(projectSlug, ex.getMessage()));
		}
		return Collections.emptyList();
	}

	private List<ProjectSupport> getProjectSupports(String projectSlug) {
		try {
			ResponseEntity<Map<String, Object>> response = getFile(projectSlug, "support.json");
			String contents = getFileContent(response);
			return List.copyOf(readValue(contents, SUPPORT_LIST));
		}
		catch (Exception ex) {
			logger.debug("Could not get project support for '%s' due to '%s'".formatted(projectSlug, ex.getMessage()));
		}
		return Collections.emptyList();
	}

	private String getProjectSupportPolicy(ResponseEntity<Map<String, Object>> response, String projectSlug) {
		try {
			String content = getFileContent(response);
			Map<String, String> frontMatter = MarkdownUtils.getFrontMatter(content);
			frontMatter.put("slug", projectSlug);
			String supportPolicy = frontMatter.get("supportPolicy");
			return (supportPolicy != null) ? supportPolicy : DEFAULT_SUPPORT_POLICY;
		}
		catch (Exception ex) {
			logger.debug("Could not get project support policy for '%s' due to '%s'".formatted(projectSlug,
					ex.getMessage()));
		}
		return DEFAULT_SUPPORT_POLICY;
	}

	private List<ProjectDocumentation> convertToProjectDocumentation(String content) throws JsonProcessingException {
		return readValue(content, DOCUMENTATION_LIST);
	}

	private <T> T readValue(String contents, TypeReference<T> type) throws JsonProcessingException {
		return this.objectMapper.readValue(contents, type);
	}

	private ResponseEntity<Map<String, Object>> getFile(String projectSlug, String fileName) {
		RequestEntity<Void> request = RequestEntity
			.get("/project/{projectSlug}/{fileName}?ref=" + this.branch, projectSlug, fileName)
			.build();
		return this.restTemplate.exchange(request, STRING_OBJECT_MAP);
	}

	private String getFileContent(ResponseEntity<Map<String, Object>> exchange) {
		String encodedContent = (String) exchange.getBody().get("content");
		String cleanedContent = StringUtils.replace(encodedContent, "\n", "");
		byte[] contents = Base64.getDecoder().decode(cleanedContent);
		return new String(contents);
	}

	enum ProjectFile {

		INDEX,

		SUPPORT,

		DOCUMENTATION,

		OTHER;

		static ProjectFile from(String fileName) {
			if (fileName.contains("index.md")) {
				return INDEX;
			}
			if (fileName.contains("documentation.json")) {
				return DOCUMENTATION;
			}
			if (fileName.contains("support.json")) {
				return SUPPORT;
			}
			return OTHER;
		}

	}

}
