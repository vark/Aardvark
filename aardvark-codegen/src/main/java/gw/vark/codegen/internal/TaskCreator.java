package gw.vark.codegen.internal;

import com.guidewire.codegen.CGUtil;
import com.guidewire.codegen.declarations.CGGosuParameter;
import com.guidewire.codegen.declarations.CGParameter;
import com.guidewire.codegen.expressions.CGBlock;
import com.guidewire.codegen.expressions.CGForEachExpr;
import com.guidewire.codegen.types.CGType;

import java.util.List;

import static com.guidewire.codegen.CGUtil.*;
import static com.guidewire.codegen.CGUtil.invoke;

public class TaskCreator implements TaskMethod {

  private final String _name;
  private final Class<?> _type;
  private final String _createMethod;

  public TaskCreator(String elementName, Class<?> type, String createMethod) {
    _name = makeLegalShortName(elementName + "Blocks");
    _type = type;
    _createMethod = createMethod;
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public CGParameter generateParameter() {
    CGType blockType = CGUtil.blockType(null, new CGParameter("val", _type));
    CGType type = type(List.class).narrow(blockType);
    return new CGGosuParameter(_name, type, _null());
  }

  @Override
  public void generateBody(CGBlock block) {
    CGType type = type(_type);
    CGParameter blk = new CGParameter("blk", type);
    CGForEachExpr foreach = CGUtil.foreach(blk, raw(_name));
    block.add(foreach);
    foreach.body().add(invoke(blk).arg(invoke(raw("taskInstance"), _createMethod)));
  }
}
