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

package io.spring.projectapi.web.index;

import io.spring.projectapi.test.WebApiTests;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.restdocs.hypermedia.LinkDescriptor;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for {@link IndexController}
 */
@WebApiTests(IndexController.class)
public class IndexControllerTests {

	@Autowired
	private MockMvc mvc;

	@Test
	public void indexReturnProjectsAndRepositories() throws Exception {
		this.mvc.perform(get("/").accept(MediaTypes.HAL_JSON))
			.andExpect(status().isOk())
			.andDo(document("show-index", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
					links(indexLinks())));
	}

	LinkDescriptor[] indexLinks() {
		return new LinkDescriptor[] {
				linkWithRel("projects").optional().description("Link to <<project, Project resources>>"),
				linkWithRel("repositories").optional().description("Link to <<repository, Repository resources>>") };
	}

}
