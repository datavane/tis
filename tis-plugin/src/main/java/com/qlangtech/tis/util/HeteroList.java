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
package com.qlangtech.tis.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.IPropertyType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class HeteroList<T extends Describable<T>> {

    private List<Descriptor<T>> descriptors;

    private List<T> items = new ArrayList<>();

    private String caption;
    private String identityId;

    private Class<?> extensionPoint;

    // 标志Item可以选几个
    private Selectable selectable;
    private final UploadPluginMeta pluginMeta;


    public HeteroList(UploadPluginMeta pluginMeta) {
        this.pluginMeta = pluginMeta;
    }

    public Selectable getSelectable() {
        return selectable;
    }

    public void setSelectable(Selectable selectable) {
        this.selectable = selectable;
    }

    public List<Descriptor<T>> getDescriptors() {
        return descriptors;
    }

    public Class<?> getExtensionPoint() {
        return this.extensionPoint;
    }

    public void setExtensionPoint(Class<?> extensionPoint) {
        this.extensionPoint = extensionPoint;
    }

    public void setIdentityId(String identity) {
        this.identityId = identity;
    }

    public void setDescriptors(List<Descriptor<T>> descriptors) {
        this.descriptors = descriptors;
    }

    public List<T> getItems() {
        return this.items == null ? Collections.emptyList() : this.items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public void addItem(T item) {
        this.items.add(item);
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public JSONObject toJSON() throws Exception {
        JSONObject o = new JSONObject();
        o.put("caption", this.getCaption());
        o.put("identityId", this.identityId);
        o.put("cardinality", this.getSelectable().identity);
        o.put("extensionPoint", this.extensionPoint.getName());

        Optional<IPropertyType.SubFormFilter> subFormFilter = pluginMeta.getSubFormFilter();
        DescriptorsJSON desc2Json = new DescriptorsJSON(this.descriptors);
        o.put("descriptors", desc2Json.getDescriptorsJSON(subFormFilter));
        JSONArray items = new JSONArray();
        JSONObject item = null;

        for (T i : this.getItems()) {
            item = (new DescribableJSON(i)).getItemJson(subFormFilter);
            items.add(item);
        }
        o.put("items", items);

        return o;
    }


}
