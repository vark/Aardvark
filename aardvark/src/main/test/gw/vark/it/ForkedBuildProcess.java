package gw.vark.it;

import gw.util.ProcessStarter;
import gw.util.Shell;

import java.io.File;
import java.util.List;

/**
 */
public abstract class ForkedBuildProcess<T extends ForkedBuildProcess> {

  @SuppressWarnings("UnusedDeclaration")
  public enum Debug {
    SOCKET,
    SHMEM
  }
  private final File _buildFile;
  private File _workingDir;
  private String _args = "";
  private Debug _debug;

  protected ForkedBuildProcess(File buildFile) {
    _buildFile = buildFile;
  }

  protected abstract List<File> createClasspath();
  protected abstract String getMainClass();

  public T withWorkingDirectory(File dir) {
    _workingDir = dir;
    //noinspection unchecked
    return (T)this;
  }

  public T withArgs(String args) {
    _args = args;
    //noinspection unchecked
    return (T)this;
  }

  @SuppressWarnings("UnusedDeclaration")
  public T withDebug(Debug debug) {
    _debug = debug;
    //noinspection unchecked
    return (T)this;
  }

  public ProcessStarter build() {
    String javaCommand = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
    StringBuilder command = new StringBuilder(javaCommand);
    command.append(" -Daardvark.dev=true");
    if (_debug != null) {
      if (_debug == Debug.SOCKET) {
        command.append(" -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005");
      }
      else {
        command.append(" -Xdebug -Xrunjdwp:transport=dt_shmem,server=y,suspend=y,address=aardvark");
      }
    }
    command.append(" -classpath ").append(join(createClasspath()));
    command.append(" ").append(getMainClass());
    if (_buildFile != null) {
      command.append(" -f ").append(_buildFile);
    }
    command.append(" ").append(_args);
    System.out.println(command);
    ProcessStarter process = Shell.buildProcess(command.toString());
    if (_workingDir != null) {
      process.setDirectory(_workingDir);
    }
    return process;
  }

  private static String join(List<File> classpath) {
    StringBuilder sb = new StringBuilder();
    for (File element : classpath) {
      if (sb.length() > 0) {
        sb.append(File.pathSeparator);
      }
      sb.append(element.getAbsolutePath());
    }
    return sb.toString();
  }
}
