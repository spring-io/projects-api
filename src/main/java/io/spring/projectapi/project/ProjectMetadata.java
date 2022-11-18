/*
 * Copyright 2012-2022 the original author or authors.
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
 *
 */

package io.spring.projectapi.project;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.hateoas.server.core.Relation;

/**
 * Representation of a project.
 *
 * @author Madhura Bhave
 */
@Relation(collectionRelation = "projects")
public class ProjectMetadata {

	private String name;

	private String slug;

	private String repositoryUrl;

	private SupportStatus status;

	@JsonProperty("name")
	public String getName() {
		return name;
	}

	@JsonProperty("title")
	public void setName(String name) {
		this.name = name;
	}

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	@JsonProperty("repositoryUrl")
	public String getRepositoryUrl() {
		return repositoryUrl;
	}

	@JsonProperty("github")
	public void setRepositoryUrl(String repositoryUrl) {
		this.repositoryUrl = repositoryUrl;
	}

	public SupportStatus getStatus() {
		return status;
	}

	public void setStatus(SupportStatus status) {
		this.status = status;
	}

	/**
	 * Support status for Spring projects.
	 *
	 */
	public enum SupportStatus {

		/**
		 * Project is incubating and is not supported for production use
		 */
		INCUBATING("Incubating"),
		/**
		 * Project is actively supported by the Spring team
		 */
		ACTIVE("Active"),
		/**
		 * Project is actively supported by the Spring community
		 */
		COMMUNITY("Community"),
		/**
		 * Project is not supported anymore
		 */
		END_OF_LIFE("End Of Life");

		private final String label;

		SupportStatus(String label) {
			this.label = label;
		}

		public String getLabel() {
			return label;
		}

	}

}
