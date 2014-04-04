package gw.vark.codegen.internal;

import com.guidewire.codegen.CGUtil;
import com.guidewire.codegen.declarations.CGGosuParameter;
import com.guidewire.codegen.declarations.CGParameter;
import com.guidewire.codegen.expressions.CGBlock;
import com.guidewire.codegen.expressions.CGForEachExpr;
import com.guidewire.codegen.types.CGType;
import org.apache.tools.ant.types.ResourceCollection;

import java.util.List;

import static com.guidewire.codegen.CGUtil.*;
import static com.guidewire.codegen.CGUtil.invoke;
import static com.guidewire.codegen.CGUtil.type;

public class TaskCustom implements TaskMethod {

  private final String _name;
  private final String _methodName;

  public TaskCustom(String methodName) {
    _name = makeLegalShortName("resources");
    _methodName = methodName;
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public CGParameter generateParameter() {
    CGType type = type(List.class).narrow(type(ResourceCollection.class));
    return new CGGosuParameter(_name, type, _null());
  }

  @Override
  public void generateBody(CGBlock block) {
    CGType type = type(ResourceCollection.class);
    CGParameter val = new CGParameter("val", type);
    CGForEachExpr foreach = CGUtil.foreach(val, raw(_name));
    block.add(foreach);
    foreach.body().add(invoke(raw("taskInstance"), _methodName).arg(val));
  }
}
