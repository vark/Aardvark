package gw.vark.typeloader;

import gw.lang.GosuShop;
import gw.lang.reflect.ParameterInfoBuilder;
import gw.util.GosuExceptionUtil;
import org.apache.tools.ant.IntrospectionHelper;
import org.apache.tools.ant.Task;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
*/
class CustomTaskMethod extends TaskMethod {
  private final String _paramName;
  private final Method _method;

  CustomTaskMethod(Class type, String paramName, Method method) {
    super(null, type);
    _paramName = paramName;
    _method = method;
  }

  @Override
  String getParamName() {
    return _paramName;
  }

  @Override
  ParameterInfoBuilder createParameterInfoBuilder() {
    return new ParameterInfoBuilder()
            .withName(getParamName())
            .withType(makeListType(_type))
            .withDefValue(GosuShop.getNullExpressionInstance());
  }

  @Override
  void invoke(Task taskInstance, Object arg, IntrospectionHelper helper) {
    for (Object argListArg : (List) arg) {
      try {
        _method.invoke(taskInstance, argListArg);
      } catch (IllegalAccessException e) {
        throw GosuExceptionUtil.forceThrow(e);
      } catch (InvocationTargetException e) {
        throw GosuExceptionUtil.forceThrow(e);
      }
    }
  }
}
