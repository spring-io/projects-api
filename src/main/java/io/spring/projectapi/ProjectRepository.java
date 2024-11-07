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

package io.spring.projectapi;

import java.util.Collection;
import java.util.List;

import io.spring.projectapi.github.Project;
import io.spring.projectapi.github.ProjectDocumentation;
import io.spring.projectapi.github.ProjectSupport;
import io.spring.projectapi.web.webhook.CacheController;

/**
 * Stores project information. Updates triggered via {@link CacheController}.
 *
 * @author Madhura Bhave
 */
public interface ProjectRepository {

	void update();

	Collection<Project> getProjects();

	Project getProject(String projectSlug);

	List<ProjectDocumentation> getProjectDocumentations(String projectSlug);

	List<ProjectSupport> getProjectSupports(String projectSlug);

	String getProjectSupportPolicy(String projectSlug);

}
