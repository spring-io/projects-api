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

package io.spring.projectapi.web.project;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.projectapi.github.GithubOperations;
import io.spring.projectapi.github.NoSuchGithubProjectException;
import io.spring.projectapi.github.Project.Status;
import io.spring.projectapi.test.ConstrainedFields;
import io.spring.projectapi.test.WebApiTests;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.FileCopyUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for {@link ProjectDetailsController}.
 *
 * @author Madhura Bhave
 */
@WebApiTests(ProjectDetailsController.class)
class ProjectDetailsControllerTests {

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private GithubOperations githubOperations;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@WithMockUser(roles = "ADMIN")
	void patchProjectDetailsWhenNotFoundReturns404() throws Exception {
		willThrow(NoSuchGithubProjectException.class).given(this.githubOperations)
			.patchProjectDetails(eq("does-not-exist"), any());
		this.mvc
			.perform(patch("/projects/does-not-exist/details").contentType(MediaType.APPLICATION_JSON)
				.content(from("patch.json")))
			.andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void patchProjectDetails() throws Exception {
		ConstrainedFields fields = ConstrainedFields.constraintsOn(ProjectDetails.class);
		this.mvc
			.perform(patch("/projects/spring-boot/details").contentType(MediaType.APPLICATION_JSON)
				.content(from("patch.json")))
			.andExpect(status().isNoContent())
			.andDo(document("patch-project-details", preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestFields(fields.withPath("bootConfig").description("Spring Boot Config"),
							fields.withPath("body").description("Project Body"))));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void patchProjectDetailsWithMissingField() throws Exception {
		io.spring.projectapi.github.Project project = new io.spring.projectapi.github.Project("Spring Boot",
				"spring-boot", "https://github.com/spring-projects/spring-boot", Status.ACTIVE);
		this.mvc
			.perform(patch("/projects/spring-boot/details").contentType(MediaType.APPLICATION_JSON)
				.content(from("patch-field-missing.json")))
			.andExpect(status().isNoContent());
		ArgumentCaptor<io.spring.projectapi.github.ProjectDetails> captor = ArgumentCaptor
			.forClass(io.spring.projectapi.github.ProjectDetails.class);
		verify(this.githubOperations).patchProjectDetails(eq("spring-boot"), captor.capture());
		io.spring.projectapi.github.ProjectDetails value = captor.getValue();
		assertThat(value.getBody()).isNull();
		assertThat(value.getSpringBootConfig()).isEqualTo("new sbc");
	}

	@Test
	void patchWhenHasNoAdminRoleReturnsUnauthorized() throws Exception {
		this.mvc
			.perform(patch("/projects/spring-boot/details").contentType(MediaType.APPLICATION_JSON)
				.content(from("patch.json")))
			.andExpect(status().isUnauthorized());
	}

	private byte[] from(String path) throws IOException {
		ClassPathResource resource = new ClassPathResource(path, getClass());
		try (InputStream inputStream = resource.getInputStream()) {
			return FileCopyUtils.copyToByteArray(inputStream);
		}
	}

}
