package io.spring.projectapi.project.version;

import java.util.List;

import org.apache.maven.model.Model;

public class SpringCloudBom extends AbstractMavenBom {

	private final Version springBootVersion;

	public SpringCloudBom(MavenCoordinates coordinates, Model model, Version springBootVersion) {
		super(coordinates, model);
		this.springBootVersion = springBootVersion;
	}

	@Override
	public Version getTargetSpringBootVersion() {
		return this.springBootVersion;
	}

	@Override
	public Version getProjectVersion(String id) {
		if ("spring-cloud".equals(id)) {
			return getCoordinates().version();
		}
		return super.getProjectVersion(id);
	}

	@Override
	protected List<String> keysForProjectId(String id) {
		return List.of("%s.version".formatted(id), "spring-cloud-%s.version".formatted(id));
	}

}
