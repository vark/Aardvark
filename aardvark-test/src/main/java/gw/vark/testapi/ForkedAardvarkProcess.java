package gw.vark.testapi;

import gw.util.ProcessStarter;
import gw.util.Shell;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 */
public class ForkedAardvarkProcess {

  private File _varkFile;
  private String _args = "";
  private List<String> _additionalClasspathElements = new ArrayList<String>();

  public ForkedAardvarkProcess withVarkFile(File varkFile) {
    _varkFile = varkFile;
    return this;
  }

  public ForkedAardvarkProcess withArgs(String args) {
    _args = args;
    return this;
  }

  public ForkedAardvarkProcess withAdditionalClasspathElement(String element) {
    _additionalClasspathElements.add(element);
    return this;
  }

  private String createClasspath() {
    // incredibly hacky way of deriving the new JVM classpath from the test classpath
    String cp = System.getProperty("java.class.path");
    StringTokenizer st = new StringTokenizer(cp, File.pathSeparator);
    StringBuilder sb = new StringBuilder();
    while (st.hasMoreTokens()) {
      String element = st.nextToken();
      boolean ignore = element.endsWith("rt.jar")
              || element.contains("jre" + File.separator + "lib" + File.separator)
              || element.endsWith("test-classes")
              || element.endsWith("aardvark-test" + File.separator + "target" + File.separator + "classes")
              || element.matches(".*[/\\\\]junit-[\\d\\.]+\\.jar$")
              || element.matches(".*[/\\\\]fest-(assert|util)-[\\d\\.]+\\.jar$");
      if (!ignore) {
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

  public ProcessStarter build() {
    String javaCommand = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
    String command = javaCommand
            + " -Daardvark.dev=true"
            //+ " -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
            + " -classpath " + createClasspath()
            + " gw.lang.Gosu"
            + " -f " + _varkFile
            + " " + _args;
    System.out.println(command);
    return Shell.buildProcess(command).withCMD();
  }
}
