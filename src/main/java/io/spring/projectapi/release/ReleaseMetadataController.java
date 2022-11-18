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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import io.spring.projectapi.ContentfulService;
import io.spring.projectapi.ResourceNotFoundException;
import io.spring.projectapi.project.ProjectMetadataController;
import io.spring.projectapi.repository.Repository;
import io.spring.projectapi.repository.RepositoryMetadataController;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Expose {@link ReleaseMetadata} resources.
 *
 * @author Madhura Bhave
 */
@RestController
@RequestMapping(produces = MediaTypes.HAL_JSON_VALUE)
@ExposesResourceFor(ReleaseMetadata.class)
public class ReleaseMetadataController {

	private final ContentfulService contentfulService;

	private final EntityLinks entityLinks;

	public ReleaseMetadataController(ContentfulService contentfulService, EntityLinks entityLinks) {
		this.contentfulService = contentfulService;
		this.entityLinks = entityLinks;
	}

	@GetMapping("/projects/{projectId}/releases")
	public CollectionModel<EntityModel<ReleaseMetadata>> listReleases(@PathVariable String projectId) {
		List<ReleaseMetadata> releases = this.contentfulService.getReleases(projectId);
		if (releases == null) {
			throw new ResourceNotFoundException("Could not find releases for project: " + projectId);
		}
		List<EntityModel<ReleaseMetadata>> models = new ArrayList<>();
		for (ReleaseMetadata release : releases) {
			EntityModel<ReleaseMetadata> model = getReleaseMetadataEntityModel(projectId, release);
			models.add(model);
		}
		CollectionModel<EntityModel<ReleaseMetadata>> collection = CollectionModel.of(models);
		collection.add(WebMvcLinkBuilder.linkTo(methodOn(ProjectMetadataController.class).showProject(projectId))
				.withRel("project"));
		collection.add(
				linkTo(methodOn(ReleaseMetadataController.class).showCurrentRelease(projectId)).withRel("current"));
		return collection;
	}

	private EntityModel<ReleaseMetadata> getReleaseMetadataEntityModel(String projectId, ReleaseMetadata release) {
		EntityModel<ReleaseMetadata> model = EntityModel.of(release);
		model.add(linkTo(methodOn(ReleaseMetadataController.class).showRelease(projectId, release.getVersion()))
				.withSelfRel());
		model.add(linkTo(
				methodOn(RepositoryMetadataController.class).showRepository(Repository.of(release.getStatus()).getId()))
						.withRel("repository"));
		return model;
	}

	@PostMapping(path = "/projects/{projectId}/releases", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> createRelease(@PathVariable String projectId,
			@RequestBody ReleaseMetadataInput input) {
		List<ReleaseMetadata> releases = this.contentfulService.getReleases(projectId);
		if (releases == null) {
			throw new ResourceNotFoundException("Could not find releases: " + projectId);
		}
		if (releases.stream().anyMatch((releaseMetadata) -> releaseMetadata.getVersion().equals(input.getVersion()))) {
			throw new InvalidReleaseException("Release already present: " + input.getVersion());
		}
		ReleaseMetadata newRelease = new ReleaseMetadata();
		newRelease.setVersion(input.getVersion());
		newRelease.setApiDocUrl(input.getApiDocUrl());
		newRelease.setReferenceDocUrl(input.getReferenceDocUrl());
		this.contentfulService.addRelease(projectId, newRelease);
		URI newReleaseURI = linkTo(methodOn(ReleaseMetadataController.class).showRelease(projectId, input.getVersion()))
				.toUri();
		return ResponseEntity.created(newReleaseURI).build();
	}

	@GetMapping("/projects/{projectId}/releases/current")
	public EntityModel<ReleaseMetadata> showCurrentRelease(@PathVariable String projectId) {
		List<ReleaseMetadata> releases = this.contentfulService.getReleases(projectId);
		if (releases == null) {
			throw new ResourceNotFoundException("Could not find releases for project: " + projectId);
		}
		for (ReleaseMetadata release : releases) {
			if (release.isCurrent()) {
				return getReleaseMetadataEntityModel(projectId, release);
			}
		}
		throw new ResourceNotFoundException("Could not find current release for project: " + projectId);
	}

	@GetMapping("/projects/{projectId}/releases/{version}")
	public EntityModel<ReleaseMetadata> showRelease(@PathVariable String projectId, @PathVariable String version) {
		List<ReleaseMetadata> releases = this.contentfulService.getReleases(projectId);
		for (ReleaseMetadata release : releases) {
			if (release.getVersion().equals(version)) {
				return getReleaseMetadataEntityModel(projectId, release);
			}
		}
		throw new ResourceNotFoundException(
				"Could not find release for project: " + projectId + " and version: " + version);
	}

	@DeleteMapping("/projects/{projectId}/releases/{version}")
	public ResponseEntity<String> deleteRelease(@PathVariable String projectId, @PathVariable String version) {
		List<ReleaseMetadata> release = this.contentfulService.getReleases(projectId);
		if (release == null) {
			throw new ResourceNotFoundException(
					"Could not find release for project: " + projectId + " and version: " + version);
		}
		this.contentfulService.deleteRelease(projectId, version);
		return ResponseEntity.noContent().build();
	}

}
