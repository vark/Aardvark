package gw.vark;

import gw.config.CommonServices;
import gw.lang.launch.ArgInfo;
import gw.lang.launch.ArgInfo.IProgramSource;
import gw.lang.parser.*;
import gw.lang.parser.exceptions.ParseResultsException;
import gw.lang.reflect.*;
import gw.lang.reflect.gs.IGosuProgram;
import gw.lang.reflect.gs.IProgramInstance;
import gw.lang.reflect.java.JavaTypes;
import gw.util.GosuExceptionUtil;
import gw.util.GosuStringUtil;
import gw.util.Pair;
import gw.util.StreamUtil;
import gw.vark.annotations.Depends;
import gw.vark.typeloader.AntlibTypeLoader;
import gw.vark.util.Stopwatch;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;

import java.io.*;
import java.util.*;

/**
 */
public class AardvarkProgram {
  private static final String REF_ID = AardvarkProgram.class.getName();

  public static AardvarkProgram getInstance(Project project) {
    return (AardvarkProgram) project.getReference(REF_ID);
  }

  public static AardvarkProgram parseWithTimer(Project project, IProgramSource programSource) throws ParseResultsException, FileNotFoundException
  {
    Stopwatch stopwatch = new Stopwatch();
    stopwatch.start();
    project.log("Parsing Aardvark buildfile...", Project.MSG_VERBOSE);

    AardvarkProgram program = parse(project, programSource);

    stopwatch.stop();
    project.log("Done parsing Aardvark buildfile in " + stopwatch.getElapsedInMS() + " ms");
    return program;
  }

  public static AardvarkProgram parse(Project project, IProgramSource programSource) throws ParseResultsException, FileNotFoundException
  {
    try {
      Reader reader = StreamUtil.getInputStreamReader(programSource.openInputStream());
      File baseDir;
      if (programSource.getFile() != null) {
        baseDir = programSource.getFile().getParentFile();
      }
      else {
        baseDir = new File(".");
      }
      return parse(project, baseDir, reader);
    }
    catch (FileNotFoundException e) {
      if (programSource instanceof ArgInfo.DefaultLocalProgramSource) {
        throw new FileNotFoundException("Default vark buildfile " + Aardvark.DEFAULT_BUILD_FILE_NAME + " doesn't exist");
      }
      else {
        throw new FileNotFoundException("Specified vark buildfile " + programSource.getRawPath() + " doesn't exist");
      }
    }
    catch (IOException e) {
      throw GosuExceptionUtil.forceThrow(e);
    }
  }

  public static AardvarkProgram parse(Project project, File varkFile) throws ParseResultsException
  {
    try {
      Reader reader = StreamUtil.getInputStreamReader(new FileInputStream(varkFile));
      return parse(project, varkFile.getParentFile(), reader);
    }
    catch (IOException e) {
      throw GosuExceptionUtil.forceThrow(e);
    }
  }

  public static AardvarkProgram parse(Project project, File baseDir, Reader reader) throws ParseResultsException {
    try {
      String content = StreamUtil.getContent(reader);

      IGosuProgramParser programParser = GosuParserFactory.createProgramParser();
      List<String> packages = getDefaultTypeUsesPackages();
      ITypeUsesMap typeUses = CommonServices.getGosuIndustrialPark().createTypeUsesMap(packages);
      for( String aPackage : packages )
      {
        typeUses.addToDefaultTypeUses( aPackage );
      }
      IType supertype = TypeSystem.getByFullName("gw.vark.AardvarkFile");
      ParserOptions options = new ParserOptions().withTypeUsesMap(typeUses).withSuperType(supertype);
      IParseResult result = programParser.parseExpressionOrProgram( content, new StandardSymbolTable( true ), options );

      AardvarkProgram aardvarkProgram = new AardvarkProgram(project, baseDir, result.getProgram());
      project.addReference(REF_ID, aardvarkProgram);
      return aardvarkProgram;
    } catch (IOException e) {
      throw GosuExceptionUtil.forceThrow(e);
    }
  }

  public static List<String> getDefaultTypeUsesPackages()
  {
    return Arrays.asList(Depends.class.getPackage().getName() + ".*", AntlibTypeLoader.GW_VARK_TASKS_PACKAGE + "*");
  }

  private final Project _project;
  private final File _baseDir;
  private final IGosuProgram _gosuProgram;
  private IProgramInstance _programInstance;
  private List<Target> _runtimeGeneratedTargets = new ArrayList<Target>();

  AardvarkProgram(Project project, File baseDir, IGosuProgram gosuProgram) {
    _project = project;
    _baseDir = baseDir.getAbsoluteFile();
    _gosuProgram = gosuProgram;
  }

