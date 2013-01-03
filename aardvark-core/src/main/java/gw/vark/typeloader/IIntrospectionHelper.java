package gw.vark.typeloader;

import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.module.IModule;

import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.List;

/**
 */
public interface IIntrospectionHelper {
  Enumeration<?> getAttributes();

  Class<?> getAttributeType(String attributeName);

  Class<?> getElementType(String elementName);

  Method getElementMethod(String elementName);

  List<?> getExtensionPoints();

  Enumeration<?> getNestedElements();

  Class<?> loadClass(String name) throws ClassNotFoundException;

  Class<?> getTaskClass();

  public class Factory {
    public static IIntrospectionHelper create(IModule module, String taskClassName) throws ClassNotFoundException {
      return TypeSystem.isSingleModuleMode() ?
              new SingleModuleIntrospection(module, taskClassName) :
              new MultiModuleIntrospection(module, taskClassName);
    }
  }
}
