package gw.vark;

import gw.lang.cli.Args;
import gw.lang.cli.CommandLineAccess;
import gw.lang.cli.ShortName;
import gw.lang.cli.LongName;

import java.util.*;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.LogLevel;

public class AardvarkOptions
{
  private boolean _initialized = false;
  private List<String> _argsToInitialize = new ArrayList<String>();
  private String[] _args;
  private boolean _projectHelp;
  private boolean _verify;
  private LogLevel _logLevel = LogLevel.INFO;
  private List<String> _targets;
  private Map<String, String> _definedProps = new HashMap<String, String>();

  private boolean _bootstrapHelp = false;
  private boolean _bootstrapVersion = false;
  private String _bootstrapLogger = null;
  private String _bootstrapFile = null;

  AardvarkOptions(String... rawArgs) {
    for (int i = 0; i < rawArgs.length; i++) {
      if (rawArgs[i].equals("-h") || rawArgs[i].equals("--help")) {
        _bootstrapHelp = true;
      }
      else if (rawArgs[i].equals("--version")) {
        _bootstrapVersion = true;
      }
      else if (rawArgs[i].equals("--logger")) {
        if (i == rawArgs.length - 1 || rawArgs[i+1].startsWith("-")) {
          throw new IllegalArgumentException("\"" + rawArgs[i] + "\" is expected to be followed by a parameter");
        }
        _bootstrapLogger = rawArgs[++i];
      }
      else if (rawArgs[i].equals("-f") || rawArgs[i].equals("--file")) {
        if (i == rawArgs.length - 1 || rawArgs[i+1].startsWith("-")) {
          throw new IllegalArgumentException("\"" + rawArgs[i] + "\" is expected to be followed by a parameter");
        }
        _bootstrapFile = rawArgs[++i];
      }
      else if (rawArgs[i].startsWith("-D")) {
        i = handleArgDefine(rawArgs, i);
      }
      else {
        _argsToInitialize.add(rawArgs[i]);
      }
    }
  }

  private void assertInitialized() {
    if (!_initialized) {
      throw new IllegalStateException("must be initialized");
    }
  }

  void initialize() {
    CommandLineAccess.setRawArgs( _argsToInitialize );
    CommandLineAccess.initialize( this, true );
    _targets = Arrays.asList(getArgs());
    _initialized = true;
  }

  public boolean isBootstrapHelp() {
    return _bootstrapHelp;
  }

  public boolean isBootstrapVersion() {
    return _bootstrapVersion;
  }

  public String getBootstrapLogger() {
    return _bootstrapLogger;
  }

  public String getBootstrapFile() {
    return _bootstrapFile;
  }

  @LongName(name = "verify")
  public boolean isVerify() {
    assertInitialized();
    return _verify;
  }

  @SuppressWarnings({"UnusedDeclaration"})
  @LongName(name = "verify")
  public void setVerify(boolean verify) {
    _verify = verify;
  }

  @ShortName(name = "p")
  @LongName(name = "projecthelp")
  public boolean isHelp() {
    assertInitialized();
    return _projectHelp;
  }

  @SuppressWarnings({"UnusedDeclaration"})
  @ShortName(name = "p")
  @LongName(name = "projecthelp")
  public void setHelp(boolean help) {
    _projectHelp = help;
  }

  public LogLevel getLogLevel() {
    assertInitialized();
    return _logLevel;
  }

  @SuppressWarnings({"UnusedDeclaration"})
  @ShortName(name = "q")
  @LongName(name = "quiet")
  public boolean isQuiet() {
    assertInitialized();
    return _logLevel == LogLevel.WARN;
  }

  @SuppressWarnings({"UnusedDeclaration"})
  @ShortName(name = "q")
  @LongName(name = "quiet")
  public void setQuiet(boolean quiet) {
    if (quiet) {
      _logLevel = LogLevel.WARN;
    }
  }

  @SuppressWarnings({"UnusedDeclaration"})
  @ShortName(name = "v")
  @LongName(name = "verbose")
  public boolean isVerbose() {
    assertInitialized();
    return _logLevel == LogLevel.VERBOSE;
  }

  @SuppressWarnings({"UnusedDeclaration"})
  @ShortName(name = "v")
  @LongName(name = "verbose")
  public void setVerbose(boolean verbose) {
    if (verbose) {
      _logLevel = LogLevel.VERBOSE;
    }
  }

  @SuppressWarnings({"UnusedDeclaration"})
  @ShortName(name = "d")
  @LongName(name = "debug")
  public boolean isDebug() {
    assertInitialized();
    return _logLevel == LogLevel.DEBUG;
  }

  @SuppressWarnings({"UnusedDeclaration"})
  @ShortName(name = "d")
  @LongName(name = "debug")
  public void setDebug(boolean debug) {
    if (debug) {
      _logLevel = LogLevel.DEBUG;
    }
  }

  @SuppressWarnings({"UnusedDeclaration"})
  @Args
  public void setArgs( String[] args )
  {
    _args = args;
  }

  @Args
  public String[] getArgs() {
    return _args;
  }

  List<String> getTargets() {
    assertInitialized();
    return _targets;
  }

  Map<String, String> getDefinedProps() {
    return _definedProps;
  }

  /* Handle -D argument */
  private int handleArgDefine(String[] args, int argPos) {
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
    String arg = args[argPos];
    String name = arg.substring(2, arg.length());
    String value;
    int posEq = name.indexOf("=");
    if (posEq > 0) {
      value = name.substring(posEq + 1);
      name = name.substring(0, posEq);
    } else if (argPos < args.length - 1) {
      value = args[++argPos];
    } else {
      throw new BuildException("Missing value for property " + name);
    }
    _definedProps.put(name, value);
    return argPos;
  }

}
