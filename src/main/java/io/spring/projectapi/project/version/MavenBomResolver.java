package io.spring.projectapi.project.version;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.aether.repository.RemoteRepository;

public interface MavenBomResolver {

	/**
	 * Maven Central repository.
	 */
	RemoteRepository MAVEN_CENTRAL = new RemoteRepository.Builder("central", "default",
			"https://repo1.maven.org/maven2")
		.build();

	/**
	 * Create a new {@link Builder} that uses Maven Central as a remote repository.
	 * @return a builder
	 */
	static Builder withDefaults() {
		return new Builder().withRemoteRepository(MAVEN_CENTRAL);
	}

	/**
	 * Resolve the {@link SpringBootBom} for the given Spring Boot version.
	 * @param springBootVersion the Spring Boot version
	 * @return the BOM for the given Spring Boot version
	 * @throws IllegalArgumentException if the Spring Boot version could not be resolved
	 */
	SpringBootBom resolveSpringBootBom(Version springBootVersion);

	/**
	 * Resolve the {@link SpringCloudBom} for the given Spring Cloud version.
	 * @param springCloudVersion the Spring Cloud version
	 * @return the BOM for the given Spring Cloud version
	 * @throws IllegalArgumentException if the Spring Cloud version could not be resolved
	 */
	SpringCloudBom resolveSpringCloudBom(Version springCloudVersion);

	class Builder {

		private Path cacheLocation;

		private final List<RemoteRepository> remoteRepositories = new ArrayList<>();

		/**
		 * Specify the location to use for caching resolved versions. If not specified, a
		 * temporary location will be used.
		 * @param cacheLocation the cache location
		 * @return {@code this}
		 */
		public Builder withCacheLocation(Path cacheLocation) {
			this.cacheLocation = cacheLocation;
			return this;
		}

		/**
		 * Add a remote repository to use for resolving versions.
		 * @param remoteRepository the remote repository to use
		 * @return {@code this}
		 */
		public Builder withRemoteRepository(RemoteRepository remoteRepository) {
			this.remoteRepositories.add(remoteRepository);
			return this;
		}

		/**
		 * Build a {@link MavenBomResolver} with the current configuration.
		 * @return a Maven BOM resolver
		 */
		public MavenBomResolver build() {
			List<RemoteRepository> repositories = (!this.remoteRepositories.isEmpty())
					? new ArrayList<>(this.remoteRepositories) : List.of(MAVEN_CENTRAL);
			return new DefaultMavenBomResolver(repositories, getOrCreateCacheLocation());
		}

		private Path getOrCreateCacheLocation() {
			try {
				return (this.cacheLocation != null) ? this.cacheLocation : Files.createTempDirectory("cache");
			}
			catch (IOException ex) {
				throw new IllegalStateException("Failed to create cache location", ex);
			}
		}

	}

}
