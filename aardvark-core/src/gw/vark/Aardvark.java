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

import gw.config.CommonServices;
import gw.lang.Gosu;
import gw.lang.mode.GosuMode;
import gw.lang.mode.RequiresInit;
import gw.lang.parser.GosuParserFactory;
import gw.lang.parser.IExpression;
import gw.lang.parser.IGosuProgramParser;
import gw.lang.parser.IParseResult;
import gw.lang.parser.ITypeUsesMap;
import gw.lang.parser.ParserOptions;
import gw.lang.parser.StandardSymbolTable;
import gw.lang.parser.exceptions.ParseResultsException;
import gw.lang.reflect.IMethodInfo;
import gw.lang.reflect.IOptionalParamCapable;
import gw.lang.reflect.IParameterInfo;
import gw.lang.reflect.IType;
import gw.lang.reflect.TypeSystem;
import gw.util.GosuExceptionUtil;
import gw.util.GosuStringUtil;
import gw.util.Pair;
import gw.util.StreamUtil;
import gw.vark.annotations.Depends;
import gw.vark.typeloader.AntlibTypeLoader;
import gw.vark.util.Stopwatch;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.ExitStatusException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.util.ClasspathUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

// TODO - gosu - expose system properties from ArgInfo?
// TODO - gosu - better help support
// TODO - gosu - pass in a default program source from gosulaunch.properties
// TODO - gosu - a way for us to add tools.jar into the bootstrap classpath
// TODO - find way to set default vark file if none is given at command line
@RequiresInit
public class Aardvark extends GosuMode
{
  public static final int GOSU_MODE_PRIORITY_AARDVARK_HELP = 0;
  public static final int GOSU_MODE_PRIORITY_AARDVARK_VERSION = 1;
  public static final int GOSU_MODE_PRIORITY_AARDVARK_INTERACTIVE = 2;
  public static final int GOSU_MODE_PRIORITY_AARDVARK = 3;

  private static final String DEFAULT_BUILD_FILE_NAME = "build.vark";
  private static Project PROJECT_INSTANCE;
  private static GosuProgramWrapper GOSU_PROGRAM_INSTANCE;

  static final int EXITCODE_VARKFILE_NOT_FOUND = 4;
  static final int EXITCODE_GOSU_VERIFY_FAILED = 8;

  private static String RAW_VARK_FILE_PATH = "";

  public static Project getProject() {
    if (PROJECT_INSTANCE == null) {
      PROJECT_INSTANCE = new Project();
    }
    return PROJECT_INSTANCE;
  }

  public static void setProject(Project project) {
    PROJECT_INSTANCE = project;
  }

  public static String getRawVarkFilePath() {
    return RAW_VARK_FILE_PATH;
  }

  public static GosuProgramWrapper getGosuProgram() {
    return GOSU_PROGRAM_INSTANCE;
  }

  private static void setGosuProgram(GosuProgramWrapper gosuProgram) {
    GOSU_PROGRAM_INSTANCE = gosuProgram;
  }

  private Project _project;
  private BuildLogger _logger;

  // this is a convenience when working in a dev environment when we might not want to use the Launcher
  public static void main( String... args ) throws Exception {
    Gosu.main(args);
  }

  public Aardvark() {
    this(new DefaultLogger());
  }

  Aardvark(BuildLogger logger) {
    resetProject(logger);
  }

  @Override
  public int getPriority() {
    return GOSU_MODE_PRIORITY_AARDVARK;
  }

  @Override
  public boolean accept() {
    return true;
  }

  @Override
  public int run() throws Exception {
    RAW_VARK_FILE_PATH = _argInfo.getProgramSource().getValue();

    AardvarkOptions options = new AardvarkOptions(_argInfo);
    File varkFile;
    GosuProgramWrapper gosuProgram;

    if (options.getLogger() != null) {
      newLogger(options.getLogger());
    }

    if ("true".equals(System.getProperty("aardvark.dev"))) {
      log("aardvark.dev is on");
      AntlibTypeLoader loader = new AntlibTypeLoader(TypeSystem.getCurrentModule());
      TypeSystem.pushTypeLoader(loader);
    }

    varkFile = _argInfo.getProgramSource().getFile();
    log("Buildfile: " + varkFile);

      try {
        gosuProgram = parseAardvarkProgramWithTimer(varkFile);
      }
      catch (ParseResultsException e) {
        logErr(e.getMessage());
        return EXITCODE_GOSU_VERIFY_FAILED;
      }

      int exitCode = 1;
      try {
        try {
          runBuild(varkFile, gosuProgram, options);
          exitCode = 0;
        } catch (ExitStatusException ese) {
          exitCode = ese.getStatus();
          if (exitCode != 0) {
            throw ese;
          }
        }
      } catch (BuildException e) {
        //printMessage(e); // (logger should have displayed the message along with "BUILD FAILED"
      } catch (Throwable e) {
        e.printStackTrace();
        printMessage(e);
      }
      return exitCode;
  }

  public void resetProject(BuildLogger logger) {
    _project = new Project();
    setLogger(logger != null ? logger : _logger);
    setProject(_project);
  }

