package gw.vark.typeloader;

import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.java.IJavaClassInfo;
import gw.lang.reflect.java.IJavaType;
import org.apache.tools.ant.Task;

import java.net.URL;

/**
 */
class TypeSystemUtil {

  static Class<?> getAntClass(String className) throws ClassNotFoundException {
    if (TypeSystem.isSingleModuleMode()) {
      return Class.forName(className);
    }

    // use TypeSystem rather than Class.forName() - the latter would pick up the Ant task class from the IDEA SDK
    // rather than from the Ant dependency within the user project that Aardvark should be working with
    try {
      IJavaType taskType = (IJavaType) TypeSystem.getByFullName(className);
      return taskType.getBackingClassInfo().getBackingClass();
    } catch (RuntimeException e) {
      if (e.getCause() instanceof ClassNotFoundException) {
        throw (ClassNotFoundException) e.getCause();
      } else {
        throw e;
      }
    }
  }
}
