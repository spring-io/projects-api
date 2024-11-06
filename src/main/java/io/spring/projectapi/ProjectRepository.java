package io.spring.projectapi;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.spring.projectapi.github.GithubOperations;
import io.spring.projectapi.github.Project;
import io.spring.projectapi.github.ProjectDocumentation;
import io.spring.projectapi.github.ProjectSupport;
import io.spring.projectapi.web.CacheController;

import org.springframework.stereotype.Component;

/**
 * Caches Github project information. Populated on start up and updates triggered via
 * {@link CacheController}.
 */
@Component
public class ProjectRepository {

	private Map<String, Project> project = new ConcurrentHashMap<>();

	private Map<String, List<ProjectDocumentation>> documentation = new ConcurrentHashMap<>();

	private Map<String, List<ProjectSupport>> support = new ConcurrentHashMap<>();

	private Map<String, String> supportPolicy = new ConcurrentHashMap<>();

	private final GithubOperations githubOperations;

	public ProjectRepository(GithubOperations githubOperations) {
		this.githubOperations = githubOperations;
	}

	public void update() {
		List<Project> projects = this.githubOperations.getProjects();
		Map<String, Project> updatedProject = new LinkedHashMap<>();
		Map<String, List<ProjectDocumentation>> updatedProjectDocumentation = new LinkedHashMap<>();
		Map<String, List<ProjectSupport>> updatedProjectSupport = new LinkedHashMap<>();
		Map<String, String> updatedProjectSupportPolicy = new LinkedHashMap<>();
		projects.forEach((project) -> {
			updatedProject.put(project.getSlug(), project);
			updatedProjectDocumentation.put(project.getSlug(),
					this.githubOperations.getProjectDocumentations(project.getSlug()));
			updatedProjectSupport.put(project.getSlug(), this.githubOperations.getProjectSupports(project.getSlug()));
			updatedProjectSupportPolicy.put(project.getSlug(),
					this.githubOperations.getProjectSupportPolicy(project.getSlug()));
		});
		this.project = updatedProject;
		this.documentation = updatedProjectDocumentation;
		this.support = updatedProjectSupport;
		this.supportPolicy = updatedProjectSupportPolicy;
	}

	public List<Project> getProjects() {
		return new ArrayList<>(this.project.values());
	}

	public Project getProject(String projectSlug) {
		return this.project.get(projectSlug);
	}

	public List<ProjectDocumentation> getProjectDocumentations(String projectSlug) {
		return this.documentation.get(projectSlug);
	}

	public List<ProjectSupport> getProjectSupports(String projectSlug) {
		return this.support.get(projectSlug);
	}

	public String getProjectSupportPolicy(String projectSlug) {
		return this.supportPolicy.get(projectSlug);
	}

}
