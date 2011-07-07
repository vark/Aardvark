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
    System.exit(exitCode);
  }

  private int run(String[] args) throws MalformedURLException {
    File sourceJar = Locator.getClassSource(getClass());
    File home = getHome(sourceJar.isDirectory() ? sourceJar : sourceJar.getParentFile());

    logPath("Launcher JAR", sourceJar);
    logPath("Launcher home", home);

    URL[] jars = collectURLs(home, sourceJar);

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
    if (dir == null) {
      throw new RuntimeException("could not find aardvark home");
    }
    if (new File(dir, "bin/vark").exists()) {
      return dir;
    }
    return getHome(dir.getParentFile());
  }

  private URL[] collectURLs(File home, File classSource) throws MalformedURLException {
    List<URL> urls = new ArrayList<URL>();

    File libDir = new File(home, "lib");

    if ("true".equals(System.getProperty("aardvark.dev"))) {
      System.out.println("aardvark.dev is set to true - using IDE-compiled classes");

      File launcherDir = getLauncherDir(classSource.isDirectory() ? classSource : classSource.getParentFile());
      File aardvarkDir = new File(launcherDir.getParentFile(), "aardvark");
      File veditDir = new File(launcherDir.getParentFile(), "vedit");
      urls.add(Locator.fileToURL(new File(launcherDir, "classes")));
      urls.add(Locator.fileToURL(new File(aardvarkDir, "classes")));
      urls.add(Locator.fileToURL(new File(veditDir, "classes")));

      urls.addAll(Arrays.asList(Locator.getLocationURLs(new File(libDir, "launcher"))));
      urls.addAll(Arrays.asList(Locator.getLocationURLs(new File(libDir, "aardvark"))));
      urls.addAll(Arrays.asList(Locator.getLocationURLs(new File(libDir, "run"))));
    }
    else {
      urls.addAll(Arrays.asList(Locator.getLocationURLs(libDir)));
    }

    File toolsJar = Locator.getToolsJar();
    logPath("tools.jar", toolsJar);
    if (toolsJar != null) {
      urls.add(Locator.fileToURL(toolsJar));
    }

    return urls.toArray(new URL[urls.size()]);
  }

  private static File getLauncherDir(File dir) {
    if (dir == null) {
      throw new RuntimeException("could not find launcher dir");
    }
    if (dir.getName().equals("launcher")) {
      return dir;
    }
    return getLauncherDir(dir.getParentFile());
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
