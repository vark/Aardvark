package gw.vark.typeloader;

import gw.lang.reflect.IType;
import gw.lang.reflect.ParameterInfoBuilder;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.java.JavaTypes;
import gw.util.concurrent.LocklessLazyVar;
import org.apache.tools.ant.IntrospectionHelper;
import org.apache.tools.ant.Task;

/**
 */
abstract class TaskMethod implements Comparable<TaskMethod> {
  protected final String _helperKey;
  protected final Class _type;
  private String _paramName;

  TaskMethod(String helperKey, Class type) {
    _helperKey = helperKey;
    _type = type;
  }

  final String getParamName() {
    if (_paramName == null) {
      _paramName = buildParamName();
    }
    return _paramName;
  }
  abstract String buildParamName();
  abstract ParameterInfoBuilder createParameterInfoBuilder();
  abstract void invoke(Task taskInstance, Object arg, IntrospectionHelper helper);

  static IType makeListType(Class parameterType) {
    return JavaTypes.LIST().getParameterizedType( TypeSystem.get(parameterType) );
  }

  @Override
  public int compareTo(TaskMethod that) {
    return this.getParamName().compareTo(that.getParamName());
  }

  public String toString() {
    return getParamName() + "(" + getClass().getSimpleName() + ")(" + _type.getName() + ")";
  }
}
