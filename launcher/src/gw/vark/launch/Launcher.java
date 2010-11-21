package gw.vark.launch;

import org.apache.tools.ant.launch.Locator;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class Launcher {

  /**
   * launch diagnostics flag; for debugging trouble at launch time.
   */
  public static boolean _launchDiag = false;

  public static final String MAIN_CLASS = "gw.vark.Aardvark";
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

    File toolsJar = Locator.getToolsJar();
    logPath("tools.jar", toolsJar);

    URL[] jars = getJarArray(aardvarkURLs, antURLs, gosuURLs, toolsJar);

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
    try {
      mainClass = loader.loadClass(MAIN_CLASS);
      AardvarkMain aardvark = (AardvarkMain) mainClass.newInstance();
      aardvark.start(args);
    } catch (InstantiationException e) {
      System.err.println("Incompatible version of " + MAIN_CLASS + " detected");
      File mainJar = Locator.getClassSource(mainClass);
      System.err.println("Location of this class " + mainJar);
      thrown = e;
    } catch (ClassNotFoundException e) {
      System.err.println("Failed to locate " + MAIN_CLASS);
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

  private static URL[] getJarArray(URL[] aardvarkJars, URL[] antJars, URL[] gosuJars, File toolsJar) throws MalformedURLException {
    int numJars = aardvarkJars.length + antJars.length + gosuJars.length;
    if (toolsJar != null) {
      numJars++;
    }
    URL[] jars = new URL[numJars];
    System.arraycopy(aardvarkJars, 0, jars, 0, aardvarkJars.length);
    System.arraycopy(antJars, 0, jars, aardvarkJars.length, antJars.length);
    System.arraycopy(gosuJars, 0, jars, antJars.length + aardvarkJars.length, gosuJars.length);

    if (toolsJar != null) {
      jars[jars.length - 1] = Locator.fileToURL(toolsJar);
    }
    return jars;
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
}
