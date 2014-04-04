package gw.vark.codegen.maven;

import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.WorkspaceReader;
import org.sonatype.aether.repository.WorkspaceRepository;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Resolves dependencies in reactor by returning pom.xml and module basedir.
 *
 * <p>
 * "pom.xml" is returned when artifact of extension (type) "pom"
 * is requested. Module base dir itself is returned otherwise.
 * </p>
 * <p>
 * By contrast, default maven {@link WorkspaceReader} returns
 * "target/classes" and "target/test-classes" as an artifact,
 * but only if "compile" or "test-compile" phase is triggered for the build.
 * </p>
 * @author isilvestrov
 */
public class ReactorWorkspaceReader implements WorkspaceReader {
  public static final String REPOSITORY_ID = ReactorWorkspaceReader.class.getSimpleName();
  private WorkspaceRepository workspaceRepository =
          new WorkspaceRepository(REPOSITORY_ID);

  private HashMap<String, MavenProject> reactorProjects = new HashMap<>();

  public ReactorWorkspaceReader(MavenSession mavenSession) {
    for (MavenProject prj : mavenSession.getProjects()) {
      reactorProjects.put(
              ArtifactUtils.key(prj.getGroupId(), prj.getArtifactId(),
                      ArtifactUtils.toSnapshotVersion(prj.getVersion())),
              prj);
    }
  }

  public MavenProject findProject(Artifact artifact) {
    return reactorProjects.get(
            ArtifactUtils.key(artifact.getGroupId(), artifact.getArtifactId(),
                    ArtifactUtils.toSnapshotVersion(artifact.getVersion())));
  }

  @Override
  public WorkspaceRepository getRepository() {
    return workspaceRepository;
  }

  @Override
  public File findArtifact(Artifact artifact) {
    MavenProject project = findProject(artifact);
    if (project != null) {
      if (artifact.getExtension().equals("pom")) {
        return new File(project.getBasedir(), "pom.xml");
      } else {
        return project.getBasedir();
      }
    }
    return null;
  }

  @Override
  public List<String> findVersions(Artifact artifact) {
    MavenProject project = findProject(artifact);
    if (project != null) {
      return Collections.singletonList(project.getVersion());
    }
    return Collections.emptyList();
  }
}
