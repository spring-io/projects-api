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

package io.spring.projectapi.github;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.projectapi.ProjectRepository;
import io.spring.projectapi.github.ProjectDocumentation.Status;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JSON tests for {@link ProjectDocumentation}.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
@JsonTest
@AutoConfigureWebClient
class ProjectDocumentationJsonTests {

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private ProjectRepository projectRepository;

	@Autowired
	private JacksonTester<ProjectDocumentation> json;

	@Test
	void convertValueToMapReturnsMap() {
		ProjectDocumentation documentation = new ProjectDocumentation("ver", false, "api", "ref", Status.PRERELEASE,
				true);
		Map<?, ?> converted = this.objectMapper.convertValue(documentation, Map.class);
		Map<String, Object> expected = new HashMap<>();
		expected.put("version", documentation.getVersion());
		expected.put("api", documentation.getApi());
		expected.put("ref", documentation.getRef());
		expected.put("status", documentation.getStatus().name());
		expected.put("current", documentation.isCurrent());
		expected.put("antora", documentation.isAntora());
		assertThat(converted).isEqualTo(expected);
	}

	@Test
	void readObjectReadsJson() throws Exception {
		ProjectDocumentation projectDocumentation = this.json.readObject("project-documentation.json");
		assertThat(projectDocumentation.getVersion()).isEqualTo("3.0.0-SNAPSHOT");
		assertThat(projectDocumentation.getApi()).isEqualTo("https://docs.spring.io/spring-boot/docs/{version}/api/");
		assertThat(projectDocumentation.getRef())
			.isEqualTo("https://docs.spring.io/spring-boot/docs/{version}/reference/html/");
		assertThat(projectDocumentation.getStatus()).isEqualTo(Status.SNAPSHOT);
		assertThat(projectDocumentation.isCurrent()).isEqualTo(true);
		assertThat(projectDocumentation.isAntora()).isEqualTo(false);
	}

}
