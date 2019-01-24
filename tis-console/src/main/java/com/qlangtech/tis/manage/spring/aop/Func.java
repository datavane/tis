/**
 * 
 */
package com.qlangtech.tis.manage.spring.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 百岁 （莫正华 baisui@taobao.com）
 * 
 *         2011-10-12 下午05:13:03
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE, ElementType.METHOD })
public @interface Func {
	String value();
}
