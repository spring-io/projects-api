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

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Java representation of the {@code project generation} type as defined in
 * <a href= "https://github.com/spring-io/spring-website/">spring-io/spring-website</a>.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectGeneration {

	private List<Generation> generations = new ArrayList<>();

	@JsonCreator(mode = Mode.PROPERTIES)
	public ProjectGeneration(List<Generation> generations) {
		this.generations.addAll(generations);
	}

	public List<Generation> getGenerations() {
		return this.generations;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Generation {

		private final String generation;

		@JsonFormat(pattern = "yyyy-MM")
		private final YearMonth initialRelease;

		@JsonFormat(pattern = "yyyy-MM")
		private final YearMonth ossSupportEnd;

		@JsonFormat(pattern = "yyyy-MM")
		private final YearMonth enterpriseSupportEnd;

		private final boolean lastMinor;

		@JsonCreator(mode = Mode.PROPERTIES)
		public Generation(String generation, YearMonth initialRelease, YearMonth ossSupportEnd,
				YearMonth enterpriseSupportEnd, boolean lastMinor) {
			this.generation = generation;
			this.initialRelease = initialRelease;
			this.ossSupportEnd = ossSupportEnd;
			this.enterpriseSupportEnd = enterpriseSupportEnd;
			this.lastMinor = lastMinor;
		}

		public String getGeneration() {
			return this.generation;
		}

		public YearMonth getInitialRelease() {
			return this.initialRelease;
		}

		public YearMonth getOssSupportEnd() {
			return this.ossSupportEnd;
		}

		public YearMonth getEnterpriseSupportEnd() {
			return this.enterpriseSupportEnd;
		}

		public boolean isLastMinor() {
			return this.lastMinor;
		}

	}

}
