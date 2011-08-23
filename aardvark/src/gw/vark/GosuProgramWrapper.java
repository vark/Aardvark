package gw.vark;

import gw.lang.reflect.gs.IGosuProgram;
import gw.lang.reflect.gs.IProgramInstance;
import org.apache.tools.ant.Target;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class GosuProgramWrapper {

  private final IGosuProgram _gosuProgram;
  private IProgramInstance _programInstance;
  private List<Target> _runtimeGeneratedTargets = new ArrayList<Target>();

  GosuProgramWrapper(IGosuProgram gosuProgram) {
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
