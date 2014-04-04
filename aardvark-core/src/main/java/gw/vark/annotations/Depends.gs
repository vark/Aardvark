/*
 * Copyright (c) 2012 Guidewire Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gw.vark.annotations

uses gw.lang.reflect.features.MethodReference
uses gw.lang.annotation.AnnotationUsage

/**
  * Declare a dependency on another target.
  */
@AnnotationUsage(gw.lang.annotation.UsageTarget.MethodTarget, gw.lang.annotation.UsageModifier.One)
class Depends implements IAnnotation, gw.vark.IDepends {
  var _dependencies : List<String>

  construct() {
    _dependencies = { }
  }

  construct(dependency : MethodReference<?, ?>) {
    _dependencies = { dependency.MethodInfo.DisplayName }
  }

  construct(dependencies : List<MethodReference<?, ?>>) {
    _dependencies = dependencies.map(\x -> x.MethodInfo.DisplayName)
  }

  override function dependencies() : List<String> {
    return _dependencies
  }
}
