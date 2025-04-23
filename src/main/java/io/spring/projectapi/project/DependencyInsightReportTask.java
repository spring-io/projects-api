package io.spring.projectapi.project;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.spring.projectapi.project.version.MavenBom;
import io.spring.projectapi.project.version.MavenDependencyId;
import io.spring.projectapi.project.version.Version;

public class DependencyInsightReportTask {

	private final List<? extends MavenBom> boms;

	public DependencyInsightReportTask(List<? extends MavenBom> boms) {
		this.boms = boms;
	}

	public Report getManagedVersions(String projectId, Version version, Iterable<MavenDependencyId> dependencies) {
		MavenBom bom = findBomFor(projectId, version);
		Version projectVersion = bom.getProjectVersion(projectId);
		return new Report(projectVersion, getManagedVersions(bom, dependencies));
	}

	public Map<MavenDependencyId, Version> getManagedVersions(MavenBom bom, Iterable<MavenDependencyId> dependencies) {
		Map<MavenDependencyId, Version> mapping = new LinkedHashMap<>();
		for (MavenDependencyId dependency : dependencies) {
			Version managedVersion = bom.getManagedDependencyVersion(dependency);
			if (managedVersion != null) {
				mapping.put(dependency, managedVersion);
			}
		}
		return mapping;
	}

	private MavenBom findBomFor(String projectId, Version version) {
		for (MavenBom bom : boms) {
			Version managedVersion = bom.getProjectVersion(projectId);
			if (managedVersion != null && managedVersion.toGeneration().equals(version.toGeneration())) {
				return bom;
			}
		}
		throw new IllegalArgumentException("No information found for %s %s".formatted(projectId, version));
	}

	public record Report(Version projectVersion, Map<MavenDependencyId, Version> managedDependencies) {

	}

}
