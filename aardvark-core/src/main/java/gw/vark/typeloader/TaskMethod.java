package gw.vark.typeloader;

import gw.lang.reflect.IType;
import gw.lang.reflect.ParameterInfoBuilder;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.java.JavaTypes;
import org.apache.tools.ant.IntrospectionHelper;
import org.apache.tools.ant.Task;

/**
 */
abstract class TaskMethod {
  protected final String _helperKey;
  protected final Class _type;

  TaskMethod(String helperKey, Class type) {
    _helperKey = helperKey;
    _type = type;
  }

  abstract String getParamName();
  abstract ParameterInfoBuilder createParameterInfoBuilder();
  abstract void invoke(Task taskInstance, Object arg, IntrospectionHelper helper);

  static IType makeListType(Class parameterType) {
    return JavaTypes.LIST().getParameterizedType( TypeSystem.get(parameterType) );
  }
}
