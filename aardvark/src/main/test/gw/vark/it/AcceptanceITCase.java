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

package gw.vark.it;

import gw.vark.Aardvark;
import junit.framework.TestCase;
import org.fest.assertions.Assertions;
import org.fest.assertions.ListAssert;

import java.io.File;

/**
 */
public class AcceptanceITCase extends TestCase {

  private File _sampleprojectDir;

  public AcceptanceITCase() {
    super();
  }

  @Override
  protected void setUp() throws Exception {
    File home = ITUtil.getProjectRoot();
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
            "Usage:",
            "        vark [-f FILE] [options] [targets...]",
            "",
            "Options:");
  }

  public void testVersion() {
    TestOutputHandler stdOut = new TestOutputHandler("stdout");
    TestOutputHandler stdErr = new TestOutputHandler("stderr");
    runAardvark("-version", stdOut, stdErr);
    assertThatOutput(stdErr).isEmpty();
    assertOutputMatches(stdOut, "m:Aardvark version \\d+\\.\\d+");
    assertThatOutput(stdOut).containsExactly(Aardvark.getVersion());
  }

  public void testDirectoryWithNoDefaultBuildVark() {
    TestOutputHandler stdOut = new TestOutputHandler("stdout");
    TestOutputHandler stdErr = new TestOutputHandler("stderr");
    runAardvark(new File(_sampleprojectDir, "src"), null, "", stdOut, stdErr);
    assertThatOutput(stdErr).contains("Default vark buildfile build.vark doesn't exist");
  }

  public void testDirectoryWithNoSpecifiedBuildVark() {
    TestOutputHandler stdOut = new TestOutputHandler("stdout");
    TestOutputHandler stdErr = new TestOutputHandler("stderr");
    File merpVark = new File(_sampleprojectDir, "merp.vark");
    runAardvark(_sampleprojectDir, merpVark, "", stdOut, stdErr);
    assertThatOutput(stdErr).contains("Specified vark buildfile " + merpVark + " doesn't exist");
  }

  public void testSampleprojectProjectHelp() {
    TestOutputHandler stdOut = new TestOutputHandler("stdout");
    TestOutputHandler stdErr = new TestOutputHandler("stderr");
    runAardvark("-p", stdOut, stdErr);
    assertThatOutput(stdErr).containsExactly("aardvark.dev is on");
    assertOutputMatches(stdOut,
            "e:Buildfile: " + _sampleprojectDir + File.separator + Aardvark.DEFAULT_BUILD_FILE_NAME,
            "m:Done parsing Aardvark buildfile in \\d+ ms",
            "e:",
            "e:Valid targets:",
            "e:",
            "e:  echo-hello -  Echos \"Hello World\"",
            "e:  epic-fail  -  Breaks the process with an intentional failure",
            "e:  compile    -  Compiles the project",
            "e:",
            "e:FEED THE VARK!",
            "e:"
    );
  }

  public void testSampleprojectFailedBuild() {
    TestOutputHandler stdOut = new TestOutputHandler("stdout");
    TestOutputHandler stdErr = new TestOutputHandler("stderr");
    runAardvark("epic-fail", stdOut, stdErr);
    assertOutputMatches(stdOut,
            "e:Buildfile: " + _sampleprojectDir + File.separator + Aardvark.DEFAULT_BUILD_FILE_NAME,
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
    runAardvark(_sampleprojectDir, null, args, stdOut, stdErr);
  }

  private void runAardvark(File dir, File varkFile, String args, TestOutputHandler stdOut, TestOutputHandler stdErr) {
    String exec = new ForkedAardvarkProcess(varkFile)
            .withWorkingDirectory(dir)
            .withArgs("-Dlauncher.path=" + buildLauncherPath() + " "
                    + "-Dlauncher.log.level=warn "
                    + "-use-tools-jar -default-program-file build.vark " + args)
            .withDebug(ForkedBuildProcess.Debug.SOCKET)
            .build()
            .withStdOutHandler(stdOut)
            .withStdErrHandler(stdErr)
            .doNotThrowOnNonZeroReturnVal()
            .exec();
    Assertions.assertThat(exec).isEmpty();
  }

  private void clean() {
    System.out.println("Running equivalent of \"vark clean\"");
    recursiveDelete(new File(_sampleprojectDir, "build"));
  }

  private void recursiveDelete(File file) {
    if (file.exists()) {
      if (file.isDirectory()) {
        //noinspection ConstantConditions
        for (File sub : file.listFiles()) {
          recursiveDelete(sub);
        }
      }
      file.delete();
    }
  }

  private static String buildLauncherPath() {
    StringBuilder launcherPath = new StringBuilder();
    File[] libFiles = new File(ITUtil.getAssemblyDir(), "lib").listFiles();
    if (libFiles != null) {
      for (File file : libFiles) {
        if (file.isFile() && file.getName().endsWith(".jar")) {
          if (launcherPath.length() > 0) {
            launcherPath.append(',');
          }
          launcherPath.append(file.getAbsolutePath());
        }
      }
      return launcherPath.toString();
    }
    else {
      throw new IllegalStateException("listFiles() returned null");
    }
  }


  private static ListAssert assertThatOutput(TestOutputHandler handler) {
    return Assertions.assertThat(handler._lines).as("Aardvark output");
  }

  private static void assertOutputMatches(TestOutputHandler stdOut, String... lines) {
    assertThatOutput(stdOut).hasSize(lines.length);
    for (int i = 0; i < lines.length; i++) {
      if (lines[i].startsWith("e:")) {
        Assertions.assertThat(stdOut._lines.get(i)).isEqualTo(lines[i].substring(2));
      }
      else if (lines[i].startsWith("m:")) {
        Assertions.assertThat(stdOut._lines.get(i)).matches(lines[i].substring(2));
      }
      else {
        throw new IllegalArgumentException("line must start with e: or m:");
      }
    }
  }

}
