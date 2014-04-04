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

package gw.vark.task;

import gw.config.Registry;
import gw.lang.Gosu;
import gw.vark.Aardvark;
import gw.vark.init.VarkClassPathThing;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

public class GosuInitTask extends Task {

  private Path _classpath;

  public Path createClasspath() {
    if (_classpath == null) {
      _classpath = new Path(getProject());
    }
    return _classpath.createPath();
  }

  public Path getClasspath() {
    return _classpath;
  }

  @Override
  public void execute() throws BuildException {
    if (getClass().getClassLoader() instanceof AntClassLoader) {
      AntClassLoader loader = (AntClassLoader) getClass().getClassLoader();
      new VarkClassPathThing().initAntClassLoader(loader);
    }

    Aardvark.setProject(getProject(), null);

    List<File> cp = deriveClasspath();

    if (_classpath != null) {
      for (String pathElement : _classpath.list()) {
        if (new File(pathElement).exists()) {
          cp.add(new File(pathElement));
        } else {
          getProject().log("path element does not exist: " + pathElement, Project.MSG_WARN);
        }
      }
    }

    Registry.setLocation(GosuInitTask.class.getResource("/gw/vark/init/registry.xml"));

    String prop = System.getProperty("java.class.path");
    StringBuilder newProp = new StringBuilder(prop);
    for (File file : cp) {
      newProp.append(File.pathSeparatorChar).append(file.getAbsolutePath());
    }

    // XXX: to make our typeloader to load...
    System.setProperty("java.class.path", newProp.toString());
    Gosu.init(cp);
    System.setProperty("java.class.path", prop);
  }

  private List<File> deriveClasspath() {
    AntClassLoader loader = (AntClassLoader) getClass().getClassLoader();

    String cpString = loader.getClasspath();
    StringTokenizer st = new StringTokenizer(cpString, File.pathSeparator);

    List<File> cp = new ArrayList<File>();
    while (st.hasMoreTokens()) {
      cp.add(new File(st.nextToken()));
    }
    return cp;
  }
}
