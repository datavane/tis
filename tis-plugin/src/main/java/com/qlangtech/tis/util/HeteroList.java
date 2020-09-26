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
package com.qlangtech.tis.util;

import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import org.json.JSONArray;
import org.json.JSONObject;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class HeteroList<T extends Describable<T>> {

    private List<Descriptor<T>> descriptors;

    private List<T> items = new ArrayList<>();

    private String caption;

    private Class<?> extensionPoint;

    // 标志Item可以选几个
    private Selectable selectable;

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
        // = items;
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
        o.put("cardinality", this.getSelectable().identity);
        o.put("extensionPoint", this.extensionPoint.getName());
        // JSONObject des = null;
        // JSONObject attrVal = null;
        // String key = null;
        // Descriptor.PropertyType val = null;
        // FormField fieldAnnot = null;
        // Map<Class<T> /*descriptor impl*/, Set<String>> descPropsMap = Maps.newHashMap();
        DescriptorsJSON desc2Json = new DescriptorsJSON(this.descriptors);
        JSONObject descriptors = desc2Json.getDescriptorsJSON();
        o.put("descriptors", descriptors);
        JSONArray items = new JSONArray();
        JSONObject item = null;
        Descriptor<T> descriptor = null;
        for (T i : this.getItems()) {
            item = (new DescribableJSON(i)).getItemJson();
            items.put(item);
        }
        o.put("items", items);
        return o;
    }

    public static <T extends Describable<T>> HeteroList<T> getHeteroList(String caption, List<T> items, Class<T> clazz) {
        HeteroList<T> hList = new HeteroList<>();
        hList.setCaption(caption);
        hList.setItems(items);
        try {
            Class<T> componentType = clazz;
            if (componentType == null) {
                throw new IllegalStateException("componentType can not be null");
            }
            Method allMethod = componentType.getMethod("all");
            hList.setDescriptors((List<Descriptor<T>>) allMethod.invoke(null));
        } catch (Exception e) {
            throw new RuntimeException("caption:" + caption, e);
        }
        return hList;
    }
    // private JSONObject getItemJson(T i) throws IllegalAccessException {
    // Descriptor<T> descriptor;
    // JSONObject item;
    // JSONObject vals;
    // descriptor = i.getDescriptor();
    // 
    // item = new JSONObject();
    // item.put("impl", descriptor.getId());
    // 
    // vals = new JSONObject();
    // // Set<String> keys = descPropsMap.get(descriptor.getT());
    // try {
    // 
    // for (Map.Entry<String, Descriptor.PropertyType> entry : descriptor.getPropertyTypes().entrySet()) {
    // 
    // if (entry.getValue().isDescribable()) {
    // vals.put(entry.getKey(), getItemJson( i.getClass().getField(entry.getKey()).get(i)));
    // } else {
    // vals.put(entry.getKey(), i.getClass().getField(entry.getKey()).get(i));
    // }
    // }
    // } catch (NoSuchFieldException e) {
    // throw new RuntimeException(
    // "fetchKeys:" + descriptor.getPropertyTypes().keySet().stream().collect(Collectors.joining(","))
    // + "，hasKeys:" +
    // Arrays.stream(i.getClass().getFields()).map((r) -> r.getName()).collect(
    // Collectors.joining(",")), e);
    // }
    // 
    // item.put("vals", vals);
    // return item;
    // }
}
