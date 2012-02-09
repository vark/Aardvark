package gw.vark;

import gw.config.CommonServices;
import gw.lang.launch.ArgInfo.IProgramSource;
import gw.lang.parser.GosuParserFactory;
import gw.lang.parser.IGosuProgramParser;
import gw.lang.parser.IParseResult;
import gw.lang.parser.ITypeUsesMap;
import gw.lang.parser.ParserOptions;
import gw.lang.parser.StandardSymbolTable;
import gw.lang.parser.exceptions.ParseResultsException;
import gw.lang.reflect.IType;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.gs.IGosuProgram;
import gw.lang.reflect.gs.IProgramInstance;
import gw.util.GosuExceptionUtil;
import gw.util.StreamUtil;
import gw.vark.annotations.Depends;
import gw.vark.typeloader.AntlibTypeLoader;
import gw.vark.util.Stopwatch;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 */
public class AardvarkProgram {

  public static AardvarkProgram parseWithTimer(IProgramSource programSource) throws ParseResultsException
  {
    Stopwatch stopwatch = new Stopwatch();
    stopwatch.start();
    Aardvark.getProject().log("Parsing Aardvark buildfile...", Project.MSG_VERBOSE);

    AardvarkProgram program = parse(programSource);

    stopwatch.stop();
    Aardvark.getProject().log("Done parsing Aardvark buildfile in " + stopwatch.getElapsedInMS() + " ms");
    return program;
  }

  public static AardvarkProgram parse(IProgramSource programSource) throws ParseResultsException
  {
    try {
      Reader reader = StreamUtil.getInputStreamReader(programSource.openInputStream());
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

      AardvarkProgram gosuProgram = new AardvarkProgram(result.getProgram());
      return gosuProgram;
    } catch (FileNotFoundException e) {
      throw GosuExceptionUtil.forceThrow(e);
    } catch (IOException e) {
      throw GosuExceptionUtil.forceThrow(e);
    }
  }

  public static List<String> getDefaultTypeUsesPackages()
  {
    return Arrays.asList(Depends.class.getPackage().getName() + ".*", AntlibTypeLoader.GW_VARK_TASKS_PACKAGE + "*");
  }

  private final IGosuProgram _gosuProgram;
  private IProgramInstance _programInstance;
  private List<Target> _runtimeGeneratedTargets = new ArrayList<Target>();

  AardvarkProgram(IGosuProgram gosuProgram) {
    _gosuProgram = gosuProgram;
  }

  void maybeEvaluate() {
    if (_programInstance == null) {
      _programInstance = _gosuProgram.getProgramInstance();
      _programInstance.evaluate(null);
    }
  }

  IGosuProgram get() {
    return _gosuProgram;
  }

  IProgramInstance getProgramInstance() {
    return _programInstance;
  }

  public List<Target> getRuntimeGeneratedTargets() {
    return _runtimeGeneratedTargets;
  }
}
