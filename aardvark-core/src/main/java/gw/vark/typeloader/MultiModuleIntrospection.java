package gw.vark.typeloader;

import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.java.IJavaType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.List;

/**
 * This is a wrapper that holds an instance of org.apache.tools.ant.IntrospectionHelper
 * as found in the Gosu type system, not necessarily the one found in the class loader.
 * In an IDEA Gosu plugin runtime, the class loader instance may be from the Ant library
 * in the IDEA classpath, and not the user's project classpath.  We want the latter.
 * <p/>
 * Note that the Antlib type loader may use the IntrospectionHelper directly during
 * standalone Aardvark execution.
 */
class MultiModuleIntrospection implements IIntrospectionHelper {

  private final Object _helperInstance;
  private final Class<?> _taskClass;
  private final Method _getAttributesMethod;
  private final Method _getAttributeTypeMethod;
  private final Method _getElementMethodMethod;
  private final Method _getElementTypeMethod;
  private final Method _getExtensionPointsMethod;
  private final Method _getNestedElementsMethod;

  MultiModuleIntrospection(String taskClassName) throws ClassNotFoundException {
    _taskClass = loadClass(taskClassName);
    try {
      Class<?> helperClass = loadClass("org.apache.tools.ant.IntrospectionHelper");

      Method getHelperMethod = helperClass.getMethod("getHelper", Class.class);
      _helperInstance = invokeMethod(null, getHelperMethod, _taskClass);

      _getAttributesMethod = helperClass.getMethod("getAttributes");
      _getAttributeTypeMethod = helperClass.getMethod("getAttributeType", String.class);
      _getElementMethodMethod = helperClass.getMethod("getElementMethod", String.class);
      _getElementTypeMethod = helperClass.getMethod("getElementType", String.class);
      _getExtensionPointsMethod = helperClass.getMethod("getExtensionPoints");
      _getNestedElementsMethod = helperClass.getMethod("getNestedElements");
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Enumeration<?> getAttributes() {
    return (Enumeration<?>) invokeMethod(_helperInstance, _getAttributesMethod);
  }

  @Override
  public Class<?> getAttributeType(String attributeName) {
    return (Class<?>) invokeMethod(_helperInstance, _getAttributeTypeMethod, attributeName);
  }

  @Override
  public Method getElementMethod(String elementName) {
    return (Method) invokeMethod(_helperInstance, _getElementMethodMethod, elementName);
  }

  @Override
  public Class<?> getElementType(String elementName) {
    return (Class<?>) invokeMethod(_helperInstance, _getElementTypeMethod, elementName);
  }

  @Override
  public List<?> getExtensionPoints() {
    return (List<?>) invokeMethod(_helperInstance, _getExtensionPointsMethod);
  }

  @Override
  public Enumeration<?> getNestedElements() {
    return (Enumeration<?>) invokeMethod(_helperInstance, _getNestedElementsMethod);
  }

  @Override
  public Class<?> loadClass(String className) throws ClassNotFoundException {
    try {
      IJavaType type = (IJavaType) TypeSystem.getByFullName(className);
      return type.getBackingClassInfo().getBackingClass();
    } catch (RuntimeException e) {
      if (e.getCause() instanceof ClassNotFoundException) {
        throw (ClassNotFoundException) e.getCause();
      } else {
        throw e;
      }
    }
  }

  @Override
  public Class<?> getTaskClass() {
    return _taskClass;
  }

  private static Object invokeMethod(Object obj, Method method, Object... args) {
    try {
      return method.invoke(obj, args);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }
}
