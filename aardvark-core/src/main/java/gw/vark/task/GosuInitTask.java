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

import gw.lang.Gosu;
import gw.vark.Aardvark;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

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
    Aardvark.setProject(getProject(), null);

    List<File> cp = deriveClasspath();

    for (String pathElement : _classpath.list()) {
      if (new File(pathElement).exists()) {
        cp.add(new File(pathElement));
      }
      else {
        getProject().log("path element does not exist: " + pathElement, Project.MSG_WARN);
      }
    }

    Gosu.init( cp );
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
