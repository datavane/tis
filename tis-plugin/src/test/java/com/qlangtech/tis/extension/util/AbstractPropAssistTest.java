/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qlangtech.tis.extension.util;


import com.qlangtech.tis.extension.util.AbstractPropAssist.MarkdownHelperContent;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2025-08-18 21:38
 **/
public class AbstractPropAssistTest extends TestCase {

    public void testHelperContent() {
        MarkdownHelperContent helperContent = new MarkdownHelperContent(PluginExtraProps.AsynPropHelp.create( "xxxx\n\n\n\n\n\n\n\nppp\n"));
        String expect = "xxxx\n\nppp";
        Assert.assertEquals("shall be equal.", expect, helperContent.getContent().toString());

        helperContent.append(new MarkdownHelperContent( PluginExtraProps.AsynPropHelp.create( "kkkkk")));

        Assert.assertEquals(expect + "\n\nkkkkk", helperContent.getContent().toString());

        helperContent.append(new MarkdownHelperContent( PluginExtraProps.AsynPropHelp.create( "\nqqq")));

        Assert.assertEquals(expect + "\n\nkkkkk\n\nqqq", helperContent.getContent().toString());
    }

}