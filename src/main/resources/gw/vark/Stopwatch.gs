package gw.vark

uses java.lang.System
uses java.lang.Long

class Stopwatch implements IAardvarkUtils {

  var _name : String
  var _total = 0L
  var _start = Long.MIN_VALUE

  construct() {
    this("Stopwatch")
  }

  construct(name : String) {
    _name = name
  }

  function start() {
    _start = System.nanoTime()
  }

  function stop() {
    _total += calcDelta()
    _start = Long.MIN_VALUE
  }

  function reset() {
    if (_start != Long.MIN_VALUE) {
      start()
    }
    _total = 0
  }

  function print() {
    var total = _total
    if (_start != Long.MIN_VALUE) {
      total += calcDelta()
    }
    log(_name + ": " + (total / 1000 / 1000) + " ms")
  }

  private function calcDelta() : long {
    return System.nanoTime() - _start
  }

}
