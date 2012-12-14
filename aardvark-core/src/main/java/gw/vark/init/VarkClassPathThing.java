package gw.vark.init;

import org.apache.tools.ant.AntClassLoader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * Create fake JAR entry to make AntClassLoader load Gosu classes.
 * <p/>
 * This is similar to GosuClassPathThing in Gosu. We register empty JAR and manually place mapping
 * from our fake JAR file into instance of GosuJarFile, which loads bytecode from Gosu rather than
 * from the JAR itself.
 */
public final class VarkClassPathThing {
  private final Method _loaderMethod;

  public VarkClassPathThing() {
    try {
      Class<?> clazz = getClass().getClassLoader().loadClass("gw.internal.gosu.loader.Loader");
      _loaderMethod = clazz.getMethod("getBytesOrClass", String.class);
    } catch (Exception e) {
      throw new IllegalStateException("Cannot load Gosu Loader class!", e);
    }
  }

  @SuppressWarnings("unchecked")
  public void initAntClassLoader(AntClassLoader loader) {
    try {
      // First, we create empty fake JAR file
      File fakeJar = File.createTempFile("fake-gosuclass", ".jar");
      if (!fakeJar.createNewFile()) {
        throw new IllegalStateException("Cannot create temporary JAR file!");
      }
      fakeJar.deleteOnExit();

      Manifest manifest = new Manifest();
      manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
      JarOutputStream target = new JarOutputStream(new FileOutputStream(fakeJar), manifest);
      target.close();

      // Second, we add it to Ant classpath.
      loader.addPathComponent(fakeJar);

      // Third, we add JarFile instance to the AntClassLoader cache.
      Field f = AntClassLoader.class.getDeclaredField("jarFiles");
      f.setAccessible(true);
      Hashtable table = (Hashtable) f.get(loader);

      table.put(fakeJar, new GosuJarFile(fakeJar));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * {@link JarFile} implementation that retrieves bytecode from Gosu TypeSystem.
   */
  private class GosuJarFile extends JarFile {
    private Set<String> _visited = new HashSet<String>();

    private GosuJarFile(File file) throws IOException {
      super(file);
    }

    @Override
    public JarEntry getJarEntry(String name) {
      if (_visited.add(name)) {
        try {
          byte[] bytes = getBytes(name);
          if (bytes != null) {
            JarEntry entry = new JarEntry(name);
            entry.setExtra(bytes);
            return entry;
          }
        } finally {
          _visited.remove(name);
        }
      }
      return null;
    }

    @Override
    public synchronized InputStream getInputStream(ZipEntry ze) throws IOException {
      return new ByteArrayInputStream(ze.getExtra());
    }
  }

  /**
   * Load bytecode from Gosu.
   */
  private byte[] getBytes(String name) {
    String strType = name.replace('/', '.');
    int iIndexClass = strType.lastIndexOf(".class");
    if (iIndexClass < 0) {
      return null;
    }

    strType = strType.substring(0, iIndexClass).replace('$', '.');
    try {
      Object bytesOrClass = _loaderMethod.invoke(null, strType);
      return (bytesOrClass instanceof byte[]) ? (byte[]) bytesOrClass : null;
    } catch (IllegalAccessException e) {
      throw new IllegalStateException("Cannot invoke gosu loader", e);
    } catch (InvocationTargetException e) {
      throw new IllegalStateException("Cannot invoke gosu loader", e);
    }
  }
}
