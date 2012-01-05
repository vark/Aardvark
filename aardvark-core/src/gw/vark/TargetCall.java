/*
 * Copyright (c) 2010 Guidewire Software, Inc.
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

package gw.vark;

import java.util.HashMap;
import java.util.Map;

/**
 * A target call derived from {@link AardvarkOptions}, with possible param values.
 */
public class TargetCall {

  private final String _targetName;
  private final Map<String, String> _params = new HashMap<String, String>();

  public TargetCall(String _targetName) {
    this._targetName = _targetName;
  }

  public void addParam(String paramName, String paramVal) {
    _params.put(paramName, paramVal);
  }

  public String getName() {
    return _targetName;
  }

  public Map<String, String> getParams() {
    return new HashMap<String, String>(_params);
  }
}
