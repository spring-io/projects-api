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

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.backoff.ExponentialBackOff;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests.
 */
@SpringBootTest
class ApplicationTests {

	@Autowired
	private RetryTemplate retryTemplate;

	@MockitoBean
	private ProjectRepository projectRepository;

	@Test
	void retryTemplate() {
		RetryPolicy retryPolicy = this.retryTemplate.getRetryPolicy();
		ExponentialBackOff backOff = (ExponentialBackOff) retryPolicy.getBackOff();
		assertThat(backOff.getInitialInterval()).isEqualTo(100L);
		assertThat(backOff.getMultiplier()).isEqualTo(2.0);
		assertThat(backOff.getMaxInterval()).isEqualTo(10000L);
	}

}
