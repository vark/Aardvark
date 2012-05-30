package gw.vark.aether;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.ant.AntRepoSys;
import org.sonatype.aether.ant.tasks.Resolve;

import java.io.File;
import java.util.ArrayList;

/**
 * An implementation of {@link ResourceCollection} that is no more than an {@link ArrayList}
 * of {@link FileResource}s.  This can be passed to task functions which accept a <code>resources</code>
 * (<code>List&lt;ResourceCollection&gt;</code>) parameter, such as <code>gw.vark.antlibs.Ant.copy</code>.
 */
public class AetherResolutionResult extends ArrayList<FileResource> implements ResourceCollection {

  private final Project _project;
  private final Resolve _resolveTask;

  AetherResolutionResult(Project project, Resolve resolveTask) {
    super();
    _project = project;
    _resolveTask = resolveTask;
  }

  @Override
  public boolean isFilesystemOnly() {
    return true;
  }

  public FileSet asFileSet() {
    RepositorySystemSession session = AntRepoSys.getInstance(_project).getSession(_resolveTask, null);
    File baseDir = session.getLocalRepository().getBasedir();

    FileSet fileset = new FileSet();
    fileset.setProject(_project);
    fileset.setDir(baseDir);
    for (FileResource resource : this) {
      File file = resource.getFile();
      String relativePath = calcRelativePath(baseDir, file);
      if (relativePath == null) {
        throw new IllegalStateException(baseDir + " is not an ancestor of " + file);
      }
      fileset.createInclude().setName(relativePath);
    }
    return fileset;
  }

  private static String calcRelativePath(File baseDir, File file) {
    if (file.getPath().startsWith(baseDir.getPath())) {
      return file.getPath().substring(baseDir.getPath().length() + 1);
    }
    return null;
  }
}
