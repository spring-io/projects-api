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

package io.spring.projectapi.web.generation;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import io.spring.projectapi.ProjectRepository;
import io.spring.projectapi.github.NoSuchGithubProjectException;
import io.spring.projectapi.github.ProjectGeneration;
import io.spring.projectapi.github.ProjectGeneration.SupportType;
import io.spring.projectapi.test.WebApiTests;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.restdocs.hypermedia.LinksSnippet;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.JsonPathResultMatchers;

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
@WebApiTests(GenerationsController.class)
class GenerationsControllerTests {

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private ProjectRepository projectRepository;

	@Test
	void generationsReturnsGenerations() throws Exception {
		given(this.projectRepository.getProjectGenerations("spring-boot")).willReturn(getProjectGenerations());
		given(this.projectRepository.getProjectSupportPolicy("spring-boot")).willReturn("SPRING_BOOT");
		this.mvc.perform(get("/projects/spring-boot/generations").accept(MediaTypes.HAL_JSON))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$._embedded.generations.length()").value("3"))
			.andExpect(generationJsonPath(0, "name").value("1.0.x"))
			.andExpect(generationJsonPath(0, "initialReleaseDate").value("2015-03-31"))
			.andExpect(generationJsonPath(0, "support").value("none"))
			.andExpect(generationJsonPath(0, "ossSupportEndDate").doesNotExist())
			.andExpect(generationJsonPath(0, "commercialSupportEndDate").doesNotExist())
			.andExpect(generationJsonPath(0, "_links.self.href")
				.value("https://api.spring.io/projects/spring-boot/generations/1.0.x"))
			.andExpect(generationJsonPath(0, "_links.project.href").value("https://api.spring.io/projects/spring-boot"))

			.andExpect(generationJsonPath(1, "name").value("2.2.x"))
			.andExpect(generationJsonPath(1, "initialReleaseDate").value("2020-02-29"))
			.andExpect(generationJsonPath(1, "support").value("extended"))
			.andExpect(generationJsonPath(1, "ossSupportEndDate").value("2021-03-31"))
			.andExpect(generationJsonPath(1, "commercialSupportEndDate").value("2021-03-31"))
			.andExpect(generationJsonPath(1, "_links.self.href")
				.value("https://api.spring.io/projects/spring-boot/generations/2.2.x"))
			.andExpect(generationJsonPath(1, "_links.project.href").value("https://api.spring.io/projects/spring-boot"))

			.andExpect(generationJsonPath(2, "name").value("2.1.x"))
			.andExpect(generationJsonPath(2, "initialReleaseDate").value("2020-01-31"))
			.andExpect(generationJsonPath(2, "support").value("default"))
			.andExpect(generationJsonPath(2, "ossSupportEndDate").value("2021-03-31"))
			.andExpect(generationJsonPath(2, "commercialSupportEndDate").value("2022-03-31"))
			.andExpect(generationJsonPath(2, "_links.self.href")
				.value("https://api.spring.io/projects/spring-boot/generations/2.1.x"))
			.andExpect(generationJsonPath(2, "_links.project.href").value("https://api.spring.io/projects/spring-boot"))

			.andExpect(jsonPath("$._links.project.href").value("https://api.spring.io/projects/spring-boot"))
			.andDo(document("list-generations", preprocessResponse(prettyPrint()), generationsLinks(),
					responseFields(
							fieldWithPath("_embedded.generations").description("An array of Project Generations"))
						.andWithPrefix("_embedded.generations[]", generationPayload())
						.and(subsectionWithPath("_links").description("Links to other resources"))));
	}

	private static JsonPathResultMatchers generationJsonPath(int index, String name) {
		return jsonPath("$._embedded.generations[%d].%s".formatted(index, name));
	}

	@Test
	void generationReturnsGeneration() throws Exception {
		given(this.projectRepository.getProjectGenerations("spring-boot")).willReturn(getProjectGenerations());
		given(this.projectRepository.getProjectSupportPolicy("spring-boot")).willReturn("SPRING_BOOT");
		this.mvc.perform(get("/projects/spring-boot/generations/2.2.x").accept(MediaTypes.HAL_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("2.2.x"))
			.andExpect(jsonPath("$._links.self.href")
				.value("https://api.spring.io/projects/spring-boot/generations/2.2.x"))
			.andExpect(jsonPath("$._links.project.href").value("https://api.spring.io/projects/spring-boot"))
			.andDo(document("show-generation", preprocessResponse(prettyPrint()), generationLinks(),
					responseFields(generationPayload())));
	}

	@Test
	void generationWhenNotFoundReturns404() throws Exception {
		given(this.projectRepository.getProjectGenerations("spring-boot"))
			.willThrow(NoSuchGithubProjectException.class);
		this.mvc.perform(get("/projects/spring-boot/generations").accept(MediaTypes.HAL_JSON))
			.andExpect(status().isNotFound());
	}

	private ProjectGeneration getProjectGenerations() {
		List<ProjectGeneration.Generation> generations = new ArrayList<>();
		generations
			.add(new ProjectGeneration.Generation("1.0.x", YearMonth.parse("2015-03"), SupportType.NONE, null, null));
		generations.add(new ProjectGeneration.Generation("2.2.x", YearMonth.parse("2020-02"), SupportType.EXTENDED,
				YearMonth.parse("2021-03"), YearMonth.parse("2021-03")));
		generations.add(new ProjectGeneration.Generation("2.1.x", YearMonth.parse("2020-01"), SupportType.DEFAULT,
				YearMonth.parse("2021-03"), YearMonth.parse("2022-03")));
		return new ProjectGeneration(generations);
	}

	FieldDescriptor[] generationPayload() {
		return new FieldDescriptor[] { fieldWithPath("name").type(JsonFieldType.STRING).description("Generation Name"),
				fieldWithPath("initialReleaseDate").type(JsonFieldType.STRING)
					.description("Date of the first release for this Generation"),
				fieldWithPath("support").type(JsonFieldType.STRING).description("Type of support"),
				fieldWithPath("ossSupportEndDate").type(JsonFieldType.STRING)
					.optional()
					.description("End date of the OSS support"),
				fieldWithPath("commercialSupportEndDate").type(JsonFieldType.STRING)
					.optional()
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
