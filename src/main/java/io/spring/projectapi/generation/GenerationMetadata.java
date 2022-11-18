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

package io.spring.projectapi.generation;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.hateoas.server.core.Relation;

/**
 * Representation of a project generation.
 *
 * @author Madhura Bhave
 */
@Relation(collectionRelation = "generations")
public class GenerationMetadata {

	private String name;

	private LocalDate initialReleaseDate;

	private LocalDate ossSupportEndDate;

	private LocalDate commercialSupportEndDate;

	@JsonProperty("name")
	public String getName() {
		return name;
	}

	@JsonProperty("branch")
	public void setName(String name) {
		this.name = name;
	}

	@JsonProperty("initialReleaseDate")
	@JsonFormat(pattern = "yyyy-MM-dd")
	public LocalDate getInitialReleaseDate() {
		return initialReleaseDate;
	}

	@JsonProperty("initialDate")
	public void setInitialReleaseDate(LocalDate initialReleaseDate) {
		this.initialReleaseDate = initialReleaseDate;
	}

	@JsonProperty("ossSupportEndDate")
	@JsonFormat(pattern = "yyyy-MM-dd")
	public LocalDate getOssSupportEndDate() {
		return ossSupportEndDate;
	}

	@JsonProperty("ossPolicyEnd")
	public void setOssSupportEndDate(LocalDate ossSupportEndDate) {
		this.ossSupportEndDate = ossSupportEndDate;
	}

	@JsonProperty("commercialSupportEndDate")
	@JsonFormat(pattern = "yyyy-MM-dd")
	public LocalDate getCommercialSupportEndDate() {
		return commercialSupportEndDate;
	}

	@JsonProperty("commercialPolicyEnd")
	public void setCommercialSupportEndDate(LocalDate commercialSupportEndDate) {
		this.commercialSupportEndDate = commercialSupportEndDate;
	}

}
