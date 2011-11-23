package gw.vark.typeloader;

import gw.fs.IDirectory;
import gw.fs.IFile;
import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeLoader;
import gw.lang.reflect.TypeLoaderBase;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.module.IModule;
import gw.util.GosuExceptionUtil;
import gw.util.Pair;
import gw.util.StreamUtil;
import gw.util.concurrent.LockingLazyVar;
import gw.vark.Aardvark;
import org.apache.tools.ant.Project;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AntlibTypeLoader extends TypeLoaderBase implements ITypeLoader{
  public static final String GW_VARK_TASKS_PACKAGE = "gw.vark.antlibs.";
  private static final String GW_VARK_TASKS_PATH = "gw/vark/antlibs";
  private static final String ANT_ANTLIB_SYMBOL = "Ant";
  private static final String ANT_ANTLIB_RESOURCE = "org/apache/tools/ant/taskdefs/defaults.properties";

  private LockingLazyVar<HashMap<String, IType>> _types = new LockingLazyVar<HashMap<String, IType>>(){
    @Override
    protected HashMap<String, IType> init()
    {
      HashMap<String, String> antlibs = new HashMap<String, String>();
      for (Pair<String, IFile> pair : TypeSystem.getExecutionEnvironment().getCurrentModule().getFileRepository().findAllFilesByExtension("antlib")) {
        IFile file = pair.getSecond();
        antlibs.put(file.getBaseName(), readFile(file).trim());
      }
      antlibs.put(ANT_ANTLIB_SYMBOL, ANT_ANTLIB_RESOURCE);

      HashMap<String, IType> types = new HashMap<String, IType>();
      for( Map.Entry<String, String> entry : antlibs.entrySet() )
      {
        String antlibName = entry.getKey();
        String antlibResource = entry.getValue();
        Aardvark.getProject().log("loading antlib " + antlibName + " (" + antlibResource + ")", Project.MSG_VERBOSE);
        String typeName = GW_VARK_TASKS_PACKAGE + antlibName;
        types.put( typeName, TypeSystem.getOrCreateTypeReference( new AntlibType( typeName, antlibResource, AntlibTypeLoader.this ) ) );
      }
      return types;
    }
  };

  public AntlibTypeLoader(IModule module) {
    super(module);
  }

  @Override
  public IType getType(String fullyQualifiedName) {
    if (fullyQualifiedName.startsWith(GW_VARK_TASKS_PACKAGE)) {
      String name = fullyQualifiedName.substring(GW_VARK_TASKS_PACKAGE.length());
      return _types.get().get( name );
    } else {
      return null;
    }
  }

  @Override
  public Set<? extends CharSequence> getAllTypeNames() {
    return _types.get().keySet();
  }

  @Override
  public List<String> getHandledPrefixes() {
    return Collections.emptyList();
  }

  @Override
  public boolean handlesNonPrefixLoads() {
    return true;
  }

  private static String readFile(IFile file) {
    Reader reader = null;
    try {
      reader = StreamUtil.getInputStreamReader(file.openInputStream());
      return StreamUtil.getContent(reader);
    } catch (IOException e) {
      throw GosuExceptionUtil.forceThrow(e);
    } finally {
      try {
        StreamUtil.close(reader);
      } catch (IOException e) { }
    }
  }
}
