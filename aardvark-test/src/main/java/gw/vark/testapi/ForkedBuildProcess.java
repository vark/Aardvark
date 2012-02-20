package gw.vark.testapi;

import gw.util.ProcessStarter;
import gw.util.Shell;
import org.apache.tools.ant.launch.Locator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

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

  protected abstract boolean accept(String element);
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
    command.append(" -classpath ").append(createClasspath());
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

  private String createClasspath() {
    // incredibly hacky way of deriving the new JVM classpath from the test classpath
    String cp = System.getProperty("java.class.path");
    StringTokenizer st = new StringTokenizer(cp, File.pathSeparator);
    List<String> classpath = new ArrayList<String>();
    while (st.hasMoreTokens()) {
      String element = st.nextToken();
      if (accept(element)) {
        classpath.add(element);
        //System.out.println("using classpath element " + element);
      }
      else {
        //System.out.println("ignoring classpath element " + element);
      }
    }
    File toolsJar = Locator.getToolsJar();
    if (toolsJar != null) {
      classpath.add(toolsJar.getPath());
    }
    return join(classpath);
  }

  private static String join(List<String> classpath) {
    StringBuilder sb = new StringBuilder();
    for (String element : classpath) {
      if (sb.length() > 0) {
        sb.append(File.pathSeparator);
      }
      sb.append(element);
    }
    return sb.toString();
  }
}
