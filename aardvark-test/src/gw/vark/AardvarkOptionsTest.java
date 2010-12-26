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

import junit.framework.TestCase;
import org.apache.tools.ant.types.LogLevel;
import org.fest.assertions.Assertions;

import java.util.Collections;

/**
 */
public class AardvarkOptionsTest extends TestCase {

  public AardvarkOptionsTest() {
    super();
  }

  public void testOptionHelp() {
    AardvarkOptions options = new AardvarkOptions("-h");
    assertTrue(options.isBootstrapHelp());
    options = new AardvarkOptions("--help");
    assertTrue(options.isBootstrapHelp());
  }

  public void testOptionVersion() {
    AardvarkOptions options = new AardvarkOptions("--version");
    assertTrue(options.isVersion());
  }

  public void testOptionFileNoParam() {
    try {
      new AardvarkOptions("-f");
      fail("expected exception");
    }
    catch (IllegalArgumentException e) {
      assertEquals("\"-f\" is expected to be followed by a value", e.getMessage());
    }
  }

  public void testOptionFileNoParam2() {
    try {
      new AardvarkOptions("--file");
      fail("expected exception");
    }
    catch (IllegalArgumentException e) {
      assertEquals("\"--file\" is expected to be followed by a value", e.getMessage());
    }
  }

  public void testOptionFileNoParam3() {
    try {
      new AardvarkOptions("-f", "--version");
      fail("expected exception");
    }
    catch (IllegalArgumentException e) {
      assertEquals("\"-f\" is expected to be followed by a value", e.getMessage());
    }
  }

  public void testOptionFileNoParam4() {
    try {
      new AardvarkOptions("--file", "--version");
      fail("expected exception");
    }
    catch (IllegalArgumentException e) {
      assertEquals("\"--file\" is expected to be followed by a value", e.getMessage());
    }
  }

  public void testOptionFile() {
    AardvarkOptions options = new AardvarkOptions("-f", "foo.vark");
    assertEquals("foo.vark", options.getBuildFile());
    options = new AardvarkOptions("--file", "foo.vark");
    assertEquals("foo.vark", options.getBuildFile());
  }

  public void testOptionLoggerNoParam() {
    try {
      new AardvarkOptions("--logger");
      fail("expected exception");
    }
    catch (IllegalArgumentException e) {
      assertEquals("\"--logger\" is expected to be followed by a value", e.getMessage());
    }
  }

  public void testOptionLoggerNoParam2() {
    try {
      new AardvarkOptions("--logger", "--version");
      fail("expected exception");
    }
    catch (IllegalArgumentException e) {
      assertEquals("\"--logger\" is expected to be followed by a value", e.getMessage());
    }
  }

  public void testOptionLogger() {
    AardvarkOptions options = new AardvarkOptions("--logger", "gw.vark.FooLogger");
    assertEquals("gw.vark.FooLogger", options.getLogger());
  }

  public void testOptionVerify() {
    AardvarkOptions options = new AardvarkOptions("--verify");
    assertTrue(options.isVerify());
  }

  public void testOptionProjectHelp() {
    AardvarkOptions options = new AardvarkOptions("-p");
    assertTrue(options.isHelp());
  }

  public void testOptionProjectHelp2() {
    AardvarkOptions options = new AardvarkOptions("--projecthelp");
    assertTrue(options.isHelp());
  }

  public void testOptionQuiet() {
    AardvarkOptions options = new AardvarkOptions("-q");
    assertEquals(LogLevel.WARN, options.getLogLevel());
  }

  public void testOptionQuiet2() {
    AardvarkOptions options = new AardvarkOptions("--quiet");
    assertEquals(LogLevel.WARN, options.getLogLevel());
  }

  public void testOptionVerbose() {
    AardvarkOptions options = new AardvarkOptions("-v");
    assertEquals(LogLevel.VERBOSE, options.getLogLevel());
  }

  public void testOptionVerbose2() {
    AardvarkOptions options = new AardvarkOptions("--verbose");
    assertEquals(LogLevel.VERBOSE, options.getLogLevel());
  }

  public void testOptionDebug() {
    AardvarkOptions options = new AardvarkOptions("-d");
    assertEquals(LogLevel.DEBUG, options.getLogLevel());
  }

  public void testOptionDebug2() {
    AardvarkOptions options = new AardvarkOptions("--debug");
    assertEquals(LogLevel.DEBUG, options.getLogLevel());
  }

  public void testDefinedPropsWithEquals() {
    AardvarkOptions options = new AardvarkOptions("-Dfoo=bar");
    Assertions.assertThat(options.getDefinedProps()).isEqualTo(Collections.singletonMap("foo", "bar"));
  }

  public void testDefinedPropsWithoutEquals() {
    AardvarkOptions options = new AardvarkOptions("-Dfoo", "bar");
    Assertions.assertThat(options.getDefinedProps()).isEqualTo(Collections.singletonMap("foo", "bar"));
  }

  public void testDefinedPropsWithoutEqualsNoParam() {
    try {
      new AardvarkOptions("-Dfoo");
      fail("expected exception");
    }
    catch (IllegalArgumentException e) {
      assertEquals("\"-Dfoo\" is expected to be followed by a value", e.getMessage());
    }
  }

  public void testDefinedPropsWithoutEqualsNoParam2() {
    try {
      new AardvarkOptions("-Dfoo", "--version");
      fail("expected exception");
    }
    catch (IllegalArgumentException e) {
      assertEquals("\"-Dfoo\" is expected to be followed by a value", e.getMessage());
    }
  }

  public void testNone() {
    AardvarkOptions options = new AardvarkOptions();
    assertFalse(options.isBootstrapHelp());
    assertFalse(options.isVersion());
    assertNull(options.getBuildFile());
    assertNull(options.getLogger());
    assertFalse(options.isVerify());
    assertFalse(options.isHelp());
    assertEquals(LogLevel.INFO, options.getLogLevel());
    Assertions.assertThat(options.getDefinedProps()).isEmpty();
  }

  public void testAll() {
    AardvarkOptions options = new AardvarkOptions("-h", "-f", "foo.vark", "--version", "--logger", "gw.vark.FooLogger", "--verify", "-p", "-Dfoo=bar");
    assertTrue(options.isBootstrapHelp());
    assertTrue(options.isVersion());
    assertEquals("foo.vark", options.getBuildFile());
    assertEquals("gw.vark.FooLogger", options.getLogger());
    assertTrue(options.isVerify());
    assertTrue(options.isHelp());
    Assertions.assertThat(options.getDefinedProps()).isEqualTo(Collections.singletonMap("foo", "bar"));
  }

  public void testAll2() {
    AardvarkOptions options = new AardvarkOptions("--help", "--file", "foo.vark", "--version", "--logger", "gw.vark.FooLogger", "--verify", "--projecthelp", "-Dfoo", "bar");
    assertTrue(options.isBootstrapHelp());
    assertTrue(options.isVersion());
    assertEquals("foo.vark", options.getBuildFile());
    assertEquals("gw.vark.FooLogger", options.getLogger());
    assertTrue(options.isVerify());
    assertTrue(options.isHelp());
    Assertions.assertThat(options.getDefinedProps()).isEqualTo(Collections.singletonMap("foo", "bar"));
  }
}
