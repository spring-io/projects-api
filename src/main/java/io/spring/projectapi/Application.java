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
import io.spring.projectapi.ApplicationProperties.Github;
import io.spring.projectapi.github.GithubOperations;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(ApplicationProperties.class)
public class Application {

	@Bean
	public GithubOperations githubOperations(RestTemplateBuilder builder, ObjectMapper objectMapper,
			ApplicationProperties properties) {
		Github github = properties.getGithub();
		String accessToken = github.getAccesstoken();
		String branch = github.getBranch();
		return new GithubOperations(builder, objectMapper, accessToken, branch);
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
