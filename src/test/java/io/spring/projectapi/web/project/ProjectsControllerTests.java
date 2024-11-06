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

package io.spring.projectapi.web.project;

import java.util.ArrayList;
import java.util.List;

import io.spring.projectapi.ProjectRepository;
import io.spring.projectapi.github.GithubOperations;
import io.spring.projectapi.github.NoSuchGithubProjectException;
import io.spring.projectapi.github.Project.Status;
import io.spring.projectapi.test.WebApiTests;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.restdocs.hypermedia.LinksSnippet;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.halLinks;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.relaxedLinks;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test for {@link ProjectsController}.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
@WebApiTests(ProjectsController.class)
class ProjectsControllerTests {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private ProjectRepository projectRepository;

	@Test
	void projectsReturnsProjects() throws Exception {
		given(this.projectRepository.getProjects()).willReturn(getProjects());
		this.mvc.perform(get("/projects").accept(MediaTypes.HAL_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$._embedded.projects.length()").value("2"))
			.andExpect(jsonPath("$._embedded.projects[0].name").value("Spring Boot"))
			.andExpect(jsonPath("$._embedded.projects[0].slug").value("spring-boot"))
			.andExpect(jsonPath("$._embedded.projects[0].repositoryUrl")
				.value("https://github.com/spring-projects/spring-boot"))
			.andExpect(jsonPath("$._embedded.projects[0].status").value("ACTIVE"))
			.andExpect(jsonPath("$._embedded.projects[0]._links.self.href")
				.value("https://api.spring.io/projects/spring-boot"))
			.andExpect(jsonPath("$._embedded.projects[0]._links.releases.href")
				.value("https://api.spring.io/projects/spring-boot/releases"))
			.andExpect(jsonPath("$._embedded.projects[0]._links.generations.href")
				.value("https://api.spring.io/projects/spring-boot/generations"))
			.andExpect(jsonPath("$._embedded.projects[1].name").value("Spring Data"))
			.andExpect(jsonPath("$._embedded.projects[1].slug").value("spring-data"))
			.andExpect(jsonPath("$._embedded.projects[1].repositoryUrl")
				.value("https://github.com/spring-projects/spring-data"))
			.andExpect(jsonPath("$._embedded.projects[1].status").value("ACTIVE"))
			.andExpect(jsonPath("$._embedded.projects[1]._links.self.href")
				.value("https://api.spring.io/projects/spring-data"))
			.andExpect(jsonPath("$._embedded.projects[1]._links.releases.href")
				.value("https://api.spring.io/projects/spring-data/releases"))
			.andExpect(jsonPath("$._embedded.projects[1]._links.generations.href")
				.value("https://api.spring.io/projects/spring-data/generations"))
			.andExpect(jsonPath("$._links.project.href").value("https://api.spring.io/projects/{id}"))
			.andExpect(jsonPath("$._links.project.templated").value(true))
			.andDo(document("list-projects", preprocessResponse(prettyPrint()), indexLinks(),
					responseFields(fieldWithPath("_embedded.projects").description("An array of Projects"))
						.andWithPrefix("_embedded.projects[]", projectPayload())
						.and(subsectionWithPath("_links").description("Links to other resources"))));
	}

	@Test
	void projectWhenNotFoundReturns404() throws Exception {
		given(this.projectRepository.getProject("does-not-exist")).willThrow(NoSuchGithubProjectException.class);
		this.mvc.perform(get("/projects/does-not-exist").accept(MediaTypes.HAL_JSON)).andExpect(status().isNotFound());
	}

	@Test
	void projectReturnsProject() throws Exception {
		given(this.projectRepository.getProject("spring-boot")).willReturn(getProjects().get(0));
		this.mvc.perform(get("/projects/spring-boot").accept(MediaTypes.HAL_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Spring Boot"))
			.andExpect(jsonPath("$._links.self.href").value("https://api.spring.io/projects/spring-boot"))
			.andExpect(jsonPath("$._links.releases.href").value("https://api.spring.io/projects/spring-boot/releases"))
			.andExpect(jsonPath("$._links.generations.href")
				.value("https://api.spring.io/projects/spring-boot/generations"))
			.andDo(document("show-project", preprocessResponse(prettyPrint()), projectLinks(),
					responseFields(projectPayload())));
	}

	private List<io.spring.projectapi.github.Project> getProjects() {
		List<io.spring.projectapi.github.Project> projects = new ArrayList<>();
		projects.add(new io.spring.projectapi.github.Project("Spring Boot", "spring-boot",
				"https://github.com/spring-projects/spring-boot", Status.ACTIVE));
		projects.add(new io.spring.projectapi.github.Project("Spring Data", "spring-data",
				"https://github.com/spring-projects/spring-data", Status.ACTIVE));
		return projects;
	}

	FieldDescriptor[] projectPayload() {
		return new FieldDescriptor[] { fieldWithPath("name").type(JsonFieldType.STRING).description("Project Name"),
				fieldWithPath("slug").type(JsonFieldType.STRING).description("URL-friendly name of the project"),
				fieldWithPath("repositoryUrl").type(JsonFieldType.STRING).description("URL for the source repository"),
				fieldWithPath("status").type(JsonFieldType.STRING)
					.description("<<project-status, Support status>> of the project"),
				subsectionWithPath("_links").description("Links to other resources") };
	}

	LinksSnippet indexLinks() {
		return relaxedLinks(halLinks(),
				linkWithRel("project").description("Link to a particular <<project, Project>>"));
	}

	LinksSnippet projectLinks() {
		return links(halLinks(), linkWithRel("self").description("Canonical self link"),
				linkWithRel("parent").optional().description("Link to <<project, parent Project>>, if any"),
				linkWithRel("generations").optional().description("Link to <<generation, Generations>>"),
				linkWithRel("releases").optional().description("Link to <<release, Releases>>"));
	}

}
