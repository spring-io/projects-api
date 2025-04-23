package io.spring.projectapi.project;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.spring.projectapi.ProjectRepository;
import io.spring.projectapi.github.ProjectDocumentation;
import io.spring.projectapi.github.ProjectDocumentation.Status;
import io.spring.projectapi.github.ProjectSupport;
import io.spring.projectapi.project.version.MavenBom;
import io.spring.projectapi.project.version.MavenBomResolver;
import io.spring.projectapi.project.version.Version;

public class ProjectMatrix {

	private final List<MavenBom> boms = new ArrayList<>();

	public ProjectMatrix(ProjectRepository repository) {
		List<ProjectDocumentation> projectSupports = repository.getProjectDocumentations("spring-boot");
		List<String> gaList = projectSupports.stream().filter((s) -> s.getStatus().equals(Status.GENERAL_AVAILABILITY)).map(ProjectDocumentation::getVersion).toList();
		MavenBomResolver resolver = MavenBomResolver.withDefaults().withCacheLocation(Paths.get("build/repo")).build();
		this.boms.addAll(gaList.stream()
				.map(Version::from)
				.map(resolver::resolveSpringBootBom)
				.toList());
		this.boms.addAll(Stream.of("2021.0.9", "2022.0.5", "2023.0.5", "2024.0.1")
				.map(Version::from)
				.map(resolver::resolveSpringCloudBom)
				.toList());
	}

	/**
	 * Return a matrix of versions for the given project.
	 * @param projectId the id of the project
	 * @return a map, keyed by the project version that this instance knows about where
	 * the value is the target Spring Boot version
	 */
	public Map<Version, Version> getVersionMappingsFor(String projectId) {
		Map<Version, Version> mapping = new LinkedHashMap<>();
		this.boms.forEach(bom -> {
			Version projectVersion = bom.getProjectVersion(projectId);
			if (projectVersion != null) {
				mapping.put(projectVersion, bom.getTargetSpringBootVersion());
			}
		});
		return mapping;
	}

	/**
	 * Return a matrix of generations for the given project.
	 * @param projectId the id of the project
	 * @return a map, keyed by the Spring Boot generation that this instance knows about where
	 * the value is the project generation
	 */
	public Map<String, String> getSpringBootGenerationsFor(String projectId) {
		Map<String, String> mapping = new LinkedHashMap<>();
		this.boms.forEach(bom -> {
			Version projectVersion = bom.getProjectVersion(projectId);
			if (projectVersion != null) {
				mapping.put(bom.getTargetSpringBootVersion().toGeneration(), projectVersion.toGeneration());
			}
		});
		return mapping;
	}

}
