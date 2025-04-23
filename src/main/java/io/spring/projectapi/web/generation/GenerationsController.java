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

package io.spring.projectapi.web.generation;

import java.time.LocalDate;
import java.util.List;

import io.spring.projectapi.ProjectRepository;
import io.spring.projectapi.github.ProjectSupport;
import io.spring.projectapi.project.ProjectSupportProvider;
import io.spring.projectapi.web.error.ResourceNotFoundException;
import io.spring.projectapi.web.project.ProjectsController;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * MVC controller for project generations API.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
@RestController
@RequestMapping(path = "/projects/{id}/generations", produces = MediaTypes.HAL_JSON_VALUE)
@ExposesResourceFor(Generation.class)
public class GenerationsController {

	private final ProjectRepository projectRepository;

	private final ProjectSupportProvider projectSupportProvider;

	public GenerationsController(ProjectRepository projectRepository, ProjectSupportProvider projectSupportProvider) {
		this.projectRepository = projectRepository;
		this.projectSupportProvider = projectSupportProvider;
	}

	@GetMapping
	public CollectionModel<EntityModel<Generation>> generations(@PathVariable String id) {
		List<ProjectSupport> supports = this.projectSupportProvider.getProjectSupports(id);
		String supportPolicy = this.projectRepository.getProjectSupportPolicy(id);
		List<Generation> generations = supports.stream()
			.map((support) -> asGeneration(support, supportPolicy))
			.toList();
		CollectionModel<EntityModel<Generation>> model = CollectionModel
			.of(generations.stream().map((generation) -> asModel(id, generation)).toList());
		model.add(linkToProject(id));
		return model;
	}

	@GetMapping("/{name}")
	public EntityModel<Generation> generation(@PathVariable String id, @PathVariable String name) {
		List<ProjectSupport> supports = this.projectRepository.getProjectSupports(id);
		String supportPolicy = this.projectRepository.getProjectSupportPolicy(id);
		List<Generation> generations = supports.stream()
			.map((support) -> asGeneration(support, supportPolicy))
			.toList();
		Generation generation = generations.stream()
			.filter((candidate) -> candidate.getName().equals(name))
			.findFirst()
			.orElseThrow(() -> new ResourceNotFoundException(
					"Generation '%s' cannot be found for project '%s'".formatted(name, id)));
		return asModel(id, generation);
	}

	private Generation asGeneration(ProjectSupport support, String supportPolicy) {
		LocalDate ossPolicyEnd = SupportPolicyCalculator.getOSSPolicyEnd(support.getInitialDate(),
				support.getOssPolicyEnd(), supportPolicy);
		LocalDate commercialPolicyEnd = SupportPolicyCalculator.getEnterprisePolicyEnd(support.getInitialDate(),
				support.getCommercialPolicyEnd(), supportPolicy, support.isLastMinor());
		return new Generation(support.getBranch(), support.getInitialDate(), ossPolicyEnd, commercialPolicyEnd);
	}

	private EntityModel<Generation> asModel(String id, Generation generation) {
		EntityModel<Generation> model = EntityModel.of(generation);
		Link linkToSelf = linkTo(methodOn(GenerationsController.class).generation(id, generation.getName()))
			.withSelfRel();
		model.add(linkToSelf);
		model.add(linkToProject(id));
		return model;
	}

	private Link linkToProject(String id) {
		return linkTo(methodOn(ProjectsController.class).project(id)).withRel("project");
	}

}
