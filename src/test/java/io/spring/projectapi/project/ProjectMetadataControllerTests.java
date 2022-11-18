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
//
package io.spring.projectapi.project;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.projectapi.ContentfulService;
import io.spring.projectapi.WebApiTest;
import io.spring.projectapi.project.ProjectMetadata.SupportStatus;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test for {@link ProjectMetadataController}.
 *
 * @author Madhura Bhave
 */
@WebApiTest
class ProjectMetadataControllerTests {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private MockMvc mvc;

	@MockBean
	private ContentfulService contentfulService;

	@Test
	public void listProjects() throws Exception {
		List<ProjectMetadata> projects = getProjects();
		given(this.contentfulService.getProjects()).willReturn(projects);
		this.mvc.perform(get("/projects").accept(MediaTypes.HAL_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$._embedded.projects.length()").value("2"))
				.andExpect(jsonPath("$._embedded.projects[0].name").value("Spring Boot"))
				.andExpect(jsonPath("$._embedded.projects[0].slug").value("spring-boot"))
				.andExpect(jsonPath("$._embedded.projects[0].repositoryUrl")
						.value("https://github.com/spring-projects/spring-boot"))
				.andExpect(jsonPath("$._embedded.projects[0].status").value("ACTIVE"))
				.andExpect(jsonPath("$._embedded.projects[0]._links.self.href")
						.value("http://localhost/projects/spring-boot"))
				.andExpect(jsonPath("$._embedded.projects[0]._links.releases.href")
						.value("http://localhost/projects/spring-boot/releases"))
				.andExpect(jsonPath("$._embedded.projects[0]._links.generations.href")
						.value("http://localhost/projects/spring-boot/generations"))
				.andExpect(jsonPath("$._embedded.projects[1].name").value("Spring Data"))
				.andExpect(jsonPath("$._embedded.projects[1].slug").value("spring-data"))
				.andExpect(jsonPath("$._embedded.projects[1].repositoryUrl")
						.value("https://github.com/spring-projects/spring-data"))
				.andExpect(jsonPath("$._embedded.projects[1].status").value("ACTIVE"))
				.andExpect(jsonPath("$._embedded.projects[1]._links.self.href")
						.value("http://localhost/projects/spring-data"))
				.andExpect(jsonPath("$._embedded.projects[1]._links.releases.href")
						.value("http://localhost/projects/spring-data/releases"))
				.andExpect(jsonPath("$._embedded.projects[1]._links.generations.href")
						.value("http://localhost/projects/spring-data/generations"))
				.andExpect(jsonPath("$._links.project.href").value("http://localhost/projects/{id}"))
				.andExpect(jsonPath("$._links.project.templated").value(true));
	}

	private List<ProjectMetadata> getProjects() {
		List<ProjectMetadata> projects = new ArrayList<>();
		ProjectMetadata project1 = new ProjectMetadata();
		project1.setName("Spring Boot");
		project1.setStatus(SupportStatus.ACTIVE);
		project1.setSlug("spring-boot");
		project1.setRepositoryUrl("https://github.com/spring-projects/spring-boot");
		ProjectMetadata project2 = new ProjectMetadata();
		project2.setName("Spring Data");
		project2.setStatus(SupportStatus.ACTIVE);
		project2.setSlug("spring-data");
		project2.setRepositoryUrl("https://github.com/spring-projects/spring-data");
		projects.add(project1);
		projects.add(project2);
		return projects;
	}

	@Test
	public void showProjectWhenProjectNotFoundShouldReturn404() throws Exception {
		given(this.contentfulService.getProjects()).willReturn(getProjects());
		this.mvc.perform(get("/projects/does-not-exist").accept(MediaTypes.HAL_JSON)).andExpect(status().isNotFound());
	}

	@Test
	public void showProject() throws Exception {
		List<ProjectMetadata> releases = getProjects();
		given(this.contentfulService.getProjects()).willReturn(releases);
		this.mvc.perform(get("/projects/spring-boot").accept(MediaTypes.HAL_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Spring Boot"))
				.andExpect(jsonPath("$._links.self.href").value("http://localhost/projects/spring-boot"))
				.andExpect(jsonPath("$._links.releases.href").value("http://localhost/projects/spring-boot/releases"))
				.andExpect(jsonPath("$._links.generations.href")
						.value("http://localhost/projects/spring-boot/generations"));
	}

}