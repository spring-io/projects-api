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
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link MarkdownUtils}.
 */
class MarkdownUtilsTests {

	@Test
	void getFrontMatterReturnsFrontMatter() throws IOException {
		Map<String, String> frontMatter = MarkdownUtils.getFrontMatter(getContent("project-index-content.md"));
		Map<String, String> expected = getExpectedFrontMatter();
		assertThat(frontMatter).containsExactlyEntriesOf(expected);
	}

	@Test
	void getFrontMatterWhenNotPresentReturnsNull() {
		assertThat(MarkdownUtils.getFrontMatter("no frontmatter")).isNull();
	}

	@Test
	void updateBody() throws IOException {
		String content = getContent("project-index-content.md");
		String expected = getContent("project-index-updated.md");
		String newBody = MarkdownUtils.getUpdatedContent(content, "new body");
		assertThat(newBody).isEqualTo(expected);
	}

	@NotNull
	private static Map<String, String> getExpectedFrontMatter() {
		Map<String, String> expected = new HashMap<>();
		expected.put("title", "Spring AMQP");
		expected.put("status", "ACTIVE");
		expected.put("description",
				"Applies core Spring concepts to the development of AMQP-based messaging solutions.");
		expected.put("image", "spring-amqp.svg?v=2");
		expected.put("featured", "true");
		expected.put("stackOverflow",
				"https://stackoverflow.com/questions/tagged/spring-amqp%20spring-rabbit%20spring-rabbitmq");
		expected.put("github", "http://github.com/spring-projects/spring-amqp");
		expected.put("site", "http://spring.io/projects/spring-amqp");
		expected.put("order", "255");
		expected.put("supportPolicy", "UPSTREAM");
		return expected;
	}

	private String getContent(String path) throws IOException {
		ClassPathResource resource = new ClassPathResource(path, getClass());
		try (InputStream inputStream = resource.getInputStream()) {
			return new String(FileCopyUtils.copyToByteArray(inputStream));
		}
	}

}
