package gw.vark.typeloader;

import gw.lang.reflect.TypeSystem;

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

  public class Factory {
    public static IIntrospectionHelper create(Class<?> taskClass) {
      return TypeSystem.isSingleModuleMode() ?
              new SingleModuleIntrospection(taskClass) :
              new MultiModuleIntrospection(taskClass);
    }
  }
}
