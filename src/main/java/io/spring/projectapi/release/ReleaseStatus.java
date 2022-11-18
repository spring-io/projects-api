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

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Release status for projects.
 *
 * @author Madhura Bhave
 */
public enum ReleaseStatus {

	/**
	 * Unstable version with limited support
	 */
	SNAPSHOT,
	/**
	 * Pre-Release version meant to be tested by the community
	 */
	PRERELEASE,
	/**
	 * Release Generally Available on public artifact repositories and enjoying full
	 * support from maintainers
	 */
	GENERAL_AVAILABILITY;

	@JsonCreator
	public static ReleaseStatus forName(String name) {
		for (ReleaseStatus status : values()) {
			if (status.name().equals(name)) {
				return status;
			}
		}

		return null;
	}

}