  public void runBuild(File varkFile, GosuProgramWrapper gosuProgram, AardvarkOptions options) throws BuildException {
    Throwable error = null;

    _logger.setMessageOutputLevel(options.getLogLevel().getLevel());

    try {
      if ( !options.isHelp() ) {
        _project.fireBuildStarted();
      }

      _project.init();

      // set user-define properties
      for (Map.Entry<String, String> prop : options.getDefinedProps().entrySet()) {
        String arg = prop.getKey();
        String value = prop.getValue();
        _project.setUserProperty(arg, value);
      }

      _project.setBaseDir(varkFile.getParentFile());
      ProjectHelper.configureProject(_project, gosuProgram, options.getTargetCalls());

      if ( options.isHelp() ) {
        log(getHelp(varkFile.getPath(), gosuProgram.get()));
        return;
      }

      Vector<String> targets = new Vector<String>();

      if (options.getTargetCalls().size() > 0) {
        targets.addAll(options.getTargets());
      }
      else if (_project.getDefaultTarget() != null) {
        targets.add(_project.getDefaultTarget());
      }

      if (targets.size() == 0) {
        logErr("No targets to run");
      }
      else {
        _project.executeTargets(targets);
      }
    } catch (RuntimeException e) {
      error = e;
      throw e;
    } catch (Error e) {
      error = e;
      throw e;
    } finally {
      if ( !options.isHelp() ) {
        _project.fireBuildFinished(error);
      }
    }
  }

  private void printMessage(Throwable t) {
    String message = t.getMessage();
    if (message != null) {
      logErr(message);
    }
  }

/*
  private File findVarkFile( String fileFromArgs ) throws IOException {
    File varkFile;
    RAW_VARK_FILE_PATH = fileFromArgs;
    if( fileFromArgs != null )
    {
      if( fileFromArgs.startsWith("http://") || fileFromArgs.startsWith("https://") )
      {
        varkFile = downloadToFile( new URL( fileFromArgs ) );
      }
      else {
        varkFile = new File( fileFromArgs );
      }
      if ( !varkFile.exists() )
      {
        throw new FileNotFoundException( "Specified vark buildfile \"" + fileFromArgs + "\" doesn't exist" );
      }
    }
    else {
      varkFile = new File( DEFAULT_BUILD_FILE_NAME );
      if ( !varkFile.exists() )
      {
        throw new FileNotFoundException( "Default vark buildfile " + DEFAULT_BUILD_FILE_NAME + " doesn't exist" );
      }
    }
    try {
      return varkFile.getCanonicalFile();
    } catch (IOException e) {
      logWarn("Could not get canonical file (" + varkFile.getPath() + ") - using absolute file instead.");
      return varkFile.getAbsoluteFile();
    }
  }

  private File downloadToFile( URL url ) throws IOException {
    File file = File.createTempFile("build", ".vark");
    URLConnection urlConnection = url.openConnection();
    urlConnection.setDoOutput(true);
    urlConnection.connect();
    InputStream inputStream = urlConnection.getInputStream();
    ReadableByteChannel inCh = Channels.newChannel(inputStream);
    FileOutputStream outputStream = new FileOutputStream(file);
    WritableByteChannel outCh = Channels.newChannel(outputStream);
    ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
    while (inCh.read(buffer) != -1) {
      buffer.flip();
      outCh.write(buffer);
      buffer.compact();
    }
    buffer.flip();
    while (buffer.hasRemaining()) {
      outCh.write(buffer);
    }
    inCh.close();
    outCh.close();
    return file;
  }
*/

  public static String getHelp( String varkFilePath, IType gosuProgram )
  {
    StringBuilder help = new StringBuilder();
    help.append( "\nValid targets in " ).append( varkFilePath ).append( ":\n" ).append( "\n" );
    List<Pair<String, String>> nameDocPairs = new ArrayList<Pair<String, String>>();
    int maxLen = 0;
    for( IMethodInfo methodInfo : gosuProgram.getTypeInfo().getMethods() )
    {
      if( isTargetMethod(gosuProgram, methodInfo) && methodInfo.getDescription() != null) // don't display targets with no doc (Ant behavior)
      {
        String name = ProjectHelper.camelCaseToHyphenated(methodInfo.getDisplayName());
        maxLen = Math.max( maxLen, name.length() );
        String description = methodInfo.getDescription();
        if (!methodInfo.getOwnersType().equals(gosuProgram)) {
          description += "\n  [in " + methodInfo.getOwnersType().getName() + "]";
        }
        IParameterInfo[] parameters = methodInfo.getParameters();
        for (int i = 0, parametersLength = parameters.length; i < parametersLength; i++) {
          IParameterInfo param = parameters[i];
          description += "\n  -" + param.getName();
          if (methodInfo instanceof IOptionalParamCapable) {
            IExpression defaultValue = ((IOptionalParamCapable) methodInfo).getDefaultValueExpressions()[i];
            if (defaultValue != null) {
              description += " (optional, default " + defaultValue.evaluate() + ")";
            }
          }
          if (GosuStringUtil.isNotBlank(param.getDescription())) {
            description += ": " + param.getDescription();
          }
        }
        nameDocPairs.add( Pair.make( name, description) );
      }
    }

    for( Pair<String, String> nameDocPair : nameDocPairs )
    {
      String name = nameDocPair.getFirst();
      String command = "  " + name + GosuStringUtil.repeat( " ", maxLen - name.length() ) + " -  ";
      int start = command.length();
      String docs = nameDocPair.getSecond();
        Iterator<String> iterator = Arrays.asList( docs.split( "\n" ) ).iterator();
        if( iterator.hasNext() )
        {
          command += iterator.next();
        }
        while( iterator.hasNext() )
        {
          command += "\n" + GosuStringUtil.repeat( " ", start ) + iterator.next();
        }
      help.append( command ).append("\n");
    }

    help.append( "\nFEED THE VARK!" ).append("\n");
    return help.toString();
  }

