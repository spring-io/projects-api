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

import java.util.Optional;

import io.spring.projectapi.web.error.ResourceNotFoundException;

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
 * MVC controller for repositories API.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
@RestController
@RequestMapping(path = "/repositories", produces = MediaTypes.HAL_JSON_VALUE)
@ExposesResourceFor(Repository.class)
public class RepositoriesController {

	@GetMapping
	public CollectionModel<EntityModel<Repository>> repositories() {
		return CollectionModel.of(Repository.ALL.stream().map(this::asModel).toList());
	}

	@GetMapping("/{id}")
	public EntityModel<Repository> repository(@PathVariable String id) {
		return asModel(findRepository(id).orElseThrow(
				() -> new ResourceNotFoundException("No artifact repository found with id '%s'".formatted(id))));
	}

	private Optional<Repository> findRepository(String id) {
		return Repository.ALL.stream().filter((candidate) -> candidate.getIdentifier().equals(id)).findFirst();
	}

	private EntityModel<Repository> asModel(Repository repository) {
		EntityModel<Repository> model = EntityModel.of(repository);
		Link linkToSelf = linkTo(methodOn(RepositoriesController.class).repository(repository.getIdentifier()))
				.withSelfRel();
		model.add(linkToSelf);
		return model;
	}

}
