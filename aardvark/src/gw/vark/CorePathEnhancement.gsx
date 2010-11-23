/*
 * Copyright (c) 2010 Guidewire Software, Inc.
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

}