  public static boolean isTargetMethod(IType gosuProgram, IMethodInfo methodInfo) {
    return methodInfo.isPublic()
            && (methodInfo.hasAnnotation(TypeSystem.get(gw.vark.annotations.Target.class))
                    || (methodInfo.getParameters().length == 0 && methodInfo.getOwnersType().equals( gosuProgram )));
  }

  private GosuProgramWrapper parseAardvarkProgramWithTimer( File varkFile ) throws ParseResultsException
  {
    Stopwatch stopwatch = new Stopwatch();
    stopwatch.start();
    logVerbose("Parsing Aardvark buildfile...");

    GosuProgramWrapper program = parseAardvarkProgram(varkFile);

    stopwatch.stop();
    log("Done parsing Aardvark buildfile in " + stopwatch.getElapsedInMS() + " ms");
    return program;
  }

  public static GosuProgramWrapper parseAardvarkProgram( File varkFile ) throws ParseResultsException
  {
    try {
      String content = StreamUtil.getContent( new FileReader( varkFile ) );

      IGosuProgramParser programParser = GosuParserFactory.createProgramParser();
      List<String> packages = getDefaultTypeUsesPackages();
      ITypeUsesMap typeUses = CommonServices.getGosuIndustrialPark().createTypeUsesMap(packages);
      for( String aPackage : packages )
      {
        typeUses.addToDefaultTypeUses( aPackage );
      }
      ParserOptions options = new ParserOptions().withTypeUsesMap(typeUses).withSuperType(getAardvarkFileBaseClass());
      IParseResult result = programParser.parseExpressionOrProgram( content, new StandardSymbolTable( true ), options );

      GosuProgramWrapper gosuProgram = new GosuProgramWrapper(result.getProgram());
      setGosuProgram(gosuProgram);
      return gosuProgram;
    } catch (FileNotFoundException e) {
      throw GosuExceptionUtil.forceThrow(e);
    } catch (IOException e) {
      throw GosuExceptionUtil.forceThrow(e);
    }
  }

  public static IType getAardvarkFileBaseClass()
  {
    return TypeSystem.getByFullName( "gw.vark.AardvarkFile" );
  }

  private static List<File> getSystemClasspath()
  {
    ArrayList<File> files = new ArrayList<File>();
    for( String file : System.getProperty( "java.class.path" ).split( File.pathSeparator ) )
    {
      files.add( new File( file ) );
    }
    return files;
  }

  private void newLogger(String loggerClassName) {
    try {
      BuildLogger newLogger = (BuildLogger) ClasspathUtils.newInstance(loggerClassName, Aardvark.class.getClassLoader(), BuildLogger.class);
      setLogger(newLogger);
    }
    catch (BuildException e) {
      logErr("The specified logger class " + loggerClassName + " could not be used because " + e.getMessage());
      throw e;
    }
  }

  private void setLogger(BuildLogger logger) {
    logger.setMessageOutputLevel( Project.MSG_INFO );
    logger.setOutputPrintStream(System.out);
    logger.setErrorPrintStream(System.err);
    _project.removeBuildListener(_logger);
    _logger = logger;
    _project.addBuildListener(logger);
  }

  public static List<String> getDefaultTypeUsesPackages()
  {
    return Arrays.asList( Depends.class.getPackage().getName() + ".*", AntlibTypeLoader.GW_VARK_TASKS_PACKAGE + "*" );
  }

  private void log(String message) {
    _project.log(message);
  }

  private void logVerbose(String message) {
    _project.log(message, Project.MSG_VERBOSE);
  }

  private void logWarn(String message) {
    _project.log(message, Project.MSG_WARN);
  }

  private void logErr(String message) {
    _project.log(message, Project.MSG_ERR);
  }

  public static String getVersion() {
    URL versionResource = Aardvark.class.getResource("/gw/vark/version.txt");
    try {
      Reader reader = StreamUtil.getInputStreamReader(versionResource.openStream());
      String version = StreamUtil.getContent(reader).trim();
      return "Aardvark version " + version;
    } catch (IOException e) {
      throw GosuExceptionUtil.forceThrow(e);
    }
  }
}
