package gw.vark.init;

import gw.config.ServiceKernel;
import gw.config.ServiceKernelInit;
import gw.lang.reflect.IEntityAccess;

import java.net.URL;
import java.net.URLClassLoader;

public class VarkServiceKernelInit implements ServiceKernelInit {
  public void init(ServiceKernel services) {
    try {
      IEntityAccess delegate = (IEntityAccess)
              Class.forName("gw.internal.gosu.parser.DefaultEntityAccess").getMethod("instance").invoke(null);
      services.redefineService(IEntityAccess.class, new VarkEntityAccess(delegate,
              new FakeURLClassLoader(delegate.getPluginClassLoader())));
    } catch (Exception e) {
      // Ignore?
    }
  }

  /**
   * Fake {@link URLClassLoader} to trick Gosu classpath thing. Gosu will try
   * to add URL to our classloader to make it load Gosu classes. We don't want
   * that since we will hook Gosu classes into AntClassLoader directly (using
   * different trick). Tricky.
   */
  private static class FakeURLClassLoader extends URLClassLoader {
    public FakeURLClassLoader(ClassLoader parent) {
      super(new URL[0], parent);
    }

    @Override
    protected void addURL(URL url) {
      // Do nothing!
    }
  }
}
