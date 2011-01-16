/*
 * Copyright (c) 2011 Guidewire Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gw.vark;

import gw.lang.Scriptable;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.java.CustomJavaTypeInfo;
import gw.util.GosuExceptionUtil;
import gw.util.StreamUtil;
import org.apache.tools.ant.Task;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CustomJavaTypeInfo(IvyTasks.CustomTypeInfo.class)
@Scriptable
public class IvyTasks {

  public static class CustomTypeInfo extends AbstractTaskCustomTypeInfo {
    private final IvyTasks _instance = new IvyTasks();

    public CustomTypeInfo() {
      super(TypeSystem.get(IvyTasks.class));
      addTasksAsMethods(getTasks());
    }

    @Override
    protected Object getOwnerInstance() {
      return _instance;
    }

    private HashMap<String, Class<? extends Task>> getTasks() {
      HashMap<String, Class<? extends Task>> tasks = new HashMap<String, Class<? extends Task>>();
      for (Map.Entry entry : readAntlib().entrySet()) {
        try {
          String taskName = (String) entry.getKey();
          String taskClassName = (String) entry.getValue();
          //noinspection unchecked
          Class<? extends Task> taskClass = (Class<? extends Task>) Class.forName(taskClassName);
          tasks.put(taskName, taskClass);
        } catch (ClassNotFoundException e) {
          //System.err.println("Class not found for task " + taskClassName);
        } catch (NoClassDefFoundError e) {
          //System.err.println("caught NCDFE while loading " + taskClassName);
        }
      }
      return tasks;
    }

    private HashMap<String, String> readAntlib() {
      // meh, too lazy to find an XML parser
      HashMap<String, String> tasks = new HashMap<String, String>();
      URL antlibResource = Thread.currentThread().getContextClassLoader().getResource("org/apache/ivy/ant/antlib.xml");
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
  }
}
