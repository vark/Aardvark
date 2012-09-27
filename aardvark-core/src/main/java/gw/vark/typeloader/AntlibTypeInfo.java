/*
 * Copyright (c) 2012 Guidewire Software, Inc.
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

import gw.lang.reflect.ConstructorInfoBuilder;
import gw.lang.reflect.IConstructorHandler;
import gw.lang.reflect.IMethodCallHandler;
import gw.lang.reflect.IType;
import gw.lang.reflect.MethodInfoBuilder;
import gw.lang.reflect.ParameterInfoBuilder;
import gw.lang.reflect.java.CustomTypeInfoBase;
import gw.util.GosuExceptionUtil;
import gw.util.Pair;
import gw.vark.Aardvark;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.IntrospectionHelper;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.ProjectHelperRepository;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.UnknownElement;
import org.apache.tools.ant.taskdefs.Antlib;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.URLResource;
import org.apache.tools.ant.util.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

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
    List<Pair<String, String>> listing;
    if (resourceName.endsWith(".properties")) {
      listing = readTaskListingFromPropertiesFile(resourceName);
    } else if (resourceName.endsWith(".xml")) {
      listing = readTaskListingFromAntlib(resourceName);
    } else {
      throw new IllegalArgumentException("resourceName must have suffix .resource or .xml");
    }

    for (Pair<String, String> entry : listing) {
      String taskName = entry.getFirst();
      String taskClassName = entry.getSecond();
      addTaskAsMethod(taskName, taskClassName);
    }
  }

  private static List<Pair<String, String>> readTaskListingFromPropertiesFile(String resourceName) {
    URL url = TypeSystemUtil.getResource(resourceName);
    InputStream in = null;
    try {
      in = url.openStream();
      if (in == null) {
        AntlibTypeLoader.log("Could not load definitions from " + url, Project.MSG_WARN);
      }
      Properties tasks = new Properties();
      tasks.load(in);
      List<Pair<String, String>> listing = new ArrayList<Pair<String, String>>();
      for (Map.Entry<Object, Object> entry : tasks.entrySet()) {
        listing.add(new Pair<String, String>((String) entry.getKey(), (String) entry.getValue()));
      }
      return listing;
    } catch (IOException e) {
      throw GosuExceptionUtil.forceThrow(e);
    } finally {
      FileUtils.close(in);
    }
  }

  private static List<Pair<String, String>> readTaskListingFromAntlib(String resourceName) {
    URL antlibUrl = TypeSystemUtil.getResource(resourceName);
    URLResource antlibResource = new URLResource(antlibUrl);
    ProjectHelperRepository helperRepository = ProjectHelperRepository.getInstance();
    ProjectHelper parser = helperRepository.getProjectHelperForAntlib(antlibResource);
    UnknownElement ue = parser.parseAntlibDescriptor(AntlibTypeLoader.NULL_PROJECT, antlibResource);
    if (!ue.getTag().equals(Antlib.TAG)) {
      throw new BuildException("Unexpected tag " + ue.getTag() + " expecting " + Antlib.TAG, ue.getLocation());
    }

    List<Pair<String, String>> listing = new ArrayList<Pair<String, String>>();
    for (Object childObj : ue.getChildren()) {
      UnknownElement child = (UnknownElement) childObj;
      if (child.getTag().equals("taskdef")) {
        Map attributes = child.getWrapper().getAttributeMap();
        listing.add(new Pair<String, String>((String)attributes.get("name"), (String)attributes.get("classname")));
      }
    }
    return listing;
  }

  protected final void addTaskAsMethod(String taskName, String taskClassName) {
    MethodInfoBuilder methodInfoBuilder = new MethodInfoBuilder()
            .withName(taskName)
            .withStatic();

    try {
      Class<? extends Task> taskClass = TypeSystemUtil.getTaskClass(taskClassName);
      TaskMethods taskMethods = processTaskMethods(taskClass);
      methodInfoBuilder
              .withReturnType(taskClass)
              .withParameters(taskMethods.getParameterInfoBuilders())
              .withCallHandler(new TaskMethodCallHandler(taskName, taskClass, taskMethods));
    } catch (ClassNotFoundException cnfe) {
      badTask(taskName, methodInfoBuilder, cnfe);
    } catch (NoClassDefFoundError ncdfe) {
      badTask(taskName, methodInfoBuilder, ncdfe);
    }

    addMethod(methodInfoBuilder.build(this));
  }

  private void badTask(String taskName, MethodInfoBuilder methodInfoBuilder, Throwable t) {
    String message = "Task " + taskName + " is unavailable, due to " + t;
    AntlibTypeLoader.log(message, Project.MSG_VERBOSE);
    methodInfoBuilder.withDescription(message);
  }

  private static TaskMethods processTaskMethods(Class<? extends Task> taskClass) {
    TypeSystemIntrospectionHelper helper = TypeSystemIntrospectionHelper.getHelper(taskClass);
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
    for (Object methodObj : helper.getExtensionPoints()) {
      Method method = (Method) methodObj;
      if (method.getName().equals("add") && method.getParameterTypes().length == 1 && method.getParameterTypes()[0] == ResourceCollection.class) {
        taskMethods.add(new CustomTaskMethod(ResourceCollection.class, "resources", method));
        break;
      }
    }
    return taskMethods;
  }

  private static class TaskMethods {
    private final Class<? extends Task> _taskClass;
    // TODO: get rid of use of TreeMap
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
        return taskInstance;
      } catch (IllegalAccessException e) { // should never happen
        throw GosuExceptionUtil.forceThrow(e);
      } catch (InstantiationException e) { // should not happen - we know we can instantiate the task
        throw GosuExceptionUtil.forceThrow(e);
      }
    }
  }
}
