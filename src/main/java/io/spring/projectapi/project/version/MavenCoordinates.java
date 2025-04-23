package io.spring.projectapi.project.version;

public record MavenCoordinates(MavenDependencyId dependencyId, Version version) {

	public MavenCoordinates(String groupId, String artifactId, Version version) {
		this(new MavenDependencyId(groupId, artifactId), version);
	}

	public static MavenCoordinates of(String gav) {
		String[] parts = gav.split(":");
		if (parts.length != 3) {
			throw new IllegalArgumentException(
					"GAV should be <groupId>:<artifactId>:<version>, got '%s'".formatted(gav));
		}
		return new MavenCoordinates(parts[0], parts[1], Version.from(parts[2]));
	}

	public String groupId() {
		return this.dependencyId.groupId();
	}

	public String artifactId() {
		return this.dependencyId.artifactId();
	}

	@Override
	public String toString() {
		return "%s:%s:%s".formatted(groupId(), artifactId(), version());
	}

}
