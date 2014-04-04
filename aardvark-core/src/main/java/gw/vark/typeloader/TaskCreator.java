package gw.vark.typeloader;

import gw.lang.GosuShop;
import gw.lang.function.IFunction1;
import gw.lang.reflect.IType;
import gw.lang.reflect.ParameterInfoBuilder;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.java.JavaTypes;
import gw.lang.reflect.module.IModule;
import gw.util.GosuExceptionUtil;
import org.apache.tools.ant.IntrospectionHelper;
import org.apache.tools.ant.Task;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
*/
class TaskCreator extends TaskMethod {

  TaskCreator(String helperKey, Class<?> type, IModule module) {
    super(helperKey, type, module);
  }

  @Override
  String buildParamName() {
    return _helperKey + "Blocks";
  }

  @Override
  ParameterInfoBuilder createParameterInfoBuilder() {
    return new ParameterInfoBuilder()
              .withName(getParamName())
              .withType(makeListOfBlocksType(_type))
              .withDefValue(GosuShop.getNullExpressionInstance());
  }

  @Override
  void invoke(Task taskInstance, Object arg, IntrospectionHelper helper) {
    for (Object argListArg : (List) arg) {
      Object created = helper.getElementCreator(null, "", taskInstance, _helperKey, null).create();
      IFunction1 f = (IFunction1) argListArg;
      f.invoke(created);
    }
  }

  private IType makeListOfBlocksType(Class<?> parameterType) {
    //HACK cgross - expose block type factory?
    try {
      Class<?> clazz = Class.forName("gw.internal.gosu.parser.expressions.BlockType");
      Constructor<?> ctor = clazz.getConstructor(IType.class, IType[].class, List.class, List.class);
      IType blkType = (IType) ctor.newInstance(JavaTypes.pVOID(), new IType[]{ TypeSystem.get(parameterType, _module) },
              Arrays.asList("arg"), Collections.<Object>emptyList());
      return JavaTypes.LIST().getGenericType().getParameterizedType(blkType);
    } catch (Exception e) {
      throw GosuExceptionUtil.forceThrow(e);
    }
  }
}
