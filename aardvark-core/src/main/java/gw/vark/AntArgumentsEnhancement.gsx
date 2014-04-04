package gw.vark

uses org.apache.tools.ant.types.Commandline

/**
 * Utility methods to work with gw.vark.antlibs command line arguments list.
 */
enhancement AntArgumentsEnhancement : List<String> {
  /**
   * Convert list of string to the list of command-line argument blocks for jvmargBlocks/argBlocks of Ant.java(...)
   */
  function toArgumentBlocks( ) : List<block(a : Commandline.Argument)> {
    return this.map( \ elt -> (\arg : Commandline.Argument -> arg.setValue(elt)))
  }
}
