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
package com.qlangtech.tis.manage.spring.aop;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.io.Text;
import org.apache.struts2.ServletActionContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.aop.AfterReturningAdvice;
import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;
import com.qlangtech.tis.manage.biz.dal.dao.IOperationLogDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.OperationLog;
import com.qlangtech.tis.manage.biz.dal.pojo.UploadResource;
import com.qlangtech.tis.manage.common.DefaultFilter;
import com.qlangtech.tis.manage.common.IUser;
import com.qlangtech.tis.manage.common.ManageUtils;
import com.qlangtech.tis.manage.common.OperationDomainLogger;
import com.qlangtech.tis.manage.common.OperationLogger;
import com.qlangtech.tis.manage.common.UserUtils;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;

/*
 * 当目标操作执行成功之后 执行
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class OperationLogAdvice implements AfterReturningAdvice {

    public static Logger logger = LoggerFactory.getLogger(OperationLogAdvice.class);

    private IOperationLogDAO operationLogDAO;

    // private static final java.util.regex.Pattern op = java.util.regex.Pattern
    // .compile("update|delete|insert");
    private static final XStream xstream = new XStream(new JsonHierarchicalStreamDriver());

    static {
        xstream.omitField(UploadResource.class, "content");
    // xstream.addImplicitCollection(Owner.class, "list");
    }

    // @SuppressWarnings("unchecked")
    @Override
    public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
        OperationLog log = new OperationLog();
        log.setCreateTime(new Date());
        // SecurityContextHolder.getContext().getUser();
        IUser user = UserUtils.getUserFromCache(ServletActionContext.getRequest());
        if (user == null) {
            throw new IllegalStateException("user can not be null");
        }
        log.setTabName(((OperationLogger) target).getEntityName());
        log.setUsrId(user.getId());
        log.setUsrName(user.getName());
        log.setOpType(getOpType(method));
        for (Object arg : args) {
            if (arg instanceof OperationDomainLogger) {
                OperationDomainLogger domainLogger = (OperationDomainLogger) arg;
                if (!domainLogger.isLogHasBeenSet()) {
                    break;
                }
                if (domainLogger.getOpDesc() != null) {
                    log.setOpDesc(domainLogger.getOpDesc());
                }
                log.setAppName(domainLogger.getOperationLogAppName());
                log.setMemo(domainLogger.getOperationLogMemo());
                log.setRuntime(domainLogger.getOperationLogRuntime());
                break;
            }
        // else if (arg instanceof CoreRequest) {
        // setOperationLogDesc(log, (CoreRequest) arg);
        // break;
        // }
        }
        if (StringUtils.isEmpty(log.getOpDesc())) {
            log.setOpDesc(StringUtils.left(xstream.toXML(args), 5000));
        }
        // Integer userId = 0;
        // try {
        // userId = Integer.parseInt(user.getId());
        // } catch (Throwable e) {
        // }
        operationLogDAO.insert(log);
    // new Xu("KN_terminatorconsole").userId(userId)
    // .userNick(log.getUsrName()).bizId(log.getTabName()).ip(
    // InetAddress.getLocalHost().getHostAddress())
    // .operateType(log.getOpType()).operateContent(
    // StringUtils.trimToEmpty(log.getOpDesc()).replaceAll(
    // "\r|\n", StringUtils.EMPTY)).log();
    }

    private String getOpType(Method method) {
        return StringUtils.left(method.getName(), 40);
    }

    public IOperationLogDAO getOperationLogDAO() {
        return operationLogDAO;
    }

    public void setOperationLogDAO(IOperationLogDAO operationLogDAO) {
        this.operationLogDAO = operationLogDAO;
    }

    public static void main(String[] arg) {
    }
}
