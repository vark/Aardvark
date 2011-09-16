package gw.vark.mavenish

uses org.apache.maven.model.Dependency

enhancement DependencyEnhancement : Dependency {

  property get Id() : String {
    return this.GroupId + ":" + this.ArtifactId + ":" + this.Type + ":" + this.Version
  }

}
