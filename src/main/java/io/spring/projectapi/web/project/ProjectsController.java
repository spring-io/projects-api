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

package io.spring.projectapi.web.project;

import java.util.List;

import io.spring.projectapi.ProjectRepository;
import io.spring.projectapi.web.generation.GenerationsController;
import io.spring.projectapi.web.project.Project.Status;
import io.spring.projectapi.web.release.ReleasesController;

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
 * MVC controller for projects API.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
@RestController
@RequestMapping(path = "/projects", produces = MediaTypes.HAL_JSON_VALUE)
@ExposesResourceFor(Project.class)
public class ProjectsController {

	private final ProjectRepository projectRepository;

	private final EntityLinks entityLinks;

	public ProjectsController(ProjectRepository projectRepository, EntityLinks entityLinks) {
		this.projectRepository = projectRepository;
		this.entityLinks = entityLinks;
	}

	@GetMapping
	public CollectionModel<EntityModel<Project>> projects() {
		List<Project> projects = this.projectRepository.getProjects().stream().map(this::asProject).toList();
		CollectionModel<EntityModel<Project>> collection = CollectionModel.of(projects.stream().map((project) -> {
			try {
				return asModel(project);
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}).toList());
		collection.add(linkTo(methodOn(ProjectsController.class).project(null)).withRel("project"));
		return collection;
	}

	@GetMapping("/{id}")
	public EntityModel<Project> project(@PathVariable String id) {
		Project project = asProject(this.projectRepository.getProject(id));
		return asModel(project);
	}

	private Project asProject(io.spring.projectapi.github.Project project) {
		Project.Status status = (project.getStatus() != null) ? Status.valueOf(project.getStatus().name()) : null;
		return new Project(project.getTitle(), project.getSlug(), project.getGithub(), status);
	}

	private EntityModel<Project> asModel(Project project) {
		EntityModel<Project> model = EntityModel.of(project);
		String id = project.getSlug();
		Link linkToReleases = linkTo(methodOn(ReleasesController.class).releases(id)).withRel("releases");
		Link linkToGenerations = linkTo(methodOn(GenerationsController.class).generations(id)).withRel("generations");
		Link linkToSelf = this.entityLinks.linkToItemResource(Project.class, id).withSelfRel();
		model.add(linkToReleases, linkToGenerations, linkToSelf);
		return model;
	}

}
