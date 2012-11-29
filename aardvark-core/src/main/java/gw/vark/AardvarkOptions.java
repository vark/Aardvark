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

package gw.vark;

import gw.lang.Gosu;
import gw.lang.launch.IArgInfo;
import gw.lang.launch.IArgKey;
import gw.lang.launch.IBooleanArgKey;
import gw.lang.launch.IStringArgKey;
import gw.lang.launch.Launch;
import gw.lang.launch.LaunchArgs;
import org.apache.tools.ant.types.LogLevel;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class AardvarkOptions
{
  public static final IStringArgKey ARGKEY_LOGGER = Launch.factory().createArgKeyBuilder("class name for a logger to use", "LOGGERFQN")
          .withLongSwitch("logger").build();
  public static final IBooleanArgKey ARGKEY_PROJECTHELP = Launch.factory().createArgKeyBuilder("show project help (e.g. targets)")
          .withShortSwitch('p').withLongSwitch("projecthelp").build();
  public static final IBooleanArgKey ARGKEY_QUIET = Launch.factory().createArgKeyBuilder("run with logging in quiet mode")
          .withShortSwitch('q').withLongSwitch("quiet").build();
  public static final IBooleanArgKey ARGKEY_VERBOSE = Launch.factory().createArgKeyBuilder("run with logging in verbose mode")
          .withShortSwitch('v').withLongSwitch("verbose").build();
  public static final IBooleanArgKey ARGKEY_DEBUG = Launch.factory().createArgKeyBuilder("run with logging in debug mode")
          .withShortSwitch('d').withLongSwitch("debug").build();
  public static final IBooleanArgKey ARGKEY_GRAPHICAL = Launch.factory().createArgKeyBuilder("starts the graphical Aardvark editor")
          .withShortSwitch('g').withLongSwitch("graphical").build();
  public static final IBooleanArgKey ARGKEY_VERSION = Launch.factory().createArgKeyBuilder("displays the version of Aardvark")
          .withLongSwitch("version").withOtherSwitch("-version").build();
  public static final IBooleanArgKey ARGKEY_HELP = Launch.factory().createArgKeyBuilder("displays this command-line help")
          .withShortSwitch('h').withLongSwitch("help").withOtherSwitch("-help").build();
  public static List<? extends IArgKey> getArgKeys() {
    return Arrays.asList(
            LaunchArgs.FILE_PROGRAM_SOURCE,
            LaunchArgs.URL_PROGRAM_SOURCE,
            LaunchArgs.CLASSPATH,
            LaunchArgs.DEFAULT_PROGRAM_FILE,
            LaunchArgs.USE_TOOLS_JAR,
            ARGKEY_PROJECTHELP,
            ARGKEY_LOGGER,
            ARGKEY_QUIET,
            ARGKEY_VERBOSE,
            ARGKEY_DEBUG,
            ARGKEY_GRAPHICAL,
            Gosu.ARGKEY_VERIFY,
            ARGKEY_VERSION,
            ARGKEY_HELP
    );
  }

  private boolean _projectHelp;
  private LogLevel  _logLevel = LogLevel.INFO;
  private LinkedHashMap<String, TargetCall> _targetCalls = new LinkedHashMap<String, TargetCall>();

  private String _logger = null;

  public AardvarkOptions(String... args) {
    this(Launch.factory().createArgInfo(args));
  }

  public AardvarkOptions(IArgInfo argInfo) {
    _logger = argInfo.consumeArg(ARGKEY_LOGGER);
    _projectHelp = argInfo.consumeArg(ARGKEY_PROJECTHELP);
    boolean quiet = argInfo.consumeArg(ARGKEY_QUIET);
    boolean verbose = argInfo.consumeArg(ARGKEY_VERBOSE);
    boolean debug = argInfo.consumeArg(ARGKEY_DEBUG);
    if (debug) {
      _logLevel = LogLevel.DEBUG;
    }
    else if (verbose) {
      _logLevel = LogLevel.VERBOSE;
    }
    else if (quiet) {
      _logLevel = LogLevel.WARN;
    }

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

  private String possiblyHandleArgValue(Deque<String> deque) {
    String value = deque.peek();
    if (value == null || value.startsWith("-")) {
      return null;
    }
    return deque.poll();
  }
}
