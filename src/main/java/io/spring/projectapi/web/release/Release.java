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

import java.util.regex.Pattern;

import org.springframework.hateoas.server.core.Relation;
import org.springframework.util.Assert;

/**
 * Representation of a project release.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
@Relation(collectionRelation = "releases")
public class Release {

	private final String version;

	private final String apiDocUrl;

	private final String referenceDocUrl;

	private final Status status;

	private final boolean current;

	public Release(String version, String apiDocUrl, String referenceDocUrl, Status status, boolean current) {
		this.version = version;
		this.status = status;
		this.current = current;
		this.referenceDocUrl = referenceDocUrl;
		this.apiDocUrl = apiDocUrl;
	}

	public String getVersion() {
		return this.version;
	}

	public Status getStatus() {
		return this.status;
	}

	public boolean isCurrent() {
		return this.current;
	}

	public String getReferenceDocUrl() {
		return this.referenceDocUrl;
	}

	public String getApiDocUrl() {
		return this.apiDocUrl;
	}

	/**
	 * Project release status.
	 */
	public enum Status {

		/**
		 * Snapshot.
		 */
		SNAPSHOT,

		/**
		 * Pre-release.
		 */
		PRERELEASE,

		/**
		 * General Availability.
		 */
		GENERAL_AVAILABILITY;

		private static final Pattern PRERELEASE_PATTERN = Pattern.compile("[A-Za-z0-9\\.\\-]+?(M|RC)\\d+");

		private static final String SNAPSHOT_SUFFIX = "SNAPSHOT";

		/**
		 * Deduce the {@link Status status} of a release given its {@code version}.
		 * @param version a project version
		 * @return the release status for this version
		 */
		public static Status fromVersion(String version) {
			Assert.notNull(version, "'version' must not be null");
			if (version.endsWith(SNAPSHOT_SUFFIX)) {
				return SNAPSHOT;
			}
			if (PRERELEASE_PATTERN.matcher(version).matches()) {
				return PRERELEASE;
			}
			return GENERAL_AVAILABILITY;
		}

	}

}
