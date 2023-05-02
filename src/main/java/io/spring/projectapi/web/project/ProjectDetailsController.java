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

import io.spring.projectapi.contentful.ContentfulService;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@link RestController} for project details.
 *
 * @author Madhura Bhave
 */
@RestController
@RequestMapping(path = "/projects/{id}/details")
class ProjectDetailsController {

	private final ContentfulService contentfulService;

	ProjectDetailsController(ContentfulService contentfulService) {
		this.contentfulService = contentfulService;
	}

	@PatchMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<String> patchProjectDetails(@PathVariable String id, @RequestBody ProjectDetails projectDetails) {
		io.spring.projectapi.contentful.ProjectDetails details = new io.spring.projectapi.contentful.ProjectDetails(
				projectDetails.getBootConfig(), projectDetails.getBody());
		this.contentfulService.patchProjectDetails(id, details);
		return ResponseEntity.noContent().build();
	}

}
