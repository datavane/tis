/**
 *
 */
package com.qlangtech.tis.manage.spring.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * 2011-10-12 下午05:13:03
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Func {
  String value();

  boolean sideEffect() default true;
}
