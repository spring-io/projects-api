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

import java.io.IOException;
import java.io.InputStream;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.spring.projectapi.github.Project.Status;
import io.spring.projectapi.github.ProjectGeneration.SupportType;
import org.hamcrest.text.MatchesPattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.util.FileCopyUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Tests for {@link GithubQueries}.
 */
class GithubQueriesTests {

	private GithubQueries queries;

	private MockServerRestTemplateCustomizer customizer;

	@BeforeEach
	void setup() {
		this.customizer = new MockServerRestTemplateCustomizer();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));
		objectMapper.registerModule(new JavaTimeModule());
		this.queries = new GithubQueries(new RestTemplateBuilder(this.customizer), objectMapper, "test-token", "test");
	}

	@Test
	void getDataReturnsProjectData() throws Exception {
		setupProjects();
		setupProjectFiles("index\\.md", "project-index-response.json");
		setupProjectFiles("documentation\\.json", "project-documentation-response.json");
		setupProjectFiles("generations\\.json", "project-generations-response.json");
		ProjectData projectData = this.queries.getData();
		assertThat(projectData.project().size()).isEqualTo(3);
		assertThat(projectData.project().get("spring-webflow").getSlug()).isEqualTo("spring-webflow");
		assertThat(projectData.documentation().get("spring-webflow")).hasSize(9);
		List<ProjectGeneration.Generation> generations = projectData.generation()
			.get("spring-webflow")
			.getGenerations();
		assertThat(generations).hasSize(16);
		assertThat(generations.get(0).getInitialRelease()).isEqualTo(YearMonth.parse("2017-01"));
		assertThat(projectData.supportPolicy().get("spring-webflow")).isEqualTo("UPSTREAM");
	}

	@Test
	void getProjectsDoesNotAddProjectIfNotFound() throws Exception {
		setupProjects();
		this.customizer.getServer()
			.expect(ExpectedCount.max(2),
					requestTo(MatchesPattern.matchesPattern("\\/project\\/spring-w.+\\/index\\.md\\?ref\\=test")))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess(from("project-index-response.json"), MediaType.APPLICATION_JSON));
		setupProjectFiles("documentation\\.json", "project-documentation-response.json");
		setupProjectFiles("generations\\.json", "project-generations-response.json");
		this.customizer.getServer()
			.expect(ExpectedCount.once(), requestTo("/project/spring-xd/index.md?ref=test"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withResourceNotFound());
		ProjectData projectData = this.queries.getData();
		assertThat(projectData.project().size()).isEqualTo(2);
		assertThat(projectData.project().get("spring-webflow").getSlug()).isEqualTo("spring-webflow");
	}

	@Test
	void projectWhenNoProjectsReturnsEmpty() {
		setupNoProjectDirectory();
		ProjectData projectData = this.queries.getData();
		assertThat(projectData.project()).isEmpty();
	}

	@Test
	void projectDocumentationWhenFileNotFoundReturnsEmptyList() throws Exception {
		setupProjects();
		setupProjectFiles("index\\.md", "project-index-response.json");
		this.customizer.getServer()
			.expect(ExpectedCount.max(2),
					requestTo(MatchesPattern
						.matchesPattern("\\/project\\/spring-w.+\\/documentation\\.json\\?ref\\=test")))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess(from("project-documentation-response.json"), MediaType.APPLICATION_JSON));
		setupProjectFiles("generations\\.json", "project-generations-response.json");
		this.customizer.getServer()
			.expect(ExpectedCount.once(), requestTo("/project/spring-xd/documentation.json?ref=test"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withResourceNotFound());
		ProjectData projectData = this.queries.getData();
		assertThat(projectData.documentation().get("spring-xd")).isEmpty();
	}

	@Test
	void projectGenerationWhenFileNotFoundReturnsEmptyList() throws Exception {
		setupProjects();
		setupProjectFiles("index\\.md", "project-index-response.json");
		setupProjectFiles("documentation\\.json", "project-documentation-response.json");
		this.customizer.getServer()
			.expect(ExpectedCount.max(2),
					requestTo(
							MatchesPattern.matchesPattern("\\/project\\/spring-w.+\\/generations\\.json\\?ref\\=test")))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess(from("project-generations-response.json"), MediaType.APPLICATION_JSON));
		this.customizer.getServer()
			.expect(ExpectedCount.once(), requestTo("/project/spring-xd/generations.json?ref=test"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withResourceNotFound());
		ProjectData projectData = this.queries.getData();
		assertThat(projectData.generation().get("spring-xd").getGenerations()).isEmpty();
	}

	@Test
	void updateDataUpdatesOnlyChangedData() throws Exception {
		ProjectData data = getProjectData();
		List<String> changes = List.of("project/spring-boot/index.md", "project/spring-framework/documentation.json");
		this.customizer.getServer()
			.expect(requestTo("/project/spring-boot?ref=test"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess());
		this.customizer.getServer()
			.expect(requestTo("/project/spring-boot/index.md?ref=test"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess(from("project-index-response.json"), MediaType.APPLICATION_JSON));
		this.customizer.getServer()
			.expect(requestTo("/project/spring-framework?ref=test"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess());
		this.customizer.getServer()
			.expect(requestTo("/project/spring-framework/documentation.json?ref=test"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess(from("project-documentation-response.json"), MediaType.APPLICATION_JSON));
		ProjectData projectData = this.queries.updateData(data, changes);
		assertThat(projectData.project().size()).isEqualTo(3);
		assertThat(projectData.project().get("spring-boot").getTitle()).isEqualTo("Spring AMQP");
		assertThat(projectData.documentation().get("spring-framework").size()).isEqualTo(9);
	}

	@Test
	void updateDataRemovesDeletedProject() {
		ProjectData data = getProjectData();
		List<String> changes = List.of("project/spring-boot/index.md", "project/spring-boot/documentation.json");
		this.customizer.getServer()
			.expect(requestTo("/project/spring-boot?ref=test"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withResourceNotFound());
		ProjectData projectData = this.queries.updateData(data, changes);
		assertThat(projectData.project().size()).isEqualTo(2);
		assertThat(projectData.project().get("spring-boot")).isNull();
		assertThat(projectData.documentation().get("spring-boot")).isNull();
		assertThat(projectData.generation().get("spring-boot")).isNull();
		assertThat(projectData.supportPolicy().get("spring-boot")).isNull();
	}

	@Test
	void updateDataWhenNoProjectFilesChangedDoesNothing() {
		ProjectData data = getProjectData();
		List<String> changes = List.of("blog.md");
		ProjectData projectData = this.queries.updateData(data, changes);
		assertThat(projectData.project().size()).isEqualTo(3);
	}

	private ProjectData getProjectData() {
		return new ProjectData(getProjects(), getProjectDocumentation(), getProjectSupports(),
				getProjectSupportPolicy());
	}

	private Map<String, Project> getProjects() {
		Project project1 = new Project("Spring Boot", "spring-boot", "github", Status.ACTIVE);
		Project project2 = new Project("Spring Batch", "spring-batch", "github", Status.ACTIVE);
		Project project3 = new Project("Spring Framework", "spring-framework", "github", Status.ACTIVE);
		return Map.of("spring-boot", project1, "spring-batch", project2, "spring-framework", project3);
	}

	private Map<String, ProjectGeneration> getProjectSupports() {
		ProjectGeneration.Generation generation1 = new ProjectGeneration.Generation("2.2.x", YearMonth.parse("2020-02"),
				SupportType.NONE, null, null, springBootLinkedGenerations("2.6.x"));
		ProjectGeneration.Generation generation2 = new ProjectGeneration.Generation("2.3.x", YearMonth.parse("2021-02"),
				SupportType.NONE, null, null, springBootLinkedGenerations("2.7.x"));
		ProjectGeneration support = new ProjectGeneration(List.of(generation1, generation2));
		return Map.of("spring-boot", support);
	}

	private Map<String, List<String>> springBootLinkedGenerations(String... versions) {
		return Map.of("spring-boot", Arrays.asList(versions));
	}

	private Map<String, List<ProjectDocumentation>> getProjectDocumentation() {
		ProjectDocumentation documentation1 = new ProjectDocumentation("1.0", false, "api", "ref",
				ProjectDocumentation.Status.PRERELEASE, true);
		ProjectDocumentation documentation2 = new ProjectDocumentation("2.0", false, "api", "ref",
				ProjectDocumentation.Status.PRERELEASE, true);
		return Map.of("spring-boot", List.of(documentation1, documentation2));
	}

	private Map<String, String> getProjectSupportPolicy() {
		return Map.of("spring-boot", "UPSTREAM");
	}

	private void setupProjectFiles(String fileName, String responseFileName) throws IOException {
		this.customizer.getServer()
			.expect(ExpectedCount.manyTimes(),
					requestTo(MatchesPattern.matchesPattern("\\/project\\/.+\\/" + fileName + "\\?ref\\=test")))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess(from(responseFileName), MediaType.APPLICATION_JSON));
	}

	private void setupProjects() throws Exception {
		this.customizer.getServer()
			.expect(requestTo("/project?ref=test"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess(from("project-all-response.json"), MediaType.APPLICATION_JSON));
	}

	private void setupNoProjectDirectory() {
		this.customizer.getServer()
			.expect(requestTo("/project?ref=test"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withResourceNotFound());
	}

	private byte[] from(String path) throws IOException {
		ClassPathResource resource = new ClassPathResource(path, getClass());
		try (InputStream inputStream = resource.getInputStream()) {
			return FileCopyUtils.copyToByteArray(inputStream);
		}
	}

}
