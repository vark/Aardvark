/*
 * Copyright (c) 2010 Guidewire Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gw.vark.typeloader;

import gw.config.CommonServices;
import gw.lang.function.IFunction1;
import gw.lang.parser.ISymbol;
import gw.lang.reflect.ConstructorInfoBuilder;
import gw.lang.reflect.IConstructorHandler;
import gw.lang.reflect.IMethodCallHandler;
import gw.lang.reflect.IType;
import gw.lang.reflect.MethodInfoBuilder;
import gw.lang.reflect.ParameterInfoBuilder;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.java.CustomTypeInfoBase;
import gw.lang.reflect.java.IJavaType;
import gw.util.GosuExceptionUtil;
import gw.util.StreamUtil;
import gw.vark.Aardvark;
import org.apache.tools.ant.IntrospectionHelper;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.EnumeratedAttribute;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AntlibTypeInfo extends CustomTypeInfoBase {
  public AntlibTypeInfo(String resourceName, IType owner) {
    super(owner);
    initTasks(resourceName);
    addConstructor(new ConstructorInfoBuilder()
            .withConstructorHandler(new IConstructorHandler() {
              @Override
              public Object newInstance(Object... args) {
                return new Object();
              }
            })
            .build(this));
  }

  private void initTasks(String resourceName) {
    Map<String, String> listing;
    if (resourceName.endsWith(".properties")) {
      listing = readTaskListingFromPropertiesFile(resourceName);
    } else if (resourceName.endsWith(".xml")) {
      listing = readTaskListingFromAntlib(resourceName);
    } else {
      throw new IllegalArgumentException("resourceName must have suffix .resource or .xml");
    }

    HashMap<String, Class<? extends Task>> tasks = new HashMap<String, Class<? extends Task>>();
    for (Map.Entry<String, String> entryObj : listing.entrySet()) {
      Map.Entry<String, String> entry = entryObj;
      try {
        String taskName = entry.getKey();
        String taskClassName = entry.getValue();
        //noinspection unchecked
        Class<? extends Task> taskClass = (Class<? extends Task>) Class.forName(taskClassName);
        tasks.put(taskName, taskClass);
      } catch (ClassNotFoundException e) {
        //System.err.println("Class not found for task " + taskClassName);
      } catch (NoClassDefFoundError e) {
        //System.err.println("caught NCDFE while loading " + taskClassName);
      }
    }

    for (Map.Entry<String, Class<? extends Task>> task : tasks.entrySet()) {
      try {
        addTaskAsMethod(task.getKey(), task.getValue());
      } catch (NoClassDefFoundError e) {
        //System.err.println("caught NCDFE while getting methods for " + taskClass);
      }
    }
  }

  private static Map<String, String> readTaskListingFromPropertiesFile(String resourceName) {
    Properties tasks = new Properties();
    URL listingResource = Thread.currentThread().getContextClassLoader().getResource(resourceName);
    InputStream in = null;
    try {
      in = listingResource.openStream();
      tasks.load(in);
      HashMap<String, String> map = new HashMap<String, String>();
      for (Map.Entry<Object, Object> entry : tasks.entrySet()) {
        map.put((String)entry.getKey(), (String)entry.getValue());
      }
      return map;
    } catch (IOException e) {
      throw GosuExceptionUtil.forceThrow(e);
    } finally {
      try {
        StreamUtil.close(in);
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private static Map<String, String> readTaskListingFromAntlib(String resourceName) {
    // meh, too lazy to find an XML parser
    HashMap<String, String> tasks = new HashMap<String, String>();
    URL antlibResource = Thread.currentThread().getContextClassLoader().getResource(resourceName);
    InputStream in = null;
    try {
      in = antlibResource.openStream();
      Reader inReader = new InputStreamReader(in, "UTF-8");
      BufferedReader reader = new BufferedReader(inReader);
      Pattern pattern = Pattern.compile("\\s*<taskdef name=\"([^\"]*)\"* classname=\"([^\"]*)\"*/>");
      for (String line = reader.readLine(); line != null; line = reader.readLine()) {
        Matcher matcher = pattern.matcher(line);
        if (matcher.matches()) {
          tasks.put(matcher.group(1), matcher.group(2));
        }
      }
      return tasks;
    }
    catch (IOException e) {
      throw GosuExceptionUtil.forceThrow(e);
    }
    finally {
      try {
        StreamUtil.close(in);
      }
      catch (IOException e) {
        e.printStackTrace();
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
      //HACK cgross - expose block type factory?
      try {
        Class<?> clazz = Class.forName("gw.internal.gosu.parser.expressions.BlockType");
        Constructor<?> ctor = clazz.getConstructor(IType.class, IType[].class, List.class, List.class);
        IType blkType = (IType) ctor.newInstance(IJavaType.pVOID, new IType[]{TypeSystem.get(parameterType)},
                Arrays.asList("arg"), Collections.<Object>emptyList());
        return IJavaType.LIST.getGenericType().getParameterizedType(blkType);
      } catch (Exception e) {
        throw GosuExceptionUtil.forceThrow(e);
      }
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
