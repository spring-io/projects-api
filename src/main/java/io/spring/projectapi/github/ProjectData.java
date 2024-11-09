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

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

/**
 * Represents cached data from Github.
 *
 * @param project all projects
 * @param documentation map of project slug to project documentations
 * @param support map of project slug to project supports
 * @param supportPolicy map of project slug to project support policy
 * @author Phillip Webb
 * @author Madhura Bhave
 */
record ProjectData(Map<String, Project> project, Map<String, List<ProjectDocumentation>> documentation,
		Map<String, List<ProjectSupport>> support, Map<String, String> supportPolicy) {

	public static ProjectData load(GithubQueries githubQueries) {
		ProjectData data = githubQueries.getData();
		return getImmutableProjectData(data);
	}

	public static ProjectData update(ProjectData data, List<String> changes, GithubQueries githubQueries) {
		ProjectData updatedData = githubQueries.updateData(data, changes);
		return getImmutableProjectData(updatedData);
	}

	@NotNull
	private static ProjectData getImmutableProjectData(ProjectData updatedData) {
		Map<String, Project> projects = updatedData.project();
		Map<String, List<ProjectDocumentation>> documentation = updatedData.documentation();
		Map<String, List<ProjectSupport>> support = updatedData.support();
		Map<String, String> supportPolicy = updatedData.supportPolicy();
		return new ProjectData(Map.copyOf(projects), Map.copyOf(documentation), Map.copyOf(support),
				Map.copyOf(supportPolicy));
	}

}
