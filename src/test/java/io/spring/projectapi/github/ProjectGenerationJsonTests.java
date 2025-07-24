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

import java.time.YearMonth;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JSON tests for {@link ProjectGeneration}.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
@JsonTest
@AutoConfigureWebClient
class ProjectGenerationJsonTests {

	@Autowired
	private JacksonTester<ProjectGeneration> json;

	@Test
	void readObjectReadsJson() throws Exception {
		ProjectGeneration projectGeneration = this.json.readObject("project-generations.json");
		assertThat(projectGeneration.getGenerations().get(0).getGeneration()).isEqualTo("1.5.x");
		assertThat(projectGeneration.getGenerations().get(0).getInitialRelease()).isEqualTo(YearMonth.parse("2017-01"));
		assertThat(projectGeneration.getGenerations().get(0).getOssSupportEnd()).isEqualTo(YearMonth.parse("2019-08"));
		assertThat(projectGeneration.getGenerations().get(0).getEnterpriseSupportEnd())
			.isEqualTo(YearMonth.parse("2020-11"));
		assertThat(projectGeneration.getGenerations().get(0).isLastMinor()).isTrue();
	}

}
