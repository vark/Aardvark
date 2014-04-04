package gw.vark.codegen.internal;

import com.guidewire.codegen.CGUtil;
import com.guidewire.codegen.declarations.CGGosuParameter;
import com.guidewire.codegen.declarations.CGParameter;
import com.guidewire.codegen.expressions.CGBlock;
import com.guidewire.codegen.types.CGType;

import static com.guidewire.codegen.CGUtil.*;

public class TaskSetter implements TaskMethod {

  private final String _name;
  private final Class<?> _type;
  private final String _setterMethod;
  private boolean _enum;

  public TaskSetter(String attributeName, Class<?> type, String setterMethod,
                    boolean isEnum) {
    _name = makeLegalShortName(attributeName);
    _type = type;
    _setterMethod = setterMethod;
    _enum = isEnum;
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public CGParameter generateParameter() {
    CGType setterType;
    if (_enum) {
      String name = _type.getName();
      int pos = name.lastIndexOf('.');
      name = pos == -1 ? name : name.substring(pos + 1);
      setterType = type("gw.vark.enums." + name.replace('$', '_'));
    } else {
      setterType = type(_type).boxify();
    }
    return new CGGosuParameter(makeLegalShortName(_name), setterType, _null());
  }

  @Override
  public void generateBody(CGBlock block) {
    if (_enum) {
      CGType type = type(_type);
      CGParameter e = new CGParameter("e", type);
      block.add(CGUtil.var(e, CGUtil._new(type)));
      block.add(invoke(e, "setValue")).arg(raw(_name + ".Val"));
      block.add(CGUtil.invoke(raw("taskInstance"), _setterMethod).arg(e));
    } else {
      block.add(CGUtil.invoke(raw("taskInstance"), _setterMethod).arg(raw(_name)));
    }
  }
}
