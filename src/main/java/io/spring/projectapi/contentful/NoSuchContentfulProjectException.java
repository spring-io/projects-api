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

import java.util.Collection;

import org.springframework.graphql.client.ClientResponseField;

/**
 * {@link ContentfulException} thrown when a project with the specified slug does not
 * exist.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
public final class NoSuchContentfulProjectException extends ContentfulException {

	private final String projectSlug;

	private NoSuchContentfulProjectException(String projectSlug) {
		super("No contentful project found for slug '%s'".formatted(projectSlug));
		this.projectSlug = projectSlug;
	}

	public String getProjectSlug() {
		return this.projectSlug;
	}

	static void throwIfEmpty(Collection<?> items, String projectSlug) {
		if (items.isEmpty()) {
			throw new NoSuchContentfulProjectException(projectSlug);
		}
	}

	static void throwIfHasNoValue(ClientResponseField field, String projectSlug) {
		if (!field.hasValue()) {
			throw new NoSuchContentfulProjectException(projectSlug);
		}
	}

}
