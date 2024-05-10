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

import java.util.List;
import java.util.Map;

/**
 * {@link ContentfulException} thrown when a project documentation with the specified
 * version does not exist.
 *
 * @author Madhura Bhave
 */
public final class NoSuchContentfulProjectDocumentationFoundException extends ContentfulException {

	private final String projectSlug;

	private final String version;

	NoSuchContentfulProjectDocumentationFoundException(String projectSlug, String version) {
		super("No contentful documentation found for slug '%s' and version '%s'".formatted(projectSlug, version));
		this.projectSlug = projectSlug;
		this.version = version;
	}

	static void throwIfHasNotPresent(List<Map<String, Object>> documentations, String projectSlug, String version) {
		documentations.stream()
			.filter((m) -> m.get("version").equals(version))
			.findFirst()
			.orElseThrow((() -> new NoSuchContentfulProjectDocumentationFoundException(projectSlug, version)));
	}

	public String getProjectSlug() {
		return this.projectSlug;
	}

	public String getVersion() {
		return this.version;
	}

}
