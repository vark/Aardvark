package gw.vark;

import gw.lang.reflect.gs.IGosuProgram;
import gw.lang.reflect.gs.IProgramInstance;

/**
 * Created by IntelliJ IDEA.
 * User: bchang
 * Date: 8/23/11
 * Time: 2:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class GosuProgramWrapper {

  private final IGosuProgram _gosuProgram;
  private IProgramInstance _programInstance;

  GosuProgramWrapper(IGosuProgram gosuProgram) {
    _gosuProgram = gosuProgram;
    _programInstance = null;
  }

  public void reset() {
    _programInstance = null;
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
}
