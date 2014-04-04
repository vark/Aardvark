package gw.vark.aether

uses org.apache.maven.model.Model
uses org.sonatype.aether.artifact.Artifact
uses org.sonatype.aether.repository.WorkspaceReader
uses org.sonatype.aether.repository.WorkspaceRepository

uses java.io.File
uses java.util.Collection
uses java.util.Map

/**
 * Implementation of {@link WorkspaceReader} that emulates Maven reactor.
 */
class VarkWorskpace implements WorkspaceReader {

  static var WORKSPACE_ID : String as WORKSPACE_ID = "workspace"

  var _repository = new WorkspaceRepository( WORKSPACE_ID )
  var _localModules : Map<String, Model> = {}

  override property get Repository( ) : org.sonatype.aether.repository.WorkspaceRepository {
    return _repository
  }

  override function findArtifact( art : Artifact ) : java.io.File {
    var pom = findLocalModule( art.GroupId, art.ArtifactId )

    // Non-local artifact
    if (pom == null) {
      return null;
    }

    // Version mismatch!
    if ( art.Version != pom.Version ) {
      return null;
    }

    if (art.Extension == "pom") {
      return pom.PomFile
    }

    var dir = new File( pom.Build.Directory )
    var name = !art.Classifier.Empty ?
        "${art.ArtifactId}-${art.Version}-${art.Classifier}.${art.Extension}" :
        pom.Build.FinalName + "." + art.Extension
    var file = new File(dir, name)
    return file;
  }

  override function findVersions( art : Artifact ) : java.util.List<String> {
    var pom = findLocalModule( art.GroupId, art.ArtifactId )
    return pom != null ? { pom.Version } : { }
  }

  property get LocalModules( ) : Collection<Model> {
    return _localModules.values( )
  }

  /**
   * Import POM file recursively into the workspace.
   */
  function importModule( pomFile : File, profiles : List<String> = null ) : Model {
    var pom = Aether.buildMavenModel(pomFile, :profiles = profiles).EffectiveModel
    _localModules.put(pom.GroupId + ':' + pom.ArtifactId, pom)
    for (var module in pom.Modules) {
      var childFile = pomFile.ParentFile.file("${module}/pom.xml")
      importModule(childFile)
    }
    return pom
  }

  function findLocalModule( groupId : String, artifactId : String ) : Model {
    return _localModules[groupId + ':' + artifactId]
  }
}