/*
 * Copyright (c) 2012 Guidewire Software, Inc.
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

package gw.vark.testapi;

import org.fest.assertions.Assertions;

/**
 */
public abstract class StringMatchAssertion {

  public static StringMatchAssertion exact(String expected) {
    return new StringEqualityMatchAssertion(expected);
  }

  public static StringMatchAssertion regex(String regex) {
    return new StringRegexMatchAssertion(regex);
  }

  private static class StringEqualityMatchAssertion extends StringMatchAssertion {
    private final String _expected;

    private StringEqualityMatchAssertion(String expected) {
      _expected = expected;
    }

    public void evaluate(String s) {
      Assertions.assertThat(s).isEqualTo(_expected);
    }
  }

  private static class StringRegexMatchAssertion extends StringMatchAssertion {
    private final String _regex;

    private StringRegexMatchAssertion(String regex) {
      _regex = regex;
    }

    public void evaluate(String s) {
      Assertions.assertThat(s).matches(_regex);
    }
  }

  public abstract void evaluate(String s);
}
