package gw.vark.typeloader;

import org.apache.tools.ant.IntrospectionHelper;
import org.apache.tools.ant.Task;

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
class SingleModuleIntrospection implements IIntrospectionHelper {

  private final IntrospectionHelper _helperInstance;

  public SingleModuleIntrospection(Class<?> taskClass) {
    _helperInstance = IntrospectionHelper.getHelper(taskClass);
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
}
