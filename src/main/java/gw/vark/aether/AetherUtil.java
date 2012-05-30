package gw.vark.aether;

import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.resources.FileResource;
import org.codehaus.plexus.interpolation.reflection.ReflectionValueExtractor;
import org.sonatype.aether.ant.tasks.Install;
import org.sonatype.aether.ant.tasks.Resolve;
import org.sonatype.aether.ant.types.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

/**
 */
@SuppressWarnings({"UnusedDeclaration"})
public class AetherUtil {

  private final Project _project;
  private final Pom _pom;

  public AetherUtil(Project project) {
    this(project, null);
  }

  public AetherUtil(Project project, Pom pom) {
    _project = project;
    _pom = pom;
  }

  public FileList resolve(Dependencies dependencies, MavenScopeCategory scopeCategory) {
    Resolve resolveTask = newResolve();
    Path path = resolveToPath(resolveTask, dependencies, scopeCategory);
    FileList list = new FileList(_project, resolveTask);
    for (Iterator it = path.iterator(); it.hasNext();) {
      FileResource file = (FileResource) it.next();
      list.add(file);
    }
    return list;
  }

  public void resolveToDir(Dependencies dependencies, MavenScopeCategory scopeCategory, File dir, String layout) {
    Resolve resolveTask = newResolve();
    resolveTask.addDependencies(dependencies);

    Resolve.Files files = resolveTask.createFiles();
    files.setProject(_project);
    files.setScopes(scopeCategory.getExpanded());
    files.setDir(dir);
    files.setLayout(layout);

    resolveTask.execute();
  }

  public Path resolveToPath(Dependencies dependencies, MavenScopeCategory scopeCategory) {
    Resolve resolveTask = newResolve();
    return resolveToPath(resolveTask, dependencies, scopeCategory);
  }

  private Path resolveToPath(Resolve resolveTask, Dependencies dependencies, MavenScopeCategory scopeCategory) {
    resolveTask.addDependencies(dependencies);

    Resolve.Path path = resolveTask.createPath();
    path.setProject(_project);
    path.setRefId("tmp.path");
    path.setScopes(scopeCategory.getExpanded());

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

  private Resolve newResolve() {
    Resolve resolveTask = initTask(new Resolve(), "resolve");
    if (_pom != null) {
      Model model = _pom.getModel(_pom);
      try {
        Object o = ReflectionValueExtractor.evaluate("project.repositories", model);
        if (o != null) {
          ArrayList pomRepos = (ArrayList) o;
          for (Object pomRepoObj : pomRepos) {
            Repository pomRepo = (Repository) pomRepoObj;
            RemoteRepository remoteRepo = new RemoteRepository();
            remoteRepo.setId(pomRepo.getId());
            remoteRepo.setUrl(pomRepo.getUrl());
            resolveTask.addRemoteRepo(remoteRepo);
          }
        }
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return resolveTask;
  }

  private <T extends Task> T initTask(T task, String name) {
    task.setProject(_project);
    task.setTaskName(name);
    task.init();
    return task;
  }
}
