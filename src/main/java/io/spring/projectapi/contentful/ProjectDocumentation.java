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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;

/**
 * Java representation of the {@code project documentation} type as defined in
 * <a href= "https://github.com/spring-io/spring-website/">spring-io/spring-website</a>.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
public class ProjectDocumentation {

	private final String version;

	private final String api;

	private final String ref;

	private final Status status;

	private final String repository;

	private final boolean current;

	@JsonCreator(mode = Mode.PROPERTIES)
	public ProjectDocumentation(String version, String api, String ref, Status status, String repository,
			boolean current) {
		this.version = version;
		this.api = api;
		this.ref = ref;
		this.status = status;
		this.repository = repository;
		this.current = current;
	}

	public String getVersion() {
		return this.version;
	}

	public String getApi() {
		return this.api;
	}

	public String getRef() {
		return this.ref;
	}

	public Status getStatus() {
		return this.status;
	}

	public String getRepository() {
		return this.repository;
	}

	public boolean isCurrent() {
		return this.current;
	}

	/**
	 * Project documentation status.
	 */
	public enum Status {

		/**
		 * Snapshot.
		 */
		SNAPSHOT,

		/**
		 * Pre-release.
		 */
		PRERELEASE,

		/**
		 * General Availability.
		 */
		GENERAL_AVAILABILITY;

	}

}
