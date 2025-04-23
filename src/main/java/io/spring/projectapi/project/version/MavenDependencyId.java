package io.spring.projectapi.project.version;

public record MavenDependencyId(String groupId, String artifactId) {

	public static MavenDependencyId of(String gav) {
		String[] parts = gav.split(":");
		if (parts.length != 2) {
			throw new IllegalArgumentException(
					"Dependency identifier should be <groupId>:<artifactId>, got '%s'".formatted(gav));
		}
		return new MavenDependencyId(parts[0], parts[1]);
	}

	@Override
	public String toString() {
		return "%s:%s".formatted(this.groupId, this.artifactId);
	}

}
