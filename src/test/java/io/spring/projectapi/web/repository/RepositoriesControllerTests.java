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
import org.springframework.test.web.servlet.MockMvc;

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
				.andExpect(jsonPath("$._embedded.repositories.length()").value("3"));
	}

	@Test
	void repositoryReturnsRepository() throws Exception {
		this.mvc.perform(get("/repositories/spring-releases").accept(MediaTypes.HAL_JSON)).andExpect(status().isOk());
	}

	@Test
	void repositoryWhenNotFoundReturns404() throws Exception {
		this.mvc.perform(get("/repositories/does-not-exist").accept(MediaTypes.HAL_JSON))
				.andExpect(status().isNotFound());
	}

}
