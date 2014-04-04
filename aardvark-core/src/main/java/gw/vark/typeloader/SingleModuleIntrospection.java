package gw.vark.typeloader;

import gw.lang.reflect.module.IModule;
import org.apache.tools.ant.IntrospectionHelper;
import org.apache.tools.ant.Task;

import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.List;

/**
 * This is a wrapper that holds an instance of org.apache.tools.ant.IntrospectionHelper
 * as found in the Gosu type system. In single module module we can safely load class
 * through class loader.
 */
class SingleModuleIntrospection implements IIntrospectionHelper {

  private final IntrospectionHelper _helperInstance;
  private final Class<?> _taskClass;

  public SingleModuleIntrospection(IModule module, String taskClassName) throws ClassNotFoundException {
    _taskClass = loadClass(taskClassName);
    _helperInstance = IntrospectionHelper.getHelper(_taskClass);
  }

  @Override
  public Enumeration<?> getAttributes() {
    return _helperInstance.getAttributes();
  }

  @Override
  public Class<?> getAttributeType(String attributeName) {
    return _helperInstance.getAttributeType(attributeName);
  }

  @Override
  public Method getElementMethod(String elementName) {
    return _helperInstance.getElementMethod(elementName);
  }

  @Override
  public Class<?> getElementType(String elementName) {
    return _helperInstance.getElementType(elementName);
  }

  @Override
  public List<?> getExtensionPoints() {
    return _helperInstance.getExtensionPoints();
  }

  @Override
  public Enumeration<?> getNestedElements() {
    return _helperInstance.getNestedElements();
  }

  @Override
  public Class<?> getTaskClass() {
    return _taskClass;
  }

  @Override
  public Class<?> loadClass(String className) throws ClassNotFoundException {
    return Class.forName(className);
  }
}
