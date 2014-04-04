package gw.vark.codegen.maven;

import gw.vark.codegen.AntlibCodegen;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.repository.WorkspaceReader;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.resolution.DependencyResult;
import org.sonatype.aether.util.DefaultRepositorySystemSession;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

/**
 */
@Mojo(name = "generate-antlibs",
        threadSafe = true,
        defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class GenerateAntlibs extends AbstractMojo {

  @Parameter(defaultValue = "${project.basedir}/generated")
  private File generatedOutput;

  @Parameter(property = "project", required = true, readonly = true)
  private MavenProject project;

  @Parameter(property = "session", required = true, readonly = true)
  private MavenSession mavenSession;

  @Parameter(property = "antlibgen.skip")
  private boolean skip;

  @Component
  private RepositorySystem repositorySystem;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (skip) {
      getLog().info("Skipping Vark antlibs generation");
      return;
    }

    try {
      doExecute();
    } catch (IOException e) {
      throw new MojoFailureException("Failed to generate Vark enums", e);
    }

    Resource resource = new Resource();
    resource.setDirectory(generatedOutput.getAbsolutePath());
    project.addResource(resource);
  }

  private void doExecute() throws IOException, MojoExecutionException, MojoFailureException {
    ReactorWorkspaceReader workspace = new ReactorWorkspaceReader(mavenSession);
    MavenProject currentProject = mavenSession.getCurrentProject();

    Artifact target = new DefaultArtifact(
            currentProject.getGroupId(),
            currentProject.getArtifactId(),
            "jar",
            currentProject.getVersion());
    final DependencyResult dependencyResurt = resolveDependencies(workspace, target, "compile");

    final AntlibCodegen.Builder codegen = new AntlibCodegen.Builder();
    codegen.withOutput(generatedOutput.toPath());

    Map<Path, FileSystem> jars = new HashMap<>();
    for (ArtifactResult result : dependencyResurt.getArtifactResults()) {
      Artifact artifact = result.getArtifact();
      Path library = artifact.getFile().toPath();
      if (library.getFileName().toString().endsWith(".jar")) {
        FileSystem jarFS = jars.get(library);
        if (jarFS == null) {
          jarFS = FileSystems.newFileSystem(library, null);
          jars.put(library, jarFS);
        }
        codegen.withClassPath(jarFS.getPath("/"));
      } else {
        codegen.withClassPath(library);
      }
    }

    for (String sourceRoot : project.getCompileSourceRoots()) {
      scanSourceTree(codegen, sourceRoot);
    }
    for (Resource resourceRoot : project.getResources()) {
      scanSourceTree(codegen, resourceRoot.getDirectory());
    }

    codegen.build().generate();


    for (FileSystem fs : jars.values()) {
      try {
        fs.close();
      } catch (IOException e) {
        // Just ignore
      }
    }
  }

  private void scanSourceTree(final AntlibCodegen.Builder codegen, String sourceRoot) throws IOException {
    Path path = project.getBasedir().toPath().resolve(sourceRoot);
    if (!Files.isDirectory(path)) {
      return;
    }
    Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (file.getFileName().toString().endsWith(".antlib")) {
          codegen.withAntlib(file);
        }
        return super.visitFile(file, attrs);
      }
    });
  }

  /**
   * Resolves dependencies using Aether. This pretty much duplicates behavior of Maven3.
   * <p/>
   * <p>
   * Main hint here is that it uses custom {@link ReactorWorkspaceReader}.
   * This implies that module root directories are resolved as artifact files for
   * reactor projects.
   * </p>
   * <p>
   * From implementation standpoint it is critical that {@code RepositorySystemSession}
   * is created by cloning one from {@code MavenSession} and {@code CollectRequest}
   * is equipped with list of {@code RemoteRepository}s got from {@code MavenSession} as well.
   * This allows to avoid making custom instantiation and configuration of these objects.
   * </p>
   */
  private DependencyResult resolveDependencies(
          WorkspaceReader workspace,
          Artifact target,
          String scope) {

    DefaultRepositorySystemSession sess = new DefaultRepositorySystemSession(mavenSession.getRepositorySession());
    sess.setWorkspaceReader(workspace);

    Dependency dependency = new Dependency(target, scope);
    CollectRequest collectRequest =
            new CollectRequest(dependency,
                    mavenSession.getCurrentProject().getRemoteProjectRepositories());
    try {
      return repositorySystem.resolveDependencies(sess, new DependencyRequest(collectRequest, null));
    } catch (DependencyResolutionException e) {
      throw new RuntimeException(e);
    }
  }
}
