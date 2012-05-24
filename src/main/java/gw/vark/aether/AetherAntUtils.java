package gw.vark.aether;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.sonatype.aether.ant.tasks.Install;
import org.sonatype.aether.ant.tasks.Resolve;
import org.sonatype.aether.ant.types.Artifact;
import org.sonatype.aether.ant.types.Dependencies;
import org.sonatype.aether.ant.types.Pom;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 */
@SuppressWarnings({"UnusedDeclaration"})
public class AetherAntUtils {

  private static Project _project;
  public static void setProject(Project project) {
    _project = project;
  }

  public static FileSet resolveToFileSet(Dependencies dependencies, String scopes) {
    List<File> fileList = resolveToFileList(dependencies, scopes);
    if (fileList.size() == 0) {
      return null;
    }
    File baseDir = findCommonAncestor(fileList, fileList.get(0), 1);
    if (baseDir.isFile()) {
      baseDir = baseDir.getParentFile();
    }

    FileSet fileset = new FileSet();
    fileset.setProject(_project);
    fileset.setDir(baseDir);
    for (File file : fileList) {
      fileset.createInclude().setName(getRelativePath(baseDir, file));
    }
    return fileset;
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

  public static List<File> resolveToFileList(Dependencies dependencies, String scopes) {
    Resolve resolveTask = initTask(new Resolve(), "resolve");
    resolveTask.addDependencies(dependencies);

    FileList fileList = new FileList();
    resolveTask.addArtifactConsumer(fileList);
    fileList.setScopes(scopes);

    resolveTask.execute();
    return fileList.getFiles();
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

  private static File findCommonAncestor(List<File> paths, File val, int idx) {
    if (idx == paths.size()) {
      return val;
    }
    File selected = paths.get(idx);
    while (depth(val) > depth(selected)) {
      val = val.getParentFile();
    }
    while (depth(val) < depth(selected)) {
      selected = selected.getParentFile();
    }
    while (!val.equals(selected)) {
      val = val.getParentFile();
      selected = selected.getParentFile();
    }
    return findCommonAncestor(paths, val, idx + 1);
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

  private static class FileList extends Resolve.ArtifactConsumer {
    private List<File> files;
    private String classifier;

    public List<File> getFiles() {
      return files;
    }

    public String getClassifier() {
      return classifier;
    }

    public void setClassifier(String c) {
      classifier = c;
    }

    public void process(org.sonatype.aether.artifact.Artifact artifact) {
      if (files == null) {
        files = new ArrayList<File>();
      }
      files.add(artifact.getFile());
    }
  }

}
