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
package com.qlangtech.tis.runtime.module.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import junit.framework.Assert;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.biz.dal.dao.IDepartmentDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.Department;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptRelation;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptRelationCriteria;
import com.qlangtech.tis.manage.common.IUser;
import com.qlangtech.tis.manage.common.RunContext;
import com.qlangtech.tis.manage.common.UserUtils;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class OrgAuthorityAction extends BasicModule {

    /**
     */
    private static final long serialVersionUID = 1L;

    // private static final Log log = LogFactory.getLog(OrgAuthorityAction.class);
    private static final XStream xstream = new XStream(new JsonHierarchicalStreamDriver());

    static {
        xstream.alias("result", JsonResult.class);
    // xstream.addImplicitCollection(JsonResult.class, "result");
    }

    @Override
    public boolean isEnableDomainView() {
        return false;
    }

    /**
     * 删除授权
     *
     * @param context
     */
    // @Func(PermissionConstant.PERMISSION_BASE_DATA_MANAGE)
    // public void doDeleteOrgAuthority(Context context) throws Exception {
    // // 'appid': appid,
    // // 'orgid':orgid,
    // // 'func':func_code
    // Integer appid = this.getInt("appid");
    // Integer orgid = this.getInt("orgid");
    // String funcCode = this.getString("func");
    // 
    // JsonResult result = new JsonResult();
    // 
    // BizFuncAuthorityCriteria criteria = new BizFuncAuthorityCriteria();
    // criteria.createCriteria().andAppIdEqualTo(appid).andDptIdEqualTo(orgid)
    // .andFuncIdEqualTo(funcCode);
    // 
    // if (this.getBizFuncAuthorityDAO().deleteByExample(criteria) > 0) {
    // result.addMsg("成功删除 appid:" + appid + " orgid:" + orgid + " func:"
    // + funcCode + " 的授权记录");
    // result.addDeleteId(funcCode + '_' + appid + '_' + orgid);
    // } else {
    // result.addError("删除失败， appid:" + appid + " orgid:" + orgid
    // + " func:" + funcCode + " 的授权记录");
    // }
    // 
    // this.getResponse().setContentType("application/json");
    // xstream.toXML(result, this.getResponse().getWriter());
    // }
    /**
     * 设置部门信息
     *
     * @param context
     */
    public void doSetDepartment(Context context) throws Exception {
        // 查看用户是否已经设置部门
        getResponse().setContentType("application/json");
        // 部门id
        Integer departmentId = this.getInt("orgadd");
        if (departmentId == null) {
            throw new IllegalArgumentException("departmentId can not be null");
        }
        UsrDptRelationCriteria query = new UsrDptRelationCriteria();
        query.createCriteria().andUsrIdEqualTo(this.getUserId());
        if (this.getUsrDptRelationDAO().countByExample(query) > 0) {
            // 已经设置过部门信息了
            getResponse().getWriter().print("{result:'您已经设置过部门信息了,如果要更改部门设置请通知管理员'}");
            return;
        }
        bindUser2Dpt(this, departmentId, UserUtils.getUser(this.getRequest(), this));
        getResponse().getWriter().print("{result:'您已成功设置部门信息'}");
    }

    public static void bindUser2Dpt(RunContext runContext, Integer departmentId, IUser user) {
        Assert.assertNotNull(user);
        UsrDptRelationCriteria query;
        UsrDptRelation relation = new UsrDptRelation();
        relation.setCreateTime(new Date());
        relation.setDptId(departmentId);
        relation.setDptName(getDepartmentName(runContext.getDepartmentDAO(), departmentId));
        relation.setUsrId(user.getId());
        relation.setUserName(user.getName());
        // relation.setIsDeleted("N");
        query = new UsrDptRelationCriteria();
        query.createCriteria().andUsrIdEqualTo(user.getId());
        Assert.assertNotNull(user.getId());
        runContext.getUsrDptRelationDAO().deleteByExample(query);
        runContext.getUsrDptRelationDAO().insert(relation);
    }

    public static String getDepartmentName(IDepartmentDAO departmentDAO, Integer dptId) {
        List<Department> dptlist = new ArrayList<Department>();
        processDepartment(departmentDAO, dptlist, dptId);
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < dptlist.size(); i++) {
            result.append(dptlist.get(i).getName());
            if (i != (dptlist.size() - 1)) {
                result.append("-");
            }
        }
        return result.toString();
    }

    /**
     * @param result
     * @param dptId
     */
    public static void processDepartment(IDepartmentDAO departmentDAO, List<Department> dptlist, Integer dptId) {
        Department department = departmentDAO.loadFromWriteDB(dptId);
        if (department == null) {
            return;
        }
        if (department.getParentId() == null) {
            dptlist.add(department);
            return;
        }
        processDepartment(departmentDAO, dptlist, department.getParentId());
        dptlist.add(department);
    }

    public static class JsonResult {

        private final List<String> error = new ArrayList<String>();

        private final List<String> msg = new ArrayList<String>();

        private final List<String> deleteids = new ArrayList<String>();

        public void addDeleteId(String deleteid) {
            this.deleteids.add(deleteid);
        }

        public void addError(String error) {
            this.error.add(error);
        }

        public void addMsg(String msg) {
            this.msg.add(msg);
        }

        public List<String> getErrors() {
            return error;
        }

        public List<String> getMsg() {
            return msg;
        }
    }

    public static class Entry {

        private final Integer id;

        private final String name;

        Entry(Integer id, String name) {
            super();
            this.id = id;
            this.name = name;
        }

        public Integer getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            return this.hashCode() == obj.hashCode();
        }

        @Override
        public int hashCode() {
            return id;
        }
    }

    // private List<Integer> parseIds(final String appRemove) {
    // String[] appid = StringUtils.split(appRemove, ",");
    // List<Integer> intAppid = new ArrayList<Integer>();
    // if (appid == null) {
    // return intAppid;
    // }
    // for (String id : appid) {
    // try {
    // intAppid.add(Integer.parseInt(id));
    // } catch (Throwable e) {
    // }
    // }
    // return intAppid;
    // }
    public static void main(String[] arg) {
        // Set<Entry> orgIdSet = new HashSet<Entry>();
        // 
        // orgIdSet.add(new Entry(1, "abc"));
        // orgIdSet.add(new Entry(2, "bca"));
        // 
        // System.out.println(orgIdSet.size());
        // Map<String, String> map = new HashMap<String, String>();
        // map.put("aaa", "bbb");
        // 
        // XStream xstream = new XStream(new JettisonMappedXmlDriver());
        // 
        // System.out.println(JsonUtil.toString(map));
        JsonResult result = new JsonResult();
        System.out.println(xstream.toXML(result));
    }
}
