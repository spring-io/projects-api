/*
 * Copyright (c) 2022. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package io.spring.projectapi.repository;

import org.springframework.hateoas.server.core.Relation;

/**
 * Representation of a repository.
 *
 * @author Madhura Bhave
 */
@Relation(collectionRelation = "repositories")
class RepositoryMetadata {

	private final String id;

	private final String name;

	private final String url;

	private final boolean snapshotsEnabled;

	RepositoryMetadata(String id, String name, String url, Boolean snapshotsEnabled) {
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
