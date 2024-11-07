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
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
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
		setupProjectFiles("support\\.json", "project-support-response.json");
		ProjectData projectData = this.queries.getData();
		assertThat(projectData.project().size()).isEqualTo(3);
		assertThat(projectData.project().get("spring-webflow").getSlug()).isEqualTo("spring-webflow");
		assertThat(projectData.documentation().get("spring-webflow")).hasSize(9);
		List<ProjectSupport> support = projectData.support().get("spring-webflow");
		assertThat(support).hasSize(14);
		assertThat(support.get(0).getInitialDate()).isEqualTo(LocalDate.parse("2017-01-30"));
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
		setupProjectFiles("support\\.json", "project-support-response.json");
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
		setupProjectFiles("support\\.json", "project-support-response.json");
		this.customizer.getServer()
			.expect(ExpectedCount.once(), requestTo("/project/spring-xd/documentation.json?ref=test"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withResourceNotFound());
		ProjectData projectData = this.queries.getData();
		assertThat(projectData.documentation().get("spring-xd")).isEmpty();
	}

	@Test
	void projectSupportsWhenFileNotFoundReturnsEmptyList() throws Exception {
		setupProjects();
		setupProjectFiles("index\\.md", "project-index-response.json");
		setupProjectFiles("documentation\\.json", "project-documentation-response.json");
		this.customizer.getServer()
			.expect(ExpectedCount.max(2),
					requestTo(MatchesPattern.matchesPattern("\\/project\\/spring-w.+\\/support\\.json\\?ref\\=test")))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess(from("project-support-response.json"), MediaType.APPLICATION_JSON));
		this.customizer.getServer()
			.expect(ExpectedCount.once(), requestTo("/project/spring-xd/support.json?ref=test"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withResourceNotFound());
		ProjectData projectData = this.queries.getData();
		assertThat(projectData.support().get("spring-xd")).isEmpty();
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
