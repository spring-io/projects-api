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
package io.spring.projectapi;

import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;

import io.spring.projectapi.ApplicationProperties.Contentful;
import io.spring.projectapi.generation.GenerationMetadata;
import io.spring.projectapi.project.ProjectMetadata;
import io.spring.projectapi.release.ReleaseMetadata;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ContentfulService}.
 *
 * @author Madhura Bhave
 */
class ContentfulServiceTests {

	private MockWebServer server;

	private WebClient.Builder builder;

	private ContentfulService service;

	private static final String CONTENTFUL_URL = "/contentful.com";

	@BeforeEach
	void setup() {
		this.server = new MockWebServer();
		this.builder = WebClient.builder();
		Contentful contentfulProperties = new Contentful("test-token", "management-token", "test-space",
				"test-environment");
		ApplicationProperties properties = new ApplicationProperties(contentfulProperties, null);
		this.service = new ContentfulService(this.builder, this.server.url(CONTENTFUL_URL).toString(), properties);
	}

	@AfterEach
	void shutdown() throws Exception {
		this.server.shutdown();
	}

	@Test
	void getProjects() throws Exception {
		Resource resource = new ClassPathResource("project/successful-response.json");
		setupMockResponse(resource);
		List<ProjectMetadata> projects = this.service.getProjects();
		expectRequest((request) -> assertThat(request.getPath()).isEqualTo("/contentful.com"));
		assertThat(projects.size()).isEqualTo(3);
	}

	@Test
	void getProject() throws Exception {
		Resource resource = new ClassPathResource("project/successful-response-single-project.json");
		setupMockResponse(resource);
		ProjectMetadata project = this.service.getProject("spring-boot");
		expectRequest((request) -> assertThat(request.getPath()).isEqualTo("/contentful.com"));
		assertThat(project.getName()).isEqualTo("Spring Boot");
	}

	@Test
	void getProjectWhenProjectDoesNotExist() throws Exception {
		Resource resource = new ClassPathResource("not-found.json");
		setupMockResponse(resource);
		ProjectMetadata project = this.service.getProject("non-existent");
		expectRequest((request) -> assertThat(request.getPath()).isEqualTo("/contentful.com"));
		assertThat(project).isNull();
	}

	@Test
	void getReleases() throws Exception {
		Resource resource = new ClassPathResource("release/successful-response.json");
		setupMockResponse(resource);
		List<ReleaseMetadata> releases = this.service.getReleases("spring-boot");
		expectRequest((request) -> {
			assertThat(request.getPath()).isEqualTo("/contentful.com");
			// FIXME line breaks cause assertion to fail
			// String body = request.getBody().readString(Charset.defaultCharset());
			// Resource document = new
			// ClassPathResource("graphql-documents/releases.graphql");
			// try {
			// String contents = FileCopyUtils
			// .copyToString(new InputStreamReader(document.getInputStream(),
			// Charset.defaultCharset()));
			// assertThat(body).contains(contents);
			// }
			// catch (IOException e) {
			// fail("Failed to load file");
			// }
		});
		assertThat(releases.size()).isEqualTo(6);
	}

	@Test
	void getReleasesWhenProjectDoesNotExist() throws Exception {
		Resource resource = new ClassPathResource("not-found.json");
		setupMockResponse(resource);
		List<ReleaseMetadata> releases = this.service.getReleases("spring-boot");
		expectRequest((request) -> assertThat(request.getPath()).isEqualTo("/contentful.com"));
		assertThat(releases).isEmpty();
	}

	@Test
	void getGenerations() throws Exception {
		Resource resource = new ClassPathResource("generation/successful-response.json");
		setupMockResponse(resource);
		List<GenerationMetadata> generations = this.service.getGenerations("spring-boot");
		expectRequest((request) -> {
			assertThat(request.getPath()).isEqualTo("/contentful.com");
			// FIXME line breaks cause assertion to fail
			// String body = request.getBody().readString(Charset.defaultCharset());
			// Resource document = new
			// ClassPathResource("graphql-documents/releases.graphql");
			// try {
			// String contents = FileCopyUtils
			// .copyToString(new InputStreamReader(document.getInputStream(),
			// Charset.defaultCharset()));
			// assertThat(body).contains(contents);
			// }
			// catch (IOException e) {
			// fail("Failed to load file");
			// }
		});
		assertThat(generations.size()).isEqualTo(10);
	}

	@Test
	void getGenerationsWhenProjectDoesNotExist() throws Exception {
		Resource resource = new ClassPathResource("not-found.json");
		setupMockResponse(resource);
		List<GenerationMetadata> generations = this.service.getGenerations("spring-boot");
		expectRequest((request) -> assertThat(request.getPath()).isEqualTo("/contentful.com"));
		assertThat(generations).isEmpty();
	}

	@Test
	void addRelease() throws Exception {
		Resource resource = new ClassPathResource("release/successful-response.json");
		setupMockResponse(resource);
		ReleaseMetadata releaseMetadata = new ReleaseMetadata();
		this.service.addRelease("spring-boot", releaseMetadata);
		expectRequest((request) -> assertThat(request.getPath()).isEqualTo("/contentful.com"));
	}

	@Test
	void deleteRelease() {

	}

	private void setupMockResponse(Resource resourceBody) throws Exception {
		try (InputStream metadataSource = resourceBody.getInputStream()) {
			try (Buffer metadataBuffer = new Buffer()) {
				metadataBuffer.readFrom(metadataSource);
				MockResponse metadataResponse = new MockResponse().setBody(metadataBuffer).setHeader("Content-Type",
						"application/json");
				this.server.enqueue(metadataResponse);
			}
		}
	}

	private void expectRequest(Consumer<RecordedRequest> consumer) throws InterruptedException {
		consumer.accept(this.server.takeRequest());
	}

}