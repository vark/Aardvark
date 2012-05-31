package gw.vark.aether;

import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.codehaus.plexus.interpolation.reflection.ReflectionValueExtractor;
import org.sonatype.aether.ant.tasks.Resolve;
import org.sonatype.aether.ant.types.Dependencies;
import org.sonatype.aether.ant.types.Pom;
import org.sonatype.aether.ant.types.RemoteRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 */
@SuppressWarnings({"UnusedDeclaration"})
public class AetherUtil {

  private final Project _project;
  private List<RemoteRepository> _remoteRepos;

  public AetherUtil(Project project) {
    this(project, Collections.<RemoteRepository>emptyList());
  }

  public AetherUtil(Project project, Pom pom) {
    this(project, extractRemoteReposFromPom(pom));
  }

  public AetherUtil(Project project, List<RemoteRepository> remoteRepos) {
    _project = project;
    _remoteRepos = remoteRepos;
  }

  public AetherResolutionResult resolve(Dependencies dependencies) {
    return resolve(dependencies, null);
  }

  public AetherResolutionResult resolve(Dependencies dependencies, MavenScopeCategory scopeCategory) {
    Resolve resolveTask = newResolve();
    Path path = resolve(resolveTask, dependencies, scopeCategory);
    AetherResolutionResult resolved = new AetherResolutionResult(_project, resolveTask, path);
    return resolved;
  }

  public void resolveToDir(Dependencies dependencies, File dir, String layout) {
    resolveToDir(dependencies, null, dir, layout);
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

  private Path resolve(Resolve resolveTask, Dependencies dependencies, MavenScopeCategory scopeCategory) {
    resolveTask.addDependencies(dependencies);

    Resolve.Path path = resolveTask.createPath();
    path.setProject(_project);
    path.setRefId("tmp.path");
    if (scopeCategory != null) {
      path.setScopes(scopeCategory.getExpanded());
    }

    resolveTask.execute();
    return (Path) _project.getReference("tmp.path");
  }

  private static List<RemoteRepository> extractRemoteReposFromPom(Pom pom) {
    try {
      List<RemoteRepository> remoteRepos = new ArrayList<RemoteRepository>();
      Model model = pom.getModel(pom);
      Object o = ReflectionValueExtractor.evaluate("project.repositories", model);
      if (o != null) {
        ArrayList pomRepos = (ArrayList) o;
        for (Object pomRepoObj : pomRepos) {
          Repository pomRepo = (Repository) pomRepoObj;
          RemoteRepository remoteRepo = new RemoteRepository();
          remoteRepo.setId(pomRepo.getId());
          remoteRepo.setUrl(pomRepo.getUrl());
          remoteRepos.add(remoteRepo);
        }
      }
      return remoteRepos;
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Resolve newResolve() {
    Resolve resolveTask = initTask(new Resolve(), "resolve");
    for (RemoteRepository remoteRepo : _remoteRepos) {
      resolveTask.addRemoteRepo(remoteRepo);
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
