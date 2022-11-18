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

package io.spring.projectapi.project;

import java.util.ArrayList;
import java.util.List;

import io.spring.projectapi.ContentfulService;
import io.spring.projectapi.ResourceNotFoundException;
import io.spring.projectapi.generation.GenerationMetadataController;
import io.spring.projectapi.release.ReleaseMetadataController;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Expose {@link ProjectMetadata} resources.
 *
 * @author Madhura Bhave
 */
@RestController
@RequestMapping(path = "/projects", produces = MediaTypes.HAL_JSON_VALUE)
@ExposesResourceFor(ProjectMetadata.class)
public class ProjectMetadataController {

	private final ContentfulService contentfulService;

	private final EntityLinks entityLinks;

	public ProjectMetadataController(ContentfulService contentfulService, EntityLinks entityLinks) {
		this.contentfulService = contentfulService;
		this.entityLinks = entityLinks;
	}

	@GetMapping("")
	public CollectionModel<EntityModel<ProjectMetadata>> listProjects() {
		List<ProjectMetadata> projects = this.contentfulService.getProjects();
		List<EntityModel<ProjectMetadata>> models = new ArrayList<>();
		for (ProjectMetadata project : projects) {
			EntityModel<ProjectMetadata> model = getProjectMetadataEntityModel(project);
			models.add(model);
		}
		CollectionModel<EntityModel<ProjectMetadata>> collection = CollectionModel.of(models);
		collection.add(linkTo(methodOn(ProjectMetadataController.class).showProject(null)).withRel("project"));
		return collection;
	}

	private EntityModel<ProjectMetadata> getProjectMetadataEntityModel(ProjectMetadata project) {
		EntityModel<ProjectMetadata> model = EntityModel.of(project);
		Link selfLink = this.entityLinks.linkToItemResource(ProjectMetadata.class, project.getSlug()).withSelfRel();
		model.add(linkTo(methodOn(ReleaseMetadataController.class).listReleases(project.getSlug())).withRel("releases"),
				linkTo(methodOn(GenerationMetadataController.class).listGenerations(project.getSlug()))
						.withRel("generations"),
				selfLink);
		return model;
	}

	@GetMapping("/{id}")
	public EntityModel<ProjectMetadata> showProject(@PathVariable String id) {
		List<ProjectMetadata> projects = this.contentfulService.getProjects();
		for (ProjectMetadata project : projects) {
			if (project.getSlug().equals(id)) {
				return getProjectMetadataEntityModel(project);
			}
		}
		throw new ResourceNotFoundException("Project not found for id: " + id);
	}

}
