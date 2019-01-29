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
package com.qlangtech.tis.trigger.module.screen;

import org.springframework.beans.factory.annotation.Autowired;
import com.qlangtech.tis.runtime.module.action.BasicModule;
import com.qlangtech.tis.trigger.biz.dal.dao.IJobMetaDataDAO;
import com.qlangtech.tis.trigger.biz.dal.dao.ITaskDAO;
import com.qlangtech.tis.trigger.biz.dal.dao.ITaskExecLogDAO;
import com.qlangtech.tis.trigger.biz.dal.dao.ITerminatorTriggerBizDalDAOFacade;
import com.qlangtech.tis.trigger.biz.dal.dao.ITriggerJobDAO;
import com.qlangtech.tis.trigger.rmi.TriggerJobConsole;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class TriggerBasicModule extends // implements
BasicModule {

    // ITerminatorTriggerBizDalDAOFacade
    private static final long serialVersionUID = 1L;

    private ITerminatorTriggerBizDalDAOFacade triggerContext;

    // protected ITerminatorTriggerBizDalDAOFacade getTriggerContext() {
    // return triggerContext;
    // }
    private IJobMetaDataDAO jobMetaDataDAO;

    private TriggerJobConsole triggerJobConsole;

    @Autowired
    public final void setTriggerJobConsole(TriggerJobConsole triggerJobConsole) {
        this.triggerJobConsole = triggerJobConsole;
    }

    // private ITerminatorTriggerBizDalDAOFacade triggerBizDalDAOFacade;
    // public ITerminatorTriggerBizDalDAOFacade getTriggerDaoContext() {
    // return triggerDaoContext;
    // }
    public final TriggerJobConsole getTriggerJobConsole() {
        return triggerJobConsole;
    }

    // terminatorTriggerBizDalDaoFacade
    @Autowired
    public void setTisTriggerBizDalDaoFacade(ITerminatorTriggerBizDalDAOFacade triggerDaoContext) {
        this.triggerContext = triggerDaoContext;
    }

    public ITerminatorTriggerBizDalDAOFacade getTerminatorTriggerBizDalDaoFacade() {
        return this.triggerContext;
    }

    public IJobMetaDataDAO getJobMetaDataDAO() {
        return jobMetaDataDAO;
    }

    @Autowired
    public void setJobMetaDataDAO(IJobMetaDataDAO jobMetaDataDAO) {
        this.jobMetaDataDAO = jobMetaDataDAO;
    }

    // @Override
    public ITaskDAO getTaskDAO() {
        return this.triggerContext.getTaskDAO();
    }

    // @Override
    public ITaskExecLogDAO getTaskExecLogDAO() {
        return this.triggerContext.getTaskExecLogDAO();
    }

    // @Override
    public ITriggerJobDAO getTriggerJobDAO() {
        return this.triggerContext.getTriggerJobDAO();
    }
}
