package gw.vark.typeloader;

import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeInfo;
import gw.lang.reflect.ITypeLoader;
import gw.lang.reflect.TypeBase;
import gw.lang.reflect.java.JavaTypes;
import gw.util.GosuClassUtil;
import gw.util.concurrent.LockingLazyVar;

public class AntlibType extends TypeBase implements IType {
  private String _name;
  private ITypeLoader _loader;
  private String _url;
  private LockingLazyVar<ITypeInfo> _typeInfo = new LockingLazyVar<ITypeInfo>() {
    @Override
    protected ITypeInfo init() {
      return new AntlibTypeInfo(_url, AntlibType.this);
    }
  };

  public AntlibType(String name, String url, ITypeLoader loader) {
    _url = url;
    _name = name;
    _loader = loader;
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public String getRelativeName() {
    return GosuClassUtil.getNameNoPackage(getName());
  }

  @Override
  public String getNamespace() {
    return GosuClassUtil.getPackage(getName());
  }

  @Override
  public ITypeLoader getTypeLoader() {
    return _loader;
  }

  @Override
  public IType getSupertype() {
    return JavaTypes.OBJECT();
  }

  @Override
  public IType[] getInterfaces() {
    return new IType[0];
  }

  @Override
  public ITypeInfo getTypeInfo() {
    return _typeInfo.get();
  }
}
