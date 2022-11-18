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

package io.spring.projectapi.generation;

import java.util.ArrayList;
import java.util.List;

import io.spring.projectapi.ContentfulService;
import io.spring.projectapi.ResourceNotFoundException;
import io.spring.projectapi.project.ProjectMetadataController;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Expose {@link GenerationMetadata} resources.
 *
 * @author Madhura Bhave
 */
@RestController
@RequestMapping(produces = MediaTypes.HAL_JSON_VALUE)
@ExposesResourceFor(GenerationMetadata.class)
public class GenerationMetadataController {

	private final ContentfulService contentfulService;

	private final EntityLinks entityLinks;

	public GenerationMetadataController(ContentfulService contentfulService, EntityLinks entityLinks) {
		this.contentfulService = contentfulService;
		this.entityLinks = entityLinks;
	}

	@GetMapping("/projects/{projectId}/generations")
	public ResponseEntity<CollectionModel<EntityModel<GenerationMetadata>>> listGenerations(
			@PathVariable String projectId) {
		List<GenerationMetadata> generations = this.contentfulService.getGenerations(projectId);
		if (generations == null) {
			throw new ResourceNotFoundException("Could not find releases for project: " + projectId);
		}
		List<EntityModel<GenerationMetadata>> models = new ArrayList<>();
		for (GenerationMetadata generation : generations) {
			EntityModel<GenerationMetadata> model = getGenerationMetadataEntityModel(projectId, generation);
			models.add(model);
		}
		CollectionModel<EntityModel<GenerationMetadata>> collection = CollectionModel.of(models);
		collection.add(WebMvcLinkBuilder.linkTo(methodOn(ProjectMetadataController.class).showProject(projectId))
				.withRel("project"));
		return ResponseEntity.ok().body(collection);
	}

	private EntityModel<GenerationMetadata> getGenerationMetadataEntityModel(String projectId, GenerationMetadata m) {
		EntityModel<GenerationMetadata> model = EntityModel.of(m);
		Link selfLink = linkTo(methodOn(GenerationMetadataController.class).showRelease(projectId, m.getName()))
				.withSelfRel();
		model.add(selfLink);
		model.add(linkTo(methodOn(ProjectMetadataController.class).showProject(projectId)).withRel("project"));
		return model;
	}

	@GetMapping("/projects/{projectId}/generations/{name}")
	public ResponseEntity<EntityModel<GenerationMetadata>> showRelease(@PathVariable String projectId,
			@PathVariable String name) {
		List<GenerationMetadata> generations = this.contentfulService.getGenerations(projectId);
		for (GenerationMetadata generation : generations) {
			if (generation.getName().equals(name)) {
				// FIXME
				// long lastModified =
				// project.getGenerationsInfo().getLastModified().toEpochSecond() * 1000;
				return ResponseEntity.ok().body(getGenerationMetadataEntityModel(projectId, generation));
			}
		}
		throw new ResourceNotFoundException("Could not find releases for project: " + projectId);

	}

}
