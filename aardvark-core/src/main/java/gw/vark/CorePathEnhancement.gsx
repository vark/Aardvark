/*
 * Copyright (c) 2012 Guidewire Software, Inc.
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
uses java.util.List
uses org.apache.tools.ant.types.FileSet
uses org.apache.tools.ant.types.Path

enhancement CorePathEnhancement : Path {

  function withFile( file : File ) : Path {
    this.setLocation(file)
    return this
  }

  function withFileset( fs : FileSet ) : Path {
    this.addFileset( fs )
    return this
  }

  function withPath( p : Path ) : Path {
    for (element in p.list()) {
      this.setPath(element)
    }
    return this
  }

}
