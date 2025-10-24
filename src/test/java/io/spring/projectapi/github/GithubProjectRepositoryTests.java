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

import java.time.YearMonth;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.spring.projectapi.github.Project.Status;
import io.spring.projectapi.github.ProjectGeneration.SupportType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link GithubProjectRepository}.
 */
class GithubProjectRepositoryTests {

	private GithubProjectRepository projectRepository;

	private GithubQueries githubQueries;

	private ProjectData data;

	@BeforeEach
	void setup() {
		this.githubQueries = mock(GithubQueries.class);
		this.data = getData("spring-boot");
		given(this.githubQueries.getData()).willReturn(this.data);
		this.projectRepository = new GithubProjectRepository(this.githubQueries);
	}

	@Test
	void dataLoadedOnBeanCreation() {
		validateCachedValues("spring-boot");
		verify(this.githubQueries).getData();
	}

	@Test
	void updateRefreshesCache() {
		List<String> changes = List.of("project/spring-boot-updated/index.md",
				"project/spring-boot-updated/documentation.json", "project/spring-boot-updated/generations.json");
		given(this.githubQueries.updateData(any(), any())).willReturn(getData("spring-boot-updated"));
		this.projectRepository.update(changes);
		assertThatExceptionOfType(NoSuchGithubProjectException.class)
			.isThrownBy(() -> this.projectRepository.getProject("spring-boot"));
		validateCachedValues("spring-boot-updated");
		verify(this.githubQueries).updateData(this.data, changes);
	}

	@Test
	void getProjectsReturnsProjects() {
		Collection<Project> projects = this.projectRepository.getProjects();
		assertThat(projects.size()).isEqualTo(3);
	}

	@Test
	void getProjectReturnsProject() {
		Project project = this.projectRepository.getProject("spring-boot");
		assertThat(project.getSlug()).isEqualTo("spring-boot");
	}

	@Test
	void getProjectDocumentationReturnsProjectDocumentation() {
		List<ProjectDocumentation> documentation = this.projectRepository.getProjectDocumentations("spring-boot");
		assertThat(documentation.size()).isEqualTo(2);
	}

	@Test
	void getProjectGenerationReturnsProjectGeneration() {
		ProjectGeneration generation = this.projectRepository.getProjectGenerations("spring-boot");
		assertThat(generation.getGenerations().size()).isEqualTo(2);
	}

	@Test
	void getProjectSupportPolicyReturnsProjectSupportPolicy() {
		String supportPolicy = this.projectRepository.getProjectSupportPolicy("spring-boot");
		assertThat(supportPolicy).isEqualTo("UPSTREAM");
	}

	@Test
	void getProjectForNonExistentProjectThrowsException() {
		assertThatExceptionOfType(NoSuchGithubProjectException.class)
			.isThrownBy(() -> this.projectRepository.getProject("spring-foo"));
	}

	@Test
	void getProjectDocumentationForNonExistentProjectThrowsException() {
		assertThatExceptionOfType(NoSuchGithubProjectException.class)
			.isThrownBy(() -> this.projectRepository.getProjectDocumentations("spring-foo"));
	}

	@Test
	void getProjectGenerationForNonExistentProjectThrowsException() {
		assertThatExceptionOfType(NoSuchGithubProjectException.class)
			.isThrownBy(() -> this.projectRepository.getProjectGenerations("spring-foo"));
	}

	@Test
	void getProjectSupportPolicyForNonExistentProjectThrowsException() {
		assertThatExceptionOfType(NoSuchGithubProjectException.class)
			.isThrownBy(() -> this.projectRepository.getProjectSupportPolicy("spring-foo"));
	}

	private void validateCachedValues(String projectSlug) {
		Collection<Project> projects = this.projectRepository.getProjects();
		assertThat(projects.size()).isEqualTo(3);
		Project updated = this.projectRepository.getProject(projectSlug);
		assertThat(updated.getSlug()).isEqualTo(projectSlug);
		ProjectGeneration projectGeneration = this.projectRepository.getProjectGenerations(projectSlug);
		assertThat(projectGeneration.getGenerations()).size().isEqualTo(2);
		List<ProjectDocumentation> documentations = this.projectRepository.getProjectDocumentations(projectSlug);
		assertThat(documentations).size().isEqualTo(2);
		String policy = this.projectRepository.getProjectSupportPolicy(projectSlug);
		assertThat(policy).isEqualTo("UPSTREAM");
	}

	private ProjectData getData(String project) {
		return new ProjectData(getProjects(project), getProjectDocumentation(project), getProjectSupports(project),
				getProjectSupportPolicy(project));
	}

	private Map<String, Project> getProjects(String project) {
		Project project1 = new Project("Spring Boot", project, "github", Status.ACTIVE);
		Project project2 = new Project("Spring Batch", "spring-batch", "github", Status.ACTIVE);
		Project project3 = new Project("Spring Framework", "spring-framework", "github", Status.ACTIVE);
		return Map.of(project, project1, "spring-batch", project2, "spring-framework", project3);
	}

	private Map<String, ProjectGeneration> getProjectSupports(String project) {
		ProjectGeneration.Generation generation1 = new ProjectGeneration.Generation("2.2.x", YearMonth.parse("2020-02"),
				SupportType.NONE, null, null);
		ProjectGeneration.Generation generation2 = new ProjectGeneration.Generation("2.3.x", YearMonth.parse("2021-02"),
				SupportType.NONE, null, null);
		ProjectGeneration support = new ProjectGeneration(List.of(generation1, generation2));
		return Map.of(project, support);
	}

	private Map<String, List<ProjectDocumentation>> getProjectDocumentation(String project) {
		ProjectDocumentation documentation1 = new ProjectDocumentation("1.0", false, "api", "ref",
				ProjectDocumentation.Status.PRERELEASE, true);
		ProjectDocumentation documentation2 = new ProjectDocumentation("2.0", false, "api", "ref",
				ProjectDocumentation.Status.PRERELEASE, true);
		return Map.of(project, List.of(documentation1, documentation2));
	}

	private Map<String, String> getProjectSupportPolicy(String project) {
		return Map.of(project, "UPSTREAM");
	}

}
