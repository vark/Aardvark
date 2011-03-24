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

package gw.vark.testapi;

import org.apache.tools.ant.launch.Locator;

import java.io.File;

/**
 */
public class TestUtil {

  public static File getHome(Class clazz) {
    File classSource = Locator.getClassSource(clazz);
    return getHome(classSource.isDirectory() ? classSource : classSource.getParentFile()).getAbsoluteFile();
  }

  private static File getHome(File dir) {
    if (new File(dir, "lib/aardvark").exists()) {
      return dir;
    }
    return getHome(dir.getParentFile());
  }

}
