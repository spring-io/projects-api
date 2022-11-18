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
package io.spring.projectapi.generation;

import java.time.LocalDate;
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
 * Tests for {@link GenerationMetadataController}.
 *
 * @author Madhura Bhave
 */
@WebApiTest
class GenerationMetadataControllerTests {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private MockMvc mvc;

	@MockBean
	private ContentfulService contentfulService;

	@Test
	public void listGenerations() throws Exception {
		List<GenerationMetadata> generations = getGenerations();
		given(this.contentfulService.getGenerations(eq("spring-boot"))).willReturn(generations);
		this.mvc.perform(get("/projects/spring-boot/generations").accept(MediaTypes.HAL_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$._embedded.generations.length()").value("2"))
				.andExpect(jsonPath("$._embedded.generations[0].name").value("2.2.x"))
				.andExpect(jsonPath("$._embedded.generations[0].initialReleaseDate").value("2020-01-01"))
				.andExpect(jsonPath("$._embedded.generations[0].ossSupportEndDate").value("2020-01-01"))
				.andExpect(jsonPath("$._embedded.generations[0].commercialSupportEndDate").value("2020-01-01"))
				.andExpect(jsonPath("$._embedded.generations[0]._links.self.href")
						.value("http://localhost/projects/spring-boot/generations/2.2.x"))
				.andExpect(jsonPath("$._embedded.generations[0]._links.project.href")
						.value("http://localhost/projects/spring-boot"))
				.andExpect(jsonPath("$._embedded.generations[1].name").value("2.1.x"))
				.andExpect(jsonPath("$._embedded.generations[1].initialReleaseDate").value("2019-01-01"))
				.andExpect(jsonPath("$._embedded.generations[1].ossSupportEndDate").value("2019-01-01"))
				.andExpect(jsonPath("$._embedded.generations[1].commercialSupportEndDate").value("2019-01-01"))
				.andExpect(jsonPath("$._embedded.generations[1]._links.self.href")
						.value("http://localhost/projects/spring-boot/generations/2.1.x"))
				.andExpect(jsonPath("$._embedded.generations[1]._links.project.href")
						.value("http://localhost/projects/spring-boot"))
				.andExpect(jsonPath("$._links.project.href").value("http://localhost/projects/spring-boot"));
	}

	private List<GenerationMetadata> getGenerations() {
		List<GenerationMetadata> generations = new ArrayList<>();
		GenerationMetadata generation1 = new GenerationMetadata();
		generation1.setName("2.2.x");
		generation1.setInitialReleaseDate(LocalDate.parse("2020-01-01"));
		generation1.setOssSupportEndDate(LocalDate.parse("2020-01-01"));
		generation1.setCommercialSupportEndDate(LocalDate.parse("2020-01-01"));
		GenerationMetadata generation2 = new GenerationMetadata();
		generation2.setName("2.1.x");
		generation2.setInitialReleaseDate(LocalDate.parse("2019-01-01"));
		generation2.setOssSupportEndDate(LocalDate.parse("2019-01-01"));
		generation2.setCommercialSupportEndDate(LocalDate.parse("2019-01-01"));
		generations.add(generation1);
		generations.add(generation2);
		return generations;
	}

	@Test
	public void whenGenerationsNotFoundShouldReturn404() throws Exception {
		given(this.contentfulService.getGenerations(eq("spring-boot"))).willReturn(null);
		this.mvc.perform(get("/projects/spring-boot/generations").accept(MediaTypes.HAL_JSON))
				.andExpect(status().isNotFound());
	}

	@Test
	public void showRelease() throws Exception {
		List<GenerationMetadata> generations = getGenerations();
		given(this.contentfulService.getGenerations(eq("spring-boot"))).willReturn(generations);
		this.mvc.perform(get("/projects/spring-boot/generations/2.2.x").accept(MediaTypes.HAL_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$.name").value("2.2.x"))
				.andExpect(
						jsonPath("$._links.self.href").value("http://localhost/projects/spring-boot/generations/2.2.x"))
				.andExpect(jsonPath("$._links.project.href").value("http://localhost/projects/spring-boot"));
	}

}