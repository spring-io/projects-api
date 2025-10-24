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

package io.spring.projectapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.projectapi.ApplicationProperties.Github;
import io.spring.projectapi.github.GithubOperations;
import io.spring.projectapi.github.GithubQueries;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpClientErrorException;

@SpringBootApplication
@EnableConfigurationProperties(ApplicationProperties.class)
public class Application {

	@Bean
	public GithubOperations githubOperations(RestTemplateBuilder builder, ObjectMapper objectMapper,
			ApplicationProperties properties, RetryTemplate retryTemplate) {
		Github github = properties.getGithub();
		String accessToken = github.getAccesstoken();
		String branch = github.getBranch();
		return new GithubOperations(builder, objectMapper, accessToken, branch, retryTemplate);
	}

	@Bean
	public GithubQueries githubQueries(RestTemplateBuilder builder, ObjectMapper objectMapper,
			ApplicationProperties properties) {
		Github github = properties.getGithub();
		String accessToken = github.getAccesstoken();
		String branch = github.getBranch();
		return new GithubQueries(builder, objectMapper, accessToken, branch);
	}

	@Bean
	public RetryTemplate retryTemplate() {
		return RetryTemplate.builder().maxAttempts(10).exponentialBackoff(100, 2, 10000).retryOn((throwable) -> {
			if (throwable instanceof HttpClientErrorException ex) {
				return (ex.getStatusCode().value() == 409);
			}
			return false;
		}).build();
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
