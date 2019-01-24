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

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroup;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroupCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerPool;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerPoolCriteria;
import com.qlangtech.tis.manage.spring.aop.Func;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import junit.framework.Assert;

/*
 * 响应所有组内的请求
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class GroupAction extends BasicModule {

    private static final long serialVersionUID = 1L;

    // @Func(PermissionConstant.PERMISSION_BASE_DATA_MANAGE)
    public void doChangeEnvironment(Context context) throws Exception {
        Integer appid = this.getInt("appid");
        Integer runtime = this.getInt("runtime");
        ServerGroupCriteria query = new ServerGroupCriteria();
        query.createCriteria().andRuntEnvironmentEqualTo(runtime.shortValue()).andAppIdEqualTo(appid).andNotDelete();
        query.setOrderByClause("group_index desc");
        List<ServerGroup> queryResult = this.getServerGroupDAO().selectByExample(query);
        int groupIndex = 0;
        for (com.qlangtech.tis.manage.biz.dal.pojo.ServerGroup group : queryResult) {
            groupIndex = group.getGroupIndex() + 1;
            break;
        }
        getResponse().getWriter().write(String.valueOf(groupIndex));
    }

    @Override
    public boolean isEnableDomainView() {
        return false;
    }

    /**
     * @param context
     * @throws Exception
     */
    public void doAddServerSuggest(Context context) throws Exception {
        // String value = " 	{"
        // + " query:'Li',"
        // +
        // " suggestions:['Liberia','Libyan Arab Jamahiriya','Liechtenstein','Lithuania'],"
        // + " data:['LR','LY','LI','LT']" + "}";
        final String query = this.getString("query");
        final RunEnvironment envir = RunEnvironment.getEnum(this.getString("envir"));
        List<ServerPool> serverList = Collections.emptyList();
        if (StringUtils.isNotBlank(query)) {
            ServerPoolCriteria criteria = new ServerPoolCriteria();
            ServerPoolCriteria.Criteria and = criteria.createCriteria().andIpAddressLike('%' + query + '%').andRuntEnvironmentEqualTo(envir.getId());
            criteria.or(criteria.createCriteria().andServerNameLike('%' + query + '%').andRuntEnvironmentEqualTo(envir.getId()));
            serverList = this.getServerPoolDAO().selectByExample(criteria);
        }
        writeSuggest2Response(query, serverList, new SuggestCallback<ServerPool>() {

            @Override
            public String getLiteral(ServerPool o) {
                return o.getServerName() + "[" + o.getIpAddress() + "]";
            }

            @Override
            public Object getValue(ServerPool o) {
                return o.getSpId();
            }
        }, getResponse());
    }

    public static <T> void writeSuggest2Response(final String query, List<T> serverList, SuggestCallback<T> callback, HttpServletResponse response) throws JSONException, IOException {
        JSONArray suggestions = new JSONArray();
        JSONArray d = new JSONArray();
        for (int i = 0; i < serverList.size(); i++) {
            T pool = serverList.get(i);
            suggestions.put(i, callback.getLiteral(pool));
            d.put(i, callback.getValue(pool));
        }
        JSONObject json = new JSONObject();
        json.put("query", query);
        json.put("suggestions", suggestions);
        json.put("data", d);
        response.setContentType("json;charset=UTF-8");
        // this.getResponse().getWriter().write(j);
        json.write(response.getWriter());
    }

    public interface SuggestCallback<T> {

        public Object getValue(T o);

        public String getLiteral(T o);
    }

    /**
     * 添加group页面，选择环境下拉框触发事件
     *
     * @param context
     * @throws Exception
     */
    /**
     * 添加一个组
     *
     * @param context
     * @throws Exception
     */
    @Func(PermissionConstant.APP_SERVER_GROUP_SET)
    public void doAddGroup(Context context) throws Exception {
        // this.getAppDomain().getRunEnvironment()
        RunEnvironment runtime = RunEnvironment.getEnum(this.getShort("runtime"));
        Integer groupIndex = this.getInt("groupIndex");
        Integer appid = this.getInt("appid");
        if (groupIndex == null) {
            this.addErrorMessage(context, "请填写组编号");
            return;
        }
        if (runtime == null) {
            this.addErrorMessage(context, "请选择运行环境");
            return;
        }
        createGroup(context, runtime, groupIndex, appid, this);
        this.addActionMessage(context, "【" + runtime.getDescribe() + "】中新创建一条服务器组成功");
    }

    public static Integer createGroup(Context context, RunEnvironment runtime, Integer groupIndex, Integer appid, BasicModule basicModule) {
        Assert.assertNotNull(appid);
        ServerGroupCriteria query = new ServerGroupCriteria();
        query.createCriteria().andRuntEnvironmentEqualTo(runtime.getId()).andGroupIndexEqualTo(groupIndex.shortValue()).andAppIdEqualTo(appid).andNotDelete();
        if (basicModule.getServerGroupDAO().countByExample(query) > 0) {
            basicModule.addErrorMessage(context, "gruop index重复");
            return null;
        }
        ServerGroup group = new ServerGroup();
        group.setRuntEnvironment(runtime.getId());
        group.setGroupIndex(groupIndex.shortValue());
        group.setCreateTime(new Date());
        group.setAppId(appid);
        return basicModule.getServerGroupDAO().insertSelective(group);
    }

    /**
     * 添加服务器
     *
     * @param context
     * @throws Exception
     */
    // @Func(PermissionConstant.APP_SERVER_SET)
    // public void doAddServer(Context context) throws Exception {
    // 
    // Integer serverid = this.getInt("server");
    // 
    // if (serverid == null) {
    // this.addErrorMessage(context, "请选择服务器");
    // return;
    // }
    // 
    // Integer gid = this.getInt("gid");
    // Assert.assertNotNull("gid can not be null", gid);
    // 
    // ServerPool serverPool = this.getServerPoolDAO().loadFromWriteDB(
    // serverid);
    // 
    // Assert.assertNotNull("the relevant serverid:" + serverid
    // + " of object can not be null", serverPool);
    // 
    // ServerCriteria query = new ServerCriteria();
    // query.createCriteria().andGidEqualTo(gid).andSpIdEqualTo(serverid);
    // if (this.getServerDAO().countFromWriteDB(query) > 0) {
    // this.addErrorMessage(context, "不能向同一个组中重复添加服务器");
    // return;
    // }
    // Server server = new Server();
    // server.setSpId(serverid);
    // server.setCreateTime(new Date());
    // server.setIpAddress(serverPool.getIpAddress());
    // server.setServerName(serverPool.getServerName());
    // server.setGid(gid);
    // 
    // this.addActionMessage(context, "成功创建服务器ID："
    // + this.getServerDAO().insertSelective(server));
    // }
    /**
     * 删除服务器
     *
     * @param context
     * @throws Exception
     */
    @Func(PermissionConstant.APP_SERVER_SET)
    public void doDeleteServer(Context context) throws Exception {
        Integer serverId = this.getInt("serverid");
        Assert.assertNotNull("serverId can not be null", serverId);
        // Server server = new Server();
        // // 设置删除
        // server.setDeleteFlag(1);
        // ServerCriteria criteria = new ServerCriteria();
        // criteria.createCriteria(false).andSidEqualTo(serverId);
        // 
        // this.getServerDAO().updateByExampleSelective(server, criteria);
        // 直接 物理删除
        // this.getServerDAO().deleteByPrimaryKey(serverId);
        getResponse().getWriter().write("delete success");
    }

    /**
     * 删除服务器组
     *
     * @param context
     * @throws Exception
     */
    @Func(PermissionConstant.APP_SERVER_GROUP_SET)
    public void doDeleteGroup(Context context) throws Exception {
        final Integer groupid = this.getInt("groupid");
        Assert.assertNotNull("groupid can not be null", groupid);
        // ServerCriteria squery = new ServerCriteria();
        // squery.createCriteria(false).andGidEqualTo(groupid);
        // Server server = new Server();
        // server.setDeleteFlag(ManageUtils.DELETE);
        // 删除依赖于group的所有server
        // this.getServerDAO().updateByExampleSelective(server, squery);
        // this.getServerDAO().deleteByExample(squery);
        // ServerGroup group = new ServerGroup();
        // group.setDeleteFlag(ManageUtils.DELETE);
        ServerGroupCriteria query = new ServerGroupCriteria();
        query.createCriteria().andGidEqualTo(groupid);
        // 删除group
        this.getServerGroupDAO().deleteByExample(query);
        this.addActionMessage(context, "成功删除组ID：" + groupid);
    }
}
