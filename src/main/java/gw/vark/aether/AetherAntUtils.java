package gw.vark.aether;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.resources.FileResource;
import org.sonatype.aether.ant.tasks.Install;
import org.sonatype.aether.ant.tasks.Resolve;
import org.sonatype.aether.ant.types.Artifact;
import org.sonatype.aether.ant.types.Dependencies;
import org.sonatype.aether.ant.types.Pom;

import java.io.File;
import java.util.Iterator;

/**
 */
@SuppressWarnings({"UnusedDeclaration"})
public class AetherAntUtils {

  private static Project _project;
  public static void setProject(Project project) {
    _project = project;
  }

  public static FileList resolve(Dependencies dependencies, String scopes) {
    Path path = resolveToPath(dependencies, scopes);
    FileList list = new FileList();
    for (Iterator it = path.iterator(); it.hasNext();) {
      FileResource file = (FileResource) it.next();
      list.add(file);
    }
    return list;
  }

  public static void resolveToDir(Dependencies dependencies, String scopes, File dir, String layout) {
    Resolve resolveTask = initTask(new Resolve(), "resolve");
    resolveTask.addDependencies(dependencies);

    Resolve.Files files = resolveTask.createFiles();
    files.setProject(_project);
    files.setScopes(scopes);
    files.setDir(dir);
    files.setLayout(layout);

    resolveTask.execute();
  }

  public static Path resolveToPath(Dependencies dependencies, String scopes) {
    Resolve resolveTask = initTask(new Resolve(), "resolve");
    resolveTask.addDependencies(dependencies);

    Resolve.Path path = resolveTask.createPath();
    path.setProject(_project);
    path.setRefId("tmp.path");
    path.setScopes(scopes);

    resolveTask.execute();
    return (Path) _project.getReference("tmp.path");
  }

  public static void install(Pom pom) {
    install(pom, null);
  }

  public static void install(Pom pom, Artifact artifact) {
    Install installTask = initTask(new Install(), "install");
    installTask.addPom(pom);
    if (artifact != null) {
      installTask.addArtifact(artifact);
    }
    installTask.execute();
  }

  public static Pom pom(File file) {
    Pom pomTask = initTask(new Pom(), "pom");
    pomTask.setFile(file);
    pomTask.execute();
    return pomTask;
  }


  private static int depth(File file) {
    if (file.getParent() == null) {
      return 0;
    }
    return depth(file.getParentFile()) + 1;
  }

  private static String getRelativePath(File baseDir, File file) {
    if (file.getPath().startsWith(baseDir.getPath())) {
      return file.getPath().substring(baseDir.getPath().length() + 1);
    }
    return null;
  }

  private static <T extends Task> T initTask(T task, String name) {
    task.setProject(_project);
    task.setTaskName(name);
    task.init();
    return task;
  }
}
