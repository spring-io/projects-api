package io.spring.projectapi.project.version;

import java.util.List;

import org.apache.maven.model.Model;

/**
 * A {@link MavenBom} for Spring Boot. Project identifiers are in the form
 * {@code spring-xyz} where {@code xyz} is the name of the project.
 * <p>
 * For instance, {@code spring-data} is the identifier of the Spring Data project. This
 * BOM also exposes any third party dependency version managed by this version of Spring
 * Boot.
 *
 * @author Stephane Nicoll
 */
public final class SpringBootBom extends AbstractMavenBom {

	SpringBootBom(MavenCoordinates coordinates, Model model) {
		super(coordinates, model);
	}

	@Override
	public Version getTargetSpringBootVersion() {
		return getCoordinates().version();
	}

	@Override
	public Version getProjectVersion(String id) {
		if ("spring-boot".equals(id)) {
			return getCoordinates().version();
		}
		return super.getProjectVersion(id);
	}

	@Override
	protected List<String> keysForProjectId(String id) {
		return List.of("%s.version".formatted(id), "%s-bom.version".formatted(id));
	}

}
