/*
 * Copyright (c) 2010 Guidewire Software, Inc.
 */

package gw.vark;

import junit.framework.TestCase;
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
    assertTrue(options.isBootstrapVersion());
  }

  public void testOptionFileNoParam() {
    try {
      new AardvarkOptions("-f");
      fail("expected exception");
    }
    catch (IllegalArgumentException e) {
      assertEquals("\"-f\" is expected to be followed by a parameter", e.getMessage());
    }
  }

  public void testOptionFileNoParam2() {
    try {
      new AardvarkOptions("--file");
      fail("expected exception");
    }
    catch (IllegalArgumentException e) {
      assertEquals("\"--file\" is expected to be followed by a parameter", e.getMessage());
    }
  }

  public void testOptionFileNoParam3() {
    try {
      new AardvarkOptions("-f", "--version");
      fail("expected exception");
    }
    catch (IllegalArgumentException e) {
      assertEquals("\"-f\" is expected to be followed by a parameter", e.getMessage());
    }
  }

  public void testOptionFileNoParam4() {
    try {
      new AardvarkOptions("--file", "--version");
      fail("expected exception");
    }
    catch (IllegalArgumentException e) {
      assertEquals("\"--file\" is expected to be followed by a parameter", e.getMessage());
    }
  }

  public void testOptionFile() {
    AardvarkOptions options = new AardvarkOptions("-f", "foo.vark");
    assertEquals("foo.vark", options.getBootstrapFile());
    options = new AardvarkOptions("--file", "foo.vark");
    assertEquals("foo.vark", options.getBootstrapFile());
  }

  public void testOptionLoggerNoParam() {
    try {
      new AardvarkOptions("--logger");
      fail("expected exception");
    }
    catch (IllegalArgumentException e) {
      assertEquals("\"--logger\" is expected to be followed by a parameter", e.getMessage());
    }
  }

  public void testOptionLoggerNoParam2() {
    try {
      new AardvarkOptions("--logger", "--version");
      fail("expected exception");
    }
    catch (IllegalArgumentException e) {
      assertEquals("\"--logger\" is expected to be followed by a parameter", e.getMessage());
    }
  }

  public void testOptionLogger() {
    AardvarkOptions options = new AardvarkOptions("--logger", "gw.vark.FooLogger");
    assertEquals("gw.vark.FooLogger", options.getBootstrapLogger());
  }

  public void testDefinedProps() {
    AardvarkOptions options = new AardvarkOptions("-Dfoo=bar");
    Assertions.assertThat(options.getDefinedProps()).isEqualTo(Collections.singletonMap("foo", "bar"));
    options = new AardvarkOptions("-Dfoo", "bar");
    Assertions.assertThat(options.getDefinedProps()).isEqualTo(Collections.singletonMap("foo", "bar"));
  }

  public void testOptionsNotInitialized() {
    AardvarkOptions options = new AardvarkOptions();
    try {
      options.isVerify();
      fail("expected exception");
    }
    catch (IllegalStateException e) {
      assertEquals("must be initialized", e.getMessage());
    }
  }

  public void testNone() {
    AardvarkOptions options = new AardvarkOptions();
    assertFalse(options.isBootstrapHelp());
    assertFalse(options.isBootstrapVersion());
    assertNull(options.getBootstrapFile());
    assertNull(options.getBootstrapLogger());
    Assertions.assertThat(options.getDefinedProps()).isEmpty();
  }

  public void testAll() {
    AardvarkOptions options = new AardvarkOptions("-h", "-f", "foo.vark", "--version", "--logger", "gw.vark.FooLogger", "-Dfoo=bar");
    assertTrue(options.isBootstrapHelp());
    assertTrue(options.isBootstrapVersion());
    assertEquals("foo.vark", options.getBootstrapFile());
    assertEquals("gw.vark.FooLogger", options.getBootstrapLogger());
    Assertions.assertThat(options.getDefinedProps()).isEqualTo(Collections.singletonMap("foo", "bar"));
  }

  public void testAll2() {
    AardvarkOptions options = new AardvarkOptions("--help", "--file", "foo.vark", "--version", "--logger", "gw.vark.FooLogger", "-Dfoo", "bar");
    assertTrue(options.isBootstrapHelp());
    assertTrue(options.isBootstrapVersion());
    assertEquals("foo.vark", options.getBootstrapFile());
    assertEquals("gw.vark.FooLogger", options.getBootstrapLogger());
    Assertions.assertThat(options.getDefinedProps()).isEqualTo(Collections.singletonMap("foo", "bar"));
  }
}
