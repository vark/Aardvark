package gw.vark.aether

uses org.sonatype.aether.spi.log.Logger
uses gw.vark.Aardvark
uses org.apache.tools.ant.Project

/**
 */
class AardvarkLogger implements Logger {
  override property get DebugEnabled(): boolean {
    return false
  }

  override property get WarnEnabled(): boolean {
    return true
  }

  override function debug(msg: java.lang.String) {
    Aardvark.getProject().log(msg, Project.MSG_DEBUG)
  }

  override function debug(msg: java.lang.String, error: java.lang.Throwable) {
    Aardvark.getProject().log(msg, error, Project.MSG_DEBUG)
  }

  override function warn(msg: java.lang.String) {
    Aardvark.getProject().log(msg, Project.MSG_WARN)
  }

  override function warn(msg: java.lang.String, error: java.lang.Throwable) {
    Aardvark.getProject().log(msg, error, Project.MSG_WARN)
  }
}