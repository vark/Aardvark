package gw.vark.aether

uses org.sonatype.aether.resolution.ArtifactResult
uses java.io.File

/**
 */
enhancement AetherArtifactResultEnhancement : ArtifactResult {

  property get ClassesDirectory( ) : File {
    var repo = this.Repository
    var art = this.Artifact
    if ( repo != null && repo.Id == VarkWorskpace.WORKSPACE_ID && art.Extension == "jar" ) {
      var classes : File
      if (art.Classifier == "") {
        classes = new File( art.MavenModel.Build.OutputDirectory )
      } else if (art.Classifier == "tests") {
        classes = new File( art.MavenModel.Build.TestOutputDirectory )
      }
      if ( classes != null && classes.exists() ) {
        return classes;
      }
    }
    return art.File
  }
}
