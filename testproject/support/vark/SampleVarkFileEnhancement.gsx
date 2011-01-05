package vark

enhancement SampleVarkFileEnhancement : gw.vark.AardvarkFile {

  /* This is a target provided by this enhancement */
  @gw.vark.annotations.Target
  function targetFromEnhancement() {
    print( "yay" )
  }

  /* This is not a target */
  function notATargetFromEnhancement() {
    print( "boo" )
  }

}