  void maybeEvaluate() {
    if (_programInstance == null) {
      _programInstance = _gosuProgram.getProgramInstance();
      _programInstance.evaluate(null);
    }
  }

  public List<Target> getRuntimeGeneratedTargets() {
    return _runtimeGeneratedTargets;
  }

  public File getBaseDir() {
    return _baseDir;
  }

  public void runBuild(LinkedHashMap<String, TargetCall> targetCalls) throws BuildException {
    Throwable error = null;

    try {
      _project.fireBuildStarted();

      _project.init();

      _project.setBaseDir(_baseDir);
      try
      {
        maybeEvaluate();
        addTargets(_project, targetCalls);
      }
      catch( Exception e )
      {
        throw new BuildException(e);
      }

      Vector<String> targets = new Vector<String>();

      if (targetCalls.size() > 0) {
        targets.addAll(targetCalls.keySet());
      }
      else if (_project.getDefaultTarget() != null) {
        targets.add(_project.getDefaultTarget());
      }

      if (targets.size() == 0) {
        _project.log("No targets to run", Project.MSG_ERR);
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
      _project.fireBuildFinished(error);
    }
  }

  public void printProjectHelp() {
    _project.init();
    _project.setBaseDir(_baseDir);
    _project.log(getHelp(_gosuProgram));
  }

  public static String getHelp( IType gosuProgram )
  {
    StringBuilder help = new StringBuilder();
    help.append("\nValid targets:\n\n");
    List<Pair<String, String>> nameDocPairs = new ArrayList<Pair<String, String>>();
    int maxLen = 0;
    for( IMethodInfo methodInfo : gosuProgram.getTypeInfo().getMethods() )
    {
      if( Aardvark.isTargetMethod(gosuProgram, methodInfo) && methodInfo.getDescription() != null) // don't display targets with no doc (Ant behavior)
      {
        String name = camelCaseToHyphenated(methodInfo.getDisplayName());
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

    help.append( "\nFEED THE VARK!" );
    return help.toString();
  }

  private void addTargets( Project project, Map<String, TargetCall> targetCalls )
  {
    List<Target> targets = new ArrayList<Target>(getRuntimeGeneratedTargets());

    for ( final IMethodInfo methodInfo : _gosuProgram.getTypeInfo().getMethods() )
    {
      if ( Aardvark.isTargetMethod(_gosuProgram, methodInfo) )
      {
        String rawTargetName = stripParens(methodInfo.getName());
        String hyphenatedTargetName = camelCaseToHyphenated(rawTargetName);

        TargetCall targetCall = targetCalls.get(rawTargetName);
        if (targetCall == null) {
          targetCall = targetCalls.get(hyphenatedTargetName);
        }

        AardvarkTarget target = new AardvarkTarget(methodInfo, _programInstance, targetCall);
        target.setProject( project );
        target.setName( hyphenatedTargetName );
        target.setDescription( methodInfo.getDescription() );

        IAnnotationInfo dependsAnnotation = methodInfo.getAnnotation( TypeSystem.get( Depends.class ) );
        if (dependsAnnotation != null) {
          Depends dependsAnnotationValue = (Depends) dependsAnnotation.getInstance();
          String[] dependencies = dependsAnnotationValue.value();
          for ( String dependencyTarget : dependencies ) {
            target.addDependency( camelCaseToHyphenated(dependencyTarget) );
          }
        }

        targets.add(target);

        if (!rawTargetName.equals(hyphenatedTargetName)) {
          Target camelcaseTarget = new Target();
          camelcaseTarget.setName(rawTargetName);
          camelcaseTarget.addDependency(hyphenatedTargetName);
          project.addTarget(camelcaseTarget);
        }
      }
    }

    for (Target target : targets) {
      project.addTarget(target);
    }
  }

  private static String stripParens(String str) {
    int openParenIdx = str.lastIndexOf("(");
    return str.substring(0, openParenIdx);
  }

  private static boolean hasUpperCase(String str) {
    for (int i = 0; i < str.length(); i++) {
      if (Character.isUpperCase(str.charAt(i))) {
        return true;
      }
    }
    return false;
  }

  static String camelCaseToHyphenated(String str) {
    if (hasUpperCase(str)) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < str.length(); i++) {
        char c = str.charAt(i);
        if (Character.isUpperCase(c)) {
          sb.append('-');
          sb.append(Character.toLowerCase(c));
        }
        else {
          sb.append(c);
        }
      }
      return sb.toString();
    }
    else {
      return str;
    }
  }

  private static class AardvarkTarget extends Target {
     private final IMethodInfo _methodInfo;
     private final IProgramInstance _gosuProgramInstance;
     private final TargetCall _targetCall;

     AardvarkTarget(IMethodInfo methodInfo, IProgramInstance gosuProgramInstance, TargetCall targetCall) {
       _methodInfo = methodInfo;
       _gosuProgramInstance = gosuProgramInstance;
       _targetCall = targetCall;
     }

     @Override
     public void execute() throws BuildException {
       int argArraySize = _methodInfo.getOwnersType() instanceof IGosuProgram ? 1 : 0;
       int offset = argArraySize;
       argArraySize += _methodInfo.getParameters().length;
       Object[] args = new Object[argArraySize];
       Map<String, String> userParams = _targetCall != null ? _targetCall.getParams() : Collections.<String, String>emptyMap();
       IParameterInfo[] parameters = _methodInfo.getParameters();
       for (int i = 0, parametersLength = parameters.length; i < parametersLength; i++) {
         IParameterInfo paramInfo = parameters[i];
         if (paramInfo.getFeatureType().equals(JavaTypes.STRING())) {
           args[offset + i] = determineStringParamVal(paramInfo.getName(), userParams, i);
         }
         else if (paramInfo.getFeatureType().equals(JavaTypes.pBOOLEAN()) || paramInfo.getFeatureType().equals(JavaTypes.BOOLEAN())) {
           args[offset + i] = determineBooleanParamVal(paramInfo.getName(), userParams, i);
         }
         else if (paramInfo.getFeatureType().equals(JavaTypes.pINT()) || paramInfo.getFeatureType().equals(JavaTypes.INTEGER())) {
           args[offset + i] = determineIntParamVal(paramInfo.getName(), userParams, i);
         }
         else {
           throw new IllegalArgumentException("type " + paramInfo.getFeatureType() + " for \"" + paramInfo.getName() + "\" not supported");
         }
       }
       if (userParams.size() > 0) {
         throw new IllegalArgumentException("no parameter named \"" + userParams.keySet().iterator().next() + "\"");
       }
       _methodInfo.getCallHandler().handleCall(_gosuProgramInstance, args);
     }

     private Object determineStringParamVal(String paramName, Map<String, String> userParams, int i) {
       boolean hasUserParam = userParams.containsKey(paramName);
       if (hasUserParam) {
         String userValue = userParams.remove(paramName);
         if (userValue == null) {
           throw new IllegalArgumentException("\"" + paramName + "\" is expected to be followed by a value");
         }
         return userValue;
       }
       else {
         IExpression defaultValue = ((IOptionalParamCapable)_methodInfo).getDefaultValueExpressions()[i];
         if (defaultValue == null) {
           throw new IllegalArgumentException("requires parameter \"" + paramName + "\"");
         }
         return defaultValue.evaluate();
       }
     }

     private Object determineBooleanParamVal(String paramName, Map<String, String> userParams, int i) {
       boolean hasUserParam = userParams.containsKey(paramName);
       if (hasUserParam) {
         String userValue = userParams.remove(paramName);
         if (userValue == null) {
           return true;
         }
         else if (userValue.equals("true")) {
           return Boolean.TRUE;
         }
         else if (userValue.equals("false")) {
           return Boolean.FALSE;
         }
         else {
           throw new IllegalArgumentException("\"" + paramName + "\" value is expected to be a boolean, was \"" + userValue + "\"");
         }
       }
       else {
         IExpression defaultValue = ((IOptionalParamCapable)_methodInfo).getDefaultValueExpressions()[i];
         if (defaultValue == null) {
           return false;
         }
         return defaultValue.evaluate();
       }
     }

     private Object determineIntParamVal(String paramName, Map<String, String> userParams, int i) {
       boolean hasUserParam = userParams.containsKey(paramName);
       if (hasUserParam) {
         String userValue = userParams.remove(paramName);
         if (userValue == null) {
           throw new IllegalArgumentException("\"" + paramName + "\" is expected to be followed by a value");
         }
         try {
           return new Integer(userValue);
         } catch (NumberFormatException e) {
           throw new IllegalArgumentException("\"" + paramName + "\" value is expected to be an int, was \"" + userValue + "\"");
         }
       }
       else {
         IExpression defaultValue = ((IOptionalParamCapable)_methodInfo).getDefaultValueExpressions()[i];
         if (defaultValue == null) {
           throw new IllegalArgumentException("requires parameter \"" + paramName + "\"");
         }
         return defaultValue.evaluate();
       }
     }
  }
}
