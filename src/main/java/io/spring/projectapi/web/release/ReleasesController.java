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

import java.net.URI;
import java.util.List;

import io.spring.projectapi.contentful.ContentfulService;
import io.spring.projectapi.contentful.ProjectDocumentation;
import io.spring.projectapi.web.error.ResourceNotFoundException;
import io.spring.projectapi.web.project.ProjectsController;
import io.spring.projectapi.web.release.Release.Status;
import io.spring.projectapi.web.repository.RepositoriesController;
import io.spring.projectapi.web.repository.Repository;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
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
 * MVC controller for project releases API.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
@RestController
@RequestMapping(path = "/projects/{id}/releases", produces = MediaTypes.HAL_JSON_VALUE)
@ExposesResourceFor(Release.class)
public class ReleasesController {

	private final ContentfulService contentfulService;

	public ReleasesController(ContentfulService contentfulService) {
		this.contentfulService = contentfulService;
	}

	@GetMapping
	public CollectionModel<EntityModel<Release>> releases(@PathVariable String id) {
		List<ProjectDocumentation> documentations = this.contentfulService.getProjectDocumentations(id);
		List<Release> releases = documentations.stream().map(this::asRelease).toList();
		CollectionModel<EntityModel<Release>> model = CollectionModel
			.of(releases.stream().map((generation) -> asModel(id, generation)).toList());
		Link linkToProject = WebMvcLinkBuilder.linkTo(methodOn(ProjectsController.class).project(id))
			.withRel("project");
		Link linkToCurrent = linkTo(methodOn(ReleasesController.class).current(id)).withRel("current");
		model.add(linkToProject, linkToCurrent);
		return model;
	}

	@GetMapping("/{version}")
	public EntityModel<Release> release(@PathVariable String id, @PathVariable String version) {
		List<ProjectDocumentation> documentations = this.contentfulService.getProjectDocumentations(id);
		List<Release> releases = documentations.stream().map(this::asRelease).toList();
		Release release = releases.stream()
			.filter((candidate) -> candidate.getVersion().equals(version))
			.findFirst()
			.orElseThrow(() -> new ResourceNotFoundException(
					"Version '%s' cannot be found for project '%s'".formatted(version, id)));
		return asModel(id, release);
	}

	@GetMapping("/current")
	public EntityModel<Release> current(@PathVariable String id) {
		this.contentfulService.getProjectDocumentations(id);
		List<ProjectDocumentation> documentations = this.contentfulService.getProjectDocumentations(id);
		List<Release> releases = documentations.stream().map(this::asRelease).toList();
		Release release = releases.stream()
			.filter(Release::isCurrent)
			.findFirst()
			.orElseThrow(() -> new ResourceNotFoundException(
					"Could not find current release for project '%s'".formatted(id)));
		return asModel(id, release);
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> add(@PathVariable String id, @RequestBody NewRelease release) {
		String version = release.getVersion();
		List<ProjectDocumentation> documentations = this.contentfulService.getProjectDocumentations(id);
		if (documentations.stream().anyMatch((candidate) -> candidate.getVersion().equals(version))) {
			String message = "Release '%s' already present for project '%s'".formatted(version, id);
			return ResponseEntity.badRequest().body(message);
		}
		Release.Status status = Release.Status.fromVersion(version);
		ProjectDocumentation projectDocumentation = new ProjectDocumentation(release.getVersion(), release.isAntora(),
				release.getApiDocUrl(), release.getReferenceDocUrl(),
				ProjectDocumentation.Status.valueOf(status.name()), false);
		this.contentfulService.addProjectDocumentation(id, projectDocumentation);
		URI linkToRelease = linkTo(methodOn(ReleasesController.class).release(id, release.getVersion())).toUri();
		return ResponseEntity.created(linkToRelease).build();
	}

	@DeleteMapping("/{version}")
	public ResponseEntity<String> delete(@PathVariable String id, @PathVariable String version) {
		this.contentfulService.deleteDocumentation(id, version);
		return ResponseEntity.noContent().build();
	}

	private Release asRelease(ProjectDocumentation documentation) {
		Release.Status status = Status.valueOf(documentation.getStatus().name());
		return new Release(documentation.getVersion(), documentation.getApi(), documentation.getRef(), status,
				documentation.isCurrent());
	}

	private EntityModel<Release> asModel(String id, Release release) {
		EntityModel<Release> model = EntityModel.of(release);
		Repository repository = getRepository(release.getStatus());
		Link linkToSelf = linkTo(methodOn(ReleasesController.class).release(id, release.getVersion())).withSelfRel();
		Link linkToRepository = linkTo(methodOn(RepositoriesController.class).repository(repository.getIdentifier()))
			.withRel("repository");
		model.add(linkToRepository, linkToSelf);
		return model;
	}

	private Repository getRepository(Release.Status status) {
		return switch (status) {
			case SNAPSHOT -> Repository.SNAPSHOT;
			case PRERELEASE -> Repository.MILESTONE;
			case GENERAL_AVAILABILITY -> Repository.RELEASE;
		};
	}

}
