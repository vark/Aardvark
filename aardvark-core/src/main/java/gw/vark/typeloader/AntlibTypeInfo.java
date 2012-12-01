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

import gw.fs.IFile;
import gw.lang.reflect.ConstructorInfoBuilder;
import gw.lang.reflect.IAnnotationInfo;
import gw.lang.reflect.IConstructorHandler;
import gw.lang.reflect.IConstructorInfo;
import gw.lang.reflect.IMethodCallHandler;
import gw.lang.reflect.IMethodInfo;
import gw.lang.reflect.IPropertyInfo;
import gw.lang.reflect.IType;
import gw.lang.reflect.MethodInfoBuilder;
import gw.lang.reflect.MethodList;
import gw.lang.reflect.ParameterInfoBuilder;
import gw.lang.reflect.TypeInfoBase;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class AntlibTypeInfo extends TypeInfoBase {

  private final IConstructorInfo _constructor;
  private final MethodList _methods;
  private final IType _owner;

  public AntlibTypeInfo(IFile resource, IType owner) {
    _owner = owner;
    _methods = initTasks(resource);
    _constructor = new ConstructorInfoBuilder()
            .withConstructorHandler(new IConstructorHandler() {
              @Override
              public Object newInstance(Object... args) {
                return new Object();
              }
            })
            .build(this);
  }

  private MethodList initTasks(IFile resource) {
    List<Pair<String, String>> listing;
    if (resource.getExtension().equals("properties")) {
      listing = readTaskListingFromPropertiesFile(resource);
    } else if (resource.getExtension().equals(".xml")) {
      listing = readTaskListingFromAntlib(resource);
    } else {
      throw new IllegalArgumentException("resourceName must have suffix .resource or .xml");
    }

    MethodList methods = new MethodList();
    for (Pair<String, String> entry : listing) {
      String taskName = entry.getFirst();
      String taskClassName = entry.getSecond();
      IMethodInfo method = createTaskAsMethod(taskName, taskClassName);
      methods.add(method);
    }
    return methods;
  }

  private static List<Pair<String, String>> readTaskListingFromPropertiesFile(IFile resource) {
    InputStream in = null;
    try {
      in = resource.openInputStream();
      if (in == null) {
        AntlibTypeLoader.log("Could not load definitions from " + resource, Project.MSG_WARN);
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

  private static List<Pair<String, String>> readTaskListingFromAntlib(IFile resource) {
    URL url;
    try {
      url = resource.toURI().toURL();
    } catch (MalformedURLException e) {
      throw GosuExceptionUtil.forceThrow(e);
    }
    URLResource antlibResource = new URLResource(url);
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

  protected final IMethodInfo createTaskAsMethod(String taskName, String taskClassName) {
    MethodInfoBuilder methodInfoBuilder = new MethodInfoBuilder()
            .withName(taskName)
            .withStatic();

    try {
      Class<? extends Task> taskClass = TypeSystemUtil.getTaskClass(taskClassName);
      TaskMethod[] taskMethods = processTaskMethods(taskClass);
      methodInfoBuilder
              .withReturnType(taskClass)
              .withParameters(createParameterInfoBuilders(taskMethods))
              .withCallHandler(new TaskCallHandler(taskName, taskClass, taskMethods));
    } catch (ClassNotFoundException cnfe) {
      badTask(taskName, methodInfoBuilder, cnfe);
    } catch (NoClassDefFoundError ncdfe) {
      badTask(taskName, methodInfoBuilder, ncdfe);
    } catch (RuntimeException re) {
      badTask(taskName, methodInfoBuilder, re);
    }

    return methodInfoBuilder.build(this);
  }

  private static ParameterInfoBuilder[] createParameterInfoBuilders(TaskMethod[] taskMethods) {
    ParameterInfoBuilder[] builders = new ParameterInfoBuilder[taskMethods.length];
    for (int i = 0; i < taskMethods.length; i++) {
      builders[i] = taskMethods[i].createParameterInfoBuilder();
    }
    return builders;
  }

  private void badTask(String taskName, MethodInfoBuilder methodInfoBuilder, Throwable t) {
    String message = "Task " + taskName + " is unavailable, due to " + t;
    AntlibTypeLoader.log(message, Project.MSG_VERBOSE);
    methodInfoBuilder.withDescription(message);
  }

  private static TaskMethod[] processTaskMethods(Class<? extends Task> taskClass) {
    List<TaskMethod> taskMethods = new ArrayList<TaskMethod>();
    TypeSystemIntrospectionHelper helper = TypeSystemIntrospectionHelper.getHelper(taskClass);

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

    return sortAndEnsureUnique(taskMethods);
  }

  private static TaskMethod[] sortAndEnsureUnique(List<TaskMethod> taskMethodList) {
    TaskMethod[] taskMethods = taskMethodList.toArray(new TaskMethod[taskMethodList.size()]);
    Arrays.sort(taskMethods);
    if (taskMethods.length > 1) {
      for (int i = 1; i < taskMethods.length; i++) {
        TaskMethod previous = taskMethods[i - 1];
        TaskMethod current = taskMethods[i];
        if (previous.getParamName().equals(current.getParamName())) {
          throw new IllegalStateException("task methods have the same param name - 1:" + previous + ", 2:" + current);
        }
      }
    }
    return taskMethods;
  }

  private static class TaskCallHandler implements IMethodCallHandler {
    private String _taskName;
    private final Class<? extends Task> _taskClass;
    private final TaskMethod[] _taskMethods;

    public TaskCallHandler(String taskName, Class<? extends Task> taskClass, TaskMethod[] taskMethods) {
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
        for (int i = 0; i < args.length; i++) {
          if (args[i] != null) {
            _taskMethods[i].invoke(taskInstance, args[i], helper);
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

  @Override
  public List<? extends IPropertyInfo> getProperties() {
    return Collections.emptyList();
  }

  @Override
  public IPropertyInfo getProperty(CharSequence charSequence) {
    return null;
  }

  @Override
  public CharSequence getRealPropertyName(CharSequence charSequence) {
    return null;
  }

  @Override
  public MethodList getMethods() {
    return _methods;
  }

  @Override
  public List<? extends IConstructorInfo> getConstructors() {
    return Collections.singletonList(_constructor);
  }

  @Override
  public List<IAnnotationInfo> getDeclaredAnnotations() {
    return Collections.emptyList();
  }

  @Override
  public IType getOwnersType() {
    return _owner;
  }
}
