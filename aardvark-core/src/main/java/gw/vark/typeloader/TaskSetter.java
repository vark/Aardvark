package gw.vark.typeloader;

import gw.lang.GosuShop;
import gw.lang.reflect.IType;
import gw.lang.reflect.ParameterInfoBuilder;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.module.IModule;
import org.apache.tools.ant.IntrospectionHelper;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.EnumeratedAttribute;

/**
 * Created with IntelliJ IDEA.
 * User: bchang
 * Date: 9/27/12
 * Time: 11:27 AM
 * To change this template use File | Settings | File Templates.
 */
class TaskSetter extends TaskMethod {
  private enum TypeCategory {
    PRIMITIVE,
    ENUM,
    PLAIN
  }

  private TypeCategory _typeCategory;

  TaskSetter(String helperKey, Class<?> type, IModule module, IIntrospectionHelper helper) {
    super(helperKey, type, module);
    if (type.isPrimitive()) {
      _typeCategory = TypeCategory.PRIMITIVE;
    } else {
      _typeCategory = TypeCategory.PLAIN;

      // Check if it is an enumerated attribute
      try {
        Class<?> clazz = helper.loadClass(EnumeratedAttribute.class.getName());
        if (clazz.isAssignableFrom(type)) {
          _typeCategory = TypeCategory.ENUM;
        }
      } catch (ClassNotFoundException ex) {
        AntlibTypeLoader.log("could not load proper EnumeratedAttribute.class", Project.MSG_VERBOSE);
      }
    }
  }

  @Override
  String buildParamName() {
    return _helperKey;
  }

  @Override
  ParameterInfoBuilder createParameterInfoBuilder() {
    return new ParameterInfoBuilder()
            .withName(getParamName())
            .withType(makeParamType(_type))
            .withDefValue(GosuShop.getNullExpressionInstance());
  }

  @Override
  void invoke(Task taskInstance, Object arg, IntrospectionHelper helper) {
    if (TypeCategory.ENUM == _typeCategory) {
      arg = EnumeratedAttribute.getInstance(_type, arg.toString().toLowerCase());
    }
    helper.setAttribute(null, taskInstance, _helperKey, arg);
  }

  IType makeParamType(Class clazz) {
    switch (_typeCategory) {
      case PRIMITIVE:
        return TypeSystem.getBoxType(TypeSystem.get(clazz));
      case ENUM:
        String enumName = TypeSystem.get(_type).getRelativeName().replace('.', '_');
        try {
          return TypeSystem.getByFullName("gw.vark.enums." + enumName);
        } catch (Exception e) {
          AntlibTypeLoader.log("could not find generated enum type for " + enumName + " - must use EnumeratedAttribute instance instead", Project.MSG_VERBOSE);
        }
        _typeCategory = TypeCategory.PLAIN;
        // fall through
      case PLAIN:
      default:
        return TypeSystem.get(clazz);
    }
  }
}
