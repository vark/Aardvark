package gw.vark.maven

uses org.apache.maven.model.Dependency

enhancement DependencyEnhancement : Dependency {

  property get Id() : String {
    return this.GroupId + ":" + this.ArtifactId + ":" + this.Type + ":" + this.Version
  }

  property get ShortId() : String {
    if (this.GroupId == PomHelper.DefaultGroupId) {
      return this.ArtifactId
    }
    return this.Id
  }

}
