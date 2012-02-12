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
import gw.vark.testapi.AardvarkTestCase;
import gw.vark.testapi.ForkedAardvarkProcess;
import gw.vark.testapi.TestUtil;
import org.fest.assertions.ListAssert;

import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 */
public class AardvarkProcessTest extends AardvarkTestCase {

  private File _sampleprojectDir;

  public AardvarkProcessTest() {
    super();
  }

  @Override
  protected void setUp() throws Exception {
    File home = TestUtil.getHome(getClass());
    _sampleprojectDir = new File(home, "sampleproject");
    clean();
  }

  @Override
  protected void tearDown() throws Exception {
    clean();
  }

  public void testHelp() {
    TestOutputHandler stdOut = new TestOutputHandler("stdout");
    TestOutputHandler stdErr = new TestOutputHandler("stderr");
    runAardvark("-h", stdOut, stdErr);
    assertThatOutput(stdErr).isEmpty();
    assertThatOutput(stdOut).startsWith(
            "Usage: vark [options] target [target2 [target3] ..]",
            "Options:");
  }

  public void testVersion() {
    TestOutputHandler stdOut = new TestOutputHandler("stdout");
    TestOutputHandler stdErr = new TestOutputHandler("stderr");
    runAardvark("--version", stdOut, stdErr);
    assertThatOutput(stdErr).isEmpty();
    assertOutputMatches(stdOut, "m:Aardvark version \\d+\\.\\d+");
    assertThatOutput(stdOut).containsExactly(Aardvark.getVersion());
  }

  public void testSampleprojectFailedBuild() {
    TestOutputHandler stdOut = new TestOutputHandler("stdout");
    TestOutputHandler stdErr = new TestOutputHandler("stderr");
    runAardvark("epic-fail", stdOut, stdErr);
    assertOutputMatches(stdOut,
            "e:Buildfile: " + _sampleprojectDir + File.separator + "build.vark",
            "m:Done parsing Aardvark buildfile in \\d+ ms",
            "e:",
            "e:epic-fail:"
    );
    assertOutputMatches(stdErr,
            "e:aardvark.dev is on",
            "e:",
            "e:BUILD FAILED",
            "e:you fail",
            "e:",
            "m:Total time: \\d+ seconds?"
    );
  }

  public void testSampleprojectRun() {
    TestOutputHandler stdOut = new TestOutputHandler("stdout");
    TestOutputHandler stdErr = new TestOutputHandler("stderr");

    stdOut._lines.clear();
    stdErr._lines.clear();
    runAardvark("", stdOut, stdErr);
    assertThatOutput(stdErr).containsExactly("aardvark.dev is on");
    assertThatOutput(stdOut).containsSequence(
            "run:",
            "     [java] Hello World"
    );
    assertThatOutput(stdOut).contains("BUILD SUCCESSFUL");
    assertTrue(new File(_sampleprojectDir, "build/dist/sampleproject.jar").exists());
  }

  private void runAardvark(String args, TestOutputHandler stdOut, TestOutputHandler stdErr) {
    File varkFile = new File(_sampleprojectDir, "build.vark");
    runAardvark(varkFile, args, stdOut, stdErr);
  }

  private void runAardvark(File varkFile, String args, TestOutputHandler stdOut, TestOutputHandler stdErr) {
    String exec = new ForkedAardvarkProcess()
            .withVarkFile(varkFile)
            .withArgs(args)
            .build()
            .withStdOutHandler(stdOut)
            .withStdErrHandler(stdErr)
            .doNotThrowOnNonZeroReturnVal()
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

  private void clean() {
    System.out.println("Running equivalent of \"vark clean\"");
    recursiveDelete(new File(_sampleprojectDir, "build"));
  }

  private void recursiveDelete(File file) {
    if (file.exists()) {
      if (file.isDirectory()) {
        for (File sub : file.listFiles()) {
          recursiveDelete(sub);
        }
      }
      file.delete();
    }
  }

  private static class TestOutputHandler implements ProcessStarter.OutputHandler {
    ArrayList<String> _lines = new ArrayList<String>();
    String _name;
    TestOutputHandler(String name) {
      _name = name;
    }
    @Override
    public void handleLine(String line) {
      _lines.add(line);
      System.out.println("AardvarkProcessTest " + _name + ": " + line);
    }
  }

}
