package gw.vark.it;

import org.fest.assertions.Assertions;
import org.fest.assertions.ListAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by IntelliJ IDEA.
 * User: bchang
 * Date: 2/13/12
 * Time: 5:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class AntTasksITCase {

  private File _sampleprojectDir;

  @Before
  public void setUp() throws Exception {
    File home = ITUtil.getProjectRoot();
    _sampleprojectDir = new File(home, "test-ant-project");
    clean();
  }

  @After
  public void tearDown() throws Exception {
    clean();
  }

  @Test
  public void testHello() {
    TestOutputHandler stdOut = new TestOutputHandler("stdout");
    TestOutputHandler stdErr = new TestOutputHandler("stderr");
    runAnt("hello", stdOut, stdErr);
    assertThatOutput(stdErr).isEmpty();
    assertThatOutput(stdOut).containsSequence(
            "",
            "hello:",
            "     [echo] hello",
            "",
            "BUILD SUCCESSFUL"
    );
  }

  @Test
  public void testGosuHello() {
    TestOutputHandler stdOut = new TestOutputHandler("stdout");
    TestOutputHandler stdErr = new TestOutputHandler("stderr");
    runAnt("gosu-hello", stdOut, stdErr);
    assertThatOutput(stdErr).isEmpty();
    assertThatOutput(stdOut).containsSequence(
            "",
            "init-gosu:",
            "",
            "gosu-hello:",
            "     [gosu] hello",
            "",
            "BUILD SUCCESSFUL"
    );
  }

  private void clean() {
  }

  private static ListAssert assertThatOutput(TestOutputHandler handler) {
    return Assertions.assertThat(handler._lines).as("Aardvark output");
  }

  private void runAnt(String args, TestOutputHandler stdOut, TestOutputHandler stdErr) {
    File buildFile = new File(_sampleprojectDir, "build.xml");
    StringBuilder props = new StringBuilder();
    File libDir = new File(ITUtil.getAssemblyDir(), "lib");
    try {
      props.append("-Dgosu.launcher=").append(ITUtil.findFile(libDir, "gosu-launcher-\\d.*\\.jar").getPath()).append(" ");
      props.append("-Dgosu.core.api=").append(ITUtil.findFile(libDir, "gosu-core-api-\\d.*\\.jar").getPath()).append(" ");
      props.append("-Dgosu.core=").append(ITUtil.findFile(libDir, "gosu-core-\\d.*\\.jar").getPath()).append(" ");
    }
    catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
    String exec = new ForkedAntProcess(buildFile)
            .withArgs(props.toString() + args)
            .build()
            .withStdOutHandler(stdOut)
            .withStdErrHandler(stdErr)
            .doNotThrowOnNonZeroReturnVal()
            .exec();
    Assertions.assertThat(exec).isEmpty();
  }

}
