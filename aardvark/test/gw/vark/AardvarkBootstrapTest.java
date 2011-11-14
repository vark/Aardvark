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

import gw.vark.testapi.AardvarkTestCase;
import gw.vark.testapi.InMemoryLogger;
import org.apache.tools.ant.BuildException;
import org.fest.assertions.ListAssert;

/**
 */
public class AardvarkBootstrapTest extends AardvarkTestCase {

  private InMemoryLogger _logger;

  public AardvarkBootstrapTest() {
    super();
  }

  public void setUp() {
    _logger = new InMemoryLogger();
  }

  public void testArgH() {
    runAardvark("-h");
    assertThatLog().startsWith(
            "Usage: vark [options] target [target2 [target3] ..]",
            "Options:"
    );
  }

  public void testArgHelp() {
    runAardvark("--help");
    assertThatLog().startsWith(
            "Usage: vark [options] target [target2 [target3] ..]",
            "Options:"
    );
  }

  public void testArgVersion() {
    runAardvark("--version");
    assertThatLog().containsExactly(
            "Aardvark version " + Aardvark.getVersion()
    );
  }

  public void testLogger() {
    runAardvark("--logger", InMemoryLogger.class.getName(), "--version");
    InMemoryLogger lastLoggerInstance = InMemoryLogger.getLastInstance();
    assertThat(lastLoggerInstance).isNotSameAs(_logger);
    assertThat(lastLoggerInstance.getMessages()).as("log output").containsExactly(
            "Aardvark version " + Aardvark.getVersion()
    );
  }

  public void testLoggerNoParam() {
    try {
      runAardvark("--logger");
      fail("expected " + IllegalArgumentException.class.getSimpleName());
    }
    catch (IllegalArgumentException e) {
      assertThat(e.getMessage()).isEqualTo("\"--logger\" is expected to be followed by a value");
    }
  }

  public void testLoggerNoParam2() {
    try {
      runAardvark("--logger", "--version");
      fail("expected " + IllegalArgumentException.class.getSimpleName());
    }
    catch (IllegalArgumentException e) {
      assertThat(e.getMessage()).isEqualTo("\"--logger\" is expected to be followed by a value");
    }
  }

  public void testLoggerClassNotFound() {
    try {
      runAardvark("--logger", "foo", "--version");
      fail("expected " + BuildException.class.getSimpleName());
    }
    catch (BuildException e) {
      assertThat(e.getMessage()).isEqualTo("Class not found: foo");
    }
    assertThatLog().containsExactly("The specified logger class foo could not be used because Class not found: foo");
  }

  public void testNonExistentFile() {
    runAardvark(Aardvark.EXITCODE_VARKFILE_NOT_FOUND, "--file", "foo.vark");
    assertThatLog().containsExactly( "Specified vark buildfile \"foo.vark\" doesn't exist" );
  }

  private ListAssert assertThatLog() {
    return assertThat(_logger.getMessages()).as("log output");
  }

  private void runAardvark(String... args) {
    runAardvark(0, args);
  }

  private void runAardvark(int expectedExitCode, String... args) {
    Aardvark a = new Aardvark(_logger);
    int exitCode = a.startAardvark(args);
    assertEquals("exit code", expectedExitCode, exitCode);
  }
}
