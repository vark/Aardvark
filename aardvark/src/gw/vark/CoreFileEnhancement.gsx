package gw.vark

uses java.io.File
uses org.apache.tools.ant.types.FileSet

/**
 */
enhancement CoreFileEnhancement : File {

  function file(childName : String) : File {
    return this.getChild(childName)
  }

  function fileset(includes : String = null, excludes : String = null) : FileSet {
    var fs = new FileSet() { :Dir = this }
    fs.Project = Aardvark.getProject()
    if (includes != null)
    {
      fs.setIncludes( includes )
    }
    if (excludes != null)
    {
      fs.setExcludes( excludes )
    }
    return fs
  }
}
