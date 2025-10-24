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

package io.spring.projectapi.github;

import java.io.IOException;

import io.spring.projectapi.github.Project.Status;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JSON tests for {@link Project}.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
@JsonTest
@AutoConfigureWebClient
class ProjectJsonTests {

	@Autowired
	private JacksonTester<Project[]> json;

	@Test
	void readObjectReadsJson() throws IOException {
		Project[] projects = this.json.readObject("project-all.json");
		assertThat(projects).hasSize(96);
	}

	@Test
	void readObjectWhenMinimalProjectReadsJson() throws IOException {
		Project project = this.json.readObject("project-minimal.json")[0];
		assertThat(project.getTitle()).isEqualTo("test");
		assertThat(project.getSlug()).isEqualTo("test");
		assertThat(project.getGithub()).isNull();
		assertThat(project.getStatus()).isNull();
	}

	@Test
	void readObjectWhenCompleteProjectReadsJson() throws IOException {
		Project project = this.json.readObject("project-complete.json")[0];
		assertThat(project.getTitle()).isEqualTo("title");
		assertThat(project.getSlug()).isEqualTo("test");
		assertThat(project.getGithub()).isEqualTo("gh");
		assertThat(project.getStatus()).isEqualTo(Status.ACTIVE);
	}

}
