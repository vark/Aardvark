package gw.vark.aether

uses org.apache.maven.model.Model

class Pom {

  var _model : Model as Model
  var _id : String as Id
  var _file : File as File
  var _groupId : String as GroupId
  var _artifactId : String as ArtifactId
  var _version : String as Version
  var _packaging : String as Packaging
  // var _remoteRepos : RemoteRepositories

}
