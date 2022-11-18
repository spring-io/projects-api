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

package io.spring.projectapi.repository;

import io.spring.projectapi.release.ReleaseStatus;

/**
 * Spring repository hosting project artifacts.
 *
 * @author Madhura Bhave
 */
public enum Repository {

	SNAPSHOT("spring-snapshots", "Spring Snapshots", "https://repo.spring.io/snapshot", true),
	MILESTONE("spring-milestones", "Spring Milestones", "https://repo.spring.io/milestone", false),
	RELEASE("spring-releases", "Spring Releases", "https://repo.spring.io/release", false);

	private final String id;

	private final String name;

	private final String url;

	private final boolean snapshotsEnabled;

	Repository(String id, String name, String url, Boolean snapshotsEnabled) {
		this.id = id;
		this.name = name;
		this.url = url;
		this.snapshotsEnabled = snapshotsEnabled;
	}

	/**
	 * Deduce the artifact repository hosting artifact for this {@link ReleaseStatus}
	 * @param status the release status
	 * @return the artifact repository
	 */
	public static Repository of(ReleaseStatus status) {
		switch (status) {
			case GENERAL_AVAILABILITY:
				return RELEASE;
			case PRERELEASE:
				return MILESTONE;
			case SNAPSHOT:
				return SNAPSHOT;
			default:
				return RELEASE;
		}
	}

	public String getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public String getUrl() {
		return this.url;
	}

	public boolean isSnapshotsEnabled() {
		return this.snapshotsEnabled;
	}

}
