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

package io.spring.projectapi.web.index;

import io.spring.projectapi.web.project.ProjectsController;
import io.spring.projectapi.web.repository.RepositoriesController;

import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Lists all resources at the root of the API.
 *
 * @author Madhura Bhave
 */
@RestController
@RequestMapping(path = "/", produces = MediaTypes.HAL_JSON_VALUE)
public class IndexController {

	@GetMapping
	public RepresentationModel index() {
		return RepresentationModel.of(null)
			.add(linkTo(methodOn(ProjectsController.class).projects()).withRel("projects"),
					linkTo(methodOn(RepositoriesController.class).repositories()).withRel("repositories"));
	}

}
