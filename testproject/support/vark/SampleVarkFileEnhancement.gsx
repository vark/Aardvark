package vark

enhancement SampleVarkFileEnhancement : gw.vark.AardvarkFile {

  /* This is a target provided by this enhancement */
  @gw.vark.annotations.Target
  function targetFromEnhancment() {
    print( "yay" )
  }

  /* This is not a target */
  function notATargetFromEnhancment() {
    print( "yay" )
  }

}