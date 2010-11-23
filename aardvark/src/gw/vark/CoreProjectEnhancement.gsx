/*
 * Copyright (c) 2010 Guidewire Software, Inc.
 */

package gw.vark

uses org.apache.tools.ant.Project
uses org.apache.tools.ant.Target

enhancement CoreProjectEnhancement : Project {

  function registerTarget(name : String, op() : void) : Target {
    var target = new Target() {
      override function execute() {
        if (op != null) {
          op()
        }
      }
    }
    target.Name = name
    this.addTarget(target)
    return target
  }

}
