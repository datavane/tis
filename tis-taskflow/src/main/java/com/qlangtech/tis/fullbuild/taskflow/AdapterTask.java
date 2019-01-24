/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.fullbuild.taskflow;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import com.alibaba.fastjson.util.IOUtils;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class AdapterTask implements ITask {

    private String content;

    private TemplateContext context;

    private static final VelocityEngine velocityEngine;

    static {
        try {
            velocityEngine = new VelocityEngine();
            Properties prop = new Properties();
            prop.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogChute");
            velocityEngine.init(prop);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void exexute(Map<String, String> params) {
        String sql = mergeVelocityTemplate(params);
        executeSql(this.getName(), sql);
    }

    protected String mergeVelocityTemplate(Map<String, String> params) {
        StringWriter writer = new StringWriter();
        try {
            velocityEngine.evaluate(createContext(params), writer, "sql", this.getContent());
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.close(writer);
        }
    }

    protected abstract void executeSql(String taskname, String sql);

    private VelocityContext createContext(Map<String, String> params) {
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("context", this.getContext());
        for (Map.Entry<String, String> entry : params.entrySet()) {
            velocityContext.put(entry.getKey(), entry.getValue());
        }
        return velocityContext;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public TemplateContext getContext() {
        return context;
    }

    public void setContext(TemplateContext context) {
        this.context = context;
    }

    @Override
    public void exexute() {
        exexute(new HashMap<String, String>());
    }

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
