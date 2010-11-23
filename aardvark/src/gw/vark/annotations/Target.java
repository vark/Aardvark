/*
 * Copyright (c) 2010 Guidewire Software, Inc.
 */

package gw.vark.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>This annotation allows non-user defined targets to be contributed by enhancements on
 * gw.vark.AardvarkFile.</p>
 * <p>It is <bold>NOT</bold> to be used in vark files, and will be ignored.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Target {
}
