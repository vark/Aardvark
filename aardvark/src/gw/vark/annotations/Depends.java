/*
 * Copyright (c) 2010 Guidewire Software, Inc.
 */

package gw.vark.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Depends {
  String[] value() default {};
}
