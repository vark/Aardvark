package gw.vark.it;

import gw.util.process.ProcessRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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
  private List<String> _args = new ArrayList<String>();
  private Debug _debug;

  protected ForkedBuildProcess(File buildFile) {
    _buildFile = buildFile;
  }

  protected abstract List<File> createClasspath();

  protected abstract String getMainClass();

  public T withWorkingDirectory(File dir) {
    _workingDir = dir;
    //noinspection unchecked
    return (T) this;
  }

  public T withArgs(String... args) {
    _args.addAll(Arrays.asList(args));
    //noinspection unchecked
    return (T) this;
  }

  @SuppressWarnings("UnusedDeclaration")
  public T withDebug(Debug debug) {
    _debug = debug;
    //noinspection unchecked
    return (T) this;
  }

  public ProcessRunner build() {
    String javaCommand = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
    List<String> command = new ArrayList<String>();
    command.add(javaCommand);
    command.add("-Daardvark.dev=true");
    if (_debug != null) {
      command.add("-Xdebug");
      if (_debug == Debug.SOCKET) {
        command.add("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005");
      } else {
        command.add("-Xrunjdwp:transport=dt_shmem,server=y,suspend=y,address=aardvark");
      }
    }
    command.add("-classpath");
    command.add(join(createClasspath()));
    command.add(getMainClass());
    if (_buildFile != null) {
      command.add("-f");
      command.add(_buildFile.getAbsolutePath());
    }
    command.addAll(_args);
    System.out.println(command);
    ProcessRunner process = new ProcessRunner(command);
    process.withEnvironmentVariable("CLASSPATH", null);
    if (_workingDir != null) {
      process.withWorkingDirectory(_workingDir);
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
