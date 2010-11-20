package gw.vark;

import gw.util.ProcessStarter;
import gw.util.Shell;
import gw.vark.testapi.AardvarkTestCase;
import org.apache.tools.ant.launch.Locator;
import org.fest.assertions.ListAssert;

import java.io.File;
import java.util.ArrayList;

/**
 */
public class AardvarkProcessTest extends AardvarkTestCase {

  private File _home;
  private File _testprojectDir;

  public AardvarkProcessTest() {
    super();
  }

  @Override
  protected void setUp() throws Exception {
    File classSource = Locator.getClassSource(getClass());
    _home = getHome(classSource.isDirectory() ? classSource : classSource.getParentFile()).getCanonicalFile();
    _testprojectDir = new File(_home, "testproject");
  }

  public void testTestprojectRun() {
    TestOutputHandler stdOut = runAardvark("clean run");
    assertThatOutput(stdOut).containsSequence(
            "run:",
            "     [java] Hello World"
    );
    assertThatOutput(stdOut).contains("BUILD SUCCESSFUL");
    assertTrue(new File(_testprojectDir, "build/dist/testproject.jar").exists());

    stdOut = runAardvark("clean");
    assertThatOutput(stdOut).contains("BUILD SUCCESSFUL");
    assertFalse(new File(_testprojectDir, "build/dist/testproject.jar").exists());
  }

  private TestOutputHandler runAardvark(String args) {
    File varkFile = new File(_testprojectDir, "build.vark");
    return runAardvark(varkFile, args);
  }

  private TestOutputHandler runAardvark(File varkFile, String args) {
    String javaCommand = System.getProperty("java.home") + "/bin/java";
    String classpathString = _home + File.separator + "launcher" + File.separator + "classes"
            + File.pathSeparator
            + _home + File.separator + "lib" + File.separator + "ant" + File.separator + "ant-launcher.jar";
    String command = javaCommand
            + " -Daardvark.dev=true"
            //+ " -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
            + " -cp " + classpathString + " gw.vark.launch.Launcher"
            + " -f " + varkFile
            + " " + args;
    System.out.println(command);
    TestOutputHandler stdOut = new TestOutputHandler();
    TestOutputHandler stdErr = new TestOutputHandler();
    String exec = Shell.buildProcess(command)
            .withStdOutHandler(stdOut)
            .withStdErrHandler(stdErr)
            .withCMD()
            .exec();
    assertThat(exec).isEmpty();
    assertThatOutput(stdErr).isEmpty();
    return stdOut;
  }

  private ListAssert assertThatOutput(TestOutputHandler handler) {
    return assertThat(handler._lines).as("Aardvark output");
  }

  private static File getHome(File dir) {
    if (new File(dir, "lib/gosu").exists()) {
      return dir;
    }
    return getHome(dir.getParentFile());
  }

  private static class TestOutputHandler implements ProcessStarter.OutputHandler {
    ArrayList<String> _lines = new ArrayList<String>();
    @Override
    public void handleLine(String line) {
      _lines.add(line);
      System.out.println(line);
    }
  }

}
