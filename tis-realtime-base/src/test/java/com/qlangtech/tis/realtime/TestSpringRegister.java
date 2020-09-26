/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.realtime;

import junit.framework.TestCase;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TestSpringRegister extends TestCase {

    public void test() {
        final String beanName = "singlebean1";
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:/com/qlangtech/tis/realtime/spring-test-context.xml") {

            protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
                DefaultListableBeanFactory factory = (DefaultListableBeanFactory) beanFactory;
                BeanDefinitionBuilder define = BeanDefinitionBuilder.genericBeanDefinition(SingletenInstance.class);
                define.setLazyInit(true);
                // factory.registerBeanDefinition(beanName, define.getBeanDefinition());
                factory.registerSingleton(beanName, new SingletenInstance());
            }
        };
        assertNotNull(context.getBean(beanName));
    }
}
