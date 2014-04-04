package gw.vark

uses org.apache.tools.ant.types.Path

uses java.io.File

enhancement AntPathEnhancement : Path {

  function append( file : File ) : Path {
    this.createPathElement().setLocation(file)
    return this
  }

  function append( cp : List<File> ) : Path {
    for (file in cp) {
      this.createPathElement().setLocation(file)
    }
    return this
  }
}
