package gw.vark.typeloader;

import gw.fs.IFile;
import gw.lang.reflect.IFileBasedType;
import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeInfo;
import gw.lang.reflect.ITypeLoader;
import gw.lang.reflect.TypeBase;
import gw.lang.reflect.java.JavaTypes;
import gw.util.GosuClassUtil;
import gw.util.concurrent.LockingLazyVar;

public class AntlibType extends TypeBase implements IFileBasedType {
  private final String _name;
  private final ITypeLoader _loader;
  private final IFile _antlib;
  private final IFile _antlibResource;
  private LockingLazyVar<ITypeInfo> _typeInfo = new LockingLazyVar<ITypeInfo>() {
    @Override
    protected ITypeInfo init() {
      return new AntlibTypeInfo(_antlibResource, AntlibType.this);
    }
  };

  public AntlibType(String name, IFile antlib, IFile antlibResource, ITypeLoader loader) {
    _name = name;
    _loader = loader;
    _antlib = antlib;
    _antlibResource = antlibResource;
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

  @Override
  public IFile[] getSourceFiles() {
    return new IFile[] { _antlib, _antlibResource };
  }

  public IFile getAntlibFile() {
    return _antlib;
  }

  public IFile getAntlibResourceFile() {
    return _antlibResource;
  }
}
