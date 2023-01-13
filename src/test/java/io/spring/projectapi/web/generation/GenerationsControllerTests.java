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

package io.spring.projectapi.web.generation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import io.spring.projectapi.contentful.ContentfulService;
import io.spring.projectapi.contentful.NoSuchContentfulProjectException;
import io.spring.projectapi.contentful.ProjectSupport;
import io.spring.projectapi.test.WebApiTest;
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
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for {@link GenerationsController}.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
@WebApiTest
class GenerationsControllerTests {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private ContentfulService contentfulService;

	@Test
	void generationsReturnsGenerations() throws Exception {
		given(this.contentfulService.getProjectSupports("spring-boot")).willReturn(getProjectSupports());
		this.mvc.perform(get("/projects/spring-boot/generations").accept(MediaTypes.HAL_JSON)).andDo(print())
				.andExpect(status().isOk()).andExpect(jsonPath("$._embedded.generations.length()").value("2"))
				.andExpect(jsonPath("$._embedded.generations[0].name").value("2.2.x"))
				.andExpect(jsonPath("$._embedded.generations[0].initialReleaseDate").value("2020-02-01"))
				.andExpect(jsonPath("$._embedded.generations[0].ossSupportEndDate").value("2020-02-03"))
				.andExpect(jsonPath("$._embedded.generations[0].commercialSupportEndDate").value("2020-02-05"))
				.andExpect(jsonPath("$._embedded.generations[0]._links.self.href")
						.value("https://api.spring.io/projects/spring-boot/generations/2.2.x"))
				.andExpect(jsonPath("$._embedded.generations[0]._links.project.href")
						.value("https://api.spring.io/projects/spring-boot"))
				.andExpect(jsonPath("$._embedded.generations[1].name").value("2.1.x"))
				.andExpect(jsonPath("$._embedded.generations[1].initialReleaseDate").value("2020-01-01"))
				.andExpect(jsonPath("$._embedded.generations[1].ossSupportEndDate").value("2020-01-03"))
				.andExpect(jsonPath("$._embedded.generations[1].commercialSupportEndDate").value("2020-01-05"))
				.andExpect(jsonPath("$._embedded.generations[1]._links.self.href")
						.value("https://api.spring.io/projects/spring-boot/generations/2.1.x"))
				.andExpect(jsonPath("$._embedded.generations[1]._links.project.href")
						.value("https://api.spring.io/projects/spring-boot"))
				.andExpect(jsonPath("$._links.project.href").value("https://api.spring.io/projects/spring-boot"))
				.andDo(document("list-generations", preprocessResponse(prettyPrint()), generationsLinks(),
						responseFields(
								fieldWithPath("_embedded.generations").description("An array of Project Generations"))
										.andWithPrefix("_embedded.generations[]", generationPayload())
										.and(subsectionWithPath("_links").description("Links to other resources"))));
	}

	@Test
	void generationReturnsGeneration() throws Exception {
		given(this.contentfulService.getProjectSupports("spring-boot")).willReturn(getProjectSupports());
		this.mvc.perform(get("/projects/spring-boot/generations/2.2.x").accept(MediaTypes.HAL_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$.name").value("2.2.x"))
				.andExpect(jsonPath("$._links.self.href")
						.value("https://api.spring.io/projects/spring-boot/generations/2.2.x"))
				.andExpect(jsonPath("$._links.project.href").value("https://api.spring.io/projects/spring-boot"))
				.andDo(document("show-generation", preprocessResponse(prettyPrint()), generationLinks(),
						responseFields(generationPayload())));
	}

	@Test
	void generationWhenNotFoundReturns404() throws Exception {
		given(this.contentfulService.getProjectSupports("spring-boot"))
				.willThrow(NoSuchContentfulProjectException.class);
		this.mvc.perform(get("/projects/spring-boot/generations").accept(MediaTypes.HAL_JSON))
				.andExpect(status().isNotFound());
	}

	private List<ProjectSupport> getProjectSupports() {
		List<ProjectSupport> result = new ArrayList<>();
		result.add(new ProjectSupport("2.2.x", LocalDate.parse("2020-02-01"), LocalDate.parse("2020-02-02"),
				LocalDate.parse("2020-02-03"), LocalDate.parse("2020-02-04"), LocalDate.parse("2020-02-05")));
		result.add(new ProjectSupport("2.1.x", LocalDate.parse("2020-01-01"), LocalDate.parse("2020-01-02"),
				LocalDate.parse("2020-01-03"), LocalDate.parse("2020-01-04"), LocalDate.parse("2020-01-05")));
		return result;
	}

	FieldDescriptor[] generationPayload() {
		return new FieldDescriptor[] { fieldWithPath("name").type(JsonFieldType.STRING).description("Generation Name"),
				fieldWithPath("initialReleaseDate").type(JsonFieldType.STRING)
						.description("Date of the first release for this Generation"),
				fieldWithPath("ossSupportEndDate").type(JsonFieldType.STRING)
						.description("End date of the OSS support"),
				fieldWithPath("commercialSupportEndDate").type(JsonFieldType.STRING)
						.description("End date of the Commercial support"),
				subsectionWithPath("_links").description("Links to other resources") };
	}

	LinksSnippet generationsLinks() {
		return links(halLinks(), linkWithRel("project").description("Link to Project"));
	}

	LinksSnippet generationLinks() {
		return links(halLinks(), linkWithRel("self").description("Canonical self link"),
				linkWithRel("project").description("Link to Project"));
	}

}
