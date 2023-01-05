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

package io.spring.projectapi.web.generation;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;

import org.springframework.hateoas.server.core.Relation;

/**
 * Representation of a project generation.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
@Relation(collectionRelation = "generations")
public class Generation {

	private final String name;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private final LocalDate initialReleaseDate;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private final LocalDate ossSupportEndDate;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private final LocalDate commercialSupportEndDate;

	@JsonCreator
	public Generation(String name, LocalDate initialReleaseDate, LocalDate ossSupportEndDate,
			LocalDate commercialSupportEndDate) {
		this.name = name;
		this.initialReleaseDate = initialReleaseDate;
		this.ossSupportEndDate = ossSupportEndDate;
		this.commercialSupportEndDate = commercialSupportEndDate;
	}

	public String getName() {
		return this.name;
	}

	public LocalDate getInitialReleaseDate() {
		return this.initialReleaseDate;
	}

	public LocalDate getOssSupportEndDate() {
		return this.ossSupportEndDate;
	}

	public LocalDate getCommercialSupportEndDate() {
		return this.commercialSupportEndDate;
	}

}
