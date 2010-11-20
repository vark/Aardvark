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

  public void testTestprojectEchoHello() {
    TestOutputHandler stdOut = runAardvark("echo-hello");
    assertOutputMatches(stdOut,
            "e:aardvark.dev is set to true - using locally compiled classes",
            "e:Buildfile: " + _testprojectDir + File.separator + "build.vark",
            "m:\\[\\d\\d:\\d\\d:\\d\\d\\] Done parsing Aardvark buildfile in \\d+ ms",
            "e:",
            "e:", // TODO - gosu bug
            "e:", // TODO - gosu bug
            "e:echo-hello:",
            "e:     [echo] Hello World",
            "e:",
            "e:BUILD SUCCESSFUL",
            "m:Total time: \\d+ seconds?"
            );
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

  private static ListAssert assertThatOutput(TestOutputHandler handler) {
    return assertThat(handler._lines).as("Aardvark output");
  }

  private static void assertOutputMatches(TestOutputHandler stdOut, String... lines) {
    assertThatOutput(stdOut).hasSize(lines.length);
    for (int i = 0; i < lines.length; i++) {
      if (lines[i].startsWith("e:")) {
        assertThat(stdOut._lines.get(i)).isEqualTo(lines[i].substring(2));
      }
      else if (lines[i].startsWith("m:")) {
        assertThat(stdOut._lines.get(i)).matches(lines[i].substring(2));
      }
      else {
        throw new IllegalArgumentException("line must start with e: or m:");
      }
    }
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
