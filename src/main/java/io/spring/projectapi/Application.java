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

package io.spring.projectapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.projectapi.ApplicationProperties.Contentful;
import io.spring.projectapi.contentful.ContentfulService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
@EnableConfigurationProperties(ApplicationProperties.class)
public class Application {

	private static final String BASE_URL = "https://graphql.contentful.com/content/v1/spaces/%s/environments/%s";

	@Bean
	public ContentfulService contentfulService(ObjectMapper objectMapper, WebClient.Builder webClientBuilder,
			ApplicationProperties properties) {
		Contentful contentful = properties.getContentful();
		String accessToken = contentful.getAccessToken();
		String spaceId = contentful.getSpaceId();
		String environmentId = contentful.getEnvironmentId();
		String baseUrl = String.format(BASE_URL, spaceId, environmentId);
		WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();
		return new ContentfulService(objectMapper, webClient, accessToken, spaceId, environmentId);
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
