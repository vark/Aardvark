/*
 * Copyright (c) 2010 Guidewire Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gw.vark;

import gw.util.ProcessStarter;
import gw.util.Shell;
import gw.vark.testapi.AardvarkTestCase;
import gw.vark.testapi.TestUtil;
import org.apache.tools.ant.launch.Locator;
import org.fest.assertions.ListAssert;

import java.io.File;
import java.util.ArrayList;

/**
 */
public class AardvarkProcessTest extends AardvarkTestCase {

  // TODO - move most of this into TestprojectTest, leave a single end-to-end test

  private File _testprojectDir;

  public AardvarkProcessTest() {
    super();
  }

  @Override
  protected void setUp() throws Exception {
    File home = TestUtil.getHome(getClass());
    _testprojectDir = new File(home, "testproject");
  }

  public void testTestprojectEchoHello() {
    TestOutputHandler stdOut = new TestOutputHandler();
    TestOutputHandler stdErr = new TestOutputHandler();
    runAardvark("echo-hello", stdOut, stdErr);
    assertThatOutput(stdErr).isEmpty();
    assertOutputMatches(stdOut,
            "e:aardvark.dev is set to true - using IDE-compiled classes",
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

  public void testTestprojectFailedBuild() {
    TestOutputHandler stdOut = new TestOutputHandler();
    TestOutputHandler stdErr = new TestOutputHandler();
    runAardvark("epic-fail", stdOut, stdErr);
    assertOutputMatches(stdOut,
            "e:aardvark.dev is set to true - using IDE-compiled classes",
            "e:Buildfile: " + _testprojectDir + File.separator + "build.vark",
            "m:\\[\\d\\d:\\d\\d:\\d\\d\\] Done parsing Aardvark buildfile in \\d+ ms",
            "e:",
            "e:", // TODO - gosu bug
            "e:", // TODO - gosu bug
            "e:epic-fail:"
    );
    assertOutputMatches(stdErr,
            "e:",
            "e:BUILD FAILED",
            "e:you fail",
            "e:",
            "m:Total time: \\d+ seconds?"
    );
  }

  public void testTestprojectRun() {
    TestOutputHandler stdOut = new TestOutputHandler();
    TestOutputHandler stdErr = new TestOutputHandler();
    runAardvark("clean run", stdOut, stdErr);
    assertThatOutput(stdErr).isEmpty();
    assertThatOutput(stdOut).containsSequence(
            "run:",
            "     [java] Hello World"
    );
    assertThatOutput(stdOut).contains("BUILD SUCCESSFUL");
    assertTrue(new File(_testprojectDir, "build/dist/testproject.jar").exists());

    stdOut = new TestOutputHandler();
    stdErr = new TestOutputHandler();
    runAardvark("clean", stdOut, stdErr);
    assertThatOutput(stdErr).isEmpty();
    assertThatOutput(stdOut).contains("BUILD SUCCESSFUL");
    assertFalse(new File(_testprojectDir, "build/dist/testproject.jar").exists());
  }

  private void runAardvark(String args, TestOutputHandler stdOut, TestOutputHandler stdErr) {
    File varkFile = new File(_testprojectDir, "build.vark");
    runAardvark(varkFile, args, stdOut, stdErr);
  }

  private void runAardvark(File varkFile, String args, TestOutputHandler stdOut, TestOutputHandler stdErr) {
    String javaCommand = System.getProperty("java.home") + "/bin/java";
    String classpathString = Locator.getClassSource(gw.vark.launch.Launcher.class).getPath()
            + File.pathSeparator
            + Locator.getClassSource(org.apache.tools.ant.launch.Launcher.class).getPath();
    String command = javaCommand
            + " -Daardvark.dev=true"
            //+ " -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
            + " -cp " + classpathString + " gw.vark.launch.Launcher"
            + " -f " + varkFile
            + " " + args;
    System.out.println(command);
    String exec = Shell.buildProcess(command)
            .withStdOutHandler(stdOut)
            .withStdErrHandler(stdErr)
            .exec();
    assertThat(exec).isEmpty();
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

  private static class TestOutputHandler implements ProcessStarter.OutputHandler {
    ArrayList<String> _lines = new ArrayList<String>();
    @Override
    public void handleLine(String line) {
      _lines.add(line);
      System.out.println(line);
    }
  }

}
