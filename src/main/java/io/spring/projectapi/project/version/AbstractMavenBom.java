package io.spring.projectapi.project.version;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.maven.model.Model;

import org.springframework.lang.Nullable;

public abstract class AbstractMavenBom implements MavenBom {

	private final MavenCoordinates coordinates;

	private final Map<MavenDependencyId, MavenCoordinates> managedDependencies;

	private final Properties properties;

	protected AbstractMavenBom(MavenCoordinates coordinates, Model model) {
		this.coordinates = coordinates;
		this.managedDependencies = model.getDependencyManagement()
			.getDependencies()
			.stream()
			.collect(Collectors.toMap(key -> new MavenDependencyId(key.getGroupId(), key.getArtifactId()),
					value -> new MavenCoordinates(value.getGroupId(), value.getArtifactId(),
							Version.from(value.getVersion())),
					(a, b) -> a));
		this.properties = model.getProperties();
	}

	@Override
	public MavenCoordinates getCoordinates() {
		return this.coordinates;
	}

	@Override
	public Iterable<MavenCoordinates> getManagedDependencies() {
		return this.managedDependencies.values();
	}

	@Override
	public Version getManagedDependencyVersion(MavenDependencyId dependencyId) {
		MavenCoordinates coordinates = this.managedDependencies.get(dependencyId);
		return (coordinates != null) ? coordinates.version() : null;
	}

	@Override
	@Nullable
	public Version getProjectVersion(String id) {
		for (String candidate : keysForProjectId(id)) {
			if (this.properties.containsKey(candidate)) {
				return Version.from(this.properties.getProperty(candidate));
			}
		}
		return null;
	}

	protected abstract List<String> keysForProjectId(String id);

}
