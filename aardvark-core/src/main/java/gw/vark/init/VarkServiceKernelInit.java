package gw.vark.init;

import gw.config.ServiceKernel;
import gw.config.ServiceKernelInit;
import gw.lang.reflect.IEntityAccess;

import java.net.URL;
import java.net.URLClassLoader;

public class VarkServiceKernelInit implements ServiceKernelInit
{
  public void init( ServiceKernel services )
  {
    try {
      IEntityAccess delegate = (IEntityAccess)
              Class.forName("gw.internal.gosu.parser.DefaultEntityAccess").getMethod("instance").invoke(null);
      services.redefineService(IEntityAccess.class, new VarkEntityAccess(delegate,
              new URLClassLoader(new URL[0], delegate.getPluginClassLoader())));
    } catch (Exception e) {
      // Ignore?
    }
  }
}
