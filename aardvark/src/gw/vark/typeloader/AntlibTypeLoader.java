package gw.vark.typeloader;

import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeLoader;
import gw.lang.reflect.TypeLoaderBase;
import gw.lang.reflect.TypeSystem;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class AntlibTypeLoader extends TypeLoaderBase implements ITypeLoader{
  public static final String GW_VARK_TASKS_PACKAGE = "gw.vark.antlibs.";
  private HashMap<String, IType> _types = new HashMap<String, IType>();

  public AntlibTypeLoader() {
  }

  @Override
  public IType getType(String fullyQualifiedName) {
    if (fullyQualifiedName.startsWith(GW_VARK_TASKS_PACKAGE)) {
      String name = fullyQualifiedName.substring(GW_VARK_TASKS_PACKAGE.length());
      return _types.get(name);
    } else {
      return null;
    }
  }

  @Override
  public Set<? extends CharSequence> getAllTypeNames() {
    return _types.keySet();
  }

  @Override
  public List<String> getHandledPrefixes() {
    return Collections.emptyList();
  }

  public void addAntlib(String antlibName, String resourceName) {
    String typeName = GW_VARK_TASKS_PACKAGE + antlibName;
    _types.put(typeName, TypeSystem.getOrCreateTypeReference(new AntlibType(typeName, resourceName, this)));
  }

}
