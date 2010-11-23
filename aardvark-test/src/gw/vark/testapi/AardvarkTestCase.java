/*
 * Copyright (c) 2010 Guidewire Software, Inc.
 */

package gw.vark.testapi;

import junit.framework.TestCase;
import org.fest.assertions.Assertions;
import org.fest.assertions.ListAssert;
import org.fest.assertions.ObjectAssert;
import org.fest.assertions.StringAssert;

import java.util.List;

/**
 */
public abstract class AardvarkTestCase extends TestCase {

  public static StringAssert assertThat(String actual) {
    return Assertions.assertThat(actual);
  }

  public static ListAssert assertThat(List<?> actual) {
    return Assertions.assertThat(actual);
  }

  public static ObjectAssert assertThat(Object actual) {
    return Assertions.assertThat(actual);
  }
}
