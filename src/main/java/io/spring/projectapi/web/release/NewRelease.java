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

package io.spring.projectapi.web.release;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

/**
 * Represents the input provided when creating a release.
 *
 * @author Madhura Bhave
 */
public class NewRelease {

	@NotBlank
	private final String version;

	@URL
	private final String referenceDocUrl;

	@URL
	private final String apiDocUrl;

	private final boolean isAntora;

	@JsonCreator
	public NewRelease(String version, String referenceDocUrl, String apiDocUrl, boolean isAntora) {
		this.version = version;
		this.referenceDocUrl = referenceDocUrl;
		this.apiDocUrl = apiDocUrl;
		this.isAntora = isAntora;
	}

	public String getVersion() {
		return this.version;
	}

	public String getReferenceDocUrl() {
		return this.referenceDocUrl;
	}

	public String getApiDocUrl() {
		return this.apiDocUrl;
	}

	public boolean isAntora() {
		return this.isAntora;
	}

}
