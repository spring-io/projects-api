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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okio.Buffer;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.http.codec.CodecsAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Tests for {@link ContentfulQueries}.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
@SpringBootTest
class ContentfulQueriesTests {

	private static final String ACCESS_TOKEN = "000000";

	@Autowired
	private MockWebServer server;

	@Autowired
	private ContentfulQueries contentfulQueries;

	@Test
	void getProjectsReturnsProjects() throws IOException {
		setupResponse("query-projects.json");
		List<Project> projects = this.contentfulQueries.getProjects();
		assertThat(projects.size()).isEqualTo(3);
		assertThat(projects.get(0).getSlug()).isEqualTo("spring-xd");
	}

	@Test
	void getProjectsWhenNoProjectsReturnsEmpty() throws IOException {
		setupResponse("query-no-projects.json");
		List<Project> projects = this.contentfulQueries.getProjects();
		assertThat(projects).isEmpty();
	}

	@Test
	void getProjectsWhenErrorThrowsException() throws IOException {
		setupResponse("query-error.json");
		assertThatExceptionOfType(ContentfulException.class).isThrownBy(this.contentfulQueries::getProjects);
	}

	@Test
	void getProjectReturnsProject() throws IOException {
		setupResponse("query-project.json");
		Project project = this.contentfulQueries.getProject("spring-xd");
		assertThat(project.getSlug()).isEqualTo("spring-xd");
	}

	@Test
	void getProjectWhenNoNoProjectMatchThrowsException() throws IOException {
		setupResponse("query-no-project.json");
		assertThatExceptionOfType(NoSuchContentfulProjectException.class)
				.isThrownBy(() -> this.contentfulQueries.getProject("spring-xd"))
				.satisfies((ex) -> assertThat(ex.getProjectSlug()).isEqualTo("spring-xd"));
	}

	@Test
	void getProjectWhenErrorThrowsException() throws IOException {
		setupResponse("query-error.json");
		assertThatExceptionOfType(ContentfulException.class)
				.isThrownBy(() -> this.contentfulQueries.getProject("spring-xd"));
	}

	@Test
	void getProjectDocumentationsReturnsDocumentations() throws IOException {
		setupResponse("query-project-documentations.json");
		List<ProjectDocumentation> documenations = this.contentfulQueries.getProjectDocumentations("spring-xd");
		assertThat(documenations).hasSize(6);
		assertThat(documenations.get(0).getVersion()).isEqualTo("3.0.0-SNAPSHOT");
	}

	@Test
	void getProjectDocumentationsWhenNoProjectMatchThrowsException() throws IOException {
		setupResponse("query-no-project.json");
		assertThatExceptionOfType(NoSuchContentfulProjectException.class)
				.isThrownBy(() -> this.contentfulQueries.getProjectDocumentations("spring-xd"))
				.satisfies((ex) -> assertThat(ex.getProjectSlug()).isEqualTo("spring-xd"));
	}

	@Test
	void getProjectSupportsReturnsSupports() throws IOException {
		setupResponse("query-project-supports.json");
		List<ProjectSupport> supports = this.contentfulQueries.getProjectSupports("spring-xd");
		assertThat(supports).hasSize(10);
		assertThat(supports.get(0).getBranch()).isEqualTo("1.5.x");
	}

	@Test
	void getProjectSupportsWhenNoProjectMatchThrowsException() throws IOException {
		setupResponse("query-no-project.json");
		assertThatExceptionOfType(NoSuchContentfulProjectException.class)
				.isThrownBy(() -> this.contentfulQueries.getProjectSupports("spring-xd"))
				.satisfies((ex) -> assertThat(ex.getProjectSlug()).isEqualTo("spring-xd"));
	}

	private void setupResponse(String name) throws IOException {
		setupResponse(new ClassPathResource(name, getClass()));
	}

	private void setupResponse(Resource resource) throws IOException {
		try (InputStream inputStream = resource.getInputStream()) {
			try (Buffer buffer = new Buffer()) {
				buffer.readFrom(inputStream);
				MockResponse response = new MockResponse();
				response.setBody(buffer);
				response.setHeader("Content-Type", "application/json");
				this.server.enqueue(response);
			}
		}
	}

	@Configuration(proxyBeanMethods = false)
	@ImportAutoConfiguration({ JacksonAutoConfiguration.class, WebClientAutoConfiguration.class,
			CodecsAutoConfiguration.class })
	static class Config {

		@Bean
		MockWebServer mockWebServer() {
			return new MockWebServer();
		}

		@Bean
		ContentfulQueries contentfulQueries(MockWebServer mockWebServer, WebClient.Builder webClientBuilder) {
			HttpUrl baseUrl = mockWebServer.url("/contentful.com");
			WebClient webClient = webClientBuilder.baseUrl(baseUrl.toString()).build();
			return new ContentfulQueries(webClient, ACCESS_TOKEN);
		}

	}

}
