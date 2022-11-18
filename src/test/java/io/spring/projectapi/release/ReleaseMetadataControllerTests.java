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
package io.spring.projectapi.release;

import java.util.ArrayList;
import java.util.List;

import io.spring.projectapi.ContentfulService;
import io.spring.projectapi.WebApiTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for {@link ReleaseMetadataController}.
 *
 * @author Madhura Bhave
 */
@WebApiTest
class ReleaseMetadataControllerTests {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private MockMvc mvc;

	@MockBean
	private ContentfulService contentfulService;

	@Test
	public void listReleases() throws Exception {
		List<ReleaseMetadata> releases = getReleases();
		given(this.contentfulService.getReleases(eq("spring-boot"))).willReturn(releases);
		this.mvc.perform(get("/projects/spring-boot/releases").accept(MediaTypes.HAL_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$._embedded.releases.length()").value("2"))
				.andExpect(jsonPath("$._embedded.releases[0].version").value("2.3.0.RELEASE"))
				.andExpect(jsonPath("$._embedded.releases[0].status").value("GENERAL_AVAILABILITY"))
				.andExpect(jsonPath("$._embedded.releases[0].current").value(true))
				.andExpect(jsonPath("$._embedded.releases[0].referenceDocUrl")
						.value("https://docs.spring.io/spring-boot/docs/2.3.0.RELEASE/reference/html/"))
				.andExpect(jsonPath("$._embedded.releases[0].apiDocUrl")
						.value("https://docs.spring.io/spring-boot/docs/2.3.0.RELEASE/api/"))
				.andExpect(jsonPath("$._embedded.releases[0]._links.self.href")
						.value("http://localhost/projects/spring-boot/releases/2.3.0.RELEASE"))
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

	private List<ReleaseMetadata> getReleases() {
		List<ReleaseMetadata> releases = new ArrayList<>();
		ReleaseMetadata release1 = new ReleaseMetadata();
		release1.setVersion("2.3.0.RELEASE");
		release1.setCurrent(true);
		release1.setStatus(ReleaseStatus.GENERAL_AVAILABILITY);
		release1.setApiDocUrl("https://docs.spring.io/spring-boot/docs/2.3.0.RELEASE/api/");
		release1.setReferenceDocUrl("https://docs.spring.io/spring-boot/docs/2.3.0.RELEASE/reference/html/");
		ReleaseMetadata release2 = new ReleaseMetadata();
		release2.setVersion("2.3.1-SNAPSHOT");
		release2.setStatus(ReleaseStatus.SNAPSHOT);
		release2.setApiDocUrl("https://docs.spring.io/spring-boot/docs/2.3.1-SNAPSHOT/api/");
		release2.setReferenceDocUrl("https://docs.spring.io/spring-boot/docs/2.3.1-SNAPSHOT/reference/html/");
		releases.add(release1);
		releases.add(release2);
		return releases;
	}

	@Test
	public void showReleaseWhenReleasesNotFoundShouldReturn404() throws Exception {
		given(this.contentfulService.getReleases(eq("spring-boot"))).willReturn(null);
		this.mvc.perform(get("/projects/spring-boot/releases").accept(MediaTypes.HAL_JSON))
				.andExpect(status().isNotFound());
	}

	@Test
	public void showRelease() throws Exception {
		List<ReleaseMetadata> releases = getReleases();
		given(this.contentfulService.getReleases(eq("spring-boot"))).willReturn(releases);
		this.mvc.perform(get("/projects/spring-boot/releases/2.3.0.RELEASE").accept(MediaTypes.HAL_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$.version").value("2.3.0.RELEASE"))
				.andExpect(jsonPath("$._links.self.href")
						.value("http://localhost/projects/spring-boot/releases/2.3.0.RELEASE"))
				.andExpect(jsonPath("$._links.repository.href").value("http://localhost/repositories/spring-releases"));
	}

	@Test
	void showCurrentRelease() throws Exception {
		List<ReleaseMetadata> releases = getReleases();
		given(this.contentfulService.getReleases(eq("spring-boot"))).willReturn(releases);
		this.mvc.perform(get("/projects/spring-boot/releases/current").accept(MediaTypes.HAL_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$.version").value("2.3.0.RELEASE"))
				.andExpect(jsonPath("$._links.self.href")
						.value("http://localhost/projects/spring-boot/releases/2.3.0.RELEASE"))
				.andExpect(jsonPath("$._links.repository.href").value("http://localhost/repositories/spring-releases"));
	}

	@Test
	void addRelease() throws Exception {

	}

	@Test
	void addReleaseWhenProjectDoesNotExist() throws Exception {

	}

	@Test
	void addReleaseWhenReleaseAlreadyExists() throws Exception {

	}

	@Test
	void deleteRelease() throws Exception {

	}

	@Test
	void deleteReleaseWhenReleaseDoesNotExist() throws Exception {

	}

	@Test
	void deleteReleaseWhenProjectDoesNotExist() throws Exception {

	}

}