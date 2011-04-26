/*
 * Copyright (c) 2011 Guidewire Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gw.vark.shell;

import gw.lang.parser.exceptions.ParseResultsException;
import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeRef;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.gs.IGosuProgram;
import gw.util.GosuClassUtil;
import gw.util.GosuExceptionUtil;
import gw.util.StreamUtil;
import gw.vark.Aardvark;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

/**
 */
public class ReloadManager {

  private HashMap<File,Long> _timestamps = new HashMap<File, Long>();
  private final File _varkFile;
  private final List<File> _cpDirs;
  private IGosuProgram _gosuProgram;

  public ReloadManager(File varkFile, IGosuProgram gosuProgram) {
    _varkFile = varkFile.getAbsoluteFile();
    _gosuProgram = gosuProgram;
    _cpDirs = parseClasspathDirs(_varkFile);

    scanForChanges(false);
  }

  public void detectAndReloadChangedResources() {
    scanForChanges(true);
  }

  public IGosuProgram getGosuProgram() {
    return _gosuProgram;
  }

  private void scanForChanges(boolean updateResource) {
    checkForVarkFileChange(updateResource);
    for (File cpDir : _cpDirs) {
      checkForClassFileChanges(cpDir, cpDir, updateResource);
    }
    if (updateResource) {
      TypeSystem.getCurrentModule().getClassLoader().getGosuClassLoader().reloadChangedClasses();
    }
  }

  private void checkForVarkFileChange(boolean updateResource) {
    long modified = _varkFile.lastModified();
    if (updateResource) {
      Long lastTimeStamp = _timestamps.get(_varkFile);
      if (lastTimeStamp == null || modified != lastTimeStamp) {
        // reparse gosu program
        try {
          _gosuProgram = Aardvark.parseAardvarkProgram(_varkFile);
        } catch (ParseResultsException e) {
          throw GosuExceptionUtil.forceThrow(e);
        }
      }
    }
    _timestamps.put(_varkFile, modified);
  }

  private void checkForClassFileChanges(File cpDir, File file, boolean updateResource) {
    if (file.isFile()) {
      String ext = GosuClassUtil.getFileExtension(file);
      if (".gs".equals(ext) || ".gsx".equals(ext)) {
        long modified = file.lastModified();
        if (updateResource) {
          Long lastTimeStamp = _timestamps.get(file);
          if (lastTimeStamp == null || modified != lastTimeStamp) {
            fireResourceUpdate(cpDir, file);
          }
        }
        _timestamps.put(file, modified);
      }
    } else if (file.isDirectory()) {
      for (File child : file.listFiles()) {
        checkForClassFileChanges(cpDir, child, updateResource);
      }
    }
  }

  private void fireResourceUpdate(File cpDir, File file) {
    String filePath = file.getPath();
    String rootPath = cpDir.getPath();
    String relPath = filePath.substring(rootPath.length() + 1, filePath.lastIndexOf('.'));
    String typeName = relPath.replace(File.separatorChar, '.');
    IType type = TypeSystem.getByFullNameIfValid(typeName);
    if (type != null) {
      TypeSystem.refresh((ITypeRef) type, true);
    }
  }

  private static List<File> parseClasspathDirs(File varkFile) {
    List<File> dirs = new ArrayList<File>();
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(varkFile));
      while (true) {
        String line = reader.readLine();
        if (line == null) {
          break;
        }

        line = line.trim();
        if (line.startsWith("classpath")) {
          line = line.substring("classpath".length()).trim().replace("\"", "");
          StringTokenizer tok = new StringTokenizer(line, ",");
          while (tok.hasMoreTokens()) {
            String cpElement = tok.nextToken();
            File dir = new File(varkFile.getParentFile(), cpElement);
            if (dir.isDirectory()) {
              dirs.add(dir.getCanonicalFile());
            }
          }
        }
      }
    } catch (IOException e) {
      throw GosuExceptionUtil.forceThrow(e);
    } finally {
      try {
        StreamUtil.close(reader);
      } catch (IOException e) { }
    }

    return dirs;
  }
}
