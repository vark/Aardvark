package gw.vark.codegen.internal;

import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.ResourceCollection;

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
class Introspector {

  private final Object _helperInstance;
  private final Class<?> _taskClass;
  private final Method _getAttributesMethod;
  private final Method _getAttributeTypeMethod;
  private final Method _getAttributeMethodMethod;
  private final Method _getElementMethodMethod;
  private final Method _getElementTypeMethod;
  private final Method _getExtensionPointsMethod;
  private final Method _getNestedElementsMethod;
  private final ClassLoader _classLoader;
  private final Class<?> _enumeratedAttribute;
  private final Class<?> _resourceCollection;

  Introspector(ClassLoader classLoader, String taskClassName) throws ClassNotFoundException {
    _classLoader = classLoader;
    _taskClass = loadClass(taskClassName);
    _enumeratedAttribute = loadClass(EnumeratedAttribute.class.getName());
    _resourceCollection = loadClass(ResourceCollection.class.getName());
    try {
      Class<?> helperClass = loadClass("org.apache.tools.ant.IntrospectionHelper");

      Method getHelperMethod = helperClass.getMethod("getHelper", Class.class);
      _helperInstance = invokeMethod(null, getHelperMethod, _taskClass);

      _getAttributesMethod = helperClass.getMethod("getAttributes");
      _getAttributeTypeMethod = helperClass.getMethod("getAttributeType", String.class);
      _getAttributeMethodMethod = helperClass.getMethod("getAttributeMethod", String.class);
      _getElementMethodMethod = helperClass.getMethod("getElementMethod", String.class);
      _getElementTypeMethod = helperClass.getMethod("getElementType", String.class);
      _getExtensionPointsMethod = helperClass.getMethod("getExtensionPoints");
      _getNestedElementsMethod = helperClass.getMethod("getNestedElements");
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  public Enumeration<?> getAttributes() {
    return (Enumeration<?>) invokeMethod(_helperInstance, _getAttributesMethod);
  }

  public Class<?> getAttributeType(String attributeName) {
    return (Class<?>) invokeMethod(_helperInstance, _getAttributeTypeMethod, attributeName);
  }

  public Method getAttributeMethod(String attributeName) {
    return (Method) invokeMethod(_helperInstance, _getAttributeMethodMethod, attributeName);
  }

  public Method getElementMethod(String elementName) {
    return (Method) invokeMethod(_helperInstance, _getElementMethodMethod, elementName);
  }

  public Class<?> getElementType(String elementName) {
    return (Class<?>) invokeMethod(_helperInstance, _getElementTypeMethod, elementName);
  }

  public List<?> getExtensionPoints() {
    return (List<?>) invokeMethod(_helperInstance, _getExtensionPointsMethod);
  }

  public Enumeration<?> getNestedElements() {
    return (Enumeration<?>) invokeMethod(_helperInstance, _getNestedElementsMethod);
  }

  public Class<?> loadClass(String className) throws ClassNotFoundException {
    return _classLoader.loadClass(className);
  }

  public Class<?> getTaskClass() {
    return _taskClass;
  }

  public boolean isEnumeratedAttribute(Class<?> clazz) {
    return _enumeratedAttribute.isAssignableFrom(clazz);
  }

  public boolean isResourceCollection(Class<?> clazz) {
    return _resourceCollection == clazz;
  }

  private static Object invokeMethod(Object obj, Method method, Object... args) {
    try {
      return method.invoke(obj, args);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  public boolean hasInitMethod() {
    try {
      _taskClass.getMethod("init");
      return true;
    } catch (NoSuchMethodException e) {
      return false;
    }
  }

  public boolean hasTaskNameMethod() {
    try {
      _taskClass.getMethod("setTaskName", String.class);
      return true;
    } catch (NoSuchMethodException e) {
      return false;
    }
  }
}
