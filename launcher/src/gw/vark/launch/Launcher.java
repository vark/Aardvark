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

import org.apache.tools.ant.launch.LaunchException;
import org.apache.tools.ant.launch.Locator;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Launcher extends AntLauncher {

  public static final String MAIN_CLASS = "gw.vark.Aardvark";
  public static final String VEDIT_CLASS = "gw.vark.editor.VEdit";

  private static Integer _exitCode = null;
  public static void setExitCode(int code) {
    if (_exitCode == null) {
      _exitCode = code;
    }
    else if (!isAardvarkDev()) {
      throw new IllegalStateException("exit code has already been set");
    }
  }

  @Override
  public String getHomePropertyName() {
    return "aardvark.home";
  }

  @Override
  public String getLibDirPropertyName() {
    return "aardvark.library.dir";
  }

  @Override
  public String getMainClassName(String[] args) {
    boolean isVedit = args[0].startsWith("vedit");
    return isVedit ? VEDIT_CLASS : MAIN_CLASS;
  }

  @Override
  protected File findHomeRelativeToDir(File dir) {
    if (dir == null) {
      throw new RuntimeException("could not find aardvark home");
    }
    if (new File(dir, "bin/vark").exists()) {
      return dir;
    }
    return findHomeRelativeToDir(dir.getParentFile());
  }

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
    } catch (LaunchException e) {
      exitCode = EXIT_CODE_ERROR;
      System.err.println(e.getMessage());
    } catch (Throwable t) {
      exitCode = EXIT_CODE_ERROR;
      t.printStackTrace(System.err);
    }
    if (exitCode == 0 && _exitCode != null) {
      exitCode = _exitCode;
    }
    if (exitCode != 0) {
      if (launchDiag) {
        System.out.println("Exit code: " + exitCode);
      }
      System.exit(exitCode);
    }
  }

  @Override
  protected URL[] getSystemURLs(File launcherDir) throws MalformedURLException {
    if (isAardvarkDev()) {
      System.out.println("aardvark.dev is on");
      List<URL> urls = new ArrayList<URL>();

      File homeDir = new File(System.getProperty(getHomePropertyName()));
      File libDir = new File(homeDir, "lib");
      File launcherJar;
      File aardvarkJar;
      File veditJar = null;

      if (launcherDir.getAbsolutePath().matches(".*out[/\\\\]production$")) {
        System.out.println("Using IJ-compiled classes");
        launcherJar = new File(launcherDir, "launcher");
        aardvarkJar = new File(launcherDir, "aardvark");
        veditJar = new File(launcherDir, "vedit");
      } else if (launcherDir.getAbsolutePath().matches(".*launcher[/\\\\]dist$")) {
        System.out.println("Using vark-compiled classes");
        launcherJar = new File(homeDir, "launcher" + File.separatorChar + "dist" + File.separatorChar + "aardvark-launcher.jar");
        aardvarkJar = new File(homeDir, "aardvark" + File.separatorChar + "dist" + File.separatorChar + "aardvark.jar");
        veditJar = new File(homeDir, "vedit" + File.separatorChar + "dist" + File.separatorChar + "aardvark-vedit.jar");
      } else {
        throw new IllegalStateException("could not locate locally-compiled classes");
      }

      urls.add(Locator.fileToURL(launcherJar));
      urls.add(Locator.fileToURL(aardvarkJar));
      if (veditJar.exists()) {
        urls.add(Locator.fileToURL(veditJar));
      }

      urls.addAll(Arrays.asList(Locator.getLocationURLs(new File(libDir, "launcher"))));
      urls.addAll(Arrays.asList(Locator.getLocationURLs(new File(libDir, "aardvark"))));
      urls.addAll(Arrays.asList(Locator.getLocationURLs(new File(libDir, "run"))));

      return urls.toArray(new URL[urls.size()]);
    }
    else {
      return super.getSystemURLs(launcherDir);
    }
  }

  private static boolean isAardvarkDev() {
    return "true".equals(System.getProperty("aardvark.dev"));
  }
}
