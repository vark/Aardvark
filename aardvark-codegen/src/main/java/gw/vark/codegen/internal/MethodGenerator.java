package gw.vark.codegen.internal;

import com.guidewire.codegen.CGUtil;
import com.guidewire.codegen.declarations.CGMethod;
import com.guidewire.codegen.declarations.CGModifier;
import com.guidewire.codegen.declarations.CGParameter;
import com.guidewire.codegen.expressions.CGBlock;
import com.guidewire.codegen.expressions.CGIfThenElseExpr;
import com.guidewire.codegen.types.CGType;
import org.apache.tools.ant.Task;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import static com.guidewire.codegen.CGUtil.*;

public class MethodGenerator {

  private Introspector _introspector;

  public MethodGenerator(Introspector introspector) {
    _introspector = introspector;
  }

  protected final CGMethod createTaskAsMethod(String taskName, String taskClassName) {
    String methodName = makeLegalShortName(taskName);
    CGType taskType = type(taskClassName);
    CGMethod method = new CGMethod(methodName, taskType, CGModifier.STATIC);
    CGParameter taskInstance = new CGParameter("taskInstance", type(taskClassName));

    CGType aardvark = type("gw.vark.Aardvark");
    CGBlock body = method.body();

    body.add(var(taskInstance, _new(taskType)));
    body.add(invoke(taskInstance, "setProject").arg(invoke(aardvark.ref(), "getProject")));

    if (_introspector.hasTaskNameMethod()) {
      body.add(invoke(taskInstance, "setTaskName").arg(lit(taskName)));
    }
    if (_introspector.hasInitMethod()) {
      body.add(invoke(taskInstance, "init"));
    }

    List<TaskMethod> parameters = new ArrayList<>();
    for (Enumeration en = _introspector.getAttributes(); en.hasMoreElements(); ) {
      String attributeName = (String) en.nextElement();
      Class<?> clazz = _introspector.getAttributeType(attributeName);
      String setterMethod = _introspector.getAttributeMethod(attributeName).getName();
      boolean isEnum = _introspector.isEnumeratedAttribute(clazz);

      parameters.add(new TaskSetter(attributeName, clazz, setterMethod, isEnum));
    }

    for (Enumeration en = _introspector.getNestedElements(); en.hasMoreElements(); ) {
      String elementName = (String) en.nextElement();
      Method elementMethod = _introspector.getElementMethod(elementName);
      Class<?> clazz = _introspector.getElementType(elementName);
      if (elementMethod.getName().startsWith("add") && elementMethod.getParameterTypes().length == 1) {
        parameters.add(new TaskAdder(elementName, clazz, elementMethod.getName()));
      } else {
        parameters.add(new TaskCreator(elementName, clazz, elementMethod.getName()));
      }
    }

    for (Object methodObj : _introspector.getExtensionPoints()) {
      Method addMethod = (Method) methodObj;
      if (addMethod.getName().equals("add") && addMethod.getParameterTypes().length == 1 &&
              _introspector.isResourceCollection(addMethod.getParameterTypes()[0])) {
        parameters.add(new TaskCustom(addMethod.getName()));
        break;
      }
    }

    Collections.sort(parameters, new Comparator<TaskMethod>() {
      @Override
      public int compare(TaskMethod o1, TaskMethod o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });

    for (TaskMethod action : parameters) {
      CGParameter param = action.generateParameter();
      method.addParameter(param);
      CGIfThenElseExpr cond = CGUtil._if(CGUtil.raw(param.getName() + " != null"));
      action.generateBody(cond.then());
      method.body().add(cond);
    }

    body.add(invoke(taskInstance, "execute"));
    body.add(_return(taskInstance.getRef()));
    return method;
  }
}
