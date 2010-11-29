/*
 * Copyright (c) 2010 Guidewire Software, Inc.
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

package gw.vark

uses java.io.File
uses gw.vark.Aardvark
uses org.apache.tools.ant.BuildException
uses org.apache.tools.ant.Project
uses org.apache.tools.ant.types.FileSet
uses org.apache.tools.ant.types.Path

/**
 * Contains the core functionality of Aardvark
 */
enhancement CoreIAardvarkUtilsEnhancement : IAardvarkUtils {
  static function log( o : Object ) {
    logInfo( o )
  }

  static function logError( o: Object ) {
    Aardvark.getProject().log( o as String, Project.MSG_ERR )
  }

  static function logWarn( o: Object ) {
    Aardvark.getProject().log( o as String, Project.MSG_WARN )
  }

  static function logInfo( o: Object ) {
    Aardvark.getProject().log( o as String, Project.MSG_INFO )
  }

  static function logVerbose( o: Object ) {
    Aardvark.getProject().log( o as String, Project.MSG_VERBOSE )
  }

  static function logDebug( o: Object ) {
    Aardvark.getProject().log( o as String, Project.MSG_DEBUG )
  }

  static function path() : Path {
    return new Path( Aardvark.getProject() )
  }

  static function path( file : File ) : Path {
    return path().withFile( file )
  }

  static function path( files : FileSet ) : Path {
    return path().withFileset( files )
  }

  static function classpath() : Path {
    return path()
  }

  static function classpath( file : File ) : Path {
    return path( file )
  }

  static function classpath( fs : FileSet ) : Path {
    return path( fs )
  }

  static function file( s : String ) : File {
    var f = new File(s)
    if( f.Absolute or s.startsWith( "/" ) ) {
      return f
    } else {
      return new File( Aardvark.getProject().BaseDir, s )
    }
  }

  static function getProperty(propName : String) : String {
    return Aardvark.getProject().getProperty(propName)
  }

  static function getRequiredProperty(propName : String) : String {
    var prop = getProperty(propName)
    if (prop == null || prop.length() == 0) {
      throw buildException("property ${propName} needs to be set")
    }
    return prop
  }

  static function buildException(message : String) : BuildException {
    throw new BuildException(message)
  }

  static property get Ant() : AntCoreTasks { return AntCoreTasks.INSTANCE }
}
