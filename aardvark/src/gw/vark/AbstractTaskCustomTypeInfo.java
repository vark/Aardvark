package gw.vark;

import gw.lang.function.IFunction1;
import gw.lang.parser.ISymbol;
import gw.lang.reflect.IMethodCallHandler;
import gw.lang.reflect.IPropertyAccessor;
import gw.lang.reflect.IType;
import gw.lang.reflect.MethodInfoBuilder;
import gw.lang.reflect.ParameterInfoBuilder;
import gw.lang.reflect.PropertyInfoBuilder;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.java.CustomTypeInfoBase;
import gw.lang.reflect.java.IJavaType;
import gw.util.GosuExceptionUtil;
import org.apache.tools.ant.IntrospectionHelper;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.EnumeratedAttribute;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public abstract class AbstractTaskCustomTypeInfo extends CustomTypeInfoBase {
  public AbstractTaskCustomTypeInfo(IType owner) {
    super(owner);
    addProperty(new PropertyInfoBuilder()
            .withStatic()
            .withName("INSTANCE")
            .withType(getOwnersType())
            .withAccessor(new IPropertyAccessor() {
              @Override
              public Object getValue(Object ctx) {
                return getOwnerInstance();
              }

              @Override
              public void setValue(Object ctx, Object value) {
                throw new UnsupportedOperationException();
              }
            })
            .build(this)
    );
  }

  protected abstract Object getOwnerInstance();

  protected final void addTasksAsMethods(Map<String, Class<? extends Task>> tasks) {
    for (Map.Entry<String, Class<? extends Task>> task : tasks.entrySet()) {
      try {
        addTaskAsMethod(task.getKey(), task.getValue());
      } catch (NoClassDefFoundError e) {
        //System.err.println("caught NCDFE while getting methods for " + taskClass);
      }
    }
  }

  protected final void addTaskAsMethod(String taskName, Class<? extends Task> taskClass) {
    //System.out.println("Adding " + taskClass);
    TaskMethods taskMethods = processTaskMethods(taskClass);

    addMethod(new MethodInfoBuilder()
            .withName(taskName)
            .withParameters(taskMethods.getParameterInfoBuilders())
            .withCallHandler(new TaskMethodCallHandler(taskName, taskClass, taskMethods))
            .build(this));
  }

  private static TaskMethods processTaskMethods(Class<? extends Task> taskClass) {
    IntrospectionHelper helper = IntrospectionHelper.getHelper(taskClass);
    TaskMethods taskMethods = new TaskMethods(taskClass);
    for (Enumeration en = helper.getAttributes(); en.hasMoreElements();) {
      String attributeName = (String) en.nextElement();
      taskMethods.add(new TaskSetter(attributeName, helper.getAttributeType(attributeName)));
    }
    for (Enumeration en = helper.getNestedElements(); en.hasMoreElements();) {
      String elementName = (String) en.nextElement();
      Method elementMethod = helper.getElementMethod(elementName);
      if (elementMethod.getName().startsWith("add") && elementMethod.getParameterTypes().length == 1) {
        taskMethods.add(new TaskAdder(elementName, helper.getElementType(elementName)));
      }
      else {
        taskMethods.add(new TaskCreator(elementName, helper.getElementType(elementName)));
      }
    }
    return taskMethods;
  }

  private static class TaskMethods {
    private final Class<? extends Task> _taskClass;
    private final TreeMap<String, TaskMethod> _methodMap = new TreeMap<String, TaskMethod>();

    private TaskMethods(Class<? extends Task> taskClass) {
      _taskClass = taskClass;
    }

    void add(TaskMethod taskMethod) {
      String paramName = taskMethod.getParamName();
      if (_methodMap.containsKey(paramName)) {
        throw new IllegalArgumentException("cannot add a duplicate param name \"" + paramName + "\" for task class " + _taskClass.getName());
      }
      _methodMap.put(paramName, taskMethod);
    }

    ParameterInfoBuilder[] getParameterInfoBuilders() {
      List<ParameterInfoBuilder> builders = new ArrayList<ParameterInfoBuilder>();
      for (TaskMethod taskMethod : _methodMap.values()) {
        builders.add(taskMethod.createParameterInfoBuilder());
      }
      return builders.toArray(new ParameterInfoBuilder[_methodMap.size()]);
    }

    ArrayList<TaskMethod> asList() {
      return new ArrayList<TaskMethod>(_methodMap.values());
    }
  }

  private static abstract class TaskMethod {
    protected final String _helperKey;
    protected final Class _type;

    protected TaskMethod(String helperKey, Class type) {
      _helperKey = helperKey;
      _type = type;
    }

    abstract String getParamName();
    abstract ParameterInfoBuilder createParameterInfoBuilder();
    abstract void invoke(Task taskInstance, Object arg, IntrospectionHelper helper);
  }

  private static class TaskSetter extends TaskMethod {
    private enum TypeCategory {
      PRIMITIVE,
      ENUM,
      PLAIN
    }
    private TypeCategory _typeCategory;

    TaskSetter(String helperKey, Class type) {
      super(helperKey, type);
      if (type.isPrimitive()) {
        _typeCategory = TypeCategory.PRIMITIVE;
      }
      else if (EnumeratedAttribute.class.isAssignableFrom(type)) {
        _typeCategory = TypeCategory.ENUM;
      }
      else {
        _typeCategory = TypeCategory.PLAIN;
      }
    }

    @Override
    String getParamName() {
      return _helperKey;
    }

    @Override
    ParameterInfoBuilder createParameterInfoBuilder() {
      return new ParameterInfoBuilder()
              .withName(getParamName())
              .withType(makeParamType(_type))
              .withDefValue(ISymbol.NULL_DEFAULT_VALUE);
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
          }
          catch (Exception e) {
            Aardvark.getProject().log("could not find generated enum type for " + enumName + " - must use EnumeratedAttribute instance instead", Project.MSG_VERBOSE);
          }
          _typeCategory = TypeCategory.PLAIN;
          // fall through
        case PLAIN:
        default:
          return TypeSystem.get(clazz);
      }
    }
  }

  private static class TaskAdder extends TaskMethod {
    TaskAdder(String helperKey, Class type) {
      super(helperKey, type);
    }

    @Override
    String getParamName() {
      return _helperKey + "List";
    }

    @Override
    ParameterInfoBuilder createParameterInfoBuilder() {
      return new ParameterInfoBuilder()
                .withName(getParamName())
                .withType(makeListType(_type))
                .withDefValue(ISymbol.NULL_DEFAULT_VALUE);
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

    static IType makeListType(Class parameterType) {
      return IJavaType.LIST.getParameterizedType( TypeSystem.get( parameterType ) );
    }
  }

  private static class TaskCreator extends TaskMethod {
    TaskCreator(String helperKey, Class type) {
      super(helperKey, type);
    }

    @Override
    String getParamName() {
      return _helperKey + "Blocks";
    }

    @Override
    ParameterInfoBuilder createParameterInfoBuilder() {
      return new ParameterInfoBuilder()
                .withName(getParamName())
                .withType(makeListOfBlocksType(_type))
                .withDefValue(ISymbol.NULL_DEFAULT_VALUE);
    }

    @Override
    void invoke(Task taskInstance, Object arg, IntrospectionHelper helper) {
      for (Object argListArg : (List) arg) {
        Object created = helper.getElementCreator(null, "", taskInstance, _helperKey, null).create();
        IFunction1 f = (IFunction1) argListArg;
        f.invoke(created);
      }
    }

    static IType makeListOfBlocksType(Class parameterType) {
      return TypeSystem.parseTypeLiteral("java.util.List<block(" + parameterType.getName() + ")>");
    }
  }

  private static class TaskMethodCallHandler implements IMethodCallHandler {
    private String _taskName;
    private final Class<? extends Task> _taskClass;
    private final TaskMethods _taskMethods;

    public TaskMethodCallHandler(String taskName, Class<? extends Task> taskClass, TaskMethods taskMethods) {
      _taskName = taskName;
      _taskClass = taskClass;
      _taskMethods = taskMethods;
    }

    @Override
    public Object handleCall(Object ctx, Object... args) {
      try {
        // see ComponentHelper.createComponent(UnknownElement, String, String)
        IntrospectionHelper helper = IntrospectionHelper.getHelper(_taskClass);
        Task taskInstance = _taskClass.newInstance();
        taskInstance.setProject(Aardvark.getProject());
        taskInstance.setTaskName(_taskName);
        taskInstance.init();
        ArrayList<TaskMethod> taskMethods = _taskMethods.asList();
        for (int i = 0; i < args.length; i++) {
          if (args[i] != null) {
            taskMethods.get(i).invoke(taskInstance, args[i], helper);
          }
        }
        taskInstance.execute();
      } catch (IllegalAccessException e) { // should never happen
        throw GosuExceptionUtil.forceThrow(e);
      } catch (InstantiationException e) { // should not happen - we know we can instantiate the task
        throw GosuExceptionUtil.forceThrow(e);
      }
      return null;
    }
  }
}
