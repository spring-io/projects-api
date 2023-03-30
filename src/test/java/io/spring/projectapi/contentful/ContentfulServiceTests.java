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

import java.time.LocalDate;
import java.util.List;

import io.spring.projectapi.contentful.Project.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link ContentfulService}.
 *
 * @author Madhura Bhave
 */
class ContentfulServiceTests {

	private ContentfulService service;

	private ContentfulQueries queries = mock(ContentfulQueries.class);

	private ContentfulOperations operations = mock(ContentfulOperations.class);

	private static final Project PROJECT = new Project("Test Project", "test-project", "github", Status.ACTIVE);

	private static final ProjectDocumentation PROJECT_DOCUMENTATION = new ProjectDocumentation("2.0", "http://api.com",
			"http://ref.com", ProjectDocumentation.Status.GENERAL_AVAILABILITY, false);

	private static final ProjectSupport PROJECT_SUPPORT = new ProjectSupport("2.2.x", LocalDate.parse("2020-02-01"),
			LocalDate.parse("2020-02-02"), LocalDate.parse("2020-02-03"), LocalDate.parse("2020-02-04"),
			LocalDate.parse("2020-02-05"));

	@BeforeEach
	void setup() {
		this.service = new ContentfulService(this.queries, this.operations);
	}

	@Test
	void getProjectsReturnsProjects() {
		given(this.queries.getProjects()).willReturn(List.of(PROJECT));
		List<Project> projects = this.service.getProjects();
		verify(this.queries).getProjects();
		assertThat(projects).containsExactly(PROJECT);
	}

	@Test
	void getProjectReturnProject() {
		given(this.queries.getProject("test-project")).willReturn(PROJECT);
		Project project = this.service.getProject("test-project");
		assertThat(project).isEqualTo(PROJECT);
		verify(this.queries).getProject("test-project");
	}

	@Test
	void getProjectDocumentations() {
		given(this.queries.getProjectDocumentations("test-project")).willReturn(List.of(PROJECT_DOCUMENTATION));
		List<ProjectDocumentation> projectDocumentations = this.service.getProjectDocumentations("test-project");
		verify(this.queries).getProjectDocumentations("test-project");
		assertThat(projectDocumentations).containsExactly(PROJECT_DOCUMENTATION);
	}

	@Test
	void getProjectSupports() {
		given(this.queries.getProjectSupports("test-project")).willReturn(List.of(PROJECT_SUPPORT));
		List<ProjectSupport> projectSupports = this.service.getProjectSupports("test-project");
		verify(this.queries).getProjectSupports("test-project");
		assertThat(projectSupports).containsExactly(PROJECT_SUPPORT);
	}

	@Test
	void addProject() {
		this.service.addProjectDocumentation("test-project", PROJECT_DOCUMENTATION);
		verify(this.operations).addProjectDocumentation("test-project", PROJECT_DOCUMENTATION);
	}

	@Test
	void deleteProject() {
		this.service.deleteDocumentation("test-project", "1.0");
		verify(this.operations).deleteDocumentation("test-project", "1.0");
	}

}
