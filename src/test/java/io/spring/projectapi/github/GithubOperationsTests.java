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

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.spring.projectapi.github.ProjectDocumentation.Status;
import org.hamcrest.text.MatchesPattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.util.FileCopyUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Tests for {@link GithubOperations}.
 *
 * @author Madhura Bhave
 */
class GithubOperationsTests {

	private static final String GITHUB_URL = "https://api.github.com/repos/spring-io/spring-website-contents/contents/";

	private GithubOperations operations;

	private MockServerRestTemplateCustomizer customizer;

	private static final String DOCUMENTATION_URI = "/project/test-project/documentation.json?ref=test";

	@BeforeEach
	void setup() {
		this.customizer = new MockServerRestTemplateCustomizer();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		objectMapper.registerModule(new JavaTimeModule());
		this.operations = new GithubOperations(new RestTemplateBuilder(this.customizer), objectMapper, "test-token",
				"test");
	}

	@Test
	void getProjectsReturnsProjects() throws Exception {
		setupFile("project-all-response.json", "/project?ref=test");
		this.customizer.getServer()
			.expect(ExpectedCount.manyTimes(),
					requestTo(MatchesPattern.matchesPattern("\\/project\\/.+\\/index\\.md\\?ref\\=test")))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess(from("project-index-response.json"), MediaType.APPLICATION_JSON));
		List<Project> projects = this.operations.getProjects();
		assertThat(projects.size()).isEqualTo(3);
		assertThat(projects.get(0).getSlug()).isEqualTo("spring-webflow");
	}

	@Test
	void getProjectsWhenNoProjectsReturnsEmpty() {
		setupNoProjectDirectory();
		List<Project> projects = this.operations.getProjects();
		assertThat(projects).isEmpty();
	}

	// @Test
	// void getProjectsWhenErrorThrowsException() throws Exception {
	// setupResponse("query-error.json");
	// assertThatExceptionOfType(GithubException.class).isThrownBy(this.operations::getProjects);
	// }

	@Test
	void getProjectReturnsProject() throws Exception {
		setupFile("project-index-response.json", "/project/spring-boot/index.md?ref=test");
		Project project = this.operations.getProject("spring-boot");
		assertThat(project.getSlug()).isEqualTo("spring-boot");
	}

	@Test
	void getProjectWhenNoProjectMatchThrowsException() throws Exception {
		setupNonExistentProject("index.md");
		assertThatExceptionOfType(NoSuchGithubProjectException.class)
			.isThrownBy(() -> this.operations.getProject("does-not-exist"))
			.satisfies((ex) -> assertThat(ex.getProjectSlug()).isEqualTo("does-not-exist"));
	}

	@Test
	void getProjectDocumentationsReturnsDocumentations() throws Exception {
		setupFile("project-documentation-response.json", DOCUMENTATION_URI);
		List<ProjectDocumentation> documentations = this.operations.getProjectDocumentations("test-project");
		assertThat(documentations).hasSize(9);
	}

	@Test
	void getProjectDocumentationsWhenNoProjectMatchThrowsException() throws Exception {
		setupNonExistentProject("documentation.json");
		assertThatExceptionOfType(NoSuchGithubProjectException.class)
			.isThrownBy(() -> this.operations.getProjectDocumentations("does-not-exist"))
			.satisfies((ex) -> assertThat(ex.getProjectSlug()).isEqualTo("does-not-exist"));
	}

	@Test
	void getProjectSupportsReturnsSupports() throws Exception {
		setupFile("project-support-response.json", "/project/spring-boot/support.json?ref=test");
		setupFile("project-index-response.json", "/project/spring-boot/index.md?ref=test");
		List<ProjectSupport> supports = this.operations.getProjectSupports("spring-boot");
		assertThat(supports).hasSize(14);
		assertThat(supports.get(0).getInitialDate()).isEqualTo(LocalDate.parse("2017-01-30"));
	}

	@Test
	void getProjectSupportsWhenNoProjectMatchThrowsException() throws Exception {
		setupNonExistentProject("support.json");
		assertThatExceptionOfType(NoSuchGithubProjectException.class)
			.isThrownBy(() -> this.operations.getProjectSupports("does-not-exist"))
			.satisfies((ex) -> assertThat(ex.getProjectSlug()).isEqualTo("does-not-exist"));
	}

	@Test
	void getProjectSupportsWhenNullReturnsEmptyList() {
		setupResourceNotFound("/project/test-project/support.json?ref=test");
		setupProject();
		List<ProjectSupport> supports = this.operations.getProjectSupports("test-project");
		assertThat(supports).isEmpty();
	}

	@Test
	void addProjectDocumentationWhenProjectDoesNotExistThrowsException() throws Exception {
		setupNonExistentProject("documentation.json");
		assertThatExceptionOfType(NoSuchGithubProjectException.class).isThrownBy(() -> this.operations
			.addProjectDocumentation("does-not-exist", getDocumentation("1.0", Status.GENERAL_AVAILABILITY)));
	}

	@Test
	void addProjectDocumentation() throws Exception {
		setupFile("project-documentation-response.json", DOCUMENTATION_URI);
		ProjectDocumentation documentation = getDocumentation("3.15.1", Status.GENERAL_AVAILABILITY);
		setupFileUpdate("project-documentation-updated-content.json", "Update documentation",
				"2d2f875ca7d476d8b01bc1db07d29b5eba1d5120");
		this.operations.addProjectDocumentation("test-project", documentation);
	}

	@Test
	void addProjectDocumentationForNonCurrent() throws Exception {
		setupFile("project-documentation-response.json", DOCUMENTATION_URI);
		ProjectDocumentation documentation = getDocumentation("0.7", Status.GENERAL_AVAILABILITY);
		setupFileUpdate("project-documentation-updated-content-non-ga.json", "Update documentation",
				"2d2f875ca7d476d8b01bc1db07d29b5eba1d5120");
		this.operations.addProjectDocumentation("test-project", documentation);
	}

	@Test
	void addFirstProjectDocumentationForGARelease() throws Exception {
		setupResourceNotFound(DOCUMENTATION_URI);
		setupProject();
		this.customizer.getServer()
			.expect(method(HttpMethod.PUT))
			.andExpect(jsonPath("$.content")
				.value(getEncodedContent("project-documentation-updated-content-first-ga.json"))) // FIXME
			.andExpect(jsonPath("$.message").value("Update documentation"))
			.andRespond(withStatus(HttpStatus.ACCEPTED));
		ProjectDocumentation documentation = getDocumentation("1.0", ProjectDocumentation.Status.GENERAL_AVAILABILITY);
		this.operations.addProjectDocumentation("test-project", documentation);

	}

	@Test
	void addFirstProjectDocumentationForNonGARelease() throws Exception {
		setupResourceNotFound(DOCUMENTATION_URI);
		setupProject();
		this.customizer.getServer()
			.expect(method(HttpMethod.PUT))
			.andExpect(jsonPath("$.content")
				.value(getEncodedContent("project-documentation-updated-content-first-non-ga.json"))) // FIXME
			.andExpect(jsonPath("$.message").value("Update documentation"))
			.andRespond(withStatus(HttpStatus.ACCEPTED));
		ProjectDocumentation documentation = getDocumentation("1.0-M1", Status.PRERELEASE);
		this.operations.addProjectDocumentation("test-project", documentation);
	}

	@Test
	void deleteProjectDocumentationWhenProjectDoesNotExistThrowsException() throws Exception {
		setupNonExistentProject("documentation.json");
		assertThatExceptionOfType(NoSuchGithubProjectException.class)
			.isThrownBy(() -> this.operations.deleteDocumentation("does-not-exist", "1.0"));
	}

	@Test
	void deleteProjectDocumentationWhenProjectDocumentationDoesNotExistThrowsException() throws Exception {
		setupFile("project-documentation-response.json", DOCUMENTATION_URI);
		assertThatExceptionOfType(NoSuchGithubProjectDocumentationFoundException.class)
			.isThrownBy(() -> this.operations.deleteDocumentation("test-project", "2.0"));
	}

	@Test
	void deleteProjectDocumentation() throws Exception {
		setupFile("project-documentation-response.json", DOCUMENTATION_URI);
		setupFileUpdate("project-documentation-deleted-content.json", "Update documentation",
				"2d2f875ca7d476d8b01bc1db07d29b5eba1d5120");
		this.operations.deleteDocumentation("test-project", "3.2.10");
	}

	@Test
	void deleteCurrentProjectDocumentation() throws Exception {
		setupFile("project-documentation-response.json", DOCUMENTATION_URI);
		setupFileUpdate("project-documentation-deleted-current-content.json", "Update documentation",
				"2d2f875ca7d476d8b01bc1db07d29b5eba1d5120");
		this.operations.deleteDocumentation("test-project", "3.3.4");
	}

	@Test
	void deletePreReleaseProjectDocumentation() throws Exception {
		setupFile("project-documentation-response.json", DOCUMENTATION_URI);
		setupFileUpdate("project-documentation-deleted-prerelease-content.json", "Update documentation",
				"2d2f875ca7d476d8b01bc1db07d29b5eba1d5120");
		this.operations.deleteDocumentation("test-project", "3.2.11-SNAPSHOT");
	}

	@Test
	void patchProjectWhenProjectDoesNotExistThrowsException() {
		setupResourceNotFound("/project/does-not-exist?ref=test");
		assertThatExceptionOfType(NoSuchGithubProjectException.class)
			.isThrownBy(() -> this.operations.patchProjectDetails("does-not-exist", new ProjectDetails(null, null)));
	}

	@Test
	void patchProjectWhenSpringBootConfigNullDoesNotUpdate() throws Exception {
		setupProject();
		setupIndex();
		setupProjectUpdate();
		this.operations.patchProjectDetails("test-project", new ProjectDetails(null, "new body"));
	}

	@Test
	void patchProjectWhenBodyNullDoesNotUpdate() throws Exception {
		setupProject();
		setupSpringBootConfig();
		setupSpringBootConfigUpdate();
		this.operations.patchProjectDetails("test-project", new ProjectDetails("Updated Spring Boot Info", null));
	}

	@Test
	void patchProjectUpdatesBodyAndDescription() throws Exception {
		setupProject();
		setupSpringBootConfig();
		setupSpringBootConfigUpdate();
		setupIndex();
		setupProjectUpdate();
		this.operations.patchProjectDetails("test-project", new ProjectDetails("Updated Spring Boot Info", "new body"));
	}

	private void setupSpringBootConfigUpdate() throws Exception {
		setupFileUpdate("project-spring-boot-info-updated.md", "Update Spring Boot Config",
				"8c41ae6d6cfb58fd5d59291432b3d2b7b8106890");
	}

	private void setupProjectUpdate() throws Exception {
		setupFileUpdate("project-index-updated.md", "Update project index", "8be3885df06f9b66581589b6447a4d49cfb1be32");
	}

	private void setupFileUpdate(String fileName, String message, String sha) throws Exception {
		this.customizer.getServer()
			.expect(method(HttpMethod.PUT))
			.andExpect(jsonPath("$.content").value(getEncodedContent(fileName)))
			.andExpect(jsonPath("$.message").value(message))
			.andExpect(jsonPath("$.sha").value(sha))
			.andRespond(withStatus(HttpStatus.ACCEPTED));
	}

	private void setupIndex() throws Exception {
		setupFile("project-index-response.json", "/project/test-project/index.md?ref=test");
	}

	private void setupSpringBootConfig() throws Exception {
		setupFile("project-spring-boot-config-response.json", "/project/test-project/springBootConfig.md?ref=test");
	}

	private void setupProject() {
		this.customizer.getServer()
			.expect(requestTo("/project/test-project?ref=test"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess());
	}

	private void setupFile(String responseFileName, String uri) throws Exception {
		this.customizer.getServer()
			.expect(requestTo(uri))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess(from(responseFileName), MediaType.APPLICATION_JSON));
	}

	private void setupNonExistentProject(String fileName) {
		setupResourceNotFound("/project/does-not-exist/" + fileName + "?ref=test");
		setupResourceNotFound("/project/does-not-exist?ref=test");
	}

	private void setupResourceNotFound(String expectedUri) {
		this.customizer.getServer()
			.expect(requestTo(expectedUri))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withResourceNotFound());
	}

	private void setupNoProjectDirectory() {
		setupResourceNotFound("/project?ref=test");
	}

	private String getEncodedContent(String path) throws Exception {
		return Base64.getEncoder().encodeToString(from(path));
	}

	private byte[] from(String path) throws IOException {
		ClassPathResource resource = new ClassPathResource(path, getClass());
		try (InputStream inputStream = resource.getInputStream()) {
			return FileCopyUtils.copyToByteArray(inputStream);
		}
	}

	private ProjectDocumentation getDocumentation(String version, Status status) {
		return new ProjectDocumentation(version, true, "http://api.com", "http://ref.com", status, false);
	}

}
