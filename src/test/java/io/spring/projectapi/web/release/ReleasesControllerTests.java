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

import io.spring.projectapi.contentful.ContentfulService;
import io.spring.projectapi.contentful.NoSuchContentfulProjectException;
import io.spring.projectapi.contentful.ProjectDocumentation;
import io.spring.projectapi.contentful.ProjectDocumentation.Status;
import io.spring.projectapi.test.WebApiTest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.FileCopyUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
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
@WebApiTest(ReleasesController.class)
class ReleasesControllerTests {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private ContentfulService contentfulService;

	@Test
	void releasesReturnsReleases() throws Exception {
		given(this.contentfulService.getProjectDocumentations("spring-boot")).willReturn(getProjectDocumentations());
		this.mvc.perform(get("/projects/spring-boot/releases").accept(MediaTypes.HAL_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$._embedded.releases.length()").value("2"))
				.andExpect(jsonPath("$._embedded.releases[0].version").value("2.3.0"))
				.andExpect(jsonPath("$._embedded.releases[0].status").value("GENERAL_AVAILABILITY"))
				.andExpect(jsonPath("$._embedded.releases[0].current").value(true))
				.andExpect(jsonPath("$._embedded.releases[0].referenceDocUrl")
						.value("https://docs.spring.io/spring-boot/docs/2.3.0/reference/html/"))
				.andExpect(jsonPath("$._embedded.releases[0].apiDocUrl")
						.value("https://docs.spring.io/spring-boot/docs/2.3.0/api/"))
				.andExpect(jsonPath("$._embedded.releases[0]._links.self.href")
						.value("http://localhost/projects/spring-boot/releases/2.3.0"))
				.andExpect(jsonPath("$._embedded.releases[0]._links.repository.href")
						.value("http://localhost/repositories/spring-releases"))
				.andExpect(jsonPath("$._embedded.releases[1].version").value("2.3.1-SNAPSHOT"))
				.andExpect(jsonPath("$._embedded.releases[1].status").value("SNAPSHOT"))
				.andExpect(jsonPath("$._embedded.releases[1].current").value(false))
				.andExpect(jsonPath("$._embedded.releases[1].referenceDocUrl")
						.value("https://docs.spring.io/spring-boot/docs/2.3.1-SNAPSHOT/reference/html/"))
				.andExpect(jsonPath("$._embedded.releases[1].apiDocUrl")
						.value("https://docs.spring.io/spring-boot/docs/2.3.1-SNAPSHOT/api/"))
				.andExpect(jsonPath("$._embedded.releases[1]._links.self.href")
						.value("http://localhost/projects/spring-boot/releases/2.3.1-SNAPSHOT"))
				.andExpect(jsonPath("$._embedded.releases[1]._links.repository.href")
						.value("http://localhost/repositories/spring-snapshots"))
				.andExpect(jsonPath("$._links.current.href")
						.value("http://localhost/projects/spring-boot/releases/current"))
				.andExpect(jsonPath("$._links.project.href").value("http://localhost/projects/spring-boot"));
	}

	@Test
	void releasesWhenNotFoundReturns404() throws Exception {
		given(this.contentfulService.getProjectDocumentations("spring-boot"))
				.willThrow(NoSuchContentfulProjectException.class);
		this.mvc.perform(get("/projects/spring-boot/releases").accept(MediaTypes.HAL_JSON))
				.andExpect(status().isNotFound());
	}

	@Test
	void releaseReturnsRelease() throws Exception {
		given(this.contentfulService.getProjectDocumentations("spring-boot")).willReturn(getProjectDocumentations());
		this.mvc.perform(get("/projects/spring-boot/releases/2.3.0").accept(MediaTypes.HAL_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$.version").value("2.3.0"))
				.andExpect(jsonPath("$._links.self.href").value("http://localhost/projects/spring-boot/releases/2.3.0"))
				.andExpect(jsonPath("$._links.repository.href").value("http://localhost/repositories/spring-releases"));
	}

	@Test
	void currentReturnsCurrentRelease() throws Exception {
		given(this.contentfulService.getProjectDocumentations("spring-boot")).willReturn(getProjectDocumentations());
		this.mvc.perform(get("/projects/spring-boot/releases/current").accept(MediaTypes.HAL_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$.version").value("2.3.0"))
				.andExpect(jsonPath("$._links.self.href").value("http://localhost/projects/spring-boot/releases/2.3.0"))
				.andExpect(jsonPath("$._links.repository.href").value("http://localhost/repositories/spring-releases"));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void addAddsRelease() throws Exception {
		given(this.contentfulService.getProjectDocumentations("spring-boot")).willReturn(getProjectDocumentations());
		String expectedLocation = "http://localhost/projects/spring-boot/releases/2.8.0";
		this.mvc.perform(post("/projects/spring-boot/releases").accept(MediaTypes.HAL_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(from("add.json"))).andExpect(status().isCreated())
				.andExpect(header().string("Location", expectedLocation));
		ArgumentCaptor<ProjectDocumentation> captor = ArgumentCaptor.forClass(ProjectDocumentation.class);
		verify(this.contentfulService).addProjectDocumentation(eq("spring-boot"), captor.capture());
		ProjectDocumentation added = captor.getValue();
		assertThat(added.getVersion()).isEqualTo("2.8.0");
		assertThat(added.getApi()).isEqualTo("https://docs.spring.io/spring-boot/docs/{version}/api/");
		assertThat(added.getRef()).isEqualTo("https://docs.spring.io/spring-boot/docs/{version}/reference/html/");
		assertThat(added.getStatus()).isEqualTo(ProjectDocumentation.Status.GENERAL_AVAILABILITY);
		assertThat(added.getRepository()).isEqualTo("spring-releases");
		assertThat(added.isCurrent()).isFalse();
		// FIXME will Contentful compute the current release like Sagan?
		// sagan.site.projects.Project.computeCurrentRelease()
	}

	@Test
	void addWhenHasNoAdminRoleReturnsUnauthorized() throws Exception {
		this.mvc.perform(post("/projects/spring-boot/releases").accept(MediaTypes.HAL_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(from("add.json")))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void addWhenProjectDoesNotExistReturnsNotFound() throws Exception {
		given(this.contentfulService.getProjectDocumentations("spring-boot"))
				.willThrow(NoSuchContentfulProjectException.class);
		this.mvc.perform(post("/projects/spring-boot/releases").accept(MediaTypes.HAL_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(from("add.json"))).andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void addWhenReleaseAlreadyExistsReturnsBadRequest() throws Exception {
		given(this.contentfulService.getProjectDocumentations("spring-boot")).willReturn(getProjectDocumentations());
		this.mvc.perform(post("/projects/spring-boot/releases").accept(MediaTypes.HAL_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(from("add-already-exists.json")))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void deleteDeletesDocumentation() throws Exception {
		given(this.contentfulService.getProjectDocumentations("spring-boot")).willReturn(getProjectDocumentations());
		this.mvc.perform(delete("/projects/spring-boot/releases/2.3.0").accept(MediaTypes.HAL_JSON))
				.andExpect(status().isNoContent());
		verify(this.contentfulService).deleteDocumentation("spring-boot", "2.3.0");
	}

	@Test
	void deleteWhenHasNoAdminRoleReturnsUnauthorized() throws Exception {
		this.mvc.perform(delete("/projects/spring-boot/releases/2.3.0").accept(MediaTypes.HAL_JSON))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void deleteWhenProjectDoesNotExistReturnsNotFound() throws Exception {
		willThrow(NoSuchContentfulProjectException.class).given(this.contentfulService)
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
		result.add(new ProjectDocumentation("2.3.0", docsRoot + "api/", docsRoot + "reference/html/",
				Status.GENERAL_AVAILABILITY, null, true));
		docsRoot = "https://docs.spring.io/spring-boot/docs/2.3.1-SNAPSHOT/";
		result.add(new ProjectDocumentation("2.3.1-SNAPSHOT", docsRoot + "api/", docsRoot + "reference/html/",
				Status.SNAPSHOT, null, false));
		return result;
	}

}
