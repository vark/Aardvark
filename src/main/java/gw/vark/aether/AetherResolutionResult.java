package gw.vark.aether;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.ant.AntRepoSys;
import org.sonatype.aether.ant.tasks.Resolve;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An implementation of {@link ResourceCollection} that is no more than an {@link ArrayList}
 * of {@link FileResource}s.  This can be passed to task functions which accept a <code>resources</code>
 * (<code>List&lt;ResourceCollection&gt;</code>) parameter, such as <code>gw.vark.antlibs.Ant.copy</code>.
 */
public class AetherResolutionResult implements ResourceCollection {

  private final Project _project;
  private final Resolve _resolveTask;
  private final Path _aetherResolved;

  AetherResolutionResult(Project project, Resolve resolveTask, Path aetherResolved) {
    super();
    _project = project;
    _resolveTask = resolveTask;
    _aetherResolved = aetherResolved;
  }

  @Override
  public Iterator iterator() {
    return _aetherResolved.iterator();
  }

  @Override
  public int size() {
    return _aetherResolved.size();
  }

  @Override
  public boolean isFilesystemOnly() {
    return true;
  }

  public Path asPath() {
    return _aetherResolved;
  }

  public List<File> asFileList() {
    List<File> list = new ArrayList<File>();
    for (Iterator it = iterator(); it.hasNext();) {
      FileResource resource = (FileResource) it.next();
      File file = resource.getFile();
      list.add(file);
    }
    return list;
  }

  public FileSet asFileSet() {
    File baseDir = getLocalRepoDir();

    FileSet fileset = new FileSet();
    fileset.setProject(_project);
    fileset.setDir(baseDir);

    for (File file : asFileList()) {
      String relativePath = calcRelativePath(baseDir, file);
      if (relativePath == null) {
        throw new IllegalStateException(baseDir + " is not an ancestor of " + file);
      }
      fileset.createInclude().setName(relativePath);
    }
    return fileset;
  }

  private File getLocalRepoDir() {
    RepositorySystemSession session = AntRepoSys.getInstance(_project).getSession(_resolveTask, null);
    return session.getLocalRepository().getBasedir();
  }

  private static String calcRelativePath(File baseDir, File file) {
    if (file.getPath().startsWith(baseDir.getPath())) {
      return file.getPath().substring(baseDir.getPath().length() + 1);
    }
    return null;
  }
}
