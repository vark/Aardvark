package gw.vark.testapi;

import gw.util.ProcessStarter;
import gw.util.Shell;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 */
public abstract class ForkedBuildProcess<T extends ForkedBuildProcess> {

  private final File _buildFile;
  private String _args = "";
  private List<String> _additionalClasspathElements = new ArrayList<String>();

  protected ForkedBuildProcess(File buildFile) {
    _buildFile = buildFile;
  }

  protected abstract boolean accept(String element);
  protected abstract String getMainClass();

  public T withArgs(String args) {
    _args = args;
    //noinspection unchecked
    return (T)this;
  }

  public T withAdditionalClasspathElement(String element) {
    _additionalClasspathElements.add(element);
    //noinspection unchecked
    return (T)this;
  }

  public ProcessStarter build() {
    String javaCommand = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
    String command = javaCommand
            + " -Daardvark.dev=true"
            //+ " -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
            + " -classpath " + createClasspath()
            + " " + getMainClass()
            + " -f " + _buildFile
            + " " + _args;
    System.out.println(command);
    return Shell.buildProcess(command).withCMD();
  }

  private String createClasspath() {
    // incredibly hacky way of deriving the new JVM classpath from the test classpath
    String cp = System.getProperty("java.class.path");
    StringTokenizer st = new StringTokenizer(cp, File.pathSeparator);
    StringBuilder sb = new StringBuilder();
    while (st.hasMoreTokens()) {
      String element = st.nextToken();
      if (accept(element)) {
        sb.append(element).append(File.pathSeparator);
        System.out.println("using classpath element " + element);
      }
      else {
        System.out.println("ignoring classpath element " + element);
      }
    }
    for (String element : _additionalClasspathElements) {
      sb.append(element).append(File.pathSeparator);
    }
    // not removing the last path separator char breaks the Gosu parsing - go figure...
    sb.deleteCharAt(sb.length() - 1);
    return "\"" + sb.toString() + "\"";
  }
}
