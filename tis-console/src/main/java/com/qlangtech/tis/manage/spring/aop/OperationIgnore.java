/**
 * 
 */
package com.qlangtech.tis.manage.spring.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Action操作不会被记录
 * @author 百岁
 *
 * @date 2019年2月22日
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface OperationIgnore {
	
}
