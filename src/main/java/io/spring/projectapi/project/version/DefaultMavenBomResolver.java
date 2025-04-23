/*
 * Copyright 2012-2024 the original author or authors.
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

package io.spring.projectapi.project.version;

import java.nio.file.Path;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.building.DefaultModelBuilder;
import org.apache.maven.model.building.DefaultModelBuilderFactory;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectModelResolver;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.RequestTrace;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.impl.RemoteRepositoryManager;
import org.eclipse.aether.internal.impl.DefaultRepositorySystem;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.spi.locator.ServiceLocator;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.repository.SimpleArtifactDescriptorPolicy;

/**
 * A {@link MavenBomResolver} that resolves versions using Maven Resolver. Maven's default
 * {@link LocalRepositoryManager} implementation is not thread-safe. To avoid corruption
 * of the local repository, interaction with the {@link RepositorySystem} is
 * single-threaded.
 *
 * @author Andy Wilkinson
 * @author Stephane Nicoll
 */
@SuppressWarnings("deprecation")
class DefaultMavenBomResolver implements MavenBomResolver {

	private static final Log logger = LogFactory.getLog(DefaultMavenBomResolver.class);

	private final List<RemoteRepository> remoteRepositories;

	private final Object monitor = new Object();

	private final RepositorySystemSession repositorySystemSession;

	private final RemoteRepositoryManager remoteRepositoryManager;

	private final RepositorySystem repositorySystem;

	DefaultMavenBomResolver(List<RemoteRepository> remoteRepositories, Path cacheLocation) {
		this.remoteRepositories = remoteRepositories;
		ServiceLocator serviceLocator = createServiceLocator();
		DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
		session.setArtifactDescriptorPolicy(new SimpleArtifactDescriptorPolicy(false, false));
		LocalRepository localRepository = new LocalRepository(cacheLocation.toFile());
		this.repositorySystem = serviceLocator.getService(RepositorySystem.class);
		session.setLocalRepositoryManager(this.repositorySystem.newLocalRepositoryManager(session, localRepository));
		session.setUserProperties(System.getProperties());
		session.setReadOnly();
		this.repositorySystemSession = session;
		this.remoteRepositoryManager = serviceLocator.getService(RemoteRepositoryManager.class);
	}

	@Override
	public SpringBootBom resolveSpringBootBom(Version springBootVersion) {
		MavenCoordinates bomCoordinates = new MavenCoordinates("org.springframework.boot", "spring-boot-dependencies",
				springBootVersion);
		return new SpringBootBom(bomCoordinates, buildEffectiveModel(bomCoordinates));
	}

	@Override
	public SpringCloudBom resolveSpringCloudBom(Version springCloudVersion) {
		MavenCoordinates bomCoordinates = new MavenCoordinates("org.springframework.cloud", "spring-cloud-dependencies",
				springCloudVersion);
		Model model = buildEffectiveModel(bomCoordinates);
		Version springBootVersion = resolveSpringBootParentVersion(springCloudVersion);
		return new SpringCloudBom(bomCoordinates, model, springBootVersion);
	}

	private Model buildEffectiveModel(MavenCoordinates pom) {
		if (logger.isDebugEnabled()) {
			logger.debug("Building effective model of '%s'".formatted(pom));
		}
		try {
			ArtifactResult bom = resolvePom(pom);
			RequestTrace requestTrace = new RequestTrace(null);
			ModelResolver modelResolver = new ProjectModelResolver(this.repositorySystemSession, requestTrace,
					this.repositorySystem, this.remoteRepositoryManager, remoteRepositories,
					ProjectBuildingRequest.RepositoryMerging.POM_DOMINANT, null);
			DefaultModelBuildingRequest modelBuildingRequest = new DefaultModelBuildingRequest();
			modelBuildingRequest.setSystemProperties(System.getProperties());
			modelBuildingRequest.setPomFile(bom.getArtifact().getFile());
			modelBuildingRequest.setModelResolver(modelResolver);
			DefaultModelBuilder modelBuilder = new DefaultModelBuilderFactory().newInstance();
			return modelBuilder.build(modelBuildingRequest).getEffectiveModel();
		}
		catch (ModelBuildingException ex) {
			Model model = ex.getModel();
			if (model != null) {
				logger.warn("Model for '" + pom + "' is incomplete: " + ex.getProblems());
				return model;
			}
			throw new IllegalStateException("Model for '" + pom + "' could not be built", ex);
		}
	}

	private Version resolveSpringBootParentVersion(Version springCloudVersion) {
		Model model = buildEffectiveModel(
				new MavenCoordinates("org.springframework.cloud", "spring-cloud-starter-parent", springCloudVersion));
		Parent parent = model.getParent();
		if (parent != null && parent.getGroupId().equals("org.springframework.boot")
				&& parent.getArtifactId().equals("spring-boot-starter-parent")) {
			return Version.from(parent.getVersion());
		}
		throw new IllegalStateException("Failed to resolve Spring Boot version for '" + springCloudVersion + "'");
	}

	private ArtifactResult resolvePom(MavenCoordinates pom) {
		synchronized (this.monitor) {
			try {
				return this.repositorySystem.resolveArtifact(this.repositorySystemSession,
						new ArtifactRequest(
								new DefaultArtifact(pom.groupId(), pom.artifactId(), "pom", pom.version().getId()),
								remoteRepositories, null));
			}
			catch (ArtifactResolutionException ex) {
				throw new IllegalArgumentException("Failed to resolve '" + pom + "'", ex);
			}

		}
	}

	private static ServiceLocator createServiceLocator() {
		DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
		locator.addService(RepositorySystem.class, DefaultRepositorySystem.class);
		locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
		locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
		return locator;
	}

}
