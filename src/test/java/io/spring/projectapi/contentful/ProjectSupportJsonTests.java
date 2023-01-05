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

package io.spring.projectapi.contentful;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JSON tests for {@link ProjectSupport}.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
@JsonTest
@AutoConfigureWebClient
class ProjectSupportJsonTests {

	@Autowired
	private JacksonTester<ProjectSupport> json;

	@Test
	void readObjectReadsJson() throws Exception {
		ProjectSupport projectSupport = this.json.readObject("project-support.json");
		assertThat(projectSupport.getBranch()).isEqualTo("1.5.x");
		assertThat(projectSupport.getInitialDate()).isEqualTo("2017-01-30");
		assertThat(projectSupport.getOssEnforcedEnd()).isEqualTo("2019-08-06");
		assertThat(projectSupport.getOssPolicyEnd()).isEqualTo("2019-08-07");
		assertThat(projectSupport.getCommercialEnforcedEnd()).isEqualTo("2019-08-08");
		assertThat(projectSupport.getCommercialPolicyEnd()).isEqualTo("2020-11-06");
	}

}
