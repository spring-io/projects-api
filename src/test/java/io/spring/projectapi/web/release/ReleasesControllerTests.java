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

package io.spring.projectapi.web.release;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.spring.projectapi.ProjectRepository;
import io.spring.projectapi.github.GithubOperations;
import io.spring.projectapi.github.NoSuchGithubProjectException;
import io.spring.projectapi.github.ProjectDocumentation;
import io.spring.projectapi.github.ProjectDocumentation.Status;
import io.spring.projectapi.test.ConstrainedFields;
import io.spring.projectapi.test.WebApiTests;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.restdocs.hypermedia.LinkDescriptor;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.FileCopyUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for {@link ReleasesController}.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
@WebApiTests(ReleasesController.class)
class ReleasesControllerTests {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private GithubOperations githubOperations;

	@MockBean
	private ProjectRepository projectRepository;

	@Test
	void releasesReturnsReleases() throws Exception {
		given(this.projectRepository.getProjectDocumentations("spring-boot")).willReturn(getProjectDocumentations());
		this.mvc.perform(get("/projects/spring-boot/releases").accept(MediaTypes.HAL_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$._embedded.releases.length()").value("2"))
			.andExpect(jsonPath("$._embedded.releases[0].version").value("2.3.0"))
			.andExpect(jsonPath("$._embedded.releases[0].status").value("GENERAL_AVAILABILITY"))
			.andExpect(jsonPath("$._embedded.releases[0].current").value(true))
			.andExpect(jsonPath("$._embedded.releases[0].referenceDocUrl")
				.value("https://docs.spring.io/spring-boot/docs/2.3.0/reference/html/"))
			.andExpect(jsonPath("$._embedded.releases[0].apiDocUrl")
				.value("https://docs.spring.io/spring-boot/docs/2.3.0/api/"))
			.andExpect(jsonPath("$._embedded.releases[0]._links.self.href")
				.value("https://api.spring.io/projects/spring-boot/releases/2.3.0"))
			.andExpect(jsonPath("$._embedded.releases[0]._links.repository.href")
				.value("https://api.spring.io/repositories/spring-releases"))
			.andExpect(jsonPath("$._embedded.releases[1].version").value("2.3.1-SNAPSHOT"))
			.andExpect(jsonPath("$._embedded.releases[1].status").value("SNAPSHOT"))
			.andExpect(jsonPath("$._embedded.releases[1].current").value(false))
			.andExpect(jsonPath("$._embedded.releases[1].referenceDocUrl")
				.value("https://docs.spring.io/spring-boot/docs/2.3.1-SNAPSHOT/reference/html/"))
			.andExpect(jsonPath("$._embedded.releases[1].apiDocUrl")
				.value("https://docs.spring.io/spring-boot/docs/2.3.1-SNAPSHOT/api/"))
			.andExpect(jsonPath("$._embedded.releases[1]._links.self.href")
				.value("https://api.spring.io/projects/spring-boot/releases/2.3.1-SNAPSHOT"))
			.andExpect(jsonPath("$._embedded.releases[1]._links.repository.href")
				.value("https://api.spring.io/repositories/spring-snapshots"))
			.andExpect(jsonPath("$._links.current.href")
				.value("https://api.spring.io/projects/spring-boot/releases/current"))
			.andExpect(jsonPath("$._links.project.href").value("https://api.spring.io/projects/spring-boot"))
			.andDo(document("list-releases", preprocessResponse(prettyPrint()),
					responseFields(fieldWithPath("_embedded.releases").description("An array of Project Releases"))
						.andWithPrefix("_embedded.releases[]", releasePayload())
						.and(subsectionWithPath("_links").description("Links to other resources")),
					links(releasesLinks())));
	}

	@Test
	void releasesWhenNotFoundReturns404() throws Exception {
		given(this.projectRepository.getProjectDocumentations("spring-boot"))
			.willThrow(NoSuchGithubProjectException.class);
		this.mvc.perform(get("/projects/spring-boot/releases").accept(MediaTypes.HAL_JSON))
			.andExpect(status().isNotFound());
	}

	@Test
	void releaseReturnsRelease() throws Exception {
		given(this.projectRepository.getProjectDocumentations("spring-boot")).willReturn(getProjectDocumentations());
		this.mvc.perform(get("/projects/spring-boot/releases/2.3.0").accept(MediaTypes.HAL_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.version").value("2.3.0"))
			.andExpect(
					jsonPath("$._links.self.href").value("https://api.spring.io/projects/spring-boot/releases/2.3.0"))
			.andExpect(jsonPath("$._links.repository.href").value("https://api.spring.io/repositories/spring-releases"))
			.andDo(document("show-release", preprocessResponse(prettyPrint()), responseFields(releasePayload()),
					links(releaseLinks())));
	}

	@Test
	void currentReturnsCurrentRelease() throws Exception {
		given(this.projectRepository.getProjectDocumentations("spring-boot")).willReturn(getProjectDocumentations());
		this.mvc.perform(get("/projects/spring-boot/releases/current").accept(MediaTypes.HAL_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.version").value("2.3.0"))
			.andExpect(
					jsonPath("$._links.self.href").value("https://api.spring.io/projects/spring-boot/releases/2.3.0"))
			.andExpect(
					jsonPath("$._links.repository.href").value("https://api.spring.io/repositories/spring-releases"));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void addAddsRelease() throws Exception {
		given(this.projectRepository.getProjectDocumentations("spring-boot")).willReturn(getProjectDocumentations());
		String expectedLocation = "https://api.spring.io/projects/spring-boot/releases/2.8.0";
		ConstrainedFields fields = ConstrainedFields.constraintsOn(NewRelease.class);
		this.mvc
			.perform(post("/projects/spring-boot/releases").accept(MediaTypes.HAL_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(from("add.json")))
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", expectedLocation));
		ArgumentCaptor<ProjectDocumentation> captor = ArgumentCaptor.forClass(ProjectDocumentation.class);
		verify(this.githubOperations).addProjectDocumentation(eq("spring-boot"), captor.capture());
		ProjectDocumentation added = captor.getValue();
		assertThat(added.getVersion()).isEqualTo("2.8.0");
		assertThat(added.getApi()).isEqualTo("https://docs.spring.io/spring-boot/docs/{version}/api/");
		assertThat(added.getRef()).isEqualTo("https://docs.spring.io/spring-boot/docs/{version}/reference/html/");
		assertThat(added.getStatus()).isEqualTo(ProjectDocumentation.Status.GENERAL_AVAILABILITY);
		assertThat(added.isCurrent()).isFalse();
		assertThat(added.isAntora()).isFalse();
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void addWithAntoraVersionAddsRelease() throws Exception {
		given(this.projectRepository.getProjectDocumentations("spring-boot")).willReturn(getProjectDocumentations());
		String expectedLocation = "https://api.spring.io/projects/spring-boot/releases/2.8.0";
		ConstrainedFields fields = ConstrainedFields.constraintsOn(NewRelease.class);
		this.mvc
			.perform(post("/projects/spring-boot/releases").accept(MediaTypes.HAL_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(from("add-with-antora.json")))
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", expectedLocation))
			.andDo(document("create-release", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
					requestFields(fields.withPath("version").description("The Release version"), fields
						.withPath("isAntora")
						.type(JsonFieldType.BOOLEAN)
						.optional()
						.description(
								"Indicates if the documentation for this release is on Antora. Defaults to false if not specified."),
							fields.withPath("referenceDocUrl")
								.description(
										"URL of the reference documentation, {version} template variable is supported"),
							fields.withPath("apiDocUrl")
								.description(
										"URL of the API documentation, {version} template variable is supported"))));
		ArgumentCaptor<ProjectDocumentation> captor = ArgumentCaptor.forClass(ProjectDocumentation.class);
		verify(this.githubOperations).addProjectDocumentation(eq("spring-boot"), captor.capture());
		ProjectDocumentation added = captor.getValue();
		assertThat(added.getVersion()).isEqualTo("2.8.0");
		assertThat(added.getApi()).isEqualTo("https://docs.spring.io/spring-boot/docs/{version}/api/");
		assertThat(added.getRef()).isEqualTo("https://docs.spring.io/spring-boot/docs/{version}/reference/html/");
		assertThat(added.getStatus()).isEqualTo(ProjectDocumentation.Status.GENERAL_AVAILABILITY);
		assertThat(added.isCurrent()).isFalse();
		assertThat(added.isAntora()).isTrue();
	}

	@Test
	void addWhenHasNoAdminRoleReturnsUnauthorized() throws Exception {
		this.mvc
			.perform(post("/projects/spring-boot/releases").accept(MediaTypes.HAL_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(from("add.json")))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void addWhenProjectDoesNotExistReturnsNotFound() throws Exception {
		given(this.projectRepository.getProjectDocumentations("spring-boot"))
			.willThrow(NoSuchGithubProjectException.class);
		this.mvc
			.perform(post("/projects/spring-boot/releases").accept(MediaTypes.HAL_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(from("add.json")))
			.andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void addWhenReleaseAlreadyExistsReturnsBadRequest() throws Exception {
		given(this.projectRepository.getProjectDocumentations("spring-boot")).willReturn(getProjectDocumentations());
		this.mvc
			.perform(post("/projects/spring-boot/releases").accept(MediaTypes.HAL_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(from("add-already-exists.json")))
			.andExpect(status().isBadRequest());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void deleteDeletesDocumentation() throws Exception {
		given(this.projectRepository.getProjectDocumentations("spring-boot")).willReturn(getProjectDocumentations());
		this.mvc.perform(delete("/projects/spring-boot/releases/2.3.0").accept(MediaTypes.HAL_JSON))
			.andExpect(status().isNoContent())
			.andDo(document("delete-release"));
		verify(this.githubOperations).deleteDocumentation("spring-boot", "2.3.0");
	}

	@Test
	void deleteWhenHasNoAdminRoleReturnsUnauthorized() throws Exception {
		this.mvc.perform(delete("/projects/spring-boot/releases/2.3.0").accept(MediaTypes.HAL_JSON))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void deleteWhenProjectDoesNotExistReturnsNotFound() throws Exception {
		willThrow(NoSuchGithubProjectException.class).given(this.githubOperations)
			.deleteDocumentation("spring-boot", "2.3.0");
		this.mvc.perform(delete("/projects/spring-boot/releases/2.3.0").accept(MediaTypes.HAL_JSON))
			.andExpect(status().isNotFound());
	}

	private byte[] from(String path) throws IOException {
		ClassPathResource resource = new ClassPathResource(path, getClass());
		try (InputStream inputStream = resource.getInputStream()) {
			return FileCopyUtils.copyToByteArray(inputStream);
		}
	}

	private List<ProjectDocumentation> getProjectDocumentations() {
		List<ProjectDocumentation> result = new ArrayList<>();
		String docsRoot;
		docsRoot = "https://docs.spring.io/spring-boot/docs/2.3.0/";
		result.add(new ProjectDocumentation("2.3.0", false, docsRoot + "api/", docsRoot + "reference/html/",
				Status.GENERAL_AVAILABILITY, true));
		docsRoot = "https://docs.spring.io/spring-boot/docs/2.3.1-SNAPSHOT/";
		result.add(new ProjectDocumentation("2.3.1-SNAPSHOT", false, docsRoot + "api/", docsRoot + "reference/html/",
				Status.SNAPSHOT, false));
		return result;
	}

	FieldDescriptor[] releasePayload() {
		return new FieldDescriptor[] {
				fieldWithPath("version").type(JsonFieldType.STRING).description("Release Version string"),
				fieldWithPath("status").type(JsonFieldType.STRING)
					.description("<<release-status, Status of this Release>>"),
				fieldWithPath("referenceDocUrl").type(JsonFieldType.STRING)
					.description("URL for the reference documentation"),
				fieldWithPath("apiDocUrl").type(JsonFieldType.STRING).description("URL for the API documentation"),
				fieldWithPath("current").type(JsonFieldType.BOOLEAN)
					.description("Whether this release is the most recent, officially supported"),
				subsectionWithPath("_links").description("Links to other resources") };
	}

	LinkDescriptor[] releasesLinks() {
		return new LinkDescriptor[] { linkWithRel("project").description("Link to Project"),
				linkWithRel("current").optional().description("Link to the <<release, Release>> marked as current") };
	}

	LinkDescriptor[] releaseLinks() {
		return new LinkDescriptor[] { linkWithRel("self").description("Canonical self link"),
				linkWithRel("repository").description("Link to the <<repository, Repository>> hosting this Release") };
	}

}
