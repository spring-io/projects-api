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

import java.util.ArrayList;
import java.util.List;

import io.spring.projectapi.ProjectRepository;
import io.spring.projectapi.github.GithubOperations;
import io.spring.projectapi.github.ProjectDocumentation;
import io.spring.projectapi.github.ProjectDocumentation.Status;
import io.spring.projectapi.test.WebApiTests;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for {@link LegacyReleasesController}.
 *
 * @author Brian Clozel
 */
@WebApiTests(LegacyReleasesController.class)
class LegacyReleasesControllerTests {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private GithubOperations githubOperations;

	@MockBean
	private ProjectRepository projectRepository;

	@Test
	void legacyReleasesReturnsReleases() throws Exception {
		given(this.projectRepository.getProjectDocumentations("spring-boot")).willReturn(getProjectDocumentations());
		this.mvc.perform(get("/project_metadata/spring-boot").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value("spring-boot"))
			.andExpect(jsonPath("$.name").value("Spring Boot"))
			.andExpect(jsonPath("$.projectReleases.length()").value("2"))
			.andExpect(jsonPath("$.projectReleases[0].version").value("2.3.0"))
			.andExpect(jsonPath("$.projectReleases[0].versionDisplayName").value("2.3.0"))
			.andExpect(jsonPath("$.projectReleases[0].current").value(true))
			.andExpect(jsonPath("$.projectReleases[0].releaseStatus").value("GENERAL_AVAILABILITY"))
			.andExpect(jsonPath("$.projectReleases[0].snapshot").value(false))
			.andExpect(jsonPath("$.projectReleases[1].version").value("2.3.1-SNAPSHOT"))
			.andExpect(jsonPath("$.projectReleases[1].versionDisplayName").value("2.3.1-SNAPSHOT"))
			.andExpect(jsonPath("$.projectReleases[1].current").value(false))
			.andExpect(jsonPath("$.projectReleases[1].releaseStatus").value("SNAPSHOT"))
			.andExpect(jsonPath("$.projectReleases[1].snapshot").value(true));
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

}
