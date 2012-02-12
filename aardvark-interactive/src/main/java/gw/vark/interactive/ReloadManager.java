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

package gw.vark.interactive;

import gw.lang.Gosu;
import gw.lang.launch.ArgInfo;
import gw.lang.parser.exceptions.ParseResultsException;
import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeRef;
import gw.lang.reflect.TypeSystem;
import gw.util.GosuClassUtil;
import gw.vark.Aardvark;
import gw.vark.AardvarkProgram;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 */
public class ReloadManager {

  private HashMap<File,Long> _timestamps = new HashMap<File, Long>();
  private final ArgInfo.IProgramSource _programSource;
  private List<File> _cpDirs;
  private AardvarkProgram _aardvarkProject;

  ReloadManager(ArgInfo.IProgramSource programSource) {
    _programSource = programSource;
  }

  public AardvarkProgram getAardvarkProject() {
    return _aardvarkProject;
  }

  public void detectAndReloadChangedResources() throws ParseResultsException {
    maybeParseVarkFile();
    scanForChanges();
  }

  private void maybeParseVarkFile() throws ParseResultsException {
    boolean newProject = false;
    if (_aardvarkProject == null) {
      newProject = true;
    }
    if (_programSource.getFile() != null) {
      File varkFile = _programSource.getFile();
      long timestamp = varkFile.lastModified();
      Long lastTimestamp = _timestamps.get(varkFile);
      if (lastTimestamp == null || timestamp != lastTimestamp) {
        newProject = true;
      }
      _timestamps.put(varkFile, timestamp);
    }
    if (newProject) {
      Project antProject = new Project();
      Aardvark.setProject(antProject, new DefaultLogger());
      _aardvarkProject = AardvarkProgram.parse(antProject, _programSource);
    }
  }

  private void scanForChanges() throws ParseResultsException {
    boolean updateResources = true;
    if (_cpDirs == null) {
      _cpDirs = parseClasspathDirs();
      updateResources = false;
    }
    for (File cpDir : _cpDirs) {
      checkForClassFileChanges(cpDir, cpDir, updateResources);
    }
    if (updateResources) {
      TypeSystem.getGosuClassLoader().reloadChangedClasses();
    }
  }

  private void checkForClassFileChanges(File cpDir, File file, boolean updateResources) {
    if (file.isFile()) {
      String ext = GosuClassUtil.getFileExtension(file);
      if (".gs".equals(ext) || ".gsx".equals(ext)) {
        long modified = file.lastModified();
        if (updateResources) {
          Long lastTimeStamp = _timestamps.get(file);
          if (lastTimeStamp == null || modified != lastTimeStamp) {
            fireResourceUpdate(cpDir, file);
          }
        }
        _timestamps.put(file, modified);
      }
    } else if (file.isDirectory()) {
      for (File child : file.listFiles()) {
        checkForClassFileChanges(cpDir, child, updateResources);
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

  private static List<File> parseClasspathDirs() {
    List<File> dirs = new ArrayList<File>();
    for (File file : Gosu.deriveClasspathFrom(Aardvark.class)) {
      if (file.isDirectory()) {
        dirs.add(file);
      }
    }

    return dirs;
  }
}
