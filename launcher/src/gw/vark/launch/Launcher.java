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

package gw.vark.launch;

import org.apache.tools.ant.launch.Locator;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Launcher {

  /**
   * launch diagnostics flag; for debugging trouble at launch time.
   */
  public static boolean _launchDiag = false;

  public static final String MAIN_CLASS = "gw.vark.Aardvark";
  public static final String VEDIT_CLASS = "gw.vark.editor.VEdit";

  /**
   * Exit code on trouble
   */
  protected static final int EXIT_CODE_ERROR = 2;

  /**
   * Entry point for starting command line Aardvark.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    int exitCode;
    try {
      Launcher launcher = new Launcher();
      exitCode = launcher.run(args);
    } catch (Throwable t) {
      exitCode = EXIT_CODE_ERROR;
      t.printStackTrace(System.err);
    }
    if (exitCode != 0) {
      System.exit(exitCode);
    }
  }

  private int run(String[] args) throws MalformedURLException {
    File sourceJar = Locator.getClassSource(getClass());
    File home = getHome(sourceJar.isDirectory() ? sourceJar : sourceJar.getParentFile());

    logPath("Launcher JAR", sourceJar);
    logPath("Launcher home", home);

    URL[] aardvarkURLs = getAardvarkURLs(home);
    URL[] antURLs = Locator.getLocationURLs(new File(home, "lib" + File.separator + "ant"));
    URL[] gosuURLs = Locator.getLocationURLs(new File(home, "lib" + File.separator + "gosu"));
    URL[] ivyURLs = Locator.getLocationURLs(new File(home, "lib" + File.separator + "ivy"));

    File toolsJar = Locator.getToolsJar();
    logPath("tools.jar", toolsJar);

    URL[] jars = getJarArray(aardvarkURLs, antURLs, gosuURLs, ivyURLs, toolsJar);

    StringBuilder baseClassPath = new StringBuilder();
    for (URL jar : jars) {
      if (baseClassPath.length() > 0) {
        baseClassPath.append(File.pathSeparatorChar);
      }
      baseClassPath.append(Locator.fromURI(jar.toString()));
    }

    setProperty("java.class.path", baseClassPath.toString());

    URLClassLoader loader = new URLClassLoader(jars);
    Thread.currentThread().setContextClassLoader(loader);
    Class mainClass = null;
    int exitCode = 0;
    Throwable thrown = null;
    String className = isVedit(args) ? VEDIT_CLASS : MAIN_CLASS;
    try {
      mainClass = loader.loadClass(className);
      AardvarkMain aardvark = (AardvarkMain) mainClass.newInstance();
      aardvark.start(args);
    } catch (InstantiationException e) {
      System.err.println("Incompatible version of " + className + " detected");
      File mainJar = Locator.getClassSource(mainClass);
      System.err.println("Location of this class " + mainJar);
      thrown = e;
    } catch (ClassNotFoundException e) {
      System.err.println("Failed to locate " + className);
      thrown = e;
    } catch (Throwable t) {
      t.printStackTrace(System.err);
      thrown = t;
    }
    if(thrown != null) {
        System.err.println("Classpath: " + baseClassPath.toString());
        System.err.println("Launcher JAR: " + sourceJar.getAbsolutePath());
        System.err.println("Launcher home: " + home.getAbsolutePath());
        exitCode = EXIT_CODE_ERROR;
    }
    return exitCode;
  }

  private static File getHome(File dir) {
    if (new File(dir, "lib/gosu").exists()) {
      return dir;
    }
    return getHome(dir.getParentFile());
  }

  private static URL[] getAardvarkURLs(File home) throws MalformedURLException {
    if ("true".equals(System.getProperty("aardvark.dev"))) {
      System.out.println("aardvark.dev is set to true - using IDE-compiled classes");
      return new URL[] {
              getClassesURL(home, "out" + File.separator + "production" + File.separator + "launcher"),
              getClassesURL(home, "out" + File.separator + "production" + File.separator + "aardvark")
      };
    }
    URL[] urls = Locator.getLocationURLs(new File(home, "lib"));
    if (urls.length != 2) {
      throw new RuntimeException("could not find aardvark jars");
    }
    return urls;
  }

  private static URL getClassesURL(File home, String relativePath) throws MalformedURLException {
    File classesDir = new File(home, relativePath);
    if (!classesDir.exists()) {
      throw new RuntimeException(classesDir + " does not exist");
    }
    if (!classesDir.isDirectory()) {
      throw new RuntimeException(classesDir + " is not a directory");
    }
    return Locator.fileToURL(classesDir);
  }

  private static URL[] getJarArray(URL[] aardvarkJars, URL[] antJars, URL[] gosuJars, URL[] ivyJars, File toolsJar) throws MalformedURLException {
    List<URL> jars = new ArrayList<URL>();
    jars.addAll(Arrays.asList(aardvarkJars));
    jars.addAll(Arrays.asList(antJars));
    jars.addAll(Arrays.asList(gosuJars));
    jars.addAll(Arrays.asList(ivyJars));

    if (toolsJar != null) {
      jars.add(Locator.fileToURL(toolsJar));
    }
    return jars.toArray(new URL[jars.size()]);
  }

  private void setProperty(String name, String value) {
    if (_launchDiag) {
      System.out.println("Setting \"" + name + "\" to \"" + value + "\"");
    }
    System.setProperty(name, value);
  }

  private void logPath(String name, File path) {
    if(_launchDiag) {
      System.out.println(name+"= \""+path+"\"");
    }
  }

  private boolean isVedit(String[] args) {
    return args.length > 0 && "vedit".equals(args[0]);
  }
}
