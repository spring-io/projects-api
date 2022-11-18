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

package io.spring.projectapi.release;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.hateoas.server.core.Relation;

/**
 * Representation of a project release.
 *
 * @author Madhura Bhave
 */
@Relation(collectionRelation = "releases")
public class ReleaseMetadata {

	private String version;

	private ReleaseStatus status;

	private boolean current;

	private String referenceDocUrl;

	private String apiDocUrl;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public ReleaseStatus getStatus() {
		return status;
	}

	public void setStatus(ReleaseStatus status) {
		this.status = status;
	}

	public boolean isCurrent() {
		return this.current;
	}

	public void setCurrent(boolean current) {
		this.current = current;
	}

	@JsonProperty("referenceDocUrl")
	public String getReferenceDocUrl() {
		return this.referenceDocUrl;
	}

	@JsonProperty("ref")
	public void setReferenceDocUrl(String referenceDocUrl) {
		this.referenceDocUrl = referenceDocUrl;
	}

	@JsonProperty("apiDocUrl")
	public String getApiDocUrl() {
		return apiDocUrl;
	}

	@JsonProperty("api")
	public void setApiDocUrl(String apiDocUrl) {
		this.apiDocUrl = apiDocUrl;
	}

}
