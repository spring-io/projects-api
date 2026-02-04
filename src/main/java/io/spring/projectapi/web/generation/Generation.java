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

package io.spring.projectapi.web.generation;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import org.springframework.hateoas.server.core.Relation;

/**
 * Representation of a project generation.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
@Relation(collectionRelation = "generations")
@JsonInclude(Include.NON_NULL)
public class Generation {

	private final String name;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private final LocalDate initialReleaseDate;

	private String support;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private final LocalDate ossSupportEndDate;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private final LocalDate commercialSupportEndDate;

	private final Map<String, List<String>> linkedGenerations;

	@JsonInclude(Include.NON_NULL)
	private final LatestPatch latestPatch;

	@JsonCreator
	public Generation(String name, YearMonth initialReleaseDate, String support, YearMonth ossSupportEndDate,
			YearMonth commercialSupportEndDate, Map<String, List<String>> linkedGenerations, LatestPatch latestPatch) {
		this.name = name;
		this.initialReleaseDate = initialReleaseDate.atEndOfMonth();
		this.support = support;
		this.ossSupportEndDate = (ossSupportEndDate != null) ? ossSupportEndDate.atEndOfMonth() : null;
		this.commercialSupportEndDate = (commercialSupportEndDate != null) ? commercialSupportEndDate.atEndOfMonth()
				: null;
		this.linkedGenerations = linkedGenerations;
		this.latestPatch = latestPatch;
	}

	public String getName() {
		return this.name;
	}

	public LocalDate getInitialReleaseDate() {
		return this.initialReleaseDate;
	}

	public String getSupport() {
		return this.support;
	}

	public LocalDate getOssSupportEndDate() {
		return this.ossSupportEndDate;
	}

	public LocalDate getCommercialSupportEndDate() {
		return this.commercialSupportEndDate;
	}

	public Map<String, List<String>> getLinkedGenerations() {
		return this.linkedGenerations;
	}

	public LatestPatch getLatestPatch() {
		return this.latestPatch;
	}

	/**
	 * Representation of the latest patch version for a generation.
	 *
	 */
	@JsonInclude(Include.NON_NULL)
	public static class LatestPatch {

		private final String oss;

		private final String enterprise;

		public LatestPatch(String oss, String enterprise) {
			this.oss = oss;
			this.enterprise = enterprise;
		}

		public String getOss() {
			return this.oss;
		}

		public String getEnterprise() {
			return this.enterprise;
		}

	}

}
