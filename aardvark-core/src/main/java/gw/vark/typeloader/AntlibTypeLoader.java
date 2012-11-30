package gw.vark.typeloader;

import gw.fs.IDirectory;
import gw.fs.IFile;
import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeLoader;
import gw.lang.reflect.RefreshKind;
import gw.lang.reflect.TypeLoaderBase;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.module.IModule;
import gw.util.GosuExceptionUtil;
import gw.util.Pair;
import gw.util.StreamUtil;
import gw.util.concurrent.LockingLazyVar;
import gw.vark.Aardvark;
import gw.vark.NoProjectInstanceException;
import org.apache.tools.ant.Project;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AntlibTypeLoader extends TypeLoaderBase implements ITypeLoader{
  public static final String GW_VARK_TASKS_PACKAGE = "gw.vark.antlibs";
  private static final String GW_VARK_TASKS_PACKAGE_WITH_DOT = GW_VARK_TASKS_PACKAGE + ".";
  private static final String GW_VARK_TASKS_PATH = "gw/vark/antlibs";
  private static final String ANT_ANTLIB_SYMBOL = "Ant";
  private static final String ANT_ANTLIB_RESOURCE = "org/apache/tools/ant/taskdefs/defaults.properties";

  // TODO - this is nasty, but for now I'm hacking in a way so that we don't get an exception
  // on Aardvark.getProject() when the typeloader is loading types in the IJ Gosu plugin
  static final Project NULL_PROJECT = new Project();
  private static Project _projectInstance = null;
  static void log(String message, int msgLevel) {
    Project project;
    synchronized(AntlibTypeLoader.class) {
      if (_projectInstance == null) {
        try {
          _projectInstance = Aardvark.getProject();
        }
        catch (NoProjectInstanceException e) {
          _projectInstance = NULL_PROJECT;
        }
      }
      project = _projectInstance;
    }
    if (project != NULL_PROJECT) {
      project.log(message, msgLevel);
    }
  }

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
        log("loading antlib " + antlibName + " (" + antlibResource + ")", Project.MSG_VERBOSE);
        String typeName = GW_VARK_TASKS_PACKAGE_WITH_DOT + antlibName;
        IFile antlibFile = findFirstFile(antlibResource);
        types.put( typeName, new AntlibType( typeName, antlibResource, antlibFile, AntlibTypeLoader.this ) );
      }
      return types;
    }
  };

  private static IFile findFirstFile(String resourceName) {
    for (IDirectory dir : TypeSystem.getCurrentModule().getFullResourcePath()) {
      IFile file = dir.file(resourceName);
      if (file != null && file.exists()) {
        return file;
      }
    }
    throw new RuntimeException(new FileNotFoundException(resourceName));
  }

  public AntlibTypeLoader(IModule module) {
    super(module);
  }

  @Override
  public IType getType(String fullyQualifiedName) {
    if (fullyQualifiedName.startsWith(GW_VARK_TASKS_PACKAGE_WITH_DOT)) {
      IType type = _types.get().get( fullyQualifiedName );
      if (type != null) {
        return TypeSystem.getOrCreateTypeReference( type );
      }
    }
    return null;
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

  @Override
  public Set<? extends CharSequence> getAllNamespaces() {
    return Collections.singleton(GW_VARK_TASKS_PACKAGE);
  }

  @Override
  public void refreshedNamespace(String s, IDirectory iDirectory, RefreshKind refreshKind) {
  }

  @Override
  public boolean hasNamespace(String s) {
    return GW_VARK_TASKS_PACKAGE.equals(s);
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
