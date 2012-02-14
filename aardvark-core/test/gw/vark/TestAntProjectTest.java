package gw.vark;

import gw.vark.testapi.AardvarkAssertions;
import gw.vark.testapi.ForkedAardvarkProcess;
import gw.vark.testapi.ForkedAntProcess;
import gw.vark.testapi.TestUtil;
import org.apache.tools.ant.types.Assertions;
import org.fest.assertions.ListAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: bchang
 * Date: 2/13/12
 * Time: 5:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestAntProjectTest {

  private File _sampleprojectDir;

  @Before
  public void setUp() throws Exception {
    File home = TestUtil.getHome(getClass());
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

  private void clean() {
  }

  private static ListAssert assertThatOutput(TestOutputHandler handler) {
    return AardvarkAssertions.assertThat(handler._lines).as("Aardvark output");
  }

  private void runAnt(String args, TestOutputHandler stdOut, TestOutputHandler stdErr) {
    File varkFile = new File(_sampleprojectDir, "build.xml");
    runAardvark(varkFile, args, stdOut, stdErr);
  }

  private void runAardvark(File buildFile, String args, TestOutputHandler stdOut, TestOutputHandler stdErr) {
    String exec = new ForkedAntProcess(buildFile)
            .withArgs(args)
            .build()
            .withStdOutHandler(stdOut)
            .withStdErrHandler(stdErr)
            .doNotThrowOnNonZeroReturnVal()
            .exec();
    AardvarkAssertions.assertThat(exec).isEmpty();
  }
}
