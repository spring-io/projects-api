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

package io.spring.projectapi.web.repository;

import io.spring.projectapi.test.WebApiTest;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.restdocs.hypermedia.LinksSnippet;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.halLinks;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
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
 * Tests for {@link RepositoriesController}.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
@WebApiTest(RepositoriesController.class)
class RepositoriesControllerTests {

	@Autowired
	private MockMvc mvc;

	@Test
	void repositoriesReturnsRepositories() throws Exception {
		this.mvc.perform(get("/repositories").accept(MediaTypes.HAL_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$._embedded.repositories.length()").value("3"))
				.andDo(document("{method-name}", preprocessResponse(prettyPrint()),
						responseFields(fieldWithPath("_embedded.repositories").description("An array of Repositories"))
								.andWithPrefix("_embedded.repositories[]", repositoryPayload())));
	}

	@Test
	void repositoryReturnsRepository() throws Exception {
		this.mvc.perform(get("/repositories/spring-releases").accept(MediaTypes.HAL_JSON)).andExpect(status().isOk())
				.andDo(document("{method-name}", preprocessResponse(prettyPrint()), responseFields(repositoryPayload()),
						repositoryLinks()));
	}

	@Test
	void repositoryWhenNotFoundReturns404() throws Exception {
		this.mvc.perform(get("/repositories/does-not-exist").accept(MediaTypes.HAL_JSON))
				.andExpect(status().isNotFound());
	}

	FieldDescriptor[] repositoryPayload() {
		return new FieldDescriptor[] {
				fieldWithPath("identifier").type(JsonFieldType.STRING).description("Repository Identifier"),
				fieldWithPath("name").type(JsonFieldType.STRING).description("Name of the Repository"),
				fieldWithPath("url").type(JsonFieldType.STRING).description("URL of the Repository"),
				fieldWithPath("snapshotsEnabled").type(JsonFieldType.BOOLEAN)
						.description("Whether SNAPSHOT artifacts are hosted on this Repository"),
				subsectionWithPath("_links").description("Links to other resources") };
	}

	LinksSnippet repositoryLinks() {
		return links(halLinks(), linkWithRel("self").description("Canonical self link"));
	}

}
