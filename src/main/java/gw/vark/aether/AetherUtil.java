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
public class AetherUtil {

  private final Project _project;

  public AetherUtil(Project project) {
    _project = project;
  }

  public FileList resolve(Dependencies dependencies, String scopes) {
    Path path = resolveToPath(dependencies, scopes);
    FileList list = new FileList();
    for (Iterator it = path.iterator(); it.hasNext();) {
      FileResource file = (FileResource) it.next();
      list.add(file);
    }
    return list;
  }

  public void resolveToDir(Dependencies dependencies, String scopes, File dir, String layout) {
    Resolve resolveTask = initTask(new Resolve(), "resolve");
    resolveTask.addDependencies(dependencies);

    Resolve.Files files = resolveTask.createFiles();
    files.setProject(_project);
    files.setScopes(scopes);
    files.setDir(dir);
    files.setLayout(layout);

    resolveTask.execute();
  }

  public Path resolveToPath(Dependencies dependencies, String scopes) {
    Resolve resolveTask = initTask(new Resolve(), "resolve");
    resolveTask.addDependencies(dependencies);

    Resolve.Path path = resolveTask.createPath();
    path.setProject(_project);
    path.setRefId("tmp.path");
    path.setScopes(scopes);

    resolveTask.execute();
    return (Path) _project.getReference("tmp.path");
  }

  public void install(Pom pom) {
    install(pom, null);
  }

  public void install(Pom pom, Artifact artifact) {
    Install installTask = initTask(new Install(), "install");
    installTask.addPom(pom);
    if (artifact != null) {
      installTask.addArtifact(artifact);
    }
    installTask.execute();
  }

  private <T extends Task> T initTask(T task, String name) {
    task.setProject(_project);
    task.setTaskName(name);
    task.init();
    return task;
  }
}
