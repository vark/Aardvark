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

import gw.lang.launch.ArgInfo;
import org.apache.tools.ant.types.LogLevel;

public class AardvarkOptions
{
  private boolean _projectHelp;
  private LogLevel  _logLevel = LogLevel.INFO;
  private LinkedHashMap<String, TargetCall> _targetCalls = new LinkedHashMap<String, TargetCall>();
  private Map<String, String> _definedProps = new HashMap<String, String>();

  private boolean _bootstrapHelp = false; // TODO - this should go into AardvarkHelpMode
  private boolean _version = false; // TODO - this should go into AardvarkVersionMode
  private String _logger = null;

  AardvarkOptions(String... args) {
    this(ArgInfo.parseArgs(args));
  }

  public AardvarkOptions(ArgInfo argInfo) {
    _bootstrapHelp = argInfo.consumeArg("-h", "--help", "-help");
    _version = argInfo.consumeArg("--version", "-version");
    _logger = argInfo.consumeArgAndParam("--logger", "-logger");
    _projectHelp = argInfo.consumeArg("-p", "--projecthelp", "-projecthelp");
    boolean quiet = argInfo.consumeArg("-q", "--quiet", "-quiet");
    boolean verbose = argInfo.consumeArg("-v", "--verbose", "-verbose");
    boolean debug = argInfo.consumeArg("-d", "--debug", "-debug");
    if (debug) {
      _logLevel = LogLevel.DEBUG;
    }
    else if (verbose) {
      _logLevel = LogLevel.VERBOSE;
    }
    else if (quiet) {
      _logLevel = LogLevel.WARN;
    }
    // TODO - handle system properties

    Deque<String> rawTargets = new LinkedList<String>(argInfo.getArgsList());
    String it = rawTargets.poll();
    while (it != null) {
      TargetCall targetCall = new TargetCall(it);
      while (rawTargets.peek() != null && rawTargets.peek().startsWith("-")) {
        String paramName = rawTargets.poll().substring(1);
        String paramVal = possiblyHandleArgValue(rawTargets);
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

  private String possiblyHandleArgValue(Deque<String> deque) {
    String value = deque.peek();
    if (value == null || value.startsWith("-")) {
      return null;
    }
    return deque.poll();
  }

  // TODO - this should be using LauncherSystemProperties
  public Map<String, String> getDefinedProps() {
    return _definedProps;
  }
}
