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

import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeRef;
import gw.lang.reflect.TypeSystem;
import gw.util.GosuClassUtil;

import java.io.File;
import java.util.HashMap;

/**
 */
public class ReloadManager {

  private static HashMap<File,Long> _timestamps;
  private static File _varkFile;
  private static IType _varkFileType;

  public static void detectAndReloadChangedResources() {
    if (!maybeInit()) {
      scanForChanges(true);
    }
  }

  private static void scanForChanges(boolean updateResource) {
    scanForChanges(_varkFile.getParentFile(), updateResource);
    if (updateResource) {
      TypeSystem.getCurrentModule().getClassLoader().getGosuClassLoader().reloadChangedClasses();
    }
  }

  private static void scanForChanges(File file, boolean updateResource) {
    if (file.isFile()) {
      String ext = GosuClassUtil.getFileExtension(file);
      if (".gs".equals(ext) || ".gsx".equals(ext) || ".vark".equals(ext)) {
        long modified = file.lastModified();
        if (updateResource) {
          Long lastTimeStamp = _timestamps.get(file);
          if (lastTimeStamp == null || modified != lastTimeStamp) {
            fireResourceUpdate(file);
          }
        }
        _timestamps.put(file, modified);
      }
    } else if (file.isDirectory()) {
      for (File child : file.listFiles()) {
        scanForChanges(child, updateResource);
      }
    }
  }

  private static void fireResourceUpdate(File file) {
    if (file.equals(_varkFile)) {
      TypeSystem.refresh((ITypeRef) _varkFileType, true);
    }
    else {
      String filePath = file.getAbsolutePath();
      String rootPath = _varkFile.getParentFile().getAbsolutePath();
      String typeName = filePath.substring(rootPath.length() + 1, filePath.lastIndexOf('.'));
      typeName = typeName.replace(File.separatorChar, '.');
      IType type = TypeSystem.getByFullNameIfValid(typeName);
      if (type != null) {
        TypeSystem.refresh((ITypeRef) type, true);
      }
    }
  }

  private static boolean maybeInit() {
    if (_varkFile == null) {
      return true;
    }
    if (_timestamps == null) {
      _timestamps = new HashMap<File, Long>();
      scanForChanges(false);
      return true;
    } else {
      return false;
    }
  }

  static void setVarkFile(File varkFile, IType type) {
    _varkFile = varkFile;
    _varkFileType = type;
  }
}
