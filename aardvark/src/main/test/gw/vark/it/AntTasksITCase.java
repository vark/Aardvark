package gw.vark.it;

import org.fest.assertions.Assertions;
import org.fest.assertions.ListAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
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
    runAnt(stdOut, stdErr, "hello");
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
    runAnt(stdOut, stdErr, "gosu-hello");
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

  private void runAnt(TestOutputHandler stdOut, TestOutputHandler stdErr, String... args) {
    File buildFile = new File(_sampleprojectDir, "build.xml");
    File libDir = new File(ITUtil.getAssemblyDir(), "lib");
    String exec = new ForkedAntProcess(buildFile)
            .withArgs("-Dlib.dir=" + libDir.getAbsolutePath())
            .withArgs(args)
            .build()
            .withStdOutHandler(stdOut)
            .withStdErrHandler(stdErr)
            .exec()
            .getBuffer();
    Assertions.assertThat(exec).isNull();
  }

}
