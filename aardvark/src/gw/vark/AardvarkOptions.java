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

import java.util.*;

import org.apache.tools.ant.types.LogLevel;

public class AardvarkOptions
{
 private boolean _projectHelp;
  private boolean _verify;
  private LogLevel _logLevel = LogLevel.INFO;
  private LinkedHashMap<String, TargetCall> _targetCalls = new LinkedHashMap<String, TargetCall>();
  private Map<String, String> _definedProps = new HashMap<String, String>();

  private boolean _bootstrapHelp = false;
  private boolean _version = false;
  private String _logger = null;
  private String _buildFile = null;

  AardvarkOptions(String... cmdLineOptions) {
    ArrayDeque<String> rawArgs = new ArrayDeque<String>(Arrays.asList(cmdLineOptions));
    ArrayDeque<String> rawTargets = new ArrayDeque<String>();

    String it = rawArgs.poll();
    while (it != null) {
      if (it.equals("-h") || it.equals("--help")) {
        _bootstrapHelp = true;
      }
      else if (it.equals("--version")) {
        _version = true;
      }
      else if (it.equals("--logger")) {
        _logger = handleArgValue(rawArgs, it);
      }
      else if (it.equals("-f") || it.equals("--file")) {
        _buildFile = handleArgValue(rawArgs, it);
      }
      else if (it.equals("--verify")) {
        _verify = true;
      }
      else if (it.equals("-p") || it.equals("--projecthelp")) {
        _projectHelp = true;
      }
      else if (it.equals("-q") || it.equals("--quiet")) {
        _logLevel = LogLevel.WARN;
      }
      else if (it.equals("-v") || it.equals("--verbose")) {
        _logLevel = LogLevel.VERBOSE;
      }
      else if (it.equals("-d") || it.equals("--debug")) {
        _logLevel = LogLevel.DEBUG;
      }
      else if (it.startsWith("-D")) {
        handleArgDefine(rawArgs, it);
      }
      else {
        rawTargets.add(it);
      }

      it = rawArgs.poll();
    }

    it = rawTargets.poll();
    while (it != null) {
      TargetCall targetCall = new TargetCall(it);
      while (rawTargets.peek() != null && rawTargets.peek().startsWith("-")) {
        String paramName = rawTargets.poll().substring(1);
        String paramVal = handleArgValue(rawTargets, paramName);
        targetCall.addParam(paramName, paramVal);
      }
      _targetCalls.put(targetCall.getName(), targetCall);
      it = rawTargets.poll();
    }
  }

  public boolean isBootstrapHelp() {
    return _bootstrapHelp;
  }

  public boolean isVersion() {
    return _version;
  }

  public String getLogger() {
    return _logger;
  }

  public String getBuildFile() {
    return _buildFile;
  }

  public boolean isVerify() {
    return _verify;
  }

  public boolean isHelp() {
    return _projectHelp;
  }

  public LogLevel getLogLevel() {
    return _logLevel;
  }

  public LinkedHashMap<String, TargetCall> getTargetCalls() {
    return _targetCalls;
  }

  public List<String> getTargets() {
    return new ArrayList<String>(_targetCalls.keySet());
  }

  public Map<String, String> getDefinedProps() {
    return _definedProps;
  }

  private String handleArgValue(ArrayDeque<String> deque, String it) {
    String value = deque.poll();
    if (value == null || value.startsWith("-")) {
      throw new IllegalArgumentException("\"" + it + "\" is expected to be followed by a value");
    }
    return value;
  }

  /* Handle -D argument */
  private void handleArgDefine(ArrayDeque<String> deque, String it) {
    /* Interestingly enough, we get to here when a user
    * uses -Dname=value. However, in some cases, the OS
    * goes ahead and parses this out to args
    *   {"-Dname", "value"}
    * so instead of parsing on "=", we just make the "-D"
    * characters go away and skip one argument forward.
    *
    * I don't know how to predict when the JDK is going
    * to help or not, so we simply look for the equals sign.
    */
    String name = it.substring(2); // 2 <= "-D".length()
    String value;
    int posEq = name.indexOf("=");
    if (posEq > 0) {
      value = name.substring(posEq + 1);
      name = name.substring(0, posEq);
    } else {
      value = handleArgValue(deque, it);
    }
    _definedProps.put(name, value);
  }

}
