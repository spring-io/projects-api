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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.spring.projectapi.ResourceNotFoundException;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Expose {@link Repository} resources.
 *
 * @author Madhura Bhave
 */
@RestController
@RequestMapping(path = "/repositories", produces = MediaTypes.HAL_JSON_VALUE)
@ExposesResourceFor(RepositoryMetadata.class)
public class RepositoryMetadataController {

	@GetMapping("")
	public CollectionModel<EntityModel<RepositoryMetadata>> listRepositories() {
		List<EntityModel<RepositoryMetadata>> models = new ArrayList<>();
		for (Repository repository : Repository.values()) {
			EntityModel<RepositoryMetadata> entityModel = getEntityModel(repository);
			models.add(entityModel);
		}
		return CollectionModel.of(models);
	}

	private EntityModel<RepositoryMetadata> getEntityModel(Repository repository) {
		RepositoryMetadata metadata = new RepositoryMetadata(repository.getId(), repository.getName(),
				repository.getUrl(), repository.isSnapshotsEnabled());
		EntityModel<RepositoryMetadata> model = EntityModel.of(metadata);
		model.add(
				linkTo(methodOn(RepositoryMetadataController.class).showRepository(repository.getId())).withSelfRel());
		return model;
	}

	@GetMapping("/{id}")
	public EntityModel<RepositoryMetadata> showRepository(@PathVariable String id) {
		Repository repository = Arrays.stream(Repository.values()).filter(r -> r.getId().equals(id)).findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("No artifact repository found with id: " + id));
		return getEntityModel(repository);
	}

}
