package gw.vark.aether

uses org.sonatype.aether.ant.types.Dependency

enhancement DependencyEnhancement : Dependency {

  property set Coords(coords : String) {
    this.setCoords(coords)
  }

}
