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
import gw.lang.parser.*;
import gw.lang.parser.exceptions.ParseResultsException;
import gw.lang.reflect.*;
import gw.lang.reflect.gs.IGosuProgram;
import gw.lang.shell.Gosu;
import gw.util.GosuExceptionUtil;
import gw.util.GosuStringUtil;
import gw.util.Pair;
import gw.util.StreamUtil;
import gw.vark.annotations.Depends;
import gw.vark.shell.InteractiveShell;
import gw.vark.typeloader.AntlibTypeLoader;
import org.apache.tools.ant.*;
import org.apache.tools.ant.launch.AntMain;
import org.apache.tools.ant.util.ClasspathUtils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.*;

public class Aardvark implements AntMain
{
  private static final String DEFAULT_BUILD_FILE_NAME = "build.vark";
  private static Project PROJECT_INSTANCE;

  static final int EXITCODE_VARKFILE_NOT_FOUND = 4;
  static final int EXITCODE_GOSU_VERIFY_FAILED = 8;

  public static Project getProject()
  {
    return PROJECT_INSTANCE;
  }

  public static void setProject(Project project) {
    PROJECT_INSTANCE = project;
  }

  private Project _project;
  private BuildLogger _logger;

  // this is a convenience when working in a dev environment when we might not want to use the Launcher
  public static void main( String... args ) {
    Aardvark a = new Aardvark();
    a.startAnt(args, null, null);
  }

  public Aardvark() {
    this(new DefaultLogger());
  }

  Aardvark(BuildLogger logger) {
    resetProject(logger);
  }

  @Override
  public void startAnt(String[] args, Properties additionalUserProperties, ClassLoader coreLoader) {
    int exitCode = startAardvark(args);
    System.exit(exitCode);
  }

  public int startAardvark(String[] args) {
    AardvarkOptions options = new AardvarkOptions(args);
    File varkFile;
    IGosuProgram gosuProgram;

    if (options.getLogger() != null) {
      newLogger(options.getLogger());
    }

    if (options.isBootstrapHelp()) {
      printHelp();
      return 0;
    }
    if (options.isVersion()) {
      log("Aardvark version " + getVersion());
      return 0;
    }

    try {
      varkFile = findVarkFile( options.getBuildFile() );
    }
    catch (IOException e) {
      logErr(e.getMessage());
      return EXITCODE_VARKFILE_NOT_FOUND;
    }
    log("Buildfile: " + varkFile);

    initGosu(varkFile);

    if ( options.isVerify() ) {
      List<Gosu.IVerificationResults> verifyResults = Gosu.verifyAllGosu(true, true);
      if (verifyResults.size() > 0) {
        for (Gosu.IVerificationResults results : verifyResults) {
          log("=========================================== " + results.getTypeName());
          log(results.getFeedback());
        }
      }
      return EXITCODE_GOSU_VERIFY_FAILED;
    } else {
      try {
        gosuProgram = parseAardvarkProgramWithTimer(varkFile);
      }
      catch (ParseResultsException e) {
        logErr(e.getMessage());
        return EXITCODE_GOSU_VERIFY_FAILED;
      }

      if (options.isInteractive()) {
        InteractiveShell.start(this, varkFile, gosuProgram);
        return 0;
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
  }

  public void resetProject(BuildLogger logger) {
    _project = new Project();
    setLogger(logger != null ? logger : _logger);
    setProject(_project);
  }

  public void runBuild(File varkFile, IGosuProgram gosuProgram, AardvarkOptions options) throws BuildException {
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
        log(getHelp(varkFile.getPath(), gosuProgram));
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
        printHelp();
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

  private File findVarkFile( String fileFromArgs ) throws IOException {
    File varkFile;
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
          if (methodInfo instanceof IOptionalParamCapable && ((IOptionalParamCapable) methodInfo).getDefaultValues()[i] != null) {
            description += " (optional, default " + ((IOptionalParamCapable) methodInfo).getDefaultValues()[i] + ")";
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

  private IGosuProgram parseAardvarkProgramWithTimer( File varkFile ) throws ParseResultsException
  {
    long parseStart = System.nanoTime();
    logVerbose("Parsing Aardvark buildfile...");

    IGosuProgram program = parseAardvarkProgram(varkFile);

    long parseEnd = System.nanoTime();
    log("Done parsing Aardvark buildfile in " + ((parseEnd - parseStart) / 1000 / 1000) + " ms");
    return program;
  }

  public static IGosuProgram parseAardvarkProgram( File varkFile ) throws ParseResultsException
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

      return result.getProgram();
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

  public static void initGosu(File varkFile) {
    Gosu.init(varkFile, getSystemClasspath());
    if ("true".equals(System.getProperty("aardvark.dev"))) {
      AntlibTypeLoader loader = new AntlibTypeLoader(TypeSystem.getCurrentModule());
      TypeSystem.pushTypeLoader(loader);
    }
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

  private void printHelp() {
    log("Usage: vark [options] target [target2 [target3] ..]");
    log("Options:");
    //log("  --debug, -d                  print debugging info");
    log("  --file <file>                use given buildfile");
    log("     -f  <file>                        ''");
    log("  --help, -h                   print this message and exit");
    log("  --logger <classname>         the class to perform logging");
    log("  --projecthelp, -p            print project help information");
    log("  --quiet, -q                  be extra quiet");
    log("  --verbose, -v                be extra verbose");
    log("  --verify                     verify Gosu code");
    log("  --version                    print the version info and exit");
  }

  public static String getVersion() {
    URL versionResource = Thread.currentThread().getContextClassLoader().getResource("gw/vark/version.txt");
    URL changelistResource = Thread.currentThread().getContextClassLoader().getResource("gw/vark/version-changelist.txt");
    try {
      String version = StreamUtil.getContent(StreamUtil.getInputStreamReader(versionResource.openStream())).trim();
      if (changelistResource != null) {
        String changelist = StreamUtil.getContent(StreamUtil.getInputStreamReader(changelistResource.openStream())).trim();
        version += "." + changelist;
      }
      return version;
    } catch (IOException e) {
      throw GosuExceptionUtil.forceThrow(e);
    }
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
}
