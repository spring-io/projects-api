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

package io.spring.projectapi.github;

import java.util.Collection;
import java.util.List;

import io.spring.projectapi.ProjectRepository;

import org.springframework.stereotype.Component;

/**
 * {@link ProjectRepository} backed by Github.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
@Component
class GithubProjectRepository implements ProjectRepository {

	private final GithubQueries githubQueries;

	private transient ProjectData projectData;

	GithubProjectRepository(GithubQueries githubQueries) {
		this.githubQueries = githubQueries;
		this.projectData = ProjectData.load(githubQueries);
	}

	@Override
	public void update() {
		this.projectData = ProjectData.load(this.githubQueries);
	}

	@Override
	public Collection<Project> getProjects() {
		return this.projectData.project().values();
	}

	@Override
	public Project getProject(String projectSlug) {
		Project project = this.projectData.project().get(projectSlug);
		NoSuchGithubProjectException.throwIfNotFound(project, projectSlug);
		return project;
	}

	@Override
	public List<ProjectDocumentation> getProjectDocumentations(String projectSlug) {
		List<ProjectDocumentation> documentations = this.projectData.documentation().get(projectSlug);
		NoSuchGithubProjectException.throwIfNotFound(documentations, projectSlug);
		return documentations;
	}

	@Override
	public List<ProjectSupport> getProjectSupports(String projectSlug) {
		List<ProjectSupport> projectSupports = this.projectData.support().get(projectSlug);
		NoSuchGithubProjectException.throwIfNotFound(projectSupports, projectSlug);
		return projectSupports;
	}

	@Override
	public String getProjectSupportPolicy(String projectSlug) {
		String policy = this.projectData.supportPolicy().get(projectSlug);
		NoSuchGithubProjectException.throwIfNotFound(policy, projectSlug);
		return policy;
	}

}
