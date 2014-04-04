package gw.vark.codegen.internal;

import com.guidewire.codegen.declarations.CGParameter;
import com.guidewire.codegen.expressions.CGBlock;

public interface TaskMethod {

  String getName();
  CGParameter generateParameter();
  void generateBody(CGBlock block);
}
