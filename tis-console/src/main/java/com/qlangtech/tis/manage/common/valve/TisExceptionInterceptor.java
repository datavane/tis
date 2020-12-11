/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.manage.common.valve;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.MethodFilterInterceptor;
import com.qlangtech.tis.lang.TisException;
import com.qlangtech.tis.manage.common.MockContext;
import com.qlangtech.tis.manage.common.TisActionMapper;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.mapper.ActionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 拦截系统异常，以控制页面友好
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年1月23日 下午2:33:00
 */
public class TisExceptionInterceptor extends MethodFilterInterceptor {

  private static final long serialVersionUID = 1L;

  private static final Logger logger = LoggerFactory.getLogger(TisExceptionInterceptor.class);

  private PlatformTransactionManager transactionManager;

  @Autowired
  public void setTransactionManager(PlatformTransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }

  @Override
  protected String doIntercept(ActionInvocation invocation) throws Exception {
    HttpServletResponse response = ServletActionContext.getResponse();
    TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
    final ActionMapping mapping = ServletActionContext.getActionMapping();
    AjaxValve.ActionExecResult execResult = null;
    try {
      final String result = invocation.invoke();
      // 一定要invoke之后再执行
      execResult = MockContext.getActionExecResult();
      if (!execResult.isSuccess()) {
        // 业务失败也要回滚
        transactionManager.rollback(status);
        return result;
      }
      if (!status.isCompleted()) {
        transactionManager.commit(status);
      }
      return result;
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      if (!status.isCompleted()) {
        transactionManager.rollback(status);
      }
      if (TisActionMapper.REQUEST_EXTENDSION_AJAX.equals(mapping.getExtension())) {
        // logger.error(e.getMessage(), e);
        List<String> errors = new ArrayList<String>();
        errors.add("服务端发生异常，请联系系统管理员");

        final Throwable[] throwables = ExceptionUtils.getThrowables(e);
        boolean findTisException = false;
        for (Throwable ex : throwables) {
          if (TisException.class.isAssignableFrom(ex.getClass())) {
            errors.add(ex.getMessage());
            findTisException = true;
            break;
          }
        }
        if (!findTisException) {
          errors.add(ExceptionUtils.getRootCauseMessage(e));
        }
        AjaxValve.writeInfo2Client(() -> false, response, false, errors, Collections.emptyList(), Collections.emptyList(), null);
        return Action.NONE;
      } else {
        throw e;
      }
    }
  }
}
