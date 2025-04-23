package io.spring.projectapi.project;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.spring.projectapi.ProjectRepository;
import io.spring.projectapi.github.ProjectSupport;

public class ProjectSupportProvider {

	private final ProjectMatrix projectMatrix;
	private final ProjectRepository projectRepository;

	private String SPRING_BOOT_SLUG = "spring-boot";

	public ProjectSupportProvider(ProjectMatrix projectMatrix, ProjectRepository projectRepository) {
		this.projectMatrix = projectMatrix;
		this.projectRepository = projectRepository;
	}

	public List<ProjectSupport> getProjectSupports(String projectId) {
		if (SPRING_BOOT_SLUG.equals(projectId)) {
			return this.projectRepository.getProjectSupports(projectId);
		}
		Map<String, String> springBootGenerations = this.projectMatrix.getSpringBootGenerationsFor(projectId);
		List<ProjectSupport> supports = new ArrayList<>();
		List<ProjectSupport> springBootSupports = this.projectRepository.getProjectSupports(SPRING_BOOT_SLUG);
		springBootSupports.forEach((s) -> {
			String branch = springBootGenerations.get(s.getBranch());
			ProjectSupport projectSupport = new ProjectSupport(branch, s.getInitialDate(), s.getOssPolicyEnd(), s.getCommercialPolicyEnd(), s.isLastMinor());
			supports.add(projectSupport);
		});
		return supports;
	}
}
