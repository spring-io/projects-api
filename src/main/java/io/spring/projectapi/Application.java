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

import java.time.Duration;

import io.spring.projectapi.ApplicationProperties.Enterprise;
import io.spring.projectapi.ApplicationProperties.Github;
import io.spring.projectapi.github.GithubOperations;
import io.spring.projectapi.github.GithubQueries;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.web.client.HttpClientErrorException;

@SpringBootApplication
@EnableConfigurationProperties(ApplicationProperties.class)
public class Application {

	@Bean
	public GithubOperations githubOperations(RestTemplateBuilder builder, JsonMapper jsonMapper,
			ApplicationProperties properties, RetryTemplate retryTemplate) {
		Github github = properties.getGithub();
		String accessToken = github.getAccesstoken();
		String branch = github.getBranch();
		return new GithubOperations(builder, jsonMapper, accessToken, branch, retryTemplate);
	}

	@Bean
	public GithubQueries githubQueries(RestTemplateBuilder builder, JsonMapper jsonMapper,
			ApplicationProperties properties) {
		Github github = properties.getGithub();
		String accessToken = github.getAccesstoken();
		String branch = github.getBranch();
		Enterprise enterprise = properties.getGithub().getEnterprise();
		String enterpriseToken = enterprise.getAccesstoken();
		String enterpriseBranch = enterprise.getBranch();
		return new GithubQueries(builder, jsonMapper, accessToken, branch, enterpriseToken, enterpriseBranch);
	}

	@Bean
	public RetryTemplate retryTemplate() {
		RetryPolicy retryPolicy = RetryPolicy.builder()
			.maxRetries(9)
			.delay(Duration.ofMillis(100))
			.multiplier(2.0)
			.maxDelay(Duration.ofMillis(10000))
			.predicate((throwable) -> {
				if (throwable instanceof HttpClientErrorException ex) {
					return (ex.getStatusCode().value() == 409);
				}
				return false;
			})
			.build();
		return new RetryTemplate(retryPolicy);
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
