package gw.vark.typeloader;

import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeLoader;
import gw.lang.reflect.TypeLoaderBase;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.module.IModule;
import gw.util.GosuExceptionUtil;
import gw.util.StreamUtil;
import gw.util.concurrent.LazyVar;
import gw.vark.Aardvark;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.launch.Locator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class AntlibTypeLoader extends TypeLoaderBase implements ITypeLoader{
  public static final String GW_VARK_TASKS_PACKAGE = "gw.vark.antlibs.";
  private static final String ANT_ANTLIB_SYMBOL = "Ant";
  private static final String ANT_ANTLIB_RESOURCE = "org/apache/tools/ant/taskdefs/defaults.properties";
  private static final String ANTLIBS_PROPERTIES = "antlibs.properties";

  private LazyVar<HashMap<String, IType>> _types = new LazyVar<HashMap<String, IType>>(){
    @Override
    protected HashMap<String, IType> init()
    {
      Properties antlibs = new Properties();
      File classSource = Locator.getClassSource(AntlibTypeLoader.class);
      File antlibsFile = findAntlibsProperties(classSource.isDirectory() ? classSource : classSource.getParentFile());
      if (antlibsFile != null && antlibsFile.exists()) {
        Aardvark.getProject().log("found user-defined antlibs: " + antlibsFile.getPath(), Project.MSG_VERBOSE);
        readProperties(antlibsFile, antlibs);
      }
      antlibs.put(ANT_ANTLIB_SYMBOL, ANT_ANTLIB_RESOURCE);

      HashMap<String, IType> types = new HashMap<String, IType>();
      for( Map.Entry entry : antlibs.entrySet() )
      {
        String antlibName = (String) entry.getKey();
        String antlibResource = (String) entry.getValue();
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
      return _types.get().get(fullyQualifiedName);
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

  private static File findAntlibsProperties(File dir) {
    if (dir == null) {
      return null;
    }
    if (new File(dir, "bin").exists()) {
      return new File(dir, ANTLIBS_PROPERTIES);
    }
    return findAntlibsProperties(dir.getParentFile());
  }

  private static void readProperties(File antlibsFile, Properties antlibs) {
    InputStream in = null;
    try {
      in = new FileInputStream(antlibsFile);
      antlibs.load(in);
    } catch (IOException e) {
      throw GosuExceptionUtil.forceThrow(e);
    } finally {
      try {
        StreamUtil.close(in);
      } catch (IOException e) { }
    }
  }
}
