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

import org.apache.tools.ant.DefaultLogger;

import java.util.ArrayList;

/**
 */
public class InMemoryLogger extends DefaultLogger {

  private final ArrayList<String> _messages = new ArrayList<String>();
  private static InMemoryLogger _lastInstance;

  public static InMemoryLogger getLastInstance() {
    return _lastInstance;
  }

  public InMemoryLogger() {
    super();
    _lastInstance = this;
  }

  public void dump() {
    for (String message : _messages) {
      System.out.print(">");
      System.out.print(message);
      System.out.println("");
    }
  }

  public ArrayList<String> getMessages() {
    return _messages;
  }

  @Override
  protected void log(String message) {
    _messages.add(message);
  }
}
