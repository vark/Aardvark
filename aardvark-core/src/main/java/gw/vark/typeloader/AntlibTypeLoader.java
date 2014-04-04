package gw.vark.typeloader;

import gw.fs.IDirectory;
import gw.fs.IFile;
import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeLoader;
import gw.lang.reflect.RefreshKind;
import gw.lang.reflect.TypeLoaderBase;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.module.IModule;
import gw.util.concurrent.LockingLazyVar;
import gw.vark.Aardvark;
import gw.vark.NoProjectInstanceException;
import org.apache.tools.ant.Project;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class AntlibTypeLoader extends TypeLoaderBase implements ITypeLoader {
  public static final String ANTLIB_EXTENSION = "antlib";
  public static final String GW_VARK_TASKS_PACKAGE = "gw.vark.antlibs";
  public static final String GW_VARK_TASKS_PACKAGE_WITH_DOT = GW_VARK_TASKS_PACKAGE + ".";

  // TODO - this is nasty, but for now I'm hacking in a way so that we don't get an exception
  // on Aardvark.getProject() when the typeloader is loading types in the IJ Gosu plugin
  static final Project NULL_PROJECT = new Project();
  private static Project _projectInstance = null;

  static void log(String message, int msgLevel) {
    Project project;
    synchronized (AntlibTypeLoader.class) {
      if (_projectInstance == null) {
        try {
          _projectInstance = Aardvark.getProject();
        } catch (NoProjectInstanceException e) {
          _projectInstance = NULL_PROJECT;
        }
      }
      project = _projectInstance;
    }
    if (project != NULL_PROJECT) {
      project.log(message, msgLevel);
    }
  }


  private LockingLazyVar<AntlibTypeDatabase> _types = new LockingLazyVar<AntlibTypeDatabase>() {
    @Override
    protected AntlibTypeDatabase init() {
      return new AntlibTypeDatabase(AntlibTypeLoader.this);
    }
  };

  public AntlibTypeLoader(IModule module) {
    super(module);
  }


  @Override
  public IType getType(String fullyQualifiedName) {
    if (fullyQualifiedName.startsWith(GW_VARK_TASKS_PACKAGE_WITH_DOT)) {
      IType type = _types.get().getType(fullyQualifiedName);
      if (type != null) {
        return TypeSystem.getOrCreateTypeReference(type);
      }
    }
    return null;
  }

  @Override
  public Set<String> computeTypeNames() {
    return _types.get().getTypeNames();
  }

  @Override
  public List<String> getHandledPrefixes() {
    return Collections.emptyList();
  }

  @Override
  public boolean handlesFile(IFile file) {
    return ANTLIB_EXTENSION.equals(file.getExtension()) || _types.get().getSourceFiles().contains(file);
  }

  @Override
  public RefreshKind refreshedFile(IFile file, String[] types, RefreshKind kind) {
    // Sanity check
    if (!handlesFile(file)) {
      throw new IllegalArgumentException("File not handled by the typeloader: " + file);
    }

    // Refresh by file.
    _types.get().reload(file, kind);
    return kind;
  }

  @Override
  public String[] getTypesForFile(IFile file) {
    return _types.get().getTypesForFile(file);
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
    // TODO: Refresh?
  }

  @Override
  public boolean hasNamespace(String s) {
    return GW_VARK_TASKS_PACKAGE.equals(s);
  }
}
