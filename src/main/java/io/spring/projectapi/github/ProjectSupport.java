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

package io.spring.projectapi.github;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Java representation of the {@code project support} type as defined in
 * <a href= "https://github.com/spring-io/spring-website/">spring-io/spring-website</a>.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
public class ProjectSupport {

	private final String branch;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private final LocalDate initialDate;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private final LocalDate ossPolicyEnd;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private final LocalDate commercialPolicyEnd;

	@JsonCreator(mode = Mode.PROPERTIES)
	public ProjectSupport(String branch, LocalDate initialRelease, LocalDate ossPolicyEnd,
			LocalDate commercialPolicyEnd) {
		this.branch = branch;
		this.initialDate = initialRelease;
		this.ossPolicyEnd = ossPolicyEnd;
		this.commercialPolicyEnd = commercialPolicyEnd;
	}

	public String getBranch() {
		return this.branch;
	}

	public LocalDate getInitialDate() {
		return this.initialDate;
	}

	public LocalDate getOssPolicyEnd() {
		return this.ossPolicyEnd;
	}

	public LocalDate getCommercialPolicyEnd() {
		return this.commercialPolicyEnd;
	}

}
