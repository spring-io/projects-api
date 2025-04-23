package io.spring.projectapi.project.version;

import org.springframework.lang.Nullable;

public interface MavenBom {

	/**
	 * Return the {@linkplain MavenCoordinates coordinates} of this bom.
	 * @return the maven coordinates
	 */
	MavenCoordinates getCoordinates();

	/**
	 * Return the {@link Version} of Spring Boot that this BOM is targeting.
	 * @return the Spring Boot version
	 */
	Version getTargetSpringBootVersion();

	/**
	 * Return the dependencies that are managed by this bom.
	 * @return the managed dependencies
	 */
	Iterable<MavenCoordinates> getManagedDependencies();

	/**
	 * Return the {@link Version} of the given dependency that is managed by this bom.
	 * @param dependencyId the dependency identifier
	 * @return the version managed by this bom or {@code null} if the given dependency is
	 * not managed
	 */
	@Nullable
	Version getManagedDependencyVersion(MavenDependencyId dependencyId);

	/**
	 * Return the {@link Version} of the given project.
	 * <p>
	 * The identifier is project-specific, and implementations can adapt the input to
	 * match their conventions.
	 * @param id the identifier of the project
	 * @return the version managed by this bom or {@code null} if no such project exists
	 */
	@Nullable
	Version getProjectVersion(String id);

}
