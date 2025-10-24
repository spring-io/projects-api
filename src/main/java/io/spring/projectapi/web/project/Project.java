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

package io.spring.projectapi.web.project;

import com.fasterxml.jackson.annotation.JsonCreator;

import org.springframework.hateoas.server.core.Relation;

/**
 * Representation of a project.
 *
 * @author Madhura Bhave
 */
@Relation(collectionRelation = "projects")
public class Project {

	private String name;

	private String slug;

	private String repositoryUrl;

	private Status status;

	@JsonCreator
	public Project(String name, String slug, String repositoryUrl, Status status) {
		this.name = name;
		this.slug = slug;
		this.repositoryUrl = repositoryUrl;
		this.status = status;
	}

	public String getName() {
		return this.name;
	}

	public String getSlug() {
		return this.slug;
	}

	public String getRepositoryUrl() {
		return this.repositoryUrl;
	}

	public Status getStatus() {
		return this.status;
	}

	/**
	 * Project status.
	 */
	public enum Status {

		/**
		 * Incubating.
		 */
		INCUBATING,

		/**
		 * Active.
		 */
		ACTIVE,

		/**
		 * Active Unsupported.
		 */
		ACTIVE_UNSUPPORTED,

		/**
		 * Community Maintained.
		 */
		COMMUNITY,

		/**
		 * End of Life.
		 */
		END_OF_LIFE

	}

}
