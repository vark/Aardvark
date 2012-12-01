package gw.vark.typeloader;

import gw.fs.IDirectory;
import gw.fs.IFile;
import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeLoader;
import gw.lang.reflect.RefreshKind;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.gs.IFileSystemGosuClassRepository;
import gw.util.GosuExceptionUtil;
import gw.util.Pair;
import gw.util.StreamUtil;
import org.apache.tools.ant.Project;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Database of all .antlib types. Backend of the typeloader implementation that does not have to deal with lazyness and
 * typeloader API.
 */
public class AntlibTypeDatabase {
  private final Map<String, AntlibType> _typesByName = new HashMap<String, AntlibType>();
  private final Map<IFile, Set<String>> _typesBySource = new HashMap<IFile, Set<String>>();
  private final ITypeLoader _typeLoader;


  AntlibTypeDatabase(ITypeLoader typeLoader) {
    _typeLoader = typeLoader;

    IFileSystemGosuClassRepository repo = typeLoader.getModule().getFileRepository();
    for (Pair<String, IFile> pair : repo.findAllFilesByExtension(AntlibTypeLoader.ANTLIB_EXTENSION)) {
      loadType(pair.getSecond());
    }
  }

  IType getType(String typeName) {
    return _typesByName.get(typeName);
  }

  String[] getTypesForFile(IFile source) {
    Set<String> types = _typesBySource.get(source);
    return types != null ? types.toArray(new String[types.size()]) : null;
  }

  Set<String> getTypeNames() {
    return _typesByName.keySet();
  }

  Set<IFile> getSourceFiles() {
    return _typesBySource.keySet();
  }

  void reload(IFile file, RefreshKind kind) {
    Set<String> types = _typesBySource.get(file);

    if (types != null) {
      for (String typeName : types) {
        AntlibType type = _typesByName.get(typeName);

        // Clear old type mappings
        _typesByName.remove(type.getName());
        removeSource(type.getAntlibFile(), type.getName());
        removeSource(type.getAntlibResourceFile(), type.getName());

        // Reload .antlib
        if (kind != RefreshKind.DELETION) {
          loadType(type.getAntlibFile());
        }
      }
    } else {
      // must be an .antlib file!
      if (!file.getExtension().equals(AntlibTypeLoader.ANTLIB_EXTENSION)) {
        throw new IllegalStateException("Unexpected source file being refreshed!");
      }

      if (kind != RefreshKind.DELETION) {
        loadType(file);
      }
    }
  }

  void removeSource(IFile source, String typeName) {
    Set<String> names = _typesBySource.get(source);
    if (names == null) {
      return;
    }
    names.remove(typeName);
    if (names.isEmpty()) {
      _typesBySource.remove(source);
    }
  }

  void addSource(IFile source, String typeName) {
    Set<String> names = _typesBySource.get(source);
    if (names == null) {
      names = new HashSet<String>();
      _typesBySource.put(source, names);
    }
    names.add(typeName);
  }

  private void loadType(IFile antlibFile) {
    String antlibResource = readFile(antlibFile).trim();
    if (antlibResource.isEmpty()) {
      // Nothing to load right now!
      return;
    }
    IFile antlibResourceFile = findFirstFile(antlibResource);

    String antlibName = antlibFile.getBaseName();
    String typeName = AntlibTypeLoader.GW_VARK_TASKS_PACKAGE_WITH_DOT + antlibName;
    AntlibTypeLoader.log("loading antlib " + antlibFile.getBaseName() + " (" + antlibResource + ")", Project.MSG_VERBOSE);

    AntlibType type = new AntlibType(typeName, antlibFile, antlibResourceFile, _typeLoader);

    _typesByName.put(type.getName(), type);
    addSource(antlibFile, type.getName());
    addSource(antlibResourceFile, type.getName());
  }

  private static IFile findFirstFile(String resourceName) {
    for (IDirectory dir : TypeSystem.getCurrentModule().getFullResourcePath()) {
      IFile file = dir.file(resourceName);
      if (file != null && file.exists()) {
        return file;
      }
    }
    throw new RuntimeException(new FileNotFoundException(resourceName));
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
      } catch (IOException e) {
        // Ignore
      }
    }
  }
}
