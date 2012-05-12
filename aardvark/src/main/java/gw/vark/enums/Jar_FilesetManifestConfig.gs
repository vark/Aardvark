package gw.vark.enums

enum Jar_FilesetManifestConfig{

  Skip("skip"),
  Merge("merge"),
  Mergewithoutmain("mergewithoutmain"),

  property get Instance() : org.apache.tools.ant.taskdefs.Jar.FilesetManifestConfig {
    return org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.Jar.FilesetManifestConfig, Val) as org.apache.tools.ant.taskdefs.Jar.FilesetManifestConfig
  }

  var _val : String as Val

  private construct( s : String ) { Val = s }

}
