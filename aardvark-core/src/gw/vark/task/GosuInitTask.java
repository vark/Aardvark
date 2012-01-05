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

package gw.vark.task;

import gw.lang.shell.Gosu;
import gw.vark.Aardvark;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
    Aardvark.setProject(getProject());

    List<File> existingPathElements = new ArrayList<File>();

    ClassLoader classLoader = getClass().getClassLoader();
    AntClassLoader antClassLoader = (AntClassLoader) classLoader;

    System.setProperty( "gw.module.path", antClassLoader.getClasspath() );

    String[] classpath = antClassLoader.getClasspath().split(System.getProperty("path.separator"));
    for (String pathElement : classpath) {
      existingPathElements.add(new File(pathElement));
    }
    for (String pathElement : _classpath.list()) {
      if (new File(pathElement).exists()) {
        existingPathElements.add(new File(pathElement));
      }
    }

    Gosu.init( null, existingPathElements );
  }
}
