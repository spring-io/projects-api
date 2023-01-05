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

package io.spring.projectapi.web.repository;

import java.util.List;

import org.springframework.hateoas.server.core.Relation;

/**
 * Representation of a repository.
 *
 * @author Madhura Bhave
 */
@Relation(collectionRelation = "repositories")
public class Repository {

	/**
	 * The snapshot repository.
	 */
	public static final Repository SNAPSHOT = new Repository("spring-snapshots", "Spring Snapshots",
			"https://repo.spring.io/snapshot", true);

	/**
	 * The milestone repository.
	 */
	public static final Repository MILESTONE = new Repository("spring-milestones", "Spring Milestones",
			"https://repo.spring.io/milestone", false);

	/**
	 * The release repository.
	 */
	public static final Repository RELEASE = new Repository("spring-releases", "Spring Releases",
			"https://repo.spring.io/release", false);

	/**
	 * All repositories.
	 */
	public static final List<Repository> ALL = List.of(SNAPSHOT, MILESTONE, RELEASE);

	private final String id;

	private final String name;

	private final String url;

	private final boolean snapshotsEnabled;

	Repository(String id, String name, String url, boolean snapshotsEnabled) {
		this.id = id;
		this.name = name;
		this.url = url;
		this.snapshotsEnabled = snapshotsEnabled;
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
