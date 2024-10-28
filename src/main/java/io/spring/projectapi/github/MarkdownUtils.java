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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterBlock;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.jetbrains.annotations.Nullable;

/**
 * Markdown parser to get frontmatter and body.
 *
 * @author Madhura Bhave
 */
final class MarkdownUtils {

	private static final Parser PARSER;

	private static String FRONTMATTER_DELIMITER = "---\n";

	private static String LINEBREAK = "\n";

	static {
		MutableDataSet options = new MutableDataSet().set(Parser.EXTENSIONS,
				List.of(YamlFrontMatterExtension.create()));
		PARSER = Parser.builder(options).build();
	}

	private MarkdownUtils() {
	}

	/**
	 * Returns the frontmatter as a map and {@code null} if frontmatter not present.
	 * @param contents file contents
	 * @return the frontmatter as a map
	 */
	static Map<String, String> getFrontMatter(String contents) {
		Node frontMatter = getFrontMatterNode(contents);
		if (frontMatter == null) {
			return null;
		}
		String[] split = frontMatter.getChars().toString().split("\n");
		Map<String, String> resultMap = new HashMap<>();
		for (String pair : split) {
			String[] keyValue = pair.split(": ", 2);
			if (keyValue.length == 2) {
				resultMap.put(keyValue[0], keyValue[1]);
			}
		}
		return resultMap;
	}

	@Nullable
	private static Node getFrontMatterNode(String contents) {
		Node document = PARSER.parse(contents);
		Node frontMatter = document.getFirstChild();
		if (!(frontMatter instanceof YamlFrontMatterBlock)) {
			return null;
		}
		return frontMatter;
	}

	/**
	 * Returns the updated file contents with the new body while preserving the
	 * frontmatter. Returns null if the frontmatter is not present.
	 * @param contents the original file contents
	 * @param updatedBody the body to update
	 * @return the updated file
	 */
	static String getUpdatedContent(String contents, String updatedBody) {
		Node frontMatter = getFrontMatterNode(contents);
		if (frontMatter == null) {
			return null;
		}
		String frontMatterString = FRONTMATTER_DELIMITER + frontMatter.getChildChars().toString() + LINEBREAK
				+ FRONTMATTER_DELIMITER;
		return frontMatterString + "\n" + updatedBody;
	}

}
