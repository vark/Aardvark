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

package gw.vark.testapi;

import org.fest.assertions.ListAssert;

/**
 */
public class InMemoryLoggerAssert extends ListAssert {

  private final InMemoryLogger _logger;

  public InMemoryLoggerAssert(InMemoryLogger logger) {
    super(logger.getMessages());
    _logger = logger;
  }

  public void matches(StringMatchAssertion... predicates) {
    hasSize(predicates.length);
    for (int i = 0; i < predicates.length; i++) {
      String actualLine = _logger.getMessages().get(i);
      predicates[i].evaluate(actualLine);
    }
  }

  public void containsLinesThatContain(String s) {
    if (firstLineThatContains(s) == null) {
      fail("expected to find a line in logger containing " + s);
    }
  }

  public void excludesLinesThatContain(String s) {
    if (firstLineThatContains(s) != null) {
      fail("did not expect to find a line in logger containing " + s);
    }
  }

  private String firstLineThatContains(String s) {
    for (String line : _logger.getMessages()) {
      if (line.contains(s)) {
        return line;
      }
    }
    return null;
  }
}
