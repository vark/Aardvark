package gw.vark.codegen.internal;

import com.guidewire.codegen.CGUtil;
import com.guidewire.codegen.declarations.CGGosuParameter;
import com.guidewire.codegen.declarations.CGParameter;
import com.guidewire.codegen.expressions.CGBlock;
import com.guidewire.codegen.expressions.CGForEachExpr;
import com.guidewire.codegen.types.CGType;

import java.util.List;

import static com.guidewire.codegen.CGUtil.*;

public class TaskAdder implements TaskMethod {

  private final String _name;
  private final Class<?> _type;
  private final String _addMethod;

  public TaskAdder(String elementName, Class<?> type, String addMethod) {
    _name = makeLegalShortName(elementName + "List");
    _type = type;
    _addMethod = addMethod;
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public CGParameter generateParameter() {
    CGType type = type(List.class).narrow(type(_type));
    return new CGGosuParameter(_name, type, _null());
  }

  @Override
  public void generateBody(CGBlock block) {
    CGType type = type(_type);
    CGParameter val = new CGParameter("val", type);
    CGForEachExpr foreach = CGUtil.foreach(val, raw(_name));
    block.add(foreach);
    foreach.body().add(invoke(raw("taskInstance"), _addMethod).arg(val));
  }
}
