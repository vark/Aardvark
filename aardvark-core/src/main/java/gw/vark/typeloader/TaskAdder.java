package gw.vark.typeloader;

import gw.lang.GosuShop;
import gw.lang.reflect.ParameterInfoBuilder;
import gw.lang.reflect.module.IModule;
import gw.util.GosuExceptionUtil;
import org.apache.tools.ant.IntrospectionHelper;
import org.apache.tools.ant.Task;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
*/
class TaskAdder extends TaskMethod {
  TaskAdder(String helperKey, Class<?> type, IModule module) {
    super(helperKey, type, module);
  }

  @Override
  String buildParamName() {
    return _helperKey + "List";
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
        helper.getElementMethod(_helperKey).invoke(taskInstance, argListArg);
      } catch (IllegalAccessException e) {
        throw GosuExceptionUtil.forceThrow(e);
      } catch (InvocationTargetException e) {
        throw GosuExceptionUtil.forceThrow(e);
      }
    }
  }
}
